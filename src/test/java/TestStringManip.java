import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static scheme.StringManip.*;

public class TestStringManip {

  /**
   * test typify()
   */
  @Test
  public void test_typify() {

    // * if data is null or 0-length, return null Object
    String val = null;
    Entry<Class<?>,String> retval = typify(val, false, true, false, true);
    assertEquals(Object.class, retval.getKey());
    assertNull(retval.getValue());

    retval = typify("", false, true, false, true);
    assertEquals(Object.class, retval.getKey());
    assertNull(retval.getValue());

    // * if data is all whitespace, return String as-is
    val = " \t\r\n\b";
    retval = typify(val, false, true, false, true);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    // * if data is only whitespace surrounded by " or ', return String as-is
    val = val + "'" + val + "'" + val;
    retval = typify(val, false, true, false, true);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = val.replace("'", "\"");
    retval = typify(val, false, true, false, true);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    // * if data is only " or ', return Character as-is
    // ! only when commonTypes == false, otherwise return String as-is
    val = "'"; // bool01, commonTypes, postfixFL, parseDates
    retval = typify(val, false, false, false, false);
    assertEquals(Character.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = "\"";
    retval = typify(val, false, false, false, false);
    assertEquals(Character.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = "'";
    retval = typify(val, false, true, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = "\"";
    retval = typify(val, false, true, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    // * "false", "False", "FALSE" should return Boolean "false"
    val = "false";
    retval = typify(val, false, true, false, true);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("false", retval.getValue());

    val = "False";
    retval = typify(val, false, true, false, true);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("false", retval.getValue());

    val = "FALSE";
    retval = typify(val, false, true, false, true);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("false", retval.getValue());

    // * "true", "True", "TRUE" should return Boolean "true"
    val = "true";
    retval = typify(val, false, true, false, true);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("true", retval.getValue());

    val = "True";
    retval = typify(val, false, true, false, true);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("true", retval.getValue());

    val = "TRUE";
    retval = typify(val, false, true, false, true); // bool01, commonTypes, postfixFL, parseDates
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("true", retval.getValue());

    // * must contain true alias or false alias, nothing else
    val = ".true 2";
    retval = typify(val, false, false, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    // * ...except whitespace and surrounding ' or "
    val = "  'False '\t";
    retval = typify(val, false, false, false, false);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("false", retval.getValue());

    val = "\b\"TRUE \t\"\r";
    retval = typify(val, false, false, false, false);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("true", retval.getValue());

    // * 0 and 1 are interpreted as "false" and "true" respectively
    val = "0"; // only if bool01 == true
    retval = typify(val, true, false, false, false);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("false", retval.getValue());

    val = "1"; // only if bool01 == true
    retval = typify(val, true, false, false, false);
    assertEquals(Boolean.class, retval.getKey());
    assertEquals("true", retval.getValue());

    val = "2"; // cannot be interpreted as Boolean
    retval = typify(val, true, false, false, false);
    assertNotEquals(Boolean.class, retval.getKey());
    assertNotEquals("true", retval.getValue());

    // * ... or as Byte if bool01 == false, commonTypes == false
    val = "0"; // only if bool01 == false, commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Byte.class, retval.getKey());
    assertEquals("0", retval.getValue());

    val = "1"; // only if bool01 == false, commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Byte.class, retval.getKey());
    assertEquals("1", retval.getValue());

    // * ... or as Double if bool01 == false, commonTypes == true
    val = "0"; // only if bool01 == false, commonTypes == true
    retval = typify(val, false, true, false, false);
    assertEquals(Double.class, retval.getKey());
    assertEquals("0.0", retval.getValue());

    val = "1"; // only if bool01 == false, commonTypes == true
    retval = typify(val, false, true, false, false);
    assertEquals(Double.class, retval.getKey());
    assertEquals("1.0", retval.getValue());

    // * otherwise, anything within [-128, 127] returns Byte
    val = "-128"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Byte.class, retval.getKey());
    assertEquals("-128", retval.getValue());

    val = "127"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Byte.class, retval.getKey());
    assertEquals("127", retval.getValue());

    // * otherwise, anything within [-32768, 32767] returns Short
    val = "-129"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Short.class, retval.getKey());
    assertEquals("-129", retval.getValue());

    val = "128"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Short.class, retval.getKey());
    assertEquals("128", retval.getValue());

    val = "-32768"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Short.class, retval.getKey());
    assertEquals("-32768", retval.getValue());

    val = "32767"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Short.class, retval.getKey());
    assertEquals("32767", retval.getValue());

