// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tpptokenization;

import java.util.Set;

import es.uvigo.esei.sing.textproc.abstracttppstep.AbstractTppProcessingStepService;
import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.tpptokenization.entity.TppTokenizedTextDocument;
import es.uvigo.esei.sing.textproc.step.tpptokenization.entity.TppTokenizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class TppTokenizationProcessingStepService extends AbstractTppProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		return new TppTokenizationProcessingStep();
	}

	@Override
	public String getName() {
		return "TppTokenization";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(TppTokenizedTextDocument.class, TppTokenizedTextWithTitleDocument.class);
	}

	@Override
	protected Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParticularParameters() {
		return Set.of();
	}
}
