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
package org.springframework.samples.petclinic.repository.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of the {@link OwnerRepository} interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
public class JpaOwnerRepositoryImpl implements OwnerRepository {

    @PersistenceContext
    private EntityManager em;


    /**
     * Important: in the current version of this method, we load Owners with all their Pets and Visits while
     * we do not need Visits at all and we only need one property from the Pet objects (the 'name' property).
     * There are some ways to improve it such as:
     * - creating a Ligtweight class (example here: https://community.jboss.org/wiki/LightweightClass)
     * - Turning on lazy-loading and using open session in view pattern
     */
    @SuppressWarnings("unchecked")
    public Collection<Owner> findByLastName(String lastName) {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have pets yet
        Query query = this.em.createQuery("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName");
        query.setParameter("lastName", lastName + "%");
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Owner> findByLastName(String lastName, Pageable pageable) throws DataAccessException {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner WHERE owner.lastName LIKE :lastName ORDER BY owner.id");
        query.setParameter("lastName", lastName + "%");
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Owner> owners = query.getResultList();
        Query countQuery = this.em.createQuery("SELECT COUNT(owner) FROM Owner owner WHERE owner.lastName LIKE :lastName");
        countQuery.setParameter("lastName", lastName + "%");
        long total = (long) countQuery.getSingleResult();
        return new PageImpl<>(owners, pageable, total);
    }

    @Override
    public Owner findById(int id) {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have pets yet
        Query query = this.em.createQuery("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id");
        query.setParameter("id", id);
        return (Owner) query.getSingleResult();
    }


    @Override
    public void save(Owner owner) {
        if (owner.getId() == null) {
            this.em.persist(owner);
        } else {
            this.em.merge(owner);
        }

    }

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Owner> findAll() throws DataAccessException {
		Query query = this.em.createQuery("SELECT owner FROM Owner owner");
        return query.getResultList();
	}

    @SuppressWarnings("unchecked")
    @Override
    public Page<Owner> findAll(@NonNull Pageable pageable) throws DataAccessException {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner ORDER BY owner.id");
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Owner> owners = query.getResultList();
        Query countQuery = this.em.createQuery("SELECT COUNT(owner) FROM Owner owner");
        long total = (long) countQuery.getSingleResult();
        return new PageImpl<>(owners, pageable, total);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Owner> findAll(String lastName, String city, String telephone, Pageable pageable) throws DataAccessException {
        StringBuilder jpql = new StringBuilder("SELECT owner FROM Owner owner WHERE 1=1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(owner) FROM Owner owner WHERE 1=1");

        if (lastName != null && !lastName.isBlank()) {
            jpql.append(" AND owner.lastName LIKE :lastName");
            countJpql.append(" AND owner.lastName LIKE :lastName");
        }
        if (city != null && !city.isBlank()) {
            jpql.append(" AND owner.city LIKE :city");
            countJpql.append(" AND owner.city LIKE :city");
        }
        if (telephone != null && !telephone.isBlank()) {
            jpql.append(" AND owner.telephone LIKE :telephone");
            countJpql.append(" AND owner.telephone LIKE :telephone");
        }

        // Whitelist allowed sort properties
        Set<String> allowedSortProperties = Set.of("lastName", "city", "id");
        String orderBy = "owner.id";
        if (pageable.getSort().isSorted()) {
            String mapped = pageable.getSort().stream()
                .filter(order -> allowedSortProperties.contains(order.getProperty()))
                .map(order -> {
                    String prop = "id".equals(order.getProperty()) ? "owner.id"
                        : "lastName".equals(order.getProperty()) ? "owner.lastName" : "owner.city";
                    return prop + (order.isDescending() ? " DESC" : " ASC");
                })
                .collect(Collectors.joining(", "));
            if (!mapped.isEmpty()) {
                orderBy = mapped;
            }
        }
        jpql.append(" ORDER BY ").append(orderBy);

        Query query = this.em.createQuery(jpql.toString());
        Query countQuery = this.em.createQuery(countJpql.toString());

        if (lastName != null && !lastName.isBlank()) {
            query.setParameter("lastName", lastName + "%");
            countQuery.setParameter("lastName", lastName + "%");
        }
        if (city != null && !city.isBlank()) {
            query.setParameter("city", city + "%");
            countQuery.setParameter("city", city + "%");
        }
        if (telephone != null && !telephone.isBlank()) {
            query.setParameter("telephone", telephone + "%");
            countQuery.setParameter("telephone", telephone + "%");
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Owner> owners = query.getResultList();
        long total = (long) countQuery.getSingleResult();
        return new PageImpl<>(owners, pageable, total);
    }

	@Override
	public void delete(Owner owner) throws DataAccessException {
		this.em.remove(this.em.contains(owner) ? owner : this.em.merge(owner));
	}

}
