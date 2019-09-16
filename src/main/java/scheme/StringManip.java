package scheme;

import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class StringManip {

  // * private default constructor because this is a utility class
  private StringManip(){}

  /**
   * Attempts to parse a given {@link String} of {@code data} and infer the type
   * of information it represents.
   *
   * <p>Using the {@code _.parse_(String)} methods for {@code _} = {@link Byte},
   * {@link Short}, etc., this method finds the narrowest type of
   * ({@link Boolean}, {@link Byte}, {@link Short}, {@link Integer},
   * {@link Long}, {@link Float}, {@link Double}, {@link Character},
   * {@link LocalDateTime}, {@link String}, in that order) which can be used to
   * losslessly represent the data contained in the provided {@code data}
   * {@link String}. The inferred class of the interpreted data and the
   * (possibly modified) argument itself are then returned in an
   * {@code Entry<Class, String>}, where the first argument is the class which
   * has been inferred and the second argument is the (possibly modified)
   * user-supplied {@code data}.</p>
   *
   * <p>{@code null} or empty {@code data} {@link String}s are assigned class
   * {@link Object} and the returned {@code data} will be {@code null}.
   * {@code data} {@link String}s which consist only of whitespace will be
   * assigned class {@link String} and the {@code data} will be returned as-is.
   * Otherwise, leading and trailing whitespace will be trimmed with
   * {@link String#trim String.trim()}, any surrounding double- or single-quotes
   * will be removed, and whitespace will be trimmed again. If the resulting
   * {@link String} is a single non-whitespace, non-numeric character, the
   * returned class will be {@link Character}. Any user-supplied string which
   * consists of only (optional) whitespace and a single pair of matching single
   * or double quotes will also be returned as-is and classed as a
   * {@link String}.</p>
   *
   * <p>The <strong>{@code bool01}</strong> flag will interpret {@code '0'} and
   * {@code '1'} as {@code false} and {@code true}, respectively. If this flag
   * is disabled, they will instead be interpreted as {@link Byte}s. The
   * characters {@code '0'} through {@code '9'} will also be interpreted as
   * {@link Byte}s, unless the {@code commonTypes} flag is enabled. The
   * <strong>{@code commonTypes}</strong> flag restricts the returned class to
   * just four classes -- {@link Boolean}, {@link String}, {@link Double}, and
   * {@link LocalDateTime} (the latter, provided that {@code parseDates} is set
   * to {@code true}).</p>
   *
   * <p>The <strong>{@code postfixFL}</strong> flag allows for the
   * interpretation of explicit {@code float}s and {@code long}s. If the
   * {@link String} contains an integer immediately followed by an {@code 'l'}
   * or an {@code 'L'}, it will be interpreted as a {@link Long} (ex:
   * {@code "42L"}). If it contains a floating-point number immediately followed
   * by an {@code 'f'} or an {@code 'F'}, it will be interpreted as a
   * {@link Float} (ex: {@code "42f"}). </p>
   *
   * <p>If the provided {@code data} cannot be parsed as any of the eight
   * primitive wrapper classes, it will be returned as-is (without being
   * stripped of surrounding whitespace or quotes, as a {@link String},
   * <em>unless</em> it can be parsed as a {@link LocalDateTime}, and the
   * <strong>{@code parseDates}</strong> flag is enabled. Recognition of
   * date-time-formatted {@link String}s is handled by {@link DateTimeFormats}.
   * To check which date-time formats are currently being recognized, use
   * {@link DateTimeFormats#strings()} or {@link DateTimeFormats#get()}. If the
   * given {@code data} {@link String} is not recognized as any other type, then
   * when it's parsed as a {@link LocalDateTime}, an attempt will be made to
   * match it against every recognised date-time format in the order that they
   * are stored, until one succeeds. For this reason, you should only add
   * date-time formats which you expect to encounter while parsing your file(s)
   * of interest.</p>
   *
   * <p>For <em>thorough</em> categorization of {@code data} {@link String}s,
   * {@code commonTypes} should be set to {@code false} and {@code parseDates}
   * should be set to {@code true}. For <em>fast</em> categorization of
   * {@code data} {@link String}s, {@code commonTypes} should be set to
   * {@code true} and {@code parseDates} should be set to {@code false}.</p>
   *
   * <h2><strong>Usage:</strong></h2>
   * First, define some aliases for brevity's sake:
   *
   * <pre>{@code
   * jshell> import static scheme.StringManip.typify
   * jshell> boolean T = true
   * jshell> boolean F = false
   * }</pre>
   *
   * {@code null} and empty {@link String}s are returned as {@code null}
   * {@link Object}s:
   * <pre>{@code
   * jshell> typify(null, T, F, T, F)
   * $14 ==> class java.lang.Object=null
   *
   * jshell> typify("", T, F, T, F)
   * $15 ==> class java.lang.Object=null
   * }</pre>
   *
   * Mismatched single- or double-quotes are returned as {@link Character}s
   * (after whitespace is trimmed):
   * <pre>{@code
   * jshell> typify(" ' \b", T, F, T, F)
   * $16 ==> class java.lang.Character='
   *
   * jshell> typify("\t \"\r", T, F, T, F)
   * $17 ==> class java.lang.Character="
   * }</pre>
   *
   * {@code data} containing only whitespace and matched quotes are returned
   * as-is:
   * <pre>{@code
   * jshell> typify(" ' ' ", T, F, T, F)
   * $18 ==> class java.lang.String= ' '
   *
   * jshell> typify("' '", T, F, T, F)
   * $19 ==> class java.lang.String=' '
   * }</pre>
   *
   * {@code '0'} and {@code '1'} are returned as {@link Boolean}s if
   * {@code bool01} (the first flag) is set to {@code true}:
   * <pre>{@code
   * jshell> typify("0", T, F, T, F)
   * $20 ==> class java.lang.Boolean=false
   *
   * jshell> typify("1", T, F, T, F)
   * $21 ==> class java.lang.Boolean=true
   * }</pre>
   *
   * ...and they're returned as {@link Byte}s otherwise:
   * <pre>{@code
   * jshell> typify("0", F, F, T, F)
   * $22 ==> class java.lang.Byte=0
   *
   * jshell> typify("1", F, F, T, F)
   * $23 ==> class java.lang.Byte=1
   * }</pre>
   *
   * ...unless {@code commonTypes} (the second flag) is enabled:
   * <pre>{@code
   * jshell> typify("0", F, T, T, F)
   * $24 ==> class java.lang.Double=0.0
   *
   * jshell> typify("1", F, T, T, F)
   * $25 ==> class java.lang.Double=1.0
   * }</pre>
   *
   * There are a few hard-coded aliases for {@code false} as well:
   * <pre>{@code
   * jshell> typify("false", F, T, T, F)
   * $26 ==> class java.lang.Boolean=false
   *
   * jshell> typify("False", F, T, T, F)
   * $27 ==> class java.lang.Boolean=false
   *
   * jshell> typify("FALSE", F, T, T, F)
   * $28 ==> class java.lang.Boolean=false
   * }</pre>
   * <p>...and similar ones for {@code true}.</p>
   *
   * <p>Postfixes for {@code long}s and {@code float}s are recognized when
   * {@code postfixFL} (the third flag) is set to {@code true}:</p>
   * <pre>{@code
   * jshell> typify("42L", F, F, T, F)
   * $29 ==> class java.lang.Long=42
   *
   * jshell> typify("42f", F, F, T, F) $30 ==> class java.lang.Float=42.0}</pre>
   *
   * ...and they're categorised as {@link String}s otherwise:
   * <pre>{@code
   * jshell> typify("42l", F, F, F, F)
   * $31 ==> class java.lang.String=42l
   *
   * jshell> typify("42F", F, F, F, F)
   * $32 ==> class java.lang.String=42F
   * }</pre>
   *
   * Numbers are boxed into the smallest available type, based on the ranges of
   * each class:
   * <pre>{@code
   * jshell> typify("2", F, F, F, F)
   * $33 ==> class java.lang.Byte=2
   *
   * jshell> typify("200", F, F, F, F)
   * $34 ==> class java.lang.Short=200
   *
   * jshell> typify("2e9", F, F, F, F)
   * $35 ==> class java.lang.Integer=2000000000
   *
   * jshell> typify("5e9", F, F, F, F)
   * $36 ==> class java.lang.Long=5000000000
   *
   * jshell> typify("5e25", F, F, F, F)
   * $37 ==> class java.lang.Float=5.0E25
   *
   * jshell> typify("5e99", F, F, F, F)
   * $38 ==> class java.lang.Double=5.0E99
   *
   * jshell> typify("5e999", F, F, F, F)
   * $39 ==> class java.lang.String=5e999
   * }</pre>
   *
   * <p>...note that, if {@code commonTypes} (the second flag) were enabled, all
   * of the above would be categorised as {@link Double}s, except for the last
   * one, which is outside the range of {@link Double}. Also note that
   * exponential notation ({@code 'e'}/{@code 'E'}) normally isn't allowed in
   * Java for integral types ({@code int} or {@code long}), but it's been added
   * here.</p>
   *
   * <p>Numbers can also contain {@code '_'} and {@code ','} as thousands
   * separators and they will be correctly parsed by this method, but any
   * number containing a decimal point will be parsed as a {@code float} as the
   * narrowest possible type:</p>
   *
   * <pre>{@code
   * jshell> typify("2,000", F, F, F, F)
   * $40 ==> class java.lang.Short=2000
   *
   * jshell> typify("60_000_000_000", F, F, F, F)
   * $41 ==> class java.lang.Long=60000000000
   *
   * jshell> typify("60_000.0", F, F, F, F)
   * $42 ==> class java.lang.Float=60000.0
   * }</pre>
   *
   * <p>If the user hasn't added any date-time formats and attempts to parse a
   * {@link String} with {@code parseDates} enabled, the default formats are
   * added to the list of recognised formats:</p>
   * <pre>{@code
   * jshell> import static scheme.DateTimeFormats.*
   *
   * jshell> strings() $43 ==> []
   *
   * jshell> typify("1976-04-01 21:12:00", F, F, F, T) $44 ==> class
   * java.time.LocalDateTime=1976-04-01 21:12:00
   *
   * jshell> strings() $45 ==> [yyyy-MM-dd HH:mm:ss, yyyy/MM/dd
   * HH:mm:ss.SS, MM/dd/yyyy hh:mm:ss a, M/d/yy H:mm, dd/MM/yyyy
   * HH:mm:ss]}</pre>
   *
   * If {@code parseDates} is disabled, date-formatted {@link String}s will be
   * returned as-is as {@link String}s:
   * <pre>{@code
   * jshell> typify("1976-04-01 21:12:00", F, F, F, F)
   * $46 ==> class java.lang.String=1976-04-01 21:12:00
   * }</pre>
   *
   * @param data raw {@link String} data to interpret
   * @param bool01 if {@code true}, {@code '0'} and {@code '1'} are interpreted
   * as {@code boolean}s
   * @param commonTypes if {@code true}, the returned class will only be one of
   * four "common" classes -- {@link Boolean}, {@link String}, {@link Double},
   * and {@link LocalDateTime}
   * @param postfixFL if {@code true}, explicit {@code float}s and {@code long}s
   * (i.e. {@code "1.1F"} or {@code "13l"}) will be interpreted, otherwise,
   * they're interpreted as {@link String}s
   * @param parseDates if {@code true}, date-time-formatted {@code String}s will
   * be interpreted as {@link LocalDateTime}s, otherwise, they'll be left as
   * {@link String}s
   *
   * @return the inferred class of the information contained in the {@code data}
   * {@link String} and that {@code data} itself (possibly slightly modified) in
   * an {@code Entry<Class, String>}.
   *
   **/
  public static Entry<Class<?>, String> typify (String data, boolean bool01, boolean commonTypes, boolean postfixFL, boolean parseDates) {

    // -2. if the input data has 0 length, return as null object
    if (data == null || data.length() == 0) return new SimpleEntry<>(Object.class, null);

    // -1. if the input data is only whitespace, return "String" and input as-is
    String s = data.trim();  int slen = s.length();
    if (slen == 0) return new SimpleEntry<>(String.class, data);

    // otherwise, strip any surrounding quotes, then strip whitespace again
    char firstChar = s.charAt(0);  char lastChar = s.charAt(slen-1);

    // slen > 1 because "'" should be returned as <Character, "'">
    if (slen > 1 && (firstChar == '"' || firstChar == '\'') && firstChar == lastChar) {
      s = s.substring(1, slen-1).trim(); // remove surrounding quotes
      slen = s.length();
    }

    // now, if s is empty, return original data as String
    if (s.length() == 0) return new SimpleEntry<>(String.class, data);
    lastChar = s.charAt(slen-1); // we'll need this later

    // In most data, numerical values are more common than boolean values. So,
    // if we want to speed up data parsing, we can move this block to the end
    // when looking only for common types. Look for "/// ***", below

    final String[] falseAliases = new String[]{ "false", "False", "FALSE" };
    final String[] trueAliases  = new String[]{ "true",  "True",  "TRUE" };

    /// 0. check if the data is Boolean (true or false)
    if (!commonTypes) {
      if     (CollectionManip.contains(falseAliases, s)) return new SimpleEntry<>(Boolean.class, "false");
      else if (CollectionManip.contains(trueAliases, s)) return new SimpleEntry<>(Boolean.class, "true");
    }

    /**
     * only Strings contain these characters -- skip all numeric processing
     * arranged roughly by frequency in ~130MB of sample DASGIP files:
     *  $ awk -vFS="" '{for(i=1;i<=NF;i++)w[$i]++}END{for(i in w) print i,w[i]}' file.txt
     */

    final char[] StringCharacters = new char[]{
      ' ', ':', 'n', 'a', 't',   'r', 'o', 'C', 'i', 'P',   'D', 's', 'c', 'S', 'u',
      'A', 'm', '=', 'O', '\\',  'd', 'p', 'T', 'M', 'g',   'I', 'b', 'U', 'h', 'H' };

    /**
     * typify() looks for the above characters in an input String before it
     * makes any attempt at parsing that String. If it finds any of the above
     * characters, it immediately skips to the String-processing section,
     * because no numerical type can contain those characters.
     *
     * Adding more characters means that there are more characters to look for
     * in the input String every time a piece of data is parsed, but it also
     * reduces the likelihood that an Exception will be thrown when String data
     * is attempted to be parsed as numerical data (which saves time).
     *
     * The characters below can also be added to the list, but the list above
     * seems to be near-optimal.
     */

    //'J', '+', 'V', 'B', 'G',   'R', 'y', '(', ')', 'v',   '_', ',', '[', ']', '/',
    //'N', 'k', 'w', '}', '{',   'X', '%', '>', 'x', '\'',  'W', '<', 'K', 'Q', 'q',
    //'z', 'Y', 'j', 'Z', '!',   '#', '$', '&', '*', ',',   ';', '?', '@', '^', '`',
    //'|', '~'};

    // check for any String-only characters; if we find them, don't bother trying to parse this as a number
    if (CollectionManip.containsAny(s, CharBuffer.wrap(StringCharacters)) == false) {

      // try again for boolean -- need to make sure it's not parsed as Byte
      if (bool01) {
        if      (s.equals("0")) return new SimpleEntry<>(Boolean.class, "false");
        else if (s.equals("1")) return new SimpleEntry<>(Boolean.class, "true");
      }

      // float and long can have appended annotations
      boolean lastCharF = (lastChar == 'f' || lastChar == 'F');
      boolean lastCharL = (lastChar == 'l' || lastChar == 'L');

      // if the number has a decimal point, immediately move to float
      boolean hasDecPnt = (s.indexOf('.') >= 0);

      // remove commas and _ from number, if there are any
      String sClean = s.replaceAll("[_,]", "");
      boolean gotoString = false;

      // if number doesn't contain a decimal point, but does contain 'e' or 'E'
      // (but not both), try to parse the bit after the 'e' or 'E' as a short
      // and add that many zeroes to the end of the number
      if (!hasDecPnt && (sClean.indexOf('e') >= 0 ^ sClean.indexOf('E') >= 0)) {
        try {
          int index = Math.max(sClean.indexOf('e'), sClean.indexOf('E'));
          sClean = sClean.substring(0, index) + String.join("",
            Collections.nCopies(Short.parseShort(sClean.substring(index+1)), "0"));

        } catch (NumberFormatException ex) {
          gotoString = true;
        }
      }

      // problem parsing exponent, go to String immediately
      if (!gotoString) {

        if (!commonTypes) { // if we're not restricted to common types, look for anything

          if (!hasDecPnt) {

            /// 1. check if data is a Byte (1-byte integer with range [-(2e7) = -128, ((2e7)-1) = 127])
            try {
              Byte b = Byte.parseByte(sClean);
              return new SimpleEntry<>(Byte.class, b.toString()); // if we make it to this line, the data parsed fine as a Byte
            } catch (NumberFormatException ex) {
              // okay, guess it's not a Byte
            }

            /// 2. check if data is a Short (2-byte integer with range [-(2e15) = -32768, ((2e15)-1) = 32767])
            try {
              Short h = Short.parseShort(sClean);
              return new SimpleEntry<>(Short.class, h.toString()); // if we make it to this line, the data parsed fine as a Short
            } catch (NumberFormatException ex) {
              // okay, guess it's not a Short
            }

            /// 3. check if data is an Integer (4-byte integer with range [-(2e31), (2e31)-1])
            try {
              Integer i = Integer.parseInt(sClean);
              return new SimpleEntry<>(Integer.class, i.toString()); // if we make it to this line, the data parsed fine as an Integer
            } catch (NumberFormatException ex) {
              // okay, guess it's not an Integer
            }

            String s_L_trimmed = sClean;

            /// 4. check if data is a Long (8-byte integer with range [-(2e63), (2e63)-1])

            //    ...first, see if the last character of the string is "L" or "l"
            //    ... Java parses "3.3F", etc. fine as a float, but throws an error with "3L", etc.
            if (postfixFL && slen > 1 && lastCharL)
              s_L_trimmed = s.substring(0, slen-1);

            try {
              Long l = Long.parseLong(s_L_trimmed);
              return new SimpleEntry<>(Long.class, l.toString()); // if we make it to this line, the data parsed fine as a Long
            } catch (NumberFormatException ex) {
              // okay, guess it's not a Long
            }

          } // end if(!hasDecPnt) block

          /// 5. check if data is a Float (32-bit IEEE 754 floating point with approximate extents +/- 3.4028235e38)
          if (postfixFL || !lastCharF) {
            try {
              Float f = Float.parseFloat(sClean);

              if (!f.isInfinite()) // if it's beyond the range of Float, maybe it's not beyond the range of Double
                return new SimpleEntry<>(Float.class, f.toString()); // if we make it to this line, the data parsed fine as a Float and is finite

            } catch (NumberFormatException ex) {
              // okay, guess it's not a Float
          } }

        } // end uncommon types 1/2

        /// 6. check if data is a Double (64-bit IEEE 754 floating point with approximate extents +/- 1.797693134862315e308 )
        if (postfixFL || !lastCharF) {
          try {
            Double d = Double.parseDouble(sClean);

            if (!d.isInfinite())
              return new SimpleEntry<>(Double.class, d.toString()); // if we make it to this line, the data parsed fine as a Double
            else // if it's beyond the range of Double, just return a String and let the user decide what to do
              return new SimpleEntry<>(String.class, s);

          } catch (NumberFormatException ex) {
            // okay, guess it's not a Double
          }
        }

      } // problem parsing exponent, go to String immediately
    } // if we have StringCharacters, we must have a String...

    // ...or a Boolean!
    if (commonTypes) { // try again for Boolean                          /// ***
      if     (CollectionManip.contains(falseAliases, s)) return new SimpleEntry<>(Boolean.class, "false");
      else if (CollectionManip.contains(trueAliases, s)) return new SimpleEntry<>(Boolean.class, "true");
    }

    /// 7. revert to String by default, with caveats...

    /// 7a. if string has length 1, it is a single character
    if (!commonTypes && slen == 1)
      return new SimpleEntry<>(Character.class, s); // end uncommon types 2/2

    /// 7b. attempt to parse String as a LocalDateTime
    if (parseDates && stringAsDate(s) != null) return new SimpleEntry<>(LocalDateTime.class, s);

    // ...if we've made it all the way to here without returning, give up and return "String" and input as-is
    return new SimpleEntry<>(String.class, data);

  }

  /**
   * Attempts to transform the given {@code text} into an identifier / variable
   * name which is maximally compatible across languages / systems.
   *
   * <p>Removes all non-alphanumeric, non-underscore characters from the given
   * {@code text}, ensures that the initial character is alphabetic (ASCII),
   * and trims all leading / trailing underscores and whitespace. If the
   * resulting {@link String} has length zero (or the initial {@code text} is
   * {@code null} or has length zero), the string {@code "X"} will be
   * returned.</p>
   *
   * <p>The resulting identifiers are valid in all JVM languages, as well as the
   * R language and SQL.</p>
   *
   * @param text text to transform into an identifier
   * @return the transformed text
   *
   */
  public static String makeValidIdentifier (String text) {

    // if text is null, return smallest possible valid String
    if (text == null || text.length() == 0) return "X";

    // remove all invalid initial characters
    String token = text.trim().replaceAll("^([^a-zA-Z]*)", "");

    // replace any successive whitespace and underscores with a single underscore
    token = token.replaceAll("[ \n\t\b\r_]+", "_");

    // remove all invalid non-initial characters
    token = token.replaceAll("[^a-zA-Z0-9_]*", "");

    // remove any trailing underscores
    token = token.replaceAll("[_]$", "");

    // "_" is an invalid identifier -- return smallest possible string
    if ("".equals(token)) return "X";

    return token;
  }

  /**
   * Attempts to parse the given {@link String} as a {@link LocalDateTime}.
   *
   * <p>Loops over the list of {@link LocalDateTime} formats defined in
   * {@link DateTimeFormats} and attempts to parse the given {@link String} as
   * a date-time string using each format, in order.</p>
   *
   * <p>The first format in the list which can interpret the {@link String} as
   * a date will be used to create and return a {@link LocalDateTime} object,
   * even if there is a more specific format further down the list. Use
   * {@link DateTimeFormats#add(java.util.Locale, String)} and
   * {@link DateTimeFormats#remove(int)} to adjust the list of recognised
   * date-time formats to your needs.</p>
   *
   * @param date {@link String} to be interpreted as a date-time
   * @return {@code date}, parsed as a {@link LocalDateTime}, if possible,
   * otherwise, {@code null}
   *
   */
  public static LocalDateTime stringAsDate (String date) {
    List<DateTimeFormatter> formats = DateTimeFormats.get();

    for (DateTimeFormatter format : formats) {
      try {
        return LocalDateTime.parse(date, format);
      } catch (java.time.format.DateTimeParseException ex) {
          // can't parse it as this format, but maybe the next one...?
    } }
    return null; // if none work, return null
  }

}