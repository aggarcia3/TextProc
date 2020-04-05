// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.luceneindex;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.luceneindex.xml.definition.FolderPathProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class LuceneIndexProcessingStepService implements ProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new LuceneIndexProcessingStep();
	}

	@Override
	public String getName() {
		return "LuceneIndex";
	}

	@Override
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		return Set.of(FolderPathProcessingStepParameter.class);
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of();
	}
}
