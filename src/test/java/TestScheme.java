import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scheme.Scheme.contains;

import org.junit.jupiter.api.Test;

public class TestScheme {

  /**
   * test PDataUtils.contains()
   */
  @Test
  public void testContains() {

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
    assertTrue(contains(oarr1, null));

    // * and false otherwise
    assertFalse(contains(oarr2, null));

  }

}