package com.kayhut.fuse.services.engine2;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.engine2.data.CsvCursorTests;
import com.kayhut.fuse.services.engine2.data.JoinE2ETests;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.GlobalElasticEmbeddedNode;
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
        CsvCursorTests.class
})
public class CsvCursorTestSuite {

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("CsvCursorTestSuite start");
        start = System.currentTimeMillis();

        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine2.dev.M2.discrete.conf").toString()), "m2.smartEpb");

        app.start("server.join=false");
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (app != null) {
            app.stop();
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("CsvCursorTestSuite elapsed: " + elapsed);
    }

    //region Fields
    private static long start;
    private static Jooby app;
    public static ElasticEmbeddedNode elasticEmbeddedNode;
    //endregion
}