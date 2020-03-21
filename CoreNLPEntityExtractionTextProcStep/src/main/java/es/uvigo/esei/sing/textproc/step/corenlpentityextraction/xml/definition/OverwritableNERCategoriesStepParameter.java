// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.corenlpentityextraction.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.TokensRegexNERAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the named entity categories that the resulting
 * {@link TokensRegexNERAnnotator} named entity term mapping file will be able
 * to override.
 * <p>
 * For example, if a NER model recognizes "measles" as CAUSE_OF_DEATH, but the
 * mapping file indicates that it is a DISEASE, then it will be marked as a
 * DISEASE if and only if CAUSE_OF_DEATH is a overwritable named entity type.
 * </p>
 * <p>
 * If the value of this parameter is the empty string, no categories will be
 * overriden (excluding defaults).
 * </p>
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "overwritableNerCategories")
public final class OverwritableNERCategoriesStepParameter extends AbstractProcessingStepParameter {}
