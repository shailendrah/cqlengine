package oracle.cep.test.ha;

public class HATestView extends BaseCQLTestCase {
	public HATestView() throws Exception {
		super();
	}

	public void testViewRelSource() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register relation R1(c1 integer)",
				"register view v1(c1 integer, c2 integer) as select c1, count(c1) as c2 from R1 group by c1 having (count(c1) > 0)",
				"register view v2(c1 integer, c2 integer) as select c1, sum(c2) from v1 group by c1 ",
				// this query was supposed to invoke the view relation source Operator.
				// but the ViewRelnSrc operator class looks obsolete and it's invoking
				// Relation source operator instead.(ref- PhyPlanGen.genPhysicalPlan ->
				// LogPlanInterpreterFactory.getInterpreter -> PhyOptRelnSrcFactory.getNewPhyOpt)
				"create query q1 as select * from v2" };

		String[] tearDownDDLs = new String[] { "drop query q1", "drop view v2",
				"drop view v1", "drop relation R1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("R1", "inpViewRelSource_R1");
		testMetadata.addSourceType("R1", SourceType.RELATION);
		testMetadata.addDestinationMetadata("q1", "outViewRelSource_q1");
		runFullSnapshotTest(testMetadata, testSchema);
	}

	public void testViewProject2() throws Exception {

	  String testSchema = new Object() {
		}.getClass().getEnclosingMethod().getName();

		String[] setupDDLs = new String[] {
				"register stream S1(c1 integer)",
				"register view v1(c1 integer, c2 integer) as select c1, count(c1) as c2 from S1 group by c1 having (count(c1) > 0)",
				"register view v2(c1 integer, c2 integer) as select c1, sum(c2) from v1 group by c1 ",
				// the query invokes Stream Source operator only. see the comments in ITestViewRelnSrc operator.
				// TODO: we need to find the query, if any, to invoke theViewStrmSrc operator.
				"create query q1 as select * from v2" };

		String[] tearDownDDLs = new String[] { "drop query q1", "drop view v2",
				"drop view v1", "drop stream S1" };
		TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
		testMetadata.addSourceMetadata("S1", "inpViewStrmSrc_S1");
		testMetadata.addSourceType("S1", SourceType.STREAM);
		testMetadata.addDestinationMetadata("q1", "outViewStrmSrc_q1");

		runFullSnapshotTest(testMetadata, testSchema);
	}

}
