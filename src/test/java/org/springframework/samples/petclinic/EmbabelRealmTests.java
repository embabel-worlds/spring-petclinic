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
package org.springframework.samples.petclinic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The clinic as an Embabel realm: the manifest, a filtered lookup and the world's
 * credential.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "embabel.realm.auth.token=test-token")
class EmbabelRealmTests {

	@LocalServerPort
	int port;

	private final HttpClient http = HttpClient.newHttpClient();

	@Test
	void publishesTheAgreedTypesAndVerbs() throws Exception {
		HttpResponse<String> manifest = send(HttpRequest.newBuilder(uri("")).GET(), "test-token");
		assertThat(manifest.statusCode()).isEqualTo(200);
		assertThat(manifest.body()).contains("\"name\":\"petclinic\"", "\"name\":\"Owner\"", "\"name\":\"Pet\"",
				"\"name\":\"Visit\"", "\"name\":\"Vet\"", "\"name\":\"addVisit\"", "\"name\":\"updateOwnerContact\"");
	}

	@Test
	void fetchesPetsByOwnerWithTheFilterApplied() throws Exception {
		String request = """
				{ "type": "Pet", "by": "ownerId", "keys": ["1", "6", "99"],
				  "where": { "property": "type", "op": "in", "value": ["cat", "dog"] } }
				""";
		HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/fetch"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(request)), "test-token");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"name\":\"Leo\"", "\"name\":\"Samantha\"", "\"name\":\"Max\"",
				"\"missing\":[\"99\"]", "\"applied\":true");
	}

	@Test
	void refusesACallWithoutTheWorldsCredential() throws Exception {
		HttpResponse<String> response = send(HttpRequest.newBuilder(uri("")).GET(), "wrong");
		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("UNAUTHENTICATED").doesNotContain("Owner");
	}

	private URI uri(String path) {
		return URI.create("http://localhost:" + this.port + "/embabel" + path);
	}

	private HttpResponse<String> send(HttpRequest.Builder request, String token) throws Exception {
		return this.http.send(request.header("Authorization", "Bearer " + token).build(),
				HttpResponse.BodyHandlers.ofString());
	}

}
