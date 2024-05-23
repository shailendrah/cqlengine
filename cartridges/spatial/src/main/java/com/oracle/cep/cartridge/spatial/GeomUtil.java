package com.oracle.cep.cartridge.spatial;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import org.apache.commons.logging.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author santkumk
 */
public class GeomUtil {

    // Radius of the earth in meters.
    private static final double EARTH_RADIUS_IN_METERS = 6371000.0;
    private static final Log log = LogUtil.getLogger(LoggerType.CUSTOMER);
    private static final String N = "North (N)";
    private static final String E = "East (E)";
    private static final String W = "West (W)";
    private static final String S = "South (S)";
    private static final String N_OF_E = " degree East of North (NE)";
    private static final String N_OF_W = " degree West of North (NW)";
    private static final String S_OF_E = " degree East of South (SE)";
    private static final String S_OF_W = " degree West of South (SW)";

    /**
     * calculate bearing i.e.,  direction or an angle, between the north-south line of earth or meridian and the line connecting the
     * previous (lat1, lng1) and current (lat2, lng2) locations.
     * ref: https://en.wikipedia.org/wiki/Bearing_(navigation)
     * @param lat1 - latitude of previous location
     * @param lng1 - longitude of previous location
     * @param lat2 - latitude of current location
     * @param lng2 - longitude of current location.
     * @return the angle in degree between 0 to 360.
     */
    public static double bearing(double lat1, double lng1, double lat2, double lng2){
        double lat1_radian = Math.toRadians(lat1);
        double lat2_radian = Math.toRadians(lat2);
        double lngDiff_radian= Math.toRadians(lng2-lng1);
        double y= Math.sin(lngDiff_radian)*Math.cos(lat2_radian);
        double x=Math.cos(lat1_radian)*Math.sin(lat2_radian)-Math.sin(lat1_radian)*Math.cos(lat2_radian)*Math.cos(lngDiff_radian);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    public enum  Direction {
        E,W,N,S, NE,NW,SE,SW, UNKNOWN
    }

    /**
     * gives the direction of object move from previous location (lat1,lng1) to current location (lat2, lng2)
     * @param lat1 - latitude of previous location
     * @param lng1 - longitude of previous location
     * @param lat2 - latitude of current location
     * @param lng2 - longitude of current location.
     * @return - Direction
     */
    public static Direction direction(double lat1, double lng1, double lat2, double lng2){
        double bearing = bearing(lat1,lng1,lat2,lng2);
        log.info("Bearing for start lat/lng:" + lat1 + "," + lng1 + " and end lat/lng: " + lat2 + "," + lng2 + " is " + bearing);
        return toDirection(bearing);
    }

    /**
     * gives the direction of object as a full text (e.g, 10 degree N of E) from previous location (lat1,lng1) to current location (lat2, lng2)
     * @param lat1 - latitude of previous location
     * @param lng1 - longitude of previous location
     * @param lat2 - latitude of current location
     * @param lng2 - longitude of current location.
     * @return - Direction
     */
    public static String directionAsString(double lat1, double lng1, double lat2, double lng2){
        double bearing = bearing(lat1,lng1,lat2,lng2);
        //bearing = Math.floor(bearing*100)/100;
        log.info("Bearing for start lat/lng:" + lat1 + "," + lng1 + " and end lat/lng: " + lat2 + "," + lng2 + " is " + bearing);
        return toDirectionAsString(bearing);
    }

    /**
     * get the angle for a direction
     * @param direction
     * @return - angle
     */
    private static double toAngle(Direction direction){

        switch (direction) {
            case E: return 0.0;
            case SE: return (270.0 + 360.0) / 2.0 ;
            case S: return 270.0;
            case SW: return (180.0 + 270.0) / 2.0 ;
            case W: return  180.0;
            case NW: return (90.0 + 180.0) / 2.0;
            case N: return 90.0;
            case NE: return 45.0;
        }
        return 0.0;
    }

    /**
     * get the direction for an angle
     * @param bearing - angle
     * @return - Direction
     */
    private static Direction toDirection(double bearing){
        if(bearing == 0.0 || bearing == 360.00)return Direction.N;
        else if(bearing > 270 && bearing < 360) return Direction.NW;
        else if(bearing == 270)return Direction.W;
        else if(bearing <270 && bearing > 180) return  Direction.SW;
        else if(bearing == 180) return Direction.S;
        else if(bearing < 180 && bearing > 90) return  Direction.SE;
        else if(bearing == 90) return Direction.E;
        else if(bearing <90 && bearing >0 ) return Direction.NE;
        else return Direction.UNKNOWN;
    }

    /**
     * get the direction for an angle
     * @param bearing - angle
     * @return - Direction
     */
    private static String toDirectionAsString(double bearing){
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        if(bearing == 0.00 || bearing == 360.00)return N;
        else if(bearing > 270 && bearing < 360) return df.format(360-bearing) + N_OF_W;
        else if(bearing == 270.00)return W;
        else if(bearing <270 && bearing > 180) return  df.format(bearing - 180) + S_OF_W;
        else if(bearing == 180.00) return S;
        else if(bearing < 180 && bearing > 90) return df.format(180 - bearing) + S_OF_E;
        else if(bearing == 90.00) return E;
        else if(bearing <90 && bearing >0 ) return df.format(bearing) + N_OF_E;
        else return Direction.UNKNOWN.toString();
    }

    /**
     * gives the direction of object move from previous location (point1) to current location (point2)
     * @param point1 - point geometry
     * @param point2 - point geometry
     * @return - Direction
     */
    public static Direction direction(Geometry point1, Geometry point2){
        if(!point1.isPoint() || !point2.isPoint()) return Direction.UNKNOWN;
        double[] point1_coords = point1.getOrdinatesArray();
        double[] point2_coords = point2.getOrdinatesArray();

        return direction(point1_coords[1], point1_coords[0], point2_coords[1], point2_coords[0]);
    }

    /**
     * gives the direction of object move from previous location (point1) to current location (point2)
     * @param point1 - point geometry
     * @param point2 - point geometry
     * @return - Direction
     */
    public static String directionAsString(Geometry point1, Geometry point2){
        if(!point1.isPoint() || !point2.isPoint()) return Direction.UNKNOWN.toString();
        double[] point1_coords = point1.getOrdinatesArray();
        double[] point2_coords = point2.getOrdinatesArray();

        return directionAsString(point1_coords[1], point1_coords[0], point2_coords[1], point2_coords[0]);
    }

    /**
     * calulate a new location (nLat, nLng) that is at the specified distance and bearing from the current location (lat, lng).
     * ref: https://en.wikipedia.org/wiki/Haversine_formula
     * @param lat - latitude of current location
     * @param lng - longitude of current location
     * @param distance - distance
     * @param bearing - direction
     * @return the angle in degree between 0 to 360.
     */
    public static double[] pointAtBearing(double lat, double lng, double distance, double bearing){
        if(distance == 0.0) return new double[]{lat,lng};
        double angularDistnace = distance / EARTH_RADIUS_IN_METERS; //angular distance in meter
        double bearing_radian = Math.toRadians(bearing);
        double lat_radian = Math.toRadians(lat);
        double lng_radian = Math.toRadians(lng);
        double b_radian = Math.asin(Math.sin(lat_radian)*Math.cos(angularDistnace) + Math.cos(lat_radian)*Math.sin(angularDistnace)*Math.cos(bearing_radian));
        double x = Math.cos(angularDistnace) - Math.sin(lat_radian) * Math.sin(b_radian);
        double y = Math.sin(bearing_radian)* Math.sin(angularDistnace)*Math.cos(lat_radian);
        double d = lng_radian + Math.atan2(y,x);
        return new double[] {Math.toDegrees(b_radian),(Math.toDegrees(d)+540.0)%360.0 - 180.0};
    }

    /**
     * calulate a new location (nLat, nLng) that is at the specified distance and direction from the current location (lat, lng).
     * @param lat - latitude of current location
     * @param lng - longigude of current location
     * @param distance - distance covered
     * @param direction - direction in which distance covered.
     * @return - new location (array of nLat & nLng)
     */
    public static double[] pointAtBearing(double lat, double lng, double distance, Direction direction){
        log.info("distance:" + distance + " angle:" + toAngle(direction));
        return pointAtBearing(lat, lng, distance, toAngle(direction));
    }

    /**
     * create the rectangular shape around a given position using the dimension (width*height).
     * the width corresponds to the longitude difference (along x-axis) and the height
     * corresponds to the latitude difference (along y-axis).
     * @param lat -latitiude of current location
     * @param lng - longitude of current location
     * @param dimension - dimension of shape, i.e., rectangle (array of width and height).
     * @return - MBR of shape/polygon (array of lat1, lng1, lat2, lng2) around the current location.
     */
    public static double[] shapeMBR(double lat, double lng, double[] dimension){
        double distance_lng = dimension[0] /2;
        double distance_lat = dimension[1] /2;
        log.debug("in shape around lat/lng: " + lat + "," + lng);
        double[] mbr_t = pointAtBearing(lat,lng,distance_lng,Direction.W);
        log.info("intial lat/lng: " + lat + "," + lng + " after " + distance_lng + " meter in Direction W lat/lng: " + mbr_t[0] + "," + mbr_t[1] );
        double[] mbr_0 = pointAtBearing(mbr_t[0], mbr_t[1],distance_lat,Direction.S);
        log.info("intial lat/lng: " + mbr_t[0] + "," + mbr_t[1] + " after " + distance_lat + " meter in Direction S lat/lng: " + mbr_0[0] + "," + mbr_0[1] );
        mbr_t = pointAtBearing(lat,lng,distance_lat,Direction.N);
        log.info("intial lat/lng: " + lat + "," + lng + " after " + distance_lat + " meter in Direction N lat/lng: " + mbr_t[0] + "," + mbr_t[1] );
        double[] mbr_1 = pointAtBearing(mbr_t[0], mbr_t[1],distance_lng,Direction.E);
        log.info("intial lat/lng: " + mbr_t[0] + "," + mbr_t[1] + " after " + distance_lat + " meter in Direction E lat/lng: " + mbr_1[0] + "," + mbr_1[1] );
        return new double[] {mbr_0[0], mbr_0[1], mbr_1[0], mbr_1[1]};
    }

}
