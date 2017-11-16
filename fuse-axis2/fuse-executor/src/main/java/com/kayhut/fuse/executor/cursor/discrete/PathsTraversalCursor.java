package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.results.QueryResult.Builder.instance;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class PathsTraversalCursor implements Cursor {
    //region Constructors
    public PathsTraversalCursor(TraversalCursorContext context) {
        this.context = context;
        this.ont = new Ontology.Accessor(context.getOntology());
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResult getNextResults(int numResults) {
        return toQuery(numResults);
    }
    //endregion

    //region Properties
    public TraversalCursorContext getContext() {
        return context;
    }
    //endregion

    //region Private Methods
    private QueryResult toQuery(int numResults) {
        QueryResult.Builder builder = instance();
        builder.withPattern(context.getQueryResource().getQuery());
        //build assignments
        (context.getTraversal().next(numResults)).forEach(path -> {
            builder.withAssignment(toAssignment(path));
        });
        return builder.build();
    }

    private Assignment toAssignment(Path path) {
        Assignment.Builder builder = Assignment.Builder.instance();
        context.getQueryResource().getExecutionPlan().getPlan().getOps().forEach(planOp -> {
            if (planOp instanceof EntityOp) {
                EEntityBase entity = ((EntityOp)planOp).getAsgEBase().geteBase();

                if(entity instanceof EConcrete) {
                    builder.withEntity(toEntity(path, (EConcrete) entity));
                } else if(entity instanceof ETyped) {
                    builder.withEntity(toEntity(path, (ETyped) entity));
                } else if(entity instanceof EUntyped) {
                    builder.withEntity(toEntity(path, (EUntyped) entity));
                }
            } else if (planOp instanceof RelationOp) {
                RelationOp relationOp = (RelationOp)planOp;
                Optional<EntityOp> prevEntityOp =
                        PlanUtil.prev(this.context.getQueryResource().getExecutionPlan().getPlan(), planOp, EntityOp.class);
                Optional<EntityOp> nextEntityOp =
                        PlanUtil.next(this.context.getQueryResource().getExecutionPlan().getPlan(), planOp, EntityOp.class);

                builder.withRelationship(toRelationship(path,
                        prevEntityOp.get().getAsgEBase().geteBase(),
                        relationOp.getAsgEBase().geteBase(),
                        nextEntityOp.get().getAsgEBase().geteBase()));
            }
        });

        return builder.build();
    }

    private Entity toEntity(Path path, EUntyped element) {
        DiscreteVertex vertex = path.get(element.geteTag());

        String eType = vertex.label();
        List<Property> properties = Stream.ofAll(vertex::properties).map(this::toProperty).toJavaList();

        return toEntity(vertex.id().toString(),eType,element.geteTag(), properties);
    }

    private Entity toEntity(Path path, EConcrete element) {
        DiscreteVertex vertex = path.get(element.geteTag());
        List<Property> properties = Stream.ofAll(vertex::properties).map(this::toProperty).toJavaList();

        return toEntity(vertex.id().toString(),element.geteType(),element.geteTag(), properties);
    }

    private Entity toEntity(Path path, ETyped element) {
        DiscreteVertex vertex = path.get(element.geteTag());
        List<Property> properties = Stream.ofAll(vertex::properties).map(this::toProperty).toJavaList();

        return toEntity(vertex.id().toString(),element.geteType(),element.geteTag(), properties);
    }

    private Entity toEntity(String eId, String eType, String eTag, List<Property> properties) {
        Entity.Builder builder = Entity.Builder.instance();
        builder.withEID(eId);
        builder.withEType(eType);
        builder.withETag(Collections.singletonList(eTag));
        builder.withProperties(properties);
        return builder.build();
    }

    private Relationship toRelationship(Path path, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
        Relationship.Builder builder = Relationship.Builder.instance();
        DiscreteEdge edge = path.get(prevEntity.geteTag() + "-->" + nextEntity.geteTag());
        builder.withRID(edge.id().toString());
        builder.withRType(rel.getrType());

        switch (rel.getDir()) {
            case R:
                builder.withEID1(edge.outVertex().id().toString());
                builder.withEID2(edge.inVertex().id().toString());
                builder.withETag1(prevEntity.geteTag());
                builder.withETag2(nextEntity.geteTag());
                break;

            case L:
                builder.withEID1(edge.inVertex().id().toString());
                builder.withEID2(edge.outVertex().id().toString());
                builder.withETag1(nextEntity.geteTag());
                builder.withETag2(prevEntity.geteTag());
        }

        return builder.build();
    }

    private Property toProperty(VertexProperty vertexProperty) {
        return new Property(ont.property$(vertexProperty.key()).getpType(), "raw", vertexProperty.value());
    }
    //endregion

    //region Fields
    private TraversalCursorContext context;
    private Ontology.Accessor ont;
    //endregion
}