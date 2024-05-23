package com.oracle.cep.cartridge.spatial;

import java.util.Map;
import java.util.HashMap;

/*
 * 
 * GeodeticParam defines parameters for Geodetic coordinates.
 * It is used in converting geodetic coordinates to local tangent projection coordinates.
 */
public class GeodeticParam
{
  public final static int CARTESIAN_SRID = 0;
  public final static int LAT_LNG_WGS84_SRID = 8307;
  public final static int LAT_LNG_MERCATOR_SRID = 3857;
  public final static int LAT_LNG_WGS84_3D_SRID = 4979;
  
  public final static double WGS84_SMA = 6378137.0; // in meters
  public final static double WGS84_ROF = 298.257223563;
  public static final double WGS84_TOL = 0.000000001;   // minimum distance that can be ignored.
  public static final double WGS84_ANYINTERACT_TOL = 0.0000005; // expressed as degrees, approx .5 meters
  public static final double WGS84_ARC_TOL = 10.0; //in meters
  
  public final static double CARTESIAN_SMA = 0.0;
  public final static double CARTESIAN_ROF = 0.0;
  public static final double CARTESIAN_TOL = 0.1; 
  public static final double CARTESIAN_ANYINTERACT_TOL = 0.1;
  public static final double CARTESIAN_ARC_TOL = 0.1;

  boolean m_serverParam;
  int    m_srid;
  double m_sma;   //Semi-major axis (a)
  double m_rof; //Reciprocal of falatterning (1/f) 
  double m_arcTolerance = 10.0;	//arc tolerance in meters
  double m_tolerance;
  double m_anyinteractTolerance;
  
  boolean m_isCartesian;
  
  
  static GeodeticParam s_Cartesian = new GeodeticParam(true, true, CARTESIAN_SRID, CARTESIAN_SMA, CARTESIAN_ROF, CARTESIAN_TOL, CARTESIAN_ANYINTERACT_TOL, CARTESIAN_ARC_TOL);

  //http://www.epsg-registry.org
  //Code EPSG:4326
  //Name WGS84
  //Type GeodeticCRS(geographic2D)
  //GeodeticDatum(WGS84, EPSG:6326)
  static GeodeticParam s_WGS84 = new GeodeticParam(true, false, LAT_LNG_WGS84_SRID, WGS84_SMA, WGS84_ROF, WGS84_TOL, WGS84_ANYINTERACT_TOL, WGS84_ARC_TOL);
  //Code EPSG:3857
  //Name WGS84/Pseudo-Mercator
  //Type ProjectedCRS
  //GeodeticDatum(WGS84, EPSG:6326)
  static GeodeticParam s_WGS84Mercator = new GeodeticParam(true, false, LAT_LNG_WGS84_SRID, WGS84_SMA, WGS84_ROF, WGS84_TOL, WGS84_ANYINTERACT_TOL, WGS84_ARC_TOL);
  //Code EPSG:4979
  //Name WGS84
  //Type GeodeticCRS(Geodetic3D)
  //GeodeticDatum(WGS84, EPSG:6326)
  static GeodeticParam s_WGS843d = new GeodeticParam(true, false, LAT_LNG_WGS84_SRID, WGS84_SMA, WGS84_ROF, WGS84_TOL, WGS84_ANYINTERACT_TOL, WGS84_ARC_TOL);
  
  @SuppressWarnings("serial")
  static Map<Integer, GeodeticParam> s_paramMap = new HashMap<Integer, GeodeticParam>() {{
	  put(CARTESIAN_SRID, s_Cartesian);
	  put(LAT_LNG_WGS84_SRID, s_WGS84);
	  put(LAT_LNG_MERCATOR_SRID, s_WGS84Mercator);
	  put(LAT_LNG_WGS84_3D_SRID, s_WGS843d);
  }};
  
  public static GeodeticParam get(int srid)
  {
    return s_paramMap.get(srid);
  }

  public static Map<Integer, GeodeticParam> getAll()
  {
    return s_paramMap;
  }

  public GeodeticParam(boolean serverParam, boolean cartesian, int srid, double sma, double rof, double tol, double anyinteractTol, double arcTol)
  {
    m_serverParam = serverParam;
    m_srid = srid;
    m_sma = sma;
    m_rof = rof;
    m_tolerance = tol;
    m_arcTolerance = arcTol;
    m_anyinteractTolerance = anyinteractTol;
    m_arcTolerance = arcTol;
    m_isCartesian = cartesian;
  }
  
  public boolean isServerParam() {return m_serverParam;}
  public boolean isCartesian() {return m_isCartesian;}
  public int getSRID() {return m_srid;}

  public double getSMA() {return m_sma;}
  public void setSMA(double sma) {m_sma = sma;}

  public double getROF() {return m_rof;}
  public void setROF(double rof) {m_rof = rof;}
  
  public double getTol() {return m_tolerance;}
  public void setTol(double tol) {m_tolerance = tol;}

  public double getAnyinteractTol() {return m_anyinteractTolerance;}
  public void setAnyinteractTol(double tol) {m_anyinteractTolerance = tol;}
  
  public double getArcTol() {return m_arcTolerance;}
  public void setArcTol(double tol) {m_arcTolerance = tol;}

  public static double getDefaultArcTol(int srid)
  {
	  double arcTol = (srid == GeodeticParam.LAT_LNG_WGS84_SRID ? GeodeticParam.WGS84_ARC_TOL:CARTESIAN_ARC_TOL);
	  return arcTol;
  }
  
  public boolean equals(Object o)
  {
    if (! (o instanceof GeodeticParam)) 
      return false;
    GeodeticParam other = (GeodeticParam) o;
    if (m_srid != other.m_srid) return false;
    if (m_sma != other.m_sma) return false;
    if (m_rof != other.m_rof) return false;
    if (m_arcTolerance != other.m_arcTolerance) return false;
    if (m_tolerance != other.m_tolerance) return false;
    if (m_anyinteractTolerance != other.m_anyinteractTolerance) return false;
    if (m_isCartesian != other.m_isCartesian) return false;
    return true;
  }
  
  public String toString()
  {
    return
      (m_isCartesian ? "Cartesian " : "") +
      "srid="+m_srid+","+
      "sma="+m_sma+","+
      "rof="+m_rof+","+
      "arcTol="+m_arcTolerance+","+
      "tolerance="+m_tolerance+","+
      "arcTolerance="+m_arcTolerance+","+
      "anyinteractTolerance="+m_anyinteractTolerance;
  }
}

