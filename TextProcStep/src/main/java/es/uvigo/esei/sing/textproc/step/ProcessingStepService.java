// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Specifies the contract of any processing step service, which allows it to
 * integrate with the application in an extensible manner.
 *
 * @author Alejandro González García
 * @see AbstractProcessingStep
 */
public interface ProcessingStepService {
	/**
	 * Returns a new instance of the processing step contained by this service,
	 * which implements {@code AbstractProcessingStep}. No unchecked exceptions can
	 * be thrown by this method.
	 *
	 * @return The described instance. It must not be {@code null}.
	 */
	public AbstractProcessingStep create();

	/**
	 * Returns the unique name of the processing step represented by this service.
	 * No unchecked exceptions can be thrown by this method.
	 *
	 * @return The described name. It must not be {@code null}.
	 */
	public String getName();

	/**
	 * Returns a set with every additional processing step parameter used by the
	 * processing step contained in this service. No unchecked exceptions can be
	 * thrown by this method.
	 *
	 * @return The described set. It can be empty and unmodifiable, but it must not
	 *         be {@code null}, and not contain {@code null} elements.
	 */
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters();

	/**
	 * Returns a set with every additional processed document types that the
	 * processing step contained in this service creates in the database. No
	 * unchecked exceptions can be thrown by this method.
	 *
	 * @return The described set. It can be empty and unmodifiable, but it must not
	 *         be {@code null}, and not contain {@code null} elements.
	 */
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes();
}
