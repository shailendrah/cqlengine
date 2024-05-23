package com.oracle.cep.cartridge.spatial.cluster;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import com.oracle.cep.cartridge.spatial.CartridgeRegistry;
import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

/**
 * @author santkumk
 */
public class SparsePartitionerTest {

	@Before
	public void setUp() throws Exception {
		SpatialCartridge.createInstance(new CartridgeRegistry());
	}
	
	@Test
	public void testPartitionsOverlapping() throws Exception{
		String[] fences = new String[]{
				"SF_fence1,Polygon,-122.403547,37.789636,-122.403118,37.785566,-122.399427,37.785023,-122.397109,37.792281,-122.403461,37.789568,-122.403547,37.789636",
				"SF_fence2,Polygon,-122.410156,37.797707,-122.411100,37.793773,-122.403633,37.793230,-122.410242,37.797775,-122.410156,37.797707,",
				"SF_fence3,Polygon,-122.418653,37.790110,-122.418653,37.786651,-122.415220,37.785294,-122.411615,37.787058,-122.413846,37.791060,-122.418567,37.790314,-122.418653,37.790110",
				"SF_fence4,Polygon,-122.412988,37.782717,-122.414104,37.787329,-122.408525,37.788686,-122.405950,37.783056,-122.412988,37.782988,-122.412988,37.782717" };
		Map<String,int[]> expPartitons = new HashMap<>();
		expPartitons.put("SF_fence1", new int[]{1,3});
		expPartitons.put("SF_fence2", new int[]{2,3});
		expPartitons.put("SF_fence3", new int[]{0,2});
		expPartitons.put("SF_fence4", new int[]{0,1});

		SparsePartitioner sp = new SparsePartitioner();
		sp.setOverlapping(true);
		double[][] boundary = Geometry.getMBRBoundary(getFenceMBRs(fences), 100);
//		System.out.println("Boundary: " + boundary[0][0] + "," + boundary[0][1] + "," + boundary[1][0] + "," + boundary[1][1]);
		Partitions grids = new RangePartitionGrid(boundary, 2, 2);
//		Iterator<Partition> git = grids.iterator();
//		while(git.hasNext())System.out.println("Partition:" + git.next());
		sp.setPartitions(grids);
		Map<String, int[]> plist = new HashMap<>();
		for (String fence : fences) {
			FenceGeom g = FenceGeom.getFenceBufferedGeom(fence,100);
			int[] p = sp.partition(g.getGeom());
			plist.put(g.getId(),p);
			System.out.println("Partitions for Geometry " + g.getId() + " is = " + Arrays.toString(p));
			System.out.println(Arrays.toString(g.getGeom().getOrdinatesArray()));
		}
		assertEquals("containing partitions of all the fences only",plist.size(),expPartitons.size());
		for (String fence : expPartitons.keySet()) {
			assertEquals(fence + " partitions doesn't match" ,expPartitons.get(fence).length,plist.get(fence).length);
			assertArrayEquals(fence + " partitions doesn't match" ,expPartitons.get(fence),plist.get(fence));
		}
	}

	
	private List<double[][]> getFenceMBRs(String[] fences) {
		List<double[][]> mbrs = new ArrayList<>();
		for (String fence : fences) {
			FenceGeom g = FenceGeom.getFenceGeom(fence);
			double[][] m = Geometry.get2dMbr(g.getGeom());
			double[][] mbr= new double[][]{{m[0][0],m[1][0]},{m[0][1],m[1][1]}};
//			System.out.println(scoord[0] + " MBR: " + mbr[0][0] + "," + mbr[0][1] + "," + mbr[1][0] + "," + mbr[1][1]);
			mbrs.add(mbr);
		}
		return mbrs;
	}

}
