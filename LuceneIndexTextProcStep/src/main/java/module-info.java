// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that builds a Lucene index for the input
 * documents.
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.luceneindex {
	requires es.uvigo.esei.sing.textproc.step;

	requires lombok;
	requires java.persistence;
	requires lucene.shaded;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.luceneindex.xml.definition;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.luceneindex.LuceneIndexTextProcStepService;
}
