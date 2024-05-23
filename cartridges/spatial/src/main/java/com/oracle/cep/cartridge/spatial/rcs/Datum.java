/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/Datum.java /main/1 2015/10/01 22:29:50 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      08/12/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/Datum.java /main/1 2015/10/01 22:29:50 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rcs;

public class Datum
{
	public static final Datum WGS84 = new Datum(Ellipsoid.WGS84, new Transform());
    public static final Datum OSGB36 = new Datum(Ellipsoid.Airy1830, 
    		new Transform( -446.448,  125.157, -542.060, -0.1502, -0.2470, -0.8421, 20.4894));
    public static final Datum ED50 = new Datum(Ellipsoid.Intl1924, 
    		new Transform(89.5, 93.8, 123.1, 0.0, 0.0, 0.156,-1.2)); 
    public static final Datum Irl1975 = new Datum(Ellipsoid.AiryModified, 
    		new Transform(-482.530,  130.596, -564.557, -1.042, -0.214, -0.631, -8.150));
    public static final Datum TokyoJapan = new Datum(Ellipsoid.Bessel1841, 
    		new Transform(148, -507, -685, 0, 0, 0, 0));
    
	Ellipsoid ellipsoid;
	Transform transform;
	
	public Datum(Ellipsoid e, Transform t)
	{
		ellipsoid = e;
		transform = t;
	}

	public Ellipsoid getEllipsoid() {return ellipsoid;}
	public Transform getTransform() {return transform;}
}
