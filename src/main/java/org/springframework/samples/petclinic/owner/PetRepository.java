/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Pets on their own, for the service layer the realm reads through. A pet does not map
 * its owner, so which owner holds which pet is read from the owner's side.
 */
interface PetRepository extends JpaRepository<Pet, Integer>, JpaSpecificationExecutor<Pet> {

	record Ownership(Integer ownerId, Integer petId) {
	}

	@Query("select new org.springframework.samples.petclinic.owner.PetRepository$Ownership(o.id, p.id) "
			+ "from Owner o join o.pets p where o.id in :ownerIds")
	List<Ownership> findOwnershipByOwnerIds(@Param("ownerIds") Collection<Integer> ownerIds);

	@Query("select new org.springframework.samples.petclinic.owner.PetRepository$Ownership(o.id, p.id) "
			+ "from Owner o join o.pets p where p.id in :petIds")
	List<Ownership> findOwnershipByPetIds(@Param("petIds") Collection<Integer> petIds);

}
