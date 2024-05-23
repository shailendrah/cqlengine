package com.oracle.cep.cartridge.spatial.cluster;

import oracle.spatial.geometry.JGeometry;

/**
 * @author santkumk
 */
public interface GeomPartitioner{
    public int[] partition(JGeometry geom);
    public int[] partition(double lng, double lat);

    public static GeomPartitioner createSparsePartitioner(RangePartitionGrid grid, boolean overlapping){
        SparsePartitioner partitioner = new SparsePartitioner();
        partitioner.setOverlapping(overlapping);
        partitioner.setPartitions(grid);
        return partitioner;
    }

    public static GeomPartitioner createSparsePartitioner(RangePartitionGrid grid){
        SparsePartitioner partitioner = new SparsePartitioner();
        partitioner.setOverlapping(true);
        partitioner.setPartitions(grid);
        return partitioner;
    }
}
