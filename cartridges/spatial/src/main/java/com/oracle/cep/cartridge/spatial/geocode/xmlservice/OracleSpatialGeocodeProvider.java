package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import org.apache.commons.logging.Log;
import org.xml.sax.InputSource;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.AddressLineType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.AddressListType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.GdfFormType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.GenFormType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.GeocodeRequest;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.InputAddressType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.InputLocationType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.ObjectFactory;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.UnformattedType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.UsForm1Type;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.req.UsForm2Type;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.resp.GeocodeResponse;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.resp.GeocodeType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.resp.MatchType;
import com.oracle.cep.cartridge.spatial.geocode.osgxml.resp.OutputAddressType;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeAddress.AddressType;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeMatches.GeocodeMatch;


public class OracleSpatialGeocodeProvider implements GeocodeProvider {

    protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);
	
	String url = System.getProperty("osa.spatial.url");
	String country = "us";
	private CacheProvider<GeocodeAddress, GeocodeMatches> geoCache = new GeoLRUCache<>(500);
	private CacheProvider<GeocodePosition, GeocodeAddress> reverseGeoCache = new GeoLRUCache<>(500);
	
	private static GeocodeProvider s_instance;
	
	public static synchronized GeocodeProvider getInstance(){
		if(s_instance == null)
			s_instance = new OracleSpatialGeocodeProvider();
		return s_instance;
	}
	
	public static synchronized GeocodeProvider getInstance(String geocodeServerUrl){
		OracleSpatialGeocodeProvider instance = (OracleSpatialGeocodeProvider) getInstance();
		instance.setUrl(geocodeServerUrl);
		return instance;
	}
	
	private void loginfo(String msg)
	{
		log.info(this.getClass().getName() + ":" + msg);
	}
	public void setUrl(String url) {
		this.url = url;
		if (log.isInfoEnabled())
			loginfo("url="+url);
	}

	public void setCountry(String v) {
		this.country = v;
		if (log.isInfoEnabled())
			loginfo("country="+country);
	}
	
	public void setCacheSize(int cacheSize){
		geoCache.setSize(cacheSize);
		reverseGeoCache.setSize(cacheSize);
	}
	static int s_nextReq = 1;

	@Override
	public GeocodeMatches geocode(GeocodeAddress inputAddress) {
		
		GeocodeMatches geocodeMatches = geoCache.get(inputAddress);
		if(geocodeMatches != null)
			return geocodeMatches;
		
		String reqStr = null;

		ObjectFactory reqFac = new ObjectFactory();
		GeocodeRequest geoCodeReq = reqFac.createGeocodeRequest();

		AddressListType addressList = reqFac.createAddressListType();
		List<GeocodeAddress> address = new ArrayList<GeocodeAddress>();
		address.add(inputAddress);
		List<InputLocationType> locationList = createInputLocationList(
				address, reqFac);
		addressList.getInputLocation().addAll(locationList);
		geoCodeReq.setAddressList(addressList);

		reqStr = getRequestXmlString(reqStr, geoCodeReq);
		GeocodeMatches geomatch = getGeoResponse(reqStr).get(0);
		geoCache.put(inputAddress, geomatch);
		return geomatch;
	}
	
	private List<GeocodeMatches> getGeoResponse(String reqStr) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
		try {
			spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		}catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
			throw new RuntimeException(e.getStackTrace().toString());
		}
		
		List<GeocodeMatches> geoResponses = new ArrayList<>();
		String res = "";
		try {
			res = requestPost(reqStr);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(GeocodeResponse.class);
			if (log.isDebugEnabled())
			{
				log.debug(res);
			}
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			StringReader r = new StringReader(res);
			Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource(r));
			GeocodeResponse geoCodeResponse = (GeocodeResponse) jaxbUnmarshaller.unmarshal(xmlSource);
			List<GeocodeType> geocode = geoCodeResponse.getGeocode();
			for(GeocodeType type : geocode){
				GeocodeMatches response = new GeocodeMatches();
				List<MatchType> matches = type.getMatch();
				for(MatchType match : matches){
					Integer matchCode = Integer.parseInt(match.getMatchCode());
					if(matchCode != 0 ){
						OutputAddressType outputAddress = match.getOutputAddress();
						Double lat = Double.parseDouble(match.getLatitude().trim());
						Double lon = Double.parseDouble(match.getLongitude().trim());
						Geometry loc = Geometry.createPoint(8307, lon, lat);
						GeocodeAddress addr = getOutputAddress(outputAddress);
						GeocodeMatch outputMatch = response.new GeocodeMatch(lat, lon, loc, addr);
						outputMatch.match_code = matchCode;
						response.add(outputMatch);
					} else {
						GeocodeMatch errorMatch = response.new GeocodeMatch();
						errorMatch.match_code = matchCode;
						errorMatch.error_message = match.getErrorMessage();
						response.add(errorMatch);
					}
				}
				geoResponses.add(response);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to get geocoding\n"+res+"\n"+e.toString(), e);
		}
		
		return geoResponses;
	}
	
	private GeocodeAddress getOutputAddress(OutputAddressType outputAddress) {
		GeocodeAddress output = new GeocodeAddress(AddressType.OUTPUT);
		output.name = outputAddress.getName();
		output.houseNumber = outputAddress.getHouseNumber();
		output.street = outputAddress.getStreet();
		output.builtupArea = outputAddress.getBuiltupArea();
		output.order1Area = outputAddress.getOrder1Area();
		output.order8Area = outputAddress.getOrder8Area();
		output.country = outputAddress.getCountry();
		output.postalCode = outputAddress.getPostalCode();
		output.postalAddonCode = outputAddress.getPostalAddonCode();
		output.side = outputAddress.getSide();
		output.percent = outputAddress.getPercent();
		output.edgeId = outputAddress.getEdgeId();
		return output;
	}
	private String getRequestXmlString(String reqStr, Object geoCodeReq) {
		try {
			JAXBContext jaxbContext = JAXBContext
					.newInstance(geoCodeReq.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter w = new StringWriter();
			jaxbMarshaller.marshal(geoCodeReq, w);
			reqStr = w.toString();
			if (log.isDebugEnabled()) {
				log.debug(reqStr);
			}
		} catch (JAXBException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Failed to create geocoding request: "
					+ e1.toString(), e1);
		}
		return reqStr;
	}

	private List<InputLocationType> createInputLocationList(
			List<GeocodeAddress> addressList, ObjectFactory reqFac) {

		List<InputLocationType> locationList = new ArrayList<>();
		for(GeocodeAddress address : addressList){
			int requestId = s_nextReq++;
			InputAddressType addressType = createInputAddress(address,reqFac);;
			InputLocationType locationType = reqFac.createInputLocationType();
			locationType.setId(Integer.toString(requestId));
			locationType.setInputAddress(addressType);
			locationList.add(locationType);
		}
		return locationList;
	}

	private List<InputLocationType> createInputPointList(
			GeocodePosition point, ObjectFactory reqFac) {

		List<InputLocationType> locationList = new ArrayList<>();
			int requestId = s_nextReq++;
			InputLocationType locationType = reqFac.createInputLocationType();
			locationType.setId(Integer.toString(requestId));
			locationType.setLatitude(String.valueOf(point.latitude));
			locationType.setLongitude(String.valueOf(point.longitude));
			locationType.setCountry(point.country);
			locationList.add(locationType);
		return locationList;
	}

	private InputAddressType createInputAddress(GeocodeAddress address,
			ObjectFactory reqFac) {
		InputAddressType addressType = reqFac.createInputAddressType();
		AddressType type = address.type;
		addressType.setMatchMode(address.matchMode);
		if(AddressType.US_FORM1.equals(type)){
			UsForm1Type us1 = new UsForm1Type();
			us1.setName(address.name);
			us1.setStreet(address.street);
			us1.setIntersectingStreet(address.intersectingStreet);
			us1.setLastline(address.lastline);
			addressType.setUsForm1(us1);
		}
		else if(AddressType.US_FORM2.equals(type)){
			UsForm2Type us2 = new UsForm2Type();
			us2.setName(address.name);
			us2.setCity(address.city);
			us2.setState(address.state);
			us2.setStreet(address.street);
			us2.setIntersectingStreet(address.intersectingStreet);
			us2.setZipCode(address.zipCode);
			addressType.setUsForm2(us2);
		}
		else if(AddressType.GDF_FORM.equals(type)){
			GdfFormType gdf = new GdfFormType();
			gdf.setBuiltupArea(address.builtupArea);
			gdf.setCountry(address.country);
			gdf.setIntersectingStreet(address.intersectingStreet);
			gdf.setName(address.name);
			gdf.setOrder1Area(address.order1Area);
			gdf.setOrder2Area(address.order2Area);
			gdf.setOrder8Area(address.order8Area);
			gdf.setPostalAddonCode(address.postalAddonCode);
			gdf.setPostalCode(address.postalCode);
			gdf.setPostalCode(address.postalCode);
			gdf.setStreet(address.street);
			addressType.setGdfForm(gdf);
		}
		else if(AddressType.GEN_FORM.equals(type)){
			GenFormType gen = new GenFormType();
			gen.setCity(address.city);
			gen.setCountry(address.country);
			gen.setIntersectingStreet(address.intersectingStreet);
			gen.setName(address.name);
			gen.setPostalAddonCode(address.postalAddonCode);
			gen.setPostalCode(address.postalCode);
			gen.setRegion(address.region);
			gen.setStreet(address.street);
			gen.setSubArea(address.subArea);
			addressType.setGenForm(gen);
		} else if(AddressType.UNFORMATTED.equals(type)){
			UnformattedType unform = reqFac.createUnformattedType();
			for (String line : address.unformatted) {
				AddressLineType adrLine = reqFac.createAddressLineType();
				adrLine.setValue(line);
				unform.getAddressLine().add(adrLine);
			}
			addressType.setUnformatted(unform);
		}
		return addressType;
	}
	@Override
	public GeocodeAddress reverseGeocode(GeocodePosition point) {
		
		GeocodeAddress in_cache = reverseGeoCache.get(point);
		if(in_cache != null)
			return in_cache;
		
		String reqStr = null;
		ObjectFactory reqFac = new ObjectFactory();
		GeocodeRequest geoCodeReq = reqFac.createGeocodeRequest();

		AddressListType addressList = reqFac.createAddressListType();
		List<InputLocationType> locationList = createInputPointList(point,
				reqFac);
		addressList.getInputLocation().addAll(locationList);
		geoCodeReq.setAddressList(addressList);

		reqStr = getRequestXmlString(reqStr, geoCodeReq);
		GeocodeMatches geocodeMatches = getGeoResponse(reqStr).get(0);
		GeocodeMatch geocodeMatch = geocodeMatches.get(0);
		if( geocodeMatch.match_code == 0 ){
			throw new RuntimeException("Failed to get reverse geocoding : match_code : "+ geocodeMatch.match_code +
					" Error Message : " + geocodeMatch.error_message);
		}
		GeocodeAddress address = geocodeMatch.address;
		reverseGeoCache.put(point, address);
		return address;
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
 
//		int responseCode = con.getResponseCode();
 
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

	public static void main(String[] args) {
		GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
		
		System.out.println("=========== Geocode Request ==========");
		GeocodeAddress input = GeocodeAddress.createUsForm1Address(null, "10 fifth ave", null, "New York, NY");
		GeocodeMatches geocodeMatches = geoProvider.geocode(input);
		System.out.println(geocodeMatches.get(0).address.getOutputAddress());
	
		System.out.println("=========== Geocode Request =========="); 
		List<String> addressList = new ArrayList<>();
		addressList.add("Mr. Larry Ellison");
		addressList.add("Oracle Corp.");
		addressList.add("500 Oracle Pky");
		addressList.add("Redwood city");
		addressList.add("CA");
		GeocodeAddress unformInput = GeocodeAddress.createUnformattedAddress(addressList);
		GeocodeMatches unMatch = geoProvider.geocode(unformInput);
		System.out.println(unMatch.get(0).address.getOutputAddress());
		
		System.out.println("========== Reverse Gecocode Request =========");
		GeocodePosition point = new GeocodePosition(-122.26193971893862, 37.53195483966782);
		GeocodeAddress reverseGeocCode = geoProvider.reverseGeocode(point);
		System.out.println(reverseGeocCode.getOutputAddress());
		
		System.out.println("=========== Geocode Error Request ==========");
		GeocodeAddress input1 = GeocodeAddress.createUsForm1Address(null, "n11n", null, "11pp");
		GeocodeMatches geocodeMatches1 = geoProvider.geocode(input1);
		System.out.println(geocodeMatches1.getSize());
	}		

}
