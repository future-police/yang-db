package com.kayhut.fuse.dispatcher.epb;

import com.kayhut.fuse.model.validation.QueryValidation;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanValidator<P, Q>{
    QueryValidation isPlanValid(P plan, Q query);
}
