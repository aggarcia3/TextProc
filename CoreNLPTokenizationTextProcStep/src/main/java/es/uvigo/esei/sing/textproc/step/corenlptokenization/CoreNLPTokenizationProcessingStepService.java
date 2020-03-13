// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlptokenization;

import java.util.Set;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.entity.CoreNLPTokenizedTextDocument;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.entity.CoreNLPTokenizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.xml.definition.LanguageProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlptokenization.xml.definition.TokenizerOptionsProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class CoreNLPTokenizationProcessingStepService implements ProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new CoreNLPTokenizationProcessingStep();
	}

	@Override
	public String getName() {
		return "CoreNLPTokenization";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(CoreNLPTokenizedTextDocument.class, CoreNLPTokenizedTextWithTitleDocument.class);
	}

	@Override
	public Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParameters() {
		return Set.of(LanguageProcessingStepParameter.class, TokenizerOptionsProcessingStepParameter.class);
	}
}
