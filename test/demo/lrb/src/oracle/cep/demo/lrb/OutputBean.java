package oracle.cep.demo.lrb;

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
     if (k == QueueElement.Kind.E_PLUS)
     {
        DemoServlet.getInstance().add(eprArgs[0], tv);
     } else if (k == QueueElement.Kind.E_MINUS)
     {
       DemoServlet.getInstance().remove(eprArgs[0], tv);
     }
   }

  public void end() throws CEPException
  {
  }

  public void start() throws CEPException
  {
  }
}
