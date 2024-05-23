package com.oracle.cep.cartridge.spatial.cluster;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import com.oracle.cep.cartridge.spatial.CartridgeRegistry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

public class RangePartitionGridTest {

	@Before
	public void setUp() throws Exception {
		SpatialCartridge.createInstance(new CartridgeRegistry());
	}

	@Test
	public void testGrid22() {
		RangePartitionGrid grid = new RangePartitionGrid(-122.26484674076376,37.53157164966378,1000,2,2);
		
		/*Iterator<Partition> g = grid.iterator();
		while(g.hasNext()){
			System.out.println(g.next());
		}*/
		assertEquals(grid.getPartitionNo(-122.26991075129725,37.52714727651951),0);
		assertEquals(grid.getPartitionNo(-122.25961106885629,37.52714727651951),1);
		assertEquals(grid.getPartitionNo(-122.27076905816735,37.53463452348846),2);
		assertEquals(grid.getPartitionNo(-122.25943940748226,37.53524708315887),3);
	}
	
	@Test
	public void testGrid12() {
		RangePartitionGrid grid = new RangePartitionGrid(-122.26484674076376,37.53157164966378,1000,1,2);
		
		/*Iterator<Partition> g = grid.iterator();
		while(g.hasNext()){
			System.out.println(g.next());
		}*/
		assertEquals(grid.getPartitionNo(-122.26991075129725,37.52714727651951),0);
		assertEquals(grid.getPartitionNo(-122.25961106885629,37.52714727651951),0);
		assertEquals(grid.getPartitionNo(-122.27076905816735,37.53463452348846),1);
		assertEquals(grid.getPartitionNo(-122.25943940748226,37.53524708315887),1);
	}
	
	@Test
	public void testGrid21() {
		RangePartitionGrid grid = new RangePartitionGrid(-122.26484674076376,37.53157164966378,1000,2,1);
		
		/*Iterator<Partition> g = grid.iterator();
		while(g.hasNext()){
			System.out.println(g.next());
		}*/
		assertEquals(grid.getPartitionNo(-122.26991075129725,37.52714727651951),0);
		assertEquals(grid.getPartitionNo(-122.25961106885629,37.52714727651951),1);
		assertEquals(grid.getPartitionNo(-122.27076905816735,37.53463452348846),0);
		assertEquals(grid.getPartitionNo(-122.25943940748226,37.53524708315887),1);
	}
	
	@Ignore
	@Test
	public void testCartesianGrid() {
		RangePartitionGrid grid = new RangePartitionGrid(100,100,10,2,2);
		
		Iterator<Partition> g = grid.iterator();
		while(g.hasNext()){
			System.out.println(g.next());
		}
		System.out.println(grid.getPartitionNo(5,5));
		System.out.println(grid.getPartitionNo(6,6));
		System.out.println(grid.getPartitionNo(7,7));
		System.out.println(grid.getPartitionNo(8,8));
	}
	
	@Test
	public void tesFullGeoGrid() {
		RangePartitionGrid grid = new RangePartitionGrid(new double[][]{{-180,-90},{180,90}},400000,400000);
		
//		Iterator<Partition> g = grid.iterator();
//		while(g.hasNext()){
//			System.out.println(g.next());
//		}
		assertEquals(grid.getPartitionNo(-122.26991075129725,37.52714727651951)%3,0);
		assertEquals(grid.getPartitionNo(-122.27076905816735,37.53463452348846)%3,1);
		assertEquals(grid.getPartitionNo(-122.25943940748226,37.53524708315887)%3,0);
		assertEquals(grid.getPartitionNo(-122.25961106885629,37.52714727651951)%3, 2);
	}
	
}
