// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that extracts new named entities from documents,
 * from seed sets of entities, using bootstrapped pattern-based learning.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.corenlpentityextraction {
	requires es.uvigo.esei.sing.textproc.step;

	requires stanford.corenlp;
	requires java.persistence;
	requires commons.csv;
	requires lombok;
	requires java.sql;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.corenlpentityextraction.CoreNLPEntityExtractionProcessingStepService;
}
