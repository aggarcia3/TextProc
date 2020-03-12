// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tpptokenization.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocumentWithTitle;

/**
 * Represents a tokenized text document which has a title.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is not thread-safe.
 */
@Entity
@Table(name = "tokenized_text_with_title_document")
public class TokenizedTextWithTitleDocument extends ProcessedDocumentWithTitle {
	/**
	 * Creates a new tokenized text document which has a title with the given
	 * parameters.
	 *
	 * @param id    The ID of the tokenized text document with title, that must
	 *              match the ID of a text document which has a title.
	 * @param text  The tokenized text of the document, with tokens separated by
	 *              spaces.
	 * @param title The tokenized title of the text document.
	 * @throws IllegalArgumentException If any argument is {@code null}.
	 */
	public TokenizedTextWithTitleDocument(final int id, final String text, final String title) {
		super(id, text, title);
	}

	/**
	 * Dummy constructor that only assigns a primary key. Intended for usage when
	 * the rest of the entity attributes are to be be assigned later.
	 *
	 * @param id The primary key of the entity.
	 * @throws IllegalArgumentException If {@code id} is {@code null}.
	 */
	public TokenizedTextWithTitleDocument(final Integer id) {
		super(id);
	}

	/**
	 * Dummy default constructor, to be used by JPA only.
	 */
	protected TokenizedTextWithTitleDocument() {}
}
