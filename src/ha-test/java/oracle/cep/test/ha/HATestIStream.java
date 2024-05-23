package oracle.cep.test.ha;

public class HATestIStream extends BaseCQLTestCase {
	public HATestIStream() throws Exception {
		super();
	}

	public void testIStream() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register relation R1(c1 integer)",
				"create query q1 as istream(select c1 from R1)" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop relation R1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("R1", "inpIStream_R1");
		testMetadata.addSourceType("R1", SourceType.RELATION);
		testMetadata.addDestinationMetadata("q1", "outIStream_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testIStreamDifferenceUsing() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register relation R1(c1 integer)",

		"create query q1 as ISTREAM ( SELECT c1 FROM R1 ) DIFFERENCE USING (c1)" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop relation R1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("R1", "inpIStreamDifferenceUsing_R1");
		testMetadata.addSourceType("R1", SourceType.RELATION);
		testMetadata.addDestinationMetadata("q1",
				"outIStreamDifferenceUsing_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testIStreamBasicType() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register relation R1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
				"create query q1 as istream(select * from R1)" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop relation R1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("R1", "inpIStreamBasicType_R1");
		testMetadata.addSourceType("R1", SourceType.RELATION);
		testMetadata.addDestinationMetadata("q1", "outIStreamBasicType_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
