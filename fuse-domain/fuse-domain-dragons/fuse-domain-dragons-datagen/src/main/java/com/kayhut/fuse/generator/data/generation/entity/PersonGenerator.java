package com.kayhut.fuse.generator.data.generation.entity;

/*-
 * #%L
 * fuse-domain-gragons-datagen
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

import com.github.javafaker.Name;
import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.data.generation.other.PropertiesGenerator;
import com.kayhut.fuse.generator.model.entity.Person;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class PersonGenerator extends EntityGeneratorBase<PersonConfiguration, Person> {

    public PersonGenerator(PersonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Person generate() {
        Name fakeName = faker.name();
        Date birthDate = RandomUtil.randomDate(configuration.getStartDateOfStory(), configuration.getEndDateOfStory());
        long lifeExpectancy = Math.round(RandomUtil.randomGaussianNumber(configuration.getLifeExpectancyMean(), configuration.getLifeExpectancySD()));
        Date deathDate = DateUtil.addYearsToDate(birthDate, (int) lifeExpectancy);
        long height = Math.max(1, Math.round(RandomUtil.randomGaussianNumber(configuration.getHeightMean(), configuration.getHeightSD())));

        return Person.Builder.get()
                .withFirstName(fakeName.firstName())
                .withLastName(fakeName.lastName())
                .withGender(PropertiesGenerator.generateGender())
                .withBirthDate(birthDate)
                .withDeathDate(deathDate)
                .withHeight((int) height)
                .build();
    }
}
