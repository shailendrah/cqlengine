package com.oracle.cep.cartridge.spatial;

import java.util.Map;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class SpatialContext
{
  public static final String GEO_PARAM = "GEO_PARAM";
  public static final String PARAM_NAME = "NAME";
  public static final String PARAM_APPNAME = "APPNAME";
  public static final String PARAM_CARTESIAN = "CARTESIAN";
  public static final String PARAM_SRID = "SRID";
  public static final String PARAM_SMA = "SMA";
  public static final String PARAM_ROF = "ROF";
  public static final String PARAM_TOL = "TOLERANCE";
  public static final String PARAM_ANYINTERACTTOL = "ANYINTERACT_TOLERANCE";
  public static final String PARAM_ARCTOL = "ARC_TOLERANCE";

  Map<String, Object> m_args;

  public static SpatialContext registerContext(String name, String appName, Map<String, Object> prop)
    throws CartridgeException
  {
      if (SpatialCartridge.SERVER_CONTEXT_NAME.equals(name))
      {
          String msg = SpatialCartridgeLogger.reservedServerContext(SpatialCartridge.SERVER_CONTEXT_NAME);
          LogUtil.severe(LoggerType.CUSTOMER, msg);
          throw new CartridgeException(SpatialCartridge.CARTRIDGE_NAME, msg);
      }
      SpatialCartridge c = SpatialCartridge.getInstance();

      GeodeticParam gparam = new GeodeticParam(false, getCartesian(prop), getSrid(prop), getSMA(prop), getROF(prop),  
    		  getTol(prop), getAnyinteractTol(prop), getArcTol(prop));
      GeodeticParam old = c.getGeoParam(gparam.getSRID());
      if (old != null)
      {
          //Allow override the server param only the first time.
          if (!old.isServerParam() && !old.equals(gparam))
          {
            String msg = SpatialCartridgeLogger.invalidSpatialContext(name, gparam.toString(), gparam.getSRID(), old.toString());
            LogUtil.severe(LoggerType.CUSTOMER, msg);
            throw new CartridgeException(SpatialCartridge.CARTRIDGE_NAME, msg);
          }
          if (!old.equals(gparam))
          {
            c.registerGeoParam(gparam.getSRID(), gparam);
          }
      }
      else
      {
        c.registerGeoParam(gparam.getSRID(), gparam);
      }
      
      prop.put(GEO_PARAM, gparam);
      prop.put(PARAM_NAME, name);
      prop.put(PARAM_APPNAME, appName);

      LogUtil.info(LoggerType.CUSTOMER, "registerApplicationContext " + appName +"." + name+" : " + gparam.toString());
      
      c.getCartridgeRegistry().registerApplicationContext(appName, name, SpatialCartridge.CARTRIDGE_NAME, prop);
      return new SpatialContext(prop);
  }

  public static void unregisterContext(String name, String appName)
    throws CartridgeException
  {
      LogUtil.info(LoggerType.CUSTOMER, "unregisterApplicationContext " + appName +"." + name);
      
      SpatialCartridge c = SpatialCartridge.getInstance();
      c.getCartridgeRegistry().unregisterApplicationContext(SpatialCartridge.CARTRIDGE_NAME, appName, name);
  }
   
  public SpatialContext(Map<String, Object> args)
  {
    m_args = args;
  }

  private static boolean getBoolean(Map<String, Object> args, String param, boolean defval)
  {
    if (args != null)
    {
      Object val = args.get(param);
      if (val != null)
      {
        if (val instanceof Boolean)
        {
          return (Boolean) val;
        }
        if (val instanceof String)
        {
          return (val.equals("true") || val.equals("1"));
        }
      }
    }
    return defval;
  }
  
  private static int getInt(Map<String, Object> args, String param, int defval)
  {
    if (args != null)
    {
      Object val = args.get(param);
      if (val != null)
      {
        if (val instanceof Integer)
        {
          return (Integer) val;
        }
        if (val instanceof String)
        {
          try
          {
            return Integer.parseInt( (String) val);
          }
          catch(NumberFormatException e)
          {
            LogUtil.severe(LoggerType.CUSTOMER, e.toString() + " : " + val);
            LogUtil.logStackTrace(e);
          }
        }
      }
    }
    return defval;
  }

  private static double getDouble(Map<String, Object> args, String param, double defval)
  {
    if (args != null)
    {
      Object val = args.get(param);
      if (val != null)
      {
        if (val instanceof Double)
        {
          return (Double) val;
        }
        if (val instanceof String)
        {
          try
          {
            return Double.parseDouble( (String) val);
          }
          catch(NumberFormatException e)
          {
            LogUtil.severe(LoggerType.CUSTOMER, e.toString() + " : " + val);
            LogUtil.logStackTrace(e);
          }
        }
      }
    }
    return defval;
  }

  public boolean getCartesian()
  {
    GeodeticParam val = (GeodeticParam) m_args.get(GEO_PARAM);
    assert (val != null);
    return val.isCartesian();
  }
  
  public int getSrid()
  {
    GeodeticParam val = (GeodeticParam) m_args.get(GEO_PARAM);
    //assert (val != null);
    if (val != null)
      return val.getSRID();
    
    //to support IDE and not having a dependency (e.g just put SRID in map)
    return getSrid(m_args);
  }

  public double getSMA()
  {
    GeodeticParam val = (GeodeticParam) m_args.get(GEO_PARAM);
    assert (val != null);
    return val.getSMA();
  }

  public double getROF()
  {
    GeodeticParam val = (GeodeticParam) m_args.get(GEO_PARAM);
    assert (val != null);
    return val.getROF();
  }

  public double getTol()
  {
    GeodeticParam val = (GeodeticParam) m_args.get(GEO_PARAM);
    assert (val != null);
    return val.getTol();
  }

  public double getAnyinteractTol()
  {
    GeodeticParam val = (GeodeticParam) m_args.get(GEO_PARAM);
    assert (val != null);
    return val.getAnyinteractTol();
  }

  public double getArcTol()
  {
    GeodeticParam val = (GeodeticParam) m_args.get(GEO_PARAM);
    assert (val != null);
    return val.getArcTol();
  }  
  
  private static boolean getCartesian(Map<String, Object> args)
  {
    return getBoolean(args, PARAM_CARTESIAN, false);
  }
  
  private static int getSrid(Map<String, Object> args)
  {
    return getInt(args, PARAM_SRID, SpatialCartridge.DEFAULT_SRID);
  }

  private static double getSMA(Map<String, Object> args)
  {
    return getDouble(args, PARAM_SMA, GeodeticParam.WGS84_SMA);
  }

  private static double getROF(Map<String, Object> args)
  {
    return getDouble(args, PARAM_ROF, GeodeticParam.WGS84_ROF);
  }

  private static double getTol(Map<String, Object> args)
  {
    return getDouble(args, PARAM_TOL, GeodeticParam.WGS84_TOL);
  }

  private static double getAnyinteractTol(Map<String, Object> args)
  {
    return getDouble(args, PARAM_ANYINTERACTTOL, GeodeticParam.WGS84_ANYINTERACT_TOL);
  }

  private static double getArcTol(Map<String, Object> args)
  {
    return getDouble(args, PARAM_ARCTOL, GeodeticParam.WGS84_ARC_TOL);
  }
}
