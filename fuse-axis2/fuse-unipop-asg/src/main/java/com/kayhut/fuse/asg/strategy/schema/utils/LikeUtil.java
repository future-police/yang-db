package com.kayhut.fuse.asg.strategy.schema.utils;

import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.SchematicEProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.exact;
import static com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.ngrams;

/**
 * Created by roman.margolis on 07/03/2018.
 */
public class LikeUtil {
    public static Iterable<EProp> applyWildcardRules(EProp eProp, GraphElementPropertySchema propertySchema) {
        Optional<GraphElementPropertySchema.ExactIndexingSchema> exactIndexingSchema = propertySchema.getIndexingSchema(exact);

        String expr = (String) eProp.getCon().getExpr();
        if (expr == null || expr.equals("")) {
            return Collections.singletonList(new SchematicEProp(
                    eProp.geteNum(),
                    eProp.getpType(),
                    exactIndexingSchema.get().getName(),
                    Constraint.of(ConstraintOp.eq, eProp.getCon().getExpr())));
        }

        List<String> wildcardParts = Stream.of(expr.split("\\*")).filter(part -> !part.equals("")).toJavaList();

        boolean prefix = !expr.startsWith("*");
        boolean suffix = !expr.endsWith("*");
        boolean exact = prefix && suffix && wildcardParts.size() == 1;

        if (exact) {
            return Collections.singletonList(new SchematicEProp(
                    eProp.geteNum(),
                    eProp.getpType(),
                    exactIndexingSchema.get().getName(),
                    Constraint.of(ConstraintOp.eq, eProp.getCon().getExpr())));
        }

        List<EProp> newEprops = new ArrayList<>();
        for (int wildcardPartIndex = 0; wildcardPartIndex < wildcardParts.size(); wildcardPartIndex++) {
            String wildcardPart = wildcardParts.get(wildcardPartIndex);

            if (wildcardPartIndex == 0 && prefix) {
                newEprops.add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, wildcardParts.get(0) + "*")));

            } else if (wildcardPartIndex == wildcardParts.size() - 1 && suffix) {
                newEprops.add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, "*" + wildcardParts.get(wildcardParts.size() - 1))));

            } else if (ngramsApplicable(eProp, propertySchema, wildcardPart)) {
                newEprops.add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        propertySchema.getIndexingSchema(ngrams).get().getName(),
                        Constraint.of(ConstraintOp.eq, wildcardPart)));

            } else {
                newEprops.add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, "*" + wildcardParts.get(wildcardPartIndex) + "*")));
            }
        }

        return newEprops;
    }

    private static boolean ngramsApplicable(EProp eProp, GraphElementPropertySchema propertySchema, String wildcardPart) {
        Optional<GraphElementPropertySchema.NgramsIndexingSchema> ngramsIndexingSchema = propertySchema.getIndexingSchema(ngrams);

        if (!wildcardPart.contains(" ") &&
                ngramsIndexingSchema.isPresent() &&
                wildcardPart.length() <= (ngramsIndexingSchema.get()).getMaxSize()) {
            return true;
        }

        return false;
    }
}