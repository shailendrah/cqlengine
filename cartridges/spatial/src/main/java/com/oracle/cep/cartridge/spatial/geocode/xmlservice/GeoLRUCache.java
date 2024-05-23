package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

import java.util.LinkedHashMap;
import java.util.Map;

public class GeoLRUCache<Key, Value> implements CacheProvider<Key, Value> {

	private class LRUCache<K, V> extends LinkedHashMap<K, V> {
		private static final long serialVersionUID = 1L;
		private int MAX_ENTRIES;

		public LRUCache(int maxEntries) {
			super(maxEntries + 1, 1.0f, true);
			MAX_ENTRIES = maxEntries;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > MAX_ENTRIES;
		}
		
		public void setCacheSize(int size){
			MAX_ENTRIES = size;
		}
	}

	private LRUCache<Key, Value> cache;
	public  GeoLRUCache(int cacheSize) {
		cache = new LRUCache<>(cacheSize);
	}
	
	@Override
	public Value get(Key key) {
		return cache.get(key);
	}

	@Override
	public void put(Key key, Value value) {
		cache.put(key, value);
	}

	@Override
	public void remove(Key key) {
		cache.remove(key);
	}

	@Override
	public void setSize(int cacheSize) {
		cache.setCacheSize(cacheSize);
	}

}
