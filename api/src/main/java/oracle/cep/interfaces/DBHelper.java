/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/DBHelper.java /main/11 2013/09/25 20:22:45 sbishnoi Exp $ */

/* Copyright (c) 2008, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/25/13 - bug 17232810
    sbishnoi    09/22/13 - bug 17232563
    sbishnoi    08/29/11 - adding support for interval year to month
    udeshmuk    06/14/11 - XbranchMerge udeshmuk_bug-11728864_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    05/25/11 - close connection/stmt if exception occurs
    sborah      10/12/09 - support for bigdecimal
    hopark      03/06/09 - add opaque type
    hopark      02/17/09 - support boolean as external datatype
    parujain    02/06/09 - bug fix
    hopark      06/27/08 - log validate exception
    sbishnoi    03/21/08 - Creation
 */
package oracle.cep.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.extensibility.type.IType.Kind;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * @version $Header:
 *          cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/DBHelper.java
 *          /main/11 2013/09/25 20:22:45 sbishnoi Exp $
 * @author sbishnoi
 * @since release specific (what release of product did this appear in)
 */

public class DBHelper
{

  private static final String s_geometryTypeName = "Geometry";
  @SuppressWarnings("serial")
  static final HashMap<String, Datatype> s_jdbcObjTypeToKind = new HashMap<String, Datatype>()
  {
    {
      put("java.sql.Timestamp", Datatype.TIMESTAMP);
      put("java.sql.Time", Datatype.TIMESTAMP);
      put("java.sql.Date", Datatype.TIMESTAMP);
      put("java.util.Date", Datatype.TIMESTAMP);
      put("java.math.BigDecimal", Datatype.BIGDECIMAL);
      put("java.lang.Integer", Datatype.INT);
      put("java.lang.Float", Datatype.FLOAT);
      put("java.lang.Long", Datatype.BIGINT);
      put("java.lang.Double", Datatype.DOUBLE);
      put("java.lang.Boolean", Datatype.BOOLEAN);
      put("java.lang.Byte", Datatype.BYTE);
      put("java.lang.Short",
          Datatype.getDecimalType(Datatype.SHORT_PRECISION, 0));
      put(char[].class.getName(), Datatype.CHAR);
      put(char[].class.getCanonicalName(), Datatype.CHAR);
      try
      {
        Class<?> cls = Class
            .forName("com.oracle.cep.cartridge.spatial.Geometry");
        Datatype geomType = new Datatype(s_geometryTypeName, cls);
        put("com.oracle.cep.cartridge.spatial.Geometry", geomType);
        put("oracle.spatial.geometry.JGeometry", geomType);
      } catch (Exception e)
      {
      }
    }
  };

  private static void setStmtValue(Datatype type, int index,
      PreparedStatement stmt) throws SQLException, CEPException
  {
    switch (type.getKind())
    {
    case INT:
      stmt.setInt(index + 1, Constants.INTEGER_CONST);
      break;
    case BOOLEAN:
      stmt.setBoolean(index + 1, Constants.BOOLEAN_CONST);
      break;
    case FLOAT:
      stmt.setFloat(index + 1, Constants.FLOAT_CONST);
      break;
    case DOUBLE:
      stmt.setDouble(index + 1, Constants.DOUBLE_CONST);
      break;
    case BIGDECIMAL:
      if (type.getPrecision() == Datatype.SHORT_PRECISION
          && type.getLength() == 0)
        stmt.setShort(index + 1,
            new Integer(Constants.INTEGER_CONST).shortValue());
      else
        stmt.setBigDecimal(index + 1,
            new BigDecimal(Constants.BIGDECIMAL_CONST));
      break;
    case BIGINT:
      stmt.setLong(index + 1, Constants.BIGINT_CONST);
      break;
    case CHAR:
      stmt.setString(index + 1, Constants.CHAR_CONST);
      break;
    case BYTE:
      stmt.setBytes(index + 1, Datatype.hexToByte(Constants.BYTE_CONST));
      break;
    case TIMESTAMP:
      stmt.setTimestamp(index + 1, new Timestamp(Constants.TIMESTAMP_CONST));
      break;
    case INTERVAL:
      stmt.setString(index + 1, Constants.INTERVAL_CONST);
      break;
    case INTERVALYM:
      stmt.setString(index + 1, Constants.INTERVALYM_CONST);
      break;
    case OBJECT:
    {
      // FIXME: ideally, we would invoke the Java/JDBC cartridge, however we
      // can't really
      // do this from this static method. In the future, let's move this code to
      // the cartridge.
      String typeName = type.name();
      Datatype dt = s_jdbcObjTypeToKind.get(typeName);
      if (dt == null)
      {
        throw new RuntimeException("Cannot handle type:" + typeName);
      }
      if (dt.getKind() == Kind.OBJECT)
      {
        if (s_geometryTypeName.equals(dt.typeName))
        {
          try
          {
            Class<?> cls = Class.forName("oracle.spatial.geometry.JGeometry");
            Constructor<?> ct = cls.getConstructor(double.class, double.class,
                int.class);
            Object geom = ct.newInstance(0.0, 0.0, 8307);
            Method storemethod = cls.getMethod("store", cls, Connection.class);
            Object st = storemethod.invoke(null, geom, stmt.getConnection());
            stmt.setObject(index + 1, st);
          } catch (Exception e)
          {
            LogUtil.logStackTrace(e);
            throw new RuntimeException("Failed to create geometry ", e);
          }
        } else
        {
          throw new RuntimeException("Cannot handle type:" + typeName);
        }
      } else
      {
        setStmtValue(dt, index, stmt);
      }
      break;
    }
    default:
      assert false;
    }
  }

