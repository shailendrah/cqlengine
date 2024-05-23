/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/Ellipsoid.java /main/1 2015/10/01 22:29:51 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/Ellipsoid.java /main/1 2015/10/01 22:29:51 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.spatial.rcs;

public class Ellipsoid
{
	double a;	//major axis
	double b;	//minor axis
	double f;	//flattenin
	public Ellipsoid(double a, double b, double f)
	{
		this.a = a;
		this.b = b;
		this.f = f;
	}
	
	public static final Ellipsoid WGS84 = new Ellipsoid(6378137, 6356752.31425, 1/298.257223563);
	public static final Ellipsoid GRS80 = new Ellipsoid(6378137, 6356752.31414, 1/298.257222101);
	public static final Ellipsoid Airy1830 = new Ellipsoid(6377563.396, 6356256.909, 1/299.3249646);
	public static final Ellipsoid AiryModified = new Ellipsoid(6377340.189, 6356034.448, 1/299.3249646);
	public static final Ellipsoid Intl1924 = new Ellipsoid(6378388, 6356911.946, 1/297);
	public static final Ellipsoid Bessel1841 = new Ellipsoid(6377397.155, 6356078.963, 1/299.152815351);
}