// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.mentionfiltering.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;

/**
 * Represents a mention filtered text-only document.
 *
 * @author Alejandro González García
 */
@Entity
@Table(name = "mention_filtered_text_document")
public final class MentionFilteredTextDocument extends ProcessedDocument {
	/**
	 * Creates a new mention filtered text-only document with the given parameters.
	 *
	 * @param id   The ID of the mention filtered text-only document, that must
	 *             match the ID of a text-only document.
	 * @param text The mention filtered text of the text-only document.
	 * @throws IllegalArgumentException If any argument is {@code null}.
	 */
	public MentionFilteredTextDocument(final int id, final String text) {
		super(id, text);
	}

	/**
	 * Dummy constructor that only assigns a primary key. Intended for usage when
	 * the rest of the entity attributes are to be be assigned later.
	 *
	 * @param id The primary key of the entity.
	 * @throws IllegalArgumentException If {@code id} is {@code null}.
	 */
	public MentionFilteredTextDocument(final Integer id) {
		super(id, "");
	}

	/**
	 * Dummy default constructor, to be used by JPA only.
	 */
	protected MentionFilteredTextDocument() {}
}
