// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.persistence;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Big class of doom that decorates a {@link EntityManager}, executing an action
 * whenever a transaction is retrieved by {@link #getTransaction()}.
 *
 * @author Alejandro González García
 */
@AllArgsConstructor
final class EntityManagerTransactionDecorator implements EntityManager {
	@NonNull
	private final EntityManager baseEntityManager;
	@NonNull
	private final Consumer<? super EntityTransaction> gotTransactionAction;

	@Override
	public void persist(Object entity) {
		baseEntityManager.persist(entity);
	}

	@Override
	public <T> T merge(T entity) {
		return baseEntityManager.merge(entity);
	}

	@Override
	public void remove(Object entity) {
		baseEntityManager.remove(entity);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return baseEntityManager.find(entityClass, primaryKey);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		return baseEntityManager.find(entityClass, primaryKey, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		return baseEntityManager.find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		return baseEntityManager.find(entityClass, primaryKey, lockMode, properties);
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		return baseEntityManager.getReference(entityClass, primaryKey);
	}

	@Override
	public void flush() {
		baseEntityManager.flush();
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		baseEntityManager.setFlushMode(flushMode);
	}

	@Override
	public FlushModeType getFlushMode() {
		return baseEntityManager.getFlushMode();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {
		baseEntityManager.lock(entity, lockMode);
	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		baseEntityManager.lock(entity, lockMode, properties);
	}

	@Override
	public void refresh(Object entity) {
		baseEntityManager.refresh(entity);
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		baseEntityManager.refresh(entity, properties);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		baseEntityManager.refresh(entity, lockMode);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		baseEntityManager.refresh(entity, lockMode, properties);
	}

	@Override
	public void clear() {
		baseEntityManager.clear();
	}

	@Override
	public void detach(Object entity) {
		baseEntityManager.detach(entity);
	}

	@Override
	public boolean contains(Object entity) {
		return baseEntityManager.contains(entity);
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		return baseEntityManager.getLockMode(entity);
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		baseEntityManager.setProperty(propertyName, value);
	}

	@Override
	public Map<String, Object> getProperties() {
		return baseEntityManager.getProperties();
	}

	@Override
	public Query createQuery(String qlString) {
		return baseEntityManager.createQuery(qlString);
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return baseEntityManager.createQuery(criteriaQuery);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Query createQuery(CriteriaUpdate updateQuery) {
		return baseEntityManager.createQuery(updateQuery);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Query createQuery(CriteriaDelete deleteQuery) {
		return baseEntityManager.createQuery(deleteQuery);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return baseEntityManager.createQuery(qlString, resultClass);
	}

	@Override
	public Query createNamedQuery(String name) {
		return baseEntityManager.createNamedQuery(name);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return baseEntityManager.createNamedQuery(name, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		return baseEntityManager.createNativeQuery(sqlString);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Query createNativeQuery(String sqlString, Class resultClass) {
		return baseEntityManager.createNativeQuery(sqlString, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		return baseEntityManager.createNativeQuery(sqlString, resultSetMapping);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		return baseEntityManager.createNamedStoredProcedureQuery(name);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		return baseEntityManager.createStoredProcedureQuery(procedureName);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
		return baseEntityManager.createStoredProcedureQuery(procedureName, resultClasses);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		return baseEntityManager.createStoredProcedureQuery(procedureName, resultSetMappings);
	}

	@Override
	public void joinTransaction() {
		baseEntityManager.joinTransaction();
	}

	@Override
	public boolean isJoinedToTransaction() {
		return baseEntityManager.isJoinedToTransaction();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return baseEntityManager.unwrap(cls);
	}

	@Override
	public Object getDelegate() {
		return baseEntityManager.getDelegate();
	}

	@Override
	public void close() {
		baseEntityManager.close();
	}

	@Override
	public boolean isOpen() {
		return baseEntityManager.isOpen();
	}

	@Override
	public EntityTransaction getTransaction() {
		final EntityTransaction entityTransaction = baseEntityManager.getTransaction();

		gotTransactionAction.accept(entityTransaction);

		return entityTransaction;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return baseEntityManager.getEntityManagerFactory();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return baseEntityManager.getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel() {
		return baseEntityManager.getMetamodel();
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		return baseEntityManager.createEntityGraph(rootType);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName) {
		return baseEntityManager.createEntityGraph(graphName);
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName) {
		return baseEntityManager.getEntityGraph(graphName);
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		return baseEntityManager.getEntityGraphs(entityClass);
	}
}
