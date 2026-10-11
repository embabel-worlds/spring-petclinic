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

import com.embabel.publisher.annotation.RealmLookup;
import com.embabel.publisher.annotation.RealmQuery;
import com.embabel.publisher.annotation.RealmVerb;
import com.embabel.publisher.where.Op;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owners, for Embabel worlds. The controllers keep calling the repository directly; this
 * is the service layer the realm publishes through, so transactions and validation apply
 * to every call.
 */
@Service
@Transactional(readOnly = true)
public class OwnerService {

	private final OwnerRepository owners;

	OwnerService(OwnerRepository owners) {
		this.owners = owners;
	}

	@RealmLookup(by = "id", maxKeys = 500)
	public List<OwnerView> byIds(Collection<Integer> ids) {
		return this.owners.findAllById(ids).stream().map(OwnerView::of).toList();
	}

	/**
	 * What the find-owners screen searches by: the start of a last name. Null finds every
	 * owner.
	 */
	public record OwnerSearch(@RealmQuery.Criterion(property = "lastName", op = Op.PREFIX) String lastNamePrefix) {
	}

	/**
	 * The find-owners search, through the repository method the screen already uses.
	 */
	@RealmQuery(maxPageSize = 200)
	public Page<OwnerView> search(OwnerSearch search, Pageable pageable) {
		String prefix = (search.lastNamePrefix() != null) ? search.lastNamePrefix() : "";
		return this.owners.findByLastNameStartingWith(prefix, pageable).map(OwnerView::of);
	}

	/**
	 * A change to an owner's contact details. Fields left out are left as they are.
	 */
	public record ContactChange(@NotNull Integer ownerId, String address, String city, String telephone) {
	}

	@RealmVerb(name = "updateOwnerContact", description = "Change an owner's address, city or telephone.",
			subject = OwnerView.class, effect = RealmVerb.Effect.WRITE, idempotent = true,
			approval = @RealmVerb.Approval(required = RealmVerb.Required.ALWAYS,
					reason = "Contact details are changed only with a second person's approval."))
	@Transactional
	public void updateContact(ContactChange change) {
		Owner owner = this.owners.findById(change.ownerId())
			.orElseThrow(() -> new IllegalArgumentException("No owner with id " + change.ownerId()));
		if (change.address() != null) {
			owner.setAddress(change.address());
		}
		if (change.city() != null) {
			owner.setCity(change.city());
		}
		if (change.telephone() != null) {
			owner.setTelephone(change.telephone());
		}
		/*
		 * Flushed here, so the entity's own constraints refuse a bad telephone inside
		 * this call.
		 */
		this.owners.saveAndFlush(owner);
	}

}
