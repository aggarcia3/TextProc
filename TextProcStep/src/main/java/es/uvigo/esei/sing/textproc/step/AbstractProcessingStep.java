// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaDelete;

import es.uvigo.esei.sing.textproc.entity.ProcessedDocument;
import es.uvigo.esei.sing.textproc.logging.TextProcLogging;
import es.uvigo.esei.sing.textproc.persistence.TextProcPersistence;
import es.uvigo.esei.sing.textproc.step.internal.ProcessingStepInterface;
import es.uvigo.esei.sing.textproc.step.xml.definition.BatchSizeProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.PageSizeProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.PrimaryKeyColumnProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.TextColumnProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.TextDocumentTableNameProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.TextDocumentWithTitleTableNameProcessingStepParameter;
import es.uvigo.esei.sing.textproc.step.xml.definition.TitleColumnProcessingStepParameter;
import lombok.NonNull;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * Contains parameter validation logic common to processing steps, reducing the
 * effort needed to implement the {@link ProcessingStepInterface} interface and
 * ensuring all processing steps behave in a consistent manner.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is not thread safe.
 */
public abstract class AbstractProcessingStep implements ProcessingStepInterface {
	// Common step parameter names
	protected static final String PAGE_SIZE_STEP_PARAMETER_NAME = new PageSizeProcessingStepParameter().getName();
	protected static final String BATCH_SIZE_STEP_PARAMETER_NAME = new BatchSizeProcessingStepParameter().getName();
	protected static final String TEXT_DOCUMENT_WITH_TITLE_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME = new TextDocumentWithTitleTableNameProcessingStepParameter().getName();
	protected static final String TEXT_DOCUMENT_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME = new TextDocumentTableNameProcessingStepParameter().getName();
	protected static final String PRIMARY_KEY_COLUMN_PROCESSING_STEP_PARAMETER_NAME = new PrimaryKeyColumnProcessingStepParameter().getName();
	protected static final String TEXT_COLUMN_PROCESSING_STEP_PARAMETER_NAME = new TextColumnProcessingStepParameter().getName();
	protected static final String TITLE_COLUMN_PROCESSING_STEP_PARAMETER_NAME = new TitleColumnProcessingStepParameter().getName();

	// Common step parameter default values
	/**
	 * The default page size. Increase for optimal performance until memory usage,
	 * DB commit performance or transaction commit frequency are an issue. Ideally,
	 * the page size should be a multiple of the batch size.
	 */
	protected static final String DEFAULT_PAGE_SIZE_STEP_PARAMETER = "32768";
	/**
	 * The default batch size. The documents in a page will be divided in batches
	 * with this many documents, as much as possible. The documents in a batch will
	 * be processed together, in the same thread.
	 */
	protected static final String DEFAULT_BATCH_SIZE_STEP_PARAMETER = "512";
	protected static final String DEFAULT_PRIMARY_KEY_COLUMN_PROCESSING_STEP_PARAMETER = "id";
	protected static final String DEFAULT_TEXT_COLUMN_PROCESSING_STEP_PARAMETER = "text";
	protected static final String DEFAULT_TITLE_COLUMN_PROCESSING_STEP_PARAMETER = "title";

	private final Map<String, Predicate<String>> validationPredicates;
	private final Set<String> requiredParameters;
	private Map<String, String> parameters = null;
	private volatile boolean databaseEntitiesChanged = false;

	// Save System.out value at class initialization time so
	// we're not affected by steps reassigning System.out
	private static final PrintStream STDOUT = System.out;
	private final DelegatingProgressBarConsumer progressBarConsumer = new DelegatingProgressBarConsumer(
		(final String str) -> {
			STDOUT.print('\r');
			STDOUT.print(str);
		}
	);

