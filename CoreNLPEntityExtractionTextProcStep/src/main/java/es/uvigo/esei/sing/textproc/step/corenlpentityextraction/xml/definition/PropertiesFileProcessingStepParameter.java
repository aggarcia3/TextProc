// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.TokensRegexNERAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the properties to pass to
 * {@link GetPatternsFromDataMultiClass}. The keys of the properties file are
 * not that well documented, and the class code itself is a poorly commented,
 * hard to read god class with more than 3600 lines of code, but there are
 * examples included with the CoreNLP distribution.
 *
 * <p>
 * Some properties should not be included in a custom properties file, either
 * because they are generated or assumed to have a fixed value. These properties
 * are:
 * </p>
 * <ul>
 * <li>{@code file} (automatically set to the temporary document file)</li>
 * <li>{@code fileFormat} (automatically set to {@code ser} for efficiency)</li>
 * <li>{@code useTargetNERRestriction} (assumed to be {@code true})</li>
 * <li>{@code useTargetParserParentRestriction} (assumed to be
 * {@code false})</li>
 * <li>{@code seedWordsFiles} (deduced from
 * {@link SeedWordsDirectoryProcessingStepParameter})</li>
 * <li>{@code patternType} (assumed to be "SURFACE")</li>
 * <li>{@code preserveSentenceSequence} (assumed to be {@code false})</li>
 * <li>{@code savePatternsWordsDir} (assumed to be {@code true})</li>
 * <li>{@code loadSavedPatternsWordsDir} (automatically set to {@code true} or
 * {@code false})</li>
 * <li>{@code patternsWordsDir} (automatically set to the temporary model
 * folder)</li>
 * <li>{@code computeAllPatterns} (should be {@code true} for consistency)</li>
 * <li>{@code identifier} (assumed to be {@code textproc})</li>
 * <li>{@code outDir} (automatically set to the appropriate temporary
 * folder)</li>
 * </ul>
 * <p>
 * Additionally, the following properties are added:
 * </p>
 * <ul>
 * <li>{@code nerModelPaths}: comma-separated list of URL or file paths to the
 * NER model to use for {@link NERCombinerAnnotator}. See
 * {@link IOUtils#getInputStreamFromURLOrClasspathOrFileSystem(String)}.</li>
 * <li>{@code fineGrainedRegexnerMapping}: the value to pass to the
 * {@code mapping} property of {@link TokensRegexNERAnnotator} when
 * instantiating a {@link NERCombinerAnnotator}. If not provided, a default
 * value will be used.</li>
 * <li>{@code applyFineGrainedRegexner}: the value to pass to the
 * {@code ner.applyFineGrained} property of {@link NERCombinerAnnotator} when
 * instantiating it. If not provided, a default value will be used.</li>
 * </ul>
 * <p>
 * See the bundled default properties file and the see also section for more
 * details.
 * </p>
 *
 * @author Alejandro González García
 * @see <a href=
 *      "https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/patterns/GetPatternsFromDataMultiClass.html">https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/patterns/GetPatternsFromDataMultiClass.html</a>,
 *      <a href=
 *      "https://nlp.stanford.edu/software/patternslearning.html">https://nlp.stanford.edu/software/patternslearning.html</a>
 *      <a href=
 *      "https://github.com/stanfordnlp/CoreNLP/blob/master/data/edu/stanford/nlp/patterns/surface/example.properties">https://github.com/stanfordnlp/CoreNLP/blob/master/data/edu/stanford/nlp/patterns/surface/example.properties</a>,
 *      <a href=
 *      "https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/patterns/GetPatternsFromDataMultiClass.java#L3005">https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/patterns/GetPatternsFromDataMultiClass.java#L3005</a>
 */
@XmlRootElement(name = "spiedProperties")
public final class PropertiesFileProcessingStepParameter extends AbstractProcessingStepParameter {}
