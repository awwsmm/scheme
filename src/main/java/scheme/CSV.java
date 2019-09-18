package scheme;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.LocalDateTime;

/**
 * Class for working with comma-separated values (CSVs) within <em>scheme</em>.
 */
public class CSV {

  // private default constructor because this is a utility class
  private CSV(){}

  /**
   * Parses a single line of text as CSV and returns the parsed tokens in a
   * {@link List}.
   *
   * <p>If the given {@code line} is null or empty, an empty {@link List} will
   * be returned. Otherwise, a regular expression is used to parse the
   * {@code line} as CSV.</p>
   *
   * @param line line of text to parse as CSV
   * @return a {@link List} of tokens parsed from the given {@code line}
   *
   */
  private static List<String> parseAsCSV (String line) {

    // if line is null or empty, return an empty array
    if (line == null || line.length() == 0) return new ArrayList<String>(0);

    // regex string for parsing lines of CSV
    String regex = "(?:,|\\n|^)[ ]*((?:\"(?:(?:\"\")*(?:[^\"])*)*\")|(?:[^\",\\n]*)|(?:\\n|$))";

    // compile regex pattern and apply it to the given line
    Matcher matcher = Pattern.compile(regex).matcher(line);

    // save all of the matches to this ArrayList
    ArrayList<String> list = new ArrayList<>();
    while (matcher.find()) list.add(matcher.group(1));

    // If the first character of a line is a comma, the regex above will miss
    // the fact that the first entry is null. So we add an empty String to the
    // beginning of the ArrayList.

    if (line.charAt(0) == ',') list.add(0, "");
    return list;
  }

  // get a file as a BufferedInputStream
  private static final BufferedInputStream getBIS (String filename) throws FileNotFoundException {
    return new BufferedInputStream(new FileInputStream(filename));
  }

  //  based on martinus' method to quickly count the number of lines in a file:
  //    https://stackoverflow.com/a/453067/2925434

  private static int nLinesInFile (String filename) throws FileNotFoundException, IOException {

    // get the buffered input stream -- throws FileNotFoundException
    InputStream is = getBIS(filename);

    // read the stream in 4KB chunks
    byte[] bytes = new byte[4096];

    // read using read() -- throws IOException
    int readChars = is.read(bytes);
    if (readChars == -1) return 0;

    // make it easy for the optimizer to tune this loop
    int count = 0;
    while (readChars == 4096) {
      for (int ii = 0; ii < 4096; ) if (bytes[ii++] == '\n') ++count;
      readChars = is.read(bytes);
    }

    // count remaining characters
    while (readChars != -1) {
      for (int ii = 0; ii < readChars; ++ii) if (bytes[ii] == '\n') ++count;
      readChars = is.read(bytes);
    }

    // return the number of lines in the file
    return count == 0 ? 1 : count;

  }

  private static int[] headerExtents = new int[]{-1, -1};

  /**
   * Returns the extents (line / row indices) which define the header region of
   * the most recently-analysed CSV file.
   * 
   * <p>Returns {@code int[]{-1, -1}} if no header region was found. Otherwise,
   * if both array elements are equal, then the header is just a single line.</p>
   * 
   * @return a two-element {@link int} array giving the first (inclusive) and
   * last (exclusive) row index (0-based) of the header region
   */
  public static int[] headerExtents() {

    int[] extents = new int[2];
    System.arraycopy(headerExtents, 0, extents, 0, 2);

    return extents;
  }

  /**
   * Works just like
   * {@link #schema(String, boolean, boolean, boolean, boolean) schema()},
   * but {@code bool01}, {@code commonTypes}, {@code postfixFL}, and
   * {@code parseDates} are set to {@code false}, {@code false}, {@code false}, 
   * and {@code true}, respectively.
   * 
   * @param file the path of the CSV file to parse
   * @return a {@code List<Entry<String, Class<?>>>} describing the schema of
   * this CSV file, where the {@link String} of each entry is the inferred
   * column name and the {@link Class} is the inferred column class
   * @throws FileNotFoundException if {@code file} refers to a file which does
   * not exist
   * @throws IOException if there was a problem reading the {@code file}
   */
  public static List<Entry<String, Class<?>>> schema (String file)
    throws FileNotFoundException, IOException {
    return schema(file, -1, -1, 35, false, false, false, true);
  }

