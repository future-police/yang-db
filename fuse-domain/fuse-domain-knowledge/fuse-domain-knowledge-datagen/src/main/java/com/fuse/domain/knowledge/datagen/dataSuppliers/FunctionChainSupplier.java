package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class FunctionChainSupplier<TIn, TOut> implements Supplier<TOut> {
    //region Constructors
    public FunctionChainSupplier(Supplier<TIn> supplier, Function<TIn, TOut> function) {
        this.supplier = supplier;
        this.function = function;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public TOut get() {
        return this.function.apply(this.supplier.get());
    }
    //endregion

    //region Fields
    private Supplier<TIn> supplier;
    private Function<TIn, TOut> function;
    //endregion
}
