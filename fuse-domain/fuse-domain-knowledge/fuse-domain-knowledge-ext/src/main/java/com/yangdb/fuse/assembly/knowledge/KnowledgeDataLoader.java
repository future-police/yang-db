package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeContext;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeTransformer;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.RvalueBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;
import com.yangdb.fuse.executor.ontology.schema.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.LoadResponse;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.collection.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext.commit;

/**
 * Created by lior.perry on 2/11/2018.
 */
public class KnowledgeDataLoader implements GraphDataLoader<String, FuseError> {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDataLoader.class);

    private Client client;
    private SimpleDateFormat sdf;
    private RawSchema schema;
    private KnowledgeTransformer transformer;
    private ObjectMapper mapper;

    @Inject
    public KnowledgeDataLoader(Config config, Client client, RawSchema schema, OntologyTransformerProvider transformerProvider, KnowledgeIdGenerator idGenerator) {
        this.schema = schema;
        this.mapper = new ObjectMapper();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        //load knowledge transformer
        final Optional<OntologyTransformer> assembly = transformerProvider.transformer(config.getString("assembly"));
        if (!assembly.isPresent())
            throw new IllegalArgumentException("No transformer provider found for selected ontology " + config.getString("assembly"));
        this.transformer = new KnowledgeTransformer(assembly.get(), schema, idGenerator, client);
        this.client = client;
    }


    public long init() throws IOException {
        String workingDir = System.getProperty("user.dir");
        File templates = Paths.get(workingDir, "indexTemplates").toFile();
        File[] templateFiles = templates.listFiles();
        if (templateFiles != null) {
            for (File templateFile : templateFiles) {
                String templateName = FilenameUtils.getBaseName(templateFile.getName());
                String template = FileUtils.readFileToString(templateFile, "utf-8");
                if (!client.admin().indices().getTemplates(new GetIndexTemplatesRequest(templateName)).actionGet().getIndexTemplates().isEmpty()) {
                    final AcknowledgedResponse acknowledgedResponse = client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(templateName)).actionGet(1500);
                    if (!acknowledgedResponse.isAcknowledged()) return -1;
                }
                final AcknowledgedResponse acknowledgedResponse = client.admin().indices().putTemplate(new PutIndexTemplateRequest(templateName).source(template, XContentType.JSON)).actionGet(1500);
                if (!acknowledgedResponse.isAcknowledged()) return -1;
            }
        }

        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices().create(new CreateIndexRequest(index)).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());
    }

    @Override
    public long drop() throws IOException {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    /**
     * transform json graph and load all data to designated indices according to schema
     *
     * @param root graph document
     * @return
     */
    public LoadResponse<String, FuseError> load(LogicalGraphModel root) throws JsonProcessingException {
        final KnowledgeContext context = transformer.transform(root);
        List<String> success = new ArrayList<>();
        success.add("Entities:"+context.getEntities().size());
        success.add("Relations:"+context.getRelations().size());
        success.add("eValues:"+context.geteValues().size());
        success.add("rValues:"+context.getrValues().size());

        KnowledgeWriterContext.Response transformationFailed = new KnowledgeWriterContext.Response("logicalTransformation")
                .success(success).failure(context.getFailed());

        //load all data to designated indices according to schema
        return commit(client, schema, mapper, context)
                .response(transformationFailed);
    }

    @Override
    public LoadResponse<String, FuseError> load(File data) throws IOException {
        String contentType = Files.probeContentType(data.toPath());
        if (Arrays.asList("application/gzip", "application/zip").contains(contentType)) {
            ByteArrayOutputStream stream = null; //unzip
            switch (contentType) {
                case "application/gzip":
                    stream = extractFile(new GZIPInputStream(Files.newInputStream(data.toPath())));
                    break;
                case "application/zip":
                    stream = extractFile(new ZipInputStream(Files.newInputStream(data.toPath())));
                    break;
            }

            String graph = new String(stream.toByteArray());
            return load(mapper.readValue(graph, LogicalGraphModel.class));
        }
        String graph = new String(Files.readAllBytes(data.toPath()));
        //read
        LogicalGraphModel root = mapper.readValue(graph, LogicalGraphModel.class);
        return load(root);
    }

    private ByteArrayOutputStream extractFile(InflaterInputStream zipIn) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
        return stream;
    }

}
