// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the directory where the seed words files for
 * {@link GetPatternsFromDataMultiClass} will be found.
 * <p>
 * The step will scan the provided file name (normally, a directory) for files
 * with "tsv" extension, and interpret their name (without the extension) as a
 * named entity label or type. The TSV files found must contain two columns,
 * "term" and "synonyms", where the first column enumerates terms, and the
 * second column the synonyms for every term (if there are no synonyms, it can
 * be empty or missing).
 * <p>
 * The results of this operation will be used to generate a value for the
 * {@code seedWordsFiles} property used by
 * {@link GetPatternsFromDataMultiClass}.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "seedWordsFilesDirectory")
public final class SeedWordsDirectoryProcessingStepParameter extends AbstractProcessingStepParameter {}
