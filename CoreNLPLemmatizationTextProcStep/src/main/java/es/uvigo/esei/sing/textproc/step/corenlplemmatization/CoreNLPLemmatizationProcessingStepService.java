// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlplemmatization;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.corenlplemmatization.entity.CoreNLPLemmatizedTextDocument;
import es.uvigo.esei.sing.textproc.step.corenlplemmatization.entity.CoreNLPLemmatizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.corenlplemmatization.xml.definition.ModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class CoreNLPLemmatizationProcessingStepService implements ProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new CoreNLPLemmatizationProcessingStep();
	}

	@Override
	public String getName() {
		return "CoreNLPLemmatization";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(CoreNLPLemmatizedTextDocument.class, CoreNLPLemmatizedTextWithTitleDocument.class);
	}

	@Override
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		return Set.of(ModelProcessingStepParameter.class);
	}
}
