/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/TupleClassGen.java /main/12 2011/09/05 22:47:26 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    This class generates the Tuple class dynamicall using BCEL.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    This class is based on the template class using BCELifier
    The template class is in test/src/TupleTmpl.java.
    The following is how to use BCELifer:  
    cd test/src
    rm TupleTmpl.class
    javac -source 1.4 -target 1.4 TupleTmpl.java'
    java -classpath $ADE_VIEW_ROOT/j2ee/home_image/generated/j2ee/home/lib/bcel.jar org.apache.bcel.util.BCELifier TupleTmpl.class | tee TupleTmplGen.java
    replace all  _classPath _classPath
//    replace all "class$oracle$cep$dataStructures$internal$memory$TupleTmpl" _classPathI
//    replace all "oracle.cep.dataStructures.internal.memory.ExecException" _cpExecException
    replace all "oracle.cep.dataStructures.internal.memory.TupleBase" _baseClassPath
    replace all "oracle.cep.dataStructures.internal.memory.TupleSpec", _cpTupleSpec
    replace all "oracle.cep.dataStructures.internal.memory.ExecutionError" _cpExecutionError
    copy createMethod_1 to createMethod_oValueSet
    find 'lcmp' and change ,0 to ,0l
    find 'lshl' and change ,1 to ,1l
    
   MODIFIED    (MM/DD/YY)
    sbishnoi    08/28/11 - adding support for interval year to month
    hopark      12/02/09 - check MaxLen on byte,char types
    hopark      10/23/09 - support number type
    hopark      02/14/09 - fix xIsObj
    hopark      05/16/08 - add xIsObj
    hopark      03/04/08 - fix xml
    hopark      02/18/08 - support obj representation of xml
    hopark      02/04/08 - support double type
    hopark      11/27/07 - add boolean type
    hopark      11/16/07 - xquery support
    hopark      07/31/07 - change base class
    hopark      07/27/07 - add synchronization
    hopark      07/20/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/TupleClassGen.java /main/11 2009/12/05 13:43:53 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.internal;

