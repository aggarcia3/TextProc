// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that tokenizes its input documents via Text
 * Processing Python.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.tpptokenization {
	requires es.uvigo.esei.sing.textproc.abstracttppstep;

	// Entities are opened to deep reflection for JPA
	opens es.uvigo.esei.sing.textproc.step.tpptokenization.entity;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.tpptokenization.TppTokenizationProcessingStepService;
}
