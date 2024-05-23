package oracle.cep.test.ha;

/**
 * Test Cases for Binary Stream Join
 *
 */
public class HATestBinStreamJoin extends BaseCQLTestCase 
{
	public HATestBinStreamJoin() throws Exception {
		super();
	}
	
  /**
	 * Test Case for Binary Stream Join
	 * Goal: Test Binary Stream Join of a stream with row window based relation
	 * @author sbishnoi
	 *
	 */
	public void testBinStreamJoin1() throws Exception {

		String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as RStream(select * from S1[now], S2[Rows 4])", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinStreamJoin1_S1");
		testMetadata.addSourceMetadata("S2", "inpBinStreamJoin1_S2");
		testMetadata.addDestinationMetadata("q1", "outBinStreamJoin1_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
	/**
	 * Test Case for Binary Stream Join
	 * Goal: Test Binary Stream Join of a stream with range window based relation
	 * @author sbishnoi
	 *
	 */
	public void testBinStreamJoin2() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as RStream(select * from S1[now], S2[Range 4 seconds])", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinStreamJoin2_S1");
		testMetadata.addSourceMetadata("S2", "inpBinStreamJoin2_S2");
		testMetadata.addDestinationMetadata("q1", "outBinStreamJoin2_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	/**
	 * Test Case for Binary Stream Join
	 * Goal: Test Binary Stream Join of a stream with row window based relation with correlation predicate
	 * @author sbishnoi
	 *
	 */
	public void testBinStreamJoin3() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as RStream(select * from S1[now] as Rel1, S2[Rows 4] as Rel2 where Rel1.c1 = Rel2.c1)", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinStreamJoin3_S1");
		testMetadata.addSourceMetadata("S2", "inpBinStreamJoin3_S2");
		testMetadata.addDestinationMetadata("q1", "outBinStreamJoin3_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	/**
	 * Test Case for Binary Stream Join
	 * Goal: Test Binary Stream Join of a stream with range window based relation with correlation predicate
	 * @author sbishnoi
	 *
	 */
	public void testBinStreamJoin4() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 char(20))",
				"register stream S2(c1 integer, c2 char(20))",
				"create query q1 as RStream(select * from S1[now] as Rel1, S2[Range 4 seconds] as Rel2 where Rel1.c1 = Rel2.c1)", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinStreamJoin4_S1");
		testMetadata.addSourceMetadata("S2", "inpBinStreamJoin4_S2");
		testMetadata.addDestinationMetadata("q1", "outBinStreamJoin4_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	
	/**
	 * Test Case for Binary Stream Join
	 * Goal: Test Binary Stream Join of a stream with range window based relation with basic data types covered
	 * @author fifang
	 *
	 */
	public void testBinStreamJoinRange() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer, c2 float, c3 double, c4 char(20), c5 boolean)",
				"register stream S2(c1 integer, c2 timestamp, c3 bigint)",
				"create query q1 as RStream(select * from S1[now], S2[Range 4 seconds])", };

		String[] tearDownDDLs = new String[] { "drop query q1",
				"drop stream S1", "drop stream S2" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpBinStreamJoinRange_S1");
		testMetadata.addSourceMetadata("S2", "inpBinStreamJoinRange_S2");
		testMetadata.addDestinationMetadata("q1", "outBinStreamJoinRange_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}
	
	     /**
         * Test Case for Binary Stream Join
         * Goal: Test Binary Stream Join of a stream with row window based relation with correlation predicate
         * @author sbishnoi
         *
         */
         public void testBinStreamJoin5() throws Exception 
         {
 
           String testSchema = new Object() {
             }.getClass().getEnclosingMethod().getName();

           String[] setupDDLs = new String[] {
            "register stream S1(c1 integer, c2 char(20))",
            "register stream S2(c1 integer, c2 char(20))",
            "create query q1 as RStream(select * from S1[now] as Rel1, S2[rows 10] as Rel2 where Rel1.c1 = Rel2.c1)", };

           String[] tearDownDDLs = new String[] { "drop query q1",
             "drop stream S1", "drop stream S2" };
           TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
           testMetadata.addSourceMetadata("S1", "inpBinStreamJoin5_S1");
           testMetadata.addSourceMetadata("S2", "inpBinStreamJoin5_S2");
           testMetadata.addDestinationMetadata("q1", "outBinStreamJoin5_q1");
           runFullSnapshotTest(testMetadata, testSchema);
         }
         
         /**
          * Test Case for Binary Stream Join
          * Goal: Test Binary Stream Join of a stream with row window based relation with correlation predicate
          * @author sbishnoi
          *
          */
          public void testBinStreamJoin6() throws Exception 
          {
  
            String testSchema = new Object() {
              }.getClass().getEnclosingMethod().getName();

            String[] setupDDLs = new String[] {
             "register stream S1(c1 integer, c2 char(20))",
             "register stream S2(c1 integer, c2 char(20))",
             "create query q1 as RStream(select * from S1[now] as Rel1, S2[rows 10] as Rel2 where Rel1.c1 = Rel2.c1)", };

            String[] tearDownDDLs = new String[] { "drop query q1",
              "drop stream S1", "drop stream S2" };
            TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
            testMetadata.addSourceMetadata("S1", "inpBinStreamJoin6_S1");
            testMetadata.addSourceMetadata("S2", "inpBinStreamJoin6_S2");
            testMetadata.addDestinationMetadata("q1", "outBinStreamJoin6_q1");
            runFullSnapshotTest(testMetadata, testSchema);
          }

}
