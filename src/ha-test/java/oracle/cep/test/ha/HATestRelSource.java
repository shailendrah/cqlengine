package oracle.cep.test.ha;

public class HATestRelSource extends BaseCQLTestCase {
	public HATestRelSource() throws Exception {
		super();
	}

	public void testRelSource() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register relation R1(c1 integer)",
				"create query q1 as dstream(select c1 from R1)" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop relation R1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("R1", "inpRelSource_R1");
		testMetadata.addSourceType("R1", SourceType.RELATION);
		testMetadata.addDestinationMetadata("q1", "outRelSource_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
/** TODO: Fix this test
	public void testRelSourceRStream() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register relation R1(c1 integer)",
				"create query q1 as rstream(select c1 from R1)" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop relation R1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("R1", "inpRelSourceRStream_R1");
		testMetadata.addSourceType("R1", SourceType.RELATION);
		testMetadata.addDestinationMetadata("q1", "outRelSourceRStream_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
*/
	public void testRelSourceBasicType() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register relation R1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
				"create query q1 as dstream(select * from R1)" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop relation R1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("R1", "inpRelSourceBasicType_R1");
		testMetadata.addSourceType("R1", SourceType.RELATION);
		testMetadata.addDestinationMetadata("q1", "outRelSourceBasicType_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
