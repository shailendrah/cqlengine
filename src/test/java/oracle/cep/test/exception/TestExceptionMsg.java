package oracle.cep.test.exception;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.util.I18NUtil;
import junit.framework.TestCase;

public class TestExceptionMsg extends TestCase
{
    public TestExceptionMsg(String name)
    {
        super(name);
    }

    public void setUp()
    {
      //We need to turn off fall back behavior once Localizer call available for ExecutionError.
  	  I18NUtil.USE_FALLBACK_TO_DESC = true;
    }

    public void tearDown()
    {
  	  I18NUtil.USE_FALLBACK_TO_DESC = true;
    }
    
    public void testMsg()
    {
    	  CEPException ex = new CEPException(ExecutionError.PRECISION_ERROR, "345234551345345254634", 13);
    	  String msg = ex.getMessage();
          String action = ex.getAction();
          String cause = ex.getCauseMessage();
    	  String emsg = "value (345234551345345254634) larger than specified precision (13) allowed for this column";
    	  String eaction = "Enter a value that complies with the numeric columns precision, or use the MODIFY option with the ALTER TABLE command to expand the precision.";
    	  String ecause = "When inserting or updating records, a numeric value was entered that exceeded the precision defined for the column.";
          //System.out.println("Message: " + ex.getMessage());
          //System.out.println("Action: " + ex.getAction());
          //System.out.println("Cause: " + ex.getCauseMessage());
          assertEquals("got:"+msg+"\nexpected:"+emsg, msg, emsg);
          assertEquals("got:"+action+"\nexpected:"+eaction, action, eaction);
          assertEquals("got:"+cause+"\nexpected:"+ecause, cause, ecause);
    }
}
