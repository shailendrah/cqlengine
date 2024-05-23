package oracle.cep.test.ha;

public class HATestUnion extends BaseCQLTestCase
{
  public HATestUnion() throws Exception
  {
    super();
  }

  public void testUnion1() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "register stream S2(c1 integer)", "create query q1 as S1 union all S2", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop stream S1",
        "drop stream S2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpUnion1_S1");
    testMetadata.addSourceMetadata("S2", "inpUnion1_S2");
    testMetadata.addDestinationMetadata("q1", "outUnion1_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testUnion2() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 char(10))",
        "register stream S2(c1 integer, c2 char(10))",
        "create query q1 as select * from S1[rows 10] union select * from S2[rows 10]", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop stream S1",
        "drop stream S2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpUnion2_S1");
    testMetadata.addSourceMetadata("S2", "inpUnion2_S2");
    testMetadata.addDestinationMetadata("q1", "outUnion2_q1");
    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testUnion3() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register relation  R1(c1 integer)",
        "register relation  R2(c1 integer)",
        "create query q1 as R1 union all R2", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop relation R1",
        "drop relation R2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("R1", "inpUnion3_R1");
    testMetadata.addSourceType("R1", SourceType.RELATION);
    testMetadata.addSourceMetadata("R2", "inpUnion3_R2");
    testMetadata.addSourceType("R2", SourceType.RELATION);
    testMetadata.addDestinationMetadata("q1", "outUnion3_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testUnion4() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 char(10))",
        "register stream S2(c1 integer, c2 char(10))",
        "create query q1 as select * from S1[rows 3] union select * from S2[rows 3]", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop stream S1",
        "drop stream S2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpUnion4_S1");
    testMetadata.addSourceMetadata("S2", "inpUnion4_S2");
    testMetadata.addDestinationMetadata("q1", "outUnion4_q1");
    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testUnion() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
        "register stream S2(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
        "create query q1 as select * from S1[rows 3] union select * from S2[rows 3]", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop stream S1",
        "drop stream S2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpUnion_S1");
    testMetadata.addSourceMetadata("S2", "inpUnion_S2");
    testMetadata.addDestinationMetadata("q1", "outUnion_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testUnionAll() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
        "register stream S2(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
        "create query q1 as S1 union all S2", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop stream S1",
        "drop stream S2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpUnionAll_S1");
    testMetadata.addSourceMetadata("S2", "inpUnionAll_S2");
    testMetadata.addDestinationMetadata("q1", "outUnionAll_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testUnion5() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "register stream S2(c1 integer)", "create query q1 as S1 union all S2", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop stream S1",
        "drop stream S2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpUnion5_S1");
    testMetadata.addSourceMetadata("S2", "inpUnion5_S2");
    testMetadata.addDestinationMetadata("q1", "outUnion5_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }

  public void testUnion6() throws Exception
  {

    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
        "register stream S2(c1 integer)", "create query q1 as S1 union all S2", };

    String[] tearDownDDLs = new String[] { "drop query q1", "drop stream S1",
        "drop stream S2" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpUnion6_S1");
    testMetadata.addSourceMetadata("S2", "inpUnion6_S2");
    testMetadata.addDestinationMetadata("q1", "outUnion6_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
}