    // * otherwise, anything within [-2147483648, 2147483647] returns Integer
    val = "-32769"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Integer.class, retval.getKey());
    assertEquals("-32769", retval.getValue());

    val = "32768"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Integer.class, retval.getKey());
    assertEquals("32768", retval.getValue());

    val = "-2147483648"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Integer.class, retval.getKey());
    assertEquals("-2147483648", retval.getValue());

    val = "2147483647"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Integer.class, retval.getKey());
    assertEquals("2147483647", retval.getValue());

    // * otherwise, anything within [-9223372036854775808, 9223372036854775807] returns Long
    val = "-2147483649"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Long.class, retval.getKey());
    assertEquals("-2147483649", retval.getValue());

    val = "2147483648"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Long.class, retval.getKey());
    assertEquals("2147483648", retval.getValue());

    val = "-9223372036854775808"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Long.class, retval.getKey());
    assertEquals("-9223372036854775808", retval.getValue());

    val = "9223372036854775807"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Long.class, retval.getKey());
    assertEquals("9223372036854775807", retval.getValue());

    // * otherwise, anything that's not Infinity when parsed as Float is a Float
    val = "-9223372036854775809"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals(((Float)Float.parseFloat(val)).toString(), retval.getValue());

    val = "9223372036854775808"; // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals(((Float)Float.parseFloat(val)).toString(), retval.getValue());

    val = String.format("-%f", Float.MAX_VALUE); // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals(((Float)Float.parseFloat(val)).toString(), retval.getValue());

    val = String.format("%f", Float.MAX_VALUE); // only if commonTypes == false
    retval = typify(val, false, false, false, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals(((Float)Float.parseFloat(val)).toString(), retval.getValue());

    // * otherwise, anything that's not Infinity when parsed as Double is a Double
    val = String.format("%f", (double) Float.MAX_VALUE * -2);
    retval = typify(val, false, false, false, false);
    assertEquals(Double.class, retval.getKey());
    assertEquals(((Double)Double.parseDouble(val)).toString(), retval.getValue());

    val = String.format("%f", (double) Float.MAX_VALUE * 2);
    retval = typify(val, false, false, false, false);
    assertEquals(Double.class, retval.getKey());
    assertEquals(((Double)Double.parseDouble(val)).toString(), retval.getValue());

    val = String.format("-%f", Double.MAX_VALUE);
    retval = typify(val, false, false, false, false);
    assertEquals(Double.class, retval.getKey());
    assertEquals(((Double)Double.parseDouble(val)).toString(), retval.getValue());

    val = String.format("%f", Double.MAX_VALUE);
    retval = typify(val, false, false, false, false);
    assertEquals(Double.class, retval.getKey());
    assertEquals(((Double)Double.parseDouble(val)).toString(), retval.getValue());

    // * everything beyond the range of Double is returned as-is as a String
    val = "-2e999";
    retval = typify(val, false, false, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = "2e999";
    retval = typify(val, false, false, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    // * if number is integral and ends in 'l' or 'L', return Long
    // bool01, commonTypes, postfixFL, parseDates
    val = "42l"; // only if commonTypes == false and postfixFL == true
    retval = typify(val, false, false, true, false);
    assertEquals(Long.class, retval.getKey());
    assertEquals("42", retval.getValue());

    val = "0L"; // only if commonTypes == false and postfixFL == true
    retval = typify(val, false, false, true, false);
    assertEquals(Long.class, retval.getKey());
    assertEquals("0", retval.getValue());

    val = "0.0L"; // only if commonTypes == false and postfixFL == true
    retval = typify(val, false, false, true, false);
    assertEquals(String.class, retval.getKey());
    assertEquals("0.0L", retval.getValue());

    // * if number ends in 'f' or 'F', return Float
    // bool01, commonTypes, postfixFL, parseDates
    val = "42f"; // only if commonTypes == false and postfixFL == true
    retval = typify(val, false, false, true, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals("42.0", retval.getValue());

    val = "42.0f"; // only if commonTypes == false and postfixFL == true
    retval = typify(val, false, false, true, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals("42.0", retval.getValue());

    val = "0F"; // only if commonTypes == false and postfixFL == true
    retval = typify(val, false, false, true, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals("0.0", retval.getValue());

    // * ...if commonTypes == true or postfixFL = false, return String as-is
    val = " 42l ";
    retval = typify(val, false, true, true, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = " 0L ";
    retval = typify(val, false, true, true, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = " 42l ";
    retval = typify(val, false, false, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = " 0L ";
    retval = typify(val, false, false, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    // * check that commas are removed from numbers correctly
    val = "5,000,000,000"; // commonTypes == false => Long
    retval = typify(val, false, false, false, false);
    assertEquals(Long.class, retval.getKey());
    assertEquals("5000000000", retval.getValue());

    // * check that underscores are removed from numbers correctly
    val = "5_000"; // commonTypes == false => Short
    retval = typify(val, false, false, false, false);
    assertEquals(Short.class, retval.getKey());
    assertEquals("5000", retval.getValue());

    // * any number with a decimal point should be a float or a double
    val = "0.";
    retval = typify(val, false, false, false, false);
    assertEquals(Float.class, retval.getKey());
    assertEquals("0.0", retval.getValue());

    // * any number with a decimal point should be a float or a double
    val = "2.e99";
    retval = typify(val, false, false, false, false);
    assertEquals(Double.class, retval.getKey());
    assertEquals("2.0E99", retval.getValue());

    // * 'e' / 'E' also work with integral numbers, but not both at once
    // bool01, commonTypes, postfixFL, parseDates
    val = "2e3";
    retval = typify(val, false, false, false, false);
    assertEquals(Short.class, retval.getKey());
    assertEquals("2000", retval.getValue());

    // cause a NumberFormatException -> interpreted as String
    val = "2ee";
    retval = typify(val, false, false, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    val = "64E7";
    retval = typify(val, false, false, false, false);
    assertEquals(Integer.class, retval.getKey());
    assertEquals("640000000", retval.getValue());

    val = "2eE3";
    retval = typify(val, false, false, false, false);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

    // parse a String as a LocalDateTime in a known format
    retval = typify("09/22/1994 16:45", false, true, false, true);
    assertEquals(LocalDateTime.class, retval.getKey());

    // parse a String as a LocalDateTime in an unknown format
    val = "09b22b1994 16b45";
    retval = typify(val, false, true, false, true);
    assertEquals(String.class, retval.getKey());
    assertEquals(val, retval.getValue());

  }

  /**
   * test makeValidIdentifier()
   */
  @Test
  public void test_makeValidIdentifier() {

    // check that a null or empty String returns "X" as an identifier
    assertEquals("X", makeValidIdentifier(null));
    assertEquals("X", makeValidIdentifier(""));

    // check that a String of entirely invalid characters => "X"
    assertEquals("X", makeValidIdentifier("!@#$%^&*"));

    // check that invalid initial characters are removed
    assertEquals("a", makeValidIdentifier("%a"));
    assertEquals("a", makeValidIdentifier("&^a"));
    assertEquals("a", makeValidIdentifier("9#$a"));
    assertEquals("a", makeValidIdentifier("_a"));
    assertEquals("a", makeValidIdentifier("2a"));

    // check that any adjacent whitespace characters are replaced with a single underscore
    assertEquals("a_a", makeValidIdentifier("a a"));
    assertEquals("a_a", makeValidIdentifier("a  a"));
    assertEquals("a_a", makeValidIdentifier("a \t a"));
    assertEquals("a_a", makeValidIdentifier("a \t\n a"));

    // check that all invalid non-initial characters are removed
    assertEquals("ba", makeValidIdentifier("b!a"));
    assertEquals("ba", makeValidIdentifier("b()a"));

    // check that all trailing underscores are removed
    assertEquals("a", makeValidIdentifier("a_"));
    assertEquals("a", makeValidIdentifier("a__"));



  }

}