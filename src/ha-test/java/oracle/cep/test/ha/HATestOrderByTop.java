package oracle.cep.test.ha;

public class HATestOrderByTop extends BaseCQLTestCase {
	public HATestOrderByTop() throws Exception {
		super();
	}

	public void testOrderByTop1() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(10))",
				"create query q1 as select S1.c2 from S1 order by c1 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTop1_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTop1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTop2() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(10))",
				"create query q1 as select S1.c2 from S1 order by c1 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTop2_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTop2_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTop3() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(10))",
				"create query q1 as select S1.c2 from S1[Rows 5] order by c1 rows 4", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTop3_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTop3_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTop4() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(10))",
				"create query q1 as select * from S1 partition by c2 order by c1 rows 2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTop4_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTop4_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	

	public void testOrderByTopBigint() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopBigint_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTopBigint_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTopBigintWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 bigint)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopBigintWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByTopBigintWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
/* TODO: Move these tests to java cartridge module
	public void testOrderByTopBigdecimal() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 number(7,5))",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopBigdecimal_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTopBigdecimal_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTopBigdecimalWithNull() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 number(7,5))",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1",
				"inpOrderByTopBigdecimalWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByTopBigdecimalWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}*/

	public void testOrderByTopChar() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopChar_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTopChar_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTopCharWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopCharWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByTopCharWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	

	public void testOrderByTopDouble() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 double)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopDouble_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTopDouble_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTopDoubleWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 double)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopDoubleWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByTopDoubleWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
	public void testOrderByTopFloat() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 float)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopFloat_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTopFloat_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTopFloatWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 float)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopFloatWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByTopFloatWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
	public void testOrderByTopIntWithNull() throws Exception {
	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 integer)",
				"create query q1 as select S1.* from S1 order by c1 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopIntWithNull_S1");
		testMetadata
				.addDestinationMetadata("q1", "outOrderByTopIntWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTopTimestamp() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 timestamp)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTopTimestamp_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTopTimestamp_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTopTimestampWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 timestamp)",
				"create query q1 as select S1.* from S1 order by c2 rows 3", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1",
				"inpOrderByTopTimestampWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByTopTimestampWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
