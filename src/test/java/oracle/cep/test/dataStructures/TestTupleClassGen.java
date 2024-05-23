/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/dataStructures/TestTupleClassGen.java /main/11 2011/09/05 22:47:27 sbishnoi Exp $ */

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
    sborah      10/15/09 - support for bigdecimal
    sborah      02/17/09 - handle constants
    hopark      10/10/08 - remove statics
    hopark      02/04/08 - support double
    hopark      11/27/07 - add boolean type
    hopark      11/16/07 - xquery support
    hopark      07/23/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/dataStructures/TestTupleClassGen.java /main/10 2009/11/09 10:10:59 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.dataStructures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.memory.DynTupleBase;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.test.InterpDrv;
import oracle.cep.dataStructures.internal.TupleClassGen;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestTupleClassGen extends TestCase
{
  private static final boolean DEBUG_TUPLE_GEN = false;
  
  protected static final int ITERATION = 100;
  protected static final String TEST_TWORK_PROPERTY = "twork";
  protected String m_work;
  protected Random m_random;
  protected static int s_nextClassId = 0;

  private static boolean s_storageInit = false;
  protected static InterpDrv s_interpDrv = null;

  public TestTupleClassGen(String name)
  {
    super(name);
    m_work = System.getProperty(TEST_TWORK_PROPERTY, "");
    m_work += File.separator;
    m_random = new Random(12345);
  }

  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
    if (s_storageInit)
      return;
    s_storageInit = true;
    try {
      ConfigManager config = new ConfigManager();
      config.setDynamicTupleClass(true);
      s_interpDrv = InterpDrv.getInstance();
      s_interpDrv.setUp(config);
    } catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  protected void verifyTuple(TupleSpec spec, ITuple tuple)
    throws ExecException
  {
    // Get the attributes
    Object[] vals = new Object[spec.getNumAttrs()];
    int nval, nval1;
    long lval, lval1;
    float fval, fval1;
    double dval, dval1;
    byte[] bval, bval1;
    char[] cval, cval1;
    Object oval, oval1;    
    boolean boolval, boolval1;
    BigDecimal nbval, nbval1;
    
    int pos;
    StringBuffer buf = new StringBuffer();
    for (pos = 0; pos < spec.getNumAttrs(); pos++)
    {
      if (pos > 0) buf.append(",");
      int n = m_random.nextInt(2);
      if (n == 1) 
      {
        tuple.setAttrNull(pos);
        vals[pos] = null;
        buf.append("null");
      } else {
	  switch(spec.getAttrType(pos).getKind())
        {
        case INT:
          nval = m_random.nextInt();
          tuple.iValueSet(pos, nval);
          vals[pos] = nval;
          buf.append(nval);
          break;
        case BIGINT:
          lval = m_random.nextLong();
          tuple.lValueSet(pos, lval);
          vals[pos] = lval;
          buf.append(lval);
          break;
        case FLOAT:
          fval = m_random.nextFloat();
          tuple.fValueSet(pos, fval);
          vals[pos] = fval;
          buf.append(fval);
          break;
        case DOUBLE:
          dval = m_random.nextDouble();
          tuple.dValueSet(pos, dval);
          vals[pos] = dval;
          buf.append(dval);
          break;
        case BYTE:
          nval = m_random.nextInt(spec.getAttrLen(pos));
          bval = new byte[nval];
          for (int j = 0; j < nval; j++)
          {
            bval[j] = (byte) m_random.nextInt(256);
          }
          tuple.bValueSet(pos, bval, nval);
          vals[pos] = bval;
          buf.append(bval);
          break;
        case CHAR:
          nval = m_random.nextInt(spec.getAttrLen(pos));
          cval = new char[nval];
          for (int j = 0; j < nval; j++)
          {
            cval[j] = (char)('A' + m_random.nextInt(30));
          }
          tuple.cValueSet(pos, cval, nval);
          vals[pos] = cval;
          buf.append(cval);
          break;
        case XMLTYPE:
          nval = m_random.nextInt(spec.getAttrLen(pos));
          cval = new char[nval];
          for (int j = 0; j < nval; j++)
          {
            cval[j] = (char)('A' + m_random.nextInt(30));
          }
          tuple.xValueSet(pos, cval, nval);
          vals[pos] = cval;
          buf.append(cval);
          break;
        case TIMESTAMP:
          lval = m_random.nextLong();
          tuple.tValueSet(pos, lval);
          vals[pos] = lval;
          buf.append(lval);
          break;
        case OBJECT:
          nval = m_random.nextInt(30);
          cval = new char[nval];
          for (int j = 0; j < nval; j++)
          {
            cval[j] = (char)('A' + m_random.nextInt(30));
          }
          oval = new String(cval);
          tuple.oValueSet(pos, oval);
          vals[pos] = oval;
          buf.append(oval);
          break;
        case INTERVAL:
          lval = m_random.nextLong();
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
          vals[pos] = lval;
          buf.append(lval);
          break;
        case INTERVALYM:
          lval = m_random.nextLong();
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
          vals[pos] = lval;
          buf.append(lval);
          break;
        case BOOLEAN:
          boolval = (m_random.nextInt() & 1) == 1;
          tuple.boolValueSet(pos, boolval);
          vals[pos] = boolval;
          buf.append(boolval);
          break;
        case BIGDECIMAL:
          dval = m_random.nextDouble();
          int precision = 2;
          int scale = 5;
          nbval = new BigDecimal(Double.toString(dval), 
                                   new MathContext(precision)
                                   ).setScale(scale, RoundingMode.HALF_UP);

          tuple.nValueSet(pos, nbval, precision, scale);
          vals[pos] = nbval;
          buf.append(nbval);
          break;
        default:
          // Should not come
          assert false;
        }//switch
      }//else
    } //for

    System.out.println(tuple.toString());
    System.out.println(buf.toString());
    int difcnt = 0;
    for (pos = 0; pos < spec.getNumAttrs(); pos++)
    {
      if (vals[pos] == null)
      {
        if (!tuple.isAttrNull(pos)) 
        {
          System.out.println(spec.getAttrType(pos) + " " + pos + " not null  ");
          difcnt++;
        }
      } else 
      {
	  switch(spec.getAttrType(pos).getKind())
        {
        case INT:
          nval = tuple.iValueGet(pos);
          nval1 = ((Integer)vals[pos]).intValue();
          if (nval != nval1) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + nval + "," + nval1);
            difcnt++;
          }
          break;
        case BIGINT:
          lval = tuple.lValueGet(pos);
          lval1 = ((Long)vals[pos]).longValue();
          if (lval != lval1)  
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + lval + "," + lval1);
            difcnt++;
          }
          break;
        case FLOAT:
          fval = tuple.fValueGet(pos);
          fval1 = ((Float)vals[pos]).floatValue();
          if (fval != fval1) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + fval + "," + fval1);
            difcnt++;
          }
            
          break;
        case DOUBLE:
          dval = tuple.dValueGet(pos);
          dval1 = ((Double)vals[pos]).doubleValue();
          if (dval != dval1) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + dval + "," + dval1);
            difcnt++;
          }
            
          break;
        case BYTE:
          bval = tuple.bValueGet(pos);
          bval1 = (byte[])vals[pos];
          nval = tuple.bLengthGet(pos);
          if (nval != bval1.length) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + nval + "," + bval1.length);
            difcnt++;
          }

          for (int j = 0; j < nval; j++)
          {
            if (bval[j] != bval1[j]) 
            {
              System.out.println(spec.getAttrType(pos) + " " + pos + " " + j + ":" + bval[j] + "," + bval1[j]);
             difcnt++;
             break;
            }
          }
          break;
        case CHAR:
          cval = tuple.cValueGet(pos);
          cval1 = (char[])vals[pos];
          nval = tuple.cLengthGet(pos);
          if (nval != cval1.length)
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + nval + "," + cval1.length);
            difcnt++;
          }

            
          for (int j = 0; j < nval; j++)
          {
            if (cval[j] != cval1[j]) 
            {
              System.out.println(spec.getAttrType(pos) + " " + pos + " " + j + ":" + cval[j] + "," + cval1[j]);
             difcnt++;
             break;
            }
          }
          break;
        case XMLTYPE:
          cval = tuple.xValueGet(pos);
          cval1 = (char[])vals[pos];
          nval = tuple.xLengthGet(pos);
          if (nval != cval1.length)
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + nval + "," + cval1.length);
            difcnt++;
          }

            
          for (int j = 0; j < nval; j++)
          {
            if (cval[j] != cval1[j]) 
            {
              System.out.println(spec.getAttrType(pos) + " " + pos + " " + j + ":" + cval[j] + "," + cval1[j]);
             difcnt++;
             break;
            }
          }
          break;
        case TIMESTAMP:
          lval = tuple.tValueGet(pos);
          lval1 = ((Long)vals[pos]).longValue();
          if (lval != lval1) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + lval + "," + lval1);
            difcnt++;
          }
          break;
        case OBJECT:
          oval = tuple.oValueGet(pos);
          oval1 = (String)vals[pos];
          if (!(oval.equals(oval1)))
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + oval + "," + oval1);
            difcnt++;
          }
          break;
        case INTERVAL:
          lval = tuple.vValueGet(pos);
          lval1 = ((Long)vals[pos]).longValue();
          if (lval != lval1) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + lval + "," + lval1);
            difcnt++;
          }
          break;
        case BOOLEAN:
          boolval = tuple.boolValueGet(pos);
          boolval1 = ((Boolean)vals[pos]).booleanValue();
          if (boolval != boolval1) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + boolval + "," + boolval1);
            difcnt++;
          }
          break;
        case BIGDECIMAL:
          nbval = tuple.nValueGet(pos);
          nbval1 = ((BigDecimal)vals[pos]);
          if (!nbval.equals(nbval1)) 
          {
            System.out.println(spec.getAttrType(pos) + " " + pos + " " + nbval + "," + nbval1);
            difcnt++;
          }
            
          break;
        default:
          // Should not come
          assert false;
        } //switch
      } //else
    } //for
    
    assertEquals(difcnt, 0);
  }
  
  protected String getBaseClassPath()
  {
    return "oracle.cep.dataStructures.internal.memory.DynTupleBase";
  }
  
  protected void genClass(TupleSpec spec)
    throws ExecException
  {
    System.out.println(spec.toString());
    String name = "Tuple_" + spec.getId() + "_" + s_nextClassId++;
    TupleClassGen gen = new TupleClassGen(getBaseClassPath(), name, spec);
    gen.create();
    if (DEBUG_TUPLE_GEN)
    {
      String filename = m_work + name + ".class";
      try {
        OutputStream f = new FileOutputStream(filename);
        gen.save(f);
        f.close();
        System.out.println(filename + " generated.");
      } catch (IOException e)
      {
        System.out.println(e);
      }
    }
    try {
     Class tupleClass = gen.loadToJvm();
     System.out.println("\n" + tupleClass.getName());
     if (DEBUG_TUPLE_GEN)
     {
       Field[] fields = tupleClass.getDeclaredFields();
       for (Field f : fields) 
       {
         System.out.println(f);
       }
       Method[] methods = tupleClass.getDeclaredMethods();
       for (Method m : methods)
       {
         System.out.println(m);
       }
     }
     DynTupleBase tuple = (DynTupleBase) tupleClass.newInstance();
     tuple.init(spec, false);
     verifyTuple(spec, tuple);
    } catch(Exception ex) {
      System.out.println(ex);
      ex.printStackTrace(System.out);
    }
  }
  
  private void genTuples(int n) throws ExecException
  {
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    TupleSpec spec = new TupleSpec(factoryMgr.getNextId());
    for (int i = 0; i < n; i++)
    {
      spec.addAttr(Datatype.INT);
      spec.addAttr(Datatype.BIGINT);
      spec.addAttr(Datatype.FLOAT);
      spec.addAttr(Datatype.DOUBLE);
      spec.addAttr(Datatype.TIMESTAMP);
      spec.addAttr(Datatype.INTERVAL);
      spec.addAttr(Datatype.OBJECT);
      spec.addAttr(new AttributeMetadata(Datatype.CHAR, 20, 0, 0));
      spec.addAttr(new AttributeMetadata(Datatype.XMLTYPE, 20, 0, 0));
      spec.addAttr(new AttributeMetadata(Datatype.BYTE, 20,0, 0));
      spec.addAttr(Datatype.BOOLEAN);
      spec.addAttr(new AttributeMetadata(Datatype.BIGDECIMAL, 0, 
          Datatype.BIGDECIMAL.getPrecision(), 0));
    }
    genClass(spec);
    
  }
  // all types
 
  public void testTuple1() throws ExecException
  {
    //genTuples(1);
  }
 
  public void testTuple2() throws ExecException
  {
    genTuples(2);
  }
  
  public void testTuple3() throws ExecException
  {
    genTuples(3);
  }

  public void testTuple4() throws ExecException
  {
    genTuples(4);
  }

  // int types with multiple
  public void testTupleX() throws ExecException
  {
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    TupleSpec spec = new TupleSpec(factoryMgr.getNextId());
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.BIGINT);
    spec.addAttr(Datatype.INT);
    spec.addAttr(Datatype.FLOAT);
    spec.addAttr(Datatype.DOUBLE);
    for (int i = 0; i < (Constants.INITIAL_ATTRS_NUMBER - 5); i++)
     spec.addAttr(Datatype.INT);
    genClass(spec);
  }
  
  public void testTupleRandom() throws ExecException
  {
    for (int i = 0; i < ITERATION; i++)
    {
      System.out.println("--------" + i);
      CEPManager cepMgr = CEPManager.getInstance();
      FactoryManager factoryMgr = cepMgr.getFactoryManager();
      TupleSpec spec = new TupleSpec(factoryMgr.getNextId());
      int nattrs = m_random.nextInt(Constants.INITIAL_ATTRS_NUMBER);
      for (int j = 0; j < nattrs; j++) 
      {
        int typ = m_random.nextInt(12);
        switch(typ)
        {
        case 0: spec.addAttr(Datatype.INT);   break;
        case 1: spec.addAttr(Datatype.BIGINT);   break;
        case 2: spec.addAttr(Datatype.FLOAT);   break;
        case 3: spec.addAttr(Datatype.TIMESTAMP);   break;
        case 4: spec.addAttr(Datatype.INTERVAL);   break;
        case 5: spec.addAttr(Datatype.OBJECT);   break;
        case 6: spec.addAttr(new AttributeMetadata(Datatype.CHAR, 20, 0, 0));   break;
        case 7: spec.addAttr(new AttributeMetadata(Datatype.BYTE, 20, 0, 0));   break;
        case 8: spec.addAttr(new AttributeMetadata(Datatype.XMLTYPE, 20, 0, 0));break;
        case 9: spec.addAttr(Datatype.BOOLEAN);   break;
        case 10: spec.addAttr(Datatype.DOUBLE);   break;
        case 11: spec.addAttr(new AttributeMetadata(Datatype.BIGDECIMAL, 0, 
            Datatype.BIGDECIMAL.getPrecision(), 0));   break;
        }
      }
      genClass(spec);
    }
  }

  public static Test suite()
  {
    if (SINGLE_TEST_NAME != null)
    {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestTupleClassGen(SINGLE_TEST_NAME));
      return suite;
    } else {
      return new TestSuite(TestTupleClassGen.class);
    }
  }
  
  public static final String SINGLE_TEST_NAME = null;//"testTuple6";
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestTupleClassGen.suite());
  }
}

