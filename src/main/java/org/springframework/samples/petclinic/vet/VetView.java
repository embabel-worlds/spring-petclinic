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
package org.springframework.samples.petclinic.vet;

import java.util.List;

import com.embabel.publisher.annotation.RealmType;
import org.springframework.samples.petclinic.model.NamedEntity;

/**
 * A {@link Vet} as Embabel worlds see it, with the names of the vet's specialties.
 */
@RealmType(name = "Vet", entity = Vet.class,
		cache = @RealmType.Cache(value = RealmType.Scope.SHARED, ttlSeconds = 3600))
public record VetView(@RealmType.Id Integer id, String firstName, String lastName, List<String> specialties) {

	static VetView of(Vet vet) {
		return new VetView(vet.getId(), vet.getFirstName(), vet.getLastName(),
				vet.getSpecialties().stream().map(NamedEntity::getName).toList());
	}

}
