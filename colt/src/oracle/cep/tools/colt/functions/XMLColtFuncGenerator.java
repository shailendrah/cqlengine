/* $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/functions/XMLColtFuncGenerator.java /main/8 2014/12/11 02:33:17 tprabish Exp $ */

/* Copyright (c) 2007, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/14/14 - bug 18240550
    sbishnoi    02/24/13 - moving floor, ceil and round to ootb implementations
    sbishnoi    04/21/08 - changing package definition
    udeshmuk    02/01/08 - support for double data type.
    mthatte     10/11/07 - Removing semi-colons
    sbishnoi    08/13/07 - add java.lang.Math
    sbishnoi    06/20/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/functions/XMLColtFuncGenerator.java /main/8 2014/12/11 02:33:17 tprabish Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.tools.colt.functions;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.StringBuilder;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

/**
 *  Creates the XML file containing information about all colt functions
 *  implemented by CEP. 
 *  note: Functions with array arguments are not supported. 
 */

class XMLColtFuncGenerator {
  
  public XMLColtFuncGenerator() {
  }

  public static void generator(String className, StringBuilder xml,
                               int clsId, Hashtable func)
    throws IOException, ClassNotFoundException {

    String    funcName;
    Class     c          = Class.forName(className);
    Method[]  mList      = c.getDeclaredMethods();
    int       functionId = 0;
    
    xml.append("<class className=\"");
    xml.append(className);
    xml.append("\" classId=\"");
    xml.append(clsId);
    xml.append("\">");
    
    Class   returnType;
    Class[] paramTypeList;
    boolean argSupported = true;
    boolean isAccessible = true;
    int     countArgs    = 0;

    Arrays.sort(mList,new Comparator<Method>() {@Override
    public int compare(Method o1, Method o2) {
    	return o1.getName().compareTo(o2.getName());
    }
	});
    
    for(Method m : mList) 
    {
      funcName      = m.getName();
      returnType    = m.getReturnType();
      paramTypeList = m.getParameterTypes();
      argSupported  = true;
      isAccessible  = true;

      // Only public member function get generated
      isAccessible = Modifier.isPublic(m.getModifiers());
      if(!isAccessible)
        continue;
      
      if(paramTypeList.length == 0)
        argSupported = false;

      // Only those methods are allowed 
      // whose parameter's data type is either short or char or boolean.
      // Parameters should be primitive data type
      // Return type should be primitive data type
      // Return type shouldn't be an array
      
      for(Class cls : paramTypeList) {
        if(cls.isArray() || 
           !Modifier.isStatic(m.getModifiers()) || 
           returnType.isArray() || 
           !returnType.isPrimitive() || 
           !cls.isPrimitive() || 
            (cls.getName()).equalsIgnoreCase("short") ||
            (cls.getName()).equalsIgnoreCase("char") ||
            (cls.getName()).equalsIgnoreCase("boolean"))
          
          argSupported = false;
      }
      
      // As MIN & MAX are keywords, 
      // so functions having these names are not allowed
      if(funcName.equalsIgnoreCase("min") || funcName.equalsIgnoreCase("max")||
         funcName.equalsIgnoreCase("floor") ||
         funcName.equalsIgnoreCase("ceil") ||
         funcName.equalsIgnoreCase("round"))
      {
        argSupported = false;
      }

      if(funcName.equalsIgnoreCase("log") ||
         funcName.equalsIgnoreCase("binomial") ||
         funcName.equalsIgnoreCase("beta") ||
         funcName.equalsIgnoreCase("gamma") ||
         funcName.equalsIgnoreCase("normal") ||
         funcName.equalsIgnoreCase("hash") ||
         funcName.equalsIgnoreCase("abs") ||
         funcName.equalsIgnoreCase("scalb") ||
         funcName.equalsIgnoreCase("getExponent") ||
         funcName.equalsIgnoreCase("signum") ||
         funcName.equalsIgnoreCase("copySign") ||
         funcName.equalsIgnoreCase("nextAfter") ||
         funcName.equalsIgnoreCase("nextUp") ||
	 funcName.equalsIgnoreCase("nextDown") ||
	 funcName.equalsIgnoreCase("floorMod") ||
	 funcName.equalsIgnoreCase("floorDiv") ||
	 funcName.equalsIgnoreCase("subtractExact") ||
	 funcName.equalsIgnoreCase("negateExact") ||
	 funcName.equalsIgnoreCase("multiplyExact") ||
	 funcName.equalsIgnoreCase("incrementExact") ||
	 funcName.equalsIgnoreCase("decrementExact") ||
	 funcName.equalsIgnoreCase("addExact") ||
         funcName.equalsIgnoreCase("sqrt") ||
         funcName.equalsIgnoreCase("ulp"))
      {
        argSupported = false;
      }

      if(!argSupported)
        continue;
      
      String tmpRet = returnType.getName();
      xml.append("<function fName=\"");
      xml.append(funcName); 
      functionId++;
      xml.append("\" fId=\"");
      xml.append(functionId);
      xml.append("\"><returntype returnTypeClass=\"");
      xml.append(((tmpRet.substring(0,1)).toUpperCase()).concat(tmpRet.substring(1)));
      if(tmpRet.equalsIgnoreCase("int"))
        xml.append("eger");
      
      xml.append("\" CEPReturnTypeClass=\"");
      if(tmpRet.equalsIgnoreCase("int"))
        xml.append("integer");
      else if(tmpRet.equalsIgnoreCase("long"))
        xml.append("bigint");
      else
        xml.append(tmpRet);
      
      xml.append("\">");
      xml.append(tmpRet);
      xml.append("</returntype><arguments>");
      
      countArgs = 0;

      // Append Parameters' information for each method of a class
      for(Class param : paramTypeList)
      {
        String paramTypeName = param.getName();
        countArgs++;
        xml.append("<arg index=\"");
        xml.append(countArgs);
        xml.append("\" dataTypeClass=\"");
        xml.append(((paramTypeName.substring(0,1)).toUpperCase()).concat(paramTypeName.substring(1)));
        if(paramTypeName.equalsIgnoreCase("int"))
          xml.append("eger");
        xml.append("\" CEPDataTypeClass=\"");
        
        // Use integer in place of int    for CEPDataTypeClass
        // Use bigint  in place of long   for CEPDataTypeClass
        
        if(paramTypeName.equalsIgnoreCase("int"))
          xml.append("integer");
        else if(paramTypeName.equalsIgnoreCase("long"))
          xml.append("bigint");
        else
          xml.append(paramTypeName);
        
        xml.append("\">");
        xml.append(param.getName());
        xml.append("</arg>");
      }
      xml.append("<noOfArgs>");
      xml.append(countArgs);
      xml.append("</noOfArgs></arguments><javaClassName nameOfClass=\"");

      // Modify function name if it already exist in class
      if(func.containsKey(funcName)) 
      {
       Integer tmp     = (Integer)(func.get(funcName)); 
       int     hashVal = tmp.intValue();
       xml.append(funcName+hashVal);
       func.put(funcName, new Integer(hashVal+1));
      }
      else 
      {
        func.put(funcName, new Integer(1));
        xml.append(funcName);
      }
      xml.append("\"></javaClassName></function>");
    }
    xml.append("</class>");
  }    
  
