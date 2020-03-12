// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.persistence;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceException;

import org.hibernate.cfg.Configuration;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;

import es.uvigo.esei.sing.textproc.logging.TextProcLogging;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Entry point to the TextProc persistence access functionalities.
 *
 * @author Alejandro González García
 * @implNote The implementation of this class is thread-safe.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextProcPersistence {
	private static final String NOT_RUNNING_ERROR_MESSAGE = "The persistence access layer is not running";

	private volatile EntityManagerFactory entityManagerFactory = null;
	private final Map<Thread, EntityManager> threadEntityManager = new ConcurrentHashMap<>();
	private final Set<TimestampedEntityTransaction> dirtyTransactions = new ConcurrentSkipListSet<>();
	private final Object runningStatusChangeLock = new Object();

	/**
	 * Utility class for the <a href=
	 * "https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">initialization
	 * on demand holder idiom</a>.
	 *
	 * @author Alejandro González García
	 */
	private static final class TextProcPersistenceInstanceHolder {
		private static final TextProcPersistence INSTANCE = new TextProcPersistence();
	}

	/**
	 * Returns the only instance of this class in the JVM. This method always
	 * returns the same object.
	 *
	 * @return The only instance of this class in the JVM.
	 */
	public static TextProcPersistence get() {
		return TextProcPersistenceInstanceHolder.INSTANCE;
	}

	/**
	 * Checks whether this persistence access layer is running. When not running,
	 * most method calls on it will throw a {@link IllegalStateException}, as
	 * documented.
	 *
	 * @return True if the persistence access layer is running, false otherwise.
	 */
	public boolean isRunning() {
		return entityManagerFactory != null;
	}

	/**
	 * Starts the TextProc persistence access layer, so it becomes ready to process
	 * data access operations.
	 *
	 * @param entityTypes A set of entity types that the JPA provider will analyze,
	 *                    in order to make them available for use. The set doesn't
	 *                    need to be modifiable.
	 *
	 * @throws PersistenceException     If some error occurs while instantiating a
	 *                                  entity manager factory for the persistence
	 *                                  unit.
	 * @throws IllegalArgumentException If {@code entityTypes} is {@code null}, or
	 *                                  contains a {@code null} element.
	 */
	public void start(@NonNull final Set<Class<?>> entityTypes) {
		synchronized (runningStatusChangeLock) {
			if (entityManagerFactory == null) {
				final Configuration entityManagerFactoryConfiguration = new Configuration();

				// We need to use Hibernate specific API to add entities that are not in
				// our JAR, because they were provided by other modules, and do this before
				// creating the session factory (EntityManagerFactory), because it is immutable.
				// It's not pretty, but it's the only possibility that allows dynamic entities
				for (final Class<?> entityType : entityTypes) {
					if (entityType == null) {
						throw new IllegalArgumentException("A entity type can't be null");
					}

					entityManagerFactoryConfiguration.addAnnotatedClass(entityType);

					// Add mapped superclasses too
					Class<?> entitySuperclass = entityType.getSuperclass();
					while (
						entitySuperclass != null &&
						entitySuperclass.getDeclaredAnnotation(MappedSuperclass.class) != null
					) {
						entityManagerFactoryConfiguration.addAnnotatedClass(entitySuperclass);
						entitySuperclass = entitySuperclass.getSuperclass();
					}
				}

				final ParsedPersistenceXmlDescriptor persistenceDescriptor = PersistenceXmlParser.locateNamedPersistenceUnit(
					TextProcPersistence.class.getResource("/META-INF/persistence.xml"),
					TextProcPersistence.class.getSimpleName()
				);

				// Code examination of the previous method call reveals that an exception should
				// be thrown in this case. But Javadoc is weak about this, so be safe
				if (persistenceDescriptor == null) {
					throw new PersistenceException("Couldn't parse the persistence descriptor");
				}

				entityManagerFactory = entityManagerFactoryConfiguration.addProperties(
					persistenceDescriptor.getProperties()
				).buildSessionFactory();
			}
		}
	}

	/**
	 * Stops the TextProc persistence access layer, so that all pending transactions
	 * are finished and no new data access operations can start until it is started
	 * again (if ever).
	 */
	public void stop() {
		synchronized (runningStatusChangeLock) {
			final EntityManagerFactory currentEntityManagerFactory = entityManagerFactory;
			if (currentEntityManagerFactory != null) {
				// Null the attribute first, so other threads see us as stopped
				entityManagerFactory = null;

				// Now close the entity manager factory
				currentEntityManagerFactory.close();
				flushEntities(true);
			}
		}
	}

	/**
	 * Returns a entity manager ready for use for the current thread. At any given
	 * time, there can be one entity manager per thread that requests one.
	 *
	 * @return The entity manager exclusive to the current thread.
	 * @throws IllegalStateException If the persistence access layer is not running.
	 */
	public EntityManager getEntityManager() {
		if (!isRunning()) {
			throw new IllegalStateException(NOT_RUNNING_ERROR_MESSAGE);
		}

		return threadEntityManager.compute(
			Thread.currentThread(),
			(final Thread key, final EntityManager value) -> {
				return value == null ?
					new EntityManagerTransactionDecorator(
						entityManagerFactory.createEntityManager(),
						(final EntityTransaction transaction) ->
							dirtyTransactions.add(new TimestampedEntityTransaction(transaction))
					)
				: value;
			}
		);
	}

	/**
	 * Commits the dirty transactions which were not rolled back, and then closes
	 * all the opened entity managers. If other parts of the application are using
	 * entity transactions when this method is invoked, its outcome is undefined.
	 * Otherwise, when this method returns, no transaction or entity manager will
	 * remain to be committed, closed or strongly referenced by internal data
	 * structures (therefore, they can be garbage collected, barring strong
	 * references elsewhere). Subsequent calls to {@link #getEntityManager()} will
	 * return new entity manager objects no matter what.
	 *
	 * @throws IllegalStateException If the application is not running.
	 */
	public void flushEntities() {
		flushEntities(false);
	}

	/**
	 * Commits the dirty transactions which were not rolled back, and then closes
	 * all the opened entity managers. If other parts of the application are using
	 * entity transactions when this method is invoked, its outcome is undefined.
	 * Otherwise, when this method returns, no transaction or entity manager will
	 * remain to be committed, closed or strongly referenced by internal data
	 * structures (therefore, they can be garbage collected, barring strong
	 * references elsewhere). Subsequent calls to {@link #getEntityManager()} will
	 * return new entity manager objects no matter what.
	 *
	 * @param ignoreRunningStatus If {@code true}, the code won't check that this
	 *                            data access layer is running before finishing
	 *                            pending transactions. This is only meant for
	 *                            internal use.
	 *
	 * @throws IllegalStateException If the application is not running and
	 *                               {@code ignoreRunningStatus} is set to
	 *                               {@code false}.
	 */
	private void flushEntities(final boolean ignoreRunningStatus) {
		if (!ignoreRunningStatus && !isRunning()) {
			throw new IllegalStateException(NOT_RUNNING_ERROR_MESSAGE);
		}

		// Commit transactions in the same order that were created.
		// This avoids database lock wait timeouts that can occur with RDBMS
		// that schedule transactions in a way that doesn't guarantee that no
		// deadlocks will occur when committing them in any order. See:
		// https://stackoverflow.com/questions/13966467/how-to-avoid-lock-wait-timeout-exceeded-exception
		final Iterator<TimestampedEntityTransaction> dirtyTransactionsIter = dirtyTransactions.iterator();
		while (dirtyTransactionsIter.hasNext()) {
			final EntityTransaction transaction = dirtyTransactionsIter.next().getEntityTransaction();

			try {
				if (transaction.isActive()) {
					if (transaction.getRollbackOnly()) {
						transaction.rollback();
					} else {
						transaction.commit();
					}
				}
			} catch (final Exception exc) {
				TextProcLogging.getLogger().log(
					Level.WARNING,
					"An exception occurred while committing or rolling back a transaction. The database status may be inconsistent",
					exc
				);
			}

			dirtyTransactionsIter.remove();
		}

		threadEntityManager
			.values().parallelStream()
			.forEach(
				(final EntityManager entityManager) -> {
					try {
						if (entityManager.isOpen()) {
							entityManager.close();
						}
					} catch (final Exception exc) {
						TextProcLogging.getLogger().log(
							Level.WARNING, "An exception occurred while closing a entity manager", exc
						);
					}
				}
			);
		threadEntityManager.clear();
	}
}
