// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tpplemmatization;

import java.util.Set;

import es.uvigo.esei.sing.textproc.abstracttppstep.AbstractTppProcessingStepService;
import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.tpplemmatization.TppLemmatizationProcessingStep.AddModelParameterAction;
import es.uvigo.esei.sing.textproc.step.tpplemmatization.entity.LemmatizedTextDocument;
import es.uvigo.esei.sing.textproc.step.tpplemmatization.entity.LemmatizedTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.tpplemmatization.xml.definition.ModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class TppLemmatizationProcessingStepService extends AbstractTppProcessingStepService {
	@Override
	public AbstractProcessingStep create() {
		final AddModelParameterAction requestParametersAction = new AddModelParameterAction();
		final TppLemmatizationProcessingStep serviceStep = new TppLemmatizationProcessingStep(requestParametersAction);

		requestParametersAction.setStep(serviceStep);

		return serviceStep;
	}

	@Override
	public String getName() {
		return "TppLemmatization";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(LemmatizedTextDocument.class, LemmatizedTextWithTitleDocument.class);
	}

	@Override
	protected Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParticularParameters() {
		return Set.of(ModelProcessingStepParameter.class);
	}
}