  /**
   * Works just like
   * {@link #schema(String, int, int, int, boolean, boolean, boolean, boolean) schema()},
   * but {@code firstHeaderRowIndex}, {@code lastHeaderRowIndex}, and
   * {@code nTestRows} are set to -1, -1, and 35, respectively.
   * 
   * <p>Setting {@code firstHeaderRowIndex} and {@code lastHeaderRowIndex} to -1
   * indicates that those values will be inferred by
   * {@link #schema(String, int, int, int, boolean, boolean, boolean, boolean) schema()},
   * which performs an analysis of row types to determine the header row
   * extents.</p>
   * 
   * @param file the path of the CSV file to parse
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
   * @return a {@code List<Entry<String, Class<?>>>} describing the schema of
   * this CSV file, where the {@link String} of each entry is the inferred
   * column name and the {@link Class} is the inferred column class
   * @throws FileNotFoundException if {@code file} refers to a file which does
   * not exist
   * @throws IOException if there was a problem reading the {@code file}
   */
  public static List<Entry<String, Class<?>>> schema (String file,
    boolean bool01, boolean commonTypes, boolean postfixFL, boolean parseDates)
    throws FileNotFoundException, IOException {
    return schema(file, -1, -1, 35, bool01, commonTypes, postfixFL, parseDates);
  }

  /**
   * Given a path to a CSV {@code file}, and a list of options, this method
   * attempts to determine the schema of the data contained within the file.
   * 
   * <p>The schema is a {@link List} of {@link Entry}s, where each {@code Entry}
   * contains two values, (1) a {@link String} column name, and (2) a
   * {@link Class} that's been inferred from the type of data contained
   * within that column.</p>
   * 
   * <p>If the user provides an invalid {@code firstHeaderRowIndex} or
   * {@code lastHeaderRowIndex}, the columns will be labeled {@code X1...XN},
   * where {@code N} is the number of columns in the file.</p>
   * 
   * <p>The minimum number of rows required to analyse and make a decent guess
   * at the type of data contained within a column is 7. This, plus a 10-row
   * metadata / header row buffer, is the minimum number of rows to be analysed.
   * If the user provides a value for {@code nTestRows} that is less than 17,
   * it will be increased to 17. Increase this value for a more confident guess
   * at the type of data contained within a column.</p>
   * 
   * @param file the path of the CSV file to parse
   * @param firstHeaderRowIndex a fixed row / line index (0-based) for the
   * beginning of the column header region
   * @param lastHeaderRowIndex a fixed row / line index (0-based) for the end
   * of the column header region
   * @param nTestRows the number of rows to analyse for determining the types of
   * data held within each column
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
   * @return a {@code List<Entry<String, Class<?>>>} describing the schema of
   * this CSV file, where the {@link String} of each entry is the inferred
   * column name and the {@link Class} is the inferred column class
   * @throws FileNotFoundException if {@code file} refers to a file which does
   * not exist
   * @throws IOException if there was a problem reading the {@code file}
   */
  public static List<Entry<String, Class<?>>> schema (String file,
    int firstHeaderRowIndex, int lastHeaderRowIndex, int nTestRows,
    boolean bool01, boolean commonTypes, boolean postfixFL, boolean parseDates)
    throws FileNotFoundException, IOException {

    // get the number of lines in this CSV file
    int nLinesInFile = nLinesInFile(file);

    // if < 1, quit early
    if (nLinesInFile < 1) {
      System.err.println("schema() : no data in file");
      return null;
    }

    //--------------------------------------------------------------------------
    //
    //  Step 1: tokenise and typify first N rows of the file
    //
    //--------------------------------------------------------------------------

    // tokenize some lines and typify the tokens
    List<List<String>>   tokens = new ArrayList<>();
    List<List<Class<?>>> types  = new ArrayList<>();

    // try to read the file
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line = null; int lineIndex = 0;

      // minimum 7 rows required for a decision, plus header buffer
      int nHeadRows = Math.max(7, nTestRows) + 10;

      // loop over lines in file, quit if no more lines to read
      while ((line = reader.readLine()) != null) {

        // increment the line index, break if more than maximum
        if (++lineIndex > nHeadRows) break;

        // parse the line into tokens
        List<String> lineTokens = parseAsCSV(line);

        // ...then, infer the types of those tokens
        List<Class<?>> lineTypes = lineTokens.stream()
          .map(e -> StringManip.typify(e, bool01, commonTypes, postfixFL, parseDates)
          .getKey()).collect(Collectors.toList());

        // add these lines to the collections defined above
        tokens.add(lineTokens);
        types.add(lineTypes);
      }

    } catch (FileNotFoundException ex) {
      System.err.println("schema() : file not found; returning null");
      return null;

    } catch (IOException ex) {
      System.err.println("schema() : I/O error; returning null");
      return null;
    }

