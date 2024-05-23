/* $Header: pcbpel/cep/test/src/oracle/cep/test/DBDestination.java /main/4 2008/10/24 15:50:21 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    sborah      09/29/08 - changing stbda16 to stacy37
    sbishnoi    03/04/08 - changing database from local m/c(stadm31) to
                           performance m/c(stbda16)
    sbishnoi    02/24/08 - add support for SegAvgSpeed and Estimated time
    sbishnoi    02/13/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/DBDestination.java /main/4 2008/10/24 15:50:21 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.demo;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.StringBuffer;


public class DBDestination extends QueryOutputBase
{
   private Connection con;
   private Statement s;
   StringBuffer sql;
   public DBDestination(ExecContext ec)
   {
     super(ec);
     try
     {
       DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
       con = DriverManager.getConnection("jdbc:oracle:thin:@stacy37.us.oracle.com:5521:rdb", "lrbadmin", "lrbadmin");
       System.out.println("Connection Established");
       s = con.createStatement();
       s.execute("truncate table ACCSEG");
       s.execute("update SegSpeed set avg_speed = 0");
       System.out.println("Initialization Done");
     }
     catch (SQLException e2){
       System.out.println(e2.toString());
     }
     catch (Exception e3) {
       System.out.println(e3.toString());
     }
   }
   
   public void putNext(TupleValue tv, QueueElement.Kind k) throws CEPException
   {
     assert eprArgs.length == 1 : eprArgs.length;
     
     if(eprArgs[0].equalsIgnoreCase("AccSeg"))
     {
       sql = new StringBuffer();  
       
       System.out.println("Inside Class:" + tv.iValueGet(0)+  k.name());
       if(k == QueueElement.Kind.E_PLUS)
       {
         sql.append("insert into ACCSEG values (");
         sql.append(tv.iValueGet(0));
         sql.append(")");
       }
       else if(k == QueueElement.Kind.E_MINUS)
       {
         sql.append("delete from ACCSEG where SEGID = ");
         sql.append(tv.iValueGet(0));
       }
       else if(k == QueueElement.Kind.E_UPDATE)
       {
         // No need to Construct UPDATE SQL statement
       }
       System.out.println(sql.toString());
       
     }
     else if(eprArgs[0].equalsIgnoreCase("SegSpeed"))
     {
       if(k == QueueElement.Kind.E_PLUS)
       {
         sql = new StringBuffer();
         sql.append("update SegSpeed set avg_speed = ");
         sql.append(tv.fValueGet(1));
         sql.append(" where segid = ");
         sql.append(tv.iValueGet(0));
       }
     }
     
     try {
       s.execute(sql.toString());
       con.commit();
     }
     catch (SQLException e2) {
       System.out.println(e2.toString());
     }
     catch (Exception e3) {
       System.out.println(e3.toString());
     }
     
     
   }
   public void start(){}
   public void end(){}
}

