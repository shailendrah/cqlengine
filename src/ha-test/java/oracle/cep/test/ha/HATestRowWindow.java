package oracle.cep.test.ha;

public class HATestRowWindow extends BaseCQLTestCase {
	
  public HATestRowWindow() throws Exception {
		super();
	}

	public void testRowWindow1() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select * from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRowWindow1_S1");
		testMetadata.addDestinationMetadata("q1", "outRowWindow1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testRowWindow2() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select * from S1[Rows 6 Slide 3]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRowWindow2_S1");
		testMetadata.addDestinationMetadata("q1", "outRowWindow2_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testRowWindow3() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[Rows 2 Slide 3]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRowWindow3_S1");
		testMetadata.addDestinationMetadata("q1", "outRowWindow3_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testRowWindow4() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRowWindow4_S1");
		testMetadata.addDestinationMetadata("q1", "outRowWindow4_q1");

		runJournalSnapshotTest(testMetadata, testSchema);
	}

	public void testRowWindow5() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[Rows 5 slide 3]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRowWindow5_S1");
		testMetadata.addDestinationMetadata("q1", "outRowWindow5_q1");

		runJournalSnapshotTest(testMetadata, testSchema);      
	}

	public void testRowWindow6() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select * from S1[Rows 3 slide 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRowWindow6_S1");
		testMetadata.addDestinationMetadata("q1", "outRowWindow6_q1");

		runJournalSnapshotTest(testMetadata, testSchema);
	}

	public void testRowWindow() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 byte(10), c7 boolean, c8 timestamp, c9 number(7,5))",
				"create query q1 as select * from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRowWindow_S1");
		testMetadata.addDestinationMetadata("q1", "outRowWindow_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
