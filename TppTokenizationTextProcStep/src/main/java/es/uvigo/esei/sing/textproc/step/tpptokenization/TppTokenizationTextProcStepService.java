// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tpptokenization;

import java.util.Set;

import es.uvigo.esei.sing.textproc.abstracttppstep.AbstractTppTextProcStepService;
import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.tpptokenization.entity.TokenizedTextDocument;
import es.uvigo.esei.sing.textproc.step.tpptokenization.entity.TokenizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class TppTokenizationTextProcStepService extends AbstractTppTextProcStepService {
	@Override
	public AbstractProcessingStep create() {
		return new TppTokenizationTextProcStep();
	}

	@Override
	public String getName() {
		return "TppTokenization";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(TokenizedTextDocument.class, TokenizedTextWithTitleDocument.class);
	}

	@Override
	protected Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParticularParameters() {
		return Set.of();
	}
}
