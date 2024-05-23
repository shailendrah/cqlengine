package oracle.cep.demo.csfb;

import java.util.List;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import com.oracle.osa.exceptions.CEPException;
import oracle.cep.interfaces.output.QueryOutputBase;

public class OutputBean extends QueryOutputBase
{
   public void putNext(TupleValue tv, QueueElement.Kind k) 
     throws CEPException
   {
     assert eprArgs.length == 1 : eprArgs.length;
     
     System.out.println(tv.toSimpleString());
     if (eprArgs[0].equals("pr3")) 
     {
       if (k == QueueElement.Kind.E_PLUS)
       {
          try
          {
            char[] vs = tv.cValueGet(0);
            int vl = tv.cLengthGet(0);
            String symbol = new String(vs, 0, vl);
            int count = tv.iValueGet(1);
            System.out.println(symbol + " , " + count);
            DemoServlet.getInstance().addSymbol(symbol, count);
          } catch(CEPException e)
          {
            System.out.println(e.toString());
          }
       } else if (k == QueueElement.Kind.E_MINUS)
       {
       }
      }
   }

  public void end() throws CEPException
  {
  }

  public void start() throws CEPException
  {
  }
}