  /*
   * Object type cannot be used to query. Otherwise, we are getting:
   * java.sql.SQLException: ORA-22901: cannot compare VARRAY or LOB attributes
   * of an object type
   */
  private static boolean canAddToStmt(Datatype dt)
  {
    if (dt.kind != Kind.OBJECT)
      return true;
    String typeName = dt.name();
    Datatype dt2 = s_jdbcObjTypeToKind.get(typeName);
    return (dt2.kind != Kind.OBJECT);
  }

  /**
   * Validate DB Schema by executing a query on schema with some const values
   * 
   * @param numAttrs
   * @param attrNames
   * @param attrTypes
   * @param tableName
   * @param connection
   * @throws CEPException
   */
  public static void validateSchema(int numAttrs, String[] attrNames,
      AttributeMetadata[] attrMetadata, String tableName, Connection connection)
      throws CEPException, SQLException
  {
    if (tableName == null || tableName.isEmpty())
    {
      LogUtil.info(LoggerType.TRACE,
          "Skipping the schema validation as tableName is either null or empty");
      return;
    }
    PreparedStatement stmt = null;
    String sql = new String("select ");
    for (int j = 0; j < attrNames.length; j++)
    {
      if (j > 0)
        sql = sql.concat(", ");
      sql = sql.concat(attrNames[j]);
    }
    sql = sql.concat(" from " + tableName + " where ");
    try
    {
      assert attrNames.length > 0 : attrNames.length;
      int npredicates = 0;
      for (int i = 0; i < numAttrs; i++)
      {
        if (canAddToStmt(attrMetadata[i].getDatatype()))
        {
          if (npredicates > 0)
            sql = sql.concat(" and ");
          sql = sql.concat(attrNames[i] + " = ?");
          npredicates++;
        }
      }
      LogUtil.info(LoggerType.TRACE, "Validating schema using sql:" + sql);
      stmt = connection.prepareStatement(sql);
      // bug-27026195, reset dbColIndex in case a column can't be added in
      // prepared statement where clause.
      int dbColIndex = 0;
      for (int j = 0; j < numAttrs; j++)
      {
        if (canAddToStmt(attrMetadata[j].getDatatype()))
        {
          setStmtValue(attrMetadata[j].getDatatype(), dbColIndex, stmt);
          dbColIndex++;
        }
      }
      stmt.execute();
      stmt.close();
    } catch (Exception e)
    {
      LogUtil.warning(LoggerType.TRACE,
          "Schema mismatch \n" + sql + "\n" + e.toString());
      throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH,
          new Object[] { tableName });
    } finally
    {
      if (stmt != null)
      {
        stmt.close();
        LogUtil.info(LoggerType.TRACE,
            "PreparedStatement used for validating" + " schema closed");
      }
    }
  }

  /**
   * Validate Database Schema using DatabaseMetadata Object
   * 
   * @param numAttrs
   * @param attrNames
   * @param attrTypes
   * @param tableName
   * @param connection
   * @throws CEPException
   * @throws SQLException
   */
  public static void validateDBSchema(int numAttrs, String[] attrNames,
      Datatype[] attrTypes, String tableName, Connection connection)
      throws CEPException, SQLException
  {
    DatabaseMetaData md = connection.getMetaData();
    ResultSet rs = md.getColumns(null, null, tableName.toUpperCase(), "%");
    int numTableColsMatched = 0;
    boolean isAnyColMatched = false;
    while (rs.next())
    {
      for (int i = 0; i < numAttrs; i++)
      {
        // System.out.println(getSQLDataType(attrTypes[i]));
        int sqlType = attrTypes[i].getSqlType();
        if (attrNames[i].equalsIgnoreCase(rs.getString("COLUMN_NAME"))
            && sqlType == Integer.parseInt(rs.getString("DATA_TYPE")))
          isAnyColMatched = true;
      }
      if (isAnyColMatched)
      {
        numTableColsMatched++;
        isAnyColMatched = false;
      }
    }
    assert numTableColsMatched <= numAttrs : numTableColsMatched;
    if (numTableColsMatched < numAttrs)
    {
      LogUtil.info(LoggerType.TRACE, "Validating schema failed");
      throw new CEPException(InterfaceError.SCHEMA_MISMATCH);
    }
  }

}
