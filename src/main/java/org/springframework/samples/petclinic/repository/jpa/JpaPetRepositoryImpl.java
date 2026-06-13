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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of the {@link PetRepository} interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
public class JpaPetRepositoryImpl implements PetRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<PetType> findPetTypes() {
        return this.em.createQuery("SELECT ptype FROM PetType ptype ORDER BY ptype.name").getResultList();
    }

    @Override
    public Pet findById(int id) {
        return this.em.find(Pet.class, id);
    }

    @Override
    public void save(Pet pet) {
        if (pet.getId() == null) {
            this.em.persist(pet);
        } else {
            this.em.merge(pet);
        }
    }

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Pet> findAll() throws DataAccessException {
		return this.em.createQuery("SELECT pet FROM Pet pet").getResultList();
	}

    @Override
    @SuppressWarnings("unchecked")
    public Page<Pet> findPets(String name, String typeName, Integer ownerId, Pageable pageable) throws DataAccessException {
        StringBuilder jpql = new StringBuilder("SELECT pet FROM Pet pet WHERE 1=1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(pet) FROM Pet pet WHERE 1=1");

        if (name != null) {
            jpql.append(" AND pet.name LIKE CONCAT(:name, '%')");
            countJpql.append(" AND pet.name LIKE CONCAT(:name, '%')");
        }
        if (typeName != null) {
            jpql.append(" AND pet.type.name = :typeName");
            countJpql.append(" AND pet.type.name = :typeName");
        }
        if (ownerId != null) {
            jpql.append(" AND pet.owner.id = :ownerId");
            countJpql.append(" AND pet.owner.id = :ownerId");
        }
        jpql.append(" ORDER BY pet.id");

        Query query = this.em.createQuery(jpql.toString());
        Query countQuery = this.em.createQuery(countJpql.toString());

        if (name != null) {
            query.setParameter("name", name);
            countQuery.setParameter("name", name);
        }
        if (typeName != null) {
            query.setParameter("typeName", typeName);
            countQuery.setParameter("typeName", typeName);
        }
        if (ownerId != null) {
            query.setParameter("ownerId", ownerId);
            countQuery.setParameter("ownerId", ownerId);
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Pet> pets = query.getResultList();
        long total = (long) countQuery.getSingleResult();
        return new PageImpl<>(pets, pageable, total);
    }

	@Override
	public void delete(Pet pet) throws DataAccessException {
		//this.em.remove(this.em.contains(pet) ? pet : this.em.merge(pet));
		String petId = pet.getId().toString();
		this.em.createQuery("DELETE FROM Visit visit WHERE pet.id=" + petId).executeUpdate();
		this.em.createQuery("DELETE FROM Pet pet WHERE id=" + petId).executeUpdate();
		if (em.contains(pet)) {
			em.remove(pet);
		}
	}

}
