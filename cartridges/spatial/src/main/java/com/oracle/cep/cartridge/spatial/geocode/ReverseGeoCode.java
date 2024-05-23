/*
The MIT License (MIT)
[OSI Approved License]
The MIT License (MIT)

Copyright (c) 2014 Daniel Glasson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.oracle.cep.cartridge.spatial.geocode;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.cep.cartridge.geocodedb.PlaceNamesRsc;
import com.oracle.cep.cartridge.spatial.geocode.kdtree.KDTree;

/**
 *
 * Created by Daniel Glasson on 18/05/2014.
 * Uses KD-trees to quickly find the nearest point
 * 
 * https://github.com/AReallyGoodName/OfflineReverseGeocode
 * 
 * place names
 * http://download.geonames.org/export/dump/
 * 
 * ReverseGeoCode reverseGeoCode = new ReverseGeoCode(new FileInputStream("c:\\AU.txt"), true);
 * System.out.println("Nearest to -23.456, 123.456 is " + geocode.nearestPlace(-23.456, 123.456));
 */
public class ReverseGeoCode {
    KDTree<GeoName> kdTree;
    
    private static final String CLASSPATH_MARKER = "classpath:";
    static Map<String, ReverseGeoCode> s_map = new ConcurrentHashMap<String, ReverseGeoCode>();
    public static ReverseGeoCode getInstance(String urlstring)
    {
    	ReverseGeoCode c = s_map.get(urlstring);
    	if (c == null)
    	{
    		InputStream is;
    		
    		int pos = urlstring.indexOf(CLASSPATH_MARKER);
    		if (pos >= 0)
    		{
    			String rsc = urlstring.substring(pos+CLASSPATH_MARKER.length());
    			is = PlaceNamesRsc.getResource(rsc);
    		}
    		else
    		{
	    		URL url;
	    		try {
					url = new URL(urlstring);
					is = url.openStream();
				} catch (MalformedURLException e) {
					throw new RuntimeException("invalid url : "+urlstring, e);
				} catch (IOException e) {
					throw new RuntimeException("failed to open url : "+urlstring, e);
				}
    		}
    		try {
				c = new ReverseGeoCode(is, true);
			} catch (IOException e) {
				throw new RuntimeException("failed to load places from url : "+urlstring, e);
			}
    		s_map.put(urlstring, c);
    	}
    	return c;
    }
    
    public ReverseGeoCode( InputStream placenames, boolean majorOnly ) throws IOException {
        ArrayList<GeoName> arPlaceNames;
        arPlaceNames = new ArrayList<GeoName>();
        // Read the geonames file in the directory
        BufferedReader in = new BufferedReader(new InputStreamReader(placenames));
        String str;
        try {
            while ((str = in.readLine()) != null) {
                GeoName newPlace = new GeoName(str);
                if ( !majorOnly || newPlace.isMajorPlace()) {
                    arPlaceNames.add(new GeoName(str));
                }
            }
        } catch (IOException ex) {
            in.close(); 
            throw ex;
        }
        in.close();
        kdTree = new KDTree<GeoName>(arPlaceNames);
    }

    public GeoName nearestPlace(double latitude, double longitude) {
        return kdTree.findNearest(new GeoName(latitude,longitude));
    }

    public static void main(String[] args)
    {
    	if (args.length == 0)
    	{
    		System.out.println("Usage)");
    		System.out.println("placename_file lat lng");
    		System.exit(0);
    	}
        String placenameFile = args[0];
        double lat = Double.parseDouble(args[1]);
        double lng = Double.parseDouble(args[2]);
		try {
			ReverseGeoCode reverseGeoCode = new ReverseGeoCode(new FileInputStream(placenameFile), true);
	        GeoName name = reverseGeoCode.nearestPlace(lat, lng);
	        System.out.println("Nearest to " + lat +","+lng + " is " + name.toString());
		} catch (FileNotFoundException e) {
			System.out.println("File not found : " + placenameFile);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException : " + placenameFile);
			e.printStackTrace();
		}
    }
    
}
