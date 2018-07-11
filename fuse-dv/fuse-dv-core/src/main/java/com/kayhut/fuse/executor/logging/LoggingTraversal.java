package com.kayhut.fuse.executor.logging;

import com.kayhut.fuse.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;

import java.util.List;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingTraversal<S, E> implements Traversal<S, E> {
    //region Constructors
    public LoggingTraversal(
            Traversal<S, E> traversal,
            Logger logger) {
        this.traversal = traversal;
        this.logger = logger;
    }
    //endregion

    //region GraphTraversal Implementation
    @Override
    public boolean hasNext() {
        return this.traversal.hasNext();
    }

    @Override
    public E next() {
        return this.traversal.next();
    }

    @Override
    public List<E> next(int amount) {
        boolean thrownExcpetion = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start next", sequence, LogType.of(start), next, ElapsedFrom.now()).log();
            return this.traversal.next(amount);
        } catch (Exception ex) {
            thrownExcpetion = true;
            new LogMessage.Impl(this.logger, error, "failed next", sequence, LogType.of(failure), next, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                new LogMessage.Impl(this.logger, trace, "finish next", sequence, LogType.of(success), next, ElapsedFrom.now()).log();
            }
        }
    }

    @Override
    public List<E> toList() {
        boolean thrownExcpetion = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start toList", sequence, LogType.of(start), toList, ElapsedFrom.now()).log();
            return this.traversal.toList();
        } catch (Exception ex) {
            thrownExcpetion = true;
            new LogMessage.Impl(this.logger, error, "failed toList", sequence, LogType.of(failure), toList, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                new LogMessage.Impl(this.logger, trace, "finish toList", sequence, LogType.of(success), toList, ElapsedFrom.now()).log();
            }
        }
    }
    //endregion

    //region Fields
    private Traversal<S, E> traversal;
    private Logger logger;

    private static LogMessage.MDCWriter next = MethodName.of("next");
    private static LogMessage.MDCWriter toList = MethodName.of("toList");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}