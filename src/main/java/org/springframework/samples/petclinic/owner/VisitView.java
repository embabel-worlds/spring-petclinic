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

/**
 * A {@link Visit} as Embabel worlds see it, with the pet it was for.
 */
@RealmType(name = "Visit", entity = Visit.class)
public record VisitView(@RealmType.Id Integer id, @RealmType.Filterable( {
		Op.GT, Op.GTE, Op.LT, Op.LTE, Op.BETWEEN }) @RealmType.Sortable LocalDate date, String description,
		@RealmType.Reference(type = PetView.class, relationship = "HAD_VISIT",
				direction = RealmType.Direction.IN) Integer petId){

	static VisitView of(Visit visit, Integer petId) {
		return new VisitView(visit.getId(), visit.getDate(), visit.getDescription(), petId);
	}

}
