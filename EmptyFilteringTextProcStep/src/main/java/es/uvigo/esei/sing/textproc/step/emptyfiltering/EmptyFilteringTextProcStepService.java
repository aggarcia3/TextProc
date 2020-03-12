// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.emptyfiltering;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.emptyfiltering.entity.NonEmptyTextDocument;
import es.uvigo.esei.sing.textproc.step.emptyfiltering.entity.NonEmptyTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.emptyfiltering.xml.definition.IncludeRedditDeletedProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class EmptyFilteringTextProcStepService implements ProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new EmptyFilteringTextProcStep();
	}

	@Override
	public String getName() {
		return "EmptyFiltering";
	}

	@Override
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		return Set.of(IncludeRedditDeletedProcessingStepParameter.class);
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(NonEmptyTextDocument.class, NonEmptyTextWithTitleDocument.class);
	}
}
