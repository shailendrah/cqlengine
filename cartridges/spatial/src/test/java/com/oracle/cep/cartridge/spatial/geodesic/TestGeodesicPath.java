/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geodesic/TestGeodesicPath.java /main/1 2015/06/18 19:14:13 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      06/08/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geodesic/TestGeodesicPath.java /main/1 2015/06/18 19:14:13 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geodesic;

import junit.framework.TestCase;

public class TestGeodesicPath extends TestCase {
	public void testDirect() {
		ReferenceSystem geod = ReferenceSystem.WGS84;
		double lat1 = 40.640, lon1 = -73.779, // JFK
		lat2 = 1.359, lon2 = 103.989; // SIN
		Geodesic g = geod.Inverse(lat1, lon1, lat2, lon2,
				ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
		GeodesicPath line = new GeodesicPath(geod, lat1, lon1, g.azi1,
				ReferenceSystem.DISTANCE_IN | ReferenceSystem.LONGITUDE);
		double s12 = g.s12, a12 = g.a12, ds0 = 500e3; // Nominal distance
														// between points = 500
														// km
		double[] expected1 = {
				40.64, -73.779,
				45.088624522948315, -73.4163912487096,
				49.53243435521156, -72.9926557320697,
				53.97096253429304, -72.48430287040068,
				58.40353976434537, -71.85489098898535,
				62.82905934003561, -71.04452466778628,
				67.24547195373593, -69.94724090357695,
				71.64856286681425, -68.35623186483942,
				76.02845249253366, -65.80772484891894,
				80.3567290132899, -61.01489048962908,
				84.51420549018646, -48.834924316652554,
				87.45474332671365, 3.8940143788450285,
				85.29525950923802, 71.94358213809579,
				81.22103811744252, 87.60160059653253,
				76.91141529030635, 93.21228048415563,
				72.53859298991509, 96.06333035970758,
				68.13910398636216, 97.79733028285952,
				63.72494286554837, 98.9730125867593,
				59.301058336202615, 99.83082330184729,
				54.86980145924301, 100.49102303937633,
				50.43241558100798, 101.02040715447237,
				45.98963410041734, 101.45907366928472,
				41.541952827576424, 101.8325944765418,
				37.08976587764101, 102.15809755054525,
				32.63343762975144, 102.4475403141038,
				28.173341767719887, 102.70958072995487,
				23.709881876563525, 102.95070239501464,
				19.243500881260854, 103.17592172947425,
				14.774683272647843, 103.3892511049286,
				10.30395242966336, 103.59401483935383,
				5.8318645162912315, 103.79307473649482,
				1.3590000000000086, 103.989
		};
		int num = (int) (Math.ceil(s12 / ds0)); // The number of intervals
		{
			// Use intervals of equal length
			double ds = s12 / num;
			int j = 0;
			for (int i = 0; i <= num; ++i) {
				g = line.Position(i * ds, ReferenceSystem.LATITUDE
						| ReferenceSystem.LONGITUDE);
				System.out.println(i + " " + g.lat2 + " " + g.lon2);
				assertTrue( Math.abs(g.lat2 - expected1[j++]) < ReferenceSystem.epsilon);
				assertTrue( Math.abs(g.lon2 - expected1[j++]) < ReferenceSystem.epsilon);
			}
		}
		
		double[] expected2 = {
				40.64, -73.779,
				45.086460702432234, -73.41658104649869,
				49.529265950817006, -72.99298479299394,
				53.967930754066806, -72.48468651138582,
				58.40173974638525, -71.85517834684403,
				62.829512612598435, -71.04442927744107,
				67.24909866597751, -69.94617474930955,
				71.65615008336188, -68.35285314960993,
				76.04060226175014, -65.79845477946834,
				80.37369610185934, -60.9874638938742,
				84.53479915283033, -48.72370094241358,
				87.45968986973789, 4.537049623094603,
				85.26581025420175, 72.16804012791195,
				81.18231164033647, 87.67710003521418,
				76.86653839694128, 93.25112563051263,
				72.48856853821259, 96.08780242677435,
				68.08473896886909, 97.81454059342744,
				63.66709059644227, 98.98594539669602,
				59.24065979013169, 99.84096327970059,
				54.807883055166826, 100.49919890522867,
				50.370072897870024, 101.02712123661979,
				45.92800951519798, 101.46464969677137,
				41.48221004067657, 101.83725100899576,
				37.03306284539249, 102.16198745742366,
				32.58089894911347, 102.45077336497715,
				28.126031385417072, 102.71223718011805,
				23.66877691981883, 102.95284148439606,
				19.209467357950057, 103.17758716071171,
				14.748454336814499, 103.39047464371274,
				10.286109842084779, 103.5948186937903,
				5.822823844783122, 103.79347310226322,
				1.3590000000000086, 103.989
		};
		{
			// Slightly faster, use intervals of equal arc length
			double da = a12 / num;
			int j = 0;
			for (int i = 0; i <= num; ++i) {
				g = line.ArcPosition(i * da, ReferenceSystem.LATITUDE
						| ReferenceSystem.LONGITUDE);
				System.out.println(i + " " + g.lat2 + " " + g.lon2);
				assertTrue( Math.abs(g.lat2 - expected2[j++]) < ReferenceSystem.epsilon);
				assertTrue( Math.abs(g.lon2 - expected2[j++]) < ReferenceSystem.epsilon);
			}
		}
	}
}