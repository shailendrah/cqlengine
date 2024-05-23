/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSRecordMetadata.java /main/1 2015/10/01 22:29:45 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSRecordMetadata.java /main/1 2015/10/01 22:29:45 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.spatial.geocode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import cern.colt.Arrays;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class NWSRecordMetadata
{
	String id;
	String primaryKeyName;
	String secondaryKeyName;
	String[] propertyNames;
	Map<String, Integer> propertyNameToPosMap;
	static Map<String, NWSRecordMetadata> s_metaMap = new HashMap<String, NWSRecordMetadata>();
	
	public static Collection<NWSRecordMetadata> getAllMetadata()
	{
		return s_metaMap.values();
	}
	
	public static NWSRecordMetadata getMetadata(String id) 
	{
		synchronized(s_metaMap)
		{
			return s_metaMap.get(id);
		}
	}
	
	public NWSRecordMetadata()
	{
		propertyNameToPosMap = new LinkedHashMap<String, Integer>();
	}
	
	public NWSRecordMetadata(String id)
	{
		setId(id);
		propertyNameToPosMap = new LinkedHashMap<String, Integer>();
	}

	public void setId(String id)
	{
		this.id = id;
		synchronized(s_metaMap)
		{
			s_metaMap.put(id, this);
		}
	}
	
	public String getId() {return id;}
	
	public void setPrimaryKey(String key)
	{
		primaryKeyName = key;
	}
	
	public String getPrimaryKey()
	{
		return primaryKeyName;
	}

	public void setSecondaryKey(String key)
	{
		secondaryKeyName = key;
	}
	
	public String getSecondaryKey()
	{
		return secondaryKeyName;
	}

	public Set<String> getPropertyNames()
	{
		return propertyNameToPosMap.keySet();
	}
	
	public String[] getPropertyNameArray()
	{
		if (propertyNames == null)
		{
			Set<String> s = propertyNameToPosMap.keySet();
			propertyNames = s.toArray(new String[0]);
		}
		return propertyNames;
	}

	public void addPropertyName(String n)
	{
		int pos = propertyNameToPosMap.size();
		propertyNameToPosMap.put(n, pos);
	}
	
	public int getPropertyPos(String n)
	{
		Integer pos = propertyNameToPosMap.get(n);
		if (pos == null) throw new RuntimeException("Unknown property name " + n + " from "+ this.toString());
		return pos;
	}

	public int getSize() {
		return propertyNameToPosMap.size();
	}

	public String toString()
	{
		return id + " " + Arrays.toString(getPropertyNameArray());
	}
	
	@SuppressWarnings("rawtypes")
	public static class Binding extends TupleBinding {
	    public void objectToEntry(Object object, TupleOutput to) {

	    	NWSRecordMetadata r = (NWSRecordMetadata) object;
	    	to.writeString(r.getId());
	    	to.writeString(r.getPrimaryKey());
	    	to.writeString(r.getSecondaryKey());
	    	Set<String> props = r.getPropertyNames();
	    	int n = props.size();
	    	to.writeInt(n);
	    	for (String v : props)
	    	{
	    		to.writeString(v);
	    	}
	    }

	    public Object entryToObject(TupleInput ti) {
	        String metaname = ti.readString();
	        	        
	    	NWSRecordMetadata r = new NWSRecordMetadata(metaname);
	        String pkey = ti.readString();
	        r.setPrimaryKey(pkey);
	        String skey = ti.readString();
	        r.setSecondaryKey(skey);
	        int n = ti.readInt();
	        for (int i = 0; i < n; i++)
	        {
	        	String k = ti.readString();
	        	r.addPropertyName(k);
	        }
	        return r;
	    }
	} 	
	
}
