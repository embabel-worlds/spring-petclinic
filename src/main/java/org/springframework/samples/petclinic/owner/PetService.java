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
import java.util.Map;
import java.util.stream.Collectors;

import com.embabel.publisher.annotation.RealmLookup;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pets, for Embabel worlds: by id, and by owner, each with the world's filter applied in
 * the database.
 */
@Service
@Transactional(readOnly = true)
public class PetService {

	private final PetRepository pets;

	PetService(PetRepository pets) {
		this.pets = pets;
	}

	@RealmLookup(by = "id", maxKeys = 500)
	public List<PetView> byIds(Collection<Integer> ids, Specification<Pet> where) {
		Map<Integer, Integer> ownerOfPet = ownerOfPet(this.pets.findOwnershipByPetIds(ids));
		return views(ids, where, ownerOfPet);
	}

	@RealmLookup(by = "ownerId", maxKeys = 200)
	public List<PetView> byOwners(Collection<Integer> ownerIds, Specification<Pet> where) {
		Map<Integer, Integer> ownerOfPet = ownerOfPet(this.pets.findOwnershipByOwnerIds(ownerIds));
		return views(ownerOfPet.keySet(), where, ownerOfPet);
	}

	private List<PetView> views(Collection<Integer> petIds, Specification<Pet> where,
			Map<Integer, Integer> ownerOfPet) {
		if (petIds.isEmpty()) {
			return List.of();
		}
		Specification<Pet> idIn = (root, query, cb) -> root.get("id").in(petIds);
		return this.pets.findAll(idIn.and(where))
			.stream()
			.map(pet -> PetView.of(pet, ownerOfPet.get(pet.getId())))
			.toList();
	}

	private static Map<Integer, Integer> ownerOfPet(List<PetRepository.Ownership> ownership) {
		return ownership.stream()
			.collect(Collectors.toMap(PetRepository.Ownership::petId, PetRepository.Ownership::ownerId));
	}

}
