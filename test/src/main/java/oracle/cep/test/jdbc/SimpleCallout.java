package oracle.cep.test.jdbc;

import java.io.FileWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Random;

import oracle.cep.common.Constants;

/**
* Simple callout class, creates random tuples and inserts into <streamName>
* ASSUMPTION: Stream has schema (int,int). 
* Can be modified in future to read from different sources. Checking this in
* as an example. 
*/

public class SimpleCallout implements Runnable
{
  String streamName;
 
  public SimpleCallout(String[] args)
  {
    super();
    this.streamName = args[0];
  }

  
   
  public void run()
  {
    try
    {
      //Load the JDBC-ODBC bridge
      Class.forName("oracle.cep.jdbc.CEPDriver");

      // specify the JDBC data source's URL
      String url = Constants.CEP_LOCAL_URL;

      // connect
      java.sql.Connection con = DriverManager.getConnection(url, "system", "oracle");
      PreparedStatement ps = con.prepareStatement("insert into " +streamName +" values (?,?,?)");
      
      for(int j=0;j<100;j++) {
              
        
        for(int i=0;i<3;i++) {
          ps.setInt(i+1, 100*i+j*10+1000);
        }
        ps.executeUpdate();
      }
      
    } catch (Exception e)
    {
      System.out.println("JMSRecver: Constructor: catch " + e);
      e.printStackTrace();
    }
  }
}
