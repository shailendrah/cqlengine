package com.oracle.cep.cartridge.spatial;

import java.util.Locale;

import weblogic.i18n.Localizer;
import weblogic.i18n.logging.CatalogMessage;
import weblogic.i18n.logging.MessageLogger;
import weblogic.i18n.logging.MessageLoggerRegistry;
import weblogic.i18n.logging.MessageLoggerRegistryListener;
import weblogic.i18n.logging.MessageResetScheduler;
import weblogic.i18ntools.L10nLookup;
import weblogic.logging.Loggable;

/** 
 * Copyright (c) 2003,2014, Oracle and/or its affiliates. All rights reserved.
 * @exclude
 */
public class SpatialCartridgeLogger
{
  private static final String LOCALIZER_CLASS = "com.oracle.cep.cartridge.spatial.SpatialCartridgeLogLocalizer";
  
  private static MessageLogger findMessageLogger() {
    return MessageLoggerRegistry.findMessageLogger(SpatialCartridgeLogger.class.getName());
  }

  private static final class MessageLoggerInitializer implements MessageLoggerRegistryListener {

    private static final MessageLoggerInitializer INSTANCE = new MessageLoggerInitializer();
    
    private static final Localizer LOCALIZER = L10nLookup.getLocalizer(
      Locale.getDefault(), LOCALIZER_CLASS, SpatialCartridgeLogger.class.getClassLoader());
    
    private MessageLogger messageLogger = findMessageLogger();
    
    private MessageLoggerInitializer() {
      MessageLoggerRegistry.addMessageLoggerRegistryListener(this);      
    }
            
    public void messageLoggerRegistryUpdated() {
      messageLogger = findMessageLogger();
    }
  }

