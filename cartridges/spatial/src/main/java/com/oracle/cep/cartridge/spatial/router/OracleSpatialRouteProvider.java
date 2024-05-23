/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/router/OracleSpatialRouteProvider.java /main/2 2015/11/03 08:32:49 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      07/29/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/router/OracleSpatialRouteProvider.java /main/2 2015/11/03 08:32:49 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.router;


import com.oracle.cep.cartridge.spatial.GeodeticParam;
import com.oracle.cep.cartridge.spatial.router.osrxml.req.*;
import com.oracle.cep.cartridge.spatial.router.osrxml.resp.Geometry;
import com.oracle.cep.cartridge.spatial.router.osrxml.resp.LineString;
import com.oracle.cep.cartridge.spatial.router.osrxml.resp.RouteResponse;
import com.oracle.cep.cartridge.spatial.router.osrxml.resp.RouteType;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import org.apache.commons.logging.Log;

import javax.xml.bind.*;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
public class OracleSpatialRouteProvider implements RouteProvider
{
    protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);//LogFactory.getLog(SpatialCartridge.LOGGER_NAME);
	
	String url = System.getProperty("osa.spark.url");
	String country = "us";
	RoutePreference routePreference = RoutePreference.SHORTEST;	//SHORTEST, FASTEST
	RoadPreference roadPreference = RoadPreference.HIGHWAY;	//HIGHWAY, local
	
	private void loginfo(String msg)
	{
		log.info(this.getClass().getName() + ":" + msg);
	}
	public void setUrl(String url) {
		this.url = url;
		if (log.isInfoEnabled())
			loginfo("url="+url);
	}

	public void setRoutePreference(String v) {
		this.routePreference = RoutePreference.fromValue(v);
		if (log.isInfoEnabled())
			loginfo("routePreference="+routePreference.name());
	}

	public void setRoadPreference(String v) {
		this.roadPreference = RoadPreference.fromValue(v);
		if (log.isInfoEnabled())
			loginfo("roadPreference="+roadPreference.name());
	}
	public void setCountry(String v) {
		this.country = v;
		if (log.isInfoEnabled())
			loginfo("country="+country);
	}
	
	static int s_nextReq = 1;
	
	@Override
	public com.oracle.cep.cartridge.spatial.Geometry route(Collection<Position> points) {
		int requestId = s_nextReq++;
		
		if (log.isInfoEnabled())
		{
			loginfo("Route");
			for (Position p : points)
			{
				log.info(p.longitude + "," + p.latitude);
			}
		}
		String reqstr = null;
		ObjectFactory fac = new ObjectFactory();
		RouteRequest req = fac.createRouteRequest();
		req.setId(BigInteger.valueOf(requestId));
		req.setRoutePreference(routePreference);
		req.setRoadPreference(roadPreference);
		req.setReturnDrivingDirections(false);
		req.setReturnLocations(false);
		req.setDistanceUnit(DistanceUnit.MILE);
		req.setTimeUnit(TimeUnit.MINUTE);
		req.setReturnRouteGeometry(true);
		int n = 1;
		for (Position p : points)
		{
			InputLocationType inp= fac.createInputLocationType();
			inp.setId(Integer.toString(n));
			inp.setCountry(country);
			inp.setLongitude(Double.toString(p.longitude));
			inp.setLatitude(Double.toString(p.latitude));
			
			RouterInputLocation r = fac.createRouterInputLocation();
			r.setInputLocation(inp);

			if (n == 1)
			{
				req.setStartLocation(r);
			}
			else if (n == points.size())
			{
				req.setEndLocation(r);
			} else
			{
				List<RouterInputLocation> l = req.getLocation();
				l.add(r);
			}
			n++;
		}		
		try {
			JAXBContext  jaxbContext = JAXBContext.newInstance(RouteRequest.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	 
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter w = new StringWriter();
		    JAXBElement<RouteRequest> e = fac.createRouteRequest(req);
			jaxbMarshaller.marshal(e, w);
			reqstr = w.toString();
			if (log.isDebugEnabled())
			{
				log.debug(reqstr);
			}
		} catch (JAXBException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Failed to create route request: "+e1.toString(), e1);
		}
		String res = "";
		try {
			res = requestPost(reqstr);
			JAXBContext jaxbContext = JAXBContext.newInstance(RouteResponse.class);
			if (log.isDebugEnabled())
			{
				log.debug(res);
			}
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			StringReader r = new StringReader(res);
			//add XmlRoot to RouteResponse after re-run jaxb
			//javax.xml.bind.UnmarshalException: unexpected element (uri:"", local:"route_response"). Expected elements are (none)
			RouteResponse result = (RouteResponse) jaxbUnmarshaller.unmarshal(r);
			RouteType route = result.getRoute();
			Geometry geom = route.getRouteGeometry();
			LineString line = geom.getLineString();
			String coords = line.getCoordinates();
			String delim = " \n\r\t ";
			List<Double> ords = new LinkedList<Double>();
			StringTokenizer st = new StringTokenizer(coords,delim);
			while (st.hasMoreTokens()) 
			{
			    String s= st.nextToken();
			    String[] v = s.split(",");
			    double lon = Double.parseDouble(v[0].trim());
			    double lat = Double.parseDouble(v[1].trim());
			    ords.add(lon);
			    ords.add(lat);
			}
			double[] ordsarray = new double[ords.size()];
			int i = 0;
			for (Double d : ords) ordsarray[i++] = d;
			return com.oracle.cep.cartridge.spatial.Geometry.createLinearLineString(GeodeticParam.LAT_LNG_WGS84_SRID, ordsarray);
		 } catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to get route\n"+res+"\n"+e.toString(), e);
		}		
	}

	@Override
	public Collection<com.oracle.cep.cartridge.spatial.Geometry> route(Collection<Position>[] requests) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String requestPost(String request) throws Exception
	{
		long ts = System.currentTimeMillis();

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		String urlParameters = "xml_request="+request;
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		long te = System.currentTimeMillis();
		log.info("Routing took "+(te-ts)+ " msec");
		return response.toString();		
	}
	
	public static void main(String[] args)
	{
		OracleSpatialRouteProvider osr = new OracleSpatialRouteProvider();
		List<Position> points = new ArrayList<Position>();
		points.add(new Position(-71.07968592592593, 42.34703962962963));
		points.add(new Position(-74.01588285714286, 40.711357142857146));
		com.oracle.cep.cartridge.spatial.Geometry result = osr.route(points);
		for (Position p : points)
			System.out.println(p.toGeometry().toJsonString());
		System.out.println(result.toJsonString());
	}
}
