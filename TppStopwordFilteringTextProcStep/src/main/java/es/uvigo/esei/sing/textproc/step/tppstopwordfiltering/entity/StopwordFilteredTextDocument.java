// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;

/**
 * Represents a stopword filtered text-only document.
 *
 * @author Alejandro González García
 */
@Entity
@Table(name = "stopword_filtered_text_document")
public final class StopwordFilteredTextDocument extends ProcessedDocument {
	/**
	 * Creates a new stopword filtered text-only document with the given parameters.
	 *
	 * @param id   The ID of the stopword filtered text-only document, that must
	 *             match the ID of a text-only document.
	 * @param text The stopword filtered text of the text-only document.
	 * @throws IllegalArgumentException If any argument is {@code null}.
	 */
	public StopwordFilteredTextDocument(final int id, final String text) {
		super(id, text);
	}

	/**
	 * Dummy constructor that only assigns a primary key. Intended for usage when
	 * the rest of the entity attributes are to be be assigned later.
	 *
	 * @param id The primary key of the entity.
	 * @throws IllegalArgumentException If {@code id} is {@code null}.
	 */
	public StopwordFilteredTextDocument(final Integer id) {
		super(id, "");
	}

	/**
	 * Dummy default constructor, to be used by JPA only.
	 */
	protected StopwordFilteredTextDocument() {}
}