import oracle.cep.common.Datatype;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.ClassGenBase;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TupleClassGen extends ClassGenBase
{
  private TupleSpec          _spec;
  
  private int _noNulls;
  private Type _fieldTypes[];  
  private String _fieldNames[];
  private String _methodGens0[][];
  private String _methodGensN[][];
  private TypeInfo _typeInfos[];  
  private int   _nullPositions[];
  
  private static final TypeDesc[] s_typeDescs = 
  {
    new TypeDesc(Datatype.INT,  Type.INT, "m_ival", 
       new String[] { "createMethod_iValueGet"/*iValueGet*/ , "createMethod_iValueSet"/*iValueSet*/ },
       new String[] { "createMethod_iValueGet2"/*iValueGet*/ , "createMethod_iValueSet2"/*iValueSet*/ } ),
    new TypeDesc(Datatype.BIGINT, Type.LONG , "m_lval",
       new String[] { "createMethod_lValueGet"/*lValueGet*/ , "createMethod_lValueSet"/*lValueSet*/ },
       new String[] { "createMethod_lValueGet2"/*lValueGet*/ , "createMethod_lValueSet2"/*lValueSet*/ } ),
    new TypeDesc(Datatype.FLOAT,  Type.FLOAT , "m_fval",
       new String[] { "createMethod_fValueGet"/*fValueGet*/ , "createMethod_fValueSet"/*fValueSet*/ },
       new String[] { "createMethod_fValueGet2"/*fValueGet*/ , "createMethod_fValueSet2"/*fValueSet*/ } ),
    new TypeDesc(Datatype.DOUBLE,  Type.DOUBLE , "m_dval",
       new String[] { "createMethod_dValueGet"/*dValueGet*/ , "createMethod_dValueSet"/*fValueSet*/ },
       new String[] { "createMethod_dValueGet2"/*dValueGet*/ , "createMethod_dValueSet2"/*dValueSet*/ } ),
    new TypeDesc(Datatype.BYTE, new ArrayType(Type.BYTE, 1) , "m_bval", 
       new String[] { "createMethod_bValueGet"/*bValueGet*/ , "createMethod_bLengthGet"/*bLengthGet*/ ,"createMethod_bValueSet"/*bValueSet*/ },
       new String[] { "createMethod_bValueGet2"/*bValueGet*/ , "createMethod_bLengthGet2"/*bLengthGet*/ , "createMethod_bValueSet2"/*bValueSet*/ } ),
    new TypeDesc(Datatype.CHAR, new ArrayType(Type.CHAR, 1) , "m_cval", 
       new String[] { "createMethod_cValueGet"/*cValueGet*/ , "createMethod_cLengthGet"/*cLengthGet*/ , "createMethod_cValueSet"/*cValueSet*/ },
       new String[] { "createMethod_cValueGet2"/*cValueGet*/ , "createMethod_cLengthGet2"/*cLengthGet*/ , "createMethod_cValueSet2"/*cValueSet*/ } ),
    new TypeDesc(Datatype.TIMESTAMP, Type.LONG , "m_tval",
       new String[] { "createMethod_tValueGet"/*tValueGet*/ , "createMethod_tValueSet"/*tValueSet*/ },
       new String[] { "createMethod_tValueGet2"/*tValueGet*/ , "createMethod_tValueSet2"/*tValueSet*/ } ),
    new TypeDesc(Datatype.OBJECT, Type.OBJECT , "m_oval",
       new String[] { "createMethod_oValueGet"/*oValueGet*/ , "createMethod_oValueSet"/*oValueSet*/ },
       new String[] { "createMethod_oValueGet2"/*oValueGet*/ , "createMethod_oValueSet2"/*oValueSet*/ } ),
    new TypeDesc(Datatype.INTERVAL, Type.LONG , "m_vval",
       new String[] { "createMethod_vValueGet"/*vValueGet*/ , "createMethod_vValueSet"/*vValueSet*/ },
       new String[] { "createMethod_vValueGet2"/*vValueGet*/ , "createMethod_vValueSet2"/*vValueSet*/ } ),
   new TypeDesc(Datatype.INTERVALYM, Type.LONG , "m_vymval",
       new String[] { "createMethod_vymValueGet"/*vymValueGet*/ , "createMethod_vymValueSet"/*vymValueSet*/ },
       new String[] { "createMethod_vymValueGet2"/*vymValueGet*/ , "createMethod_vymValueSet2"/*vymValueSet*/ } ),
    new TypeDesc(Datatype.XMLTYPE, Type.OBJECT, "m_xval", 
       new String[] { "createMethod_xValueGet"/*xValueGet*/ , "createMethod_xLengthGet"/*xLengthGet*/ , 
                      "createMethod_xValueSet"/*xValueSet*/, "createMethod_xObjValueSet"/*xObjValueSet*/, 
                      "createMethod_xObjValueGet"/*xObjValueGet*/, "createMethod_xIsObj" /*xIsObj*/  },
       new String[] { "createMethod_xValueGet2"/*xValueGet*/ , "createMethod_xLengthGet2"/*xLengthGet*/ , 
                      "createMethod_xValueSet2"/*xValueSet*/, "createMethod_xObjValueSet2"/*xObjValueSet2*/, 
                      "createMethod_xObjValueGet2"/*xObjValueGet2*/, "createMethod_xIsObj2" /*xIsObj*/  } ),
    new TypeDesc(Datatype.BOOLEAN, Type.BOOLEAN, "m_boolval", 
       new String[] { "create_boolValueGet"/*boolValueGet*/ , "create_boolValueSet"/*boolValueSet*/ },
       new String[] { "create_boolValueGet2"/*boolValueGet*/ , "create_boolValueSet2"/*boolValueSet*/ } ),
    new TypeDesc(Datatype.BIGDECIMAL, new ObjectType("java.math.BigDecimal"), "m_nval", 
       new String[] { "createMethod_nValueGet"/*nValueGet*/ , 
                      "createMethod_nPrecisionGet"/*nPrecisionGet*/ , 
                      "createMethod_nValueSet"/*nValueSet*/, 
                      "createMethod_nScaleGet"/*scaleGet*/ ,  },
       new String[] { "createMethod_nValueGet2"/*nValueGet*/ , 
                      "createMethod_nPrecisionGet2"/*nPrecisionGet*/ , 
                      "createMethod_nValueSet2"/*nValueSet*/,  
                      "createMethod_nScaleGet2"/*nScaleGet2*/ } ),
  };

  private static class TypeInfo
  {
    List<Integer> _posList;
    int[]         _positions;

    public TypeInfo() 
    {
     _posList = new LinkedList<Integer>();
     _positions = null;
    }
    
    int getUsages() 
    {
      return _posList.size();
    }
          
    void addPos(int pos)
    {
      _posList.add(pos);       
      _positions = null; 
    }
    
    int[] getPositions()
    {
      if (_positions != null)
        return _positions;
      _positions = new int[_posList.size()];
      int i = 0;
      for (Integer pos : _posList)
      {
        _positions[i++] = pos;
      }
      return _positions;
    }
  }
  
  private static class TypeDesc
  {
    Datatype _type;
    Type     _ftype;
    String   _fname;
    String[] _mgens0;
    String[] _mgensN;
    TypeDesc(Datatype t, Type ft, String fn, String[] mgens0, String[] mgensN)
    {
      _type = t;
      _ftype = ft;
      _fname = fn;
      _mgens0 = mgens0;
      _mgensN = mgensN;
    }
  }
  
  public TupleClassGen(String baseClassPath, String className, TupleSpec spec) 
  {
     super(baseClassPath,
           "oracle.cep.dataStructures.internal",
           "class$oracle$cep$dataStructures$internal",
           className);

    _spec = spec;      
    Datatype.Kind[] types = Datatype.Kind.values();
    int ntypes = types.length;
    _typeInfos = new TypeInfo[ntypes];
    _fieldTypes = new Type[ntypes];  
    _fieldNames = new String[ntypes];
    _methodGens0 = new String[ntypes][];
    _methodGensN = new String[ntypes][];
    for (TypeDesc td :    s_typeDescs)
    {
      int typ = td._type.ordinal();
      _fieldTypes[typ] = td._ftype;
      _fieldNames[typ] = td._fname;
      _methodGens0[typ] = td._mgens0;
      _methodGensN[typ] = td._mgensN;
      _typeInfos[typ] = new TypeInfo();
    }
  }

  protected void createFieldsMethods()
  {
    FieldGen field;

    int numAttrs = _spec.getNumAttrs();
    _noNulls = numAttrs / 64 + 1;
    _nullPositions = null;
    for (int i = 0; i < numAttrs; i++)
    {
      Datatype attrType = _spec.getAttrType(i);
      int typ = attrType.ordinal();
      _typeInfos[typ].addPos(i);
    }
    for (Datatype.Kind dtype : Datatype.Kind.values())
    {
      int typ = dtype.ordinal();
      if (_typeInfos[typ] == null) continue;
      int n =  _typeInfos[typ].getUsages();
      if (n == 0) continue;
      for (int i = 0; i < n; i++)
      {
        Type ftype = _fieldTypes[typ];
        String name = _fieldNames[typ];
        name += i;
        field = new FieldGen(ACC_PROTECTED, ftype, name, _cp);
        _cg.addField(field.getField());
        if (dtype == Datatype.CHAR.getKind())
        {
          name = "m_clen" + i;
          field = new FieldGen(ACC_PROTECTED, Type.INT, name, _cp);
          _cg.addField(field.getField());
        } else if (dtype == Datatype.BYTE.getKind())
        {
          name = "m_blen" + i;
          field = new FieldGen(ACC_PROTECTED, Type.INT, name, _cp);
          _cg.addField(field.getField());
        } else if (dtype == Datatype.XMLTYPE.getKind())
        {
          name = "m_xlen" + i;
          field = new FieldGen(ACC_PROTECTED, Type.INT, name, _cp);
          _cg.addField(field.getField());
        } else if (dtype == Datatype.BIGDECIMAL.getKind())
        {
          name = "m_precision" + i;
          field = new FieldGen(ACC_PROTECTED, Type.INT, name, _cp);
          _cg.addField(field.getField());
          name = "m_scale" + i;
          field = new FieldGen(ACC_PROTECTED, Type.INT, name, _cp);
          _cg.addField(field.getField());
        }
      }
    }

    for (int i = 0; i < _noNulls; i++)
    {
      field = new FieldGen(ACC_PROTECTED, Type.LONG, "m_null"+i, _cp);
      _cg.addField(field.getField());
    }

    field = new FieldGen(ACC_STATIC, new ObjectType("java.lang.Class"), _classPathI, _cp);
    _cg.addField(field.getField());

    createMethod_0();    
    methodGen(null, _noNulls);
    
    
    for (Datatype.Kind dtype : Datatype.Kind.values())
    {
      int typ = dtype.ordinal();
      if (_typeInfos[typ] == null) continue;
      int n =  _typeInfos[typ].getUsages();
      if (n == 0) continue;
      methodGen(dtype, n);
    }
  }

  private void methodGen(Datatype.Kind dtype, int n)
  {
    String methodName = "";
    String methodNames0[];
    String methodNamesN[];
    if (dtype == null)
    {
      methodNames0 = new String[] {        
        "createMethod_1", //isAttrNull
        "createMethod_2", //setAttrNull
        "createMethod_3", //setAttrbNullFalse
     };
      methodNamesN = new String[] {        
        "create_isAttrNull", //isAttrNull
        "create_setAttrNull", //setAttrNull
        "create_setAttrNullFalse", //setAttrbNullFalse
     };
   } else {
     int typ = dtype.ordinal();
     methodNames0 = _methodGens0[typ];
     methodNamesN = _methodGensN[typ];
    }
    try {
      if (n == 1)
      {
        for (String mn : methodNames0) 
        {
          methodName = mn;
          java.lang.reflect.Method  m = this.getClass().getDeclaredMethod(methodName);          
          if (m != null) 
          {
            m.setAccessible(true);
            m.invoke(this);
          } else {
            assert false : methodName + " is not found";
          }
        }
      } else {
        for (String mn : methodNamesN) 
        {
          methodName = mn;
          java.lang.reflect.Method m = this.getClass().getDeclaredMethod(methodName, new Class[] {int.class});          
          if (m != null) 
          {
            m.setAccessible(true);
            Object[] args = new Object[1];
            args[0] = new Integer(n);
            m.invoke(this, args);
          } else {
            assert false : methodName + " is not found";
          }
        }
      }
    }
    catch (NoSuchMethodException ex) 
    {
      assert false : methodName + " " + ex.toString();
    }
    catch (InvocationTargetException ex) 
    { 
      assert false : methodName + " " + ex.toString();
    }
    catch (IllegalAccessException ex) 
    { 
      assert false : methodName + " " + ex.toString();
    }
  }
  
  private int[] getNullPositions()
  {
    if (_nullPositions != null)
      return _nullPositions;
    _nullPositions = new int[_noNulls];
    for (int i = 0; i < _noNulls; i++)
      _nullPositions[i] = i;
    return _nullPositions;
  }
  
  //private void createMethod_xValueSet() {
  private void create_isAttrNull(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT }, new String[] { "arg0" }, "isAttrNull", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IDIV);
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IREM);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_10 = il.append(_factory.createLoad(Type.INT, 2));

    int[] positions = getNullPositions();
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select tableswitch_11 = new TABLESWITCH(positions, ihandles, null); //-----
    il.append(tableswitch_11);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] ifeqs = new BranchInstruction[n+1]; //-------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    InstructionHandle[] ifeqtargets = new InstructionHandle[n+1]; //-------
    InstructionHandle[] gototargets = new InstructionHandle[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_null" + i; //-----
   
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----

    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.GETFIELD));
    il.append(new PUSH(_cp, 1l));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.ISHL);
    il.append(InstructionConstants.I2L);
    il.append(InstructionConstants.LAND);
    il.append(new PUSH(_cp, 0l));
    il.append(InstructionConstants.LCMP);
        BranchInstruction ifeq_47 = _factory.createBranchInstruction(Constants.IFEQ, null);
    il.append(ifeq_47);
    ifeqs[i] = ifeq_47;
    il.append(new PUSH(_cp, 1));
        BranchInstruction goto_51 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_51);
    gotos[i] = goto_51;
    InstructionHandle ih_54 = il.append(new PUSH(_cp, 0));
    ifeqtargets[i] = ih_54;
    InstructionHandle ih_55 = il.append(_factory.createReturn(Type.INT));
    gototargets[i] = ih_55;
    } //-----
    
    InstructionHandle ih_110 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_111 = il.append(_factory.createReturn(Type.INT));
    tableswitch_11.setTarget(ih_110);
    for (int i = 0; i < n; i++) //------
    { //-----
    tableswitch_11.setTarget(i, targets[i]);
    ifeqs[i].setTarget(ifeqtargets[i]);
    gotos[i].setTarget(gototargets[i]);
    }
    
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }


  //private void createMethod_25() {
  private void create_setAttrNull(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT }, new String[] { "arg0" }, "setAttrNull", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IDIV);
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IREM);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_10 = il.append(_factory.createLoad(Type.INT, 2));

    int[] positions = getNullPositions();
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select tableswitch_11 = new TABLESWITCH(positions, ihandles, null); //-----

    il.append(tableswitch_11);
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_null" + i; //-----
   
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36;
    
    il.append(InstructionConstants.DUP);
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.GETFIELD));
    il.append(new PUSH(_cp, 1));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.ISHL);
    il.append(InstructionConstants.I2L);
    il.append(InstructionConstants.LOR);
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_49;
    BranchInstruction goto_49 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_49 = il.append(goto_49);
    gotos[i] = goto_49;
    } //-----

    InstructionHandle ih_98 = il.append(_factory.createReturn(Type.VOID));
    tableswitch_11.setTarget(ih_98);

    for (int i = 0; i < n; i++) //------
    { //-----
    tableswitch_11.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_98);
    }
        
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_26() {
  private void create_setAttrNullFalse(int n) 
  {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT }, new String[] { "arg0" }, "setAttrbNullFalse", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "getNumAttrs", Type.INT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        BranchInstruction if_icmplt_8 = _factory.createBranchInstruction(Constants.IF_ICMPLT, null);
    il.append(if_icmplt_8);
    InstructionHandle ih_8 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_68 = il.append(_factory.createReturn(Type.VOID));

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IDIV);
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_27 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IREM);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_32 = il.append(_factory.createLoad(Type.INT, 2));

    int[] positions = getNullPositions();
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select tableswitch_33 = new TABLESWITCH(positions, ihandles, null); //-----

    il.append(tableswitch_33);
    
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_null" + i; //-----
   
    InstructionHandle ih_60 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_60;

    il.append(InstructionConstants.DUP);
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.GETFIELD));
    il.append(new PUSH(_cp, 1));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.ISHL);
    il.append(new PUSH(_cp, -1));
    il.append(InstructionConstants.IXOR);
    il.append(InstructionConstants.I2L);
    il.append(InstructionConstants.LAND);
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_75;
    BranchInstruction goto_75 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_75 = il.append(goto_75);
    gotos[i] = goto_75;
    }
    
    InstructionHandle ih_128 = il.append(_factory.createReturn(Type.VOID));
    if_icmplt_8.setTarget(ih_22);
    tableswitch_33.setTarget(ih_128);
    for (int i = 0; i < n; i++) //------
    { //-----
    tableswitch_33.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_128);
    }    

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();

  }

