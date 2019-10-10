package com.yangdb.fuse.generator.data.generation.entity;

/*-
 *
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.github.javafaker.Faker;


/**
 * Created by benishue on 15-May-17.
 */

/**
 * @param <C> - Configuration Per Entity
 * @param <E> - Entity (e.g., Dragon, Person)
 */
public abstract class EntityGeneratorBase<C, E> {

    //region Ctrs
    public EntityGeneratorBase(C configuration) {
        this.faker = new Faker();
        this.configuration = configuration;
    }
    //endregion

    //region Abstract Methods
    public abstract E generate();
    //endregion

    //region Fields
    protected final Faker faker;
    protected final C configuration;
    //endregion

}
