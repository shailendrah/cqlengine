package oracle.cep.test.dataStructures;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.factory.memory.TuplePtrFactory;
import oracle.cep.service.CEPManager;
import oracle.cep.test.InterpDrv;

public class TestTuple extends TestCase
{
  static final int      ITERATION = 10000;
  static int fSAMPLE_SIZE = 10000;
  static long fSLEEP_INTERVAL = 100;
  static final int      VCHAR_LEN = 20;
  static final int MAX_ATTRS_NUMBER = 50;
  
  protected TupleSpec             m_spec;
  protected IAllocator<ITuplePtr> m_tupleFactory;
  protected List<TupleItem>       m_items;
  protected ITuplePtr             m_temp;
  protected boolean               m_doEvicts;
  
  public class TupleItem
  {
    ITuplePtr    m_ref;
    int       m_ival[];
    long      m_lval[];
    float     m_fval[];
    double    m_dval[];
    byte[]    m_bv[];
    int       m_bvlen[];
    char[]    m_cv[];
    int       m_cvlen[];
    long      m_tval[];
    long      m_vval[];
    long      m_vymval[];
    Object    m_ov[];
    BigDecimal m_nval[];
    
    public TupleItem()
    {
      m_ival = new int[MAX_ATTRS_NUMBER];
      m_lval = new long[MAX_ATTRS_NUMBER];
      m_fval = new float[MAX_ATTRS_NUMBER];
      m_dval = new double[MAX_ATTRS_NUMBER];
      m_bv = new byte[MAX_ATTRS_NUMBER][];
      m_bvlen = new int[MAX_ATTRS_NUMBER];
      m_cv = new char[MAX_ATTRS_NUMBER][];
      m_cvlen = new int[MAX_ATTRS_NUMBER];
      m_tval = new long[MAX_ATTRS_NUMBER];
      m_vval = new long[MAX_ATTRS_NUMBER];
      m_vymval = new long[MAX_ATTRS_NUMBER];
      m_ov = new Object[MAX_ATTRS_NUMBER];
      m_nval = new BigDecimal[MAX_ATTRS_NUMBER];
    }
    
