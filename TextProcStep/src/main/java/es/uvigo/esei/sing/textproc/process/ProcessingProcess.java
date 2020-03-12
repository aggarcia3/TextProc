// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.process;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.SchemaFactoryConfigurationError;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import es.uvigo.esei.sing.textproc.process.xml.definition.ProcessingProcessDefinition;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.ProcessingStepService;
import es.uvigo.esei.sing.textproc.step.ProcessingStepServices;
import es.uvigo.esei.sing.textproc.step.xml.definition.ProcessingStepDefinition;
import es.uvigo.esei.sing.textproc.step.xml.definition.ProcessingStepParameter;
import lombok.NonNull;

/**
 * This class represents a processing process, defined by a XML document, and is
 * responsible for parsing and executing it.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is thread-safe.
 */
public final class ProcessingProcess {
	private static final String PROCESS_DECLARATION_XSD_RESOURCE = "/process_definition.xsd";

	/**
	 * Parses and executes the process declaration defined in the given input
	 * stream. This method doesn't return until all processes were executed.
	 *
	 * @param declarationInput The input stream which contains the process
	 *                         declaration, in XML.
	 * @throws ProcessingException      If an exception occurs during parsing or
	 *                                  execution.
	 * @throws IllegalArgumentException If {@code declarationInput} is {@code null}.
	 */
	public void executeProcessDeclaration(@NonNull final InputStream declarationInput) throws ProcessingException {
		ProcessingProcessDefinition processDefinition;

		// Unmarshall the process definition
		try {
			// The required JAXB context includes the process definition itself, and
			// any parameter definition provided by processing step services
			final Collection<Class<?>> jaxbContextClasses = new HashSet<>();
			jaxbContextClasses.add(ProcessingProcessDefinition.class);

			for (final ProcessingStepService stepService : ProcessingStepServices.getServiceLoader()) {
				jaxbContextClasses.addAll(stepService.getAdditionalParameters());
			}

			final Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(
				jaxbContextClasses.toArray(new Class<?>[jaxbContextClasses.size()])
			).createUnmarshaller();

			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true); // Limit resource usage

			// Treat warnings during unmarshalling as errors
			schemaFactory.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(final SAXParseException exception) throws SAXException {
					throw exception;
				}

				@Override
				public void error(final SAXParseException exception) throws SAXException {
					throw exception;
				}

				@Override
				public void fatalError(final SAXParseException exception) throws SAXException {
					throw exception;
				}
			});

			jaxbUnmarshaller.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(final ValidationEvent event) {
					return false;
				}
			});

			jaxbUnmarshaller.setSchema(
				SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(
					ProcessingProcess.class.getResource(PROCESS_DECLARATION_XSD_RESOURCE)
				)
			);

			processDefinition = jaxbUnmarshaller.unmarshal(
				new StreamSource(declarationInput), ProcessingProcessDefinition.class
			).getValue();

			assert processDefinition != null : "The unmarshalled process definition element can't be null";
		} catch (final SchemaFactoryConfigurationError | UnsupportedOperationException | JAXBException | SAXException exc) {
			throw new ProcessingException(
				"An exception occurred while reading the process declaration", exc
			);
		}

		// We have the process definition loaded to a object graph. Execute each step in order
		for (final ProcessingStepDefinition stepDefinition : processDefinition.getProcessingSteps()) {
			Map<String, String> parametersMap;
			final List<ProcessingStepParameter> parameters = stepDefinition.getParameters();

			// Convert parameters in list to a map
			parametersMap = new HashMap<>(
				(int) Math.ceil(parameters.size() / 0.75)
			);
			for (final ProcessingStepParameter parameter : parameters) {
				parametersMap.put(parameter.getName(), parameter.getValue());
			}

			stepDefinition.getAction().execute(Collections.unmodifiableMap(parametersMap));
		}
	}
}
