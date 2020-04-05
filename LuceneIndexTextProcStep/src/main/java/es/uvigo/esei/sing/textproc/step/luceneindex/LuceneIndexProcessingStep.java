// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.luceneindex;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import es.uvigo.esei.sing.textproc.step.AbstractProcessingStep;
import es.uvigo.esei.sing.textproc.step.ProcessingException;
import es.uvigo.esei.sing.textproc.step.luceneindex.xml.definition.FolderPathProcessingStepParameter;

/**
 * Builds a Lucene index for all the input documents.
 * <p>
 * Example declaration for this step in a process definition file:
 * </p>
 * <pre>
 * {@code <step action="LuceneIndex">
 * 	<parameters>
 * 		<textDocumentWithTitleTableName>non_empty_submission</textDocumentWithTitleTableName>
 * 		<textDocumentTableName>non_empty_comment</textDocumentTableName>
 * 		<li:folderPath>my_index</li:folderPath>
 * 	</parameters>
 * </step>}
 * </pre>
 *
 * @author Alejandro González García
 */
final class LuceneIndexProcessingStep extends AbstractProcessingStep {
	private static final String FOLDER_PATH_PROCESSING_STEP_PARAMETER_NAME = new FolderPathProcessingStepParameter().getName();

	/**
	 * Instantiates a Lucene index building processing step.
	 */
	LuceneIndexProcessingStep() {
		super(
			// Additional mandatory and optional parameters, with their validation function
			Map.of(
				FOLDER_PATH_PROCESSING_STEP_PARAMETER_NAME, (final String value) -> value != null && !value.isBlank()
			),
			// Additional mandatory parameters
			Set.of()
		);
	}

	@Override
	protected void run() throws ProcessingException {
		try {
			final FSDirectory indexFolder = FSDirectory.open(Path.of(
				getParameters().getOrDefault(FOLDER_PATH_PROCESSING_STEP_PARAMETER_NAME, "lucene_index")
			));

			// Use a simple whitespace analyzer and tokenizer because input documents are assumed to be processed.
			// BM25 is a state of the art TF-IDF-based similarity metric
			final IndexWriterConfig indexConfig = new IndexWriterConfig(new WhitespaceAnalyzer())
				.setOpenMode(OpenMode.CREATE)
				.setSimilarity(new BM25Similarity());

			// The field type for every document text attribute
			final FieldType fieldType = new FieldType();
			fieldType.setStored(false); // Do not store the raw value. We store a key instead
			fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			fieldType.setStoreTermVectors(true);
			fieldType.setStoreTermVectorPositions(true);
			fieldType.freeze();

			try (final IndexWriter luceneIndex = new IndexWriter(indexFolder, indexConfig)) {
				for (int i = 0; i < unprocessedDocumentTypesNames.size(); ++i) {
					final String[] unprocessedAttributeNames = unprocessedDocumentsAttributes.get(i);

					forEachDocumentInNativeQuery(
						unprocessedDocumentsQuerySuppliers.get(i),
						String.format("Adding %s to Lucene index", unprocessedDocumentTypesNames.get(i)),
						numberOfUnprocessedEntitiesProviders.get(i).get(),
						(final List<String[]> batchAttributes) -> {
							final Collection<IndexableField> documentFields = new ArrayList<>(unprocessedAttributeNames.length);

							for (final String[] documentAttributes : batchAttributes) {
								// Add a field with the primary key
								documentFields.add(
									new StoredField("id", Integer.parseInt(documentAttributes[0]))
								);

								// Add the text attributes of the document as fields
								for (int j = 0; j < unprocessedAttributeNames.length; ++j) {
									documentFields.add(
										new Field(unprocessedAttributeNames[0], documentAttributes[j + 1], fieldType)
									);
								}

								try {
									luceneIndex.addDocument(documentFields);
								} catch (final IllegalArgumentException | IOException exc) {
									throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
								}

								// Clear the collection for reuse
								documentFields.clear();
							}
						},
						null
					);
				}

				luceneIndex.commit();
			}
		} catch (final IllegalArgumentException | IOException exc) {
			throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
		}
	}
}
