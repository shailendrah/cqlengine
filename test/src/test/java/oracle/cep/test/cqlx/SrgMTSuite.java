package oracle.cep.test.cqlx;


import junit.framework.TestSuite;
import oracle.cep.test.cqlxframework.AbsCqlxTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class SrgMTSuite extends AbsCqlxTestSuite {
    
    public static TestSuite suite()
    {
      SrgMTSuite s = new SrgMTSuite();
      return s.generateSuite();
    }
    
    public SrgMTSuite()
    {
        super("srg_mt", "srg_mt.xml");
    }
}
