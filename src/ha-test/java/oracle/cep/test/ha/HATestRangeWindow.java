package oracle.cep.test.ha;

public class HATestRangeWindow extends BaseCQLTestCase {
	
  public HATestRangeWindow() throws Exception {
		super();
	}

	public void testRangeWindow1() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select * from S1[Range 10 seconds]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRangeWindow1_S1");
		testMetadata.addDestinationMetadata("q1", "outRangeWindow1_q1");

		runFullSnapshotTest(testMetadata, testSchema);

	}

	public void testRangeWindow2() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer)",
				"create query q1 as select * from S1[Range 10 seconds Slide 5 seconds]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRangeWindow2_S1");
		testMetadata.addDestinationMetadata("q1", "outRangeWindow2_q1");

		runFullSnapshotTest(testMetadata, testSchema);

	}

	public void testRangeWindow3() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[Range 10 seconds]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRangeWindow3_S1");
		testMetadata.addDestinationMetadata("q1", "outRangeWindow3_q1");

		 runJournalSnapshotTest(testMetadata, testSchema);      ;
	}

	public void testRangeWindow4() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[Range 10 seconds Slide 3 seconds]", };
		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRangeWindow4_S1");
		testMetadata.addDestinationMetadata("q1", "outRangeWindow4_q1");

		runJournalSnapshotTest(testMetadata, testSchema);
	}

	public void testRangeWindow() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
				"create query q1 as select * from S1[Range 10 seconds Slide 3 seconds]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRangeWindow_S1");
		testMetadata.addDestinationMetadata("q1", "outRangeWindow_q1");

		runJournalSnapshotTest(testMetadata, testSchema);
	}
}
