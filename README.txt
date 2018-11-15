The program can be run using the following steps:

* Create a Eclipse project using the pom.xml

* There is only one java program: InterviewHW.java

* Run program from Eclipse "Run Configurations"

* Example of configuration: -e presto -f csv -db jerry_db -tb ohlcv -h my -l 20 -m 100 -M 1251109200 -c Symbol,Timestamp,Day,Open,High,Low,Close,Volume

* See test cases file for test cases being used in my runs for details
  Handle many combinations of input parameters.  
  - not specifying db name or table name: program will exit
  - other cases whereby user enters bad input for time range or limit for "optional" parameters
    such as not a number (string) or negative value: these are recoverable errors and default values will be used.


*** Note: it seems like connection to presto works on the development environment. However, Hive connection appears not working