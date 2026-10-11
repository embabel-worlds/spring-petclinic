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

import com.embabel.publisher.annotation.RealmType;
import com.embabel.publisher.where.Op;
import com.fasterxml.jackson.annotation.JsonClassDescription;

/**
 * An {@link Owner} as Embabel worlds see it: the owner's own fields, without their pets,
 * which are a type of their own.
 */
@RealmType(name = "Owner", entity = Owner.class,
		cache = @RealmType.Cache(value = RealmType.Scope.SHARED, ttlSeconds = 60, negativeTtlSeconds = 300))
@JsonClassDescription("A pet owner registered with the clinic.")
public record OwnerView(@RealmType.Id Integer id, String firstName, @RealmType.Filterable( {
		Op.EQ, Op.IN, Op.PREFIX, Op.IEQ, Op.IPREFIX }) @RealmType.Sortable String lastName, String address,
		@RealmType.Filterable({ Op.EQ, Op.IN }) @RealmType.Sortable String city, String telephone){

	static OwnerView of(Owner owner) {
		return new OwnerView(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getAddress(),
				owner.getCity(), owner.getTelephone());
	}

}
