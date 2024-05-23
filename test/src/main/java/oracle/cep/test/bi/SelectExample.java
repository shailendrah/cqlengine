package oracle.cep.test.bi;
/*
 * This sample shows how to retrieve and list all the names
 * (FIRST_NAME, LAST_NAME) from the EMPLOYEES table
 *
 * Please use jdk1.2 or later version 
 */

// You need to import the java.sql package to use JDBC
import java.sql.*;
import oracle.jdbc.pool.OracleDataSource;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * This is a test data generator for the BI POC
 * <p>
 *
 * The arguments to main are as follows -
 * <ol>
 * <li> Name of the file to hold FACT table data
 * <li> Name of the file to hold FACT2 table data
 * <li> Name of the file to hold FORECAST table data
 * <li> Name of the file to hold MARKET table data
 * <li> Name of the file to hold PERIOD table data
 * <li> Name of the file to hold PRODUCT table data
 * <li> Name of the file to hold UET table data
 * </ol>
 *
 * This test program can be enhanced later on, whereby the underlying tables
 * can be described and the stream data generated accordingly
 */

class KeyTimeMap
{
  int  key;
  long time;

  KeyTimeMap(int  key, long time) 
  {
    this.key  = key;
    this.time = time;
  }
};

public class SelectExample
{
  public static void main (String args [])
       throws Exception
  {
    int j = 0;
    String  outFileNameFact1 = args[j++];
    File        f1   = new File(outFileNameFact1);
    PrintWriter pw   = new PrintWriter(f1);
    ArrayList<KeyTimeMap> arr = new ArrayList<KeyTimeMap>();

    String url = "jdbc:oracle:oci8:@";
    try {
      String url1 = System.getProperty("JDBC_URL");
      if (url1 != null)
        url = url1;
    } catch (Exception e) {
      // If there is any security exception, ignore it
      // and use the default
    }

    // Create a OracleDataSource instance and set properties
    OracleDataSource ods = new OracleDataSource();
    ods.setUser("paint");
    ods.setPassword("paint");
    ods.setURL(url);

    // Connect to the database
    Connection conn = ods.getConnection(); 

    // Create a Statement
    Statement stmt = conn.createStatement ();

    ResultSet rset = stmt.executeQuery ("select PERKEY, PERDESC from period where levelx = 'WEEK'");

    char[] dtArr = new char[10];
    // Iterate through the result and print the employee names
    while (rset.next ())
    {
      int key = rset.getInt(1);
      String dtStr = rset.getString(2).substring(12, 20);
      dtArr[0] = '2';
      dtArr[1] = '0';
      dtArr[2] = dtStr.charAt(6);
      dtArr[3] = dtStr.charAt(7);
      dtArr[4] = '-';
      dtArr[5] = dtStr.charAt(0);
      dtArr[6] = dtStr.charAt(1);
      dtArr[7] = '-';
      dtArr[8] = dtStr.charAt(3);
      dtArr[9] = dtStr.charAt(4);
      String dtStr2 = new String(dtArr);
      Date newDt = Date.valueOf(dtStr2);
      arr.add(new KeyTimeMap(key, newDt.getTime()));
    }

    rset = stmt.executeQuery ("select fact.perkey, fact.prodkey, fact.mktkey, fact.units, fact.dollars from FACT, PERIOD where PERIOD.perkey = FACT.perkey and period.levelx = 'WEEK' order by PERIOD.perkey");

    pw.println("i, i, i, i, i");

    // Iterate through the result and print the employee names
    while (rset.next ())
    {
      // Get the time from the key
      int key = rset.getInt(1);
      long time = 0;
      for (int i = 0; i < arr.size(); i++)
      {
	KeyTimeMap map = arr.get(i);
	if (map.key == key)
	{
	  time = map.time;
	  break;
	}
      }

      assert time != 0;

      int int1 = rset.getInt(1);
      boolean int1null = rset.wasNull();
      int int2 = rset.getInt(2);
      boolean int2null = rset.wasNull();
      int int3 = rset.getInt(3);
      boolean int3null = rset.wasNull();
      int int4 = rset.getInt(4);
      boolean int4null = rset.wasNull();
      int int5 = rset.getInt(5);
      boolean int5null = rset.wasNull();

      pw.println (time + " " + (int1null ? " " : int1) + ", " + (int2null ? " " : int2) + ", " + (int3null ? " " : int3) + ", " + (int4null ? " " : int4) + ", " + (int5null ? " " : int5));
    }

    pw.close();
    // Close the ResultSet
    rset.close();

    /** Format to be used for the timestamps */
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
    sdf.setLenient(false);

    String  outFileNameMarket  = args[j++];
    File        f4   = new File(outFileNameMarket);
    PrintWriter pw4   = new PrintWriter(f4);

    // Select first_name and last_name column from the employees table
    rset = stmt.executeQuery ("select mktkey, mktdesc, seq, levelx, mk_short_id, district, region from market");

    pw4.println("i, c 50, i, c 20, c 20, c 40, c 20, s");

    // Iterate through the result and print the employee names
    while (rset.next ())
    {
      int int1 = rset.getInt(1);
      boolean int1null = rset.wasNull();
      String str2 = rset.getString(2);
      boolean str2null = rset.wasNull();
      int int3 = rset.getInt(3);
      boolean int3null = rset.wasNull();
      String str4 = rset.getString(4);
      boolean str4null = rset.wasNull();
      String str5 = rset.getString(5);
      boolean str5null = rset.wasNull();
      String str6 = rset.getString(6);
      boolean str6null = rset.wasNull();
      String str7 = rset.getString(7);
      boolean str7null = rset.wasNull();

      pw4.println ("1 + " + (int1null ? " " : int1) + ", " + (str2null ? " " : "\"" + str2 + "\"") + ", " + (int3null ? " " : int3) + ", " + (str4null ? " " : "\"" + str4 + "\"") + ", " + (str5null ? " " : "\"" + str5 + "\"") + ", " + (str6null ? " " : "\"" + str6 + "\"") + ", " + (str7null ? " " : "\"" + str7 + "\""));
    }

    pw4.println("h 9999999999999999");

    pw4.close();
    rset.close();


    String  outFileNameProduct  = args[j++];
    File        f6   = new File(outFileNameProduct);
    PrintWriter pw6   = new PrintWriter(f6);

    // Select first_name and last_name column from the employees table
    rset = stmt.executeQuery ("select PRODKEY, PRODDESC, LEVELX, COLOR, FINISH, TYPE_, SIZEX, SEQ, BRAND from product");

    pw6.println("f, c 510, c 510, c 510, c 510, c 510, c 510, c 510, c 510, s");

    // Iterate through the result and print the employee names
    while (rset.next ())
    {
      float float1 = rset.getFloat(1);
      boolean float1null = rset.wasNull();
      String str2 = rset.getString(2);
      boolean str2null = rset.wasNull();
      String str3 = rset.getString(3);
      boolean str3null = rset.wasNull();
      String str4 = rset.getString(4);
      boolean str4null = rset.wasNull();
      String str5 = rset.getString(5);
      boolean str5null = rset.wasNull();
      String str6 = rset.getString(6);
      boolean str6null = rset.wasNull();
      String str7 = rset.getString(7);
      boolean str7null = rset.wasNull();
      String str8 = rset.getString(8);
      boolean str8null = rset.wasNull();
      String str9 = rset.getString(9);
      boolean str9null = rset.wasNull();

      pw6.println ("1 + " + (float1null ? " " : float1) + ", " + (str2null ? " " : "\"" + str2 + "\"") + ", " + (str3null ? " " : "\"" + str3 + "\"") + ", " + (str4null ? " " : "\"" + str4 + "\"") + ", " + (str5null ? " " : "\"" + str5 + "\"") + ", " + (str6null ? " " : "\"" + str6 + "\"") + ", " + (str7null ? " " : "\"" + str7 + "\"") + ", " + (str8null ? " " : "\"" + str8 + "\"") + ", " + (str9null ? " " : "\"" + str9 + "\""));
    }

    pw6.println("h 9999999999999999");

    pw6.close();
    // Close the ResultSet
    rset.close();


    String  outFileNamePeriod  = args[j++];
    File        f5   = new File(outFileNamePeriod);
    PrintWriter pw5   = new PrintWriter(f5);

    // Select first_name and last_name column from the employees table
    rset = stmt.executeQuery ("select PERKEY, PERDESC, MONTH_, YEAR_, LEVELX, PERIOD_N, SEQ, SHORTDESC, CURRENTPERIOD, YAGO, MAGO, WAGO from period");

    pw5.println("i, c 40, t, c 4, c 10, c 10, i, c 50, c 1, i , i , i, s");

    // Iterate through the result and print the employee names
    while (rset.next ())
    {
      int int1 = rset.getInt(1);
      boolean int1null = rset.wasNull();
      String str2 = rset.getString(2);
      boolean str2null = rset.wasNull();
      Date dt = rset.getDate(3);
      boolean dt3null = rset.wasNull();
      String str4 = rset.getString(4);
      boolean str4null = rset.wasNull();
      String str5 = rset.getString(5);
      boolean str5null = rset.wasNull();
      String str6 = rset.getString(6);
      boolean str6null = rset.wasNull();
      int int7 = rset.getInt(7);
      boolean int7null = rset.wasNull();
      String str8 = rset.getString(8);
      boolean str8null = rset.wasNull();
      String str9 = rset.getString(9);
      boolean str9null = rset.wasNull();
      int int10 = rset.getInt(10);
      boolean int10null = rset.wasNull();
      int int11 = rset.getInt(11);
      boolean int11null = rset.wasNull();
      int int12 = rset.getInt(12);
      boolean int12null = rset.wasNull();
     
      pw5.println ("1 + " + (int1null ? " " : int1) + ", " + (str2null ? " " : "\"" + str2 + "\"") + ", " + (dt3null ? " " : "\"" + sdf.format(dt) + "\"") + ", " + (str4null ? " " : "\"" + str4 + "\"") + ", " + (str5null ? " " : "\"" + str5 + "\"") + ", " + (str6null ? " " : "\"" + str6 + "\"") + ", " + (int7null ? " " : int7) + ", " + (str8null ? " " : "\"" + str8 + "\"") + ", " + (str9null ? " " : "\"" + str9 + "\"") + ", " + (int10null ? " " : int10) + ", " + (int11null ? " " : int11) + ", " + (int12null ? " " : int12));
    }

    pw5.println("h 9999999999999999");

    pw5.close();
    // Close the ResultSet
    rset.close();



    // Close the Statement
    stmt.close();

    // Close the connection
    conn.close();   
  }
}
