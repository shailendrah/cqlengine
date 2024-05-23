package com.oracle.cep.cartridge.spatial.geocode;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.oracle.cep.cartridge.geocodedb.PlaceNamesRsc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class CountriesGeocode {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CCA2 = "cca2";

    private static final boolean DUMP_JSON = false;

    public static class Country {
        public Map<String, Object> name;
        public String[] tld;
        public String cca2;
        public String ccn3;
        public String cca3;
        public String cioc;
        public String[] currency;
        public String[] callingCode;
        public String capital;
        public String[] altSpellings;
        public String region;
        public String subresion;
        public Map<String, Object> languages;
        public Map<String, Object> translations;
        public double[] latlng;
        public String demonym;
        public String landlocked;
        public String[] borders;
        public double area;

        private Set<String> getNameFrom(Set<String> r, Map<String,Object> nmap) {
            if (nmap == null) return r;
            Object v = nmap.get("common");
            if ( v != null) r.add((String) v);
            v = nmap.get("official");
            if ( v != null) r.add((String) v);
            v = nmap.get("native");
            if (v != null && v instanceof Map) {
                Map<String, Object> nv = (Map<String, Object>) v;
                v = nv.get(nv.keySet().iterator().next());
                Map<String, Object> nv2 = (Map<String, Object>) v;
                return getNameFrom(r, nv2);
            }
            return r;
        }

        public Set<String> getNames() {
            Set<String> r = new LinkedHashSet<>();
            return getNameFrom(r, name);
        }
    }

    private static class Countries {
        public List<Country> countries;
    }

    Map<String,Country> lookupByName = new HashMap<>();
    Map<String,Country> lookupByCCA2 = new HashMap<>();
    Map<String,Country> lookupByCCA3 = new HashMap<>();

    private static CountriesGeocode s_instance = null;
    public static synchronized CountriesGeocode getInstance()
    {
        if (s_instance != null) return s_instance;
        String rsc = "/countries.json";
        InputStream is = PlaceNamesRsc.getResource(rsc);
        if (DUMP_JSON) {
            String result = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            System.out.println(result);
        }
        s_instance = new CountriesGeocode(is);
        return s_instance;
    }

    public CountriesGeocode(InputStream is) {
        Reader reader = new InputStreamReader(is);
        Gson gson = new Gson();
        Country[] clist = gson.fromJson(reader, Country[].class);
        for (Country c : clist) {
            Set<String> names = c.getNames();
            for (String n : names) {
                String cname = n.toLowerCase();
                lookupByName.put(cname, c);
            }
            String cca2 = c.cca2.toLowerCase();
            lookupByCCA2.put(cca2, c);
            String cca3 = c.cca3.toLowerCase();
            lookupByCCA3.put(cca3, c);
        }
    }

    private Map<String, Country> getLookup(String field) {
        Map<String,Country> lookup = null;
        if (field.equals(FIELD_NAME)) lookup = lookupByName;
        else if (field.equals(FIELD_CCA2)) lookup = lookupByCCA2;
        return lookup;
    }

    public Country get(String field, String symbol) {
        Country country = null;
        Map<String,Country> lookup = getLookup(field);
        if(lookup != null)
        {
            country = lookup.get(symbol.toLowerCase());
        }
        return country;
    }

    public Country getByCCA3(String symbol) {
        Country country = null;
        country = lookupByCCA3.get(symbol.toLowerCase());
        return country;
    }

}
