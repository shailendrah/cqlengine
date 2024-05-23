package oracle.cep.test.dataStructures;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.test.storage.TestStorageBase;


public class TestStoredSinglyListTuple extends TestSinglyListTuple
{
  public TestStoredSinglyListTuple()
  {
    super();
  }

  protected void setUpSys()
  {
    TestStorageBase.setUpStorage(true);

  }

  public static void main(String[] args)
  {
      junit.textui.TestRunner.run(TestStoredSinglyListTuple.class);
  }
}
