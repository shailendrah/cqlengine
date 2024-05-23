package oracle.cep.test.demo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CEPCSKunckle {
  PreparedStatement pstmt;
  
  public CEPCSKunckle() {
    try{
      Class.forName("oracle.cep.jdbc.CEPDriver");
      String hostName = "localhost";
      String url = "jdbc:oracle:cep:@"+ hostName + ":1199";
      
       // connect
        Connection conn = DriverManager.getConnection(url, "system", "oracle");
        Statement stmt = conn.createStatement();

        // c1 - torque, c2-position, c3- id no
        stmt.executeUpdate("register stream S2 (c1 int, c2 int, c3 int, c4 int)");
        stmt.executeUpdate("register relation R (c1 int, c2 int)");
        stmt.executeUpdate("alter stream S2 add source push");
        stmt.executeUpdate("alter relation R add source \"<EndPointReference><Address>file:///C:/cep/work/cep/torque.txt</Address></EndPointReference>\"");
        stmt.executeUpdate("create view v1(c1 int, c2 int, c3 timestamp, c4 int) as select T.actTor, T.actPos, to_timestamp(T.tim), T.id from S2 MATCH_RECOGNIZE ( MEASURES A.c1 as actTor, A.c2 as actPos, A.c3 as id, A.element_time as tim PATTERN(A) DEFINE A as ((A.c1 != PREV(A.c1)) or (A.c2 != PREV(A.c2))) )as T ");
       stmt.executeUpdate("create query q2 as select v1.c4, v1.c2,v1.c3, case when v1.c1 > R.c2 then (v1.c1-1) when v1.c1 < R.c2 then (v1.c1+1) else v1.c1 end from v1[NOW], R where R.c1 = v1.c4 ");
        stmt.executeUpdate("alter query q2 add destination \"<EndPointReference><Address>jms:jms/ConnectionFactory:jms/out2</Address></EndPointReference>\"");
        stmt.executeUpdate("alter query q2 start");
        stmt.executeUpdate("create query q5 as select * from S2");
        stmt.executeUpdate("alter query q5 add destination \"<EndPointReference><Address>file:///C:/cep/work/cep/out5.txt</Address></EndPointReference>\"");
        stmt.executeUpdate("alter query q5 start");

        pstmt = conn.prepareStatement("insert into S2 values (?, ?, ?, ?, ?)");
        
       
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
  }
  
  public void pushData(long ts, int i1, int i2, int i3, int i4)
  {
    System.out.println("pushed 2");
    try {
    pstmt.setLong(1, ts);
      pstmt.setInt(2, i1);
        pstmt.setInt(3, i2);
        pstmt.setInt(4, i3);
        pstmt.setInt(5, i4);
        pstmt.executeUpdate();
    } catch (SQLException e) {
      
      e.printStackTrace();
    }
  }
}
