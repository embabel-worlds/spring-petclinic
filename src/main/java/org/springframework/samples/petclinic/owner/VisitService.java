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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.embabel.publisher.annotation.RealmLookup;
import com.embabel.publisher.annotation.RealmVerb;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Visits, for Embabel worlds: by id and by pet, and booking a new one. A pet has a
 * handful of visits, so the publisher applies the world's filter to what the lookups
 * return.
 */
@Service
@Transactional(readOnly = true)
public class VisitService {

	private final VisitRepository visits;

	private final PetRepository pets;

	VisitService(VisitRepository visits, PetRepository pets) {
		this.visits = visits;
		this.pets = pets;
	}

	@RealmLookup(by = "id", maxKeys = 500)
	public List<VisitView> byIds(Collection<Integer> ids, Sort sort) {
		return views(ids, sort, petOfVisit(this.visits.findAttendanceByVisitIds(ids)));
	}

	@RealmLookup(by = "petId", maxKeys = 200)
	public List<VisitView> byPets(Collection<Integer> petIds, Sort sort) {
		Map<Integer, Integer> petOfVisit = petOfVisit(this.visits.findAttendanceByPetIds(petIds));
		return views(petOfVisit.keySet(), sort, petOfVisit);
	}

	private List<VisitView> views(Collection<Integer> visitIds, Sort sort, Map<Integer, Integer> petOfVisit) {
		if (visitIds.isEmpty()) {
			return List.of();
		}
		return this.visits.findByIdIn(visitIds, sort)
			.stream()
			.map(visit -> VisitView.of(visit, petOfVisit.get(visit.getId())))
			.toList();
	}

	private static Map<Integer, Integer> petOfVisit(List<VisitRepository.Attendance> attendance) {
		return attendance.stream()
			.collect(Collectors.toMap(VisitRepository.Attendance::visitId, VisitRepository.Attendance::petId));
	}

	/**
	 * A visit to book.
	 */
	public record NewVisit(@NotNull Integer petId, @NotNull LocalDate date, @NotBlank String description) {
	}

	/**
	 * The visit that was booked.
	 */
	public record VisitAdded(Integer visitId) {
	}

	@RealmVerb(description = "Record a visit for a pet.", subject = PetView.class, effect = RealmVerb.Effect.WRITE,
			approval = @RealmVerb.Approval(required = RealmVerb.Required.NEVER))
	@Transactional
	public VisitAdded addVisit(NewVisit request) {
		/*
		 * The same rule the booking form applies: a visit is booked for a day after
		 * today.
		 */
		if (!request.date().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("A visit is booked for a day after today");
		}
		Pet pet = this.pets.findById(request.petId())
			.orElseThrow(() -> new IllegalArgumentException("No pet with id " + request.petId()));
		Visit visit = new Visit();
		visit.setDate(request.date());
		visit.setDescription(request.description());
		pet.addVisit(visit);
		this.pets.flush();
		return new VisitAdded(visit.getId());
	}

}
