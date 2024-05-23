package oracle.cep.test.demo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CEPDashBoard {
  PreparedStatement pstmt;
  
  public CEPDashBoard() {
    try{
      Class.forName("oracle.cep.jdbc.CEPDriver");
      String hostName = "localhost";
      String url = "jdbc:oracle:cep:@" + hostName + ":1199";
      
       // connect
        Connection conn = DriverManager.getConnection(url , "system", "oracle");
        Statement stmt = conn.createStatement();

            // c1-length c2-height c3-width c4-weight
        stmt.executeUpdate("register stream S3 (len int, ht int, wid int, wt int, id int)");
        stmt.executeUpdate("alter stream S3 add source push");
        stmt.executeUpdate(" create query q3 as select len, ht, wid, wt, (len*ht*wid) as vol, ((len*ht*wid)/wt) as thickness, to_timestamp(element_time), id from S3 ");
        stmt.executeUpdate("alter query q3 add destination \"<EndPointReference><Address>jms:jms/ConnectionFactory:jms/out3</Address></EndPointReference>\"");
        stmt.executeUpdate("alter query q3 start");
        stmt.executeUpdate("create query q6 as select * from S3");
        stmt.executeUpdate("alter query q6 add destination \"<EndPointReference><Address>file:///C:/cep/work/cep/out6.txt</Address></EndPointReference>\"");
        stmt.executeUpdate("alter query q6 start");

        pstmt = conn.prepareStatement("insert into S3 values (?, ?, ?, ?, ?, ?)");
        
        stmt.executeUpdate("alter system run duration = 0");
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
  }
  
  public void pushData(long ts, int i1, int i2, int i3, int i4, int i5)
  {
   System.out.println("pushed");
    try {
    pstmt.setLong(1, ts);
      pstmt.setInt(2, i1);
        pstmt.setInt(3, i2);
        pstmt.setInt(4, i3);
        pstmt.setInt(5, i4);
        pstmt.setInt(6, i5);
        pstmt.executeUpdate();
    } catch (SQLException e) {
      
      e.printStackTrace();
    }
  }
}
