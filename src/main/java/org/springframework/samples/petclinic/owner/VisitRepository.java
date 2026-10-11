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
 * Visits on their own, for the service layer the realm reads through. A visit does not
 * map its pet, so which pet had which visit is read from the pet's side.
 */
interface VisitRepository extends JpaRepository<Visit, Integer>, JpaSpecificationExecutor<Visit> {

	record Attendance(Integer petId, Integer visitId) {
	}

	@Query("select new org.springframework.samples.petclinic.owner.VisitRepository$Attendance(p.id, v.id) "
			+ "from Pet p join p.visits v where p.id in :petIds")
	List<Attendance> findAttendanceByPetIds(@Param("petIds") Collection<Integer> petIds);

	@Query("select new org.springframework.samples.petclinic.owner.VisitRepository$Attendance(p.id, v.id) "
			+ "from Pet p join p.visits v where v.id in :visitIds")
	List<Attendance> findAttendanceByVisitIds(@Param("visitIds") Collection<Integer> visitIds);

}
