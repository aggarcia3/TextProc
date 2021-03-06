// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.mentionfiltering;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.mentionfiltering.entity.MentionFilteredTextDocument;
import es.uvigo.esei.sing.textproc.step.mentionfiltering.entity.MentionFilteredTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.mentionfiltering.xml.definition.MentionTypeProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class MentionFilteringProcessingStepService implements ProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new MentionFilteringProcessingStep();
	}

	@Override
	public String getName() {
		return "MentionFiltering";
	}

	@Override
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		return Set.of(MentionTypeProcessingStepParameter.class);
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(MentionFilteredTextDocument.class, MentionFilteredTextWithTitleDocument.class);
	}
}
