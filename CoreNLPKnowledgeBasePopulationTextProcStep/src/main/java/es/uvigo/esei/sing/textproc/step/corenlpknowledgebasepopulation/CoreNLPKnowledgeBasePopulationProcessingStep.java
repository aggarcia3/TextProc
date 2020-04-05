// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.persistence.PersistenceException;
import javax.ws.rs.core.UriBuilder;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb2.TDB2Factory;

import edu.stanford.nlp.coref.CorefProperties.CorefAlgorithmType;
import edu.stanford.nlp.coref.CorefProperties.MentionDetectionType;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotator;
import edu.stanford.nlp.naturalli.OpenIE;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.CorefAnnotator;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.SentimentAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator.TokenizerType;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass;
import edu.stanford.nlp.util.CoreMap;
import es.uvigo.esei.sing.textproc.logging.TextProcLogging;
import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.BaseModelURIProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceAnimateFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceCountriesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceDictionaryListProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceDictionaryPMIProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceGenderNumberFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceInanimateFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceMentionDetectionModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceSignaturesProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceStatesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceStatesProvincesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.CoreferenceWordCountsProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.DemonymsFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.DependencyParseModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.JenaDatasetFolderProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.KnowledgeBaseExportFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.NERMappingsFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.NERPropertiesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.OpenIEPropertiesFileProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.POSModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.ParserModelProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition.SentimentModelProcessingStepParameter;
import lombok.NonNull;

/**
 * Populates a RDF knowledge base stored in Apache Jena's TDB2 format, using the
 * NER, OpenIE and sentiment annotation facilities included with CoreNLP.
 * <p>
 * Example declaration for this step in a process definition file:
 * </p>
 * <pre>
 * {@code <step action="CoreNLPKnowledgeBasePopulation">
 * 	<parameters>
 * 		<textDocumentWithTitleTableName>non_empty_submission</textDocumentWithTitleTableName>
 * 		<textDocumentTableName>non_empty_comment</textDocumentTableName>
 * 		<cnlpkbp:jenaDatasetFolder>my_kb</cnlpkbp:jenaDatasetFolder>
 * 	</parameters>
 * </step>}
 * </pre>
 *
 * @author Alejandro González García
 */
final class CoreNLPKnowledgeBasePopulationProcessingStep extends AbstractProcessingStep {
	private static final String NER_MAPPINGS_FILE_STEP_PARAMETER_NAME = new NERMappingsFileProcessingStepParameter().getName();
	private static final String JENA_DATASET_FOLDER_STEP_PARAMETER_NAME = new JenaDatasetFolderProcessingStepParameter().getName();
	private static final String BASE_MODEL_URI_STEP_PARAMETER_NAME = new BaseModelURIProcessingStepParameter().getName();
	private static final String POS_MODEL_PROCESSING_STEP_PARAMETER_NAME = new POSModelProcessingStepParameter().getName();
	private static final String DEPENDENCY_PARSE_MODEL_PROCESSING_STEP_PARAMETER_NAME = new DependencyParseModelProcessingStepParameter().getName();
	private static final String PARSER_MODEL_PROCESSING_STEP_PARAMETER_NAME = new ParserModelProcessingStepParameter().getName();
	private static final String COREFERENCE_MODEL_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceModelProcessingStepParameter().getName();
	private static final String COREFERENCE_MENTION_DETECTION_MODEL_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceMentionDetectionModelProcessingStepParameter().getName();
	private static final String DEMONYMS_FILE_PROCESSING_STEP_PARAMETER_NAME = new DemonymsFileProcessingStepParameter().getName();
	private static final String COREFERENCE_ANIMATE_FILE_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceAnimateFileProcessingStepParameter().getName();
	private static final String COREFERENCE_INANIMATE_FILE_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceInanimateFileProcessingStepParameter().getName();
	private static final String COREFERENCE_STATES_FILE_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceStatesFileProcessingStepParameter().getName();
	private static final String COREFERENCE_GENDER_NUMBER_FILE_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceGenderNumberFileProcessingStepParameter().getName();
	private static final String COREFERENCE_COUNTRIES_FILE_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceCountriesFileProcessingStepParameter().getName();
	private static final String COREFERENCE_STATES_PROVINCES_FILE_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceStatesProvincesFileProcessingStepParameter().getName();
	private static final String COREFERENCE_DICTIONARY_LIST_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceDictionaryListProcessingStepParameter().getName();
	private static final String COREFERENCE_DICTIONARY_PMI_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceDictionaryPMIProcessingStepParameter().getName();
	private static final String COREFERENCE_SIGNATURES_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceSignaturesProcessingStepParameter().getName();
	private static final String COREFERENCE_WORD_COUNTS_PROCESSING_STEP_PARAMETER_NAME = new CoreferenceWordCountsProcessingStepParameter().getName();
	private static final String SENTIMENT_MODEL_PROCESSING_STEP_PARAMETER_NAME = new SentimentModelProcessingStepParameter().getName();
	private static final String NER_PROPERTIES_FILE_STEP_PARAMETER_NAME = new NERPropertiesFileProcessingStepParameter().getName();
	private static final String OPENIE_PROPERTIES_FILE_STEP_PARAMETER_NAME = new OpenIEPropertiesFileProcessingStepParameter().getName();
	private static final String KNOWLEDGE_BASE_EXPORT_FILE_STEP_PARAMETER_NAME = new KnowledgeBaseExportFileProcessingStepParameter().getName();

