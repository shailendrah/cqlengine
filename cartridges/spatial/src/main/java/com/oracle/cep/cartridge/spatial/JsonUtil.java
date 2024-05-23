/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/JsonUtil.java /main/4 2015/10/01 22:29:44 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/02/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/JsonUtil.java /main/4 2015/10/01 22:29:44 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import oracle.cep.common.CEPDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.oracle.cep.cartridge.spatial.GeomGenerator;
import com.oracle.cep.cartridge.spatial.GeomSpec;
import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.Geometry.GeomType;

public class JsonUtil
{
	public static final String GEOJSON_FEATURES_KEY = "features";
	public static final String[] GEOJSON_FLATTEN_KEYS = new String[] { "properties" };
	public static final String[] GEOJSON_IGNORE_KEYS = new String[] { "type" };		

	public static Gson createGson()
	{
        CEPDateFormat df = CEPDateFormat.getInstance();
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat(df.getDefaultDBFormat().toPattern());
        builder.registerTypeAdapter(Geometry.class, new GeometrySerializer());
        builder.registerTypeAdapter(Geometry.class, new GeometryDeserializer());
		Gson gson = builder.create();
		return gson;
	}

	public static JsonObject geometryToJson(Geometry geom)
	{
		JsonObject obj = new JsonObject();
		String typename = geom.getTypeName();
		if (geom.getGeomType() == GeomType.Circle)
		{
			//The ordinates are already in polygon, we should use Polygon type even though the source type was circle.
			typename = GeomType.Polygon.name();
		}
		obj.add(Geometry.JSON_TAG_TYPE, new JsonPrimitive(typename) );
		obj.add(Geometry.JSON_TAG_SRID, new JsonPrimitive(geom.getSRID()) );
		JsonArray jsoncoords = new JsonArray();
		JsonArray csa = new JsonArray();
		double[] coords = geom.getOrdinatesArray();
		int dim = geom.getDimensions();
		for (int i = 0; i < coords.length; i++)
		{
			if (csa.size() == dim) 
			{
				jsoncoords.add(csa);
				csa = new JsonArray();
			}
			csa.add(new JsonPrimitive((coords[i])) );
		}
		if (csa.size() > 0)
		{
			jsoncoords.add(csa); 
		}
		obj.add(Geometry.JSON_TAG_COORDS, jsoncoords);
		return obj;
	}
	
    public static String getString(Object v)
	{
    	if (v == null) return null;
    	if (v instanceof String)
    		return ((String)v);
    	if (v instanceof char[])
    		return new String((char[]) v);
    	if (v instanceof byte[])
    		return new String((byte[]) v);
    	return v.toString();
	}
	
    public static int getInt(Object v)
	{
    	if (v == null) return 0;
    	if (v instanceof Number)
    		return ((Number)v).intValue();
   		return getInt((String)v);
	}
    
	@SuppressWarnings("unchecked")
	public static Geometry fromMap(int srid, int dimension, Map<String, Object> jsonv)
	{
		GeomSpec spec = new GeomSpec();
		GeomSpec.Config cfg = new GeomSpec.Config();
		spec.setConfig(cfg);
		String type = getString( jsonv.get(Geometry.JSON_TAG_TYPE) );
		GeomType geomType = Geometry.stringToGeomType(type);
		spec.setType(geomType);
		int geomsrid = srid;
		int sridv = getInt( jsonv.get(Geometry.JSON_TAG_SRID) );
		if (sridv != 0) geomsrid = sridv;
		cfg.setSrid(geomsrid);
		cfg.setDim(dimension);
		ArrayList<Object> coordsv = (ArrayList<Object>) jsonv.get(Geometry.JSON_TAG_COORDS);
		ArrayList<Double> coordsl = new ArrayList<Double>();
		flatCopyArrayList(coordsv, coordsl);
		double[] coords = new double[coordsl.size()];
		for (int i = 0; i < coordsl.size(); i++) coords[i] = coordsl.get(i);
		spec.setCoords(coords);
		return GeomGenerator.create(spec);
	}

	public static void flatCopyArrayList(JsonArray src, ArrayList<Double> dest)
	{
		for (int i = 0; i < src.size(); i++)
		{
			JsonElement e = src.get(i);
			if (e.isJsonArray())
			{
				JsonArray l = e.getAsJsonArray();
				flatCopyArrayList(l, dest);
			}
			else
			{
				dest.add(e.getAsDouble());
			}
		}		
	}   	

