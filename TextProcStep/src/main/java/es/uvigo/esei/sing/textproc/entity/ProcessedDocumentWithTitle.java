// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents a processed text document which has a title.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is not thread-safe.
 */
@MappedSuperclass
public abstract class ProcessedDocumentWithTitle extends ProcessedDocument {
	@Getter @Setter @NonNull @Column(nullable = false)
	private String title;

	/**
	 * Creates a new processed text document which has a title with the given
	 * parameters.
	 *
	 * @param id    The ID of the processed text document with title, that must
	 *              match the ID of a text document which has a title.
	 * @param text  The processed text of the document, with tokens separated by
	 *              spaces.
	 * @param title The processed title of the text document.
	 * @throws IllegalArgumentException If any parameter is {@code null}.
	 */
	protected ProcessedDocumentWithTitle(final int id, final String text, @NonNull final String title) {
		super(id, text);
		this.title = title;
	}

	/**
	 * Dummy constructor that only assigns a primary key. Intended for usage when
	 * the rest of the entity attributes are to be be assigned later.
	 *
	 * @param id The primary key of the entity.
	 * @throws IllegalArgumentException If {@code id} is {@code null}.
	 */
	protected ProcessedDocumentWithTitle(@NonNull final Integer id) {
		super(id, "");
		this.title = "";
	}

	/**
	 * Dummy default constructor, to be used by JPA only.
	 */
	protected ProcessedDocumentWithTitle() {
		this.title = "";
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder(super.toString());

		stringBuilder
			.append('\n').append("Title: ").append(title);

		return stringBuilder.toString();
	}
}
