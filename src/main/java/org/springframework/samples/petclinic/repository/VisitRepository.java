/*
 * Copyright 2002-2017 the original author or authors.
 *
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
 */
package org.springframework.samples.petclinic.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Visit;

/**
 * Repository class for <code>Visit</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
public interface VisitRepository {

    /**
     * Save a <code>Visit</code> to the data store, either inserting or updating it.
     *
     * @param visit the <code>Visit</code> to save
     * @see BaseEntity#isNew
     */
    void save(Visit visit) throws DataAccessException;

    List<Visit> findByPetId(Integer petId);

    /**
     * Retrieve <code>Visit</code>s from the data store, optionally filtered by pet and visit date
     * range, ordered by visit date descending.
     *
     * @param petId     the pet id to filter by, or <code>null</code> for any pet
     * @param startDate the inclusive lower bound for the visit date, or <code>null</code> for no lower bound
     * @param endDate   the inclusive upper bound for the visit date, or <code>null</code> for no upper bound
     * @return a <code>List</code> of matching <code>Visit</code>s (empty if none match)
     */
    List<Visit> findVisits(Integer petId, LocalDate startDate, LocalDate endDate) throws DataAccessException;

	Visit findById(int id) throws DataAccessException;
	
	Collection<Visit> findAll() throws DataAccessException;

	void delete(Visit visit) throws DataAccessException;

}