	protected static final String DATA_ACCESS_EXCEPTION_MESSAGE = "An exception occurred during a data access operation";
	/**
	 * The names of all the unprocessed document types.
	 */
	protected final List<String> unprocessedDocumentTypesNames = List.of(
		"titled docs",
		"untitled docs"
	);
	/**
	 * The native query suppliers for all the known types of unprocessed documents,
	 * in the same order as {@code unprocessedDocumentTypesNames}.
	 */
	protected final List<Supplier<? extends Query>> unprocessedDocumentsQuerySuppliers = List.of(
		() -> TextProcPersistence.get().getEntityManager().createNativeQuery(buildUnprocessedDocumentWithTitleSelectStatement()),
		() -> TextProcPersistence.get().getEntityManager().createNativeQuery(buildUnprocessedDocumentSelectStatement())
	);
	/**
	 * The non primary key attribute names of all unprocessed document types, in the
	 * same order as {@code unprocessedDocumentTypesNames}.
	 */
	protected final List<String[]> unprocessedDocumentsAttributes = List.of(
		new String[] { "title", "text" },
		new String[] { "text" }
	);
	/**
	 * Suppliers that count how many unprocessed entities of a type there are. The
	 * list is in the same order as {@code unprocessedDocumentTypesNames}.
	 */
	protected final List<Supplier<Long>> numberOfUnprocessedEntitiesProviders = List.of(
		() -> getUnprocessedDocumentsWithTitle(),
		() -> getUnprocessedDocuments()
	);

