/* $Header: ClassGenBase.java 21-apr-2008.13:39:34 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/08 - use ContextClassLoader
    hopark      08/10/07 - Creation
 */

/**
 *  @version $Header: ClassGenBase.java 21-apr-2008.13:39:34 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class ClassGenBase implements Constants
{
  protected InstructionFactory _factory;
  protected ConstantPoolGen    _cp;
  protected ClassGen           _cg;
  protected String             _className;
  protected String             _baseClassPath;
  protected String             _classPath;
  protected String             _classPathI;
  protected JavaClass          _class;

  protected static DirectLoader s_classLoader = new DirectLoader();

  private static class DirectLoader extends SecureClassLoader
  {
    Map<String, Class> loadedClasses;
    protected DirectLoader() {
        super(Thread.currentThread().getContextClassLoader());
        //TupleClassGen.class.getClassLoader());
        loadedClasses = new HashMap<String,Class>();
    }
    
    protected Class load(String name, byte[] data) {
        Class cls = super.defineClass(name, data, 0, data.length);
        loadedClasses.put(name, cls);
        return cls;
    }
    
    protected Class findClass(String name) throws ClassNotFoundException
    {
      Class cls = loadedClasses.get(name);
      if (cls == null)
        return super.findClass(name);
      return cls;
    }
 }
  
  public ClassGenBase(String baseClassPath, String classPath, String classPathI, String className) 
  {
    _className = className;
    _baseClassPath = baseClassPath;
    _classPath = classPath + "." + _className;
    _classPathI = classPathI + "$" + _className;

    _cg = new ClassGen(_classPath, 
                       _baseClassPath, 
                       className + ".java", 
                       ACC_PUBLIC | ACC_SUPER, 
                       new String[] {  });

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
    _class = null;
  }

  public void create()
  {
    synchronized(s_classLoader)
    {
      createFieldsMethods();
      _class = _cg.getJavaClass();
    }
  }
  
  protected abstract void createFieldsMethods();

  public static ClassLoader getClassLoader()
  {
    return s_classLoader;
  }
  
  public Class loadToJvm()
  {
    synchronized(s_classLoader)
    {
      
    assert (_class != null);
    byte[] bytes = _class.getBytes();
    try {
     Class tupleClass = s_classLoader.load(_classPath, bytes);
     return tupleClass;
    } catch(Exception ex) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ex);
    }
    return null;
    
    }
  }
  
  public void save(OutputStream out) throws IOException 
  {
    assert (_class != null);
    _class.dump(out);
  }
  
  public void dump()
  {
    if (_class == null)
    {
      System.out.println("null class");
      return;
    }
    System.out.println(_class);
    Method[] methods = _class.getMethods();
    for (Method method : methods)
    {
      System.out.println(method);
      Code code = method.getCode();
      if (code != null)
        System.out.println(code);
    }
  } 
}
