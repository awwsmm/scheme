package scheme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;

/**
 * Utility class for working with date-time formats
 * within <em>scheme</em>.
 */
public class DateTimeFormats {

  // * private default constructor because this is a utility class
  private DateTimeFormats(){}

  // list of Date-Time format objects
  private static List<DateTimeFormatter> formats = new ArrayList<DateTimeFormatter>(10);

  // list of Date-Time format strings, i.e. "yyyy/MM/dd HH:mm:ss"
  private static List<String> formatStrings = new ArrayList<String>(10);

  // try to load the default date-time formats from src/main/resources/
  static {
    try ( // load resource file -- cast to BufferedReader so we have readLine() method
      InputStream is = CSV.class.getClassLoader().
        getResourceAsStream("dateFormats.dat");
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      BufferedReader file = new BufferedReader(isr)) {
      formatAdder(file);

    } catch (UnsupportedEncodingException ex) {
      System.err.println("DateTimeFormats : UnsupportedEncodingException encountered while trying to register default date-time formats");

    } catch (IOException ex) {
      System.err.println("DateTimeFormats : IOException encountered while trying to register default date-time formats");
    }
  }

  // private helper method for and add()
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
            System.err.printf("DateTimeFormats : '%s' is not a valid java.util.Locale%n", locale.toString());
            continue;
          }
        } catch (MissingResourceException ex) {
          System.err.printf("DateTimeFormats : '%s' is not a valid java.util.Locale%n", locale.toString());
          continue;
        }

        // second part is format, which is surrounded by quotes
        String format = line.substring(line.indexOf('"')+1, line.length()-1);

        // add new DateTimeFormatter to formats list
        formatStrings.add(format);
        formats.add(DateTimeFormatter.ofPattern(format, locale));

      } // end while()

    } catch (IOException ex) {
      System.err.println("DateTimeFormats : internal IOException encountered");
      return false;
    }

    return true;
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
  public static List<DateTimeFormatter> get() {
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
  public static List<String> strings() {
    return Collections.unmodifiableList(formatStrings);
  }

  /**
   * Adds the specified format to the list of recognised date-time formats.
   *
   * <p>{@code locale} must be a valid {@link Locale}, e.g. "en_US".</p>
   *
   * <p>{@code format} must be a valid {@link DateTimeFormatter}-styled
   * date-time format, e.g. "MM/dd/yyyy HH:mm".</p>
   *
   * @param locale the {@link Locale} of this date-time format
   * @param format a {@link String} representation of this date-time format
   * @return {@code true} if format was successfully added to list of
   * recognised formats, {@code false} otherwise
   */
  public static boolean add (Locale locale, String format) {

    // if String is null or empty, abort
    if (format == null || format.equals("")) {
      System.err.println("add() : format String cannot be null or empty");
      return false;
    }

    // if Locale is null, abort
    if (locale == null) {
      System.err.println("add() : locale cannot be null");
      return false;
    }

    // add new DateTimeFormatter to formats list
    formatStrings.add(format);
    formats.add(DateTimeFormatter.ofPattern(format, locale));
    return true;
  }

  /**
   * Removes the specified format from the list of recognized date-time formats,
   * if it exists.
   *
   * <p>Formats are removed according to their indices. Note that the first
   * format is at index {@code 0}, which means that, for a list of {@code N}
   * formats, the last format has index {@code N-1}.</p>
   *
   * <p>The formats returned from {@link #get()} and {@link #strings()} are
   * returned in order, by their indices.</p>
   *
   * @param index index of the date-time format to remove
   *
   * @return the removed format as a {@link DateTimeFormatter} object, wrapped
   * in an {@link Optional}; returns an empty {@link Optional} if the index is
   * out of range.
   *
   * @see #strings() strings() returns an ordered list of the
   * currently-recognized date-time formats
   */
  public static Optional<DateTimeFormatter> remove (int index) {
    if (index < 0 || index >= formats.size()) { return Optional.empty(); }
    else {
      Optional<DateTimeFormatter> retval = Optional.of(formats.get(index));
      formats.remove(index);
      formatStrings.remove(index);
      return retval;
    }
  }

}