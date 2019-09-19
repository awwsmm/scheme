import scheme._

import java.io.FileNotFoundException
import java.sql.Timestamp
import java.time.LocalDateTime
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import scala.collection.JavaConversions._

def JavatoSpark[T](dataType: String): DataType = {
  dataType match {
    case "class java.lang.Boolean"       => BooleanType
    case "class java.lang.Byte"          => ByteType
    case "class java.lang.Character"     => StringType
    case "class java.lang.Double"        => DoubleType
    case "class java.lang.Float"         => FloatType
    case "class java.lang.Integer"       => IntegerType
    case "class java.time.LocalDateTime" => TimestampType
    case "class java.lang.Long"          => LongType
    case "class java.lang.Short"         => ShortType
    case "class java.lang.Object"        => StringType
    case "class java.lang.String"        => StringType
    case _                               => null
  }
}

def StringtoSpark(value: String, dataType: DataType): Any = {
  if (value == null) return null
  dataType match {
    case BooleanType   => value.toBoolean
    case ByteType      => value.toByte
    case DoubleType    => value.toDouble
    case FloatType     => value.toFloat
    case IntegerType   => value.toInt
    case LongType      => value.toLong
    case ShortType     => value.toShort
    case TimestampType => Timestamp.valueOf(StringManip.stringAsDate(value))
	case _             => value
  }
}

def CSVtoParquet[_](filename: String): org.apache.spark.sql.Dataset[_] = {

  try {

    // first, infer Java-style CSV schema using scheme package
    val javaSchema = CSV.schema(filename, false, true, false, true)

    // map Java types => Spark types to create Parquet schema
    val sparkSchema = StructType(javaSchema.map(e =>
      StructField(e.getKey, JavatoSpark(e.getValue.toString), true)).toArray)

    // get Spark types as a Seq
    val sparkTypes = javaSchema.map(e => JavatoSpark(e.getValue.toString)).toSeq

  	// get the header extents
    val headerExtents = CSV.headerExtents()

    // create our custom DataFrame by...
    val df = spark.createDataFrame(

      // ...reading the CSV file without a schema (all Strings)
	  spark.read.csv(filename).rdd.

        // ...removing all header rows and metadata above
	    zipWithIndex.filter(_._2 > headerExtents(1)).keys.

        // ...parsing each token as its appropriate type
        map { row => Row.fromSeq(
          row.toSeq.zip(sparkTypes).map {
            case (a, b) => StringtoSpark(a.asInstanceOf[String], b) } )}

	  // ...and applying the schema we determined earlier
	  , sparkSchema);

    // return the dataset
    return df

  } catch {
    case _: FileNotFoundException => println(s"ERROR: File '$filename' not found")
    return null
  }

}
