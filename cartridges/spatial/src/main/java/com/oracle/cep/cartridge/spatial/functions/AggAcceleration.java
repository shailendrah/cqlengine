package com.oracle.cep.cartridge.spatial.functions;

import java.util.LinkedList;

import org.apache.commons.logging.Log;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDAError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.IAggrFunctionMetadata;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.type.IType;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;

/**
 * Aggreagated acceleration function
 * 
 * @author kmulay
 * 
 */
public class AggAcceleration implements IAggrFunctionMetadata {

  public static final String NAME = "aggacceleration";
  private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

  static Datatype[] S_PARAMTYPES = { Datatype.CHAR, Datatype.BIGINT,
      Datatype.OBJECT, Datatype.CHAR };

  Datatype[] s_paramTypes;

  int m_srid = SpatialCartridge.DEFAULT_SRID;

  boolean supportsIncremental = true;

  public static AggAcceleration getMetadata(Datatype[] paramTypes,
      ICartridgeContext ctx) throws MetadataNotFoundException {

    if (paramTypes == null) {
      paramTypes = S_PARAMTYPES;
    }
    if (paramTypes.length < 3) {
      return null;
    }
    if (paramTypes[0].kind == IType.Kind.CHAR &&
        paramTypes[1].kind != IType.Kind.BIGINT &&
        paramTypes[2].kind != IType.Kind.OBJECT)
      return null;

    if (paramTypes.length == 4 &&
        paramTypes[3].kind != IType.Kind.CHAR)
      return null;

    return new AggAcceleration(paramTypes);
  }

  public static AggAcceleration getMetadata(ICartridgeContext ctx)
      throws MetadataNotFoundException {
    int srid = SpatialCartridge.getContextSRID(ctx);
    return new AggAcceleration(srid);
  }

  AggAcceleration(Datatype[] paramTypes) {
    this.s_paramTypes = paramTypes;
  }

  AggAcceleration(int srid) {
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
        return new AggAccelerationFunction();
      }

      @Override
      public void freeAggrFunctionHandler(IAggrFunction handler)
          throws UDAException {
      }
    };
  }

  public static class AggAccelerationFunction extends AggrFunctionImpl {
    private static final long serialVersionUID = -3000759982278638389L;
    LinkedList<Double> speeds;
    Double totalSpeed;
    Geometry previousLocation;
    long previousTime;
    Double previousAggrSpeed;

    @Override
    public void initialize() throws UDAException {
      speeds = new LinkedList<Double>();
      totalSpeed = 0.0;
      previousLocation = null;
      previousTime = 0L;
    }

    public void handlePlus(AggrValue[] value, AggrValue result)
        throws UDAException {
      String id = new String((char[]) (value[0]).getValue());
      Long time = (Long) (value[1]).getValue();
      Geometry location = (Geometry) (value[2]).getValue();
      String unit = null;
      if (value.length > 3)
        unit = new String((char[]) (value[3]).getValue());
      double speed = 0.0;
      if (speeds.size() == 0) {
        result.setValue(speed);
        previousLocation = location;
        speeds.add(0.0);
        previousTime = time;
        previousAggrSpeed = 0.0;
      } else {
        try {
          double dist = Geometry.distance(location, previousLocation);
          if (unit == null || unit.equalsIgnoreCase("mph")) {
            speed = (dist / 1609.344)
                / ((time - previousTime) / 3600000000000.0);
          } else if (unit.equalsIgnoreCase("kph")) {
            speed = (dist / 1000) / ((time - previousTime) / 3600000000000.0);
          } else
            throw new UDAException(UDAError.INVALID_UNIT);
        } catch (Exception e) {

        }
        // calculate aggregated speed with simple moving average
        speeds.add(speed);
        totalSpeed = totalSpeed + speed;
        double aggrspeed = totalSpeed / speeds.size();
        result.setValue((aggrspeed - previousAggrSpeed)
            / ((time - previousTime) / 3600000000000.0));
        previousAggrSpeed = aggrspeed;
        previousLocation = location;
        previousTime = time;
      }
    }

    public void handleMinus(AggrValue[] value, AggrValue result)
        throws UDAException {
      Long time = (Long) (value[1]).getValue();
      double removedSpeed = speeds.remove();
      totalSpeed = totalSpeed - removedSpeed;
      double aggrspeed = totalSpeed / speeds.size();
      result.setValue((aggrspeed - previousAggrSpeed)
          / ((time - previousTime) / 3600000000000.0));
    }
  }

  @Override
  public boolean supportsIncremental() {
    return supportsIncremental;
  }

}
