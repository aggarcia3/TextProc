// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.persistence;

import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityTransaction;

import lombok.Getter;
import lombok.NonNull;

/**
 * Encapsulates a entity transaction with its creation time. Objects of this
 * class can be sorted by the entity transaction creation order. However,
 * two timestamped entity transactions are equal if and only if their underlying
 * entity transaction are equal.
 *
 * @author Alejandro González García
 */
final class TimestampedEntityTransaction implements Comparable<TimestampedEntityTransaction> {
	private static final AtomicLong NEXT_CREATION_ID = new AtomicLong();

	@Getter
	private final EntityTransaction entityTransaction;
	private final long creationId;

	/**
	 * Encapsulates the given entity transaction in a timestamped entity
	 * transaction.
	 *
	 * @param entityTransaction The entity transaction to encapsulate.
	 * @throws IllegalArgumentException If {@code entityTransaction} is {@code null}.
	 */
	public TimestampedEntityTransaction(@NonNull final EntityTransaction entityTransaction) {
		this.entityTransaction = entityTransaction;
		this.creationId = NEXT_CREATION_ID.getAndIncrement();
	}

	@Override
	public int compareTo(final TimestampedEntityTransaction o) {
		return (int) Math.max(Math.min(Integer.MIN_VALUE, creationId - o.creationId), Integer.MAX_VALUE);
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof TimestampedEntityTransaction &&
			((TimestampedEntityTransaction) other).entityTransaction.equals(entityTransaction);
	}

	@Override
	public int hashCode() {
		return entityTransaction.hashCode();
	}
}
