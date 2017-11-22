package com.kayhut.fuse.unipop.predicates;

import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.function.BiPredicate;

/**
 * Created by roman.margolis on 16/11/2017.
 */
public class ExistsP<V> extends P<V> {
    //region Constructors
    public ExistsP() {
        super(null, null);
    }
    //endregion

    //region Override Methods
    @Override
    public P<V> negate() {
        return new NotExistsP<V>();
    }

    @Override
    public String toString() {
        return "exists";
    }

    @Override
    public int hashCode() {
        return "exists".hashCode();
    }
    //endregion
}