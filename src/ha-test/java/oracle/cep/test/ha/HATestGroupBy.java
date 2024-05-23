package oracle.cep.test.ha;

import oracle.cep.snapshot.SnapshotContext;

public class HATestGroupBy extends BaseCQLTestCase
{
  public HATestGroupBy() throws Exception {
    super();
  }
    
  public void testGroupBy1() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "create query q1 as select count(*) from S1", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupBy1_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupBy1_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupBy2() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 char(10), c2 integer)",
        "create query q1 as select c1,count(*) from S1 group by c1",
    // StreamSrc -> GroupAggr -> Project -> Output
    };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupBy2_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupBy2_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  /** TODO: Move this test to java cartridge module
  public void testGroupByBigdecimal() throws Exception {

    testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2  number(7,5)) ",
        "create query q1 as select c2, count(*) from S1 group by c2", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBigdecimal_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBigdecimal_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }*/

  public void testGroupByBigint() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2  bigint) ",
        "create query q1 as select c2, count(*) from S1 group by c2", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBigint_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBigint_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupByBoolean() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2  boolean) ",
        "create query q1 as select c2, count(*) from S1 group by c2", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBoolean_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBoolean_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupByChar() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 char(20)) ",
        "create query q1 as select c2, count(*) from S1 group by c2", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByChar_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByChar_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupByDouble() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 double) ",
        "create query q1 as select c2, count(*) from S1 group by c2", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByDouble_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByDouble_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupByFloat() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 float) ",
        "create query q1 as select c2,count(*) from S1 group by c2", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByFloat_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByFloat_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupByTimestamp() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2  timestamp) ",
        "create query q1 as select c2, count(*) from S1 group by c2", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByTimestamp_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByTimestamp_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupByBuiltInAggr1() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "create query q1 as select count(*), sum(c1), avg(c1), min(c1), max(c1) from S1", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr1_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr1_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  public void testGroupByBuiltInAggr2() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "create query q1 as select count(*), sum(c1), avg(c1), min(c1), max(c1) from S1[range 2]", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr2_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr2_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  public void testGroupByBuiltInAggr3() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "create query q1 as select median(c1) from S1", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr3_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr3_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
   public void testGroupByBuiltInAggr3WithNull() throws Exception {

      String testSchema = new Object() {
      }.getClass().getEnclosingMethod().getName();

      String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
          "create query q1 as select median(c1) from S1", };

      String[] tearDownDDLs = new String[] { "drop query q1",
          "drop stream S1" };
      TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
      testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr3WithNull_S1");
      testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr3WithNull_q1");

      runFullSnapshotTest(testMetadata, testSchema);
    }
  
  public void testGroupByBuiltInAggr4() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "create query q1 as select mean(c1) from S1", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr4_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr4_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  public void testGroupByBuiltInAggr5() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "create query q1 as select quantile(c1, 0.25), quantile(c1, 0.5), quantile(c1,0.75) from S1", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr5_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr5_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  public void testGroupByBuiltInAggr6() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer, c2 bigint, c3 float, c4 double, c5 number(7,5))",
          "create query q1 as select round(standardDeviation(c1)), round(standardDeviation(c2)), round(standardDeviation(c3)), round(standardDeviation(c4)), standardDeviation(c5) from S1", };

    String[] tearDownDDLs = new String[] { "drop query q1",
          "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr6_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr6_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
    
  public void testGroupByBuiltInAggr7() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 float)",
        "create function summation(c1 float) return float aggregate using \"oracle.cep.test.ha.userfunctions.TkSum\"",
        "create query q1 as select summation(c1) from S1", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr7_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr7_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testGroupByBuiltInAggr8() throws Exception {

      String testSchema = new Object() {
      }.getClass().getEnclosingMethod().getName();

      String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
          "create query q1 as select count(*), sum(c1), avg(c1), min(c1), max(c1) from S1[rows 2]", };

      String[] tearDownDDLs = new String[] { "drop query q1",
          "drop stream S1" };
      TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
      testMetadata.addSourceMetadata("S1", "inpGroupByBuiltInAggr8_S1");
      testMetadata.addDestinationMetadata("q1", "outGroupByBuiltInAggr8_q1");

      runFullSnapshotTest(testMetadata, testSchema);
    }
  
  public void testGroupByPreserveCols1() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(busid integer, lat double, long double, driverid integer, busname char(50))",
        "create query q1 as select busid, count(*), current(lat), current(long), current(driverid), current(busname) from S1 group by busid"};

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByPreserveCols1_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByPreserveCols1_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  public void testGroupByPreserveCols2() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(busid integer, lat double, long double, driverid integer, busname char(50))",
        "create query q1 as select busid, count(*), current(lat), current(long), current(driverid), current(busname) from S1[RANGE 10 seconds] group by busid"};

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpGroupByPreserveCols1_S1");
    testMetadata.addDestinationMetadata("q1", "outGroupByPreserveCols2_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
}
