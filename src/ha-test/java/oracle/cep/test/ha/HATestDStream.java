package oracle.cep.test.ha;

public class HATestDStream extends BaseCQLTestCase {

	public HATestDStream() throws Exception {
		super();
	}

	public void testDStream() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as dstream(select c1 from S1[rows 4])" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDStream_S1");
		testMetadata.addSourceType("S1", SourceType.STREAM);
		testMetadata.addDestinationMetadata("q1", "outDStream_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDStreamDifferenceUsing() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer)",

				"create query q1 as DSTREAM ( SELECT c1 FROM S1 [RANGE 1 MILLISECONDS] ) DIFFERENCE USING (c1)" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDStreamDifferenceUsing_S1");
		testMetadata.addSourceType("S1", SourceType.STREAM);
		testMetadata.addDestinationMetadata("q1",
				"outDStreamDifferenceUsing_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDStreamBasicType() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp)",
				"create query q1 as dstream(select * from S1[rows 4])" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDStreamBasicType_S1");
		testMetadata.addSourceType("S1", SourceType.STREAM);
		testMetadata.addDestinationMetadata("q1", "outDStreamBasicType_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
 /* TODO: Move the test to java cartridge module
	public void testDStreamBigdecimal() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 number(7,5))",
				"create query q1 as dstream(select * from S1[rows 4])" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDStreamBigdecimal_S1");
		testMetadata.addSourceType("S1", SourceType.STREAM);
		testMetadata.addDestinationMetadata("q1", "outDStreamBigdecimal_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}*/

}
