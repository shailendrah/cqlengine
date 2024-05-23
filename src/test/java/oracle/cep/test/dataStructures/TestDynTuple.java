package oracle.cep.test.dataStructures;

import java.util.Random;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.factory.memory.TuplePtrFactory;
import oracle.cep.service.CEPManager;
import oracle.cep.test.dataStructures.TestTuple.TupleItem;

public class TestDynTuple extends TestTuple
{
  Random m_rand = new Random(123456); 

  protected IAllocator<ITuplePtr> getTupleFactory(TupleSpec spec)
  {
    CEPManager cepMgr = CEPManager.getInstance();
    TuplePtrFactory fac =new TuplePtrFactory(cepMgr, spec, 99);
    fac.useDynamicTupleClass(true);
    return fac;
  }

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestDynTuple.class);
  }
}
