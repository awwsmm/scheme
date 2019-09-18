import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import static scheme.DateTimeFormats.*;

import java.util.List;
import java.util.Locale;

public class TestDateTimeFormats {

  // number of default formats (see resources/dateFormats.dat)
  static final int N = 11;

  /**
   * test get()
   */
  @Test
  public void test_get() {

    // check the default List -- see resources/dateFormats.dat
    List<DateTimeFormatter> formats = get();
    assertEquals(N, formats.size());

    // check the first and last formats
    Locale locale = new Locale("en", "IE");
    DateTimeFormatter first = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", locale);
    DateTimeFormatter last  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm",    locale);

    assertEquals(first.toString(), formats.get(0).toString());
    assertEquals(last.toString(),  formats.get(N-1).toString());

  }

  /**
   * test add() and remove()
   */
  @Test
  public void test_add_remove() {

    // check the default List -- see resources/dateFormats.dat
    assertEquals(N, get().size());

    // add some new formats to the list
    add(new Locale("en"),             "dd-MM-yyyy HH.mm.ss");
    add(new Locale("en", "IE"),       "dd-MM-yyyy HH.mm.ss");
    add(new Locale("en", "IE", "??"), "dd-MM-yyyy HH.mm.ss");

    // check the modified List
    assertEquals(N+3, get().size());

    // check the new formats
    DateTimeFormatter last = DateTimeFormatter.ofPattern(
      "dd-MM-yyyy HH.mm.ss", new Locale("en", "IE", "??"));

    assertEquals(last.toString(), get().get(N+2).toString());

    // remove the new formats
    remove(N);
    remove(N);
    remove(N);

    // check the length again
    assertEquals(N, get().size());

    // check the last format (see dateFormats.dat)
    last = DateTimeFormatter.ofPattern(
      "dd/MM/yyyy HH:mm", new Locale("en", "IE", "XX"));

    assertEquals(last.toString(), get().get(N-1).toString());

    // test that null or empty format strings give an error
    assertFalse(add(new Locale("en"), null));
    assertFalse(add(new Locale("en"), ""));

    // test that null Locale gives an error
    assertFalse(add(null, "dd/MM/yyyy HH:mm"));

  }

  /**
   * test strings()
   */
  @Test
  public void test_strings() {

    // get the List<String> from strings()
    List<String> strings = strings();

    // check first and last format (see dateFormats.dat)
    assertEquals("dd.MM.yyyy HH:mm:ss", strings.get(0));
    assertEquals("dd/MM/yyyy HH:mm",    strings.get(N-1));

  }
  
}