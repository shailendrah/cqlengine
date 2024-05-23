package oracle.cep.test.ha;

public class HATestPattern extends BaseCQLTestCase
{
  public HATestPattern() throws Exception
  {
    super();
  }

 
  //Test Corresponding to tkpattern_q2 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern1() throws Exception
  {  
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 float)",      
      "create query q1 as select T.Ac1,T.Bc1,T.Dc1 from S1 "
      + "MATCH_RECOGNIZE ( MEASURES A.c1 as Ac1, B.c1 as Bc1, D.c1 as Dc1 "
      + "PATTERN(A B C* D) "
      + "DEFINE A as A.c1 = 30, B as B.c2 = 10.0, C as C.c1 = 7, D as D.c1 = 40) as T ",
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern1_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern1_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //TODO: Not working even if all data is in first phase; Need to check if the test data and pattern
  // query is correct or not.
  /**  
  //Test Corresponding to tkpattern_q2 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx 
  public void testPattern2() throws Exception
  {  
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",      
      "create query q1 as select T.Ac1,T.Cc1 from S1 MATCH_RECOGNIZE ( MEASURES A.c1 as Ac1, C.c1 as Cc1 PATTERN(A B+ C) DEFINE A as A.c1 = prev(A.c1), B as B.c1 = 10, C as C.c1 = 7) as T "
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern2_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern2_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q2 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
   
  public void testPattern3() throws Exception
  {  
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 integer)",
      "create query q1 as select T.firstW,T.lastZ from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as firstW, last(Z.c1) as lastZ"
      + " PATTERN(A W+ X+ Y+ Z+)"
      + " DEFINE A as A.c2 = A.c2, W as W.c2 < prev(W.c2), X as X.c2 > prev(X.c2), Y as Y.c2 < prev(Y.c2), Z as Z.c2 > prev(Z.c2)"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern3_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern3_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }*/
   
  //Test Corresponding to tkpattern_q4 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx 
  public void testPattern4() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select T.Ac1,T.Cc1 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as Ac1, C.c1 as Cc1"
      + " PATTERN(A B* C)"
      + " DEFINE A as A.c1 > 20, B as B.c1 < 16, C as C.c1 < 14"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern4_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern4_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q5 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx 
  public void testPattern5() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select T.Ac1,T.Bc1 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as Ac1, B.c1 as Bc1"
      + " PATTERN(A B C*)"
      + " DEFINE A as A.c1 > 20, B as B.c1 = 0, C as C.c1 < 15"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern5_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern5_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q6 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx 
  public void testPattern6() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select T.Ac1,T.Cc1 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as Ac1, C.c1 as Cc1"
      + " PATTERN(A B? C)"
      + " DEFINE A as A.c1 > 20, B as B.c1 = 0, C as C.c1 < 15"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern6_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern6_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q7 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx 
  public void testPattern7() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select T.Ac1,T.Bc1 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as Ac1, B.c1 as Bc1"
      + " PATTERN(A B C* D*)"
      + " DEFINE A as A.c1 > 20, B as B.c1 = 0, C as C.c1 < 15, D as D.c1 < 10"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern7_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern7_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q10,tkpattern_q12 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx 
  public void testPattern8() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 float)",
      "create query q1 as select T.Ac1, T.sumB, T.firstB, T.lastB, T.avgB, T.Cc1 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as Ac1, sum(B.c1) as sumB, first(B.c1) as firstB, last(B.c1) as lastB, avg(B.c1) as avgB, C.c1 as Cc1"
      + " PATTERN(A B* C)"
      + " DEFINE A as A.c1 = 10, B as B.c1 > A.c1 , C as C.c1 > B.c1"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern8_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern8_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q13 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  // TODO: No Output For First Phase Too; Validate whether query inside CQLX test works or not
  /*
  public void testPattern9() throws Exception  
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 integer)",
      "create query q1 as select T.firstW,T.lastZ from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES first(W.c1) as firstW, last(Z.c1) as lastZ"
      + " PATTERN(W+ X+ Y+ Z+)"
      + " DEFINE W as W.c2 < prev(W.c2), X as X.c2 > prev(X.c2), Y as Y.c2 < prev(Y.c2), Z as Z.c2 > prev(Z.c2)"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern9_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern9_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }*/
  
  //Test Corresponding to tkpattern_q14 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern10() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 integer, c3 integer)",
      "create query q1 as select T.Atime60, T.id from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1+60 as Atime60, A.c3 as id"
      + " PATTERN(A B* C)"
      + " DEFINE A as A.c2 = 10, B as ((B.c2 != 20) AND (B.c1 <= A.c1 + 60)), C as C.c1 > (A.c1 + 60)"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern10_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern10_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q15 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  //TODO: No Output For First Phase Too; Validate whether query inside CQLX test works or not
  /*
  public void testPattern11() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select T.sumA from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES sum(A.c1) as sumA "
      + " PATTERN(A*)"
      + " DEFINE A as A.c1 > prev(A.c1)"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern11_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern11_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }*/
  
  //Test Corresponding to tkpattern_q43 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern12() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 float)",
      "create query q1 as select  T.Ac1, T.countB, T.sumC from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as Ac1, count(B.c1) as countB, sum(C.c1) as sumC  "
      + " PATTERN(A B+? C*)"
      + " DEFINE A as A.c1 = 10, B as B.c1 > A.c1 , C as C.c1 > 40"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern12_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern12_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q46 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern13() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 float)",
      "create query q1 as select  T.Ac1,T.Bc1,T.Dc1 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as Ac1, B.c1 as Bc1, D.c1 as Dc1"
      + " PATTERN((A B) (C* D))"
      + " DEFINE A as A.c1 = 30, B as B.c2 = 10.0, C as C.c1 = 7, D as D.c1 = 40"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern13_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern13_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q51 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern14() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select  T.sumA, T.Bc1, T.Cc1 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES sum(A.c1) as sumA, B.c1 as Bc1, C.c1 as Cc1 "
      + " PATTERN(B C|A*)"
      + " DEFINE A as A.c1 = 10, B as B.c1 > 5 , C as C.c1 > 40"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern14_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern14_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  /*
  //Test Corresponding to tkpattern_q55 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  //TODO: No Output For First Phase Too; Validate whether query inside CQLX test works or not
  public void testPattern15() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 integer)",
      "create query q1 as select T.firstW,T.lastZ,T.sumDecrArm,T.sumIncrArm,T.overallAvg from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES tkpattern_S2.c1 as firstW, last(tkpattern_S1.c1) as lastZ, sum(tkpattern_S3.c2) as sumDecrArm, sum(tkpattern_S4.c2) as sumIncrArm, avg(tkpattern_S5.c2) as overallAvg "
      + " PATTERN(A W+ X+ Y+ Z+)"
      + " SUBSET tkpattern_S1 = (Z) tkpattern_S2 = (A) tkpattern_S3 = (A,W,Y) tkpattern_S4 = (X,Z) tkpattern_S5 = (A,W,X,Y,Z) tkpattern_S6 = (Y)"
      + " DEFINE W as W.c2 < prev(W.c2), X as X.c2 > prev(X.c2), Y as tkpattern_S6.c2 < prev(Y.c2), Z as Z.c2 > prev(Z.c2)"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern15_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern15_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }*/
  
  //Test Corresponding to tkpattern_q60 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern16() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select T.p1, T.p2 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as p1, B.c1 as p2 include timer events"
      + " PATTERN(A B+) duration 10"
      + " DEFINE A as A.c1 = 10, B as B.c1 != A.c1"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern16_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern16_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q75 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern17() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer)",
      "create query q1 as select  T.p1, T.p2, T.p3 from S1 "
      + " MATCH_RECOGNIZE ("
      + " MEASURES A.c1 as p1, B.c1 as p2, sum(B.c1) as p3 ALL MATCHES include timer events"
      + " PATTERN(A B*) duration multiples of 10 "
      + " DEFINE A as A.c1 = 10, B as B.c1 != A.c1"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern17_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern17_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q81 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern18() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 char(4))",
      "create query q1 as select T.c2, T.p1, T.p2 from S1 "
      + " MATCH_RECOGNIZE ("
      + " PARTITION BY c2 MEASURES A.c1 as p1, B.c1 as p2, A.c2 as c2 include timer events "
      + " PATTERN(A B*) duration c1+4  "
      + " DEFINE B as B.c1 != A.c1"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern18_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern18_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
