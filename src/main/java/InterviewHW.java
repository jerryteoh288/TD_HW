import java.sql.*;
import java.util.Properties;
import com.treasuredata.client.*;

import org.apache.commons.cli.*;

public class InterviewHW {

	// Validate if an input parameter which suppose to be an integer is indeed an integer.  
	// In this method, negative number is considered invalid input
	  public static boolean  isInteger (String input1)
	  {
	    try
	    {
	      // the String to int conversion happens here
	      int i = Integer.parseInt(input1);
		  if (i >0) 
			  return true;
		  else 
			  return false;
	    }
	    catch (NumberFormatException nfe)
	    {
	      System.out.println("NumberFormatException: " + nfe.getMessage());
	      return false;
	    }

	  }
	  
	 // Connecting to TD to query  data based on input parameters
	  public static void connectWithApiKey(String[] param)
			  throws SQLException
	  {
		  ResultSet rs;
		  String dbname=param[0];
		  String tablename=param[1];
		  String mintime= param[2];
		  String maxtime= param[3];
		  String engine= param[4];
		  String format= param[5];
		  String limit= param[6];

		  String sel_str=param[7];
		  System.out.println(sel_str);
		  if (sel_str.equals("")) {
			  sel_str="SELECT * ";
		  }
		  else
			  sel_str= "SELECT "+sel_str;

		  //System.out.println("Select string:"+ sel_str);
		  String range_str="";
		  Connection conn;
		  if (mintime.equals("") && !maxtime.equals("")) {
			  int maxt= Integer.parseInt(maxtime);

			  range_str= " WHERE TD_TIME_RANGE(time, NULL ,"+ maxt+") ";
			  System.out.println("Here* min time is null: range_str "+ range_str);
		  }
		  else if (maxtime.equals("") && !mintime.equals("")) {
			  int mint= Integer.parseInt(mintime);
			  range_str=" WHERE TD_TIME_RANGE(time, "+mint+ ", NULL ";
			  System.out.println("Here**: range_str "+ range_str);

		  }
		  else if (!mintime.equals("") && !maxtime.equals("")) {
			  range_str=" WHERE Timestamp BETWEEN "+mintime+ " AND " + maxtime+" ";
			  //System.out.println("Here***: range_str "+ range_str);

		  }
		  else if (mintime.equals("") && maxtime.equals("")) {
			  range_str=" ";
			  System.out.println("Here***: range_str "+ range_str);

		  }
		  //System.out.println(dbname + "  "+ tablename + "  engine" +engine);
		  Properties props = new Properties();
		  props.setProperty("apikey", "6092/c1d9e1c7eae582374c1a9fafde87961218faf95a" );

		  if (engine.equals("hive")) {
			  System.out.println("Using Hive");
			  conn = DriverManager.getConnection(
					  "jdbc:td://api-development.treasuredata.com/"+ dbname+ ";useSSL=true;type=hive",
					  props);
		  }
		  else {
			  conn = DriverManager.getConnection(
					  "jdbc:td://api-development.treasuredata.com/"+ dbname+ ";useSSL=true;type=presto",
					  props

					  );
		  }
		  

		  Statement st = conn.createStatement();

		  try {
			  
			  if (limit=="") {
				  System.out.println(sel_str +" FROM "+ tablename);
				  rs = st.executeQuery(sel_str +" FROM "+ tablename);
			  }
			  else {

				  if (range_str.equals("")) {
					  rs = st.executeQuery(sel_str +" FROM "+ tablename +
							  "  limit " +limit);
				  }

				  else {
					 // System.out.println( sel_str +" FROM "+ tablename +range_str +"  limit " +limit);
					  rs = st.executeQuery(sel_str +" FROM "+ tablename +
							  range_str +"  limit " +limit);

				  }

			  }
			  ResultSetMetaData rsmd = rs.getMetaData();


			  int numCol = rsmd.getColumnCount();
			  //** Print column name as header for data to be printed below
			  for (int k=1; k<= numCol; k++) {
				  System.out.print(String.format(" %s ", rsmd.getColumnName(k)));
			  }

			  while (rs.next()) {
				  // int count = rs.getInt(1);

				  long time = rs.getLong(2);
				  String symbol = rs.getString(1);
				  //** csv format will add a comma after each column
				  if (format.equals("csv")) {
					  for (int k=1; k<= numCol; k++) {
						  System.out.print(String.format(" %s", rs.getString(k)));
						  if (k<numCol)
							  System.out.print(",");

					  }
					  System.out.println();
				  }else {
					  for (int k=1; k<= numCol; k++) {
						  System.out.print(String.format(" %s ", rs.getString(k)));
					  }
					  System.out.println();
				  }
			  }
			  rs.close();
		  }
		  finally {
			  st.close();
			  conn.close();
		  }
	  }
	  
