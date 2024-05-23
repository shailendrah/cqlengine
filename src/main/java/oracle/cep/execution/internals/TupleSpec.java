/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/TupleSpec.java /main/15 2009/11/09 10:10:58 sborah Exp $ */
/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */
/*
 DESCRIPTION
 Declares TupleSpec in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sborah    07/15/09 - support for bigdecimal
    sborah    12/15/08 - handle constants
    hopark    10/10/08 - remove statics
    hopark    03/07/08 - add addManagedObj
    hopark    02/05/08 - parameterized error
    hopark    07/31/07 - add dynamic tuple class gen
    najain    05/09/07 - variable length datatype support
    hopark    05/08/07 - remove initTupleAttrs(ITuple api cleanup)
    najain    04/11/07 - add copy
    najain    03/12/07 - bug fix
    hopark    03/06/07 - removed FactoryManagerContext
    parujain  11/16/06 - assign address
    parujain  11/09/06 - Logical Operators implementation
    najain    07/21/06 - ref-count tuples 
    najain    04/10/06 - add constructor from PhyOpt
    skaluska  03/27/06 - implementation
    anasrini  03/24/06 - add toString 
    anasrini  03/24/06 - bug fix 
    najain    03/17/06 - make addAttr public 
    anasrini  03/15/06 - add online interfaces as well 
    skaluska  02/25/06 - Creation
    skaluska  02/25/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/TupleSpec.java /main/15 2009/11/09 10:10:58 sborah Exp $
 *  @author  najain
 *  @since   1.0
 */
package oracle.cep.execution.internals;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.common.Constants;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.TupleClassGen;
import oracle.cep.phyplan.PhyOpt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Externalizable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;


/**
 * TupleSpec
 *
 * @author najain
 */
public class TupleSpec implements Externalizable
{
  private static final long serialVersionUID = -2654548713332875268L;

  private Class         tupleClass;
  private String        basClassPath;
  
  private static final boolean TUPLE_GEN_DEBUG = false;

  private int id;
  
  /** Specification of the attributes in the input stream */
  private ArrayList<AttrSpec> attrs;
 
  private BitSet   managedObjs;
  
  /** Number of attributes used */
  private int numAttrs;
  
  /** Number of bit positions used for Boolean(scratch) */
  private int numBitPos;
  
  /** Position for boolean datatype used only in the 
   case of evaluation of BInstrs i.e. execution of Conditions */
  private int boolPos;
  
  private List<IChgNotifier> listeners;
  
  public interface IChgNotifier
  {
    void attrUpdated(int pos);
    void attrAdded(int pos);
  };
  
  /**
   * Constructor for TupleSpec -- all pther constructors call this
   * @param id TODO
   */
  public TupleSpec(int id, int maxAttrs)
  {
    this.id = id;
    attrs = new ExpandableArray<AttrSpec>(maxAttrs);
    numAttrs = 0;
    numBitPos = 0;
    boolPos  = 0;
    tupleClass = null;
    managedObjs = new BitSet();
    listeners = new LinkedList<IChgNotifier>();
  }

  public TupleSpec() {
      listeners = new LinkedList<IChgNotifier>();
  }
  
  /**
   * Default Constructor
   * @param id TODO
   */
  public TupleSpec(int id) {
    this(id, Constants.INITIAL_ATTRS_NUMBER);
  }

  /**
   * @param id TODO
   * @param op Physical Operator to initialize the tuple specification from
   * @throws ExecException 
   */
  public TupleSpec(int id, PhyOpt op) throws ExecException
  {
    this(id, op.getNumAttrs());

    for (int a = 0 ; a < op.getNumAttrs(); a++)
      addAttr(a, op.getAttrMetadata(a));
  }

  public BitSet getManagedObjs() {return managedObjs;}
  /**
   * Get the unique id. for this tupleSpec
   * @return the id
   */
  public int getId() {
    return id;
  }
  
