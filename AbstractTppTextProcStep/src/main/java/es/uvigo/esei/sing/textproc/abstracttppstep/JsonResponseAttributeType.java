// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.abstracttppstep;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Enumerates all the applicable JSON types for an attribute that are to be
 * found in a Text Processing Python response.
 *
 * @author Alejandro González García
 */
public enum JsonResponseAttributeType {
	/**
	 * A JSON array of strings.
	 */
	STRING_ARRAY(List.of("")),
	/**
	 * A JSON string.
	 */
	STRING("");

	@Getter(AccessLevel.PACKAGE) @NonNull
	private final JsonObject dummyValue;

	/**
	 * Instantiates a new enum constant. This method is to be invoked by the JVM
	 * only.
	 *
	 * @param value The value to put in the dummy JSON value.
	 */
	private JsonResponseAttributeType(@NonNull final Object value) {
		this.dummyValue = Json.createObjectBuilder(Map.of("text", value)).build();
	}
}