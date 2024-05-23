/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/GeomSpec.java /main/4 2015/10/01 22:29:44 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/04/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/GeomSpec.java /main/4 2015/10/01 22:29:44 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.spatial;

import com.oracle.cep.cartridge.spatial.GeomGenerator.VarResult;
import com.oracle.cep.cartridge.spatial.Geometry.GeomType;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public class GeomSpec
{
	public static class Config implements Externalizable
	{
		public static final int FLAG_SIMPLE_POINT = 1 << 0; //simplepoint - lng,lat
		public static final int FLAG_HAS_NO_ORDS = 1 << 1;  //has number of coordinates
		public static final int FLAG_CSV_ORDS = 1 << 2;     //coordinates is in "csv" format
		public static final int FLAG_CSV = 1 << 3;          //spec is in csv format
        public static final int FLAG_SIMPLE_POLYGON = 1 << 4; //simplepolygon only with coordinate - lng1,lat1,lng2,lat2,...
        public static final int FLAG_SIMPLE_PATH = 1 << 5;  //simplepath only with coordinates- lng1,lat1,lng2,lat2,...

		private int srid;
		private int dim;
		private int flag;
		private int length;
		
		public Config()
		{
			flag = -1;
			dim = 2;
			srid = GeodeticParam.LAT_LNG_WGS84_SRID;
			length = -1;
		}

		public Config(int flag, int dim, int srid) {
			this.flag = flag;
			this.dim = dim;
			this.srid = srid;
            length = -1;
		}

		public void setSrid(int v) {srid = v;}
		public int getSrid() { return srid;}
		public void setDim(int v) {dim = v;}
		public int getDim() { return dim;}
		public void setFlag(int v) { flag = v;}
		public int getFlag() { return flag;}
		public boolean needFlagSetup() { return flag < 0;}
		public int getLength() { return length;}
		
	    private void addGeomSpecFlag(boolean val, int v)
	    {
	    	if (flag < 0) flag = 0;
	    	flag |= val ?  v : 0 ;
	    }
	    
	    public void setCsvGeometry(boolean val){
	        addGeomSpecFlag(val, FLAG_CSV);
	    }    

	    public void setCsvCoords(boolean val){
	        addGeomSpecFlag(val, FLAG_CSV_ORDS);
	    }    

	    public void setSimplePointGeometry(boolean val){
	        addGeomSpecFlag(val, FLAG_SIMPLE_POINT);
	    }    
	          
	    public void setSimplePolygonGeometry(boolean val){
	        addGeomSpecFlag(val, FLAG_SIMPLE_POLYGON);
	    }    

	    public void setSimplePathGeometry(boolean val){
	        addGeomSpecFlag(val, FLAG_SIMPLE_PATH);
	    }    

	    public void setHasNoOfCoords(boolean val){
	        addGeomSpecFlag(val, FLAG_HAS_NO_ORDS);
	    }

		public void autoFlag(Object[] args, int startIndex, int endIndex)
		{
			flag = GeomSpec.autoFlag(args, startIndex, endIndex);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(srid);
			out.writeInt(dim);
			out.writeInt(flag);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			srid = in.readInt();
			dim = in.readInt();
			flag = in.readInt();
		}
	}


    //point - lng,lat
    public static final Config WGS84_POINT2D = new Config(Config.FLAG_SIMPLE_POINT, 2, GeodeticParam.LAT_LNG_WGS84_SRID);
    //polygon - ords
    public static final Config WGS84_POLYGON2D = new Config(Config.FLAG_SIMPLE_POLYGON, 2, GeodeticParam.LAT_LNG_WGS84_SRID);
    //path - ords
    public static final Config WGS84_PATH2D = new Config(Config.FLAG_SIMPLE_PATH, 2, GeodeticParam.LAT_LNG_WGS84_SRID);
    //type,ords
    public static final Config WGS84_2D_TYPE_ORDS = new Config(0, 2, GeodeticParam.LAT_LNG_WGS84_SRID);
    //type,no,ords
    public static final Config WGS84_2D_TYPE_NO_ORDS = new Config(Config.FLAG_HAS_NO_ORDS, 2, GeodeticParam.LAT_LNG_WGS84_SRID);
    //type,no,"ords"
    public static final Config WGS84_2D_TYPE_NO_CSVORDS = new Config(Config.FLAG_HAS_NO_ORDS|Config.FLAG_CSV_ORDS, 2, GeodeticParam.LAT_LNG_WGS84_SRID);
    //type, "ords"
    public static final Config WGS84_2D_TYPE_CSVORDS = new Config(Config.FLAG_CSV_ORDS, 2, GeodeticParam.LAT_LNG_WGS84_SRID);

	private GeomType type;
	private double[] coords;
	private double[][] mutltiCoords;
	private double arcTol = GeodeticParam.WGS84_ARC_TOL;
	private Config config;
	
	public GeomSpec()
	{
	}
	
	public void setConfig(Config cfg)
	{
		config = cfg;
	}
	
	public int getSrid() {
		return config.srid;
	}

	public int getDim() {
		return config.dim;
	}

	public double getArcTol() {
		return arcTol;
	}

	public void setArcTol(double v) {
		this.arcTol = v;
	}	
	
	public GeomType getType() {
		return type;
	}

	public void setType(GeomType type) {
		this.type = type;
	}

	public double[] getCoords() {
		return coords;
	}

	public void setCoords(double[] coords) {
		this.coords = coords;
	}
	
	
	public static GeomSpec fromCsv(Config cfg, String v)
	{
		double arcTol = GeodeticParam.getDefaultArcTol(cfg.getSrid());
		return fromCsv(cfg, arcTol, v);
	}
	
	public static GeomSpec fromCsv(Config cfg, double arcTol, String v)
	{
		List<String> csv = new ArrayList<String>();
		for (String vv : v.split(",")) {
			String val = vv.trim();
			csv.add(val);
		}
		String[] args = csv.toArray(new String[0]);
		VarResult r = fromArray(cfg, arcTol, args, 0);
		return (GeomSpec)r.result;
	}
	
	public static VarResult fromArray(Config cfg, Object[] args, int startIndex)
	{
		double arcTol = GeodeticParam.getDefaultArcTol(cfg.getSrid());
		return fromArray(cfg, arcTol, args, startIndex);
	}

	public static VarResult fromArray(Config cfg, double arcTol, Object[] args, int startIndex)
	{
	    VarResult r = new VarResult();
		int flag = cfg.getFlag();
		if (flag < 0) {
			flag = autoFlag(args, startIndex, args.length);
		}
		int dim = cfg.getDim();
		boolean isCsv = (flag & Config.FLAG_CSV) != 0;
		if (isCsv)
		{
			String v = stringVal(args[startIndex]);
			int oldflag = flag;
			flag &= ~Config.FLAG_CSV;
			cfg.setFlag(flag);
			GeomSpec spec = fromCsv(cfg, arcTol, v);
			cfg.setFlag(oldflag);
			r.result = spec;
			r.consumed = 1;
			return r;
		}
		GeomSpec spec = new GeomSpec();
		spec.setConfig(cfg);
		spec.arcTol = arcTol;
		boolean simplePoint = (flag & Config.FLAG_SIMPLE_POINT) != 0;
		if (simplePoint) {
			spec.type = GeomType.Point;
			VarResult r1 = getCoords(flag, args, startIndex, 2, dim);
			spec.coords = (double[]) r1.result;
            r.result = spec;
            r.consumed = 2;
            return r;
		}
        boolean simplePolygon = (flag & Config.FLAG_SIMPLE_POLYGON) != 0;
        boolean simplePath = (flag & Config.FLAG_SIMPLE_PATH) != 0;
        GeomType geomTyp = null;
        if (simplePolygon) geomTyp = GeomType.Polygon;
        else if (simplePath) geomTyp = GeomType.LineString;
        
        if (geomTyp == null) {
            String typ = stringVal(args[startIndex++]);
            geomTyp = Geometry.stringToGeomType(typ.trim());
            if (geomTyp == null) {
                throw new RuntimeException(SpatialCartridgeLogger
                        .UnknownGeometryErrorLoggable(typ).getMessage());
            }
        }
        int requiredNoOfCoords = 0; //required number of coordinates. 0 means variable length
		switch(geomTyp)
		{
		case Point: requiredNoOfCoords = 2; break;
		case Circle: requiredNoOfCoords = 3; break;
		case Rectangle: requiredNoOfCoords = 4; break;
		case Polygon: requiredNoOfCoords = 0; break;
		case LineString: requiredNoOfCoords = 0; break;
		case MultiPolygon: requiredNoOfCoords = 0; break;
		default:
			break;
		}
		spec.type = geomTyp;
		VarResult r1 = getCoords(flag, args, startIndex, requiredNoOfCoords, dim);
		spec.coords = (double[]) r1.result;
        r.result = spec;
        r.consumed = r1.consumed;
        return r;
	}

	private static VarResult getCoords(int flag, Object[] args, int startIndex, int requiredNoOfCoords, int dim) {
	    VarResult r = new VarResult();
	    int consumed = 0;
		boolean hasNoCoords = (flag & Config.FLAG_HAS_NO_ORDS) != 0;
		boolean csvCoords = (flag & Config.FLAG_CSV_ORDS) != 0;
		int len = args.length - startIndex;
		if (hasNoCoords) {
			len = intVal(args[startIndex++]);
			requiredNoOfCoords = len;
		}
        consumed = len;

        if (csvCoords) {
			String v = stringVal(args[startIndex++]);
			consumed = 1;
			List<Double> csv = new ArrayList<Double>();
			for (String vv : v.split(",")) {
				String val = vv.trim();
				double d = Double.parseDouble(val);
				csv.add(d);
			}
			args = csv.toArray(new Double[0]);
			startIndex = 0;
			len = args.length;
		}
		if (requiredNoOfCoords > 0) {
			if (args.length < (startIndex + requiredNoOfCoords)) {
				throw new RuntimeException(SpatialCartridgeLogger
						.NotEnoughGeometryArgumentsLoggable(requiredNoOfCoords,
								args.length - startIndex).getMessage());
			}
		}
		double[] coords = new double[len];
		int n = 0;
		for (n = 0; n < len; n++) {
			coords[n] = doubleVal(args[startIndex++]);
		}
		if (requiredNoOfCoords <= 0 && (n % dim) != 0) {
			throw new RuntimeException(SpatialCartridgeLogger
					.MismatchedCoordinatesPairErrorLoggable(dim).getMessage());
		}
		r.result = coords;
		r.consumed = consumed;
		return r;
	}

	public void  setMultiCoords(double[][] coords) {
		mutltiCoords = coords;
	}
	
	public double[][] getMultiCoords() {
		return mutltiCoords;
	}

	
	public static int autoFlag(Object[] args) 
	{
		return autoFlag(args, 0, args.length);
	}
	
    /**
     * autoFlag tries to detect the configuration by matching with the well-known formats
     *
     * @param args
     * @param startIndex
     * @param endIndex
     * @return
     */
	private static int autoFlag(Object[] args, int startIndex, int endIndex)
	{
		int flag = 0;
		Object v,v1,v2;
		
		int len = endIndex - startIndex;
		//string
		if (len == 1)
		{
			v = args[startIndex];
			if (isString(v))
			{
				flag |= Config.FLAG_CSV;
				args = ((String)v).split(",");
				startIndex = 0;
				endIndex = args.length;
				len = args.length;
			}
		} 
		//number,number
		//string,string
		if (len == 2)
		{
			v = args[startIndex];
			v1 = args[startIndex+1];
			if (isDouble(v) && isDouble(v1))
			{
				//number, number
				flag |= Config.FLAG_SIMPLE_POINT;
				return flag;
			}
			else if (isString(v) && isString(v1))
			{
				//type, csv
				flag |= Config.FLAG_CSV_ORDS;
				return flag;
			}			
		} else {
			v = args[startIndex];
			v1 = args[startIndex+1];
			v2 = args[startIndex+2];
			//string,number,number,...
			//string,int,string
			//string,int,number,number,...
			if (isString(v))
			{
				if (isInteger(v1))
				{
					flag |= Config.FLAG_HAS_NO_ORDS;
					if (isString(v2))
					{
						//type, int , csv
						flag |= Config.FLAG_CSV_ORDS;
						return flag;
					}
				} else if (isString(v1) || isString(v2))
				{
					//type, csv
					flag |= Config.FLAG_CSV_ORDS;
					return flag;
				}
			}			
		}
		return flag;
	}
	
	public static boolean isString(Object v)
	{
		if (v == null) return false;
		if (v instanceof Double || v instanceof Float ) return false;	
		if (v instanceof Integer || v instanceof Long ) return false;	
		if ( (v instanceof String || v instanceof char[]))
		{
			String str;
			if (v instanceof String) str = (String) v;
			else str = new String((char[])v);
			if (str.charAt(0) == '\'' || str.charAt(0) == '\"') return true;
			if (str.indexOf(',') >= 0) return true;
			if (isNumber(str, true)) return false;
			return true;
		}
		return false;
	}
	
	public static boolean isDouble(Object v)
	{
		if (v == null) return false;
		if (v instanceof Double || v instanceof Float ) return true;	
	    String str = v.toString();
		return isNumber(str, true);
	}
	
	public static boolean isInteger(Object v)
	{
		if (v == null) return false;
		if (v instanceof Integer || v instanceof Long ) return true;	
	    String str = v.toString();
		return isNumber(str, false);
	}
	
	public static boolean isNumber(String str, boolean canHaveDot)
	{
	    int length = str.length();
	    if (length == 0) {
	    	return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	    	if (length == 1) {
	    		return false;
	    	}
	    	i = 1;
	    }
	    for (; i < length; i++) {
	    	char c = str.charAt(i);
	    	if (c <= '/' || c >= ':') {
	    		if (canHaveDot && c == '.') continue;
	    		return false;
	    	}
	    }
	    return true;
	}
	
	public static int intVal(Object val) {
		if (val == null)
			return 0;
		if (val instanceof Number) {
			return ((Number) val).intValue();
		}
		if (val instanceof String) {
			return Integer.parseInt((String) val);
		}
		if (val instanceof char[]) {
			return Integer.parseInt(new String((char[]) val));
		}
		return Integer.parseInt(val.toString());
	}

	public static double doubleVal(Object val) {
		if (val == null)
			return 0.0;
		if (val instanceof Number) {
			return ((Number) val).doubleValue();
		}
		if (val instanceof String) {
			return Double.parseDouble((String) val);
		}
		if (val instanceof char[]) {
			return Double.parseDouble(new String((char[]) val));
		}
		return Double.parseDouble(val.toString());
	}

	public static String stringVal(Object val) {
		if (val == null)
			return "";
		if (val instanceof String) {
			return (String) val;
		}
		if (val instanceof char[]) {
			return new String((char[]) val);
		}
		return val.toString();
	}


	
}