//  private void createMethod_27() {
  private void createMethod_iValueGet2(int n) 
  {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "iValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));
    
    int[] positions = _typeInfos[Datatype.INT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_ival" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.INT));
    } //-----
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.INT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_28() {
  private void createMethod_iValueSet2(int n) 
  {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "iValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.INT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_ival" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_45;
    BranchInstruction goto_45 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_45 = il.append(goto_45);
    gotos[i] = goto_45;
    } //-----
    
    InstructionHandle ih_64 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_68 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_64);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_68);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_29() {
  private void createMethod_lValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "lValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.BIGINT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_lval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.LONG));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0l));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.LONG));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_30() {
  private void createMethod_lValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "lValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.BIGINT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_lval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----
    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_46;
    BranchInstruction goto_46 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_46 = il.append(goto_46);
    gotos[i] = goto_46;
    }
    

    InstructionHandle ih_67 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_71 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_67);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_71);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_31() {
  private void createMethod_fValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.FLOAT, new Type[] { Type.INT }, new String[] { "arg0" }, "fValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.FLOAT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);
    
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_fval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.FLOAT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.FLOAT));
    } //-----

    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0.0f));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.FLOAT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_32() {
  private void createMethod_fValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.FLOAT }, new String[] { "arg0", "arg1" }, "fValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.FLOAT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_fval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----

    il.append(_factory.createLoad(Type.FLOAT, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.FLOAT, Constants.PUTFIELD));
    InstructionHandle ih_46;
    BranchInstruction goto_46 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_46 = il.append(goto_46);
    gotos[i] = goto_46;
    } //-----
    
    InstructionHandle ih_67 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_71 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_67);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_71);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_33() {
  private void createMethod_tValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "tValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.TIMESTAMP.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_tval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----

    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.LONG));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0l));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.LONG));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_34() {
  private void createMethod_tValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "tValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.TIMESTAMP.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_tval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----

    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_46;
    BranchInstruction goto_46 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_46 = il.append(goto_46);
    gotos[i] = goto_46;
    } //-----

    InstructionHandle ih_67 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_71 = il.append(_factory.createReturn(Type.VOID));
    lookupswitch_6.setTarget(ih_67);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_71);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_35() {
  private void createMethod_vValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "vValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.INTERVAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_vval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----

    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.LONG));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0l));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.LONG));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_36() {
  private void createMethod_vValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "vValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.INTERVAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_vval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----

    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_46;
    BranchInstruction goto_46 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_46 = il.append(goto_46);
    gotos[i] = goto_46;
    } //-----
    

    InstructionHandle ih_67 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_71 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_67);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_71);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  
  private void createMethod_vymValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "vymValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.INTERVALYM.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_vval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----

    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.LONG));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0l));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.LONG));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_vymValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "vymValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.INTERVALYM.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_vval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----

    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_46;
    BranchInstruction goto_46 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_46 = il.append(goto_46);
    gotos[i] = goto_46;
    } //-----
    

    InstructionHandle ih_67 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_71 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_67);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_71);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_37() {
  private void createMethod_cValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.CHAR, 1), new Type[] { Type.INT }, new String[] { "arg0" }, "cValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.CHAR.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_cval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    il.append(_factory.createFieldAccess(_classPath, varname, new ArrayType(Type.CHAR, 1), Constants.GETFIELD));
    il.append(_factory.createReturn(Type.OBJECT));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(InstructionConstants.ACONST_NULL);
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.OBJECT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_38() {
  private void createMethod_cLengthGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "cLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.CHAR.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_clen" + i; //-----
   
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.INT));
    }

    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.INT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_39() {
  private void createMethod_cValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ArrayType(Type.CHAR, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "cValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "checkMaxLen", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKEVIRTUAL));

    InstructionHandle ih_6 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.CHAR.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_7 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_7);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    BranchInstruction[] goto0s = new BranchInstruction[n+1]; //-------
    InstructionHandle[] goto0Targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] ifnonnulls = new BranchInstruction[n+1]; //-------
    InstructionHandle[] ifnonnullTargets = new InstructionHandle[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_cval" + i; //-----
    String varname1 = "m_clen" + i; //-----

    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 2));
    targets[i] = ih_40; //----

        BranchInstruction ifnonnull_41 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_41);
    ifnonnulls[i] = ifnonnull_41; //------

    InstructionHandle ih_44 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_49 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_49);
    goto0s[i] = goto_49;

    InstructionHandle ih_52 = il.append(_factory.createLoad(Type.OBJECT, 0));
    ifnonnullTargets[i] = ih_52; //------

    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_57 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, varname, new ArrayType(Type.CHAR, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "copyVChar", new ArrayType(Type.CHAR, 1), new Type[] { new ArrayType(Type.CHAR, 1), new ArrayType(Type.CHAR, 1), Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createFieldAccess(_classPath, varname, new ArrayType(Type.CHAR, 1), Constants.PUTFIELD));
    InstructionHandle ih_71 = il.append(_factory.createLoad(Type.OBJECT, 0));
    goto0Targets[i] = ih_71;

    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, varname1, Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_76;
    BranchInstruction goto_76 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_76 = il.append(goto_76);
    gotos[i] = goto_76;
    } //-----

    InstructionHandle ih_157 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_161 = il.append(_factory.createReturn(Type.VOID));
    lookupswitch_7.setTarget(ih_157);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_7.setTarget(i, targets[i]);
    goto0s[i].setTarget(goto0Targets[i]);
    gotos[i].setTarget(ih_157);
    ifnonnulls[i].setTarget(ifnonnullTargets[i]);
    }


    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_iValueGet0() {
  private void createMethod_bValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.BYTE, 1), new Type[] { Type.INT }, new String[] { "arg0" }, "bValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.BYTE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);
    
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_bval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    il.append(_factory.createFieldAccess(_classPath, varname, new ArrayType(Type.BYTE, 1), Constants.GETFIELD));
    il.append(_factory.createReturn(Type.OBJECT));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(InstructionConstants.ACONST_NULL);
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.OBJECT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_iValueGet1() {
  private void createMethod_bLengthGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "bLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.BYTE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_1 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_1);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_blen" + i; //-----
   
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.INT));
    }

    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.INT));

    lookupswitch_1.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_1.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_bValueSet2() {
  private void createMethod_bValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ArrayType(Type.BYTE, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "bValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "checkMaxLen", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_6 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.BYTE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_7 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_7);
    
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    BranchInstruction[] goto0s = new BranchInstruction[n+1]; //-------
    InstructionHandle[] goto0Targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] ifnonnulls = new BranchInstruction[n+1]; //-------
    InstructionHandle[] ifnonnullTargets = new InstructionHandle[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_bval" + i; //-----
    String varname1 = "m_blen" + i; //-----

    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 2));
    targets[i] = ih_40; //----

        BranchInstruction ifnonnull_41 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_41);
    ifnonnulls[i] = ifnonnull_41; //------

    InstructionHandle ih_44 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_49 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_49);
    goto0s[i] = goto_49;

    InstructionHandle ih_52 = il.append(_factory.createLoad(Type.OBJECT, 0));
    ifnonnullTargets[i] = ih_52; //------

    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_57 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, varname, new ArrayType(Type.BYTE, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "copyVByte", new ArrayType(Type.BYTE, 1), new Type[] { new ArrayType(Type.BYTE, 1), new ArrayType(Type.BYTE, 1), Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createFieldAccess(_classPath, varname, new ArrayType(Type.BYTE, 1), Constants.PUTFIELD));
    InstructionHandle ih_71 = il.append(_factory.createLoad(Type.OBJECT, 0));
    goto0Targets[i] = ih_71;

    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, varname1, Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_76;
    BranchInstruction goto_76 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_76 = il.append(goto_76);
    gotos[i] = goto_76;
    } //-----

    InstructionHandle ih_157 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_161 = il.append(_factory.createReturn(Type.VOID));
    lookupswitch_7.setTarget(ih_157);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_7.setTarget(i, targets[i]);
    goto0s[i].setTarget(goto0Targets[i]);
    gotos[i].setTarget(ih_157);
    ifnonnulls[i].setTarget(ifnonnullTargets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_iValueGet3() {
  private void createMethod_oValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.OBJECT, new Type[] { Type.INT }, new String[] { "arg0" }, "oValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.OBJECT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_oval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.OBJECT));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(InstructionConstants.ACONST_NULL);
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.OBJECT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_iValueGet4() {
  private void createMethod_oValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1" }, "oValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.OBJECT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_oval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----

    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.OBJECT, Constants.PUTFIELD));
    InstructionHandle ih_45;
    BranchInstruction goto_45 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_45 = il.append(goto_45);
    gotos[i] = goto_45;
    } //-----
    
    InstructionHandle ih_64 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_68 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_64);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_68);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

   
  //////////////////////////////////////////////////////////////////////////////
  // From TupleTmpl.java  
  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT }, new String[] { "arg0" }, "isAttrNull", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IREM);
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_5 = il.append(new PUSH(_cp, 1l));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.LSHL);
    il.append(_factory.createStore(Type.LONG, 3));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_null0", Type.LONG, Constants.GETFIELD));
    il.append(_factory.createLoad(Type.LONG, 3));
    il.append(InstructionConstants.LAND);
    il.append(new PUSH(_cp, 0l));
    il.append(InstructionConstants.LCMP);
        BranchInstruction ifeq_17 = _factory.createBranchInstruction(Constants.IFEQ, null);
    il.append(ifeq_17);
    il.append(new PUSH(_cp, 1));
        BranchInstruction goto_21 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_21);
    InstructionHandle ih_24 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_25 = il.append(_factory.createReturn(Type.INT));
    ifeq_17.setTarget(ih_24);
    goto_21.setTarget(ih_25);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_2() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT }, new String[] { "arg0" }, "setAttrNull", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IREM);
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createFieldAccess(_classPath, "m_null0", Type.LONG, Constants.GETFIELD));
    il.append(new PUSH(_cp, 1l));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.LSHL);
    il.append(InstructionConstants.LOR);
    il.append(_factory.createFieldAccess(_classPath, "m_null0", Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_3() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT }, new String[] { "arg0" }, "setAttrbNullFalse", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "getNumAttrs", Type.INT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        BranchInstruction if_icmplt_5 = _factory.createBranchInstruction(Constants.IF_ICMPLT, null);
    il.append(if_icmplt_5);
    InstructionHandle ih_8 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_12 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(new PUSH(_cp, 64));
    il.append(InstructionConstants.IREM);
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_18 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createFieldAccess(_classPath, "m_null0", Type.LONG, Constants.GETFIELD));
    il.append(new PUSH(_cp, 1l));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.LSHL);
    il.append(new PUSH(_cp, -1l));
    il.append(InstructionConstants.LXOR);
    il.append(InstructionConstants.LAND);
    il.append(_factory.createFieldAccess(_classPath, "m_null0", Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_34 = il.append(_factory.createReturn(Type.VOID));
    if_icmplt_5.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_iValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "iValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.INT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_ival0", Type.INT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_iValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "iValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.INT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_ival0", Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_lValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "lValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGINT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0l));
    il.append(_factory.createReturn(Type.LONG));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_lval0", Type.LONG, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.LONG));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_lValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "lValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGINT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_lval0", Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_fValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.FLOAT, new Type[] { Type.INT }, new String[] { "arg0" }, "fValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.FLOAT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0.0f));
    il.append(_factory.createReturn(Type.FLOAT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_fval0", Type.FLOAT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.FLOAT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_fValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.FLOAT }, new String[] { "arg0", "arg1" }, "fValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.FLOAT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.FLOAT, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_fval0", Type.FLOAT, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_tValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "tValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.TIMESTAMP.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0l));
    il.append(_factory.createReturn(Type.LONG));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_tval0", Type.LONG, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.LONG));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_tValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "tValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.TIMESTAMP.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_tval0", Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_vValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "vValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.INTERVAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0l));
    il.append(_factory.createReturn(Type.LONG));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_vval0", Type.LONG, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.LONG));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_vValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "vValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.INTERVAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_vval0", Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }
  
  private void createMethod_vymValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT }, new String[] { "arg0" }, "vymValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.INTERVALYM.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0l));
    il.append(_factory.createReturn(Type.LONG));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_vval0", Type.LONG, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.LONG));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_vymValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.LONG }, new String[] { "arg0", "arg1" }, "vymValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.INTERVALYM.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.LONG, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_vval0", Type.LONG, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_cValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.CHAR, 1), new Type[] { Type.INT }, new String[] { "arg0" }, "cValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.CHAR.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_cval0", new ArrayType(Type.CHAR, 1), Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.OBJECT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_cLengthGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "cLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.CHAR.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_clen0", Type.INT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_cValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ArrayType(Type.CHAR, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "cValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "checkMaxLen", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_6 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.CHAR.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 2));
        BranchInstruction ifnonnull_13 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_13);
    InstructionHandle ih_16 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_21 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_21);
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_29 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_cval0", new ArrayType(Type.CHAR, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "copyVChar", new ArrayType(Type.CHAR, 1), new Type[] { new ArrayType(Type.CHAR, 1), new ArrayType(Type.CHAR, 1), Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createFieldAccess(_classPath, "m_cval0", new ArrayType(Type.CHAR, 1), Constants.PUTFIELD));
    InstructionHandle ih_43 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, "m_clen0", Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_48 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    ifnonnull_13.setTarget(ih_24);
    goto_21.setTarget(ih_43);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_bValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.BYTE, 1), new Type[] { Type.INT }, new String[] { "arg0" }, "bValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BYTE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_bval0", new ArrayType(Type.BYTE, 1), Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.OBJECT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_bLengthGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "bLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BYTE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_blen0", Type.INT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_bValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ArrayType(Type.BYTE, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "bValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "checkMaxLen", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_6 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BYTE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_10 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_10);
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_18 = il.append(_factory.createLoad(Type.OBJECT, 2));
        BranchInstruction ifnonnull_19 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_19);
    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_27 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_27);
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_35 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_bval0", new ArrayType(Type.BYTE, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "copyVByte", new ArrayType(Type.BYTE, 1), new Type[] { new ArrayType(Type.BYTE, 1), new ArrayType(Type.BYTE, 1), Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createFieldAccess(_classPath, "m_bval0", new ArrayType(Type.BYTE, 1), Constants.PUTFIELD));
    InstructionHandle ih_49 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, "m_blen0", Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_54 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_10.setTarget(ih_18);
    ifnonnull_19.setTarget(ih_30);
    goto_27.setTarget(ih_49);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_oValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.OBJECT, new Type[] { Type.INT }, new String[] { "arg0" }, "oValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.OBJECT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_oval0", Type.OBJECT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.OBJECT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_oValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1" }, "oValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.OBJECT.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_oval0", Type.OBJECT, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.CHAR, 1), new Type[] { Type.INT }, new String[] { "arg0" }, "xValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xlen0", Type.INT, Constants.GETFIELD));
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_18 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_18);
    InstructionHandle ih_21 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.GETFIELD));
        BranchInstruction ifnonnull_25 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_25);
    il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createInvoke("java.lang.Object", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.String", "toCharArray", new ArrayType(Type.CHAR, 1), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_41 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    InstructionHandle ih_51 = il.append(_factory.createReturn(Type.OBJECT));

    if_icmpeq_4.setTarget(ih_13);
    if_icmpne_18.setTarget(ih_41);
    ifnonnull_25.setTarget(ih_30);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();

  }

  private void createMethod_xLengthGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "xLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xlen0", Type.INT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ArrayType(Type.CHAR, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "xValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 2));
        BranchInstruction ifnonnull_13 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_13);
    InstructionHandle ih_16 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_21 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_21);
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_29 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "copyVChar", new ArrayType(Type.CHAR, 1), new Type[] { new ArrayType(Type.CHAR, 1), new ArrayType(Type.CHAR, 1), Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.PUTFIELD));
    InstructionHandle ih_43 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, "m_xlen0", Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_48 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    ifnonnull_13.setTarget(ih_24);
    goto_21.setTarget(ih_43);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.CHAR, 1), new Type[] { Type.INT }, new String[] { "arg0" }, "xValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String xval = "m_xval" + i; //-----
    String xlen = "m_xlen" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----

    il.append(_factory.createFieldAccess(_classPath, xlen, Type.INT, Constants.GETFIELD));
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_18 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_18);
    InstructionHandle ih_21 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, xval, Type.OBJECT, Constants.GETFIELD));
        BranchInstruction ifnonnull_25 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_25);
    il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, xval, Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createInvoke("java.lang.Object", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.String", "toCharArray", new ArrayType(Type.CHAR, 1), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_41 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, xval, Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    InstructionHandle ih_51 = il.append(_factory.createReturn(Type.OBJECT));

    if_icmpne_18.setTarget(ih_41);
    ifnonnull_25.setTarget(ih_30);
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(InstructionConstants.ACONST_NULL);
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.OBJECT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_38() {
  private void createMethod_xLengthGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "xLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_xlen" + i; //-----
   
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.INT));
    }

    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.INT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_39() {
  private void createMethod_xValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ArrayType(Type.CHAR, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "xValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    BranchInstruction[] goto0s = new BranchInstruction[n+1]; //-------
    InstructionHandle[] goto0Targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] ifnonnulls = new BranchInstruction[n+1]; //-------
    InstructionHandle[] ifnonnullTargets = new InstructionHandle[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_xval" + i; //-----
    String varname1 = "m_xlen" + i; //-----
   

    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 2));
    targets[i] = ih_40; //----

        BranchInstruction ifnonnull_41 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_41);
    ifnonnulls[i] = ifnonnull_41; //------
    
    InstructionHandle ih_44 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_49 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_49);
    goto0s[i] = goto_49;

    InstructionHandle ih_52 = il.append(_factory.createLoad(Type.OBJECT, 0));
    ifnonnullTargets[i] = ih_52; //------

    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "copyVChar", new ArrayType(Type.CHAR, 1), new Type[] { new ArrayType(Type.CHAR, 1), new ArrayType(Type.CHAR, 1), Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.OBJECT, Constants.PUTFIELD));
    InstructionHandle ih_66 = il.append(_factory.createLoad(Type.OBJECT, 0));
    goto0Targets[i] = ih_66;
    
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, varname1, Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_71;
    BranchInstruction goto_71 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_71 = il.append(goto_71);
    gotos[i] = goto_71;
    } //-----
    

    InstructionHandle ih_153 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_157 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_153);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    goto0s[i].setTarget(goto0Targets[i]);
    gotos[i].setTarget(ih_157);
    ifnonnulls[i].setTarget(ifnonnullTargets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void create_boolValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT }, new String[] { "arg0" }, "boolValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BOOLEAN.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_boolval0", Type.BOOLEAN, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void create_boolValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.BOOLEAN }, new String[] { "arg0", "arg1" }, "boolValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BOOLEAN.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_boolval0", Type.BOOLEAN, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void create_boolValueGet2(int n) 
  {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT }, new String[] { "arg0" }, "boolValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));
    
    int[] positions = _typeInfos[Datatype.BOOLEAN.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_boolval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    
    il.append(_factory.createFieldAccess(_classPath, varname, Type.BOOLEAN, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.INT));
    } //-----
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.INT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void create_boolValueSet2(int n) 
  {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.BOOLEAN }, new String[] { "arg0", "arg1" }, "boolValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.BOOLEAN.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_boolval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.BOOLEAN, Constants.PUTFIELD));
    InstructionHandle ih_45;
    BranchInstruction goto_45 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_45 = il.append(goto_45);
    gotos[i] = goto_45;
    } //-----
    
    InstructionHandle ih_64 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_68 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_64);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_68);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xObjValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1" }, "xValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 2));
        BranchInstruction ifnonnull_13 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_13);
    InstructionHandle ih_16 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_21 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_21);
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_29 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.PUTFIELD));
    InstructionHandle ih_34 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(new PUSH(_cp, -1));
    il.append(_factory.createFieldAccess(_classPath, "m_xlen0", Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_39 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    ifnonnull_13.setTarget(ih_24);
    goto_21.setTarget(ih_34);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xObjValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.OBJECT, new Type[] { Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1" }, "getItem", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xlen0", Type.INT, Constants.GETFIELD));
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_18 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_18);
    InstructionHandle ih_21 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_26 = il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xval0", Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xlen0", Type.INT, Constants.GETFIELD));
    il.append(_factory.createInvoke("oracle.cep.dataStructures.internal.memory.XmltypeAttrVal", "parseNode", Type.OBJECT, new Type[] { Type.OBJECT, new ArrayType(Type.CHAR, 1), Type.INT }, Constants.INVOKESTATIC));
    InstructionHandle ih_44 = il.append(_factory.createReturn(Type.OBJECT));
    if_icmpeq_4.setTarget(ih_13);
    if_icmpne_18.setTarget(ih_26);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xIsObj() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT }, new String[] { "arg0" }, "xIsObj", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xlen0", Type.INT, Constants.GETFIELD));
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_18 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_18);
    il.append(new PUSH(_cp, 1));
        BranchInstruction goto_22 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_22);
    InstructionHandle ih_25 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_26 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    if_icmpne_18.setTarget(ih_25);
    goto_22.setTarget(ih_26);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xObjValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1"}, "xValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    BranchInstruction[] goto0s = new BranchInstruction[n+1]; //-------
    InstructionHandle[] goto0Targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] ifnonnulls = new BranchInstruction[n+1]; //-------
    InstructionHandle[] ifnonnullTargets = new InstructionHandle[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_xval" + i; //-----
    String varname1 = "m_xlen" + i; //-----
   

    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 2));
    targets[i] = ih_40; //----

        BranchInstruction ifnonnull_41 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_41);
    ifnonnulls[i] = ifnonnull_41; //------
    
    InstructionHandle ih_44 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_49 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_49);
    goto0s[i] = goto_49;

    InstructionHandle ih_52 = il.append(_factory.createLoad(Type.OBJECT, 0));
    ifnonnullTargets[i] = ih_52; //------

    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.OBJECT, Constants.PUTFIELD));

    InstructionHandle ih_66 = il.append(_factory.createLoad(Type.OBJECT, 0));
    goto0Targets[i] = ih_66;
    
    il.append(new PUSH(_cp, -1));
    il.append(_factory.createFieldAccess(_classPath, varname1, Type.INT, Constants.PUTFIELD));

    InstructionHandle ih_71;
    BranchInstruction goto_71 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_71 = il.append(goto_71);
    gotos[i] = goto_71;
    } //-----
    

    InstructionHandle ih_153 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_157 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_153);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    goto0s[i].setTarget(goto0Targets[i]);
    gotos[i].setTarget(ih_157);
    ifnonnulls[i].setTarget(ifnonnullTargets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xObjValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.OBJECT, new Type[] { Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1" }, "getItem", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_xval" + i; //-----
    String varname1 = "m_xlen" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----

    il.append(_factory.createFieldAccess(_classPath, varname1, Type.INT, Constants.GETFIELD));
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_41 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_41);
    InstructionHandle ih_44 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_49 = il.append(_factory.createLoad(Type.OBJECT, 2));
    if_icmpne_41.setTarget(ih_49);
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.OBJECT, Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, varname1, Type.INT, Constants.GETFIELD));
    il.append(_factory.createInvoke("oracle.cep.dataStructures.internal.memory.XmltypeAttrVal", "parseNode", Type.OBJECT, new Type[] { Type.OBJECT, new ArrayType(Type.CHAR, 1), Type.INT }, Constants.INVOKESTATIC));
    il.append(_factory.createReturn(Type.OBJECT));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(InstructionConstants.ACONST_NULL);
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.OBJECT));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_dValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.DOUBLE, new Type[] { Type.INT }, new String[] { "arg0" }, "dValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.DOUBLE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0.0));
    il.append(_factory.createReturn(Type.DOUBLE));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_dval0", Type.DOUBLE, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.DOUBLE));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_dValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.DOUBLE }, new String[] { "arg0", "arg1" }, "dValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.DOUBLE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.DOUBLE, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_dval0", Type.DOUBLE, Constants.PUTFIELD));
    InstructionHandle ih_22 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_dValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.DOUBLE, new Type[] { Type.INT }, new String[] { "arg0" }, "dValueGet", _classPath, il, _cp);

    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.DOUBLE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_23);
    
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_dval" + i; //-----
   
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_56; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.DOUBLE, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.DOUBLE));
    } //-----

    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0.0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.DOUBLE));

    lookupswitch_23.setTarget(ih_51);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

