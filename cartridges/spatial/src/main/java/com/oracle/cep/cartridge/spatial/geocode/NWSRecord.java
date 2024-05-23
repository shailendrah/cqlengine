/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSRecord.java /main/1 2015/10/01 22:29:45 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      07/08/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSRecord.java /main/1 2015/10/01 22:29:45 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geocode;

import java.util.ArrayList;
import java.util.List;

import cern.colt.Arrays;

import com.oracle.cep.cartridge.spatial.GeodeticParam;
import com.oracle.cep.cartridge.spatial.GeomGenerator;
import com.oracle.cep.cartridge.spatial.GeomSpec;
import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.Geometry.GeomType;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class NWSRecord
{
	NWSRecordMetadata meta;
	String[] properties;
	String geomType;
	int dimension;
	List<double[]> coordinates;
	
	public NWSRecord()
	{
		coordinates = new ArrayList<double[]>();
	}
	
	public NWSRecord(NWSRecordMetadata m)
	{
		coordinates = new ArrayList<double[]>();
		setMetadata(m);
	}
	
	public void setMetadata(NWSRecordMetadata m)
	{
		meta = m;
		properties = new String[meta.getSize()];
	}
	
	public String[] getProperties()
	{
		return properties;
	}
	
	public void putProperty(String n, String v)
	{
		int pos = meta.getPropertyPos(n);
		properties[pos] = v;
	}
	
	public String getProperty(String key) {
		int pos = meta.getPropertyPos(key);
		return properties[pos];
	}	
	
	public void setGeomType(String n)
	{
		geomType = n;
	}
	
	public String getGeomType()
	{
		return geomType;
	}
	
	public int getDimension() { return dimension;}
	public void setDimension(int dim) {
		dimension = dim;
	}

	public List<double[]> getCoordinates()
	{
		return coordinates;
	}
	
	public void addCoordinates(double[] c)
	{
		coordinates.add(c);
	}

	public String toString()
	{
		StringBuilder b = new StringBuilder();
		b.append(Arrays.toString(getProperties()));
		b.append(" ");
		b.append(getGeomType());
		b.append(" ");
		for (double[] c : coordinates)
		{
			b.append(" [");
			int maxcnt = 2;
			for (int i = 0; i < c.length; i++)
			{
				if (i < maxcnt) {
					b.append(" ");
					b.append(c[i]);
				} else if (i == maxcnt) b.append("...");
			}
			b.append("]");
		}
		return b.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public static class Binding extends TupleBinding {
		NWSRecordMetadata meta;
		public Binding(NWSRecordMetadata meta)
		{
			this.meta = meta;
		}
		
		public void setMetadata(NWSRecordMetadata meta) {
			this.meta = meta;
		}
		
	    public void objectToEntry(Object object, TupleOutput to) {

	    	NWSRecord r = (NWSRecord)object;
	    	String[] props = r.getProperties();
	    	int n = props.length;
	    	to.writeInt(n);
	    	for (String v : props)
	    	{
	    		to.writeString(v);
	    	}
	    	to.writeString(r.getGeomType());
	    	to.writeInt(r.getDimension());
	    	List<double[]> coords = r.getCoordinates();
	    	n = coords.size();
	    	to.writeInt(n);
	    	for (double[] c : coords)
	    	{
	    		n = c.length;
	    		to.writeInt(n);
	    		for (int i = 0; i < n; i++)
	    			to.writeDouble(c[i]);
	    	}
	    }

	    public Object entryToObject(TupleInput ti) {
	        NWSRecord r = new NWSRecord();
	        r.setMetadata(meta);
	        String[] propNames = meta.getPropertyNameArray(); 
	        int n = ti.readInt();
	        for (int i = 0; i < n; i++)
	        {
	        	String k = propNames[i];
	        	String v = ti.readString();
	        	r.putProperty(k, v);
	        }
	        String geomType = ti.readString();
	        r.setGeomType(geomType);
	        int dim = ti.readInt();
	        r.setDimension(dim);
	        n = ti.readInt();
	        for (int i = 0; i < n; i++)
	        {
	            int cn = ti.readInt();
	            double[] d = new double[cn];
	            for (int j = 0; j < cn; j++)
	            	d[j] = ti.readDouble();
	            r.addCoordinates(d);
	        }
	        return r;
	    }

	} 	
	
	public static class SecondKeyCreator implements SecondaryKeyCreator {

	    private Binding theBinding;
	    private String key;
	    
	    // Use the constructor to set the tuple binding
	    SecondKeyCreator(String key) {
	        theBinding = new Binding(null);
	        this.key = key;
	    }

	    public void setMetadata(NWSRecordMetadata meta)
	    {
	    	theBinding.setMetadata(meta);
	    }
	    
	    // Abstract method that we must implement
	    public boolean createSecondaryKey(SecondaryDatabase secDb,
	        DatabaseEntry keyEntry,    // From the primary
	        DatabaseEntry dataEntry,   // From the primary
	        DatabaseEntry resultEntry) // set the key data on this.
	        throws DatabaseException {

	        if (dataEntry != null) {
	            NWSRecord record  =
	                  (NWSRecord)theBinding.entryToObject(dataEntry);
	            String theItem = record.getProperty(key);
	            resultEntry.setData(theItem.getBytes());
	        }
	        return true;
	    }
	}

	public Geometry getGeometry() {
		GeomSpec spec = new GeomSpec();
		GeomSpec.Config cfg = new GeomSpec.Config();
		cfg.setDim(dimension);
		cfg.setSrid(GeodeticParam.LAT_LNG_WGS84_SRID);
		spec.setConfig(cfg);
		GeomType gtype = Geometry.stringToGeomType(geomType);
		boolean show = false;
		//MultiPolygon does not work with JGeometry.
		//Only use the outside ring for now.
		if (gtype == GeomType.MultiPolygon) {
			gtype = GeomType.Polygon;
		}
		spec.setType(gtype);
		if (gtype == GeomType.MultiPolygon)
		{
			double[][] mcrds = new double[coordinates.size()][];
			for (int i = 0; i < coordinates.size(); i++)
			{
				mcrds[i] = coordinates.get(i);
			}
			spec.setMultiCoords(mcrds);
		}
		else
		{
			spec.setCoords(coordinates.get(0));
		}
		Geometry g = GeomGenerator.create(spec);
		return g;
	}

}
