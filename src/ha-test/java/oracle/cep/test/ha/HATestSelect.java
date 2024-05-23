package oracle.cep.test.ha;

public class HATestSelect extends BaseCQLTestCase {
	public HATestSelect() throws Exception {
		super();
	}

	public void testSelect1() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select S1.c1 from S1 where S1.c1 < 8", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelect1_S1");
		testMetadata.addDestinationMetadata("q1", "outSelect1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testSelect2() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 char(10), c2 integer)",
				"create query q1 as select c1,count(*) from S1 group by c1 having count(*) > 1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelect2_S1");
		testMetadata.addDestinationMetadata("q1", "outSelect2_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testSelect3() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select * from S1[Rows 4] where c1 > 1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpSelect3_S1");
		testMetadata.addDestinationMetadata("q1", "outSelect3_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
