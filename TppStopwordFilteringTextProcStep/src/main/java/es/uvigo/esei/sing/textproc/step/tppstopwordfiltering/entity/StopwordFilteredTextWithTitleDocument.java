// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocumentWithTitle;

/**
 * Represents a stopword filtered text document which has a title.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is not thread-safe.
 */
@Entity
@Table(name = "stopword_filtered_text_with_title_document")
public class StopwordFilteredTextWithTitleDocument extends ProcessedDocumentWithTitle {
	/**
	 * Creates a new stopword filtered text document which has a title with the
	 * given parameters.
	 *
	 * @param id    The ID of the stopword filtered text document with title, that
	 *              must match the ID of a text document which has a title.
	 * @param text  The stopword filtered text of the document.
	 * @param title The stopword filtered title of the text document.
	 * @throws IllegalArgumentException If any argument is {@code null}.
	 */
	public StopwordFilteredTextWithTitleDocument(final int id, final String text, final String title) {
		super(id, text, title);
	}

	/**
	 * Dummy constructor that only assigns a primary key. Intended for usage when
	 * the rest of the entity attributes are to be be assigned later.
	 *
	 * @param id The primary key of the entity.
	 * @throws IllegalArgumentException If {@code id} is {@code null}.
	 */
	public StopwordFilteredTextWithTitleDocument(final Integer id) {
		super(id);
	}

	/**
	 * Dummy default constructor, to be used by JPA only.
	 */
	protected StopwordFilteredTextWithTitleDocument() {}
}
