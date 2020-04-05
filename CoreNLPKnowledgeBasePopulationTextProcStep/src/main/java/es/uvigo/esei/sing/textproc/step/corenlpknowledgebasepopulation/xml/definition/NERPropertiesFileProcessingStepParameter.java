package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the properties file to pass to
 * {@link NERCombinerAnnotator}, for configuring how named entities will be
 * recognized.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "nerProperties")
public final class NERPropertiesFileProcessingStepParameter extends AbstractProcessingStepParameter {}
