package com.oracle.cep.cartridge.spatial.cluster;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.commons.logging.Log;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.spatial.geometry.JGeometry;

import com.oracle.cep.cartridge.spatial.Geometry;

/**
 * this class represents a virtual grid of range partition. 
 * @author santkumk
 *
 */
public class RangePartitionGrid implements Partitions, Serializable {
	private static final long serialVersionUID = -4123184342699552287L;
	double m_minLat;
	double m_minLng;
	double m_maxLat;
	double m_maxLng;
	int m_nLatGrid;
	int m_nLngGrid;
	double m_overlapLat = 0.0d;
	double m_overlapLng = 0.0d;
	private double lngGridSize;
	private double latGridSize;
	protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

	/**
	 * Creates a virtual grid using the MBR of a circle for a given point and radius
	 * @param lng - longitude of a point(location)
	 * @param lat - latitude of a point
	 * @param radius - radius/distance we want to define the virtual grid of size.
	 * @param nLngGrid - total no. of longitude grids
	 * @param nLatGrid - total no. of latitude grids
	 */
	public RangePartitionGrid(double lng, double lat, int radius, int nLngGrid,
			int nLatGrid) {
		Geometry circle = Geometry.createCircle(lng, lat, radius);
		double[][] mbr = Geometry.get2dMbr(circle);
		log.debug("minLng:"+ mbr[0][0] + " minLat:" + mbr[1][0] + " maxLng:" + mbr[0][1] + " maxLat:"
				+ mbr[1][1]);
		m_minLng = mbr[0][0];
		m_maxLng = mbr[0][1];
		m_minLat = mbr[1][0];
		m_maxLat = mbr[1][1];
		m_nLatGrid = nLatGrid;
		m_nLngGrid = nLngGrid;
		lngGridSize = (m_maxLng - m_minLng) / m_nLngGrid;
		latGridSize = (m_maxLat - m_minLat) / m_nLatGrid;
		log.debug("Created RangePartitionGrid boundary[" + m_minLng + "," + m_minLat + "," + m_maxLng + "," + m_maxLat
				+ "]" + "LngGridSize=" + lngGridSize + " LatGridSize=" + latGridSize);
	}
	
	/**
	 * creates a virtual grid on given MBR
	 * @param mbr - grid boundary
	 * @param nLngGrid - total no. of longitude grids
	 * @param nLatGrid - total no.of latitude grids
	 *
	 */
	public RangePartitionGrid(double[][] mbr, int nLngGrid, int nLatGrid) {
		log.debug("MBR" + mbr[0][0] + ":" + mbr[0][1] + ":" + mbr[1][0] + ":"
				+ mbr[1][1]);
		m_minLng = mbr[0][0];
		m_minLat = mbr[0][1];
		m_maxLng = mbr[1][0];
		m_maxLat = mbr[1][1];
		m_nLatGrid = nLatGrid;
		m_nLngGrid = nLngGrid;
		lngGridSize = (m_maxLng - m_minLng) / m_nLngGrid;
		latGridSize = (m_maxLat - m_minLat) / m_nLatGrid;
		log.debug("Created RangePartitionGrid boundary[" + m_minLng + "," + m_minLat + "," + m_maxLng + "," + m_maxLat
				+ "]" + "LngGridSize=" + lngGridSize + " LatGridSize=" + latGridSize);
	}

	public double getMinLat() {
		return m_minLat;
	}

	public double getMinLng() {
		return m_minLng;
	}

	public double getMaxLat() {
		return m_maxLat;
	}

	public double getMaxLng() {
		return m_maxLng;
	}

	public int getLatGrid() {
		return m_nLatGrid;
	}

	public int getLngGrid() {
		return m_nLngGrid;
	}

	public double getOverlapLat() {
		return m_overlapLat;
	}

	public void setOverlapLat(double v) {
		m_overlapLat = v;
	}

	public double getOverlapLng() {
		return m_overlapLng;
	}

	public void setOverlapLng(double v) {
		m_overlapLng = v;
	}

	@Override
	public Iterator<Partition> iterator() {
		return new RangeParitionIterator();
	}

	private class RangeParitionIterator implements Iterator<Partition> {
		double m_xGrid;
		double m_yGrid;
		int m_curXGrid;
		int m_curYGrid;
		int m_nextId;
		boolean m_done;

		public RangeParitionIterator() {
			m_xGrid = (m_maxLng - m_minLng) / m_nLngGrid;
			m_yGrid = (m_maxLat - m_minLat) / m_nLatGrid;
			m_curXGrid = 0;
			m_curYGrid = 0;
			m_nextId = 0;
			m_done = false;
		}

		@Override
		public boolean hasNext() {
			return !m_done;
		}

		@Override
		public Partition next() {
			JGeometry geom = null;
			double x1 = getMinXGrid();
			double y1 = getMinYGrid();
			double x2 = getMaxXGrid();
			double y2 = getMaxYGrid();

			if (m_overlapLat != 0.0d && m_overlapLng != 0.0d) {
				x1 -= m_overlapLng;
				x2 += m_overlapLng;
				y1 -= m_overlapLat;
				y2 += m_overlapLat;
			}
			geom = Geometry.createRectangle(8307, x1, y1, x2, y2);
			if (!nextGrid()) {
				m_done = true;
			}

			return new RangePartition(m_nextId++, geom);
		}

		@Override
		public void remove() {
			throw new RuntimeException("Not supported");
		}

		protected boolean nextGrid() {
			m_curXGrid++;
			if (m_curXGrid >= m_nLngGrid) {
				m_curXGrid = 0;
				m_curYGrid++;
				if (m_curYGrid >= m_nLatGrid) {
					return false;
				}
			}
			return true;
		}

		protected double getMinXGrid() {
			return m_minLng + m_curXGrid * m_xGrid;
		}

		protected double getMaxXGrid() {
			return getMinXGrid() + m_xGrid;
		}

		protected double getMinYGrid() {
			return m_minLat + m_curYGrid * m_yGrid;
		}

		protected double getMaxYGrid() {
			return getMinYGrid() + m_yGrid;
		}
	}

	/**
	 * find the partition number (or grid) in which the given location/point will fall in. 
	 * @param lng - point's longitude 
	 * @param lat - point's latitude
	 * @return - partition number
	 */
	public int getPartitionNo(double lng, double lat) {
		int xGrid = (int) ((lng - m_minLng) / lngGridSize);
		int yGrid = (int) ((lat - m_minLat) / latGridSize);
		return (xGrid + yGrid*m_nLngGrid) % (m_nLngGrid * m_nLatGrid);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RangePartitionGrid{");
		sb.append("m_minLat=").append(m_minLat);
		sb.append(", m_minLng=").append(m_minLng);
		sb.append(", m_maxLat=").append(m_maxLat);
		sb.append(", m_maxLng=").append(m_maxLng);
		sb.append(", m_nLatGrid=").append(m_nLatGrid);
		sb.append(", m_nLngGrid=").append(m_nLngGrid);
		sb.append(", m_overlapLat=").append(m_overlapLat);
		sb.append(", m_overlapLng=").append(m_overlapLng);
		sb.append(", lngGridSize=").append(lngGridSize);
		sb.append(", latGridSize=").append(latGridSize);
		sb.append('}');
		return sb.toString();
	}
}