  /**
   * Get the number of registered attributes
   * @return the number of registered attributes
   */
  public int getNumAttrs() {
    return numAttrs;
  }

 
  /**
   * Add attribute with specified type and max length
   * @param pos Position
   * @param typ Type
   * @param len Max length
   * @throws ExecException 
   */
  public void addAttr(int pos, AttributeMetadata attrMetadata)
  throws ExecException
  {
    attrs.set(pos, new AttrSpec(attrMetadata));  
   
    numAttrs++;
    attrAdded(pos);
    notifyAddAttr(pos);
  }

  /**
   * Add the Boolean Attribute. It will re-use the byte
   * if already allocated. This is used mainly for conditions
   * 
   * @return array containing Attribute position and bitPosition
   * @throws ExecException
   */
  public int[] addBooleanAttr() throws ExecException
  {
    if(numBitPos == 0)
    {
      boolPos = numAttrs;
      // Since we want to use just one bit for the result
      // of any computation, we allocate one BYTE and then grow
      // the size if necessary
      addAttr(boolPos, new AttributeMetadata(Datatype.BYTE, 1,0,0));
    }
    else
    {
      int bpos = (numBitPos+1)/Constants.BITS_PER_BYTE;
      int len = attrs.get(boolPos).getAttrMetadata().getLength();
      // Need to grow
      if(len <= bpos)
       attrs.get(boolPos).getAttrMetadata().setLength(len+1);
      attrUpdated(boolPos);
      notifyUpdateAttr(boolPos);
    }
    int ret_addr[] = new int[2];
    ret_addr[0] = boolPos;
    ret_addr[1] = numBitPos;
    numBitPos++;
    return ret_addr;
  }
  
  /**
   * Update attribute with specified type and max length
   * @param pos Position
   * @param typ Type
   * @param len Max length
   * @throws ExecException 
   */
  public void updateAttr(int pos, AttributeMetadata attrMetadata) 
  throws ExecException
  {
    if (pos >= attrs.size())
      throw new ExecException(ExecutionError.TUPLESPEC_OVERFLOW, attrs.size());
   
    attrs.set(pos, new AttrSpec(attrMetadata));
   
    attrUpdated(pos);
    notifyUpdateAttr(pos);
  }

  public int addManagedObj(Datatype typ) 
    throws ExecException 
  {
    int pos = numAttrs;
    managedObjs.set(pos);
    addAttr(pos, new AttributeMetadata(typ, 0, 0, 0));
    return pos;
  }
  
  // REFORMAT LATER ON !!! 
  /**
   * Add attribute at the next available position
   * <p>
   * This method should be used for those datatypes that are fixed length
   * @param typ datatype for the attribute
   * @return the position where the attribute has been added
   */
  public int addAttr(Datatype typ) throws ExecException 
  {
    int pos = numAttrs;
    addAttr(pos, new AttributeMetadata(typ, 0, 0, 0));
    return pos;
  }

  
  /**
   * Add attribute at the next available position
   * <p>
   * This method should be used for those datatypes that are variable length
   * @param attrMetadata The metadata of the attribute added.
   * @return the position where the attribute has been added
   */
  public int addAttr(AttributeMetadata attrMetadata) throws ExecException 
  {
    int pos = numAttrs;
    addAttr(pos, attrMetadata);
    return pos;
  }

  /**
   * Get attribute precision for specified position
   * @param pos Position
   * @return Precision for attribute
   */
  public AttributeMetadata getAttrMetadata(int pos)
  {
    if (pos >= numAttrs)
      throw new IllegalArgumentException(pos + " >= " + numAttrs);

    return attrs.get(pos).getAttrMetadata();
  }
  /**
   * Get attribute type for specified position
   * @param pos Position
   * @return Datatype for attribute
   */
  public Datatype getAttrType(int pos)
  {
    if (pos >= numAttrs)
      throw new IllegalArgumentException(pos + " >= " + numAttrs);

    return attrs.get(pos).getType();
  }

