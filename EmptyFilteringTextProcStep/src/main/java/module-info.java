// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that doesn't copy as processed documents the
 * input documents which are empty of meaning.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.emptyfiltering {
	requires es.uvigo.esei.sing.textproc.step;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.emptyfiltering.xml.definition;

	// Entities are opened to deep reflection for JPA
	opens es.uvigo.esei.sing.textproc.step.emptyfiltering.entity;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.emptyfiltering.EmptyFilteringTextProcStepService;
}