	  /* Main method will parse input parameters: 
	   * a) Invalid optional parameters: default values will be used and program will procees
	   * b) Not entering db name or table name: will exit program with exit code -1
	   * c) Call connectWithApiKey(....) to query data
	   * 	   * 
	   */
	  public static void main(String[] args) {
		  String dbname="";
		  String tablename="";
		  String format="tabular";
		  String min_time="";
		  String max_time="";
		  String engine="presto";
		  String limit="";
		  String col_list="";
		  int mintime=0;
		  int maxtime=0;


		  String [] param =new String[8];
		  CommandLineParser parser = new DefaultParser();
		  Options options = new Options();

		  options.addOption("f", true, "Format");
		  options.addOption("m", true, "Minimum timestamp");
		  options.addOption("M", true, "Maximum timestamp");
		  options.addOption("e", true, "Query Engine");
		  options.addOption("db", true, "Database Name");
		  options.addOption("tb", true, "Table Name");
		  options.addOption("l", true, "Query Limit");
		  options.addOption("h", false, "Shows this Help");
		  options.addOption("c", true, "List of Column");


		  try {
			  CommandLine commandLine = parser.parse(options, args);

			  if ((commandLine.hasOption("db")) && (commandLine.hasOption("tb"))){
				  dbname= commandLine.getOptionValue("db");
				  tablename= commandLine.getOptionValue("tb");
				  System.out.println("Database name:" + dbname +  "\nTable name:  "+ tablename);
			  }
			  else {
				  HelpFormatter formatter = new HelpFormatter();
				  formatter.printHelp("Command Line Parameters", options);
				  System.out.println("db name and/or table name not specified, exiting.. return code -1");
				  return ; // Exit program if database name and/or table name not specified

			  }
			  if (commandLine.hasOption("l")){
				  limit= commandLine.getOptionValue("l");
				  if (!isInteger(limit)) {
					  System.out.println("***Error! limit parameter "+limit+" is not valid, reset to show all data");
					  limit="";
				  }
			  }          
			  if (commandLine.hasOption("f")){
				  format= commandLine.getOptionValue("f");
				  System.out.println("format= "+format);
			  }
			  if (commandLine.hasOption("e")){
				  engine= commandLine.getOptionValue("e");
				  System.out.println("engine= "+engine);
				  if (!(engine.equals("presto")) && !(engine.equals("hive"))) {
					  System.out.println("***Error! " + engine+ " is unsupported engine, presto will be used");
				  }

			  }
			  if (commandLine.hasOption("m")){
				  min_time= commandLine.getOptionValue("m");
				  if (!isInteger(min_time)) {
					  System.out.println("***Error! min_time "+min_time+" is not valid, reset to null");
					  min_time="";
				  }
				  else {
					  mintime= Integer.parseInt(min_time);
					  System.out.println("min time= "+min_time);
				  }

			  }
			  if (commandLine.hasOption("M")){
				  max_time= commandLine.getOptionValue("M");
				  if (!isInteger(max_time)) {
					  System.out.println("***Error! min_time "+max_time+" is not valid, reset to null");
					  max_time="";
				  }
				  else {
					  maxtime= Integer.parseInt(max_time);
					  System.out.println("max time= "+max_time);
				  }

			  }
			  if (commandLine.hasOption("c")){
				  col_list= commandLine.getOptionValue("c");
				  System.out.println("column list= "+col_list);
			  }


			  //System.out.println(commandLine.getOptionValue("db"));
			  //System.out.println(commandLine.getOptionValue("tb"));

			  if (commandLine.hasOption("h")) {
				  HelpFormatter formatter = new HelpFormatter();
				  formatter.printHelp("CommandLineParameters", options);
			  }

		  } catch (ParseException e) {
			  e.printStackTrace();
		  }
		  //System.out.println("***mintime "+min_time+ "maxtime "+ max_time);

		  if (mintime>maxtime) {  // Reset min_time and max_time to empty string ==> will show all data
			  System.out.println("***Error! mintime "+mintime+" >"+ maxtime+" maxtime, resetting to nothing and all Timestamp will be shown");
			  if (!max_time.equals("")) {
				  System.out.println("Here*************");
				  min_time="" ;
				  max_time="" ;	
			  }

		  }
		  param[0]=dbname;
		  param[1]=tablename;
		  param[2]=min_time;
		  param[3]=max_time;
		  param[4]= engine;
		  param[5]=format;
		  param[6]=limit;
		  param[7]=col_list;
		  try {
			  connectWithApiKey(param);
			  System.out.println("Success, return code 0");
			  System.exit(0);
		  } catch (SQLException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  System.out.println("Error, return code -1");
			  System.exit(-1);
		  }


	  }
	

	

}
