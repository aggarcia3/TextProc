package es.uvigo.esei.sing.textproc.step.corenlpknowledgebasepopulation.xml.definition;

import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.naturalli.OpenIE;
import es.uvigo.esei.sing.textproc.step.xml.definition.AbstractProcessingStepParameter;

/**
 * Parameter for specifying the properties file to pass to
 * {@link OpenIE}.
 *
 * @author Alejandro González García
 */
@XmlRootElement(name = "openIEProperties")
public final class OpenIEPropertiesFileProcessingStepParameter extends AbstractProcessingStepParameter {}
