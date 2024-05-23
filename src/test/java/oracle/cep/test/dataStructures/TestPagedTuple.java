package oracle.cep.test.dataStructures;

import java.util.Random;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.factory.paged.TupleFactory;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;

public class TestPagedTuple extends TestTuple
{
  private static boolean DO_ALLOCTEST = false;
  private static final int MAXP  = 400;
  private static final int SUBP  = 20;
  private static final int NPASS = 25;
  private static final int NLOOP = 12;
  Random m_rand = new Random(123456); 

  protected IAllocator<ITuplePtr> getTupleFactory(TupleSpec spec)
  {
    CEPManager cepMgr = CEPManager.getInstance();
    ConfigManager cm = cepMgr.getConfigMgr();
    int initPageTableSize = cm.getTupleInitPageTableSize();
    TupleFactory fac = new TupleFactory(cepMgr, spec, 99, initPageTableSize);
    fac.setInitialPages(120);
    return fac;
  }

  protected void printStat()
  {
    TupleFactory fac = (TupleFactory) m_tupleFactory;
    System.out.println(fac.toString());
  }

  protected void resetFac() throws CEPException
  {
    TupleFactory fac = (TupleFactory) m_tupleFactory;
    fac.reset();
  }

  private TupleItem alloc() throws CEPException
  {
    TupleItem item = new TupleItem();
    item.m_ref = m_tupleFactory.allocate();
    int j = m_rand.nextInt(1000);
    item.fill(j);
    return item;
  }
  
  public void testAllocFree() throws CEPException
  {
    if (!DO_ALLOCTEST) return;
    System.out.print("testing alloc/free " + NLOOP + " loops");
    TupleFactory fac = (TupleFactory) m_tupleFactory;
    fac.reset();
    
    TupleItem items[] = new TupleItem[MAXP];
    for (int loop = 0; loop < NLOOP; loop++)
    {
      System.out.println("loop " + loop);
      for (int pass = 0; pass < NPASS; pass++)
      {
        for (int subpass = 0; subpass < SUBP; subpass++)
        {
          int alloc = 0;
          int free = 0;
          for (int i = 0; i < MAXP; i++)
          {
            int r = m_rand.nextInt();
            if  ( (r & 8) != 0)
            {
              if (items[i] != null)
              {
              //REMOVE_REFCNT m_tupleFactory.release(items[i].m_ref);
                items[i] = null;
                free++;
              } 
              else 
              {
                items[i] = alloc();
                alloc++;
              }
            }
          }
          //System.out.println("alloc = " + alloc + " free = " + free);
        }
        for (int i = 0; i < MAXP; i++)
        {
          if (items[i] != null)
          {
            items[i].verify(true);
          }
        }
      }
      printStat();
    }
    /* //REMOVE_REFCNT 
    for (int i = 0; i < MAXP; i++)
    {
      if (items[i] != null)
      {
        m_tupleFactory.release(items[i].m_ref);
      }
    }
    */
    printStat();
  }
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestPagedTuple.class);
  }
}
