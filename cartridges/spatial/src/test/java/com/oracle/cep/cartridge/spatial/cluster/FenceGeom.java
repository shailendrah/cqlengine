package com.oracle.cep.cartridge.spatial.cluster;

import com.oracle.cep.cartridge.spatial.Geometry;
import oracle.spatial.geometry.JGeometry;

/**
 * @author santkum
 */
public class FenceGeom {
    String id;
    JGeometry geom;

    public FenceGeom(String id, JGeometry geom) {
        this.id = id;
        this.geom = geom;
    }

    public String getId() {
        return id;
    }

    public JGeometry getGeom() {
        return geom;
    }

    public static  FenceGeom getFenceGeom(String fence){
        String[] scoord = fence.split(",");
        double[] dcoord = new double[scoord.length-2];
        for (int i = 2; i < scoord.length; i++) {
            dcoord[i-2] = Double.parseDouble(scoord[i]);
        }
        Geometry polygon = Geometry.createLinearPolygon(dcoord);
        return  new FenceGeom(scoord[0],polygon);
    }
    public static  FenceGeom getFenceBufferedGeom(String fence,double buffer) throws Exception {
        String[] scoord = fence.split(",");
        double[] dcoord = new double[scoord.length-2];
        for (int i = 2; i < scoord.length; i++) {
            dcoord[i-2] = Double.parseDouble(scoord[i]);
        }
        Geometry polygon = Geometry.createLinearPolygon(dcoord);
        JGeometry geom =Geometry.bufferedMBR(polygon,buffer);
        return  new FenceGeom(scoord[0],geom);
    }
}
