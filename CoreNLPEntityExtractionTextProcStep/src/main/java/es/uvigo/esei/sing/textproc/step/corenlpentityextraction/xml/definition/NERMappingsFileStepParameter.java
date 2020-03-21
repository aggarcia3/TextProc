// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.TokensRegexNERAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the path to the file where the resulting NER
 * mappings will be stored. These NER mappings relate terms present in the seed
 * dictionaries and discovered terms with their named entity category, in a
 * format ready to be parsed by {@link TokensRegexNERAnnotator}.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "nerMappingsFile")
public final class NERMappingsFileStepParameter extends AbstractProcessingStepParameter {}