//Test Corresponding to tkpattern_q111 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern19() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 integer, c3 integer)",
      "create query q1 as select T.Ac1, T.Ac3, T.Dc3, T.Ec3  from S1 "
      + " MATCH_RECOGNIZE ("
      + " PARTITION BY c1 MEASURES A.c1 as Ac1, A.c3 as Ac3, D.c3 as Dc3, E.c3 as Ec3 "
      + " PATTERN (A (B C D | E)) within 3 "
      + " DEFINE A as A.c2=10, B as B.c2=20 , C as C.c2=30, D as D.c2=40, E as E.c2=20"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern19_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern19_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q112 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern20() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 integer, c3 integer)",
      "create query q1 as select T.Ac1, T.Ac3, T.Dc3, T.Ec3  from S1 "
      + " MATCH_RECOGNIZE ("
      + " PARTITION BY c1 MEASURES A.c1 as Ac1, A.c3 as Ac3, D.c3 as Dc3, E.c3 as Ec3  ALL MATCHES"
      + " PATTERN (A (B C D | E)) within 3 "
      + " DEFINE A as A.c2=10, B as B.c2=20 , C as C.c2=30, D as D.c2=40, E as E.c2=20"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern20_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern20_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  //Test Corresponding to tkpattern_q115 of SPARKCQL_HOME/cqlengine/test/sql/tkpattern.cqlx
  public void testPattern21() throws Exception
  {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    
    String[] setupDDLs = new String[] 
    {
      "register stream S1(c1 integer, c2 integer, c3 integer)",
      "create query q1 as select T.Ac1, T.Ac3, T.Dc3, T.Ec3 from S1 "
      + " MATCH_RECOGNIZE ("
      + " PARTITION BY c1 MEASURES A.c1 as Ac1, A.c3 as Ac3, D.c3 as Dc3, E.c3 as Ec3 "
      + " PATTERN (A (B C D | E)) within inclusive 3 "
      + " DEFINE A as A.c2=10, B as B.c2=20 , C as C.c2=30, D as D.c2=40, E as E.c2=20"
      + " ) as T"
    };  

    String[] tearDownDDLs = new String[]
    {
      "drop query q1",
      "drop stream S1"
    };    
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs); 
    testMetadata.addSourceMetadata("S1","inpPattern21_S1");
    testMetadata.addDestinationMetadata("q1", "outPattern21_q1");
    
    runFullSnapshotTest(testMetadata, testSchema);
  }
}