  public static void main(String[] args) {

    // clsId will be a unique number for each package
    XMLColtFuncGenerator funcGen   = new XMLColtFuncGenerator();
    PrintWriter          out       = null;
    int                  clsId     = 1;
    Hashtable            functions = new Hashtable();  
    StringBuilder        xml;
    
    try 
    {
      File path = new File(args[0]);
      if (path.getParentFile() != null) {
          path.getParentFile().mkdirs();
      }
      out = new PrintWriter(path);
      xml = new StringBuilder();
      xml.append("<package>");
      funcGen.generator("cern.jet.math.Arithmetic", xml, clsId, functions);
      clsId += 1;
      funcGen.generator("cern.jet.math.Bessel", xml, clsId, functions);
      clsId += 1;
      funcGen.generator("cern.jet.random.engine.RandomSeedTable", xml, clsId, functions);
      clsId += 1;
      funcGen.generator("cern.jet.stat.Gamma", xml, clsId, functions);
      clsId += 1;
      funcGen.generator("cern.jet.stat.Probability", xml, clsId, functions);
      clsId += 1;
      funcGen.generator("cern.colt.bitvector.QuickBitVector", xml, clsId, functions);
      clsId += 1;
      funcGen.generator("cern.colt.map.HashFunctions", xml, clsId, functions);
      clsId += 1;
      funcGen.generator("java.lang.Math", xml, clsId, functions);
      xml.append("</package>");
      out.append(xml.toString());
      out.flush();
    }
    catch(IOException e) 
    {
      System.out.println("Problem in writing to xml file");
      e.printStackTrace();
    }
    catch(ClassNotFoundException e) 
    {
      e.printStackTrace();
    }
    finally 
    {
      if(out != null)
        out.close();
    }
  }
}
