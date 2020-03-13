// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that removes stopwords from the input documents,
 * via Text Processing Python.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.tppstopwordfiltering {
	requires es.uvigo.esei.sing.textproc.abstracttppstep;

	requires lombok;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.xml.definition;

	// Entities are opened to deep reflection for JPA
	opens es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.entity;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.tppstopwordfiltering.TppStopwordFilteringProcessingStepService;
}
