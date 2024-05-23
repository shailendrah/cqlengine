/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/PageClassGen.java /main/9 2010/10/27 23:23:49 sborah Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    This class generates the Page class dynamicall using BCEL.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    This class is based on the template class using BCELifier
    The template class is in test/src/PageTmpl.java.
    The following is how to use BCELifer:  
    cd test/src
    rm PageTmpl.class
    javac -source 1.4 -target 1.4 -classpath $ADE_VIEW_ROOT/pcbpel/cep/build/classes PageTmpl.java
    java -classpath $ADE_VIEW_ROOT/j2ee/home_image/generated/j2ee/home/lib/bcel.jar org.apache.bcel.util.BCELifier PageTmpl.class | tee PageTmplGen.java
    replace all  "oracle.cep.memmgr.PageTmpl" _classPath
    replace all "oracle.cep.memmgr.PageBase" _baseClassPath
    copy createMethod2 ~ 
    make sure createMethodXX matches with type in createFieldsMethods
    
   MODIFIED    (MM/DD/YY)
    hopark      10/24/10 - fix no of attrib limit
    hopark      05/16/08 - add xIsObj
    hopark      02/10/08 - object representation of xml
    hopark      02/04/08 - support double type
    hopark      12/28/07 - add xmltype
    hopark      11/27/07 - add boolean type
    hopark      11/16/07 - xquery support
    hopark      10/24/07 - optimize
    hopark      10/04/07 - add externalizable
    hopark      07/20/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/PageClassGen.java /main/9 2010/10/27 23:23:49 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class PageClassGen extends ClassGenBase 
{
  private PageLayout         _layout;
  private int                _nObjs;
  
  public PageClassGen(String baseClassPath, String className, PageLayout layout, int objs) 
  {
    super(baseClassPath,
         "oracle.cep.memmgr",
         "class$oracle$cep$memmgr",
         className);
         
    _layout = layout;
    _nObjs = objs;
  }

  protected void createFieldsMethods()
  {
    createFields();
    
    // create Constructor
    createMethod_00();
    createMethod_0();
    
    // create accessors
    short[] typeUsages = _layout.getTypeUsages();
    for (byte type = 0; type < typeUsages.length; type++)
    {
      short t = typeUsages[type];
      if (t <= 0)
        continue;
      switch(type) 
      {
      case PageLayout.INT:
        /* @iValueGet@ */ createMethod_2();
        /* @iValueSet@ */ createMethod_3();
        /* @getIntAttrs@ */ createMethod_36();
        /* @setIntAttrs@ */ createMethod_48();
        break;
      case PageLayout.LONG:
        /* @lValueGet@ */ createMethod_4();
        /* @lValueSet@ */ createMethod_5();
        /* @getLongAttrs@ */ createMethod_37();
        /* @setLongAttrs@ */ createMethod_49();
        break;
      case PageLayout.FLOAT:
        /* @fValueGet@ */ createMethod_6();
        /* @fValueSet@ */ createMethod_7();
        /* @getFloatAttrs@ */ createMethod_38();
        /* @setFloatAttrs@ */ createMethod_50();
        break;
      case PageLayout.DOUBLE:
        /* @dValueGet@ */ createMethod_30();
        /* @dValueSet@ */ createMethod_31();
        /* @getDoubleAttrs@ */ createMethod_39();
        /* @setDoubleAttrs@ */ createMethod_51();
        break;
      case PageLayout.TIME:
        /* @tValueGet@ */ createMethod_8();
        /* @tValueSet@ */ createMethod_9();
        /* @getTimeAttrs@ */ createMethod_40();
        /* @setTimeAttrs@ */ createMethod_52();
        break;
      case PageLayout.INTERVAL:
        /* @vValueGet@ */ createMethod_11();
        /* @vValueSet@ */ createMethod_10();
        /* @getIntervalAttrs@ */ createMethod_41();
        /* @setIntervalAttrs@ */ createMethod_53();
        break;
      case PageLayout.VCHAR:
        /* @cLengthGet@ */ createMethod_13();
        /* @cValueGet@ */ createMethod_12();
        /* @cLengthSet@ */ createMethod_14();
        /* @cValueSet@ */ createMethod_15();
        /* @getCharAttrs@ */ createMethod_42();
        /* @setCharAttrs@ */ createMethod_54();
        break;
      case PageLayout.VBYTE:
        /* @bLengthGet@ */ createMethod_17();
        /* @bValueGet@ */ createMethod_16();
        /* @bLengthSet@ */ createMethod_18();
        /* @bValueSet@ */ createMethod_19();
        /* @getByteAttrs@ */ createMethod_43();
        /* @setByteAttrs@ */ createMethod_55();
        break;
      case PageLayout.OBJ:
        /* @oValueGet@ */ createMethod_20();
        /* @oValueSet@ */ createMethod_21();
        /* @getObjAttrs@ */ createMethod_44();
        /* @setObjAttrs@ */ createMethod_56();
        break;
      case PageLayout.XML:
        /* @xLengthGet@ */ createMethod_22();
        /* @xValueGet@ */ createMethod_24();
        /* @xLengthSet@ */ createMethod_23();
        /* @xValueSet@ */ createMethod_25();
        /* @xObjValueGet@ */ createMethod_26();
        /* @xObjValueSet@ */ createMethod_27();
        /* @getXMLAttrs@ */ createMethod_45();
        /* @setXMLAttrs@ */ createMethod_57();
        /* @xIsObj@ */ createMethod_60();
        break;
      case PageLayout.BOOLEAN:
        /* @boolValueGet@ */ createMethod_28();
        /* @boolValueSet@ */ createMethod_29();
        /* @getBoolAttrs@ */ createMethod_46();
        /* @setBoolAttrs@ */ createMethod_58();
        break;
      case PageLayout.BIGDECIMAL:
        /* @nValueGet@ */ createMethod_32();
        /* @nPrecisionGet@ */ createMethod_33();
        /* @nScaleGet@ */ createMethod_34();
        /* @nValueSet@ */ createMethod_35();
        /* @getNumberAttrs@ */ createMethod_47();
        /* @setNumberAttrs@ */ createMethod_59();
        break;
      }
    }
    
  }

  private void createFields()
  {
    FieldGen field;

    short[] typeUsages = _layout.getTypeUsages();
    for (byte type = 0; type < typeUsages.length; type++)
    {
      short t = typeUsages[type];
      if (t <= 0)
        continue;
      int noprims = _nObjs * t;
      switch(type) 
      {
      case PageLayout.INT:
        field = new FieldGen(0, new ArrayType(Type.INT, 1), "m_intAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.LONG:
        field = new FieldGen(0, new ArrayType(Type.LONG, 1), "m_longAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.FLOAT:
        field = new FieldGen(0, new ArrayType(Type.FLOAT, 1), "m_floatAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.DOUBLE:
        field = new FieldGen(0, new ArrayType(Type.DOUBLE, 1), "m_doubleAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.TIME:
        field = new FieldGen(0, new ArrayType(Type.LONG, 1), "m_timeAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.INTERVAL:
        field = new FieldGen(0, new ArrayType(Type.LONG, 1), "m_intervalAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.VCHAR:
        field = new FieldGen(0, new ArrayType(Type.CHAR, 2), "m_charAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.VBYTE:
        field = new FieldGen(0, new ArrayType(Type.BYTE, 2), "m_byteAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.OBJ:
        field = new FieldGen(0, new ArrayType(Type.OBJECT, 1), "m_objAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.XML:
        field = new FieldGen(0, new ArrayType(Type.OBJECT, 1), "m_xmlAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.BOOLEAN:
        field = new FieldGen(0, new ArrayType(Type.BOOLEAN, 1), "m_boolAttrs", _cp);
        _cg.addField(field.getField());
        break;
      case PageLayout.BIGDECIMAL:
        field = new FieldGen(0, new ArrayType(new ObjectType("java.math.BigDecimal"), 1), "m_numberAttrs", _cp);
        _cg.addField(field.getField());
        break;
      }
    }
  }    

  private void createMethod_00() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "oracle.cep.memmgr.PageTmpl", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(_baseClassPath, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));     InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }
  
  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, new ObjectType("oracle.cep.memmgr.PageLayout") }, new String[] { "arg0", "arg1", "arg2" }, "<init>", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke(_baseClassPath, "<init>", Type.VOID, new Type[] { Type.INT, Type.INT, new ObjectType("oracle.cep.memmgr.PageLayout") }, Constants.INVOKESPECIAL));

    short[] typeUsages = _layout.getTypeUsages();
    for (byte type = 0; type < typeUsages.length; type++)
    {
      short t = typeUsages[type];
      if (t <= 0)
        continue;
      int noprims = _nObjs * t;
      switch(type) 
      {
      case PageLayout.INT:
        InstructionHandle ih_6 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.INT, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.PUTFIELD));
        break;
      case PageLayout.LONG:
        InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.LONG, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_longAttrs", new ArrayType(Type.LONG, 1), Constants.PUTFIELD));
        break;
      case PageLayout.FLOAT:
        InstructionHandle ih_23 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.FLOAT, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_floatAttrs", new ArrayType(Type.FLOAT, 1), Constants.PUTFIELD));
        break;
      case PageLayout.DOUBLE:
        InstructionHandle ih_23d = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.DOUBLE, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_doubleAttrs", new ArrayType(Type.DOUBLE, 1), Constants.PUTFIELD));
        break;
      case PageLayout.TIME:
        InstructionHandle ih_32 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.LONG, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_timeAttrs", new ArrayType(Type.LONG, 1), Constants.PUTFIELD));
        break;
      case PageLayout.INTERVAL:
        InstructionHandle ih_41 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.LONG, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_intervalAttrs", new ArrayType(Type.LONG, 1), Constants.PUTFIELD));
        break;
      case PageLayout.VCHAR:
        InstructionHandle ih_50 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(new ArrayType(Type.CHAR, 1), (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_charAttrs", new ArrayType(Type.CHAR, 2), Constants.PUTFIELD));
        break;
      case PageLayout.VBYTE:
        InstructionHandle ih_60 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(new ArrayType(Type.BYTE, 1), (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_byteAttrs", new ArrayType(Type.BYTE, 2), Constants.PUTFIELD));
        break;
      case PageLayout.XML:
        InstructionHandle ih_xml = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.OBJECT, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.PUTFIELD));
        break;
      case PageLayout.OBJ:
        InstructionHandle ih_70 = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.OBJECT, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_objAttrs", new ArrayType(Type.OBJECT, 1), Constants.PUTFIELD));
        break;
      case PageLayout.BOOLEAN:
        InstructionHandle ih_bool = il.append(_factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(_cp, noprims));
        il.append(_factory.createNewArray(Type.BOOLEAN, (short) 1));
        il.append(_factory.createFieldAccess(_classPath, "m_boolAttrs", new ArrayType(Type.BOOLEAN, 1), Constants.PUTFIELD));
        break;
      case PageLayout.BIGDECIMAL:
       InstructionHandle ih_number = il.append(_factory.createLoad(Type.OBJECT, 0));
       il.append(new PUSH(_cp, noprims));
       il.append(_factory.createNewArray(new ObjectType("java.math.BigDecimal"), (short) 1));
       il.append(_factory.createFieldAccess(_classPath, "m_numberAttrs", new ArrayType(new ObjectType("java.math.BigDecimal"), 1), Constants.PUTFIELD));
       break;         
     
      }
    }

    InstructionHandle ih_80 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  /* XXXXXX Begin - Copy from template */
  private void createMethod_2() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "iValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_3() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "iValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_4() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "lValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_longAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.LALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.LONG));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_5() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.LONG }, new String[] { "arg0", "arg1", "arg2" }, "lValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_longAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.LONG, 3));
    il.append(InstructionConstants.LASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_6() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.FLOAT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "fValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_floatAttrs", new ArrayType(Type.FLOAT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.FALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.FLOAT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_7() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.FLOAT }, new String[] { "arg0", "arg1", "arg2" }, "fValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_floatAttrs", new ArrayType(Type.FLOAT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createLoad(Type.FLOAT, 3));
    il.append(InstructionConstants.FASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_8() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "tValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_timeAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.LALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.LONG));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_9() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.LONG }, new String[] { "arg0", "arg1", "arg2" }, "tValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_timeAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.LONG, 3));
    il.append(InstructionConstants.LASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_10() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.LONG }, new String[] { "arg0", "arg1", "arg2" }, "vValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intervalAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.LONG, 3));
    il.append(InstructionConstants.LASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_11() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.LONG, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "vValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intervalAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.LALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.LONG));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_12() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.CHAR, 1), new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "cValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_charAttrs", new ArrayType(Type.CHAR, 2), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.AALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_13() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "cLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IALOAD);
    InstructionHandle ih_31 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_14() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "cLengthSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_10 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_26 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_34 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_15() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, new ArrayType(Type.CHAR, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2", "arg3" }, "cValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createInvoke(_classPath, "checkMaxLen", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_charAttrs", new ArrayType(Type.CHAR, 2), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(InstructionConstants.AASTORE);
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 6));
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 6));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_65 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_16() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.BYTE, 1), new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "bValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_byteAttrs", new ArrayType(Type.BYTE, 2), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.AALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_17() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "bLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IALOAD);
    InstructionHandle ih_31 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_18() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "bLengthSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_10 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_26 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_34 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_19() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, new ArrayType(Type.BYTE, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2", "arg3" }, "bValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createInvoke(_classPath, "checkMaxLen", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_byteAttrs", new ArrayType(Type.BYTE, 2), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(InstructionConstants.AASTORE);
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 6));
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 6));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_65 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_20() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.OBJECT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "oValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_objAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.AALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_21() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1", "arg2" }, "oValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_objAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(InstructionConstants.AASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_22() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "xLengthGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IALOAD);
    InstructionHandle ih_31 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_23() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.INT }, new String[] { "arg0", "arg1", "arg2" }, "xLengthSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_10 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_26 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_34 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_24() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.CHAR, 1), new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "xValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(InstructionConstants.IALOAD);
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_48 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_48);
    InstructionHandle ih_51 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.AALOAD);
    il.append(_factory.createStore(Type.OBJECT, 6));
    InstructionHandle ih_59 = il.append(_factory.createLoad(Type.OBJECT, 6));
        BranchInstruction ifnonnull_61 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_61);
    il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_66 = il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(_factory.createInvoke("java.lang.Object", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.String", "toCharArray", new ArrayType(Type.CHAR, 1), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_75 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.AALOAD);
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    InstructionHandle ih_87 = il.append(_factory.createReturn(Type.OBJECT));
    if_icmpne_48.setTarget(ih_75);
    ifnonnull_61.setTarget(ih_66);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_25() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, new ArrayType(Type.CHAR, 1), Type.INT }, new String[] { "arg0", "arg1", "arg2", "arg3" }, "xValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createInvoke(_classPath, "checkMaxLen", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(InstructionConstants.AASTORE);
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 6));
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 6));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_56 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_65 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_26() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.OBJECT, new Type[] { Type.INT, Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1", "arg2" }, "xObjValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.AALOAD);
    il.append(_factory.createStore(Type.OBJECT, 5));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 6));
    InstructionHandle ih_34 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 6));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_50 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IALOAD);
    il.append(_factory.createStore(Type.INT, 7));
    InstructionHandle ih_59 = il.append(_factory.createLoad(Type.INT, 7));
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_62 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_62);
    InstructionHandle ih_65 = il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_68 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createCheckCast(new ArrayType(Type.CHAR, 1)));
    il.append(_factory.createLoad(Type.INT, 7));
    il.append(_factory.createInvoke(_classPath, "parseNode", Type.OBJECT, new Type[] { Type.OBJECT, new ArrayType(Type.CHAR, 1), Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_83 = il.append(_factory.createReturn(Type.OBJECT));
    if_icmpne_62.setTarget(ih_68);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_27() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.OBJECT }, new String[] { "arg0", "arg1", "arg2" }, "xObjValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(InstructionConstants.AASTORE);
    InstructionHandle ih_23 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_33 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_49 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(new PUSH(_cp, -1));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_57 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_28() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "boolValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_boolAttrs", new ArrayType(Type.BOOLEAN, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.BALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_29() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.BOOLEAN }, new String[] { "arg0", "arg1", "arg2" }, "boolValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_boolAttrs", new ArrayType(Type.BOOLEAN, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.BASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_30() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.DOUBLE, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "dValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_doubleAttrs", new ArrayType(Type.DOUBLE, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.DALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.DOUBLE));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_31() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, Type.DOUBLE }, new String[] { "arg0", "arg1", "arg2" }, "dValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 5));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_doubleAttrs", new ArrayType(Type.DOUBLE, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createLoad(Type.DOUBLE, 3));
    il.append(InstructionConstants.DASTORE);
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_32() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ObjectType("java.math.BigDecimal"), new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "nValueGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_numberAttrs", new ArrayType(new ObjectType("java.math.BigDecimal"), 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.AALOAD);
    InstructionHandle ih_20 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_33() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "nPrecisionGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IALOAD);
    InstructionHandle ih_31 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_34() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.INT, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "nScaleGet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLength2Pos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IALOAD);
    InstructionHandle ih_31 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_35() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.INT, Type.INT, new ObjectType("java.math.BigDecimal"), Type.INT, Type.INT }, new String[] { "arg0", "arg1", "arg2", "arg3", "arg4" }, "nValueSet", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createNew("java.math.BigDecimal"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("java.math.BigDecimal", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createNew("java.math.MathContext"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createInvoke("java.math.MathContext", "<init>", Type.VOID, new Type[] { Type.INT }, Constants.INVOKESPECIAL));
    il.append(_factory.createInvoke("java.math.BigDecimal", "<init>", Type.VOID, new Type[] { Type.STRING, new ObjectType("java.math.MathContext") }, Constants.INVOKESPECIAL));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(_factory.createFieldAccess("java.math.RoundingMode", "HALF_UP", new ObjectType("java.math.RoundingMode"), Constants.GETSTATIC));
    il.append(_factory.createInvoke("java.math.BigDecimal", "setScale", new ObjectType("java.math.BigDecimal"), new Type[] { Type.INT, new ObjectType("java.math.RoundingMode") }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 6));
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(_factory.createInvoke("java.math.BigDecimal", "precision", Type.INT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.INT, 4));
        BranchInstruction if_icmple_37 = _factory.createBranchInstruction(Constants.IF_ICMPLE, null);
    il.append(if_icmple_37);
    InstructionHandle ih_40 = il.append(_factory.createNew("oracle.cep.execution.ExecException"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createFieldAccess("oracle.cep.exceptions.ExecutionError", "PRECISION_ERROR", new ObjectType("oracle.cep.exceptions.ExecutionError"), Constants.GETSTATIC));
    il.append(new PUSH(_cp, 2));
    il.append(_factory.createNewArray(Type.OBJECT, (short) 1));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, 0));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("java.math.BigDecimal", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(InstructionConstants.AASTORE);
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, 1));
    il.append(_factory.createNew("java.lang.Integer"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(_factory.createInvoke("java.lang.Integer", "<init>", Type.VOID, new Type[] { Type.INT }, Constants.INVOKESPECIAL));
    il.append(InstructionConstants.AASTORE);
    il.append(_factory.createInvoke("oracle.cep.execution.ExecException", "<init>", Type.VOID, new Type[] { new ObjectType("oracle.cep.exceptions.ExecutionError"), new ArrayType(Type.OBJECT, 1) }, Constants.INVOKESPECIAL));
    il.append(InstructionConstants.ATHROW);
    InstructionHandle ih_74 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 7));
    InstructionHandle ih_89 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_numberAttrs", new ArrayType(new ObjectType("java.math.BigDecimal"), 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 7));
    il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(InstructionConstants.AASTORE);
    InstructionHandle ih_98 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 8));
    InstructionHandle ih_108 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 8));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 7));
    InstructionHandle ih_124 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 7));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_133 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLength2Pos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 8));
    InstructionHandle ih_143 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 8));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 7));
    InstructionHandle ih_159 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 7));
    il.append(_factory.createLoad(Type.INT, 5));
    il.append(InstructionConstants.IASTORE);
    InstructionHandle ih_168 = il.append(_factory.createReturn(Type.VOID));
    if_icmple_37.setTarget(ih_74);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_36() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.INT, 1), Type.NO_ARGS, new String[] {  }, "getIntAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_37() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.LONG, 1), Type.NO_ARGS, new String[] {  }, "getLongAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_longAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_38() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.FLOAT, 1), Type.NO_ARGS, new String[] {  }, "getFloatAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_floatAttrs", new ArrayType(Type.FLOAT, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_39() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.DOUBLE, 1), Type.NO_ARGS, new String[] {  }, "getDoubleAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_doubleAttrs", new ArrayType(Type.DOUBLE, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_40() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.LONG, 1), Type.NO_ARGS, new String[] {  }, "getTimeAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_timeAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_41() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.LONG, 1), Type.NO_ARGS, new String[] {  }, "getIntervalAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intervalAttrs", new ArrayType(Type.LONG, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_42() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.CHAR, 2), Type.NO_ARGS, new String[] {  }, "getCharAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_charAttrs", new ArrayType(Type.CHAR, 2), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_43() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.BYTE, 2), Type.NO_ARGS, new String[] {  }, "getByteAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_byteAttrs", new ArrayType(Type.BYTE, 2), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_44() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.OBJECT, 1), Type.NO_ARGS, new String[] {  }, "getObjAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_objAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_45() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.OBJECT, 1), Type.NO_ARGS, new String[] {  }, "getXMLAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_46() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(Type.BOOLEAN, 1), Type.NO_ARGS, new String[] {  }, "getBoolAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_boolAttrs", new ArrayType(Type.BOOLEAN, 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_47() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ArrayType(new ObjectType("java.math.BigDecimal"), 1), Type.NO_ARGS, new String[] {  }, "getNumberAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_numberAttrs", new ArrayType(new ObjectType("java.math.BigDecimal"), 1), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_48() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.INT, 1) }, new String[] { "arg0" }, "setIntAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_49() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.LONG, 1) }, new String[] { "arg0" }, "setLongAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_longAttrs", new ArrayType(Type.LONG, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_50() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.FLOAT, 1) }, new String[] { "arg0" }, "setFloatAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_floatAttrs", new ArrayType(Type.FLOAT, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_51() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.DOUBLE, 1) }, new String[] { "arg0" }, "setDoubleAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_doubleAttrs", new ArrayType(Type.DOUBLE, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_52() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.LONG, 1) }, new String[] { "arg0" }, "setTimeAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_timeAttrs", new ArrayType(Type.LONG, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_53() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.LONG, 1) }, new String[] { "arg0" }, "setIntervalAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_intervalAttrs", new ArrayType(Type.LONG, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_54() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.CHAR, 2) }, new String[] { "arg0" }, "setCharAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_charAttrs", new ArrayType(Type.CHAR, 2), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_55() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.BYTE, 2) }, new String[] { "arg0" }, "setByteAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_byteAttrs", new ArrayType(Type.BYTE, 2), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_56() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.OBJECT, 1) }, new String[] { "arg0" }, "setObjAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_objAttrs", new ArrayType(Type.OBJECT, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_57() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.OBJECT, 1) }, new String[] { "arg0" }, "setXMLAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_xmlAttrs", new ArrayType(Type.OBJECT, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_58() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(Type.BOOLEAN, 1) }, new String[] { "arg0" }, "setBoolAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_boolAttrs", new ArrayType(Type.BOOLEAN, 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_59() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ArrayType(new ObjectType("java.math.BigDecimal"), 1) }, new String[] { "arg0" }, "setNumberAttrs", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(_classPath, "m_numberAttrs", new ArrayType(new ObjectType("java.math.BigDecimal"), 1), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_60() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.INT, Type.INT }, new String[] { "arg0", "arg1" }, "xIsObj", _classPath, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_layout", new ObjectType("oracle.cep.memmgr.PageLayout"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("oracle.cep.memmgr.PageLayout", "getLengthPos", Type.SHORT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_offsets", new ArrayType(Type.SHORT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_noAttribs", Type.SHORT, Constants.GETFIELD));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(InstructionConstants.IADD);
    il.append(InstructionConstants.SALOAD);
    il.append(_factory.createStore(Type.INT, 4));
    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(_classPath, "m_intAttrs", new ArrayType(Type.INT, 1), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 4));
    il.append(InstructionConstants.IALOAD);
    il.append(new PUSH(_cp, -1));
        BranchInstruction if_icmpne_32 = _factory.createBranchInstruction(Constants.IF_ICMPNE, null);
    il.append(if_icmpne_32);
    il.append(new PUSH(_cp, 1));
        BranchInstruction goto_36 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_36);
    InstructionHandle ih_39 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_40 = il.append(_factory.createReturn(Type.INT));
    if_icmpne_32.setTarget(ih_39);
    goto_36.setTarget(ih_40);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

}
