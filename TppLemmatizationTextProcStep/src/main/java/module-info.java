// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that lemmatizes the input document tokens,
 * separated by spaces.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.tpplemmatization {
	requires es.uvigo.esei.sing.textproc.abstracttppstep;

	requires lombok;
	requires es.uvigo.esei.sing.textproc.step;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.tpplemmatization.xml.definition;

	// Entities are opened to deep reflection for JPA
	opens es.uvigo.esei.sing.textproc.step.tpplemmatization.entity;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.tpplemmatization.TppLemmatizationProcessingStepService;
}