	public static Geometry jsonToGeometry(JsonObject obj)
	{
		GeomSpec spec = new GeomSpec();
		GeomSpec.Config cfg = new GeomSpec.Config();
		spec.setConfig(cfg);
		String type = obj.get(Geometry.JSON_TAG_TYPE).getAsString();
		GeomType geomType = Geometry.stringToGeomType(type);
		spec.setType(geomType);
		int geomsrid = obj.get(Geometry.JSON_TAG_SRID).getAsInt();
		cfg.setSrid(geomsrid);
		JsonArray csa = obj.get(Geometry.JSON_TAG_COORDS).getAsJsonArray();
		ArrayList<Double> coordsl = new ArrayList<Double>();
		JsonElement a0 = csa.get(0);
		int dimension = 2;
		if (a0.isJsonArray())
		{
			JsonArray ar0 = a0.getAsJsonArray();
			JsonElement a1 = ar0.get(0);
			if (a1.isJsonArray())
			{
				ar0 = a1.getAsJsonArray();
			}
			dimension = ar0.size();
		}
		flatCopyArrayList(csa, coordsl);
		double[] coords = new double[coordsl.size()];
		for (int i = 0; i < coordsl.size(); i++) coords[i] = coordsl.get(i);
		cfg.setDim(dimension);
		spec.setCoords(coords);
		return GeomGenerator.create(spec);
	}

	private static class GeometrySerializer implements JsonSerializer<Geometry> {

		@Override
		public JsonElement serialize(Geometry geom, Type typ,
				JsonSerializationContext context) {
			return geometryToJson(geom);
		}
	}

	
	private static class GeometryDeserializer implements JsonDeserializer<Geometry> {
		@Override
		public Geometry deserialize(JsonElement json, Type typ,
				JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			return jsonToGeometry(obj);
		}
	}


	public static void flatCopyArrayList(ArrayList<Object> src, ArrayList<Double> dest)
	{
		for (Object xy : src)
		{
			if (xy instanceof ArrayList)
			{
				@SuppressWarnings("unchecked")
				ArrayList<Object> l = (ArrayList<Object>) xy;
				flatCopyArrayList(l, dest);
			}
			else
			{
				dest.add((Double) xy);
			}
		}		
	}   
	
	public static final String DIM_PROPERTY = "dim";
	
	//flatten the inner most list to the upper level
	// [  [  [x1, y1], [x2,y2] ] ]
	// --> [  [  x1, y1, x2,y2 ] ]
	public static void flatCopyArrayList(List<Object> src, Properties props)
	{
		List<Object> d = null;
		for (Object xy : src)
		{
			if (xy instanceof List)
			{
				@SuppressWarnings("unchecked")
				List<Object> l = (List<Object>) xy;
				Object v = l.get(0);
				if (v instanceof List)
				{
					flatCopyArrayList(l, props);
				}
				else
				{
					if (d == null) d = new ArrayList<Object>();
					if (props != null) {
						props.put(DIM_PROPERTY, l.size());
						props = null;
					}
					for (Object vv : l)
						d.add(vv);
				}
			}
		}	
		if (d != null)
		{
			src.clear();
			src.addAll(d);
		}
	}

	@SuppressWarnings("unchecked")
	public static String dump(List<Object> s, int maxcnt)
	{
		StringBuilder b = new StringBuilder();
		b.append("[ ");
		int cnt = 0;
		for (Object xy : s)
		{
			if (xy instanceof List)
			{
				b.append(" ");
				b.append(dump((List<Object>)xy, maxcnt));
				b.append(" ");
			}
			else
			{
				if (cnt < maxcnt)
				{
					b.append(" ");
					b.append(xy);
					b.append(" ");
				}
				if (cnt == maxcnt) b.append(" ... ");
				cnt++;
			}
		}
		b.append(" ]");
		return b.toString();
	}
	
	/*
	public static void main(String[]args)
	{
		List<Object> s = new ArrayList<Object>() {{
			add( new ArrayList<Object>() {{
				add(new ArrayList<Double>() {{	add(1.0); add(2.0); }}); 
				add(new ArrayList<Double>() {{	add(3.0); add(4.0); }}); 
			}});
			add( new ArrayList<Object>() {{
				add(new ArrayList<Double>() {{	add(11.0); add(12.0); }}); 
				add(new ArrayList<Double>() {{	add(13.0); add(14.0); }}); 
			}});
		}};
		System.out.println(dump(s));
		flatCopyArrayList(s);
		System.out.println(dump(s));
	}
	*/
	
	@SuppressWarnings("serial")
	public static class JsonEvents extends ArrayList<Object> {
	}	

	@SuppressWarnings("serial")
	public static class JsonEvent extends LinkedHashMap<String, Object> {
	}	

