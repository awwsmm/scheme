
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static scheme.CSV.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

public class TestCSV {

  // private method to read resource file from this repo
  private String getResourcePath (String name) throws FileNotFoundException, IOException {

    URL url = TestCSV.class.getClassLoader().getResource(name);
    String path = (new File(url.getFile())).getAbsolutePath();

    return path;
  }

  /**
   * test schema()
   */
  @Test
  public void test_schema() throws FileNotFoundException, IOException {

    // path to resource (example CSV) file
    String filename = getResourcePath("example0.csv");

    // get schema
    List<Entry<String, Class<?>>> schema = schema(filename);

    // verify column names of example CSV file
    assertEquals("one",   schema.get(0).getKey());
    assertEquals("two",   schema.get(1).getKey());
    assertEquals("three", schema.get(2).getKey());
    assertEquals("four",  schema.get(3).getKey());

    // verify classes of example CSV file
    assertEquals(    Byte.class, schema.get(0).getValue() );
    assertEquals( Boolean.class, schema.get(1).getValue() );
    assertEquals(  String.class, schema.get(2).getValue() );
    assertEquals( Integer.class, schema.get(3).getValue() );

    // assert that an empty file returns null
    filename = getResourcePath("example1.csv");
    assertNull(schema(filename));

    // parse a file with multiple header rows
    filename = getResourcePath("example2.csv");
    schema = schema(filename, 0, 1, 10, false, false, false, true);
    System.out.println(schema);


  }

}