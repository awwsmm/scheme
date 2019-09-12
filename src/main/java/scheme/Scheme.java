package scheme;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Optional;

public class Scheme {

  // * private default constructor because this is a utility class
  private Scheme(){}

  // list of Date-Time format objects
  private static List<DateTimeFormatter> formats = new ArrayList<DateTimeFormatter>(10);

  // list of Date-Time format strings, i.e. "yyyy/MM/dd HH:mm:ss"
  private static List<String> formatStrings = new ArrayList<String>(10);

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
   * <p>Note that both {@link String} and {@link StringBuilder} implement the
   * {@link CharSequence} interface.</p>
   *
   * @param target {@link CharSequence} in which to search for the characters
   * which comprise the {@code source} {@link CharSequence}
   * @param source {@link CharSequence} whose characters should be searched for
   * within the {@code target} {@link CharSequence}
   *
   * @return {@code true} if neither the {@code target} nor the {@code source}
   * is {@code null} nor empty and if the characters contained within
   * {@code target} are a superset of those contained within {@code source}
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
   * Provides the same functionality as
   * {@link #containsAny(CharSequence, CharSequence)}, but the second argument
   * here is a {@code char[]} instead of a {@link CharSequence}.
   *
   * @param target {@link CharSequence} in which to search for the characters
   * which comprise the {@code source} {@code char[]}
   * @param source {@code char[]}} whose characters should be searched for
   * within the {@code target} {@link CharSequence}
   *
   * @return {@code true} if neither the {@code target} nor the {@code source}
   * is {@code null} nor empty and if the characters contained within
   * {@code target} are a superset of those contained within {@code source}
   *
   * @see #containsAny(CharSequence, CharSequence) containsAny(CharSequence,
   * CharSequence)
   */
  public static boolean containsAny (CharSequence target, char[] source) {
    if (source == null) return false; // fail fast -- can't wrap(null)
    return containsAny(target, CharBuffer.wrap(source));
  }

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
   * date-time-formatted {@link String}s is handled by {@link #addFormats() addFormats()} and
   * {@link #addFormats(String) addFormats(String)}. To check
   * which date-time formats are currently being recognized, use
   * {@link #getFormatStrings() getFormatStrings()}. If the given
   * {@code data} {@link String} is not recognized as any other type, then when
   * it's parsed as a {@link LocalDateTime}, an attempt will be made to match it
   * against every recognised date-time format in the order that they are
   * stored, until one succeeds. For this reason, you should only add date-time
   * formats which you expect to encounter while parsing your file(s) of
   * interest.</p>
   *
   * <p>For <em>thorough</em> categorization of {@code data} {@link String}s,
   * {@code commonTypes} should be set to {@code false} and {@code parseDates}
   * should be set to {@code true}. For <em>fast</em> categorization of
   * {@code data} {@link String}s, {@code commonTypes} should be set to
   * {@code true} and {@code parseDates} should be set to {@code false}. By
   * default (when {@link #typify(String)}, with no flags, is called),
   * {@code bool01} is set to {@code false}, {@code commonTypes} is set to
   * {@code true}, {@code postfixFL} is set to {@code false}, and
   * {@code parseDates} is set to {@code true}.</p>
   *
   * <h2><strong>Usage:</strong></h2>
   * First, define some aliases for brevity's sake:
   *
   * <pre>{@code
   * jshell> import static com.nibrt.parser.PStringUtils.typify
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
   * jshell> typify("42f", F, F, T, F)
   * $30 ==> class java.lang.Float=42.0
   * }</pre>
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
   * jshell> import static com.nibrt.parser.PDateTimeUtils.*
   *
   * jshell> getFormatStrings()
   * $43 ==> []
   *
   * jshell> typify("1976-04-01 21:12:00", F, F, F, T)
   * $44 ==> class java.time.LocalDateTime=1976-04-01 21:12:00
   *
   * jshell> getFormatStrings()
   * $45 ==> [yyyy-MM-dd HH:mm:ss, yyyy/MM/dd HH:mm:ss.SS, MM/dd/yyyy hh:mm:ss a, M/d/yy H:mm, dd/MM/yyyy HH:mm:ss]
   * }</pre>
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

    final String[] falseAliases = new String[]{ "false", "False", "FALSE" };
    final String[] trueAliases  = new String[]{ "true",  "True",  "TRUE" };

    // -2. if the input data has 0 length, return as null object
    if (data == null || data.length() == 0) return new SimpleEntry<>(Object.class, null);

    String s = data.trim();
    int slen = s.length();

    // -1. if the input data is only whitespace, return "String" and input as-is
    if (slen == 0) return new SimpleEntry<>(String.class, data);

    // otherwise, strip any surrounding quotes, then strip whitespace again
    char firstChar = s.charAt(0);
    char lastChar = s.charAt(slen-1);

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

    /// 0. check if the data is Boolean (true or false)
    if (!commonTypes) {
      if     (contains(falseAliases, s)) return new SimpleEntry<>(Boolean.class, "false");
      else if (contains(trueAliases, s)) return new SimpleEntry<>(Boolean.class, "true");
    }

    // check for any String-only characters; if we find them, don't bother trying to parse this as a number
    if (containsAny(s, StringCharacters) == false) {

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
      if     (contains(falseAliases, s)) return new SimpleEntry<>(Boolean.class, "false");
      else if (contains(trueAliases, s)) return new SimpleEntry<>(Boolean.class, "true");
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

  //----- helper function which attempts to parse a String as a date -----------

  public static LocalDateTime stringAsDate (String date) {
    List<DateTimeFormatter> formats = getFormats();

    // load default formats if user hasn't specified any
    if (formats.size() == 0) {
      addFormats();
      formats = getFormats();
    }

    for (DateTimeFormatter format : formats) {
      try {
        return LocalDateTime.parse(date, format);
      } catch (java.time.format.DateTimeParseException ex) {
          // can't parse it as this format, but maybe the next one...?
    } }
    return null; // if none work, return null
  }

  /**
   * Attempts to parse a given {@link String} of {@code data} and infer the type
   * of information it represents.
   *
   * <p>Works exactly like
   * {@link #typify(String, boolean, boolean, boolean, boolean)}, but sets the
   * flags to {@code false}, {@code true}, {@code false}, and {@code true},
   * respectively.</p>
   *
   * @param data raw {@link String} of data to interpret
   *
   * @return the inferred class of the information contained in the {@code data}
   * {@link String} and that {@code data} itself (possibly slightly modified) in
   * an {@code Entry<Class, String>}.
   *
   * @see #typify(String, boolean, boolean, boolean, boolean) typify(String,
   * boolean, boolean, boolean, boolean) for more information
   **/
  public static Entry<Class<?>, String> typify (String data) {
    return typify(data, false, true, false, true);
  }

  /**
   * Extracts the data from an {@code Entry<Class, String>} returned from
   * {@link #typify(String, boolean, boolean, boolean, boolean) typify()} and
   * returns the result as an object of the correct class.
   *
   * <p>This method returns {@code null} when it's passed an
   * {@code Entry<Class, String>} whose {@link String} component is
   * {@code null} (see
   * {@link #typify(String, boolean, boolean, boolean, boolean) typify()}).</p>
   *
   * <h2><strong>Usage:</strong></h2>
   * <pre>{@code
   * jshell> import java.time.LocalDateTime
   * jshell> import java.util.Map.Entry
   * jshell> import static com.nibrt.parser.PStringUtils.*
   * }</pre>
   *
   * <p>This method takes an {@code Entry<Class, String>} returned from
   * {@link #typify(String, boolean, boolean, boolean, boolean) typify()} and
   * casts the {@code String} component of the {@code Entry} to the class
   * represented by the {@code Class} component of the {@code Entry}. The
   * value returned by this method can be safely assigned to an object of that
   * {@code Class}:</p>
   *
   * <pre>{@code
   * jshell> Entry<Class, String> entry = typify("1976-04-01 21:12:00")
   * entry ==> class java.time.LocalDateTime=1976-04-01 21:12:00
   *
   * jshell> LocalDateTime ldt = decodeTypify(entry)
   * ldt ==> 1976-04-01T21:12
   * }</pre>
   *
   * <p>With Java 10+, local variable type inference can be used, as well,
   * making the assignment even easier:</p>
   *
   * <pre>{@code
   * jshell> var ldt = decodeTypify(entry) // returns a LocalDateTime object
   * ldt ==> 1976-04-01T21:12
   *
   * jshell> var d = decodeTypify(typify(" 3 ")) // returns a Double
   * d ==> 3.0
   *
   * jshell> var s = decodeTypify(typify(" hello ")) // returns a String
   * s ==> " hello "
   * }</pre>
   *
   * <p>This, of course, removes the need to explicitly declare the class of the
   * returned object, so no warnings will be thrown if the type is not what you
   * expected. This is not the case with explicit variable types:</p>
   *
   * <pre>{@code
   * jshell> Double q = decodeTypify(typify("hello"))
   * |  Exception java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Double (java.lang.String and java.lang.Double are in module java.base of loader 'bootstrap')
   * |        at (#11:1)
   * }</pre>
   *
   * <p>If the class contained within {@code entry.getKey()} is unknown to
   * {@link #typify(String, boolean, boolean, boolean, boolean) typify()}, this
   * method returns {@code null}.</p>
   *
   * @param <T> type of object returned by this method
   * @param entry object returned from
   * {@link #typify(String, boolean, boolean, boolean, boolean) typify()}
   *
   * @return ({@link Entry#getValue() entry.getValue()}) as an object of
   * class ({@link Entry#getKey() entry.getKey()})
   *
   * @see #typify(String, boolean, boolean, boolean, boolean) typify() for
   * more information
   **/
  @SuppressWarnings("unchecked")
  public static <T> T decodeTypify (Entry<Class<?>, String> entry) {

    // null check
    if (entry.getValue() == null) return null;

    // String
    if (entry.getKey() == String.class)
      return (T) entry.getValue();

    // Boolean
    else if (entry.getKey() == Boolean.class)
      return (T) (Boolean) Boolean.parseBoolean(entry.getValue());

    // Byte
    else if (entry.getKey() == Byte.class)
      return (T) (Byte) Byte.parseByte(entry.getValue());

    // Character
    else if (entry.getKey() == Character.class)
      return (T) (Character) entry.getValue().charAt(0);

    // Short
    else if (entry.getKey() == Short.class)
      return (T) (Short) Short.parseShort(entry.getValue());

    // Integer
    else if (entry.getKey() == Integer.class)
      return (T) (Integer) Integer.parseInt(entry.getValue());

    // Long
    else if (entry.getKey() == Long.class)
      return (T) (Long) Long.parseLong(entry.getValue());

    // Float
    else if (entry.getKey() == Float.class)
      return (T) (Float) Float.parseFloat(entry.getValue());

    // Double
    else if (entry.getKey() == Double.class)
      return (T) (Double) Double.parseDouble(entry.getValue());

    // LocalDateTime
    else if (entry.getKey() == LocalDateTime.class)
      return (T) stringAsDate(entry.getValue());

    else return null;
  }

  // get narrowest common type (NCT) from collection of types
  //   ie. Float && Double => Double
  //       Integer && Long => Long
  //       Boolean && Float => String
  //       Character && Byte => String
  //       Short && Float => Float

    // Object        => <column majority>
    // Boolean       => BIT
    // Byte          => TINYINT
    // Short         => SMALLINT
    // Integer       => INTEGER
    // Long          => BIGINT
    // Float         => REAL
    // Double        => DOUBLE
    // LocalDateTime => TIMESTAMP
    // Character     => CHAR(1)
    // String        => VARCHAR (trailing spaces removed)

  public static Class<?> narrowestCommonType (Collection<Class<?>> types)
    throws IllegalStateException {

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
      hasByte || hasDouble || hasFloat || hasInteger || hasLong || hasShort;
    boolean hasNonNumeric =
      hasBoolean || hasCharacter || hasLocalDateTime || hasString;

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
    else throw new IllegalStateException("cannot determine type");

  }

  /**
   * Returns all known date-time formats as an
   * {@link Collections#unmodifiableList unmodifiableList} of
   * {@link DateTimeFormatter}s.
   * @return all known date-time formats as an
   * {@link Collections#unmodifiableList unmodifiableList} of
   * {@link DateTimeFormatter}s.
   *
   */
  public static List<DateTimeFormatter> getFormats() {
    return Collections.unmodifiableList(formats);
  }

  /**
   * Returns all known date-time formats as an
   * {@link Collections#unmodifiableList unmodifiableList} of
   * {@link String}s.
   * @return all known date-time formats as an
   * {@link Collections#unmodifiableList unmodifiableList} of
   * {@link String}s.
   *
   */
  public static List<String> getFormatStrings() {
    return Collections.unmodifiableList(formatStrings);
  }

  /**
   * Adds the list of default date-time formats to the list of recognized
   * date-time formats.
   *
   * <p>Note that before this method is called, no {@link String} will be
   * recognized as a properly-formatted date-time (unless
   * {@link #addFormats(String) addFormats(String)} has been called with a valid
   * file name).</p>
   *
   * @return {@code true} if there were no errors
   *
   * @see #getFormatStrings()
   */
  // * reads from src/main/resources/dateFormats.dat
  public static boolean addFormats() {

    try ( // load resource file -- cast to BufferedReader so we have readLine() method
      InputStream is = Scheme.class.getClassLoader().
        getResourceAsStream("dateFormats.dat");
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      BufferedReader file = new BufferedReader(isr)) {
      return formatAdder(file);

    } catch (UnsupportedEncodingException ex) {
      System.err.println("addFormats() : UnsupportedEncodingException");
      return false;

    } catch (IOException ex) {
      System.err.println("addFormats() : IOException");
      return false;
    }
  }

  /**
   * Attempts to add the list of date-time formats in the specified file to the
   * list of recognized date-time formats.
   *
   * <p>Date-time formats should be in the form:</p>
   *
   * <pre>{@code
   * LOCALE "date format string"
   * }</pre>
   *
   * <p>For example:</p>
   *
   * <pre>{@code
   * en_IE "dd/MM/yyyy HH:mm:ss"
   * }</pre>
   *
   * <p>Locale strings should be formatted according to Java's {@link Locale}
   * specifications. Common locales include "en_IE", "en_US", "fr_FR", etc. In
   * a date-time format file, there should be only one format per line, and
   * there <strong>must</strong> be a locale and a formatting string,
   * surrounded by quotes. Date-time formatting strings should be constructed
   * following the guidelines in Java's {@link DateTimeFormatter} class.</p>
   *
   * <p>This method will not return {@code false} (i.e. fail) if any locales or
   * formats are invalid; rather, it will simply print a warning to the user
   * and skip that entry, or add the invalid format to the list of recognized
   * formats, respectively. It is the user's responsibility to ensure that all
   * locales and formatting strings used are valid. Unwanted formats can be
   * removed from the list of recognized formats with
   * {@link #removeFormat(int) removeFormat()}.</p>
   *
   * <p>This method returns {@code false} if there has been an error and
   * {@code true} otherwise. This means that it can return {@code true} even if
   * no new formats have been added to the list of recognized date-time
   * formats. Make sure to check that your desired format is being recognized
   * by calling {@link #getFormatStrings()} and printing the result.</p>
   *
   * @param fileName name of a file which contains a list of date-time formats
   *
   * @return {@code false} if the file cannot be found or if there's some other
   * {@link Exception}
   */
  public static boolean addFormats (String fileName) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
      new FileInputStream(fileName)))) {
      return formatAdder(reader);

    } catch (FileNotFoundException ex) {
      System.err.println("addFormats(String) : FileNotFoundException");
      return false;

    } catch (IOException ex) {
      System.err.println("addFormats(String) : IOException");
      return false;
    }
  }

  /**
   * * private helper method for addFormats(String) and addFormats()
   */
  private static boolean formatAdder (BufferedReader file) {

    // no null files allowed
    if (file == null) return false;

    try { // given a BufferedReader, try to read the file:

      String line = null; // line read from file
      while ((line = file.readLine()) != null) {

        // first part of *.dat file is locale, which contains no spaces
        String   localeString = line.substring(0, line.indexOf(' '));
        String[] localeParts = localeString.split("_");
        Locale   locale = null;

        if (localeParts.length > 2)
          locale = new Locale(localeParts[0], localeParts[1], localeParts[2]);

        else if (localeParts.length > 1)
          locale = new Locale(localeParts[0], localeParts[1]);

        else
          locale = new Locale(localeString);

        try { // validate the locale
          if (locale.getISO3Language() == null || locale.getISO3Country() == null){
            System.err.printf("'%s' is not a valid java.util.Locale%n", locale.toString());
            continue;
          }
        } catch (MissingResourceException ex) {
          System.err.printf("'%s' is not a valid java.util.Locale%n", locale.toString());
          continue;
        }

        // second part is format, which is surrounded by quotes
        String format = line.substring(line.indexOf('"')+1, line.length()-1);

        // add new DateTimeFormatter to formats list
        formatStrings.add(format);
        formats.add(DateTimeFormatter.ofPattern(format, locale));

      } // end while()

    } catch (IOException ex) {
      System.err.println("formatAdder() : IOException");
      return false;
    }

    return true;
  }

  /**
   * Removes the specified format from the list of recognized date-time formats,
   * if it exists.
   *
   * <p>Formats are removed according to their indices. Note that the first
   * format is at index 0, which means that, for a list of N formats, the last
   * format has index N-1.</p>
   *
   * @param index index of the date-time format to remove
   *
   * @return the removed format as a {@link DateTimeFormatter} object, wrapped
   * in an {@link Optional}; returns an empty {@link Optional} if the index is
   * out of range.
   *
   * @see #getFormatStrings() getFormatStrings() returns an ordered list of the
   * currently-recognized date-time formats
   */
  public static Optional<DateTimeFormatter> removeFormat (int index) {
    if (index < 0 || index >= formats.size()) { return Optional.empty(); }
    else {
      Optional<DateTimeFormatter> retval = Optional.of(formats.get(index));
      formats.remove(index);
      formatStrings.remove(index);
      return retval;
    }
  }

}
