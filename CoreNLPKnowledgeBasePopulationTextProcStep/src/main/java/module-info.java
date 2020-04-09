// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Processing step for TextProc that populates a knowledge base stored in Apache
 * Jena's TDB2 format, using the NER, OpenIE and sentiment annotation facilities
 * included with CoreNLP.
 * <p>
 * This step is considerably memory intensive, requiring a maximum Java heap
 * size of at least 6 GiB on Debian Bullseye x86_64 OpenJDK 11. The exact figure
 * may vary depending on the Java version, JVM implementation, models and corpus.
 * </p>
 *
 * @author Alejandro González García
 */
module es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation {
	requires es.uvigo.esei.sing.textproc.step;

	requires stanford.corenlp;
	requires java.persistence;
	requires lombok;
	requires java.sql;
	requires org.apache.jena.tdb2;
	requires org.apache.jena.arq;
	requires org.apache.jena.core;
	requires java.ws.rs;
	requires es.uvigo.esei.sing.textproc.logging;

	// JAXB needs deep reflection access
	opens es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

	provides es.uvigo.esei.sing.textproc.step.ProcessingStepService
		with es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.CoreNLPKnowledgeBasePopulationProcessingStepService;
}
