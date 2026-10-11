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

import java.time.LocalDate;

import com.embabel.publisher.annotation.RealmType;
import com.embabel.publisher.where.Op;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * A {@link Pet} as Embabel worlds see it. The owner is a reference by id; the pet's
 * visits are a type of their own.
 */
@RealmType(name = "Pet", entity = Pet.class, cache = @RealmType.Cache(value = RealmType.Scope.SHARED, ttlSeconds = 60))
public record PetView(@RealmType.Id Integer id, String name, @RealmType.Filterable( {
		Op.GT, Op.GTE, Op.LT, Op.LTE, Op.BETWEEN }) LocalDate birthDate,
		@RealmType.Path("type.name") @RealmType.Filterable({ Op.EQ,
				Op.IN }) @JsonPropertyDescription("cat, dog, lizard, snake, bird or hamster") String type,
		@RealmType.Reference(type = OwnerView.class, relationship = "OWNS",
				direction = RealmType.Direction.IN) Integer ownerId){

	static PetView of(Pet pet, Integer ownerId) {
		return new PetView(pet.getId(), pet.getName(), pet.getBirthDate(),
				pet.getType() == null ? null : pet.getType().getName(), ownerId);
	}

}