//  private void createMethod_32() {
  private void createMethod_dValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.DOUBLE }, new String[] { "arg0", "arg1" }, "dValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.DOUBLE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_6 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_6);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_dval" + i; //-----
   
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_40; //----

    il.append(_factory.createLoad(Type.DOUBLE, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, Type.DOUBLE, Constants.PUTFIELD));
    InstructionHandle ih_46;
    BranchInstruction goto_46 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_46 = il.append(goto_46);
    gotos[i] = goto_46;
    } //-----
    
    InstructionHandle ih_67 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_71 = il.append(_factory.createReturn(Type.VOID));

    lookupswitch_6.setTarget(ih_67);

    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_6.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_71);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_xIsObj2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT }, new String[] { "arg0" }, "xIsObj", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.XMLTYPE.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_23);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_xlen" + i; //-----

    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.GETFIELD));
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_41 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_41);
    il.append(new PUSH(_cp, 1));
        BranchInstruction goto_45 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_45);
    InstructionHandle ih_48 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_49 = il.append(_factory.createReturn(Type.INT));
    goto_45.setTarget(ih_49);
    if_icmpne_41.setTarget(ih_48);
    } //-----
    

    InstructionHandle ih_153 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));

    lookupswitch_23.setTarget(ih_153);
    for (int i = 0; i < n; i++) //------
    { //-----
      lookupswitch_23.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nValueGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ObjectType("java.math.BigDecimal"), new Type[] { Type.INT }, new String[] { "arg0" }, "nValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_nval0", new ObjectType("java.math.BigDecimal"), Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.OBJECT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nPrecisionGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "nPrecisionGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_precision0", Type.INT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nScaleGet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "nScaleGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_scale0", Type.INT, Constants.GETFIELD));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    if_icmpeq_4.setTarget(ih_13);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nValueSet() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ObjectType("java.math.BigDecimal"), Type.INT, Type.INT }, new String[] { "arg0", "arg1", "arg2", "arg3" }, "nValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    int pos = positions[0];
    il.append(new PUSH(_cp, pos));
        BranchInstruction if_icmpeq_4 = _factory.createBranchInstruction(Constants.IF_ICMPEQ, null);
    il.append(if_icmpeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 2));
        BranchInstruction ifnonnull_13 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_13);
    InstructionHandle ih_16 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_21 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_21);
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_29 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess(_classPath, "m_nval0", new ObjectType("java.math.BigDecimal"), Constants.PUTFIELD));
    InstructionHandle ih_34 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, "m_precision0", Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_39 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createFieldAccess(_classPath, "m_scale0", Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_45 = il.append(_factory.createReturn(Type.VOID));
    if_icmpeq_4.setTarget(ih_12);
    ifnonnull_13.setTarget(ih_24);
    goto_21.setTarget(ih_45);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nValueGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ObjectType("java.math.BigDecimal"), new Type[] { Type.INT }, new String[] { "arg0" }, "nValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));

    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_23 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_23);
    
    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_nval" + i; //-----
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----
    il.append(_factory.createFieldAccess(_classPath, varname, new ObjectType("java.math.BigDecimal"), Constants.GETFIELD));
    il.append(_factory.createReturn(Type.OBJECT));
    }

    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(InstructionConstants.ACONST_NULL);
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.OBJECT));
    lookupswitch_23.setTarget(ih_51);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_23.setTarget(i, targets[i]);
    }


    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nPrecisionGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "nPrecisionGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_1 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_1);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_precision" + i; //-----
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.INT));
    }

    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.INT));
    lookupswitch_1.setTarget(ih_51);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_1.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nScaleGet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT }, new String[] { "arg0" }, "nScaleGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_1 = new LOOKUPSWITCH(positions, ihandles, null); //-----
    il.append(lookupswitch_1);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_scale" + i; //-----
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_36; //----
    il.append(_factory.createFieldAccess(_classPath, varname, Type.INT, Constants.GETFIELD));
    il.append(_factory.createReturn(Type.INT));
    }
    
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_55 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_56 = il.append(_factory.createReturn(Type.INT));
    lookupswitch_1.setTarget(ih_51);
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_1.setTarget(i, targets[i]);
    }

    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_nValueSet2(int n) {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, new ObjectType("java.math.BigDecimal"), Type.INT, Type.INT }, new String[] { "arg0", "arg1", "arg2", "arg3" }, "nValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 2));
        BranchInstruction ifnonnull_1 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_1);
    InstructionHandle ih_4 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrNull", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        BranchInstruction goto_9 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_9);
    
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.INT, 1));
    int[] positions = _typeInfos[Datatype.BIGDECIMAL.ordinal()].getPositions(); //-----
    assert (positions != null); //-----
    InstructionHandle[] ihandles = new InstructionHandle[positions.length]; //-----
        Select lookupswitch_13 = new LOOKUPSWITCH(positions, ihandles, null); //-----

    il.append(lookupswitch_13);

    InstructionHandle[] targets = new InstructionHandle[n+1]; //------
    BranchInstruction[] gotos = new BranchInstruction[n+1]; //-------
    for (int i = 0; i < n; i++) //------
    { //-----
    String varname = "m_nval" + i; //-----
    String pvarname = "m_precision" + i; //-----
    String svarname = "m_scale" + i; //-----
   
    InstructionHandle ih_48 = il.append(_factory.createLoad(Type.OBJECT, 0));
    targets[i] = ih_48; //----

    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke(_classPath, "setAttrbNullFalse", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_53 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess(_classPath, varname, new ObjectType("java.math.BigDecimal"), Constants.PUTFIELD));
    InstructionHandle ih_58 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createFieldAccess(_classPath, pvarname, Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_63 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createFieldAccess(_classPath, svarname, Type.INT, Constants.PUTFIELD));
    InstructionHandle ih_69;
    BranchInstruction goto_69 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_69 = il.append(goto_69);
    gotos[i] = goto_69;
    }

    InstructionHandle ih_120 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_classPath, "throwInvalidAttr", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));

    InstructionHandle ih_124 = il.append(_factory.createReturn(Type.VOID));
    ifnonnull_1.setTarget(ih_12);
    goto_9.setTarget(ih_124);
    lookupswitch_13.setTarget(ih_120);
    
    for (int i = 0; i < n; i++) //------
    { //-----
    lookupswitch_13.setTarget(i, targets[i]);
    gotos[i].setTarget(ih_124);
    }
    
    
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

}

