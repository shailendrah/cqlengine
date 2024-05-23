/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/storage/TestStorageBase.java /main/22 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/02/11 - fixing tests after datastructure changes
    hopark      04/03/11 - refactor storage
    sborah      10/15/09 - support for bigdecimal
    hopark      10/10/08 - remove statics
    hopark      09/15/08 - add schema testing
    hopark      08/22/08 - allow change storage
    hopark      03/18/08 - reorg config
    hopark      03/17/08 - set storage folder
    hopark      02/04/08 - support double
    hopark      12/06/07 - cleanup spill
    hopark      10/31/07 - add properties
    hopark      06/20/07 - cleanup
    hopark      04/05/07 - 
    parujain    03/22/07 - handle exception
    hopark      03/21/07 - use ITuple
    najain      03/15/07 - cleanup
    najain      03/12/07 - bug fix
    najain      03/08/07 - cleanup
    hopark      01/10/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/storage/TestStorageBase.java /main/20 2009/11/09 10:10:59 sborah Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.storage;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import junit.framework.TestCase;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.StorageManager;
import oracle.cep.test.InterpDrv;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/storage/TestStorageBase.java /main/20 2009/11/09 10:10:59 sborah Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public abstract class TestStorageBase extends TestCase
{
  protected int m_storageId;
  protected IStorage m_storage;
  protected IStorage m_metastorage;
  protected Helper m_helper;

  protected static final String TEST_TWORK_PROPERTY = "twork";
  private static boolean s_storageInit = false;
  private static String s_storageFolder = null;
  protected static InterpDrv s_interpDrv = null;
  
  public static class ByteArray
  {
    byte[] m_c;
    ByteArray(byte[] c)
    {
      m_c  = c;
    }
    public boolean equals(Object o)
    {
      if (!(o instanceof ByteArray)) return false;
      ByteArray other = (ByteArray) o;
      return compareBytes(m_c, other.m_c);
    }

    public static boolean compareBytes(byte[] src, byte[] dest)
    {
      if (src == null || dest == null)
        return false;
      if (src.length != dest.length)
        return false;
      for (int i = 0; i < src.length; i++)
      {
        if (src[i] != dest[i])
          return false;
      }
      return true;
    }

  }
  
  public static class CharArray
  {
    char[] m_c;
    CharArray(char[] c)
    {
      m_c = c;
    }
    public boolean equals(Object o)
    {
      if (!(o instanceof CharArray)) return false;
      CharArray other = (CharArray) o;
      return compareChars(m_c, other.m_c);
    }

    public static boolean compareChars(char[] src, char[] dest)
    {
      if (src == null || dest == null)
        return (src == dest);
      if (src.length != dest.length)
        return false;
      for (int i = 0; i < src.length; i++)
      {
        if (src[i] != dest[i])
          return false;
      }
      return true;
    }
  }
  public static class ObjContent extends HashMap<String,Object>
  {
    ObjContent()
    {
      super();
    }
    
    public void add(String n, Object v)
    {
      this.put(n, v);
    }
    
    public boolean equals(Object other)
    {
      if (!(other instanceof ObjContent)) return false;
      ObjContent ol = (ObjContent) other;
      if (ol.size() != size()) return false;
      Set<String> okeys = ol.keySet();
      for (String key : okeys)
      {
        Object ov = ol.get(key);
        Object v = get(key);
        boolean b = true;
        if (ov == null || v == null) b = false;
        if (!ov.equals(v)) b = false;
        if (!b) 
        {
          System.out.println(key+ " "+ov + ":" + v);
          return false;
        }
      }
      return true;
    }
  }

  public static class Helper
  {
    protected TupleSpec specs[];
    private static Helper s_inst = null;

    public static Helper getInstance()
    {
      if (s_inst == null)
      {
        s_inst = new Helper();
      }
      return s_inst;
    }

    private Helper()
    {
      try
      {
        specs = new TupleSpec[2];
        CEPManager cepMgr = CEPManager.getInstance();
        ConfigManager cfg = new ConfigManager();
        cepMgr.setConfig(cfg);
        try {
			cepMgr.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        String path = cfg.getStorageFolder();
	    try
	    {
	      File f = new File(path);
	      f.mkdirs();
	    }
	    catch(Exception e)
	    {
	      System.out.println("Failed to create "+ path + "\n" + e.toString());
	    }

        FactoryManager factoryMgr = cepMgr.getFactoryManager();
        specs[0] = new TupleSpec(factoryMgr.getNextId(), 2);
        specs[0].addAttr(0, new AttributeMetadata(Datatype.INT, 2, Datatype.INT.getPrecision(), 0));
        specs[0].addAttr(1, new AttributeMetadata(Datatype.CHAR, 30, Datatype.CHAR.getPrecision(), 0));

        specs[1] = new TupleSpec(factoryMgr.getNextId(), 3);
        specs[1].addAttr(0, new AttributeMetadata(Datatype.INT, 2, Datatype.INT.getPrecision(), 0));
        specs[1].addAttr(1, new AttributeMetadata(Datatype.CHAR, 30, 0, 0));
        specs[1].addAttr(2, new AttributeMetadata(Datatype.OBJECT, 4, 0, 0));

      } catch (CEPException e)
      {
        System.out.println(e.toString());
      }
    }


    public void compareObj(Object a, Object b, String[] ignore)
    {
      Class aclass = a.getClass();
      Class bclass = b.getClass();
      boolean t = aclass.equals(bclass);
      assertTrue(t);
      Field fields[] = aclass.getDeclaredFields();
      for (int i = 0; i < fields.length; ++i) 
      {
        Field f = fields[i];
        if (ignore != null)
        {
          boolean iflag= false;
          for (String name : ignore)
          {
            if (f.getName().equals(name)) 
            {
              iflag = true;
              break;
            }
          }
          if (iflag) continue;
        }
        f.setAccessible(true);
        try 
        {
          Object afv = f.get(a);
          Object bfv = f.get(b);
          if (afv == null || bfv == null)
          {
            t = (afv == bfv);
          } else {
            t = afv.equals(bfv);
          }
          if (!t) System.out.println(aclass.getName() + "." + f.getName() + " " + afv + ":" + bfv);
          assertTrue(t);
        } catch(IllegalAccessException e)
        {
          
        }
      }
    }
    
    public ObjContent getContent(int specid, ITuple tuple1) throws CEPException
    {
        ObjContent res = new ObjContent();
        TupleSpec tupleSpec = specs[specid];
        int num = tupleSpec.getNumAttrs();
        res.add("AttrNum", new Integer(num));
        // Allocate attributes
        int ival1, ival2;
        long lval1, lval2;
        int len1, len2;
        byte[] bval1, bval2;
        char[] cval1, cval2;
        float fval1, fval2;
        double dval1, dval2;
        Object oval1, oval2;
        for (int i = 0; i < num; i++)
        {
	    switch (tupleSpec.getAttrType(i).getKind())
          {
          case INT:
            ival1 = tuple1.iValueGet(i);
            res.add("INT_"+i, new Integer(ival1));
            break;
          case BIGINT:
            lval1 = tuple1.lValueGet(i);
            res.add("BIGINT_"+i, new Long(lval1));
            break;
          case FLOAT:
            fval1 = tuple1.fValueGet(i);
            res.add("FLOAT_"+i, new Float(fval1));
            break;
          case DOUBLE:
            dval1 = tuple1.dValueGet(i);
            res.add("DOUBLE_"+i, new Double(dval1));
            break;
          case CHAR:
            len1 = tuple1.isAttrNull(i) ? 0 : tuple1.cLengthGet(i);
            res.add("CHARLEN_"+i, new Integer(len1));
            cval1 = tuple1.isAttrNull(i) ? null : tuple1.cValueGet(i);
            res.add("CHAR_"+i, new CharArray(cval1));
            break;
          case BYTE:
            len1 = tuple1.isAttrNull(i) ? 0 : tuple1.bLengthGet(i);
            res.add("BYTELEN_"+i, new Integer(len1));
            bval1 = tuple1.isAttrNull(i) ? null : tuple1.bValueGet(i);
            res.add("BYTELEN_"+i, new ByteArray(bval1));
            break;
          case OBJECT:
            oval1 = tuple1.oValueGet(i);
            res.add("OBJ_"+i, oval1);
            break;
          case TIMESTAMP:
            lval1 = tuple1.tValueGet(i);
            res.add("TS_"+i, new Long(lval1));
            break;
          case INTERVAL:
            lval1 = tuple1.vValueGet(i);
            res.add("INTERVAL_"+i, new Long(lval1));
            break;
          default:
            assert false;
          }
        }
        return res;
      }

    public void compareTuple(int specid, ITuplePtr tuple1Ptr,
                             ITuple tuple2) throws CEPException
    {
      ITuple tuple1 = (ITuple)tuple1Ptr.pinTuple(IPinnable.READ);
      compareTuple(specid, tuple1, tuple2);
    //REMOVE_TUPLEPIN tuple1Ptr.unpinTuple();
    }

    public void compareTuple(int specid, ITuple tuple1,
                             ITuple tuple2) throws CEPException
    {
      TupleSpec tupleSpec = specs[specid];
      int num = tupleSpec.getNumAttrs();
      // Allocate attributes
      int ival1, ival2;
      long lval1, lval2;
      int len1, len2;
      byte[] bval1, bval2;
      char[] cval1, cval2;
      float fval1, fval2;
      double dval1, dval2;
      Object oval1, oval2;
      for (int i = 0; i < num; i++)
      {
	  switch (tupleSpec.getAttrType(i).getKind())
        {
        case INT:
          ival1 = tuple1.iValueGet(i);
          ival2 = tuple2.iValueGet(i);
          assertEquals(ival1, ival2);
          break;
        case BIGINT:
          lval1 = tuple1.iValueGet(i);
          lval2 = tuple2.lValueGet(i);
          assertEquals(lval1, lval2);
          break;
        case FLOAT:
          fval1 = tuple1.fValueGet(i);
          fval2 = tuple2.fValueGet(i);
          assertEquals(fval1, fval2);
          break;
        case DOUBLE:
          dval1 = tuple1.dValueGet(i);
          dval2 = tuple2.dValueGet(i);
          assertEquals(dval1, dval2);
          break;
        case CHAR:

          len1 = tuple1.isAttrNull(i) ? 0: tuple1.cLengthGet(i);
          len2 = tuple2.isAttrNull(i) ? 0: tuple2.cLengthGet(i);
          assertEquals(len1, len2);
          cval1 = tuple1.isAttrNull(i) ? null: tuple1.cValueGet(i);
          cval2 = tuple2.isAttrNull(i) ? null: tuple2.cValueGet(i);
          assertTrue(CharArray.compareChars(cval1, cval2));
          break;
        case BYTE:
          len1 = tuple1.isAttrNull(i) ? 0: tuple1.bLengthGet(i);
          len2 = tuple2.isAttrNull(i) ? 0: tuple2.bLengthGet(i);
          assertEquals(len1, len2);
          bval1 = tuple1.isAttrNull(i) ? null: tuple1.bValueGet(i);
          bval2 = tuple2.isAttrNull(i) ? null: tuple2.bValueGet(i);
          assertTrue(ByteArray.compareBytes(bval1, bval2));
          break;
        case OBJECT:
          oval1 = tuple1.oValueGet(i);
          oval2 = tuple2.oValueGet(i);
          assertTrue(oval1.equals(oval2));
          break;
        case TIMESTAMP:
          lval1 = tuple1.tValueGet(i);
          lval2 = tuple2.tValueGet(i);
          assertEquals(lval1, lval2);
          break;
        case INTERVAL:
          lval1 = tuple1.vValueGet(i);
          lval2 = tuple2.vValueGet(i);
          assertEquals(lval1, lval2);
          break;
        default:
          assert false;
        }
      }
    }

    public IAllocator getTupleFactory(int specid)
    {
      CEPManager cepMgr = CEPManager.getInstance();
      FactoryManager factoryMgr = cepMgr.getFactoryManager();
      return factoryMgr.get(specs[specid]);
    }

    public ITuplePtr allocTuple(int specid) throws CEPException
    {
      CEPManager cepMgr = CEPManager.getInstance();
      FactoryManager factoryMgr = cepMgr.getFactoryManager();
      IAllocator fac1 = factoryMgr.get(specs[specid]);
      return (ITuplePtr)fac1.allocate();
    }

    public void setTuple(int specid, ITuplePtr tuplePtr,
                         Object... arguments) throws CEPException
    {
      int ival;
      long lval;
      byte[] bval;
      char[] cval;
      float fval;
      double dval;
      String s;

      ITuple tuple = (ITuple)tuplePtr.pinTuple(IPinnable.WRITE);
      int pos = 0;
      for (Object arg: arguments)
      {
	  switch (specs[specid].getAttrType(pos).getKind())
        {
        case INT:
          ival = ((Integer)arg).intValue();
          tuple.iValueSet(pos, ival);
          break;
        case BIGINT:
          lval = ((Long)arg).longValue();
          tuple.lValueSet(pos, lval);
          break;
        case FLOAT:
          fval = ((Float)arg).floatValue();
          tuple.fValueSet(pos, fval);
          break;
        case DOUBLE:
          dval = ((Double)arg).doubleValue();
          tuple.dValueSet(pos, dval);
          break;
        case CHAR:
          s = (String)arg;
          if (s == null)
          {
            tuple.cValueSet(pos, null, 0);
          } else {
            cval = new char[s.length()];
            s.getChars(0, s.length(), cval, 0);
            tuple.cValueSet(pos, cval, cval.length);
          }
          break;
        case BYTE:
          bval = (byte[])arg;
          tuple.bValueSet(pos, bval, bval.length);
          break;
        case OBJECT:
          tuple.oValueSet(pos, arg);
          break;
        case TIMESTAMP:
          lval = ((Long)arg).longValue();
          tuple.tValueSet(pos, lval);
          break;
        case INTERVAL:
          lval = ((Long)arg).longValue();
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
          tuple.vValueSet(pos, lval, defaultFormat);
          break;
        case INTERVALYM:
          lval = ((Long)arg).longValue();
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
          tuple.vymValueSet(pos, lval, defaultFormat);
          break;
        default:
          assert false;
        }
        pos++;
      }
    //REMOVE_TUPLEPIN tuplePtr.unpinTuple();

    }
  }

  public static ConfigManager setUpStorage(boolean storage) 
  {
    return setUpStorage(storage, null, "BDBStorage");
  }
  
  public static ConfigManager setUpStorage(boolean storage, String storageName) 
  {
	  return setUpStorage(storage, null, storageName);
  }

  public static ConfigManager  setUpStorage(boolean storage, ConfigManager config) 
  {
	  return setUpStorage(storage, config, "BDBStorage");
  }

  public static ConfigManager setUpStorage(boolean storage, ConfigManager config, String storageName) 
  {
    if (s_storageInit)
      return config;
    System.out.println("setUpStorage");
    s_storageInit = true;
    if (config == null)
      config = new ConfigManager();
    s_storageFolder = System.getProperty(TEST_TWORK_PROPERTY, ".");
    s_storageFolder += File.separator;
    s_storageFolder += "cep"; 
    s_storageFolder += File.separator;
    s_storageFolder += "storage"; //This should be the same as the setting in deploy_test.xml
    String temp = System.getProperty(Constants.STORAGE_FOLDER, null);
    if (temp != null && temp.length() > 0)
    {
      s_storageFolder = temp;
    }
    System.setProperty(StorageManager.SPILL_STORAGE_PROPERTY, storageName);
    System.setProperty(StorageManager.METADATA_STORAGE_PROPERTY, storageName);
    System.setProperty(Constants.STORAGE_FOLDER, s_storageFolder);
    config.setStorageFolder(s_storageFolder);
    config.setMetadataStorageName(storageName);
    if (storage)
    {
      config.setSpillStorageName(storageName);
      config.setStorageCacheSize(-10);
      cleanStorageFolder();
    }
    InterpDrv.cleanStorageFolder(s_storageFolder);
    try {
      s_interpDrv = InterpDrv.getInstance();
      IEvictPolicy evictPolicy = new oracle.cep.memmgr.evictPolicy.BottomUpPolicy();
      s_interpDrv.setEvictPolicy(evictPolicy);
      s_interpDrv.setUp(config);
    } catch(Exception e)
    {
      e.printStackTrace();
    }
    return config;
  }

  public static void cleanStorageFolder()
  {
    File sf = new File(s_storageFolder);
    String[] fileNames = sf.list();
    if (fileNames != null)
    {
      for (String filename: fileNames)
      {
        String path = s_storageFolder + File.separator + filename;
        boolean success = (new File(path)).delete();
        if (!success)
        {
          System.out.println("failed to delete " + path);
        }
      }
    }
  }

  public TestStorageBase(String storageName) throws Exception
  {
    ConfigManager cfg = setUpStorage(true, storageName);
    CEPManager cepMgr = CEPManager.getInstance();
    cepMgr.setConfig(cfg);
    cepMgr.init();
    StorageManager sm = cepMgr.getStorageManager();
    m_storage = sm.getSpillStorage();
    m_metastorage = sm.getMetadataStorage();
    m_helper = new Helper();
  }

  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
  }

  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
  }

  public void testSimpleTuple() throws CEPException
  {
    /******************
    int specid = 0;
    ITuplePtr tuple1r = m_helper.allocTuple(specid);
    m_helper.setTuple(specid, tuple1r, 12345, "Test");
    ITuple tuple = tuple1r.pin(IStoredElement.READ);
    m_storage.putRecord(null, tuple1r.getNameSpace(), tuple1r.getKey(), tuple);
    tuple1r.unpin();

    ITuple tuple1 =
      (ITuple)m_storage.getRecord(null, tuple1r.getNameSpace(), tuple1r.getKey());
    assertNotNull(tuple1);
    m_helper.compareTuple(specid, tuple1r, tuple1);
    m_storage.deleteRecord(null, tuple1r.getNameSpace(), tuple1r.getId());
    tuple1 =
        (ITuple)m_storage.getRecord(null, tuple1r.getNameSpace(), tuple1r.getKey());
    assertNull(tuple1);
    ******************/
  }

  public void testObjTuple() throws CEPException
  {
    /**********
    int specid = 1;
    ITuplePtr tuple1r = m_helper.allocTuple(specid);
    m_helper.setTuple(specid, tuple1r, 56789, "Test1", "Object");
    ITuple tuple = tuple1r.pin(IStoredElement.READ);
    m_storage.putRecord(null, tuple1r.getNameSpace(), tuple1r.getKey(), tuple);
    tuple1r.unpin();

    ITuple tuple1 =
      (ITuple)m_storage.getRecord(null, tuple1r.getNameSpace(), tuple1r.getKey());
    assertNotNull(tuple1);
    m_helper.compareTuple(specid, tuple1r, tuple1);
    *******************/
  }

  private static class SecondMax extends AggrFunctionImpl implements IAggrFnFactory,
                                                                     Serializable
  {

    int max;
    int secondMax;
    int numInputs;

    public IAggrFunction newAggrFunctionHandler() throws UDAException
    {
      return new SecondMax();
    }

    public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException
    {
    }

    public void initialize() throws UDAException
    {
      max = 0;
      secondMax = 0;
      numInputs = 0;
    }

    public void handlePlus(AggrInteger value,
                           AggrInteger result) throws UDAException
    {
      int v = 0;

      if (!value.isNull())
      {
        numInputs++;
        v = value.getValue();
      }


      if (numInputs == 0)
      {
        result.setNull(true);
      }

      else if (numInputs == 1)
      {
        max = v;
        result.setValue(max);
        return;
      }

      else if (numInputs == 2)
      {
        if (v > max)
        {
          secondMax = max;
          max = v;
        } else
          secondMax = v;
      } else
      {
        if (v > max)
        {
          secondMax = max;
          max = v;
        } else if (v > secondMax)
          secondMax = v;
      }

      result.setValue(secondMax);
    }

    public boolean equals(Object o)
    {
      SecondMax other = (SecondMax)o;
      if (max != other.max)
        return false;
      if (secondMax != other.secondMax)
        return false;
      if (numInputs != other.numInputs)
        return false;
      return true;
    }
  }

  public void testTupleAggrFunc() throws CEPException
  {
    /****************
    int specid = 1;
    ITuplePtr tuple1r = m_helper.allocTuple(specid);
    IAggrFunction aggrFunc = new SecondMax();
    AggrInteger res = new AggrInteger();
    AggrInteger v = new AggrInteger();
    v.setValue(1);
    aggrFunc.handlePlus(v, res);
    v.setValue(2);
    aggrFunc.handlePlus(v, res);
    m_helper.setTuple(specid, tuple1r, 56789, "TestAggr", aggrFunc);
    ITuple tuple = tuple1r.pin(IStoredElement.READ);
    m_storage.putRecord(null, tuple1r.getNameSpace(), tuple1r.getKey(), tuple);
    tuple1r.unpin();

    ITuple tuple1 =
      (ITuple)m_storage.getRecord(null, tuple1r.getNameSpace(), tuple1r.getKey());
    assertNotNull(tuple1);
    m_helper.compareTuple(specid, tuple1r, tuple1);
    **********************/
  }
  /*
    public void testDoublyList() throws CEPException
    {
        int specid = 1;
        ITuplePtr tuple1r = m_helper.allocTuple(specid);
        IDoublyList<ITuplePtr> tupList = (IDoublyList<ITuplePtr>) ttupleListFac.alloc();
        tupList.setFactory(ttupleNodeFac);

        ITuplePtr tuple1_0 = m_helper.allocTuple(0);
        setTuple(0, tuple1_0, 123);
        tupList.add(tuple1_0);
        ITuplePtr tuple1_1 = m_helper.allocTuple(0);
        setTuple(1, tuple1_1, 456, "def");
        tupList.add(tuple1_1);
        ITuplePtr tuple1_2 = m_helper.allocTuple(0);
        setTuple(1, tuple1_2, 456, "def");
        tupList.add(tuple1_2);

        setTuple(specid, tuple1r, 56789, "TestHdr", tupList);

        ITuple tuple = tuple1r.pin();
        storage.putRecord(null, tuple1r.getNameSpace(), tuple1r.getKey(), tuple);
        tuple1r.unpin();

        ITuple tuple1 = (ITuple) storage.getRecord(null, tuple1r.getNameSpace(), tuple1r.getKey());
        assertNotNull(tuple1);
        compareTuple(specid, tuple1r, tuple1);
    }

    public void testTupleList() throws CEPException
    {
        TimeStampFactory tsFac = (TimeStampFactory) FactoryManager.get(FactoryManagerContext.TIMESTAMP_FACTORY_ID);

        ListFactory<ITuplePtr, TimeStamp> lFactory = (ListFactory<ITuplePtr, TimeStamp>)
          FactoryManager.get(FactoryManagerContext.LIST_FACTORY_ID);

        List<ITuplePtr, TimeStamp>  head = (List<ITuplePtr, TimeStamp>) lFactory.alloc();
        List<ITuplePtr, TimeStamp>  tail = (List<ITuplePtr, TimeStamp>) lFactory.alloc();
        TimeStamp ts1 = (TimeStamp) tsFac.alloc();
        TimeStamp ts2 = (TimeStamp) tsFac.alloc();
        ITuplePtr tuple1_0 = m_helper.allocTuple(0);
        setTuple(0, tuple1_0, 123, "abc");
        ITuplePtr tuple1_1 = m_helper.allocTuple(0);
        setTuple(1, tuple1_1, 456, "def");
        head.setFirstElem(tuple1_0);
        head.setSecElem(ts1);
        tail.setFirstElem(tuple1_1);
        tail.setSecElem(ts2);
        head.next = tail;

        storage.putRecord(null, head.getNameSpace(), head.getId(), head);
        storage.putRecord(null, tail.getNameSpace(), tail.getId(), tail);

        List<ITuplePtr, TimeStamp>  headElem1 = (List<ITuplePtr, TimeStamp> ) storage.getRecord(null, head.getNameSpace(), head.getId());
        assertNotNull(headElem1);
        List<ITuplePtr, TimeStamp>  tailElem1 = (List<ITuplePtr, TimeStamp> ) storage.getRecord(null, tail.getNameSpace(), tail.getId());
        assertNotNull(tailElem1);
        assertTrue(head.equals(headElem1));
        assertTrue(tail.equals(tailElem1));
    }

    public void testTupleElemList() throws CEPException
    {
        ElementListFactory<Element> lFactory = (ElementListFactory<Element>)
          FactoryManager.get(FactoryManagerContext.ELEMENT_LIST_FACTORY_ID);

        ElementFactory elemFactory = (ElementFactory)
          FactoryManager.get(FactoryManagerContext.ELEMENT_FACTORY_ID);

        TimeStampFactory tsFac = (TimeStampFactory)
          FactoryManager.get(FactoryManagerContext.TIMESTAMP_FACTORY_ID);

        Element e = (Element) elemFactory.alloc();
        e.setKind(ElementKind.E_PLUS);
        ITuplePtr tuple1 = m_helper.allocTuple(0);
        setTuple(0, tuple1, 123, "abc");
        TimeStamp ts1 = (TimeStamp) tsFac.alloc();
        e.setTs(ts1);
        e.setTuple(tuple1);

        Element e1 = (Element) elemFactory.alloc();
        e.setKind(ElementKind.E_PLUS);
        ITuplePtr tuple2 = m_helper.allocTuple(0);
        setTuple(0, tuple2, 456, "xyz");
        TimeStamp ts2 = (TimeStamp) tsFac.alloc();
        e1.setTs(ts2);
        e1.setTuple(tuple2);

        ElementList<Element> list = (ElementList<Element>) lFactory
            .alloc();
        ElementList<Element> list1 = (ElementList<Element>) lFactory
            .alloc();
        list1.setElem(e1);

        // Initialize list element
        list.setElem(e);
        list.next = list1;

        storage.putRecord(null, list.getNameSpace(), list.getId(), list);
        storage.putRecord(null, list1.getNameSpace(), list1.getId(), list1);
        ElementList<Element> listElemR1 = ( ElementList<Element>) storage.getRecord(null, list.getNameSpace(), list.getId());
        assertNotNull(listElemR1);
        assertTrue(list.equals(listElemR1));
        ElementList<Element> listElemR2 = ( ElementList<Element>) storage.getRecord(null, list1.getNameSpace(), list1.getId());
        assertNotNull(listElemR2);
        assertTrue(list1.equals(listElemR2));

    }

    public void testNode() throws CEPException
    {
        int specid = 1;
        ITuplePtr tuple1r = m_helper.allocTuple(specid);

        ITuplePtr tuple1_0 = m_helper.allocTuple(0);
        setTuple(0, tuple1_0, 123);
        ITuplePtr tuple1_1 = m_helper.allocTuple(0);
        setTuple(1, tuple1_1, 456, "def");
        ITuplePtr tuple1_2 = m_helper.allocTuple(0);
        setTuple(1, tuple1_2, 456, "def");

        NodeFactory<ITuplePtr> nFactory = (NodeFactory<ITuplePtr>)
          FactoryManager.get(FactoryManagerContext.DOUBLY_LIST_NODE_FACTORY_ID);
        INode<ITuplePtr> n1 = (INode<ITuplePtr>) nFactory.alloc();
        n1.setNodeElem(tuple1_0);

        INode<ITuplePtr> n2 = (INode<ITuplePtr>) nFactory.alloc();
        n2.setNodeElem(tuple1_1);
        n1.setNext(n2);
        n2.setPrev(n1);

        storage.putRecord(null, n1.getNameSpace(), n1.getId(), n1);
        storage.putRecord(null, n2.getNameSpace(), n2.getId(), n2);

        INode<ITuple> n1r = (INode<ITuple>) storage.getRecord(null, n1.getNameSpace(), n1.getKey());
        assertNotNull(n1r);
        assertTrue(n1.equals(n1r));
        INode<ITuple> n2r = (INode<ITuple>) storage.getRecord(null, n2.getNameSpace(), n2.getKey());
        assertNotNull(n2r);
        assertTrue(n2.equals(n2r));
    }
*/

  public static class TestObj implements Serializable
  {
    String schema;
    String key;
    String val;
    
    public TestObj() {};
    public TestObj(String s, String k, String v) {schema = s; key = k; val = v;}
    public String toString() {return key + "=" + val;}
    public int hashCode() {
      int h[] = new int[2];
      h[0] = key.hashCode();
      h[1] = val.hashCode();
      return Arrays.hashCode(h);
    }
    public boolean equals(Object o)
    {
      TestObj other = (TestObj) o;
      return (schema.equals(other.schema) && key.equals(other.key) && val.equals(other.val));
    }
  }
  
  private int querySchemaKeys(String nameSpace, String schema)
  {
    int count = 0;
    //verify query
    System.out.println("queryKey for Schema " + schema);
    IStorageContext qctx = m_storage.initQuery(nameSpace, schema);
    while (true) 
    {
      String k = (String) m_storage.getNextKey(qctx);
      if (k == null) break;
      System.out.println(k);
      count++;
    }
    return count;
  }
  
  private int querySchemaRecords(String nameSpace, String schema)
  {
    int count = 0;
    //verify query
    System.out.println("queryRecordsfor Schema=" + schema);
    IStorageContext qctx = m_storage.initQuery(nameSpace, schema);
    while (true) 
    {
      Object k = m_storage.getNextRecord(qctx);
      if (k == null) break;
      System.out.println(k.toString());
      count++;
    }
    return count;
  }  
  public void testSchema() throws CEPException
  {
    String nameSpace = "TESTNS";
    boolean transactional = true;
    m_metastorage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, nameSpace, transactional, 
        String.class, String.class, TestObj.class);
    String[] schemas = new String[] {"Test1", "Test2"};
    TestObj[] objs = new TestObj[] {
        new TestObj("Test1", "1", "TestObj1"),
        new TestObj("Test2", "21", "TestObj21"),
        new TestObj("Test1", "2", "TestObj2"),
        new TestObj("Test2", "22", "TestObj22"),
        new TestObj("Test2", "23", "TestObj23"),
      new TestObj("Test1", "3", "TestObj3") };
    int total = 0;
    int[] counts = new int[2];
    counts[0] = 0;
    counts[1] = 0;
    for (TestObj obj : objs)
    {
      total++;
      String schema = obj.schema;
      for (int i = 0; i < schemas.length; i++)
      {
        if (schemas[i].equals(schema))
        {
          counts[i]++;
          break;
        }
      }
      String key = schema + "." + obj.key;
      boolean b = m_storage.putRecord(null, nameSpace, schema, key, obj);
      assertTrue(b);
    }
    //verify individual retrieval
    for (TestObj obj : objs)
    {
      String schema = obj.schema;
      String key = schema + "." + obj.key;
      TestObj obj1 = (TestObj) m_storage.getRecord(null, nameSpace, key);
      if (obj1 == null)
      {
        System.out.println("Failed to get ns="+nameSpace + " " + key);
      }
      assertNotNull(obj1);
      System.out.println(obj1);
      if (!obj.equals(obj1))
      {
        System.out.println("Failed to verify ns="+nameSpace + " " + key + obj.toString() + "," +obj1.toString());
      }
      assertEquals(obj, obj1);
    }
    
    int count = querySchemaKeys(nameSpace, null);
    if (count != total) 
    {
      System.out.println("key counts for " + nameSpace + " " + count + " expected:"+total);
    }

    assertEquals(count, total);
    count = querySchemaRecords(nameSpace, null);
    if (count != total) 
    {
      System.out.println("record counts for " + nameSpace + " " + count + " expected:"+total);
    }

    assertEquals(count, total);
    
    //verify query
    for (int i = 0; i < schemas.length; i++)
    {
      count = querySchemaKeys(nameSpace, schemas[i]);
      if (count != counts[i]) 
      {
        System.out.println("key counts for " + schemas[i] + " " + count + " expected:"+counts[i]);
      }

      assertEquals(count, counts[i]);
      count = querySchemaRecords(nameSpace, schemas[i]);
      if (count != counts[i]) 
      {
        System.out.println("record counts for " + schemas[i] + " " + count + " expected:"+counts[i]);
      }

      assertEquals(count, counts[i]);
    }
    
    //verify delete
    for (TestObj obj : objs)
    {
      String schema = obj.schema;
      int stotal = 0;
      for (int i = 0; i < schemas.length; i++)
      {
        if (schemas[i].equals(schema))
        {
          stotal = --counts[i];
          break;
        }
      }
      String key = schema + "." + obj.key;
      boolean b = m_storage.deleteRecord(null, nameSpace, schema, key);
      assertTrue(b);
      TestObj obj1 = (TestObj) m_storage.getRecord(null, nameSpace, key);
      if (obj1 != null)
      {
        System.out.println("Filed to get ns="+nameSpace + " " + key);
      }
      assertNull(obj1);
      
      total--;
      count = querySchemaKeys(nameSpace, null);
      if (count != total) 
      {
        System.out.println("key counts for " + nameSpace + " " + count + " expected:"+total);
      }

      assertEquals(count, total);
      count = querySchemaRecords(nameSpace, null);
      if (count != total) 
      {
        System.out.println("record counts for " + nameSpace + " " + count + " expected:"+total);
      }

      assertEquals(count, total);
            
      count = querySchemaKeys(nameSpace, schema);
      if (count != stotal) 
      {
        System.out.println("key count for " + schema + " " + count + " expected:"+stotal);
      }
      assertEquals(count, stotal);
      
      count = querySchemaRecords(nameSpace, schema);
      if (count != stotal) 
      {
        System.out.println("record count for " + schema + " " + count + " expected:"+stotal);
      }
      assertEquals(count, stotal);
    }
  }
}
