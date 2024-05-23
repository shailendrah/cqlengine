package com.oracle.cep.cartridge.spatial.cluster;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.spatial.geometry.JGeometry;
import org.apache.commons.logging.Log;

import java.util.*;

/**
 * @author santkumk
 */
public class RangePartitioner implements GeomPartitioner{
    private RangePartitionGrid grid;
    private double buffer;
    protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

    public RangePartitioner(RangePartitionGrid grid){
        log.debug("RangePartitioner of grid="+grid);
        this.grid = grid;
    }

    public RangePartitioner(RangePartitionGrid grid, double buffer){
        this(grid);
        this.buffer = buffer;
    }

    @Override
    public int[] partition(JGeometry geom) {
        double[] coords = geom.getOrdinatesArray();
        Set<Integer> pSet = new HashSet<>();
        log.debug("total coords=" + coords.length + " geom= " + geom);
        assert coords.length % 2 == 0;
        int idx = 0;
        while(idx < coords.length){
            pSet.add(grid.getPartitionNo(coords[idx], coords[idx+1]));
            idx = idx +2;
        }
        log.debug("partition list=" + pSet + " for geom " + geom);
        return pSet.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public int[] partition(double lng, double lat) {
        return new int[]{grid.getPartitionNo(lng, lat)};
    }
}
