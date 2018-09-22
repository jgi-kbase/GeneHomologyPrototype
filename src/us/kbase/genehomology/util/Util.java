package us.kbase.genehomology.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

/** Miscellaneous utility methods.
 * @author gaprice@lbl.gov
 *
 */
public class Util {
	
	/** Throw an exception if the given string is null or whitespace only.
	 * @param s the string to test.
	 * @param name the name of the string to include in the exception.
	 * @throws IllegalArgumentException if the string is null or whitespace only.
	 */
	public static void exceptOnEmpty(final String s, final String name)
			throws IllegalArgumentException {
		if (isNullOrEmpty(s)) {
			throw new IllegalArgumentException(name + " cannot be null or whitespace only");
		}
	}

	/** Check if a string is null or whitespace only.
	 * @param s the string to test.
	 * @return true if the string is null or whitespace only, false otherwise.
	 */
	public static boolean isNullOrEmpty(final String s) {
		return s == null || s.trim().isEmpty();
	}
	
	/** Check that the provided collection is not null and contains no null elements.
	 * @param col the collection to test.
	 * @param name the name of the collection to use in any error messages.
	 */
	public static <T> void checkNoNullsInCollection(final Collection<T> col, final String name) {
		checkNotNull(col, name);
		for (final T item: col) {
			if (item == null) {
				throw new NullPointerException("Null item in collection " + name);
			}
		}
	}
	
	/** Check that the provided collection is not null and contains no null or whitespace-only
	 * strings.
	 * @param strings the collection to check.
	 * @param name the name of the collection to use in any error messages.
	 */
	public static void checkNoNullsOrEmpties(final Collection<String> strings, final String name) {
		checkNotNull(strings, name);
		for (final String s: strings) {
			if (isNullOrEmpty(s)) {
				throw new IllegalArgumentException(
						"Null or whitespace only string in collection " + name);
			}
		}
	}
}
