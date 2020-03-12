// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents the common contract to all processed documents
 * <p>
 * Non-abstract subclasses of this class must define a public constructor with a
 * single {@link Integer} parameter (the primitive type is not allowed), which
 * is the primary key of the entity, that must initialize any other field to
 * dummy values.
 * <p>
 * They also must define a protected no-argument constructor for JPA.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is not thread-safe.
 */
@MappedSuperclass
@AllArgsConstructor
public abstract class ProcessedDocument {
	@NonNull @Getter @Id
	private Integer id;
	@NonNull @Column(nullable = false) @Getter @Setter
	private String text;

	/**
	 * Dummy default constructor, to be used by JPA only.
	 */
	protected ProcessedDocument() {
		this.id = Integer.MIN_VALUE;
		this.text = "";
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder("- ");

		stringBuilder.append(getClass().getSimpleName())
			.append('\n').append("ID: ").append(id)
			.append('\n').append("Text: ").append(text);

		return stringBuilder.toString();
	}
}
