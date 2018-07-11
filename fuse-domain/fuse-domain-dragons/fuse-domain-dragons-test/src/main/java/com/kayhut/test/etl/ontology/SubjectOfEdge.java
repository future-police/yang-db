package com.kayhut.test.etl.ontology;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.test.etl.*;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.model.execution.plan.Direction.out;
import static com.kayhut.test.scenario.ETLUtils.*;

/**
 * Created by moti on 6/7/2017.
 */
public interface SubjectOfEdge {


    static void main(String args[]) throws IOException {

        // FREEZE
        // Add sideB type
        // redundant field
        // dup + add direction
        Map<String, String> outConstFields=  new HashMap<>();
        outConstFields.put(ENTITY_A_TYPE, PERSON);
        outConstFields.put(ENTITY_B_TYPE, KINGDOM);
        AddConstantFieldsTransformer outFieldsTransformer = new AddConstantFieldsTransformer(outConstFields, out);
        Map<String, String> inConstFields=  new HashMap<>();
        inConstFields.put(ENTITY_A_TYPE, KINGDOM);
        inConstFields.put(ENTITY_B_TYPE, PERSON);
        AddConstantFieldsTransformer inFieldsTransformer = new AddConstantFieldsTransformer(inConstFields, Direction.in);

        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(getClient(),
                redundant(SUBJECT_OF_KINGDOM, out, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                PERSON,
                redundant(SUBJECT_OF_KINGDOM, out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                KINGDOM,
                out.name());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(getClient(),
                redundant(SUBJECT_OF_KINGDOM,  Direction.in, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                KINGDOM,
                redundant(SUBJECT_OF_KINGDOM, Direction.in,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                PERSON, Direction.in.name());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(SINCE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, SUBJECT_OF_KINGDOM);
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                duplicateEdgeTransformer,
                outFieldsTransformer,
                inFieldsTransformer,
                redundantOutTransformer,
                redundantInTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\kingdomsRelations_SUBJECT_OF_PERSON.csv",
                "C:\\demo_data_6June2017\\kingdomsRelations_SUBJECT_OF_PERSON-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_A_ID, ENTITY_B_ID,  SINCE),
                5000);
        transformer.transform();

    }

}