    public void fill(int j) throws CEPException
    {
      int ipos = 0;
      int lpos = 0;
      int fpos = 0;
      int dpos = 0;
      int bvpos = 0;
      int cvpos = 0;
      int tpos = 0;
      int vpos = 0;
      int opos = 0;
      int npos = 0;
    //REMOVE_TUPLEPIN ITuplePtr tr = m_ref.pin(Pinmode.WRITE);
      ITuple tuple = m_ref.pinTuple(IPinnable.WRITE);
      int numAttrs = m_spec.getNumAttrs();
      for (int i = 0; i < numAttrs; i++)
      {
	  switch (m_spec.getAttrType(i).getKind())
        {
          case INT:
            int ival = (int) 500 * (ipos + 1) + j;
            tuple.iValueSet(i, ival);
            m_ival[ipos++] = ival;
            break;
            
          case BIGINT:
            long lval = 100000 * (lpos + 1)  + j;
            tuple.lValueSet(i, lval);
            m_lval[lpos++] = lval;
            break;
            
          case FLOAT:
            float fval = 0.678f * (fpos + 1) + j;
            tuple.fValueSet(i, fval);
            m_fval[fpos++] = fval;
            break;
            
          case DOUBLE:
            double dval = 0.94586 * (dpos + 1) + j;
            tuple.dValueSet(i, dval);
            m_dval[dpos++] = dval;
            break;
            
          case CHAR:
            int cvlen = VCHAR_LEN - 10 + ( j % 5);
            char[] cv = new char[cvlen+1];
            char c = 'A';
            cv[0] = (char) (c + (j & 0xff));
            cv[1] = (char) (c + ((j >> 8) & 0xff));
            cv[2] = (char) (c + ((j >> 16) & 0xff));
            cv[3] = (char) (c + ((j >> 24) & 0xff));
            for (int k = 4; k < cvlen; k++) 
            {
                cv[k] = c;
                c++;
                if (c == 'z') c = 'A';
            }
            tuple.cValueSet(i, cv, cvlen);
            m_cv[cvpos] = cv;
            m_cvlen[cvpos] = cvlen;
            cvpos++;
            break;
          case BYTE:
            int bvlen = VCHAR_LEN - ( j % 10);
            byte[] bv = new byte[bvlen+1];
            bv[0] = (byte) (j & 0xff);
            bv[1] = (byte) ((j >> 8) & 0xff);
            bv[2] = (byte) ((j >> 16) & 0xff);
            bv[3] = (byte) ((j >> 24) & 0xff);
            for (int k = 4; k < bvlen; k++) 
            {
                bv[k] = (byte) k;
            }
            tuple.bValueSet(i, bv, bvlen);
            m_bv[bvpos] = bv;
            m_bvlen[bvpos] = bvlen;
            bvpos++;
            break;
          case TIMESTAMP:
            long tval = 9900* (tpos + 1)  + j;
            tuple.tValueSet(i, new Timestamp(tval));
            m_tval[tpos++] = tval;
            break;
          case INTERVAL:
            long vval = 3000* (vpos + 1)  + j;
            IntervalFormat defaultFormat = null;
            try
            {
              defaultFormat = new IntervalFormat(TimeUnit.DAY,
                                                 TimeUnit.SECOND,
                                                 9,
                                                 9);
            } 
            catch (CEPException e)
            {
              // Unreachable as interval format is constructed with valid params
              assert false;
            }
            tuple.vValueSet(i, vval, defaultFormat);
            m_vval[vpos++] = vval;
            break;
          case INTERVALYM:
            long vymval = 3000* (vpos + 1)  + j;
            defaultFormat = null;
            try
            {
              defaultFormat = new IntervalFormat(TimeUnit.YEAR,
                                                 TimeUnit.MONTH,
                                                 9,
                                                 true);
            } 
            catch (CEPException e)
            {
              // Unreachable as interval format is constructed with valid params
              assert false;
            }
            tuple.vymValueSet(i, vymval, defaultFormat);
            m_vymval[vpos++] = vymval;
            break;
          case OBJECT:
            String sv = "Hello" + Long.toString(j);
            tuple.oValueSet(i, sv);
            m_ov[opos++] = sv;
            break;
        case BIGDECIMAL:
            dval = 0.94586 * (npos + 1) + j;
            int precision = 2;
            int scale = 5;
            BigDecimal nval = new BigDecimal(Double.toString(dval), 
                                   new MathContext(precision)
                                   ).setScale(scale, RoundingMode.HALF_UP);

            tuple.nValueSet(i, nval, precision, scale);
            m_nval[npos++] = nval;
            break;

          default:
            assert false;
        }
      }
      
      // set values

    //REMOVE_TUPLEPIN m_ref.unpinTuple();
    //REMOVE_TUPLEPIN m_ref.unpin();
      
      //verify(true);
    }
    
