package com.oracle.cep.cartridge.spatial.cluster;

import java.util.Arrays;

import org.apache.commons.logging.Log;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.spatial.geometry.JGeometry;

/**
 * It simply represents a geometry of a specific partition (i.e., nth partition)
 * 
 * @author santkumk
 *
 */
public class Partition {
	protected int m_partitionNo;
	protected JGeometry m_geometry;
	protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

	public Partition(int pno, JGeometry geom) {
		m_partitionNo = pno;
		m_geometry = geom;
	}

	/**
	 * @return partition number
	 */
	public int getPartitionNo() {
		return m_partitionNo;
	}

	/**
	 * @return geometry object of this partition
	 */
	public JGeometry getGeometry() {
		return m_geometry;
	}

	/**
	 * check whether the given geometry belongs to this partition
	 * @param g
	 * @return true if g is inside this partitioned geometry, false otherwise
	 */
	public boolean contain(JGeometry g) {
		try {
			return (g.anyInteract(m_geometry, 0.0d, "true"));
		} catch (Exception e) {
			log.error(e);
			return false;
		}
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		 b.append(this.getClass().getSimpleName());
		 b.append(" ");
		 b.append(m_partitionNo);
		 b.append("\n");
		double[] coords = m_geometry.getOrdinatesArray();
		b.append(Arrays.toString(coords));
		return b.toString();
	}
}