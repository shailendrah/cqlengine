package com.oracle.cep.cartridge.spatial;

import org.junit.Test;

import static com.oracle.cep.cartridge.spatial.GeomUtil.Direction;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author santkumk
 */
public class GeomUtilTest {
    @Test
    public void direction() throws Exception {
        assertEquals(GeomUtil.direction(37.5297021, -122.2658318, 37.53995459, -122.25295559), Direction.NE);// http://www.geomidpoint.com/destination/viewmap.html?37.53995459&-122.25295559&37.5297021&-122.2658318
        assertEquals(GeomUtil.direction(Geometry.createPoint(8307, -122.2658318, 37.5297021), Geometry.createPoint(8307, -122.25295559, 37.53995459)), Direction.NE);
        assertEquals(GeomUtil.directionAsString(Geometry.createPoint(8307, -122.2658318, 37.5297021), Geometry.createPoint(8307, -122.25295559, 37.53995459)), "44.87 degree East of North (NE)");
        assertEquals(GeomUtil.direction(37.733838960107775, -122.460930317559, 37.73451775947836, -122.44857069862982), Direction.NE);
        assertEquals(GeomUtil.direction(37.733838960107775, -122.460930317559, 37.74334158497128, -122.45320555572826), Direction.NE);
        assertEquals(GeomUtil.direction(37.733838960107775, -122.460930317559, 37.743070098331685, -122.47225996824409), Direction.NW);
        assertEquals(GeomUtil.direction(37.733838960107775, -122.460930317559, 37.72705062413469, -122.47740980946459), Direction.SW);
        assertEquals(GeomUtil.direction(37.733838960107775, -122.460930317559, 37.72419933746067, -122.44839903725581), Direction.SE);
        assertEquals(GeomUtil.direction(37.733838960107775, -122.460930317559, 37.733838960107775, -122.43947264580696), Direction.NE);
        assertEquals(GeomUtil.direction(37.733838960107775, -122.460930317559, 37.74388455526307, -122.460930317559), Direction.N);
    }

    @Test
    public void testShapeMBR() throws Exception {
        double[] mbr = GeomUtil.shapeMBR(37.5297021, -122.2658318, new double[]{1000, 2000});
        double[] expMBR = new double[]{37.52520494990282, -122.27717132958765, 37.53419816587395, -122.25449158684421};
        assertArrayEquals(expMBR, mbr, 0.00001);
    }

}