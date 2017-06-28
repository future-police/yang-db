package com.kayhut.fuse.services.engine2;

/**
 * Created by Roman on 21/06/2017.
 */

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.FuseRunner;
import com.kayhut.fuse.services.engine2.data.DfsNonRedundantEntityRelationEntityTest;
import com.kayhut.fuse.services.engine2.data.PromiseEdgeTest;
import com.kayhut.fuse.services.engine2.data.SingleEntityTest;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.nio.file.Paths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CursorTest.class,
        PageTest.class,
        QueryTest.class,
        SingleEntityTest.class,
        PromiseEdgeTest.class,
        DfsNonRedundantEntityRelationEntityTest.class
})
public class NonRedundantTestSuite {
    @BeforeClass
    public static void setup() throws Exception {
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine2.dev.conf").toString()),
                        "m1.dfs.non_redundant");

        new FuseRunner().run(app, new FuseRunner.Options(Paths.get("src", "test", "conf", "logback.xml").toString(), false));
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