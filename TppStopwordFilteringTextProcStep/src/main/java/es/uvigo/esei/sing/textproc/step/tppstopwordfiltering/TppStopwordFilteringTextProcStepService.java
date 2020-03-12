// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.tppstopwordfiltering;

import java.util.Set;

import es.uvigo.esei.sing.textproc.abstracttppstep.AbstractTppTextProcStepService;
import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.TppStopwordFilteringTextProcStep.AddLanguageParameterAction;
import es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.entity.StopwordFilteredTextDocument;
import es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.entity.StopwordFilteredTextWithTitleDocument;
import es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.xml.definition.LanguageProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * The service implementation for this processing step.
 *
 * @author Alejandro González García
 */
public final class TppStopwordFilteringTextProcStepService extends AbstractTppTextProcStepService {
	@Override
	public AbstractProcessingStep create() {
		final AddLanguageParameterAction requestParametersAction = new AddLanguageParameterAction();
		final TppStopwordFilteringTextProcStep serviceStep = new TppStopwordFilteringTextProcStep(requestParametersAction);

		requestParametersAction.setStep(serviceStep);

		return serviceStep;
	}

	@Override
	public String getName() {
		return "TppStopwordFiltering";
	}

	@Override
	public Set<Class<? extends ProcessedDocument>> getProcessedDocumentTypes() {
		return Set.of(StopwordFilteredTextDocument.class, StopwordFilteredTextWithTitleDocument.class);
	}

	@Override
	protected Set<Class<? extends AbstractProcessingStepParameter>> getAdditionalParticularParameters() {
		return Set.of(LanguageProcessingStepParameter.class);
	}
}
