package com.oracle.cep.cartridge.spatial.geocode;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.oracle.cep.cartridge.spatial.geocode.CountriesGeocode.Country;

public class TestCountryGeocode extends TestCase
{
    public static class TestCountry {
        public String name;
        public int area;
        public String cioc;
        public String cca2;
        public String capital;
        public double lat;
        public double lng;
        public String cca3;

        public TestCountry(String name, double lat, double lng, String cca2, String capital)
        {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
            this.cca2 = cca2;
            this.capital = capital;
        }
    }

    static List<TestCountry> countryList = new ArrayList<>();
    private void verifyCountry(TestCountry tc, Country c) {
        assertTrue(c != null);
        assertEquals(tc.cca2, c.cca2);
        assertEquals(tc.capital, c.capital);
        assertEquals(tc.lat, c.latlng[0]);
        assertEquals(tc.lng, c.latlng[1]);
    }

    public void testCountriesGeocode()
    {
        CountriesGeocode c = CountriesGeocode.getInstance();
        TestCountry tc = new TestCountry("United States", 38.0, -97.0, "US", "Washington D.C.");
        Country c1 = c.get(CountriesGeocode.FIELD_NAME, tc.name);
        verifyCountry(tc, c1);
        c1 = c.get(CountriesGeocode.FIELD_CCA2, tc.cca2);
        verifyCountry(tc, c1);
    }
}
