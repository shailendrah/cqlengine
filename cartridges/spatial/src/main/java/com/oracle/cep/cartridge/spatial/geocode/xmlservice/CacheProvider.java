package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

public interface CacheProvider<Key,Value> {
	
	public Value get(Key key);
	
	public void put(Key key, Value value);
	
	public void remove(Key key);
	
	public void setSize(int cacheSize);
}
