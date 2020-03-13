// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlplemmatization.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;

/**
 * Represents a lemmatized text-only document.
 *
 * @author Alejandro González García
 */
@Entity
@Table(name = "lemmatized_text_document")
public class CoreNLPLemmatizedTextDocument extends ProcessedDocument {
	/**
	 * Creates a new lemmatized text-only document with the given parameters.
	 *
	 * @param id   The ID of the lemmatized text-only document, that must match the
	 *             ID of a text-only document.
	 * @param text The lemmatized text of the text-only document, with tokens
	 *             separated by spaces.
	 * @throws IllegalArgumentException If any argument is {@code null}.
	 */
	public CoreNLPLemmatizedTextDocument(final int id, final String text) {
		super(id, text);
	}

	/**
	 * Dummy constructor that only assigns a primary key. Intended for usage when
	 * the rest of the entity attributes are to be be assigned later.
	 *
	 * @param id The primary key of the entity.
	 * @throws IllegalArgumentException If {@code id} is {@code null}.
	 */
	public CoreNLPLemmatizedTextDocument(final Integer id) {
		super(id, "");
	}

	/**
	 * Dummy default constructor, to be used by JPA only.
	 */
	protected CoreNLPLemmatizedTextDocument() {}
}
