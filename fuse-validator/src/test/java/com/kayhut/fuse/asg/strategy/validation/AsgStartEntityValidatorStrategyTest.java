package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.ValidationContext;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.relProp;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.unTyped;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by liorp on 6/6/2017.
 */
public class AsgStartEntityValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(unTyped(1))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
            .next(unTyped(3))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgStartEntityValidatorStrategy strategy = new AsgStartEntityValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testNameMismatchQuery() {
        AsgStartEntityValidatorStrategy strategy = new AsgStartEntityValidatorStrategy();
        query.setOnt("bela");
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertEquals(validationContext.errors()[0],AsgStartEntityValidatorStrategy.ERROR_1);
    }

    @Test
    public void testStartOnlyElementQuery() {
        AsgStartEntityValidatorStrategy strategy = new AsgStartEntityValidatorStrategy();
        ValidationContext validationContext = strategy.apply(AsgQuery.Builder.start("Q1", "Dragon").build(), new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertEquals(validationContext.errors()[0],AsgStartEntityValidatorStrategy.ERROR_2);
    }
}
