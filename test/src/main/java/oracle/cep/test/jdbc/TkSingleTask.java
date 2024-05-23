package oracle.cep.test.jdbc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import oracle.cep.common.Constants;

public class TkSingleTask
{

  /**
   * @param args
   */
  public static void main(String[] args)
  {

    try
    {
      // Load the JDBC-ODBC bridge
      Class.forName("oracle.cep.jdbc.CEPDriver");

      // specify the JDBC data source's URL
      String url = "jdbc:oracle:ceplocal";

      // connect
      Connection con = DriverManager.getConnection(url, "system", "oracle");
      
      //creates a stream, a simple query and launches a callout
      Statement s = con.createStatement();
      s.execute("create stream S (a integer, b integer)");
      s.execute("alter stream S add source push");
      s.execute("create query q as select * from S");
      s.execute("alter query q add destination \"<EndPointReference><Address>" 
          +"file:///tmp/singletask.txt</Address></EndPointReference>\"");
      s.execute("alter query q start");
      s.execute("alter system run duration = 0");
      s.execute("alter system start trusted callout " +
          "\"<EndPointReference>" +
          "<CallOutName>SimpleCallout</CallOutName>" +
          "<CallOutArguments>" +
          "<Argument>S</Argument>" +
          "</CallOutArguments>" +
          "</EndPointReference>\"");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}
