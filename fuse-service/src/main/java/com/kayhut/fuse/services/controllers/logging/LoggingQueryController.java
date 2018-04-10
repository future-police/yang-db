package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.ExternalRequestId;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.services.suppliers.ExternalRequestIdSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryAndFetchRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.QueryController;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingQueryController implements QueryController {
    public static final String controllerParameter = "LoggingQueryController.@controller";
    public static final String loggerParameter = "LoggingQueryController.@logger";

    //region Constructors
    @Inject
    public LoggingQueryController(
            @Named(controllerParameter) QueryController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            ExternalRequestIdSupplier externalRequestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.externalRequestIdSupplier = externalRequestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), create.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(request.getId())).write();

        ContentResponse<QueryResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start create", LogType.of(start), create).log();
            response = this.controller.create(request);
            new LogMessage.Impl(this.logger, info, "finish create", LogType.of(success), create, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish create", LogType.of(success), create, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), "create", "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed create", LogType.of(failure), create, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .externalRequestId(this.externalRequestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryAndFetchRequest request) {
        boolean thrownException = false;

        try {
            this.logger.trace("start createAndFetch");
            return controller.createAndFetch(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed createAndFetch", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish createAndFetch");
            }
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfo.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestId.of(this.requestIdSupplier.get())).write();

        ContentResponse<StoreResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfo", LogType.of(start), getInfo).log();
            this.logger.trace("start getInfo");
            response = this.controller.getInfo();
            new LogMessage.Impl(this.logger, info, "finish getInfo", LogType.of(success), getInfo, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getInfo", LogType.of(success), getInfo, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfo.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getInfo", LogType.of(failure), getInfo, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfo.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .externalRequestId(this.externalRequestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryId.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(queryId)).write();

        ContentResponse<QueryResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryId", LogType.of(start), getInfoByQueryId).log();
            response = this.controller.getInfo(queryId);
            new LogMessage.Impl(this.logger, info, "finish getInfoByQueryId", LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryId", LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryId", LogType.of(failure), getInfoByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .externalRequestId(this.externalRequestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getV1ByQueryId.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(queryId)).write();

        ContentResponse<Query> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getV1ByQueryId", LogType.of(start), getV1ByQueryId).log();
            response = this.controller.getV1(queryId);
            new LogMessage.Impl(this.logger, info, "finish getV1ByQueryId", LogType.of(success), getV1ByQueryId, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getV1ByQueryId", LogType.of(success), getV1ByQueryId, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getV1ByQueryId.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getV1ByQueryId", LogType.of(failure), getV1ByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getV1ByQueryId.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .externalRequestId(this.externalRequestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }
    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getAsgByQueryId.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(queryId)).write();

        ContentResponse<AsgQuery> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getAsgByQueryId", LogType.of(start), getAsgByQueryId).log();
            response = this.controller.getAsg(queryId);
            new LogMessage.Impl(this.logger, info, "finish getAsgByQueryId", LogType.of(success), getAsgByQueryId, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getAsgByQueryId", LogType.of(success), getAsgByQueryId, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getAsgByQueryId.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getAsgByQueryId", LogType.of(failure), getAsgByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getAsgByQueryId.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .externalRequestId(this.externalRequestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.trace("start explain");
            return controller.explain(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed explain", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish explain");
            }
        }
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.trace("start planVerbose");
            return controller.planVerbose(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed planVerbose", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish planVerbose");
            }
        }
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), delete.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(queryId)).write();

        ContentResponse<Boolean> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start delete", LogType.of(start), delete).log();
            response = this.controller.delete(queryId);
            new LogMessage.Impl(this.logger, info, "finish delete", LogType.of(success), delete, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish delete", LogType.of(success), delete, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed delete", LogType.of(failure), delete, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .externalRequestId(this.externalRequestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }
    //endregion

    //region Fields
    private QueryController controller;
    private RequestIdSupplier requestIdSupplier;
    private ExternalRequestIdSupplier externalRequestIdSupplier;

    private Logger logger;
    private MetricRegistry metricRegistry;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter getInfo = MethodName.of("getInfo");
    private static MethodName.MDCWriter getV1ByQueryId = MethodName.of("getV1ByQueryId");
    private static MethodName.MDCWriter getAsgByQueryId = MethodName.of("getAsgByQueryId");
    private static MethodName.MDCWriter getInfoByQueryId = MethodName.of("getInfoByQueryId");
    private static MethodName.MDCWriter delete = MethodName.of("delete");
    //endregion
}
