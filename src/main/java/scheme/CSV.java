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

  public static List<Entry<String, Class<?>>> schema (String file)
    throws FileNotFoundException, IOException {
    return schema(file, -1, -1, 35, false, false, false, true);
  }

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
