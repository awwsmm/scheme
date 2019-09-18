package scheme;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Utility class which provides functions for working with {@link Collection}s,
 * {@link CharSequence}s, {@link Iterable}s, and arrays within <em>scheme</em>.
 */
public class CollectionManip {

  // * private default constructor because this is a utility class
  private CollectionManip(){}

  /**
   * Returns {@code true} if the {@code target} array contains the
   * {@code source} object, even if the {@code source} object is {@code null}.
   *
   * <p>Returns {@code false} if the {@code target} array is {@code null} or
   * empty. Uses the {@code source}'s {@code equals()} method to test for
   * equality with the members of the {@code target} array.</p>
   *
   * @param <T> class of {@code source} and the objects contained in
   * {@code target}
   * @param target array in which to search for the {@code source} object
   * @param source object to search for within the {@code target} array
   *
   * @return {@code true} if the {@code source} object can be found within the
   * {@code target} array
   *
   */
  public static final <T> boolean contains (T[] target, T source) {

    // if the target array is empty or length 0, it contains nothing
    if (target == null || target.length == 0) return false;

    // be careful with nulls
    if (source == null){ for (T t : target) if (t == null)        return true;
    } else {             for (T t : target) if (source.equals(t)) return true; }

    // return false by default
    return false;
  }

  /**
   * Returns {@code true} if any of the characters in the {@code source}
   * {@link CharSequence} are also contained within the {@code target}
   * {@link CharSequence}.
   *
   * <p>Returns {@code false} if either the {@code source} or the {@code target}
   * {@link CharSequence} is {@code null} or empty.</p>
   *
   * <p>(Note that both {@link String} and {@link StringBuilder} implement the
   * {@link CharSequence} interface.)</p>
   *
   * @param target {@link CharSequence} in which to search for the characters
   * which comprise the {@code source} {@link CharSequence}
   * @param source {@link CharSequence} whose characters should be searched for
   * within the {@code target} {@link CharSequence}
   *
   * @return {@code true} if neither the {@code target} nor the {@code source}
   * is {@code null} nor empty and if the characters contained within
   * {@code target} are a superset of those contained within {@code source}
   *
   */
  public static boolean containsAny (CharSequence target, CharSequence source) {

    // return false if either target or source is null
    if (target == null || source == null) return false;

    // get length of source and target
    int lSource = source.length();
    int lTarget = target.length();

    // return false if either target or source is empty
    if (lSource == 0 || lTarget == 0) return false;

    // loop over chars in both sequences and see if any match
    for (int tt = 0; tt < lTarget; ++tt)
      for (int ss = 0; ss < lSource; ++ss)
        if (source.charAt(ss) == target.charAt(tt))
          return true;

    // return false by default
    return false;
  }

  /**
   * Returns the narrowest common type (NCT) from a {@link Collection} of
   * {@code types}.
   *
   * <p>Attempts to infer the narrowest common type (NCT) which can be used to
   * describe the collection of {@code types} passed as the argument. For
   * example, if a {@link Float} and a {@link Double} are passed, the narrowest
   * type which can be used to describe both of those pieces of data without any
   * loss of precision is the {@link Double} type. But if a {@link Float} and a
   * {@link Byte} are passed, these both can be fully described by a
   * {@link Float}. If an {@link Integer} and a {@link Long} are passed,
   * {@link Long} is the NCT.</p>
   *
   * <p>If any numeric and non-numeric types are both contained within
   * {@code types}, then the NCT is necessarily {@link String}. The same goes
   * for mixed non-numeric types like {@link Character} and
   * {@link LocalDateTime}.</p>
   *
   * @param types a {@link Collection} of types for which the NCT should be found
   * @return the NCT from the given {@link Collection} of {@code types}
   * @throws IllegalStateException when the NCT cannot be determined or if
   * {@code types} is null or empty
   *
   **/
  public static Class<?> narrowestCommonType (Collection<Class<?>> types)
    throws IllegalStateException {

    // if types is null or empty, throw Exception
    if (types == null || types.size() < 1)
      throw new IllegalStateException("narrowestCommonType() : Collection is null or empty");

    // does the Collection contain these particular types?
    boolean hasString        = types.contains(String.class);
    if (hasString) return String.class; // String anywhere => return String

    boolean hasBoolean       = types.contains(Boolean.class);
    boolean hasByte          = types.contains(Byte.class);
    boolean hasCharacter     = types.contains(Character.class);
    boolean hasDouble        = types.contains(Double.class);
    boolean hasFloat         = types.contains(Float.class);
    boolean hasInteger       = types.contains(Integer.class);
    boolean hasLocalDateTime = types.contains(LocalDateTime.class);
    boolean hasLong          = types.contains(Long.class);
    boolean hasShort         = types.contains(Short.class);
    boolean hasObject        = types.contains(Object.class); // empty

    // groups of types
    boolean hasNumeric =
      hasDouble || hasFloat || hasLong || hasInteger || hasShort || hasByte;

    boolean hasNonNumeric =
      hasLocalDateTime || hasCharacter || hasBoolean;

    // if any numeric type and any non-numeric type, return String
    if (hasNumeric && hasNonNumeric) return String.class;

    // if any mixed non-numeric types, return String
    if ((hasBoolean && (hasCharacter || hasLocalDateTime)) ||
        (hasCharacter && hasLocalDateTime))
      return String.class;

    // at this point, types must only contain mixed numeric types or a single type
         if (hasDouble)  return Double.class;  // double is widest numeric type
    else if (hasFloat)   return Float.class;   // floating point? must use float or double
    else if (hasLong)    return Long.class;    // wider range than Integer
    else if (hasInteger) return Integer.class; // wider range than Short
    else if (hasShort)   return Short.class;   // wider range than Byte
    else if (hasByte)    return Byte.class;    // narrowest numeric type

    // at this point, types must only contain a single non-numeric type
         if (hasBoolean)       return Boolean.class;
    else if (hasCharacter)     return Character.class;
    else if (hasLocalDateTime) return LocalDateTime.class;
    else if (hasObject)        return Object.class;
    else throw new IllegalStateException("narrowestCommonType() : cannot determine type");

  }

