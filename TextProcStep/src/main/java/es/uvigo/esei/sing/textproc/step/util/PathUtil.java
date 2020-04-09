// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.sing.textproc.step.util;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * This class contains static utility methods for manipulating filesystem
 * objects that don't belong to the responsibilities of a single class.
 *
 * @author Alejandro González García
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtil {
	/**
	 * Attempts a best effort to clean up the file tree whose root is at the
	 * specified path (that is, all the files and directories inside a directory,
	 * like {@code rm -rf} does on Unix systems). Any I/O error that occurs is
	 * silently discarded.
	 *
	 * @param root The root location of file system (sub)tree that will be deleted.
	 * @throws IllegalArgumentException If {@code root} is {@code null}.
	 */
	public static void deletePathRecursively(@NonNull final Path root) {
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					// Ignore that the file doesn't exist, because another process deleted it
					// or whatever
					Files.deleteIfExists(file);

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
					// Delete the directory itself after its entries are deleted
					Files.deleteIfExists(dir);

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (final IOException ignored) {}
	}

	/**
	 * Returns an approximation to the total disk space consumed by a file tree
	 * whose root is at the specified path (in other words, the total disk space
	 * consumed by all the files inside a directory, including files in
	 * subdirectories).
	 * <p>
	 * If an I/O error occurs while processing a file or directory, which can be
	 * caused, for instance, by insufficient permissions to read it, it won't be
	 * counted for the total. On the other hand, even if no I/O error occurs, it is
	 * not guaranteed that the resulting number is the exact total size of the files
	 * in the file system because of transparent compression, sparse files and so
	 * on. Also, symbolic links are resolved.
	 * </p>
	 *
	 * @param root The root location of file system (sub)tree whose size will be
	 *             computed.
	 * @return The total approximate size of the disk space consumed by the
	 *         (sub)tree, in bytes.
	 * @throws IllegalArgumentException If {@code root} is {@code null}.
	 */
	public static long getTotalPathSize(@NonNull final Path root) {
		final VariableHolder<Long> totalSize = new VariableHolder<>(0L);

		try {
			Files.walkFileTree(
				root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
						// Ignore named pipes and such
						if (attrs.isRegularFile()) {
							totalSize.setVariable(totalSize.getVariable() + attrs.size());
						}

						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
						// Ignore failures
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
						// Ignore failures
						return FileVisitResult.CONTINUE;
					}
				}
			);
		} catch (final IOException ignored) {}

		return totalSize.getVariable();
	}
}
