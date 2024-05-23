package com.oracle.cep.cartridge.spatial.cluster;

import oracle.spatial.geometry.JGeometry;

import com.oracle.cep.cartridge.spatial.Geometry;

/**
 * It represents a rectangular area (grid). it contains the geometry (of type
 * rectangle) and the partition number associated with this.
 * 
 * @author santkumk
 *
 */
public class RangePartition extends Partition {

	/**
	 * Create a Range Partition
	 * @param srid - SRID 
	 * @param pno - partition number
	 * @param lblat - lower bound latitude
	 * @param ublat - upper bound latitude
	 * @param lblng - lower bound longitude
	 * @param ublng - upper bound longitude
	 */
	public RangePartition(int srid, int pno, double lblat, double ublat,
			double lblng, double ublng) {
		super(pno, (JGeometry) Geometry.createRectangle(srid, lblng, lblat,
				ublng, ublat));
	}

	/**
	 * create a Range Partition, given a geometry
	 * @param pno - partition number
	 * @param geom - geometry object
	 */
	public RangePartition(int pno, JGeometry geom) {
		super(pno, geom);
	}

	public int compareTo(Partition other) {
		if (!(other instanceof RangePartition))
			throw new IllegalArgumentException("Expecting RangePartition");
		RangePartition o = (RangePartition) other;
		double[] pts = m_geometry.getOrdinatesArray();
		double[] opts = o.m_geometry.getOrdinatesArray();
		if (pts[0] < opts[1])
			return -1;
		if (pts[0] == opts[1]) {
			if (pts[2] < opts[2])
				return -1;
			if (pts[2] == opts[2])
				return 0;
		}
		return 1;
	}
}
