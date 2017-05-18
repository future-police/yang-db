package com.kayhut.fuse.asg.strategy.PropertiesGrouping;
import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
/**
 * Created by benishue on 24-Apr-17.
 */
public class AsgPropsGroupingStrategyTest {
    //This Eprop is not under an AND quantifier and should be replaced by the EPropGroup Element -  e.g. Q3 on V1
    @Test
    public void simpleCaseGroupTest() throws Exception {
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q1");
        List<EBase> elements = new ArrayList<EBase>();
        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);
        ETyped eTypedA = new ETyped();
        eTypedA.seteNum(1);
        eTypedA.seteTag("A");
        eTypedA.seteType(1);
        eTypedA.setNext(2);
        elements.add(eTypedA);
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        elements.add(rel);
        ETyped eTypedB = new ETyped();
        eTypedB.seteNum(3);
        eTypedB.seteTag("B");
        eTypedB.seteType(2);
        eTypedB.setNext(4);
        elements.add(eTypedB);
        EProp eProp = new EProp();
        eProp.seteNum(4);
        eProp.setpType("1.1");
        eProp.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("dragonA");
        eProp.setCon(con);
        elements.add(eProp);
        query.setElements(elements);
        //endregion
        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        AsgEBase<EBase> originalEPropAsgEbase = AsgQueryUtil.getElement(asgQuery, 4).get();
        AsgEntityPropertiesGroupingStrategy asgEntityPropertiesGroupingStrategy = new AsgEntityPropertiesGroupingStrategy();
        asgEntityPropertiesGroupingStrategy.apply(asgQuery, new AsgStrategyContext());
        //Checking that the ASG query still hold - nothing has broken
        assertEquals(0, asgQuery.getStart().geteBase().geteNum());
        assertEquals(0,asgQuery.getStart().getNext().get(0).getParents().get(0).geteBase().geteNum());
        AsgEBase<? extends EBase> asgEBase1 = asgQuery.getStart().getNext().get(0);
        assertEquals(asgEBase1.geteBase().geteNum(),1);
        //Checking the EProp grouping mechanism
        AsgEBase<EBase> newEPropGroupAsgEbase = AsgQueryUtil.getElement(asgQuery, 4).get();
        assertNotNull(newEPropGroupAsgEbase);
        assertEquals(3, newEPropGroupAsgEbase.getParents().get(0).geteNum());
        assertEquals(0, newEPropGroupAsgEbase.getNext().size());
        assertEquals(originalEPropAsgEbase.geteNum(), newEPropGroupAsgEbase.geteNum());
        assertTrue(((EPropGroup)newEPropGroupAsgEbase.geteBase()).geteProps().contains(originalEPropAsgEbase.geteBase()));
        assertTrue(((EPropGroup)newEPropGroupAsgEbase.geteBase()).geteProps().size() == 1);
    }
    // Query with an AND Quantifier where all his children are Eprop Type -  e.g., Q27 - 2
    @Test
    public void andQuantifierWithAllEPropsTest() throws Exception {
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q1");
        List<EBase> elements = new ArrayList<EBase>();
        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);
        ETyped eTypedA = new ETyped();
        eTypedA.seteNum(1);
        eTypedA.seteTag("A");
        eTypedA.seteType(1);
        eTypedA.setNext(2);
        elements.add(eTypedA);
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        elements.add(rel);
        ETyped eTypedB = new ETyped();
        eTypedB.seteNum(3);
        eTypedB.seteTag("B");
        eTypedB.seteType(2);
        eTypedB.setNext(4);
        elements.add(eTypedB);
        Quant1 quant = new Quant1();
        quant.seteNum(4);
        quant.setqType(QuantType.all);
        quant.setNext(Arrays.asList(5,6));
        elements.add(quant);
        //Entity Property  -> Dragon Name = Black
        EProp eProp1 = new EProp();
        eProp1.seteNum(5);
        eProp1.setpType("1.1");
        eProp1.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("dragonA");
        eProp1.setCon(con);
        elements.add(eProp1);
        //Entity Property  -> Dragon Color = Black
        EProp eProp2 = new EProp();
        eProp2.seteNum(6);
        eProp2.setpType("3");
        eProp2.setpTag("2");
        Constraint con1 = new Constraint();
        con1.setOp(ConstraintOp.eq);
        con1.setExpr("black");
        eProp2.setCon(con);
        elements.add(eProp2);
        query.setElements(elements);
        //endregion
        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        AsgEBase<EBase> originalEProp1AsgEbase = AsgQueryUtil.getElement(asgQuery, 5).get();
        AsgEBase<EBase> originalEProp2AsgEbase = AsgQueryUtil.getElement(asgQuery, 6).get();
        AsgQuant1PropertiesGroupingStrategy asgQuant1PropertiesGroupingStrategy = new AsgQuant1PropertiesGroupingStrategy();
        asgQuant1PropertiesGroupingStrategy.apply(asgQuery, new AsgStrategyContext());
        //Checking that the ASG query still hold - nothing has broken
        assertEquals(0, asgQuery.getStart().geteBase().geteNum());
        assertEquals(0,asgQuery.getStart().getNext().get(0).getParents().get(0).geteBase().geteNum());
        AsgEBase<? extends EBase> asgEBase1 = asgQuery.getStart().getNext().get(0);
        assertEquals(asgEBase1.geteBase().geteNum(),1);
        //Checking the EProp grouping mechanism
        AsgEBase<EBase> newEPropGroupAsgEbase = AsgQueryUtil.getElement(asgQuery, 5).get();
        assertNotNull(newEPropGroupAsgEbase);
        assertEquals(EPropGroup.class, newEPropGroupAsgEbase.geteBase().getClass());
        assertFalse(AsgQueryUtil.getElement(asgQuery, 6).isPresent()); // Eprop with eNum=6 Should be removed from the query
        //AND Quantifier eNum = 4
        assertEquals(4, newEPropGroupAsgEbase.getParents().get(0).geteNum());
        assertEquals(0, newEPropGroupAsgEbase.getNext().size());
        //Checking that our EPropGroup Contains the original EProps
        assertTrue(((EPropGroup)newEPropGroupAsgEbase.geteBase()).geteProps().contains(originalEProp1AsgEbase.geteBase()));
        assertTrue(((EPropGroup)newEPropGroupAsgEbase.geteBase()).geteProps().contains(originalEProp2AsgEbase.geteBase()));
        //eProp List of size = 2 in EPropGroup element
        assertTrue(((EPropGroup)newEPropGroupAsgEbase.geteBase()).geteProps().size() == 2);
        assertEquals(AsgQueryUtil.getElement(asgQuery,4).get().getNext().get(0).geteNum(), newEPropGroupAsgEbase.geteNum());
    }
    @Test
    public void vQuantifierWithEPropsChildrenGroupingTest() throws Exception{
        //region Query Building
        Query query = new Query(); //Q3 - 2
        query.setOnt("Dragons");
        query.setName("Q3-2");
        List<EBase> elements = new ArrayList<EBase>();
        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);
        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);
        eTyped.setNext(2);
        elements.add(eTyped);
        Quant1 quant1 = new Quant1();
        quant1.seteNum(2);
        quant1.setqType(QuantType.all);
        quant1.setNext(Arrays.asList(3,4));
        elements.add(quant1);
        EProp eProp = new EProp();
        eProp.seteNum(3);
        eProp.setpType("1.1");
        eProp.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("Brandon");
        eProp.setCon(con);
        elements.add(eProp);
        Rel rel1 = new Rel();
        rel1.seteNum(4);
        rel1.setrType(1);
        rel1.setDir(Rel.Direction.R);
        rel1.setNext(5);
        elements.add(rel1);
        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(5);
        eTyped2.seteTag("B");
        eTyped2.seteType(2);
        elements.add(eTyped2);
        query.setElements(elements);
        //endregion
        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        AsgEBase<EBase> originalEPropAsgEbase = AsgQueryUtil.getElement(asgQuery, 3).get();
        AsgQuant1PropertiesGroupingStrategy asgQuant1PropertiesGroupingStrategy = new AsgQuant1PropertiesGroupingStrategy();
        asgQuant1PropertiesGroupingStrategy.apply(asgQuery, new AsgStrategyContext());
        //Checking that the ASG query still hold - nothing has broken
        assertEquals(0, asgQuery.getStart().geteBase().geteNum());
        assertEquals(0,asgQuery.getStart().getNext().get(0).getParents().get(0).geteBase().geteNum());
        AsgEBase<? extends EBase> asgEBase1 = asgQuery.getStart().getNext().get(0);
        assertEquals(asgEBase1.geteBase().geteNum(),1);
        //Checking the EProp grouping mechanism
        AsgEBase<EBase> newEPropGroupAsgEbase = AsgQueryUtil.getElement(asgQuery, 3).get();
        assertNotNull(newEPropGroupAsgEbase);
        assertEquals(EPropGroup.class, newEPropGroupAsgEbase.geteBase().getClass());
        //AND Quantifier eNum = 2
        assertEquals(2, newEPropGroupAsgEbase.getParents().get(0).geteNum());
        assertEquals(0, newEPropGroupAsgEbase.getNext().size());
        assertTrue(((EPropGroup)newEPropGroupAsgEbase.geteBase()).geteProps().contains(originalEPropAsgEbase.geteBase()));
        //eProp List of size = 2 in EPropGroup
        assertTrue(((EPropGroup)newEPropGroupAsgEbase.geteBase()).geteProps().size() == 1);
        //AND Quantifier eNum = 2 has 2 children: 1 Eprop and 1 ETyped(enum=5)
        List<AsgEBase<? extends EBase>> nextChildren = AsgQueryUtil.getElement(asgQuery, 2).get().getNext();
        AsgEBase<EBase> rel4AsgEBase = AsgQueryUtil.getElement(asgQuery, 4).get();
        assertTrue(nextChildren.contains(rel4AsgEBase));
        assertTrue(rel4AsgEBase.getParents().get(0).getNext().contains(newEPropGroupAsgEbase));
    }
    @Test
    public void relPropsGroupingTest() throws Exception{
        AsgQuery asgQuery = AsgQueryStore.Q188_V1();
        AsgEBase<EBase> originalRelProp1AsgEbase = AsgQueryUtil.getElement(asgQuery, 4).get();
        AsgEBase<EBase> originalRelProp2AsgEbase = AsgQueryUtil.getElement(asgQuery, 5).get();
        AsgRelPropertiesGroupingStrategy asgRelPropertiesGroupingStrategy = new AsgRelPropertiesGroupingStrategy();
        asgRelPropertiesGroupingStrategy.apply(asgQuery, new AsgStrategyContext());
        //Checking that the ASG query still hold - nothing has broken
        assertEquals(0, asgQuery.getStart().geteBase().geteNum());
        assertEquals(0,asgQuery.getStart().getNext().get(0).getParents().get(0).geteBase().geteNum());
        AsgEBase<? extends EBase> asgEBase1 = asgQuery.getStart().getNext().get(0);
        assertEquals(asgEBase1.geteBase().geteNum(),1);
        //Checking the RelProps grouping mechanism
        AsgEBase<EBase> newRelPropGroupAsgEbase = AsgQueryUtil.getElement(asgQuery, 4).get();
        assertNotNull(newRelPropGroupAsgEbase);
        assertEquals(RelPropGroup.class, newRelPropGroupAsgEbase.geteBase().getClass());
        assertFalse(AsgQueryUtil.getElement(asgQuery, 5).isPresent()); // Relprop with eNum=5 Should be removed from the query
        assertEquals(2, newRelPropGroupAsgEbase.getParents().get(0).geteNum());
        assertEquals(0, newRelPropGroupAsgEbase.getB().size());
        //Checking that our RelPropGroup Contains the original EProps
        assertTrue(((RelPropGroup)newRelPropGroupAsgEbase.geteBase()).getrProps().contains(originalRelProp1AsgEbase.geteBase()));
        assertTrue(((RelPropGroup)newRelPropGroupAsgEbase.geteBase()).getrProps().contains(originalRelProp2AsgEbase.geteBase()));
    }
    @Test
    public void hQuantifierWithRelPropsGroupingTest() throws Exception{
        AsgQuery asgQuery = AsgQueryStore.Q187_V1();
        AsgEBase<EBase> originalRelProp1AsgEbase = AsgQueryUtil.getElement(asgQuery, 5).get();
        AsgEBase<EBase> originalRelProp2AsgEbase = AsgQueryUtil.getElement(asgQuery, 6).get();
        AsgHQuantifierPropertiesGroupingStrategy asgHQuantifierPropertiesGroupingStrategy = new AsgHQuantifierPropertiesGroupingStrategy();
        asgHQuantifierPropertiesGroupingStrategy.apply(asgQuery, new AsgStrategyContext());
        //Checking that the ASG query still hold - nothing has broken
        assertEquals(0, asgQuery.getStart().geteBase().geteNum());
        assertEquals(0,asgQuery.getStart().getNext().get(0).getParents().get(0).geteBase().geteNum());
        AsgEBase<? extends EBase> asgEBase1 = asgQuery.getStart().getNext().get(0);
        assertEquals(asgEBase1.geteBase().geteNum(),1);
        //Checking the RelProps grouping mechanism
        List<AsgEBase<RelPropGroup>> relPropGroups = AsgQueryUtil.getElements(asgQuery, RelPropGroup.class);
        assertTrue(relPropGroups.size() == 2);
        AsgEBase<EBase> newRelPropGroup1AsgEbase = AsgQueryUtil.getElement(asgQuery, 5).get();
        assertNotNull(newRelPropGroup1AsgEbase);
        assertEquals(RelPropGroup.class, newRelPropGroup1AsgEbase.geteBase().getClass());
        assertEquals(4, newRelPropGroup1AsgEbase.getParents().get(0).geteNum());
        assertEquals(0, newRelPropGroup1AsgEbase.getB().size());
        AsgEBase<EBase> newRelPropGroup2AsgEbase = AsgQueryUtil.getElement(asgQuery, 6).get();
        assertNotNull(newRelPropGroup2AsgEbase);
        assertEquals(RelPropGroup.class, newRelPropGroup2AsgEbase.geteBase().getClass());
        assertEquals(4, newRelPropGroup2AsgEbase.getParents().get(0).geteNum());
        assertEquals(0, newRelPropGroup2AsgEbase.getB().size());
        //Checking that our RelPropGroup Contains the original EProps
        assertTrue(((RelPropGroup)newRelPropGroup1AsgEbase.geteBase()).getrProps().contains(originalRelProp1AsgEbase.geteBase()));
        assertTrue(((RelPropGroup)newRelPropGroup2AsgEbase.geteBase()).getrProps().contains(originalRelProp2AsgEbase.geteBase()));
    }
}