/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/FunctionMetadataLocator.java /main/10 2012/03/08 21:19:00 alealves Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      09/28/10 - add logging
 hopark      02/22/10 - remove AggrMBR, not supported in PS2 because cartridge infra does not support aggr functions.
 hopark      02/01/10 - add OrdsGenerator2
 alealves    11/27/09 - Data cartridge context, default package support
 udeshmuk    09/11/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/FunctionMetadataLocator.java /main/10 2012/03/08 21:19:00 alealves Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.spatial.SpatialCartridge;
import com.oracle.cep.cartridge.spatial.rtreeindex.*;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import org.apache.commons.logging.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FunctionMetadataLocator
    implements IUserFunctionMetadataLocator
{
  protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);//LogFactory.getLog(SpatialCartridge.LOGGER_NAME);
  
  private static Class<?>[] s_functions = {
    OrdsGenerator.class,
    OrdsGenerator2.class,
    ElemInfoGenerator.class,
    Distance.class,
    toGeometry.class,
    OpContain.class,
    OpInside.class,
    OpWithinDistance.class,
    OpAnyinteract.class,
    OpFilter.class,
    OpNN.class,
    OpInside3d.class,
    OpNearBy.class,
    Speed.class,
    AggSpeed.class,
    Acceleration.class,
    AggAcceleration.class,
    OpLocation.class,
    OpNearByPlace.class,
    Direction.class,
    ShapeGeometry.class,
    Buffer.class
  };

  private IUserFunctionMetadata getFunctionMetadata(Class<?> func, Datatype[] paramTypes, ICartridgeContext context)
  {
      try
      {
        Class<?>[] argTypes = {Datatype[].class, ICartridgeContext.class};
        Method m = func.getMethod("getMetadata", argTypes);
        Object[] args = {paramTypes, context};
        return (IUserFunctionMetadata) m.invoke(null, args);
      }
      catch(IllegalAccessException ie)
      {
    	ie.printStackTrace();
        assert (false) : ie.toString()+ "\ndefine 'getMetadata' in " + func.getSimpleName();
      }
      catch(NoSuchMethodException e)
      {
      	e.printStackTrace();
        assert (false) : e.toString()+ "\ndefine 'getMetadata' in " + func.getSimpleName();
      }
      catch(InvocationTargetException e)
      {
        Throwable cause = e.getCause();
        if (cause != null)
        {
          System.out.println(cause.toString());
          cause.printStackTrace();
        }
        else 
        {
          System.out.println("Unknown cause");
          e.printStackTrace();
        }
        assert (false) : e.toString()+ "\n"+(cause == null ? "": cause.toString())+"\nfailed to invoke 'getMetadata' in " + func.getSimpleName();
      }
      return null;
  }
  
  public static String getFuncSig(String name, Datatype[] paramTypes)
  {
	  StringBuilder b = new StringBuilder();
	  b.append(name);
	  b.append("(");
	  for (int i=0; i < paramTypes.length; i++)
	  {
		  if (i > 0) b.append(",");
	      Datatype typ = paramTypes[i];
	      b.append(typ.typeName);
	  }
	  b.append(")");
	  return b.toString();
  }
  
  @Override
  public IUserFunctionMetadata getFunction(String name, Datatype[] paramTypes, ICartridgeContext context)
      throws MetadataNotFoundException, AmbiguousMetadataException
  {
    if (log.isDebugEnabled())
    {
      SpatialCartridge.debugLog(log, this, " getFunction " + getFuncSig(name, paramTypes));
    }
    IUserFunctionMetadata  metadata = null;
    
    //Invoke the following logics using reflection.
    //if (name.equalsIgnoreCase(CreatePoint.NAME))
    //  metadata = CreatePoint.getMetadata(paramTypes);
    //
    for (Class<?> func : s_functions)
    {
      try
      {
        Field fname = func.getField("NAME");
        String s = (String) fname.get(null);
        if (name.equalsIgnoreCase(s))
        {
          metadata = getFunctionMetadata(func, paramTypes, context);
          break;
        }
      }
      catch(IllegalAccessException ie)
      {
        assert (false) : ie.toString()+ "\ndefine 'NAME' in " + func.getSimpleName();
      }
      catch(NoSuchFieldException e)
      {
        assert (false) : e.toString()+ "\ndefine 'NAME' in " + func.getSimpleName();
      }
    }
    if (metadata == null)
    {
      if (log.isDebugEnabled())
      {
        SpatialCartridge.debugLog(log, this, " MetadataNotFoundException not found: " + getFuncSig(name, paramTypes));
      }
      throw new MetadataNotFoundException(
          SpatialCartridge.CARTRIDGE_NAME, getFuncSig(name, paramTypes));
    }    
    return metadata;

  }

  public List<IUserFunctionMetadata> getAllFunctions(ICartridgeContext context)
      throws MetadataNotFoundException
  {
    List<IUserFunctionMetadata>  functions = new ArrayList<IUserFunctionMetadata>();
    
    for (Class<?> func : s_functions)
    {
  	  IUserFunctionMetadata  metadata = getFunctionMetadata(func, null, context);
      if (metadata != null)
      {
       	functions.add(metadata);
      }
    }

    return functions;
  }

}
