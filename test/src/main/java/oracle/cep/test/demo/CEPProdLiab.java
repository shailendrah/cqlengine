package oracle.cep.test.demo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CEPProdLiab {
  PreparedStatement pstmt;
  
  public CEPProdLiab() {
    try{
      Class.forName("oracle.cep.jdbc.CEPDriver");
      String hostName = "localhost";
      String url = "jdbc:oracle:cep:@" + hostName + ":1199";
      
       // connect
        Connection conn = DriverManager.getConnection(url, "system", "oracle");
        Statement stmt = conn.createStatement();

        // c1- length c2-height c3-weight
        stmt.executeUpdate("register stream S1 (c1 int, c2 int, c3 int)");
        stmt.executeUpdate("alter stream S1 add source push");
        stmt.executeUpdate("create query q1 as select T.len, T.ht, T.wt from S1 MATCH_RECOGNIZE( MEASURES A.c1 as len, A.c2 as ht, A.c3 as wt PATTERN (A) DEFINE A as A.c1 != PREV(A.c1,1,100) or A.c2 != PREV(A.c2, 1, 100) or A.c3 != PREV(A.c3, 1, 100) ) as T");
       stmt.executeUpdate("alter query q1 add destination \"<EndPointReference><Address>jms:jms/ConnectionFactory:jms/out1</Address></EndPointReference>\"");
       // stmt.executeUpdate("alter query q1 add destination \"<EndPointReference><Address>file:///C:/cep/work/cep/out1.txt</Address></EndPointReference>\"");
        stmt.executeUpdate("alter query q1 start");
        stmt.executeUpdate("create query q4 as select * from S1");
        stmt.executeUpdate("alter query q4 add destination \"<EndPointReference><Address>file:///C:/cep/work/cep/out4.txt</Address></EndPointReference>\"");
        stmt.executeUpdate("alter query q4 start");
        

        pstmt = conn.prepareStatement("insert into S1 values (?, ?, ?, ?)");
        
       
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
  }
  
  
  public void pushData(long ts, int i1, int i2, int i3)
  {
    System.out.println("pushed 1");
    try {
    pstmt.setLong(1, ts);
      pstmt.setInt(2, i1);
        pstmt.setInt(3, i2);
        pstmt.setInt(4, i3);
        pstmt.executeUpdate();
    } catch (SQLException e) {
      
      e.printStackTrace();
    }
  }
}
