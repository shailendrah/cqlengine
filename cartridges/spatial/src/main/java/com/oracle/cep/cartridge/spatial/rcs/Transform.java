/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/Transform.java /main/1 2015/10/01 22:29:51 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/Transform.java /main/1 2015/10/01 22:29:51 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rcs;
public class Transform
{
	double tx, ty, tz, rx, ry, rz, s;
	
	public Transform()
	{
		tx = ty = tx = rx = ry = rz =s = 0.0;
	}
	
	public Transform(double tx, double ty, double tz, double rx, double ry, double rz, double s)
	{
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
		this.rx = rx;
		this.ry = ry;
		this.rz = rz;
		this.s = s;
	}

	public Transform getInverse() {
		return new Transform(-tx, -ty, -tz, -rx, -ry, -rz, -s);
	}
}