	/**
	 * Constructs a new abstract processing step, with the given parameter
	 * validation predicates and required parameters. Common validation parameters
	 * will be added automatically.
	 *
	 * @param validationPredicates The validation predicates to use to validate the
	 *                             parameters, including optional ones.
	 * @param requiredParameters   A set of parameter names whose presence is
	 *                             required.
	 * @throws IllegalArgumentException If any parameter is {@code null}.
	 */
	protected AbstractProcessingStep(
		@NonNull final Map<String, Predicate<String>> validationPredicates, @NonNull final Set<String> requiredParameters
	) {
		final Map<String, Predicate<String>> commonValidationPredicates = Map.of(
			PAGE_SIZE_STEP_PARAMETER_NAME, (final String value) -> {
				try {
					if (Integer.parseInt(value) < 1) {
						throw new NumberFormatException();
					}

					return true;
				} catch (final NumberFormatException exc) {
					return false;
				}
			},
			BATCH_SIZE_STEP_PARAMETER_NAME, (final String value) -> {
				try {
					final int actualValue = Integer.parseInt(value);
					if (actualValue < 1) {
						throw new NumberFormatException();
					}

					return
						actualValue <= Integer.parseInt(
							getParameters().getOrDefault(PAGE_SIZE_STEP_PARAMETER_NAME, DEFAULT_PAGE_SIZE_STEP_PARAMETER)
						);
				} catch (final NumberFormatException exc) {
					return false;
				}
			},
			TEXT_DOCUMENT_WITH_TITLE_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME, (final String value) ->
				value != null && !value.isBlank(),
			TEXT_DOCUMENT_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME, (final String value) ->
				value != null && !value.isBlank(),
			PRIMARY_KEY_COLUMN_PROCESSING_STEP_PARAMETER_NAME, (final String value) ->
				value != null && !value.isBlank(),
			TEXT_COLUMN_PROCESSING_STEP_PARAMETER_NAME, (final String value) ->
				value != null && !value.isBlank(),
			TITLE_COLUMN_PROCESSING_STEP_PARAMETER_NAME, (final String value) ->
				value != null && !value.isBlank()
		);

		final Set<String> commonRequiredParameters = Set.of(
			TEXT_DOCUMENT_WITH_TITLE_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME,
			TEXT_DOCUMENT_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME
		);

		final Map<String, Predicate<String>> actualValidationPredicates = new HashMap<>(
			(int) Math.ceil((commonValidationPredicates.size() + validationPredicates.size()) / 0.75)
		);
		final Set<String> actualRequiredParameters = new HashSet<>(
			(int) Math.ceil((commonRequiredParameters.size() + requiredParameters.size()) / 0.75)
		);

		actualValidationPredicates.putAll(commonValidationPredicates);
		actualValidationPredicates.putAll(validationPredicates);
		actualRequiredParameters.addAll(commonRequiredParameters);
		actualRequiredParameters.addAll(requiredParameters);

		this.validationPredicates = Collections.unmodifiableMap(actualValidationPredicates);
		this.requiredParameters = Collections.unmodifiableSet(actualRequiredParameters);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote The implementation of this method saves the user provides parameter
	 *           values, executes their validation predicates, and then invokes
	 *           {@link #run()}, making sure a JPA transaction has begun.
	 */
	@Override
	public final void execute(@NonNull final Map<String, String> parameters) throws ProcessingException {
		final EntityTransaction entityTransaction = TextProcPersistence.get().getEntityManager().getTransaction();
		final String stepName = getClass().getSimpleName();
		boolean startedTransaction = false;
		boolean transactionSuccessful = true;

		this.parameters = parameters;

		System.out.println();
		validateParameters();

		try {
			if (!entityTransaction.isActive()) {
				entityTransaction.begin();
				startedTransaction = true;
			}

			System.out.print("> Executing ");
			System.out.print(stepName);
			System.out.println("...");

			run();

			System.out.println();
			System.out.print("> ");
			System.out.print(stepName);
			System.out.println(" done.");
		} catch (final ProcessingException exc) {
			transactionSuccessful = false;
			throw exc;
		} catch (final PersistenceException exc) {
			transactionSuccessful = false;
			throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
		} finally {
			if (startedTransaction && !transactionSuccessful) {
				entityTransaction.setRollbackOnly();
			}

			// Silently flush changes, just in case it was not done per page
			TextProcPersistence.get().flushEntities();
		}
	}

	/**
	 * Validates the user-specified parameter map. This method doesn't have any side
	 * effect if validation is successful.
	 *
	 * @throws ProcessingException If the validation is unsuccessful.
	 */
	private void validateParameters() throws ProcessingException {
		if (!parameters.keySet().containsAll(requiredParameters)) {
			throw new ProcessingException(
				"Missing required parameter for step " + getClass().getSimpleName()
			);
		}

		for (final Entry<String, String> entry : parameters.entrySet()) {
			if (!validationPredicates.get(entry.getKey()).test(entry.getValue())) {
				throw new ProcessingException(
					"The value for the parameter " + entry.getKey() + " in the " + getClass().getSimpleName() + " step is not valid"
				);
			}
		}
	}

	/**
	 * Returns the parameters provided by the user for this step.
	 *
	 * @return An unmodifiable, non-null map with the step parameters, where the
	 *         keys are the parameter names.
	 */
	protected final Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Executes the given action for each batch of documents retrieved by a native
	 * JPA query. For the purposes of this method, native JPA queries are to be used
	 * when the queried table doesn't have a configured mapping entity. Therefore,
	 * it returns the value of the queried columns as strings, not performing any
	 * relational to object mapping beyond that.
	 * <p>
	 * To maximize performance, the processing action may be executed in any thread,
	 * so thread-safety must be guaranteed in its implementation if shared state is
	 * to be accessed.
	 * </p>
	 * <p>
	 * This method assumes a transaction is already active.
	 * </p>
	 *
	 * @param querySupplier     A query supplier, that must return an appropriate,
	 *                          non-null native query object when invoked. This
	 *                          allows recreating the query object when needed.
	 * @param taskName          The name of the task that will be performed with the
	 *                          documents. It will be shown to the user.
	 * @param numberOfDocuments The total number of documents that will be processed
	 *                          by the action. It must be zero or greater.
	 * @param action            The action to execute for every batch of documents.
	 *                          A batch contains at least one document. The list
	 *                          supplied to the consumer is not modifiable.
	 * @param pageEndAction     The action to execute after a document page is
	 *                          processed, if processing is successful. It might be
	 *                          {@code null}, in which case nothing will be done. In
	 *                          any case, no matter if processing is successful or
	 *                          not, any database transactions made by calling
	 *                          methods of this class are committed or rolled back
	 *                          before invoking this action.
	 * @throws ProcessingException If any parameter is invalid, or an exception
	 *                             occurred during the processing.
	 */
	protected final void forEachDocumentInNativeQuery(
		@NonNull final Supplier<? extends Query> querySupplier, @NonNull final String taskName, final long numberOfDocuments,
		@NonNull final ProcessingConsumer<List<String[]>> action,
		final Runnable pageEndAction
	) throws ProcessingException {
		long numberOfPages;
		long numberOfSteps;
		final int pageSize = Integer.parseInt(
			getParameters().getOrDefault(PAGE_SIZE_STEP_PARAMETER_NAME, DEFAULT_PAGE_SIZE_STEP_PARAMETER)
		);
		final int batchSize = Integer.parseInt(
			getParameters().getOrDefault(BATCH_SIZE_STEP_PARAMETER_NAME, DEFAULT_BATCH_SIZE_STEP_PARAMETER)
		);
		final List<String[]> entityAttributesBatch = new ArrayList<>(batchSize);

		numberOfPages = numberOfDocuments / pageSize + (numberOfDocuments % pageSize == 0 ? 0 : 1);
		numberOfSteps =
			// At least one step per page
			numberOfPages +
			// For each surely complete page (every page except the last), there can be an extra step
			// because of documents that didn't complete a batch
			(numberOfPages - 1) * (pageSize % batchSize == 0 ? 0 : 1) +
			// For the last page, there can be an extra step too (but it might not be a complete page)
			((numberOfDocuments % pageSize) % batchSize == 0 ? 0 : 1);

		try {
			for (int page = 0; page < numberOfPages; ++page) {
				final Query query = querySupplier.get();

				if (query == null) {
					throw new ProcessingException("The query supplier returned a null query");
				}

				query.setFirstResult(page * pageSize);
				query.setMaxResults(pageSize);

				// We do not use getResultStream() to increase the chance that the resulting stream has a defined size
				final Stream<?> resultStream = query.getResultList().parallelStream();

				ProgressBar.wrap(
					resultStream,
					new ProgressBarBuilder()
						.setConsumer(progressBarConsumer)
						.setTaskName(taskName + " (" + (page + 1) + "/" + numberOfSteps + ")")
						.setStyle(ProgressBarStyle.ASCII)
						.showSpeed()
				).forEach((final Object result) -> {
					String[] attributes;
					List<String[]> currentBatch = null;

					try {
						if (result.getClass().isArray()) {
							// Several columns in result. Convert them to strings
							// (casting Object[] to String[] doesn't work)
							attributes = new String[((Object[]) result).length];
	
							for (int i = 0; i < attributes.length; ++i) {
								attributes[i] = columnAttributeToString(((Object[]) result)[i]);
							}
						} else {
							// One column in result. Wrap it in an array
							attributes = new String[] { columnAttributeToString(result) };
						}

						synchronized (entityAttributesBatch) {
							entityAttributesBatch.add(attributes);

							// Batch completed. Copy it so we exit the critical section ASAP,
							// and we can add documents to a new batch without the consumer noticing
							if (entityAttributesBatch.size() >= batchSize) {
								currentBatch = List.copyOf(entityAttributesBatch);
								entityAttributesBatch.clear();
							}
						}

						// Accept the batch. This can block until the batch is processed,
						// but as we are outside a critical section it's not a bottleneck
						if (currentBatch != null) {
							action.accept(currentBatch);
						}
					} catch (final Throwable exc) {
						// Exceptions thrown by this method get silently discarded.
						// Handle that by logging them
						TextProcLogging.getLogger().log(
							Level.WARNING, "An exception occurred while processing a document. Skipping...", exc
						);
					}
				});

				// Accept any remaining entity that did not make it to a complete batch
				try {
					synchronized (entityAttributesBatch) {
						final int lastBatchSize = entityAttributesBatch.size();

						if (lastBatchSize > 0) {
							try (final ProgressBar progressBar =
								new ProgressBarBuilder()
									.setConsumer(progressBarConsumer)
									.setTaskName(taskName + " (" + (page + 2) + "/" + numberOfSteps + ")")
									.setStyle(ProgressBarStyle.ASCII)
									.showSpeed()
									.setInitialMax(lastBatchSize)
									.build()
							) {
								action.accept(Collections.unmodifiableList(entityAttributesBatch));
								progressBar.stepTo(lastBatchSize);
							}
						}
					}
				} catch (final ProcessingException exc) {
					TextProcLogging.getLogger().log(
						Level.WARNING, "An exception occurred while processing a document. Skipping...", exc
					);
				}

				System.out.println();
				if (databaseEntitiesChanged) {
					System.out.println("> Committing changes to the database...");
					TextProcPersistence.get().flushEntities();
					System.out.print("> Changes committed.");

					databaseEntitiesChanged = false;
				}

				if (pageEndAction != null) {
					pageEndAction.run();
				}
			}
		} catch (final PersistenceException exc) {
			throw new ProcessingException(DATA_ACCESS_EXCEPTION_MESSAGE, exc);
		}
	}


	/**
	 * Stores a processed document in the database, from its processed attributes.
	 * This method starts and commits or rollbacks a JPA transaction, if no
	 * transaction is already active.
	 *
	 * @param documentType        The type of document that is being processed, and
	 *                            will be stored. It will be instantiated via
	 *                            reflection, so the module containing the type
	 *                            definition must open its package for deep
	 *                            reflection to the module containing this code.
	 * @param primaryKey          The primary key of the processed document.
	 * @param processedAttributes The processed attributes of the document. Their
	 *                            names (keys) must match the attributes of the
	 *                            concrete document type.
	 * @throws ProcessingException      If some error occurs during the operation.
	 * @throws IllegalArgumentException If any parameter is {@code null}.
	 * @throws PersistenceException     If some data access error occurs.
	 */
	protected final void saveProcessedDocument(
		@NonNull final Class<? extends ProcessedDocument> documentType, final int primaryKey, @NonNull final Map<String, String> processedAttributes
	) throws ProcessingException {
		final EntityManager thisThreadEntityManager = TextProcPersistence.get().getEntityManager();
		final EntityTransaction entityTransaction = thisThreadEntityManager.getTransaction();
		final ProcessedDocument entityToPersist;
		boolean startedTransaction = false;
		boolean transactionSuccessful = true;

		try {
			entityToPersist = documentType.getConstructor(Integer.class).newInstance(primaryKey);
		} catch (final ReflectiveOperationException exc) {
			throw new ProcessingException(
				"Document class implementation contract violation, or unappropriate permissions for reflective operation", exc
			);
		}

		if (!entityTransaction.isActive()) {
			entityTransaction.begin();
			startedTransaction = true;
		}

		try {
			thisThreadEntityManager.persist(entityToPersist);
			databaseEntitiesChanged = true;
		} catch (final PersistenceException exc) {
			transactionSuccessful = false;
			throw exc;
		} finally {
			if (startedTransaction && !transactionSuccessful) {
				entityTransaction.setRollbackOnly();
			}
		}

		// Change the entity processed data
		for (final Entry<String, String> processedAttribute : processedAttributes.entrySet()) {
			try {
				final String attributeName = processedAttribute.getKey();

				documentType
					.getMethod("set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1), String.class)
					.invoke(entityToPersist, processedAttribute.getValue());
			} catch (final ReflectiveOperationException exc) {
				throw new ProcessingException(
					"Couldn't invoke attribute setter", exc
				);
			}
		}
	}

	/**
	 * Constructs the SELECT SQL statement for retrieving unprocessed text documents
	 * with title from a database, using native queries.
	 *
	 * @return The described statement.
	 */
	protected final String buildUnprocessedDocumentWithTitleSelectStatement() {
		final Map<String, String> parameters = getParameters();
		final StringBuilder dmlSentenceBuilder = new StringBuilder(96);

		dmlSentenceBuilder.append("SELECT ");
		dmlSentenceBuilder.append(
			parameters.getOrDefault(PRIMARY_KEY_COLUMN_PROCESSING_STEP_PARAMETER_NAME, DEFAULT_PRIMARY_KEY_COLUMN_PROCESSING_STEP_PARAMETER)
		);
		dmlSentenceBuilder.append(", ");
		dmlSentenceBuilder.append(
			parameters.getOrDefault(TITLE_COLUMN_PROCESSING_STEP_PARAMETER_NAME, DEFAULT_TITLE_COLUMN_PROCESSING_STEP_PARAMETER)
		);
		dmlSentenceBuilder.append(", ");
		dmlSentenceBuilder.append(
			parameters.getOrDefault(TEXT_COLUMN_PROCESSING_STEP_PARAMETER_NAME, DEFAULT_TEXT_COLUMN_PROCESSING_STEP_PARAMETER)
		);
		dmlSentenceBuilder.append(" FROM ");
		dmlSentenceBuilder.append(
			parameters.get(TEXT_DOCUMENT_WITH_TITLE_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME)
		);

		return dmlSentenceBuilder.toString();
	}

	/**
	 * Constructs the SELECT SQL statement for retrieving unprocessed text documents
	 * without title from a database, using native queries.
	 *
	 * @return The described statement.
	 */
	protected final String buildUnprocessedDocumentSelectStatement() {
		final Map<String, String> parameters = getParameters();
		final StringBuilder dmlSentenceBuilder = new StringBuilder(64);

		dmlSentenceBuilder.append("SELECT ");
		dmlSentenceBuilder.append(
			parameters.getOrDefault(PRIMARY_KEY_COLUMN_PROCESSING_STEP_PARAMETER_NAME, DEFAULT_PRIMARY_KEY_COLUMN_PROCESSING_STEP_PARAMETER)
		);
		dmlSentenceBuilder.append(", ");
		dmlSentenceBuilder.append(
			parameters.getOrDefault(TEXT_COLUMN_PROCESSING_STEP_PARAMETER_NAME, DEFAULT_TEXT_COLUMN_PROCESSING_STEP_PARAMETER)
		);
		dmlSentenceBuilder.append(" FROM ");
		dmlSentenceBuilder.append(
			parameters.get(TEXT_DOCUMENT_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME)
		);

		return dmlSentenceBuilder.toString();
	}

	/**
	 * Returns the number of unprocessed text documents with title in the database.
	 * This method assumes a transaction is already active.
	 *
	 * @return The described number.
	 * @throws PersistenceException If some error occurs while executing SQL
	 *                              statements in the database.
	 */
	protected final long getUnprocessedDocumentsWithTitle() {
		return ((Number) TextProcPersistence.get().getEntityManager().createNativeQuery(
			"SELECT COUNT(*) FROM " +
			getParameters().get(TEXT_DOCUMENT_WITH_TITLE_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME)
		).getSingleResult()).longValue();
	}

	/**
	 * Returns the number of unprocessed text documents without title in the
	 * database. This method assumes a transaction is already active.
	 *
	 * @return The described number.
	 * @throws PersistenceException If some error occurs while executing SQL
	 *                              statements in the database.
	 */
	protected final long getUnprocessedDocuments() {
		return ((Number) TextProcPersistence.get().getEntityManager().createNativeQuery(
			"SELECT COUNT(*) FROM " +
			getParameters().get(TEXT_DOCUMENT_TABLE_NAME_PROCESSING_STEP_PARAMETER_NAME)
		).getSingleResult()).longValue();
	}

	/**
	 * Deletes all the processed documents of a given type from the database.
	 *
	 * @param <T>          The type of documents to delete.
	 * @param documentType The type of documents to delete.
	 * @throws IllegalArgumentException If {@code documentType} is {@code null}.
	 * @throws PersistenceException     If some error occurs while executing SQL
	 *                                  statements in the database.
	 */
	protected final <T extends ProcessedDocument> void deleteAllProcessedDocumentsOfType(@NonNull final Class<T> documentType) {
		final EntityManager entityManager = TextProcPersistence.get().getEntityManager();

		final CriteriaDelete<T> deleteCriteria = entityManager.getCriteriaBuilder().createCriteriaDelete(documentType);
		deleteCriteria.from(documentType);

		System.out.println("> Deleting " + documentType.getSimpleName() + " entities...");
		entityManager.createQuery(deleteCriteria).executeUpdate();

		databaseEntitiesChanged = true;
	}

	/**
	 * Executes the processing step implemented by this object. The processing step
	 * parameters are already validated and available upon request on
	 * {@link #getParameters()}. This method is invoked in the context of a JPA
	 * transaction that is started and committed or rolled back automatically.
	 *
	 * @throws ProcessingException If an exception occurs during execution.
	 */
	protected abstract void run() throws ProcessingException;

	/**
	 * Converts a column attribute value to a string.
	 *
	 * @param attribute The attribute to convert.
	 * @return The converted attribute value.
	 * @throws ProcessingException If the value could not be converted.
	 */
	private String columnAttributeToString(final Object attribute) throws ProcessingException {
		if (attribute != null) {
			return attribute.toString();
		} else {
			throw new ProcessingException("Unexpected column type");
		}
	}

	/**
	 * A consumer of data to be processed, which can throw a checked
	 * {@link ProcessingException}.
	 *
	 * @author Alejandro González García
	 *
	 * @param <T> The type of data to be processed.
	 * @see Consumer
	 */
	@FunctionalInterface
	public static interface ProcessingConsumer<T> {
		/**
		 * Performs the processing operation on the given argument.
		 *
		 * @param t The input argument.
		 * @see TransactionalMethod
		 */
		public void accept(final T t) throws ProcessingException;
	}

	/**
	 * A processing consumer that does nothing. It never throws exceptions.
	 *
	 * @author Alejandro González García
	 *
	 * @param <T> The type of objects consumed by this consumer.
	 */
	public static final class NullProcessingConsumer<T> implements ProcessingConsumer<T> {
		@Override
		public void accept(final T t) throws ProcessingException {
			// Do nothing
		}
	}
}
