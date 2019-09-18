import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scheme.CollectionManip.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCollectionManip {

  /**
   * test contains()
   */
  @Test
  public void test_contains() {

    // create some objects to play with later
    Object  o = new Object();
    Integer i = 42;
    String  s = "test";

    // create some arrays
    Object[] oarr0 = new Object[]{};
    Object[] oarr1 = new Object[]{ null, o, null, i, null, s };
    Object[] oarr2 = new Object[]{ o, i, s };

    // * test that a null target array always returns false (contains nothing)
    assertFalse(contains(null, null));
    assertFalse(contains(null, o));

    // * test that an empty target array always returns false (contains nothing)
    assertFalse(contains(oarr0, null));
    assertFalse(contains(oarr0, o));

    // * test that contains() returns true when the target contains the source
    assertTrue(contains(oarr1, o));
    assertTrue(contains(oarr1, s));
    assertTrue(contains(oarr1, "test")); // note: we use equals(), not ==
    assertTrue(contains(oarr2, "test")); // note: we use equals(), not ==
    assertTrue(contains(oarr1, null));

    // * and false otherwise
    assertFalse(contains(oarr2, null));
    assertFalse(contains(oarr2, 6));

  }

  /**
   * test containsAny()
   */
  @Test
  public void test_containsAny() {

    // create some CharSequences
    String s = "hello";

    // * test that a null target always returns false (contains nothing)
    assertFalse(containsAny(null, s));

    // * test that an empty target always returns false (contains nothing)
    assertFalse(containsAny("", s));

    // * test that a null source always returns false (contains nothing)
    assertFalse(containsAny(s, null));

    // * test that an empty target always returns false (contains nothing)
    assertFalse(containsAny(s, ""));

    // * test that contains() returns true when the target contains the source
    assertTrue(containsAny(s, "hx"));
    assertTrue(containsAny(s, "h"));
    assertTrue(containsAny(s, "qe"));

    // * and false otherwise
    assertFalse(containsAny(s, "x"));
    assertFalse(containsAny(s, "q"));

  }

  /**
   * test transpose()
   */
  @Test
  public void test_transpose() {

    // create a List<List<>> to play around with
    List<List<String>> ll = new ArrayList<>();

    // add some rows to it
    ll.add(Arrays.asList("A", "B"));
    ll.add(Arrays.asList("C"));
    ll.add(Arrays.asList("D", "E", "F"));

    // above "table" looks like... transpose so it looks like...

    //  | A | B |   |        | A | C | D |
    //  | C |   |   |   =>   | B |   | E |
    //  | D | E | F |        |   |   | F |

    List<List<String>> tt = transpose(ll);

    // check some bits of the transposed table
    assertEquals("B", tt.get(1).get(0));
    assertEquals("D", tt.get(0).get(2));
    assertNull(tt.get(2).get(0));

    // ensure that null List<List<>> returns null
    assertNull(transpose(null));

    // ensure that empty List<List<>> returns empty List<List<>>
    assertEquals(0, transpose(new ArrayList<>()).size());

  }

  /**
   * test narrowestCommonType()
   */
  @Test
  public void test_narrowestCommonType() {

    // ensure that an Exception is thrown if the Collection of Classes is null or empty
    assertThrows(IllegalStateException.class, () -> narrowestCommonType(null));
    assertThrows(IllegalStateException.class, () -> narrowestCommonType(new ArrayList<>()));

    //--------------------------------------------------------------------------

    // create a List<> with only the Byte class
    List<Class<?>> types = new ArrayList<>();
    types.add(Byte.class);

    // test that Byte is the NCT
    assertEquals(Byte.class, narrowestCommonType(types));

    // add Short and ensure NCT is Short
    types.add(Short.class);
    assertEquals(Short.class, narrowestCommonType(types));

    // add Integer and ensure NCT is Integer
    types.add(Integer.class);
    assertEquals(Integer.class, narrowestCommonType(types));

    // add Long and ensure NCT is Long
    types.add(Long.class);
    assertEquals(Long.class, narrowestCommonType(types));

    // add Float and ensure NCT is Float
    types.add(Float.class);
    assertEquals(Float.class, narrowestCommonType(types));

    // add Double and ensure NCT is Double
    types.add(Double.class);
    assertEquals(Double.class, narrowestCommonType(types));

    //--------------------------------------------------------------------------

    // add a non-numeric type and ensure that NCT is String
    types.add(LocalDateTime.class);
    assertEquals(String.class, narrowestCommonType(types));

    //--------------------------------------------------------------------------

    // ensure that Boolean => Boolean
    assertEquals(Boolean.class, narrowestCommonType(Arrays.asList(Boolean.class)));

    // ensure that Character => Character
    assertEquals(Character.class, narrowestCommonType(Arrays.asList(Character.class)));

    // ensure that LocalDateTime => LocalDateTime
    assertEquals(LocalDateTime.class, narrowestCommonType(Arrays.asList(LocalDateTime.class)));

    // ensure that Object => Object
    assertEquals(Object.class, narrowestCommonType(Arrays.asList(Object.class)));

    // ensure that String => String
    assertEquals(String.class, narrowestCommonType(Arrays.asList(String.class)));

    //--------------------------------------------------------------------------

    // ensure that mixed non-numeric types return String
    assertEquals(String.class, narrowestCommonType(Arrays.asList(Character.class, Boolean.class)));
    assertEquals(String.class, narrowestCommonType(Arrays.asList(LocalDateTime.class, Boolean.class)));
    assertEquals(String.class, narrowestCommonType(Arrays.asList(Character.class, LocalDateTime.class)));

    // give an unknown type and ensure that an Exception is thrown
    class DummyClass {};
    assertThrows(IllegalStateException.class, () -> narrowestCommonType(Arrays.asList(DummyClass.class)));



  }

  /**
   * test similarity()
   */
  @Test
  public void test_similarity() {

    // check that similarity is 0.0 if either Iterable is null
    assertEquals(0.0, similarity(null, Arrays.asList(42)));
    assertEquals(0.0, similarity(Arrays.asList(42), null));
    assertEquals(0.0, similarity(null, null));

    // check that similarity is 0.0 if either Iterable is empty
    assertEquals(0.0, similarity(new ArrayList<>(), Arrays.asList(42)));
    assertEquals(0.0, similarity(Arrays.asList(42), new ArrayList<>()));
    assertEquals(0.0, similarity(new ArrayList<>(), new ArrayList<>()));

    // check that similarity is 0.0 if Iterables are different sizes
    assertEquals(0.0, similarity(Arrays.asList(19, 42), Arrays.asList(42)));
    assertEquals(0.0, similarity(Arrays.asList(42), Arrays.asList(19, 42)));

    // check that equivalent Iterables have 1.0 similarity
    assertEquals(1.0, similarity(Arrays.asList(19, 42), Arrays.asList(19, 42)));

    // check that each different element reduces the similarity
    assertEquals(0.5, similarity(Arrays.asList(19, 42), Arrays.asList(19, -1)));
    assertEquals(0.0, similarity(Arrays.asList(19, 42), Arrays.asList(-1, -1)));
    assertEquals(0.5, similarity(Arrays.asList(19, -1), Arrays.asList(19, 42)));
    assertEquals(0.0, similarity(Arrays.asList(-1, -1), Arrays.asList(19, 42)));

    // check that each null element reduces the similarity
    assertEquals(0.5, similarity(Arrays.asList(19, 42), Arrays.asList(19, null)));
    assertEquals(0.0, similarity(Arrays.asList(19, 42), Arrays.asList(null, null)));
    assertEquals(0.5, similarity(Arrays.asList(19, null), Arrays.asList(19, 42)));
    assertEquals(0.0, similarity(Arrays.asList(null, null), Arrays.asList(19, 42)));

  }

}