    public void verify(boolean verify) throws CEPException
    {
      int ipos = 0;
      int lpos = 0;
      int fpos = 0;
      int dpos = 0;
      int bvpos = 0;
      int cvpos = 0;
      int tpos = 0;
      int vpos = 0;
      int opos = 0;
      int npos = 0;
      
    //REMOVE_TUPLEPIN ITuplePtr tr = m_ref.pin(Pinmode.READ);
      ITuple tuple = m_ref.pinTuple(IPinnable.READ);
      int numAttrs = m_spec.getNumAttrs();
      for (int i = 0; i < numAttrs; i++)
      {
	  switch (m_spec.getAttrType(i).getKind())
        {
          case INT:
            int ival1 = tuple.iValueGet(i);
            assertEquals(m_ival[ipos++], ival1);
            break;
            
          case BIGINT:
            long lval1 = tuple.lValueGet(1);
            if (verify)
            {
              assertEquals(m_lval[lpos++], lval1);
            }
            break;
            
          case FLOAT:
            float fval1 = tuple.fValueGet(i);
            if (verify)
            {
              assertEquals(m_fval[fpos++], fval1);
            }
            break;
            
          case DOUBLE:
            double dval1 = tuple.dValueGet(i);
            if (verify)
            {
              assertEquals(m_dval[dpos++], dval1);
            }
            break;

          case CHAR:
            char[] cv1 = tuple.cValueGet(i);
            
            if (verify)
            {
              char[] cv0 = m_cv[cvpos];
              int count = 0;
              for (int k = 0; k < m_cvlen[cvpos]; k++)
              {
                if (cv0[k] == cv1[k])
                  count++;
              }
              assertEquals(m_cvlen[cvpos], count);
            }
            cvpos++;
            break;
          case BYTE:
            byte[] bv1 = tuple.bValueGet(i);
            
            if (verify)
            {
              byte[] bv0 = m_bv[bvpos];
              int count = 0;
              for (int k = 0; k < m_bvlen[bvpos]; k++)
              {
                if (bv0[k] == bv1[k])
                  count++;
              }
              assertEquals(m_bvlen[bvpos], count);
            }
            bvpos++;
            break;
          case TIMESTAMP:
            long tval1 = tuple.tValueGet(i);
            if (verify)
            {
              assertEquals(m_tval[tpos++], tval1);
            }
            break;
          case INTERVAL:
            long vval1 = tuple.vValueGet(i);
            if (verify)
            {
              assertEquals(m_vval[vpos++], vval1);
            }
            break;
          case INTERVALYM:
            long vymval1 = tuple.vymValueGet(i);
            if (verify)
            {
              assertEquals(m_vymval[vpos++], vymval1);
            }
            break;
          case OBJECT:
            Object o1 = tuple.oValueGet(i);
            if (verify)
            {
              String sv0 = (String) m_ov[opos++];
              String sv1 = (String) o1;
              assertEquals(sv0, sv1);
            }
            break;
          case BIGDECIMAL:
            BigDecimal nval1 = tuple.nValueGet(1);
            if (verify)
            {
              assertEquals(m_nval[npos++], nval1);
            }
            break;
          default:
            assert false;
        }
      }
    //REMOVE_TUPLEPIN m_ref.unpinTuple();
    //REMOVE_TUPLEPIN m_ref.unpin();
    }
  }

  public TestTuple()
  {
    m_spec = null;
  }
  
