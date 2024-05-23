package oracle.cep.test.ha;

public class HATestDistinct extends BaseCQLTestCase {

	public HATestDistinct() throws Exception {
		super();
	}

	public void testDistinct1() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(10))",
				"create query q1 as select distinct c1, c2 from S1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinct1_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinct1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDistinct2() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();
		;

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(10))",
				"create query q1 as select distinct c1, c2 from S1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinct2_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinct2_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDistinct3() throws Exception {

	  String testSchema = "testDistinct3";

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer)",
				"create query q1 as select distinct c1 from S1[Range 10 seconds]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinct3_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinct3_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDistinct4() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select distinct c1 from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinct4_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinct4_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDistinct5() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select distinct count(*) from S1[Rows 3]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinct5_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinct5_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDistinctBigint() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint)",
				"create query q1 as select distinct c2 from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinctBigint_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinctBigint_q1");

		runFullSnapshotTest(testMetadata, testSchema);

	}
	
 /** TODO: Move this test to java cartridge module
	public void testDistinctBigdecimal() throws Exception {

		testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 number(7,5))",
				"create query q1 as select distinct c2 from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };

		testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinctBigdecimal_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinctBigdecimal_q1");

		runFullSnapshotTest();
	}*/

	public void testDistinctBoolean() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 boolean)",
				"create query q1 as select distinct c2 from S1[Range 10 seconds]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinctBoolean_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinctBoolean_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
          
	public void testDistinctChar() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select distinct c2 from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinctChar_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinctChar_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
	public void testDistinctDouble() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 double)",
				"create query q1 as select distinct c2 from S1[Range 10 seconds]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinctDouble_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinctDouble_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDistinctFloat() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 float)",
				"create query q1 as select distinct c2 from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinctFloat_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinctFloat_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testDistinctTimestamp() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 timestamp)",
				"create query q1 as select distinct c2 from S1[Rows 5]", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpDistinctTimestamp_S1");
		testMetadata.addDestinationMetadata("q1", "outDistinctTimestamp_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}