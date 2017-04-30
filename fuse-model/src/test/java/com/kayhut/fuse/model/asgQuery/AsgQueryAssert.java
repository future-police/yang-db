package com.kayhut.fuse.model.asgQuery;

import com.kayhut.fuse.model.query.QueryAssert;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.quant.Quant1;
import org.junit.Assert;

/**
 * Created by Roman on 25/04/2017.
 */
public class AsgQueryAssert {
    public static void assertEquals(AsgEBase asgEBase1, AsgEBase asgEBase2) {
        if (asgEBase1 == null) {
            Assert.assertTrue(asgEBase2 == null);
        }

        Assert.assertTrue(asgEBase1 != null && asgEBase2 != null);
        Assert.assertEquals(asgEBase1.geteNum(), asgEBase2.geteNum());

        if (asgEBase1.geteBase() == null) {
            Assert.assertTrue(asgEBase2.geteBase() == null);
        }

        Assert.assertTrue(asgEBase1.geteBase() != null && asgEBase2.geteBase() != null);

        Assert.assertEquals(asgEBase1.geteBase().getClass(), asgEBase2.geteBase().getClass());
        Assert.assertEquals(asgEBase1.geteBase().geteNum(), asgEBase2.geteBase().geteNum());

        if (asgEBase1.geteBase() instanceof EConcrete) {
            QueryAssert.assertEquals((EConcrete)asgEBase1.geteBase(), (EConcrete)asgEBase2.geteBase());
        } else if (asgEBase1.geteBase() instanceof ETyped) {
            QueryAssert.assertEquals((ETyped)asgEBase1.geteBase(), (ETyped)asgEBase2.geteBase());
        } else if (asgEBase1.geteBase() instanceof EUntyped) {
            QueryAssert.assertEquals((EUntyped)asgEBase1.geteBase(), (EUntyped)asgEBase2.geteBase());
        } else if (asgEBase1.geteBase() instanceof Rel) {
            QueryAssert.assertEquals((Rel)asgEBase1.geteBase(), (Rel)asgEBase2.geteBase());
        } else if (asgEBase1.geteBase() instanceof Quant1) {
            QueryAssert.assertEquals((Quant1)asgEBase1.geteBase(), (Quant1)asgEBase2.geteBase());
        }
    }
}