	public static List<Object> parseJson(Object source)
	{
		return parseJson(source, GEOJSON_FEATURES_KEY);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> parseJson(Object source, String arrayKey)
	{
		PushbackReader srcReader = null;
		if (source instanceof Reader)
		{
			srcReader = new PushbackReader((Reader)source);
		} else if (source instanceof String)
		{
			srcReader = new PushbackReader(new StringReader((String)source));
		} else throw new IllegalArgumentException("Invalid json source");
		Gson gson = JsonUtil.createGson();
		List<Object> objs = null;
		try
		{
			JsonEvent ev;
			int ch;
			do 
			{
				try {
					ch = srcReader.read();
				} catch (IOException e) {
					throw new RuntimeException("Failed to load json", e);
				}
			} while ( Character.isWhitespace(ch) );
			try {
				srcReader.unread(ch);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load json", e);
			}
			//check if the json is array type of geojson
			if (ch == '[')
			{
				objs = gson.fromJson(srcReader, JsonEvents.class);
			}
			else
			{
				ev = gson.fromJson(srcReader, JsonEvent.class);
				if (arrayKey == null)
				{
					//If arrayKey is not given, use the first found ArrayList type
					for (String k : ev.keySet())
					{
						Object v = ev.get(k);
						if (v instanceof ArrayList)
						{
							objs = new JsonEvents();
							objs.addAll((ArrayList<Object>) v);
							break;
						}
					}
				}
				else
				{
					Object v = ev.get(arrayKey);
					if (v != null && v instanceof ArrayList)
					{
						objs = new JsonEvents();
						objs.addAll((ArrayList<Object>) v);
					}
					else
					{
			            throw new RuntimeException(SpatialCartridgeLogger.invalidArrayKeyLoggable(arrayKey).getMessage());    
					}
				}
			}
		} catch(JsonSyntaxException e)
		{
			throw new RuntimeException("Failed to load json", e);						
		}
		return objs;
	}

	public static void processFlattenIgnore(Map<String, Object> obj)
	{
		processFlattenIgnore(obj, GEOJSON_FLATTEN_KEYS, GEOJSON_IGNORE_KEYS);
	}

	public static void processFlattenIgnore(Map<String, Object> obj, String[] flattenKeys, String[] ignoreKeys)
	{
		if (flattenKeys != null && flattenKeys.length > 0)
		{
			for (String props : flattenKeys)
			{
				Object v = obj.get(props);
				if (v != null && v instanceof Map )
				{
					@SuppressWarnings("unchecked")
					Map<String, Object> vals = (Map<String, Object>) v;
					for (String k : vals.keySet())
					{
						Object kv = vals.get(k);
						obj.put(k, kv);
					}
					obj.remove(props);
				}
			}
		}
		if (ignoreKeys != null && ignoreKeys.length > 0)
		{
			for (String props : ignoreKeys)
			{
				obj.remove(props);
			}
		}		
	}

    private static void encodeSignedNumber(StringBuilder b, int num) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~(sgn_num);
        }
        encodeNumber(b, sgn_num);
    }

    private static void encodeNumber(StringBuilder b, int num) {
    	while (num >= 0x20) {
        	int nextValue = (0x20 | (num & 0x1f)) + 63;
            b.append((char)(nextValue));
            num >>= 5;
        }
        num += 63;
        b.append((char)(num));
    }
    
    /**
     * Encode a polyline with Google polyline encoding method
     * @param polyline the polyline
     * @param precision 1 for a 6 digits encoding, 10 for a 5 digits encoding. 
     * @return the encoded polyline, as a String
     */
    public static String encode(double[] coords, int start, int end, int precision) {
    	double factor = Math.pow(10, precision == 0 ? 5:precision);
		StringBuilder encodedPoints = new StringBuilder();
		int prev_lat = 0, prev_lng = 0;
		int i = start;
		while(i < end)
		{
			int lng =  (int) Math.round(coords[i++] * factor);
			int lat = (int) Math.round(coords[i++] * factor);
			encodeSignedNumber(encodedPoints, lat - prev_lat);
			encodeSignedNumber(encodedPoints, lng - prev_lng);			
			prev_lat = lat;
			prev_lng = lng;
		}
		return encodedPoints.toString();
	}

    /**
     * Decode a "Google-encoded" polyline
     * @param encodedString
     * @param precision 1 for a 6 digits encoding, 10 for a 5 digits encoding. 
     * @return the polyline. 
     */
    public static double[] decode(String encodedString, int precision) {
    	double factor = Math.pow(10, precision == 0 ? 5:precision);
        ArrayList<Double> polyline = new ArrayList<Double>();
        int index = 0;
        int len = encodedString.length();
        int lat = 0, lng = 0;
        while (index < len) {
        	char c;
            int b, shift = 0, result = 0;
            do {
                c = encodedString.charAt(index++);
                b = c - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                c = encodedString.charAt(index++);
                b = c - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double longitude = lng/factor;
            double latitude = lat/factor;
            polyline.add(longitude);
            polyline.add(latitude);
        }
        double[] coords = new double[polyline.size()];
        int i = 0;
        for (Double d : polyline) coords[i++] = d;
        return coords;
    }
}
