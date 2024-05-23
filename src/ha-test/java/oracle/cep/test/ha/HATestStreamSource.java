package oracle.cep.test.ha;


public class HATestStreamSource extends BaseCQLTestCase {

	public HATestStreamSource() throws Exception {
		super();
	}

	public void testStreamSource1() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select * from S1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpStreamSource1_S1");
		testMetadata.addDestinationMetadata("q1", "outStreamSource1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testStreamSource() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
				"create query q1 as select * from S1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpStreamSource_S1");
		testMetadata.addDestinationMetadata("q1", "outStreamSource_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
