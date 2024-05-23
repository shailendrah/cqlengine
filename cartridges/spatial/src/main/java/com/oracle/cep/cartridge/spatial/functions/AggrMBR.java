package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.AggrObj;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.IAggrFunctionMetadata;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.RTree;

public class AggrMBR implements IAggrFunctionMetadata {
  public static final String NAME = "MBR";

  static Datatype[] S_PARAMTYPES = new Datatype[] { SpatialCartridge
      .getCQLType(Geometry.class) };

  static Datatype[] s_paramTypes;

  int m_srid = SpatialCartridge.DEFAULT_SRID;


  public static AggrMBR getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
      throws MetadataNotFoundException {
    if (paramTypes == null) {
      Datatype cqlType = Geometry.getGeometryType();
      paramTypes = new Datatype[] { cqlType };
    } else {
      if (paramTypes.length != 1)
        return null;

      if (!Geometry.isAllGeometryType(paramTypes[0])) {
        return null;
      }
    }
    s_paramTypes = paramTypes;
    int srid = SpatialCartridge.getContextSRID(ctx);
    return new AggrMBR(srid);
  }

  public static AggrMBR getMetadata(ICartridgeContext ctx)
      throws MetadataNotFoundException {
    int srid = SpatialCartridge.getContextSRID(ctx);
    return new AggrMBR(srid);
  }

  AggrMBR(int srid) {
    this.m_srid = srid;
  }

  @Override
  public Datatype getReturnType() {
    return SpatialCartridge.getCQLType(double[].class);
  }

  public Datatype[] getParameterTypes() {
    return s_paramTypes; // S_PARAMTYPES;
  }

  @Override
  public int getNumParams() {
    return s_paramTypes.length; // S_PARAMTYPES.length;
  }

  @Override
  public IAttribute getParam(int pos) throws MetadataException {
    return new Attribute("attr" + pos, s_paramTypes[pos], 0);
  }

  @Override
  public String getSchema() {
    return SpatialCartridge.CARTRIDGE_NAME;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public IAggrFnFactory getAggrFactory() {
    return new IAggrFnFactory() {
      @Override
      public IAggrFunction newAggrFunctionHandler() throws UDAException {
        return new AggrMBRFunction();
      }

      @Override
      public void freeAggrFunctionHandler(IAggrFunction handler)
          throws UDAException {
      }
    };
  }

  private static class AggrMBRFunction extends AggrFunctionImpl {
    /**
     * 
     */
    private static final long serialVersionUID = -3000759982278698387L;
    RTree m_rtree;

    @Override
    public void initialize() throws UDAException {
      m_rtree = new RTree(2, 6, 1);
    }

    double[] getMbr(int dim, double[][] mbh) {
      double[] mbr = new double[6];
      if (dim == 2) {
        mbr[0] = mbh[0][0];
        mbr[2] = mbh[0][1];
        mbr[1] = mbh[1][0];
        mbr[3] = mbh[1][1];
      } else {
        mbr[0] = mbh[0][0];
        mbr[3] = mbh[0][1];
        mbr[1] = mbh[1][0];
        mbr[4] = mbh[1][1];
        mbr[2] = mbh[2][0];
        mbr[5] = mbh[2][1];
      }
      return mbr;
    }

    public void handlePlus(AggrValue[] value, AggrValue result)
        throws UDAException {
      JGeometry v = null;
      double[] mbr = null;
      if (!value[0].isNull()) {
        v = (JGeometry) ((AggrObj) (value[0])).getValue();
        m_rtree.addEntry(Geometry.get2dMbr(v), v);
        double[][] mbh = m_rtree.getMBH();
        mbr = getMbr(v.getDimensions(), mbh);
      }
      ((AggrObj) result).setValue(mbr);
    }

    public void handleMinus(AggrValue[] value, AggrValue result)
        throws UDAException {
      JGeometry v = null;
      double[] mbr = null;
      if (!value[0].isNull()) {
        v = (JGeometry) ((AggrObj) (value[0])).getValue();
        m_rtree.removeEntry(Geometry.get2dMbr(v), v);
        double[][] mbh = m_rtree.getMBH();
        mbr = getMbr(v.getDimensions(), mbh);
      }
      ((AggrObj) result).setValue(mbr);
    }
  }

  @Override
  public boolean supportsIncremental() {
    return true;
  }
}