  /**
   * Transposes a {@code List<List<T>>} ({@link List} of {@code List}s) and
   * returns the result.
   *
   * <p>If the given {@code listOfLists} contains any {@code null}
   * {@code List}s (as rows), then the returned transposed {@code List<List<T>>}
   * will contain{@code null} values down that row's corresponding column.</p>
   *
   * @param <T> type of objects contained within the inner {@link List}s
   * @param listOfLists {@code List} of {@code List}s to transpose
   * @return listOfLists, transposed, with {@code null} values representing
   * empty or missing "cells"
   *
   */
  public static <T> List<List<T>> transpose (List<List<T>> listOfLists) {

    // if null, return null
    if (listOfLists == null) return null;

    // get number of rows -> this becomes number of columns
    int nCols = listOfLists.size();

    // if no data, return empty List
    if (nCols == 0) return new ArrayList<>();

    // get maximum row length -> this becomes maximum column length
    int nRows = listOfLists.stream().mapToInt(l -> l.size()).max().orElse(0);

    // create List<List<?>> to return
    List<List<T>> retval = new ArrayList<>();

    // initialise retval with the correct number of appropriately-sized rows
    for (int rr = 0; rr < nRows; ++rr)
      retval.add(new ArrayList<>(Collections.nCopies(nCols, null)));

    // loop over rows, then over columns of original data
    for (int cc = 0; cc < nCols; ++cc) {

      // get row of original data (column of new, transposed data)
      List<T> col = listOfLists.get(cc);

      // if original data exists here, use; otherwise, leave null
      for (int rr = 0; rr < nRows; ++rr)
        if (col.size() > rr) retval.get(rr).set(cc, col.get(rr));

    }

    return retval;

  }

  /**
   * Returns the "similarity" of two {@link Iterable}s on a scale of
   * {@code [0.0, 1.0]}.
   *
   * <p>If the two {@link Iterable}s are of different lengths, or either or both
   * of the {@link Iterable}s are {@code null}, they are not comparable and this
   * method returns a similarity of {@code 0.0}.</p>
   *
   * <p>Otherwise, the {@link Iterable}s are compared element-by-element with
   * the {@code equals()} method of the element from {@code A}. For each pair
   * of elements for which {@code equals()} returns {@code true}, a
   * "{@code similarity}" counter is incremented. Once all pairs of elements
   * have been compared, {@code similarity} is divided by the number of elements
   * in the {@link Iterable}s and the result is returned as a
   * {@code double}.</p>
   *
   * <p>The two {@link Iterable}s must contain the same class of data
   * ({@code U}), for instance, two {@code List<String>}s can be compared, or a
   * {@code List<String>} and a {@code Set<String>}, but not a
   * {@code List<String>} and a {@code List<Boolean>}. If a generic
   * {@code Iterable} is used (ie. {@code List}, with no type specified), the
   * type is asumed to be {@code Object}. So if two generic {@code Iterable}s
   * are compared, they will both be assumed to contain {@code Object}s, which
   * -- though it will not throw any errors -- is usually not what you want.</p>
   *
   * <p>See {@link Iterable} for all classes and subinterfaces which implement
   * the {@link Iterable} interface. Ones of note are {@link Collection},
   * {@link List}, and {@link Set}.</p>
   *
   * @param <U> type of objects contained within the {@link Iterable}s
   * @param <T> {@code Iterable<U>} or a subclass
   * @param A first {@link Iterable} to compare
   * @param B second {@link Iterable} to compare
   *
   * @return the "similarity" of two {@link Iterable}s on a scale of
   * {@code [0.0, 1.0]}
   *
   */
  public static <U extends Object, T extends Iterable<U>> double similarity (T A, T B) {

    // if either list is null, 0% similarity
    if (A == null || B == null) return 0.0;

    // get iterators
    Iterator<U> itA = A.iterator();
    Iterator<U> itB = B.iterator();

    // number of elements in A; similarity b/t A and B
    int size = 0;
    double similarity = 0.0;

    // keep track of equal elements
    while (itA.hasNext()) {

      // if lists are different sizes, they're not comparable
      if (!itB.hasNext()) return 0.0;

      // otherwise, get next element of A and increment size counter
      U nextA = itA.next();  ++size;

      // if next A element is null, increment B but don't increment similarity
      if (nextA == null) { itB.next(); continue; }

      // increase similarity if element in A equals element in B
      if (nextA.equals(itB.next())) ++similarity;
    }

    // if B has more elements left, lists are different sizes
    if (itB.hasNext()) return 0.0;

    // if A is empty, no comparison can be made
    if (size < 1) return 0.0;

    // normalise similarity and return
    return similarity / size;
  }

}
