package oracle.cep.test.ha;

public class HATestRStream extends BaseCQLTestCase {
	public HATestRStream() throws Exception {
		super();
	}
	
	public void testRStream() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as rstream(select c1 from S1[range 4 milliseconds])" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRStream_S1");
		testMetadata.addSourceType("S1", SourceType.STREAM);
		testMetadata.addDestinationMetadata("q1", "outRStream_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testRStreamBasicType() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint,  c3 float, c4 double, c5 char(20), c6 boolean, c7 timestamp, c8 number(7,5))",
				"create query q1 as rstream(select * from S1[range 4 milliseconds])" };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpRStreamBasicType_S1");
		testMetadata.addSourceType("S1", SourceType.STREAM);
		testMetadata.addDestinationMetadata("q1", "outRStreamBasicType_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	 public void testRStream1() throws Exception {

	    String testSchema = new Object() {
	    }.getClass().getEnclosingMethod().getName();

	    String[] setupDDLs = new String[] {
	        "register stream S1(productid int, producttypeid int, name char(1024), price double, description char(1024))",
	        "create query q1 as rstream(select median(price) from S1[range 1 hour slide 1 seconds])" };

	    String[] tearDownDDLs = new String[] { "drop query q1",
	        "drop stream S1" };
	    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
	    testMetadata.addSourceMetadata("S1", "inpRStream1_S1");
	    testMetadata.addSourceType("S1", SourceType.STREAM);
	    testMetadata.addDestinationMetadata("q1", "outRStream1_q1");

	    runFullSnapshotTest(testMetadata, testSchema);
	  }
}