  /**
   * Get attribute length for specified position
   * @param pos Position
   * @return Length for attribute
   */
  public int getAttrLen(int pos)
  {
    if (pos >= numAttrs)
      throw new IllegalArgumentException(pos + " >= " + numAttrs);

    return attrs.get(pos).getLength();
  }
  
  /**
   * Get attribute precision for specified position
   * @param pos Position
   * @return Precision for attribute
   *//*
  public int getAttrPrecision(int pos)
  {
    if (pos >= numAttrs)
      throw new IllegalArgumentException(pos + " >= " + numAttrs);

    return attrs.get(pos).getPrecision();
  }
  
  *//**
   * Get attribute scale for specified position
   * @param pos Position
   * @return Scale for attribute
   *//*
  public int getAttrScale(int pos)
  {
    if (pos >= numAttrs)
      throw new IllegalArgumentException(pos + " >= " + numAttrs);

    return attrs.get(pos).getScale();
  }*/

  public void copy(TupleSpec src) throws ExecException
  {
    //assert attrs.size() >= src.getNumAttrs();
    for (int i = 0; i < src.getNumAttrs(); i++)
      addAttr(i,src.getAttrMetadata(i));
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<TupleSpec>");
    for (int i=0; i<numAttrs; i++) {
      sb.append("<AttrSpec pos=\"" + i + "\" type=\"" + getAttrType(i) +
                "\" len=\"" + getAttrLen(i) + "\" />");
    }
    sb.append("</TupleSpec>");
    return sb.toString();
  }
  
  public void setBaseClass(String bcp)
  {
    basClassPath = bcp;
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.execution.internals.TupleSpec.IChgNotifier#attrAdded(int)
   */
  private void attrAdded(int pos)
  {
    assert (tupleClass == null) : "TupleSpec cannot be changed after tuple class is generated " + toString();
    tupleClass = null;
  }

  private void attrUpdated(int pos)
  {
    assert (tupleClass == null) : "TupleSpec cannot be changed after tuple class is generated " + toString();
    tupleClass = null;
  }
  
  public Class getTupleClass()
  {
    if (tupleClass != null)
      return tupleClass;
    
    assert (basClassPath != null);
    String name = "Tuple_" + getId();
    TupleClassGen gen = new TupleClassGen(basClassPath, name, this);
    gen.create();
    if (TUPLE_GEN_DEBUG)
    {
      String filename = "/tmp/" + name + ".class";
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
    tupleClass = gen.loadToJvm();
    return tupleClass;
  }

  //TODO use generic event handling
  public synchronized void addListener(IChgNotifier listener)
  {
    listeners.add(listener);
  }
  
  private synchronized void notifyAddAttr(int pos)
  {
    try {
      for (IChgNotifier listener : listeners)
      {
        listener.attrAdded(pos);
      }
    }
    catch(Throwable e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
  }

  private synchronized void notifyUpdateAttr(int pos)
  {
    try {
      for (IChgNotifier listener : listeners)
      {
        listener.attrUpdated(pos);
      }
    }
    catch(Throwable e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
  }


  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(id);
      out.writeInt(numAttrs);
      out.writeInt(numBitPos);
      out.writeInt(boolPos);
      out.writeObject(tupleClass != null ? tupleClass.getName() : null);
      out.writeObject(basClassPath);;
      for (int i = 0; i < numAttrs; i++) {
          AttrSpec a = attrs.get(i);
          out.writeObject(a);
      }
      out.writeObject(managedObjs);
  }


  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      id = in.readInt();
      numAttrs = in.readInt();
      numBitPos = in.readInt();
      boolPos = in.readInt();
      String clz = (String)in.readObject();
      if (clz != null) {
          tupleClass = Class.forName(clz);
      }
      basClassPath = (String)in.readObject();;
      attrs = new ExpandableArray<AttrSpec>(numAttrs+1);
      for (int i = 0; i < numAttrs; i++) {
          AttrSpec a = (AttrSpec)in.readObject();
          attrs.add(a);
      }
      managedObjs = (BitSet) in.readObject();

  }
}
