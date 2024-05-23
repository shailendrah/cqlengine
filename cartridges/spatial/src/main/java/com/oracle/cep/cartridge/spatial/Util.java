/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/Util.java /main/1 2015/10/01 22:29:58 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/16/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/Util.java /main/1 2015/10/01 22:29:58 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial;

public class Util
{
	public static int minId(int a, int b) { return a < b ? a:b;}
	public static long minId(long a, long b) { return a < b ? a:b;}
	public static float minId(float a, float b) { return a < b ? a:b;}
	public static double minId(double a, double b) { return a < b ? a:b;}
	public static String minId(String a, String b) 
	{ 
		int n = 0;
		if (a == null) n = b == null ? 0:1;
		else if (b == null) n = a == null ? 0:-1;
		else n = a.compareTo(b);
		return n < 0 ? a:b;
	}
	public static byte[] minId(byte[] a, byte[] b) 
	{ 
		int n = 0;
		if (a == null) n = b == null ? 0:1;
		else if (b == null) n = a == null ? 0:-1;
		else {
			String s = minId(new String(a), new String(b));
			return s.getBytes();
		}
		return n < 0 ? a:b;
	}
	public static int maxId(int a, int b) { return a > b ? a:b;}
	public static long maxId(long a, long b) { return a > b ? a:b;}
	public static float maxId(float a, float b) { return a > b ? a:b;}
	public static double maxId(double a, double b) { return a > b ? a:b;}
	public static String maxId(String a, String b) 
	{ 
		int n = 0;
		if (a == null) n = b == null ? 0:1;
		else if (b == null) n = a == null ? 0:-1;
		else n = a.compareTo(b);
		return n > 0 ? a:b;
	}
	public static byte[] maxId(byte[] a, byte[] b) 
	{ 
		int n = 0;
		if (a == null) n = b == null ? 0:1;
		else if (b == null) n = a == null ? 0:-1;
		else {
			String s = maxId(new String(a), new String(b));
			return s.getBytes();
		}
		return n > 0 ? a:b;
	}
}
