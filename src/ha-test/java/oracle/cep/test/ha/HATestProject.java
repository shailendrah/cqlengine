package oracle.cep.test.ha;

public class HATestProject extends BaseCQLTestCase {
	public HATestProject() throws Exception {
		super();
	}

	public void testProject1() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 char(10), c2 integer)",
				"create query q1 as select c1,count(*) from S1 group by c1 having count(*) > 1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpProject1_S1");
		testMetadata.addDestinationMetadata("q1", "outProject1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testProject2() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(1))",
				"create query q1 as select * from S1[Rows 4] where c1 > 1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpProject2_S1");
		testMetadata.addDestinationMetadata("q1", "outProject2_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
