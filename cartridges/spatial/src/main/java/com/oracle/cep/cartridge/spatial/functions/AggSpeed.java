package com.oracle.cep.cartridge.spatial.functions;

import java.util.LinkedList;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import org.apache.commons.logging.Log;

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
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * Aggreagated speed function
 * 
 * @author kmulay
 * 
 */
public class AggSpeed implements IAggrFunctionMetadata {

  public static final String NAME = "aggspeed";
  private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

  static Datatype[] S_PARAMTYPES = { Datatype.CHAR, Datatype.BIGINT,
      Datatype.OBJECT, Datatype.CHAR };

  Datatype[] s_paramTypes;

  int m_srid = SpatialCartridge.DEFAULT_SRID;

  boolean supportsIncremental = true;

  public static AggSpeed getMetadata(Datatype[] paramTypes,
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

    if (paramTypes.length == 4) {
      if (paramTypes[3].kind != IType.Kind.CHAR)
        return null;
    }
    return new AggSpeed(paramTypes);
  }

  public static AggSpeed getMetadata(ICartridgeContext ctx)
      throws MetadataNotFoundException {
    int srid = SpatialCartridge.getContextSRID(ctx);
    return new AggSpeed(srid);
  }

  AggSpeed(Datatype[] paramTypes) {
    this.s_paramTypes = paramTypes;
  }

  AggSpeed(int srid) {
    this.m_srid = srid;
  }

  @Override
  public Datatype getReturnType() {
    return SpatialCartridge.getCQLType(double.class);
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
        return new AggSpeedFunction();
      }

      @Override
      public void freeAggrFunctionHandler(IAggrFunction handler)
          throws UDAException {
      }
    };
  }

  public static class AggSpeedFunction extends AggrFunctionImpl {
    private static final long serialVersionUID = -3000759982278638389L;
    LinkedList<Double> speeds;
    Double totalSpeed;
    Geometry previousLocation;
    long previousTime;

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
      
      //if there is no previous location speed will be zero.
      if (speeds.size() == 0) {
        result.setValue(speed);
        previousLocation = location;
        speeds.add(0.0);
        previousTime = time;
      } else {
        // else we calculate aggregated speed based on simple moving average.
        try {
          double dist = Geometry.distance(location, previousLocation);
          if (dist == 0.0)
            speed = 0.0;
          else if (unit == null || unit.equalsIgnoreCase("mph")) {
            speed = (dist / 1609.344)
                / ((time - previousTime) / 3600000000000.0);
          } else if (unit.equalsIgnoreCase("kph")) {
            speed = (dist / 1000) / ((time - previousTime) / 3600000000000.0);
          } else
            throw new UDAException(UDAError.INVALID_UNIT);
          } catch (Exception e) {

        }
        // add current speed to list and update total speed
        speeds.add(speed);
        totalSpeed = totalSpeed + speed;
        previousLocation = location;
        previousTime = time;
        // sets result as totalseed/ number of tuples in window
        result.setValue(totalSpeed / speeds.size());
      }
     
    }

    public void handleMinus(AggrValue[] value, AggrValue result)
        throws UDAException {
      // removes expiring speed from list of speeds
      double removedSpeed = speeds.remove();
      totalSpeed = totalSpeed - removedSpeed;
      result.setValue(totalSpeed / speeds.size());
     
    }
  }

  @Override
  public boolean supportsIncremental() {
    return supportsIncremental;
  }

}