  /**
   * "{0}" is reserved for the server cartridge context.
   *
   * messageid:  2069000
   * severity:   error
   */
  public static String reservedServerContext(String arg0)  {
    Object [] args = { arg0 };
    Loggable l = (new Loggable("2069000", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable reservedServerContextLoggable(String arg0)  {
    Object[] args = { arg0 };
    Loggable l = new Loggable("2069000", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * {0}({1}) cannot be registered. "{3}" is already using the same srid, please use other srid.
   *
   * messageid:  2069001
   * severity:   error
   */
  public static String invalidSpatialContext(String arg0, String arg1, int arg2, String arg3)  {
    Object [] args = { arg0, arg1, Integer.valueOf(arg2), arg3 };
    Loggable l = (new Loggable("2069001", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable invalidSpatialContextLoggable(String arg0, String arg1, int arg2, String arg3)  {
    Object[] args = { arg0, arg1, Integer.valueOf(arg2), arg3 };
    Loggable l = new Loggable("2069001", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The 'Contain' operator can only be used with GTYPE_POINT.
   *
   * messageid:  2069002
   * severity:   error
   */
  public static String InvalidGeomTypeForContain()  {
    Object [] args = {  };
    Loggable l = (new Loggable("2069002", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable InvalidGeomTypeForContainLoggable()  {
    Object[] args = {  };
    Loggable l = new Loggable("2069002", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The buffering operation has failed with the exception below.
   *
   * messageid:  2069003
   * severity:   error
   */
  public static String BufferingErrorUnknown()  {
    Object [] args = {  };
    Loggable l = (new Loggable("2069003", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable BufferingErrorUnknownLoggable()  {
    Object[] args = {  };
    Loggable l = new Loggable("2069003", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The buffering operation has failed due to SRID mismatch.
   *
   * messageid:  2069004
   * severity:   error
   */
  public static String BufferingErrorSRID()  {
    Object [] args = {  };
    Loggable l = (new Loggable("2069004", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable BufferingErrorSRIDLoggable()  {
    Object[] args = {  };
    Loggable l = new Loggable("2069004", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Operations on geometries containing arcs must use non-zero tolerance.
   *
   * messageid:  2069005
   * severity:   error
   */
  public static String ZeroToleranceForArcs()  {
    Object [] args = {  };
    Loggable l = (new Loggable("2069005", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable ZeroToleranceForArcsLoggable()  {
    Object[] args = {  };
    Loggable l = new Loggable("2069005", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Operation "{0}" on geodetic 3D geometries not supported.
   *
   * messageid:  2069006
   * severity:   error
   */
  public static String Geodetic3DGeometryNotSupported(String arg0)  {
    Object [] args = { arg0 };
    Loggable l = (new Loggable("2069006", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable Geodetic3DGeometryNotSupportedLoggable(String arg0)  {
    Object[] args = { arg0 };
    Loggable l = new Loggable("2069006", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Containing geometry must either be a solid 3D geometry, or contained geometry must be a point inside a 3D geometry.
   *
   * messageid:  2069007
   * severity:   error
   */
  public static String EitherSolidOrPointInPolygon()  {
    Object [] args = {  };
    Loggable l = (new Loggable("2069007", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable EitherSolidOrPointInPolygonLoggable()  {
    Object[] args = {  };
    Loggable l = new Loggable("2069007", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Operation "{0}" on compound 3D geometries is not supported.
   *
   * messageid:  2069008
   * severity:   error
   */
  public static String Compound3DGeometriesNotSupported(String arg0)  {
    Object [] args = { arg0 };
    Loggable l = (new Loggable("2069008", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable Compound3DGeometriesNotSupportedLoggable(String arg0)  {
    Object[] args = { arg0 };
    Loggable l = new Loggable("2069008", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Unknown geometry type : {0}.
   *
   * messageid:  2069009
   * severity:   error
   */
  public static String UnknownGeometryError(String arg0)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069009")) return "2069009"; 
    }

    Object [] args = { arg0 };
    CatalogMessage catalogMessage = new CatalogMessage("2069009", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069009", 0);
    }

    return "2069009";
  }

  public static Loggable UnknownGeometryErrorLoggable(String arg0)  {
    Object[] args = { arg0 };
    Loggable l = new Loggable("2069009", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Not enough geometry arguments.
   *
   * messageid:  2069010
   * severity:   error
   */
  public static String NotEnoughGeometryArguments(int arg0, int arg1)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069010")) return "2069010"; 
    }

    Object [] args = { Integer.valueOf(arg0), Integer.valueOf(arg1) };
    CatalogMessage catalogMessage = new CatalogMessage("2069010", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069010", 0);
    }

    return "2069010";
  }

  public static Loggable NotEnoughGeometryArgumentsLoggable(int arg0, int arg1)  {
    Object[] args = { Integer.valueOf(arg0), Integer.valueOf(arg1) };
    Loggable l = new Loggable("2069010", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The number of coordinate does not match with the given dimension( {0} ).
   *
   * messageid:  2069011
   * severity:   error
   */
  public static String MismatchedCoordinatesPairError(int arg0)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069011")) return "2069011"; 
    }

    Object [] args = { Integer.valueOf(arg0) };
    CatalogMessage catalogMessage = new CatalogMessage("2069011", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069011", 0);
    }

    return "2069011";
  }

  public static Loggable MismatchedCoordinatesPairErrorLoggable(int arg0)  {
    Object[] args = { Integer.valueOf(arg0) };
    Loggable l = new Loggable("2069011", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The geometry type is not {1} : {0} ).
   *
   * messageid:  2069012
   * severity:   error
   */
  public static String InvalidGeometryType(String arg0, String arg1)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069012")) return "2069012"; 
    }

    Object [] args = { arg0, arg1 };
    CatalogMessage catalogMessage = new CatalogMessage("2069012", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069012", 0);
    }

    return "2069012";
  }

  public static Loggable InvalidGeometryTypeLoggable(String arg0, String arg1)  {
    Object[] args = { arg0, arg1 };
    Loggable l = new Loggable("2069012", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The srid of geometry is not {1} : {0} ).
   *
   * messageid:  2069013
   * severity:   error
   */
  public static String InvalidSRID(String arg0, int arg1)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069013")) return "2069013"; 
    }

    Object [] args = { arg0, Integer.valueOf(arg1) };
    CatalogMessage catalogMessage = new CatalogMessage("2069013", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069013", 0);
    }

    return "2069013";
  }

  public static Loggable InvalidSRIDLoggable(String arg0, int arg1)  {
    Object[] args = { arg0, Integer.valueOf(arg1) };
    Loggable l = new Loggable("2069013", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The dimension of geometry is not {1} : {0}.
   *
   * messageid:  2069014
   * severity:   error
   */
  public static String InvalidDimension(String arg0, int arg1)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069014")) return "2069014"; 
    }

    Object [] args = { arg0, Integer.valueOf(arg1) };
    CatalogMessage catalogMessage = new CatalogMessage("2069014", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069014", 0);
    }

    return "2069014";
  }

  public static Loggable InvalidDimensionLoggable(String arg0, int arg1)  {
    Object[] args = { arg0, Integer.valueOf(arg1) };
    Loggable l = new Loggable("2069014", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Invalid arrayKey({0}) : either it does not exist or the type is not array
   *
   * messageid:  2069015
   * severity:   error
   */
  public static String invalidArrayKey(String arg0)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069015")) return "2069015"; 
    }

    Object [] args = { arg0 };
    CatalogMessage catalogMessage = new CatalogMessage("2069015", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069015", 0);
    }

    return "2069015";
  }

  public static Loggable invalidArrayKeyLoggable(String arg0)  {
    Object[] args = { arg0 };
    Loggable l = new Loggable("2069015", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The srid {0} is not supported for a circle.
   *
   * messageid:  2069016
   * severity:   error
   */
  public static String InvalidSRIDForCircle(int arg0)  {

    if (0 > 0) {
      if (MessageResetScheduler.getInstance().isMessageLoggingDisabled("2069016")) return "2069016"; 
    }

    Object [] args = { Integer.valueOf(arg0) };
    CatalogMessage catalogMessage = new CatalogMessage("2069016", 8, args, MessageLoggerInitializer.LOCALIZER);
    catalogMessage.setStackTraceEnabled(true);
    catalogMessage.setDiagnosticVolume("Off");
    catalogMessage.setExcludePartition(false);     
    MessageLoggerInitializer.INSTANCE.messageLogger.log(catalogMessage);

    if (0 > 0) {
      MessageResetScheduler.getInstance().scheduleMessageReset("2069016", 0);
    }

    return "2069016";
  }

  public static Loggable InvalidSRIDForCircleLoggable(int arg0)  {
    Object[] args = { Integer.valueOf(arg0) };
    Loggable l = new Loggable("2069016", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * The tolerance value must be a positive number greater than zero.
   *
   * messageid:  2069017
   * severity:   error
   */
  public static String NonPositiveTolerance()  {
    Object [] args = {  };
    Loggable l = (new Loggable("2069017", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable NonPositiveToleranceLoggable()  {
    Object[] args = {  };
    Loggable l = new Loggable("2069017", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, SpatialCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }



}
