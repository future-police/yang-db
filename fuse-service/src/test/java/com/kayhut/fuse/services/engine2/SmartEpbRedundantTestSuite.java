package com.kayhut.fuse.services.engine2;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.engine2.data.DfsRedundantEntityRelationEntityTest;
import com.kayhut.fuse.services.engine2.data.SmartEpbRedundantEntityRelationEntityTest;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SmartEpbRedundantEntityRelationEntityTest.class
})
public class SmartEpbRedundantTestSuite {
    @BeforeClass
    public static void setup() throws Exception {
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine2.dev.conf").toString()), "m1.smartEpb");

        app.start("server.join=false");
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (app != null) {
            app.stop();
        }

        if (elasticEmbeddedNode != null) {
            if (elasticEmbeddedNode.getClient() != null) {
                elasticEmbeddedNode.getClient().close();
            }

            elasticEmbeddedNode.close();
        }
    }

    //region Fields
    private static Jooby app;
    public static ElasticEmbeddedNode elasticEmbeddedNode;
    //endregion
}