  /**
   * Sets up the test fixture. (Called before every test case method.)
   */
  public void setUp()
  {
    try 
    {
      InterpDrv.getInstance().setUp(null);
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
  }

  /**
   * Tears down the test fixture. (Called after every test case method.)
   */
  public void tearDown()
  {
  }

  public long memoryUsage()
  {
    Runtime runtime = Runtime.getRuntime();
    runtime.runFinalization();
    runtime.gc();
    runtime.gc();
    runtime.gc();
    runtime.gc();
    runtime.gc();
    MemoryMXBean mmxb = ManagementFactory.getMemoryMXBean();
    MemoryUsage musage = mmxb.getHeapMemoryUsage();
    return musage.getUsed();
  }

  protected IAllocator<ITuplePtr> getTupleFactory(TupleSpec spec)
  {
    CEPManager cepMgr = CEPManager.getInstance();
    return new TuplePtrFactory(cepMgr, spec, 99);
  }

  private static long getMemoryUse(){
    putOutTheGarbage();
    long totalMemory = Runtime.getRuntime().totalMemory();

    putOutTheGarbage();
    long freeMemory = Runtime.getRuntime().freeMemory();

    return (totalMemory - freeMemory);
  }

  private static void putOutTheGarbage() 
  {
    for (int i = 0; i < 5; i++) 
    {
      collectGarbage();
      collectGarbage();
    }
  }

  private static void collectGarbage() {
    try {
      System.gc();
      Thread.currentThread().sleep(fSLEEP_INTERVAL);
      System.runFinalization();
      Thread.currentThread().sleep(fSLEEP_INTERVAL);
    }
    catch (InterruptedException ex){
      ex.printStackTrace();
    }
  }
  
  
  public void tupleTest(boolean verify) throws CEPException
  {
    m_items = new LinkedList<TupleItem>();
    for (int j = 0; j < ITERATION; j++)
    {
      //if ((j % 1000) == 0) System.out.println(j);
      TupleItem item = new TupleItem();
      m_items.add(item);
      item.m_ref = m_tupleFactory.allocate();
      item.fill(j);
    }
    for (int j = 0; j < ITERATION; j++)
    {
      //if ((j % 1000) == 0) System.out.println(j);
      TupleItem item = m_items.get(j);
      item.verify(verify);
    }
    if (m_doEvicts)
    {
      for (int j = 0; j < ITERATION; j++)
      {
        //if ((j % 1000) == 0) System.out.println(j);
        TupleItem item = m_items.get(j);
        ITuplePtr ptr = item.m_ref;
        ptr.evict();
      }
      for (int j = 0; j < ITERATION; j++)
      {
        //if ((j % 1000) == 0) System.out.println(j);
        TupleItem item = m_items.get(j);
        item.verify(verify);
      }
    }
  }

  protected void printStat()
  {
  }

  protected void resetFac() throws CEPException
  {
  }
  
  public void timeTest() throws CEPException
  {
    long mem0 = memoryUsage();
    long start = System.currentTimeMillis();
    tupleTest(false);
    long end = System.currentTimeMillis();
    long tdiff = (end - start);
    double throughput = ((double) ITERATION / tdiff) * 1000;
    System.out
        .println(ITERATION + " iterations took approximately: " + tdiff + " ms");
    System.out.println("The throughput is approximately: " + throughput
        + " calls / s");
    long mem1 = memoryUsage();
    long memuse = mem1 - mem0;
    System.out.println("Memory Usage: " + memuse);
  }

  public void sizeTest() throws CEPException
  {
    resetFac();
    ITuplePtr[] objects = new ITuplePtr[fSAMPLE_SIZE];

    //build a bunch of identical objects
    try {
      long startMemoryUse = getMemoryUse();
      for (int idx=0; idx < objects.length ; ++idx) {
        objects[idx] = m_tupleFactory.allocate();
      }
      long endMemoryUse = getMemoryUse();
      long memuse = endMemoryUse - startMemoryUse;
      float approximateSize = ( memuse ) /((float) fSAMPLE_SIZE);
      long result = Math.round( approximateSize );
      System.out.println(fSAMPLE_SIZE + " tuples, Memory Usage: " + memuse);
      System.out.println("Tuple size: " + result);
      printStat();
    }
    catch (Exception ex) {
      System.err.println("Cannot create object" + ex);
    }
  }

  public void verifyTest() throws CEPException
  {
    tupleTest(true);
  }
  
  // mixed attributes
  public void test1() throws CEPException
  {
    System.out.println("----- Testing all Attributes");
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    TupleSpec spec = new TupleSpec(factoryMgr.getNextId());
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.BIGINT);
    spec.addAttr(Datatype.FLOAT);
    spec.addAttr(Datatype.DOUBLE);
    spec.addAttr(new AttributeMetadata(Datatype.BYTE, VCHAR_LEN, 0, 0));
    spec.addAttr(new AttributeMetadata(Datatype.CHAR, VCHAR_LEN, 0, 0));
    spec.addAttr(new AttributeMetadata(Datatype.BIGDECIMAL, 0, 
                   Datatype.BIGDECIMAL.getPrecision(), 0));
    spec.addAttr(Datatype.TIMESTAMP);
    spec.addAttr(Datatype.OBJECT);
    spec.addAttr(Datatype.INTERVAL);
    m_spec = spec;
    m_tupleFactory = getTupleFactory(m_spec);
    m_temp = m_tupleFactory.allocate();

    timeTest();
    sizeTest();
    verifyTest();
  }
  
  // 6 int : lin road
  public void test2() throws CEPException
  {
    System.out.println("----- Testing 6 ints");
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    TupleSpec spec = new TupleSpec(factoryMgr.getNextId());
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.INT);
    m_spec = spec;
    m_tupleFactory = getTupleFactory(m_spec);
    m_temp = m_tupleFactory.allocate();

    timeTest();
    sizeTest();
    verifyTest();
  }

  // max int
  public void test3() throws CEPException
  {
    System.out.println("------ Testing max int attrs");
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    TupleSpec spec = new TupleSpec(factoryMgr.getNextId());
    for (int i = 0; i < Constants.INITIAL_ATTRS_NUMBER; i++)
    {
      spec.addAttr(Datatype.INT);
    }
    m_spec = spec;
    m_tupleFactory = getTupleFactory(m_spec);
    m_temp = m_tupleFactory.allocate();

    timeTest();
    sizeTest();
    verifyTest();
  }

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestTuple.class);
  }
}