    // get maximum column index we've seen
    int nCols = types.stream().mapToInt(e -> e.size()).max().orElse(0);

    if (nCols < 1) {
      System.err.println("schema() : no data found in file");
      return null;
    }

    //--------------------------------------------------------------------------
    //
    //  Step 2: attempt to find the header / data split by analysing types
    //          (if the user has provided an invalid header row range)
    //
    //--------------------------------------------------------------------------

    int firstHeaderRow = firstHeaderRowIndex;
    int  lastHeaderRow =  lastHeaderRowIndex;

    if ( firstHeaderRow > lastHeaderRow
      || firstHeaderRow < 0 || firstHeaderRow >= nLinesInFile
      ||  lastHeaderRow < 0 ||  lastHeaderRow >= nLinesInFile) {

      // header row should comprise all String-type data
      List<Class<?>> dummyHeader = Collections.nCopies(nCols, String.class);

      // get similarity between each row and dummy header row
      List<Double> similarity = IntStream.range(0, types.size())
        .mapToDouble(ii -> CollectionManip.similarity(types.get(ii), dummyHeader))
        .boxed().collect(Collectors.toList());

      // get maximum similarity across test rows
      Double maxSimilarity = similarity.stream().mapToDouble(x -> x).max().getAsDouble();

      // starting from the end of the range, find first row with max similarity
      lastHeaderRow = similarity.lastIndexOf(maxSimilarity);

      // if last header row is last row in range, definitely a problem
      if (lastHeaderRow == (types.size()-1)) {
        System.err.println("schema() : could not infer column names");
        lastHeaderRow = firstHeaderRow = -1;

      // otherwise, we may have found the column headers
      } else {

        // find first row prior to lastHeaderRow that doesn't look like a header row
        List<Double> potentialHeader = similarity.subList(0, lastHeaderRow);
        Collections.reverse(potentialHeader);

        firstHeaderRow = potentialHeader.lastIndexOf(maxSimilarity);
        if (firstHeaderRow < 0) firstHeaderRow = lastHeaderRow;
        else firstHeaderRow = lastHeaderRow - (firstHeaderRow + 1);

      }
    }

    // save header extents to class variable
    headerExtents[0] = firstHeaderRow;
    headerExtents[1] = lastHeaderRow;

    //--------------------------------------------------------------------------
    //
    //  Step 3: parse column headers or generate dummy ones
    //
    //--------------------------------------------------------------------------

    // create column names
    List<String> colNames = new ArrayList<>(nCols);

    // if at least one header row, start constructing column names
    if (firstHeaderRow >= 0) {

      // merged column headers
      List<String> mergedHeaders = new ArrayList<>(tokens.get(firstHeaderRow));

      // if multiple header rows, merge header rows into column names
      if (lastHeaderRow > firstHeaderRow) {

        // loop over header rows
        for (int rr = 1; rr <= (lastHeaderRow - firstHeaderRow); ++rr) {

          // get this header row
          List<String> headerRow = tokens.get(firstHeaderRow+rr);

          // loop over this row's elements
          for (int cc = 0; cc < headerRow.size(); ++cc) {
            if (mergedHeaders.size() < (cc+1)) mergedHeaders.add("");
            mergedHeaders.set(cc, mergedHeaders.get(cc) + "_" + headerRow.get(cc));
          }
        }
      }

      // clean the merged headers
      for (int ii = 0; ii < mergedHeaders.size(); ++ii)
        mergedHeaders.set(ii, StringManip.makeValidIdentifier(mergedHeaders.get(ii)));

      // copy to outer variable
      colNames = mergedHeaders;

    } else { // ...if no header rows, give colNames generic names
      for (int xx = 1; xx <= nCols; ++xx) colNames.add("X" + xx);
    }

    //--------------------------------------------------------------------------
    //
    //  Step 4: infer type of data held in each column
    //
    //--------------------------------------------------------------------------

    // transpose lines of classes into columns of classes
    List<List<Class<?>>> transposedTypes = CollectionManip.transpose(
      types.subList(lastHeaderRow+1, types.size()));

    // compress classes by finding narrowest common type
    List<Class<?>> colClasses = transposedTypes.stream().
      map(l -> CollectionManip.narrowestCommonType(l)).collect(Collectors.toList());

    // zip colNames and colClasses lists into a single list
    List<Entry<String, Class<?>>> schema = new ArrayList<>();

    for (int ii = 0; ii < colClasses.size(); ++ii)
      schema.add(new SimpleEntry<>(colNames.get(ii), colClasses.get(ii)));

    return schema;

  }

}
