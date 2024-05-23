package oracle.cep.test.ha;

public class HATestOrderBy extends BaseCQLTestCase {
	public HATestOrderBy() throws Exception {
		super();
	}

	public void testOrderBy1() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(10))",
				"create query q1 as select S1.c2 from S1 where S1.c1 < 12 order by c1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderBy1_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderBy1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	public void testOrderByBigint() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 bigint)",
				"create query q1 as select * from S1 where S1.c2 >0  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByBigint_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByBigint_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByBigintWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 bigint)",
				"create query q1 as select * from S1 where S1.c2 >0  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByBigintWithNull_S1");
		testMetadata
				.addDestinationMetadata("q1", "outOrderByBigintWithNull_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	/*
	public void testOrderByBigdecimal() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 number(7,5))",
				"create query q1 as select * from S1 where S1.c2 >11.90006  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByBigdecimal_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByBigdecimal_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByBigdecimalWithNull() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 number(7,5))",
				"create query q1 as select * from S1 where S1.c2 <99.90006  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByBigdecimalWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByBigdecimalWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
*/
	
	public void testOrderByChar() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 char(20))",
				"create query q1 as select * from S1 order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByChar_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByChar_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByCharWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 char(20))",
				"create query q1 as select * from S1 order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByCharWithNull_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByCharWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	

	public void testOrderByDouble() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 double)",
				"create query q1 as select * from S1 where S1.c2 >3.24  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByDouble_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByDouble_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByDoubleWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 double)",
				"create query q1 as select * from S1 where S1.c2 >3.24  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByDoubleWithNull_S1");
		testMetadata
				.addDestinationMetadata("q1", "outOrderByDoubleWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	

	public void testOrderByFloat() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 float)",
				"create query q1 as select * from S1 where S1.c2 >2.3  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByFloat_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByFloat_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByFloatWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer,  c2 float)",
				"create query q1 as select * from S1 where S1.c2 >2.3  order by c2", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByFloatWithNull_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByFloatWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByIntWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer)",
				"create query q1 as select * from S1 where S1.c1 < 100  order by c1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByIntWithNull_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByIntWithNull_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTimestamp() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 timestamp)",
				"create query q1 as select * from S1  order by c1", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTimestamp_S1");
		testMetadata.addDestinationMetadata("q1", "outOrderByTimestamp_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testOrderByTimestampWithNull() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] { "register stream S1(c1 timestamp)",
				"create query q1 as select * from S1  order by c1 ", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpOrderByTimestampWithNull_S1");
		testMetadata.addDestinationMetadata("q1",
				"outOrderByTimestampWithNull_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
