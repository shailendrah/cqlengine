package oracle.cep.test.ha;

import java.util.Properties;

import oracle.cep.test.ha.BaseCQLTestCase.SourceType;

/**
 * Test Cases for Binary Join
 *
 */

public class HATestBinJoin extends BaseCQLTestCase {
	public HATestBinJoin() throws Exception {
		super();
	}

	/**
	 * Test Case for Binary Join
	 * Goal: Test Binary Join of two Row window based relations
	 * @author sbishnoi
	 *
	 */
	public void testBinJoin1() throws Exception {
		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[rows 4], S2[Rows 4]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinJoin1_S1");
		testMetadata.addSourceMetadata("S2", "inpBinJoin1_S2");
		testMetadata.addDestinationMetadata("q1", "outBinJoin1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	/**
	 * Test Case for Binary Join 
	 * Goal: Test Binary Join of Two Range Window based relations
	 * @author sbishnoi
	 *
	 */
	public void testBinJoin2() throws Exception {
	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();
		;

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[range 4], S2[range 4]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinJoin2_S1");
		testMetadata.addSourceMetadata("S2", "inpBinJoin2_S2");
		testMetadata.addDestinationMetadata("q1", "outBinJoin2_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	/**
	 * Test Case for Binary Join
	 * Goal: Test Binary Join of two Row window based relations with predicate clause
	 * @author sbishnoi
	 *
	 */
	public void testBinJoin3() throws Exception {
	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[rows 4] as Rel1, S2[rows 4] as Rel2 where Rel1.c1 = Rel2.c1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinJoin3_S1");
		testMetadata.addSourceMetadata("S2", "inpBinJoin3_S2");
		testMetadata.addDestinationMetadata("q1", "outBinJoin3_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
	/**
	 * Test Case for Binary Join
	 * Goal:
	 * 1. Test Binary Join of two RANGE Window based relations
	 * 2. Test Predicate with Binary Join
	 */
	public void testBinJoin4() throws Exception {
	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[range 4] as Rel1, S2[range 4] as Rel2 where Rel1.c1 = Rel2.c1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinJoin4_S1");
		testMetadata.addSourceMetadata("S2", "inpBinJoin4_S2");
		testMetadata.addDestinationMetadata("q1", "outBinJoin4_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
	/**
	 * Test Case for Binary Join
	 * Goal:
	 * 1. Test Multiway Binary Join of Three Streams each having a row window
	 * 2. Test Predicate based multiway binary join
	 * @author sbishnoi
	 *
	 */
	public void testBinJoin5() throws Exception {
	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"register stream S3(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[rows 4] as Rel1, S2[Rows 4] as Rel2, S3[Rows 4] as Rel3 where Rel1.c1 = Rel2.c1 and Rel1.c1 = Rel3.c1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2", "drop stream S3" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinJoin5_S1");
		testMetadata.addSourceMetadata("S2", "inpBinJoin5_S2");
		testMetadata.addSourceMetadata("S3", "inpBinJoin5_S3");
		testMetadata.addDestinationMetadata("q1", "outBinJoin5_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	/**
	 * Test Case for Binary Join
	 * Goal:
	 * 1. Test Binary Join of a Stream and an External Relation
	 * @author sbishnoi
	 *
	 */
	public void testBinJoin6() throws Exception {
	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register external relation tkExternal_R2(d1 integer, d2 bigint, d3 char(10))",
				"create query q1 as select * from S1[now] as Rel, tkExternal_R2 as Ext where Rel.c1 = Ext.d1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop relation tkExternal_R2" };

		Properties prop = readProperties();

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinJoin6_S1");
		testMetadata.addSourceMetadata("tkExternal_R2", "soainfra");
		testMetadata.addSourceType("tkExternal_R2",
				SourceType.EXTERNAL_RELATION);
		testMetadata.addDestinationMetadata("q1", "outBinJoin6_q1");
		testMetadata.addDataSource(
				"soainfra",
				"jdbc:oracle:thin:" + prop.getProperty("testUser") + "/"
						+ prop.getProperty("testPassword") + "@//"
						+ prop.getProperty("host") + ":"
						+ prop.getProperty("port") + "/"
						+ prop.getProperty("sid"));

		runFullSnapshotTest(testMetadata, testSchema);

	}

	/**
	 * Test Case for Binary Join
	 * Goal:
	 * Goal: Test Binary Join of Two Range Window based relations with basic data types covered
	 * @author fifang
	 *
	 */
	public void testBinJoinRange() throws Exception {
	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 float, c3 double, c4 char(20), c5 boolean)",
				"register stream S2(c1 integer, c2 timestamp, c3 bigint)",
				"create query q1 as select * from S1[range 4], S2[range 4]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinJoinRange_S1");
		testMetadata.addSourceMetadata("S2", "inpBinJoinRange_S2");
		testMetadata.addDestinationMetadata("q1", "outBinJoinRange_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
  /**
   * Test Case for Binary Join
   * Goal: Test Binary Join of two Row window based relations where S1 is silent
   * in phase 1 so events of S2 are enqueued.
   * This tests the snapshot for queue.
   * @author sbishnoi
   *
   */
  public void testBinJoin7() throws Exception {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 char(20))",
        "register stream S2(c1 integer, c2 char(20))",
        "create query q1 as select * from S1[rows 4], S2[Rows 4]", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1", "drop stream S2" };

    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpBinJoin7_S1");
    testMetadata.addSourceMetadata("S2", "inpBinJoin7_S2");
    testMetadata.addDestinationMetadata("q1", "outBinJoin7_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
	
	/**
   * Test Case for Binary Join
   * Goal: Test Binary Join of two Row window based relations where S1 is silent
   * in phase 1 so events of S2 are enqueued.
   * This tests the snapshot for queue.
   * @author sbishnoi
   *
   */
  public void testBinJoin8() throws Exception {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer, c2 char(20))",
        "register stream S2(c1 integer, c2 char(20))",
        "create query q1 as select * from S1[rows 4], S2[Rows 4]", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1", "drop stream S2" };

    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpBinJoin8_S1");
    testMetadata.addSourceMetadata("S2", "inpBinJoin8_S2");
    testMetadata.addDestinationMetadata("q1", "outBinJoin8_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
  /**
   * Test Case for Binary Join
   * Goal: Test Binary Join of a Row window based relation with a Silent Relation
   * This tests the incremental snapshot optimization for silent relation.
   * @author sbishnoi
   *
 /*public void testBinJoin9() throws Exception {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 integer)",
        "register relation R1(c1 integer, c2 char(20)) is silent",
        "create query q1 as select * from S1[rows 10], R1 ", };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1", "drop relation R1" };

    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpBinJoin9_S1");
    testMetadata.addSourceMetadata("R1", "inpBinJoin9_R1");
    testMetadata.addSourceType("R1", SourceType.RELATION);
    testMetadata.addDestinationMetadata("q1", "outBinJoin9_q1");
    
    runJournalSnapshotTest(testMetadata, testSchema);
  }*/
}