	private static final Properties DEFAULT_NER_PROPERTIES;
	private static final Properties DEFAULT_OPENIE_PROPERTIES;

	private static final String CPU_THREADS_STRING = Integer.toString(Runtime.getRuntime().availableProcessors());

	static {
		try {
			DEFAULT_NER_PROPERTIES = new Properties();
			DEFAULT_NER_PROPERTIES.load(
				new InputStreamReader(
					CoreNLPKnowledgeBasePopulationProcessingStep.class.getResourceAsStream("/default_ner_properties.properties"),
					StandardCharsets.UTF_8
				)
			);

			DEFAULT_OPENIE_PROPERTIES = new Properties();
			DEFAULT_OPENIE_PROPERTIES.load(
				new InputStreamReader(
					CoreNLPKnowledgeBasePopulationProcessingStep.class.getResourceAsStream("/default_openie_properties.properties"),
					StandardCharsets.UTF_8
				)
			);
		} catch (final IOException exc) {
			throw new ExceptionInInitializerError(
				"Couldn't load the default NER properties resource. Is the JAR of this step correctly packaged?"
			);
		}
	}

	/**
	 * Instantiates a Stanford CoreNLP knowledge base population processing step.
	 */
	CoreNLPKnowledgeBasePopulationProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.ofEntries(
				Map.entry(NER_MAPPINGS_FILE_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(JENA_DATASET_FOLDER_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(BASE_MODEL_URI_STEP_PARAMETER_NAME, (final String value) -> {
					try {
						new URI(value);
						return true;
					} catch (final NullPointerException | URISyntaxException exc) {
						return false;
					}
				}),
				Map.entry(POS_MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(DEPENDENCY_PARSE_MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(PARSER_MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_MENTION_DETECTION_MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(DEMONYMS_FILE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_ANIMATE_FILE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_INANIMATE_FILE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_STATES_FILE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_GENDER_NUMBER_FILE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_COUNTRIES_FILE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_STATES_PROVINCES_FILE_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_DICTIONARY_LIST_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_DICTIONARY_PMI_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_SIGNATURES_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(COREFERENCE_WORD_COUNTS_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(SENTIMENT_MODEL_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(NER_PROPERTIES_FILE_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(OPENIE_PROPERTIES_FILE_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()),
				Map.entry(KNOWLEDGE_BASE_EXPORT_FILE_STEP_PARAMETER_NAME, (final String value) -> KnowledgeBaseExportFileProcessingStepParameter.isValid(value))
			),
			// Additional mandatory parameters
			Set.of()
		);
	}

	@Override
	protected void run() throws ProcessingException {
		final String nerPropertiesFile = getParameters().get(NER_PROPERTIES_FILE_STEP_PARAMETER_NAME);
		final String openIEPropertiesFile = getParameters().get(OPENIE_PROPERTIES_FILE_STEP_PARAMETER_NAME);
		final String additionalNamedEntitiesFile = getParameters().get(NER_MAPPINGS_FILE_STEP_PARAMETER_NAME);

		try {
			final URI baseModelURI = new URI(
				getParameters().getOrDefault(BASE_MODEL_URI_STEP_PARAMETER_NAME, "textproc-kb:/")
			);
			final UriBuilder baseEntityUriBuilder = UriBuilder.fromUri(baseModelURI.resolve("entity"));
			final UriBuilder basePropertyUriBuilder = UriBuilder.fromUri(baseModelURI.resolve("property"));
			final UriBuilder baseDocumentTypeUriBuilder = UriBuilder.fromUri(baseModelURI.resolve("document-type"));

			// First, set up the CoreNLP pipeline.
			// Here we really use the library (and hardware that executes this) to its full potential :)

			final Properties sentimentProperties = new Properties();
			setPropertyFromParameter(sentimentProperties, "sentiment.model",
				SENTIMENT_MODEL_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a model included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/iy9hd5ons0dl0ev/sentiment.ser.gz"
			);

			final Properties parserProperties = new Properties();
			// Use as many threads as CPU threads we have, unless the user overrides it
			parserProperties.setProperty("parser.nthreads", CPU_THREADS_STRING);
			setPropertyFromParameter(parserProperties, "parser.model",
				PARSER_MODEL_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a model included with the latest CoreNLP models (as of 03-26-2020).
				// It is a shift-reduce parser, more efficient than the default, based on PCFG.
				// See: https://nlp.stanford.edu/software/srparser.html
				"https://dl.dropboxusercontent.com/s/ltq27ez8lr4nwb4/englishSR.beam.ser.gz"
			);
			parserProperties.setProperty("parser.binaryTrees", Boolean.toString(true));

			final Properties nerProperties = new Properties();
			nerProperties.putAll(DEFAULT_NER_PROPERTIES);
			// Use as many threads as CPU threads we have, unless the user overrides it
			nerProperties.setProperty("ner.nthreads", CPU_THREADS_STRING);
			// Load custom properties and additional named entity files if applicable
			if (nerPropertiesFile != null) {
				nerProperties.load(new FileReader(nerPropertiesFile, StandardCharsets.UTF_8));
			}
			if (additionalNamedEntitiesFile != null) {
				nerProperties.setProperty("ner.additional.regexner.mapping", additionalNamedEntitiesFile);
			}

			final Properties dependencyParseProperties = new Properties();
			dependencyParseProperties.setProperty("nthreads", CPU_THREADS_STRING);
			setPropertyFromParameter(dependencyParseProperties, "model",
				DEPENDENCY_PARSE_MODEL_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a model included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/jrrgezz7ng29i2i/english_wsj_UD.gz"
			);

			final Properties corefProperties = new Properties();
			// Fastneural is faster than pure neural, and hopefully more precise than statistical
			corefProperties.setProperty("coref.algorithm", CorefAlgorithmType.FASTNEURAL.name());
			setPropertyFromParameter(corefProperties, "coref.fastneural.modelPath",
				COREFERENCE_MODEL_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a model included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/45jb596a0vrbnf7/fast-english-model.ser.gz"
			);
			// Dependency should be more lightweight than rule-based, and pretty accurate still
			corefProperties.setProperty("coref.md.type", MentionDetectionType.DEPENDENCY.name());
			// Just in case the default value for this property changes
			corefProperties.setProperty("coref.useConstituencyParse", Boolean.toString(false));
			setPropertyFromParameter(corefProperties, "coref.md.model",
				COREFERENCE_MENTION_DETECTION_MODEL_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a model included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/y6jlnef93skrdcn/md-model-dep.ser.gz"
			);
			setPropertyFromParameter(corefProperties, "coref.demonym",
				DEMONYMS_FILE_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/psw2waecnciaf7h/demonyms.txt"
			);
			setPropertyFromParameter(corefProperties, "coref.animate",
				COREFERENCE_ANIMATE_FILE_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/lb6b3q56qmdoewv/animate.unigrams.txt"
			);
			setPropertyFromParameter(corefProperties, "coref.inanimate",
				COREFERENCE_INANIMATE_FILE_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/przll0y71o4ptw3/inanimate.unigrams.txt"
			);
			setPropertyFromParameter(corefProperties, "coref.states",
				COREFERENCE_STATES_FILE_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/8tzjk6754jlz7hi/state-abbreviations.txt"
			);
			setPropertyFromParameter(corefProperties, "coref.big.gender.number",
				COREFERENCE_GENDER_NUMBER_FILE_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/wuuys6h45yei6mn/gender.data.gz"
			);
			setPropertyFromParameter(corefProperties, "coref.countries",
				COREFERENCE_COUNTRIES_FILE_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/lbzjgj3hcn3y9g9/countries"
			);
			setPropertyFromParameter(corefProperties, "coref.states.provinces",
				COREFERENCE_STATES_PROVINCES_FILE_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/63sykdzz4ofbkkx/statesandprovinces"
			);
			setPropertyFromParameter(corefProperties, "coref.dictlist",
				COREFERENCE_DICTIONARY_LIST_PROCESSING_STEP_PARAMETER_NAME,
				// Mirrors of files included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/0a9epbf00tufs9b/coref.dict1.tsv," +
				"https://dl.dropboxusercontent.com/s/qr4to5et0nxtg4d/coref.dict2.tsv," +
				"https://dl.dropboxusercontent.com/s/41ttt0lyz3fmiza/coref.dict3.tsv," +
				"https://dl.dropboxusercontent.com/s/n61v6zrqsi8rnqt/coref.dict4.tsv"
			);
			setPropertyFromParameter(corefProperties, "coref.dictpmi",
				COREFERENCE_DICTIONARY_PMI_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/0a9epbf00tufs9b/coref.dict1.tsv"
			);
			setPropertyFromParameter(corefProperties, "coref.signatures",
				COREFERENCE_SIGNATURES_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/azd2yshj0db1nez/ne.signatures.txt"
			);
			setPropertyFromParameter(corefProperties, "coref.statistical.wordCounts",
				COREFERENCE_WORD_COUNTS_PROCESSING_STEP_PARAMETER_NAME,
				// Mirror of a file included with the latest CoreNLP models (as of 03-26-2020)
				"https://dl.dropboxusercontent.com/s/cs1avdx96vabgr1/word_counts.ser.gz"
			);

			final Properties openIEProperties = new Properties();
			openIEProperties.putAll(DEFAULT_OPENIE_PROPERTIES);
			// Load custom properties if necessary
			if (openIEPropertiesFile != null) {
				nerProperties.load(new FileReader(openIEPropertiesFile, StandardCharsets.UTF_8));
			}

			final AnnotationPipeline nlpPipeline = new AnnotationPipeline();
			// We assume the input is already tokenized, so we use a cheap whitespace tokenizer
			nlpPipeline.addAnnotator(new TokenizerAnnotator(false, TokenizerType.Whitespace));
			// Required by POS annotator
			nlpPipeline.addAnnotator(new WordsToSentencesAnnotator(false));
			// Required by sentiment annotator, shift-reduce parser and NER
			nlpPipeline.addAnnotator(
				new POSTaggerAnnotator(
					getParameters().getOrDefault(
						POS_MODEL_PROCESSING_STEP_PARAMETER_NAME,
						// Mirror of a model included with the latest CoreNLP models (as of 03-26-2020)
						"https://dl.dropboxusercontent.com/s/bmqus6uon0cr9ek/english-caseless-left3words-distsim.tagger"
					), false, Integer.MAX_VALUE, 1
				)
			);
			// Required by sentiment annotator
			nlpPipeline.addAnnotator(new ParserAnnotator("parser", parserProperties));
			// Required by NER and OpenIE
			nlpPipeline.addAnnotator(new MorphaAnnotator(false));
			// These two are required by OpenIE coreference resolution
			nlpPipeline.addAnnotator(new DependencyParseAnnotator(dependencyParseProperties));
			nlpPipeline.addAnnotator(new NERCombinerAnnotator(nerProperties));
			nlpPipeline.addAnnotator(new CorefAnnotator(corefProperties));
			// Required by OpenIE
			nlpPipeline.addAnnotator(new NaturalLogicAnnotator());
			nlpPipeline.addAnnotator(new SentimentAnnotator("sentiment", sentimentProperties));
			nlpPipeline.addAnnotator(new OpenIE(openIEProperties));

			// Connect to the Jena dataset and get the RDF triple graph
			final Dataset jenaDataset = TDB2Factory.connectDataset(
				getParameters().getOrDefault(JENA_DATASET_FOLDER_STEP_PARAMETER_NAME, "knowledge_base")
			);
			final Model tripleGraph = jenaDataset.getNamedModel(baseModelURI.toASCIIString());

			// Clear previous data from the graph
			jenaDataset.begin(ReadWrite.WRITE);
			try {
				tripleGraph.removeAll();
				jenaDataset.commit();
			} finally {
				jenaDataset.end();
			}

			// Some constant properties
			final Property confidenceProperty = tripleGraph.createProperty(
				basePropertyUriBuilder.fragment("confidence").build().toASCIIString()
			);
			final Property sentimentClassProperty = tripleGraph.createProperty(
				basePropertyUriBuilder.fragment("sentiment-class").build().toASCIIString()
			);
			final Property documentTypeProperty = tripleGraph.createProperty(
				basePropertyUriBuilder.fragment("document-type").build().toASCIIString()
			);
			final Property documentIdProperty = tripleGraph.createProperty(
				basePropertyUriBuilder.fragment("document-id").build().toASCIIString()
			);
			final Property documentSentenceNumberProperty = tripleGraph.createProperty(
				basePropertyUriBuilder.fragment("document-sentence-number").build().toASCIIString()
			);

			for (int i = 0; i < unprocessedDocumentTypesNames.size(); ++i) {
				final Resource documentTypeNode = tripleGraph.createResource(
					baseDocumentTypeUriBuilder.fragment(
						unprocessedDocumentTypesNames.get(i)
					).build().toASCIIString()
				);

				final String[] unprocessedAttributeNames = unprocessedDocumentsAttributes.get(i);

				forEachDocumentInNativeQuery(
					unprocessedDocumentsQuerySuppliers.get(i),
					String.format("Populating KB from %s", unprocessedDocumentTypesNames.get(i)),
					numberOfUnprocessedEntitiesProviders.get(i).get(),
					(final List<String[]> batchAttributes) -> {
						// Start a Jena dataset transaction for writing
						// Transactions are serializable, so no worries about phantom reads
						// and such
						jenaDataset.begin(ReadWrite.WRITE);

						try {
							for (final String[] documentAttributes : batchAttributes) {
								for (int j = 0; j < unprocessedAttributeNames.length; ++j) {
									final Annotation annotatedAttribute = new Annotation(documentAttributes[j + 1]);

									nlpPipeline.annotate(annotatedAttribute);

									int sentenceNumber = 0;
									for (final CoreMap sentence : annotatedAttribute.get(SentencesAnnotation.class)) {
										final Collection<RelationTriple> triples = sentence.get(
											NaturalLogicAnnotations.RelationTriplesAnnotation.class
										);

										for (final RelationTriple triple : triples) {
											final String subjectText = triple.subjectLemmaGloss();
											final String relationText = triple.relationLemmaGloss();
											final String objectText = triple.objectLemmaGloss();

											// Create the nodes in the graph
											final Resource subject = tripleGraph.createResource(
												baseEntityUriBuilder.fragment(
													subjectText
												).build().toASCIIString()
											);

											final Property relation = tripleGraph.createProperty(
												basePropertyUriBuilder.fragment(
													relationText
												).build().toASCIIString()
											);

											final Resource object = tripleGraph.createResource(
												baseEntityUriBuilder.fragment(
													objectText
												).build().toASCIIString()
											);

											// Add the subject-object relation (actual knowledge)
											subject.addProperty(relation, object);

											// Now add metadata about the relation instance
											final ReifiedStatement reifiedStatement = tripleGraph.createStatement(
												subject, relation, object
											).createReifiedStatement();

											// Confidence level
											reifiedStatement.addProperty(
												confidenceProperty,
												tripleGraph.createTypedLiteral((float) triple.confidence)
											);

											// Sentiment class
											reifiedStatement.addProperty(
												sentimentClassProperty,
												sentence.get(SentimentClass.class)
											);

											// Document type
											reifiedStatement.addProperty(
												documentTypeProperty,
												documentTypeNode
											);

											// Document ID (primary key)
											reifiedStatement.addProperty(
												documentIdProperty,
												tripleGraph.createTypedLiteral(Integer.parseInt(documentAttributes[0]))
											);

											// Sentence number within the document
											reifiedStatement.addProperty(
												documentSentenceNumberProperty,
												tripleGraph.createTypedLiteral(sentenceNumber)
											);
										}

										++sentenceNumber;
									}
								}
							}

							// Everything went well, commit any changes made to the dataset
							jenaDataset.commit();
						} finally {
							jenaDataset.end();
						}
					},
					null
				);
			}

			// Export the knowledge base to a file if needed
			String knowledgeBaseExportValue = null;
			if (
				(knowledgeBaseExportValue = getParameters().get(
					KNOWLEDGE_BASE_EXPORT_FILE_STEP_PARAMETER_NAME
				)) != null
			) {
				final String path = KnowledgeBaseExportFileProcessingStepParameter.getPathFromValue(
					knowledgeBaseExportValue
				);
				final String format = KnowledgeBaseExportFileProcessingStepParameter.getFormatFromValue(
					knowledgeBaseExportValue
				);

				jenaDataset.begin(ReadWrite.READ);
				try {
					tripleGraph.write(Files.newOutputStream(Path.of(path)), format);

					jenaDataset.commit();
				} catch (final Exception exc) {
					// I'm pretty sure that exceptions are thrown if some I/O error occurs.
					// It is kind of sloppy to wrap everything in a RuntimeException,
					// because it makes documentation writers forget about documenting what
					// can be thrown, and we can't react specifically to the error...
					TextProcLogging.getLogger().log(
						Level.WARNING,
						"An exception occurred while exporting the knowledge base, so it was not completely exported",
						exc
					);
				} finally {
					jenaDataset.end();
				}
			}
		} catch (final IOException | URISyntaxException | IllegalArgumentException | PersistenceException exc) {
			throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
		}
	}

	/**
	 * Sets a {@link Properties} object key to the same value that a step parameter,
	 * or to a default value if that step parameter doesn't have a value.
	 *
	 * @param properties   The properties object whose specified key will be
	 *                     assigned the described value.
	 * @param propertyKey  The property key whose value will be set to that of the
	 *                     parameter.
	 * @param parameterKey The key of the parameter, used to retrieve its value.
	 * @param defaultValue The value to set in the property key if the specified
	 *                     parameter key (or name) doesn't have a value.
	 * @throws IllegalArgumentException If some parameter is {@code null}, except
	 *                                  {@code defaultValue}.
	 */
	private final void setPropertyFromParameter(
		@NonNull final Properties properties, @NonNull final String propertyKey,
		@NonNull final String parameterKey, final String defaultValue
	) {
		properties.setProperty(propertyKey,
			getParameters().getOrDefault(
				parameterKey, defaultValue
			)
		);
	}
}
