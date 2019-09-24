package com.yangdb.fuse.test.framework.index;

/*-
 * #%L
 * fuse-test-framework
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

import com.yangdb.fuse.client.elastic.BaseFuseElasticClient;
import com.yangdb.fuse.client.elastic.TransportFuseElasticClient;
import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static com.yangdb.fuse.test.framework.TestUtil.deleteFolder;


/**
 * Created by moti on 3/19/2017.
 */
public class ElasticEmbeddedNode implements AutoCloseable {

    //region PluginConfigurableNode Implementation
    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins, Path path, String nodeName) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, new HashMap<>(), path, () -> nodeName), classpathPlugins,false);
        }

//        @Override
//        protected void registerDerivedNodeNameWithLogger(String nodeName) {
//            LogConfigurator.setNodeName(nodeName);
//        }
    }
    //endregion

    //region Members
    private final int httpPort;
    private final String esWorkingDir;
    private final int numberOfShards;
    private Node node;

    static int httpTransportPort;
    static String nodeName;
    static BaseFuseElasticClient client = null;
    //endregion

    //region Constructors
    public ElasticEmbeddedNode(String clusterName) throws Exception {
        this("target/es", 9200, 9300, clusterName);
    }

    public ElasticEmbeddedNode(String clusterName, int numberOfShards) throws Exception {
        this("target/es", 9200, 9300, clusterName, numberOfShards);
    }

    public ElasticEmbeddedNode() throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic");
    }

    public ElasticEmbeddedNode(ElasticIndexConfigurer... configurers) throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic", configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, ElasticIndexConfigurer... configurers) throws Exception {
        this(esWorkingDir, httpPort, httpTransportPort, nodeName, 1, configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, int numberOfShards, ElasticIndexConfigurer... configurers) throws Exception {
        ElasticEmbeddedNode.httpTransportPort = httpTransportPort;
        ElasticEmbeddedNode.nodeName = nodeName;
        this.esWorkingDir = esWorkingDir;
        this.httpPort = httpPort;
        this.numberOfShards = numberOfShards;
        prepare();

        for (ElasticIndexConfigurer configurer : configurers) {
            configurer.configure(getClient(nodeName,httpTransportPort));
        }
    }

    //endregion

    //region Methods
    public static BaseFuseElasticClient getClient() {
        return getClient(nodeName,httpTransportPort);
    }

    public static BaseFuseElasticClient getClient(String nodeName,int httpTransportPort) {
        if (client == null) {
            try {
                Settings settings = Settings.builder()
                        .put("cluster.name", nodeName)
                        .build();
                client = new TransportFuseElasticClient(settings)
                        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
            } catch (Throwable e) {//catch (UnknownHostException e) {
                throw new UnknownError(e.getMessage());
            }
        }

        return client;
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing");
        closeClient();
        if (this.node != null) {
            this.node.close();
            this.node = null;
        }


        deleteFolder(esWorkingDir);
    }

    public static void closeClient() {
        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void prepare() throws Exception {
        this.close();

        Settings settings = Settings.builder()
                .put("cluster.name", nodeName)
                .put("path.home", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.cors.enabled", "true")
//                .put("script.auto_reload_enabled", "false")
                .put("transport.tcp.port", httpTransportPort)
                .build();

        this.node = new PluginConfigurableNode(settings, Arrays.asList(
                Netty4Plugin.class,
//                CommonScriptPlugin.class,
                CommonAnalysisPlugin.class
        ), Paths.get(esWorkingDir), nodeName);

        this.node = this.node.start();
    }
    //endregion
}
