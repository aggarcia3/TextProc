// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that lemmatizes each token of its input
 * documents via Stanford CoreNLP.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.corenlplemmatization {
	requires es.uvigo.esei.sing.textproc.step;

	requires stanford.corenlp;
	requires java.persistence;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.corenlplemmatization.xml.definition;

	// Entities are opened to deep reflection for JPA
	opens es.uvigo.esei.sing.textproc.step.corenlplemmatization.entity;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.corenlplemmatization.CoreNLPLemmatizationProcessingStepService;
}
