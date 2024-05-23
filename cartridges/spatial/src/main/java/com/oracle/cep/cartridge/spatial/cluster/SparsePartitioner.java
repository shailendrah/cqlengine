package com.oracle.cep.cartridge.spatial.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.RTree;

import org.apache.commons.logging.Log;

import com.oracle.cep.cartridge.spatial.Geometry;

public class SparsePartitioner implements GeomPartitioner{
	private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);
	private RTree rtree;
	private int[] result = new int[1];
	private boolean exactMatch;
	private int maxPartition = 0;
	private boolean overlapping;

	public SparsePartitioner() {
		super();
		int nd = 2;
		int ns = 6;
		int mf = 1;

		rtree = new RTree(nd, ns, mf);
	}

	public void setExactMatch(boolean exactMatch) {
		log.debug("setExactMatch " + exactMatch);
		this.exactMatch = exactMatch;
	}
	
	public void setOverlapping(boolean overlapping) {
		this.overlapping = overlapping;
	}

	public void setPartitions(Partitions partitionList) {
		maxPartition = 0;
		for (Partition p : partitionList) {
			if (!(p instanceof Partition))
				throw new IllegalArgumentException("Expecting "
						+ Partition.class.getName() + " but got "
						+ p.getClass().getName());
			rtree.addEntry(Geometry.get2dMbr(p.getGeometry()), p);
			log.debug("add partition : " + p.toString());
			if (p.getPartitionNo() > maxPartition)
				maxPartition = p.getPartitionNo();
		}
	}

	public int[] partition(JGeometry geom) {
		if (geom == null)
			throw new IllegalArgumentException("Invalid Geometry " + geom);
		double[][] mbr = Geometry.get2dMbr(geom);
		ArrayList scanres = new ArrayList();
		rtree.search(mbr, scanres);

		List<Integer> res = null;
		if (overlapping)
			res = new ArrayList<Integer>(4);

		Iterator i = scanres.iterator();
		while (i.hasNext()) {
			Partition n = (Partition) i.next();
			int partno = -1;
			if (!exactMatch)
				partno = n.getPartitionNo();
			if (n.contain(geom))
				partno = n.getPartitionNo();
			if (partno >= 0) {
				if (overlapping) {
					res.add(partno);
				} else {
					result[0] = partno;
					return result;
				}
			}
		}
		if (overlapping) {
			int sz = res.size();
			if (sz != 0) {
				int[] r = new int[sz];
				for (int j = 0; j < sz; j++)
					r[j] = res.get(j);
				return r;
			}
		}
		result[0] = 0;
		return result;
	}

	@Override
	public int[] partition(double lng, double lat) {
		JGeometry point = Geometry.createPoint(lng,lat);
		return partition(point);
	}

}
