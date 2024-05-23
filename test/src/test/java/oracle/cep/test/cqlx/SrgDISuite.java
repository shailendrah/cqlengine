package oracle.cep.test.cqlx;


import junit.framework.TestSuite;
import oracle.cep.test.cqlxframework.AbsCqlxTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class SrgDISuite extends AbsCqlxTestSuite {
    
    public static TestSuite suite()
    {
      SrgDISuite s = new SrgDISuite();
      return s.generateSuite();
    }
    
    public SrgDISuite()
    {
        super("srg_di", "srg_di.xml");
    }
}
