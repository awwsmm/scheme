# scheme

A minimal package for intelligently inferring schemata of CSV files.

![JaCoCo Java Code Coverage Score](target/coverage.svg) [![Build Status](https://travis-ci.com/awwsmm/scheme.svg?branch=master)](https://travis-ci.com/awwsmm/scheme) [![Link to Javadoc](https://awwsmm.github.io/scheme/javadoc.svg)](https://awwsmm.github.io/scheme/)

__Self-contained__ -- no external dependencies
__Compatible__ -- runs on any Java version >= 8
__Easy__ -- works immediately with no configuration required

Example usage:

```bash
$ cat example.csv
myint1,datetime,myint2,myfloat,mybool
0,09.09.2015 08:19:59,56,-0.757355714,true
1,09.09.2015 08:20:59,56,,false
,09.09.2015 08:21:59,56,-0.37630229,true
,09.09.2015 08:22:59,56,-0.176843335,true
4,,56,-0.111098334,true
5,09.09.2015 08:24:59,56,0.02202878,false

$ jshell --class-path target/scheme-1.0.jar
```

```java
|  Welcome to JShell -- Version 11.0.2
|  For an introduction type: /help intro

jshell> import static scheme.CSV.schema

jshell> schema("example.csv")
$2 ==> [myint1=class java.lang.Byte, datetime=class java.time.LocalDateTime, myint2=class java.lang.Byte, myfloat=class java.lang.Float, mybool=class java.lang.Boolean]
```

Built to more intelligently infer schemata for creating Parquet files from CSV.
