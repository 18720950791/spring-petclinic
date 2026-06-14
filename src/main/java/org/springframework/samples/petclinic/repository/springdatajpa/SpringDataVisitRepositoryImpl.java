/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.repository.springdatajpa;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Visit;

/**
 * @author Vitaliy Fedoriv
 *
 */

@Profile("spring-data-jpa")
public class SpringDataVisitRepositoryImpl implements VisitRepositoryOverride {

	@PersistenceContext
    private EntityManager em;

	@Override
	@SuppressWarnings("unchecked")
	public List<Visit> findVisits(Integer petId, LocalDate startDate, LocalDate endDate) throws DataAccessException {
        Query query = this.em.createQuery(
            "SELECT v FROM Visit v "
                + "WHERE (:petId IS NULL OR v.pet.id = :petId) "
                + "AND (:startDate IS NULL OR v.date >= :startDate) "
                + "AND (:endDate IS NULL OR v.date <= :endDate) "
                + "ORDER BY v.date DESC");
        query.setParameter("petId", petId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
	}

	@Override
	public void delete(Visit visit) throws DataAccessException {
		String visitId = visit.getId().toString();
		this.em.createQuery("DELETE FROM Visit visit WHERE id=" + visitId).executeUpdate();
        if (em.contains(visit)) {
            em.remove(visit);
        }
	}


}
