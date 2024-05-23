package oracle.cep.test.ha;

public class HATestSelfJoin extends BaseCQLTestCase {
	public HATestSelfJoin() throws Exception {
		super();
	}

	public void testSelfJoin1() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[rows 4] as R1, S1[rows 2] as R2" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelfJoin1_S1");
		testMetadata.addDestinationMetadata("q1", "outSelfJoin1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testSelfJoin2() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[range 4 seconds] as R1, S1[range 2 seconds] as R2" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelfJoin2_S1");
		testMetadata.addDestinationMetadata("q1", "outSelfJoin2_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testSelfJoinRange() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp)",
				"create query q1 as select * from S1[range 4 seconds] as R1, S1[range 2 seconds] as R2" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelfJoinRange_S1");
		testMetadata.addDestinationMetadata("q1", "outSelfJoinRange_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testSelfJoinRows() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 byte(10), c7 boolean)",
				"create query q1 as select * from S1[rows 4] as R1, S1[rows 2] as R2" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelfJoinRows_S1");
		testMetadata.addDestinationMetadata("q1", "outSelfJoinRows_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testSelfJoinRowsTimestamp() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 char(20), c2 timestamp)",
				"create query q1 as select * from S1[rows 4] as R1, S1[rows 2] as R2" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelfJoinRowsTimestamp_S1");
		testMetadata
				.addDestinationMetadata("q1", "outSelfJoinRowsTimestamp_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
/* TODO: Move these tests to java cartridge module
  public void testSelfJoinRangeBigdecimal() throws Exception {

    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();

    String[] setupDDLs = new String[] {
        "register stream S1(c1 char(20), c2 number(7,5))",
        "create query q1 as select * from S1[range 4 seconds] as R1, S1[range 2 seconds] as R2" };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop stream S1" };

    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("S1", "inpSelfJoinRangeBigdecimal_S1");
    testMetadata.addDestinationMetadata("q1",
        "outSelfJoinRangeBigdecimal_q1");

    runFullSnapshotTest(testMetadata, testSchema);
  }
  
	public void testSelfJoinRowsBigdecimal() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 number(7,5))",
				"create query q1 as select * from S1[rows 4] as R1, S1[rows 2] as R2" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelfJoinRowsBigdecimal_S1");
		testMetadata.addDestinationMetadata("q1",
				"outSelfJoinRowsBigdecimal_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}*/

}
