/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/metadata/TestSchema.java /main/4 2010/05/27 09:44:55 parujain Exp $ */

/* Copyright (c) 2008, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/21/10 - remove drop schema ddl
    hopark      10/07/08 - use execContext to remove statics
    parujain    10/01/08 - drop schema
    parujain    09/25/08 - multiple schema
    parujain    09/25/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/metadata/TestSchema.java /main/4 2010/05/27 09:44:55 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.metadata;

import junit.framework.TestCase;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;

import oracle.cep.service.ExecContext;
import oracle.cep.test.InterpDrv;
import oracle.cep.exceptions.CEPException;
public class TestSchema extends TestCase
{
  
  InterpDrv m_driver = null;
  ExecContext m_execContext = null;
  
  public TestSchema(String name)
  {
    super(name);
  }
  
  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
      m_driver = InterpDrv.getInstance();
      try {
          if (!m_driver.setUp(null)) fail();
          m_execContext = m_driver.getExecContext();
      } catch (Exception e) 
      {
          System.out.println(e);
      }
      
      // Further initalization
  }

  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
      // Destroy previous run status..
      m_driver.tearDown();
      m_driver = null;
  }
  
  private static class SchemaDesc
  {
    String ddl;
    String schema; 
    
    SchemaDesc(String cql, String schema)
    {
      this.ddl = cql;
      this.schema = schema;
    }
  }
 
  public void dropSchemaDDL(String schema) throws CEPException
  {
    System.out.println("dropping schema " +schema);
    m_execContext.dropSchema(schema, true);
  }
 
  public boolean executeDDL(String ddl) throws CEPException
  {
      CommandInterpreter cmd = m_execContext.getCmdInt();
      Command c = m_execContext.getCmd(); 
      c.setCql(ddl);
      cmd.execute(c);
      
      System.out.println("Executed " + "\"" + ddl + "\"");
      if (c.isBSuccess()) {
          String res = c.getErrorMsg();
          if (res == null) 
          {
              System.out.println("Success");
          } else {
              System.out.println(res);
          }
      } else {
          System.out.println("Error: " + c.getErrorMsg());        
      }
      return c.isBSuccess();
  }
  
  private static SchemaDesc[] ddls = {
    new SchemaDesc("register stream S(c1 integer, c2 float)", "schema1"),
    new SchemaDesc("register stream S(c1 float, c2 integer)", "schema2"),
    new SchemaDesc("create view v1(c1 float) as select c2 from S", "schema1"),
    new SchemaDesc("create view v1(c1 float) as select c1 from S","schema2"),
    new SchemaDesc("create query q1 as select * from v1", "schema1"),
    new SchemaDesc("create query q1 as select * from v1", "schema2"),
    new SchemaDesc("create function concat(c1 char, c2 char) return char as language java name  \"oracle.cep.test.userfunctions.TkUsrConcat\" ", "schema1"),
    new SchemaDesc("create query q2 as select concat(\"abc\", \"def\") from S", "schema1"),
    new SchemaDesc("drop function concat(char,char)", "schema1"),
    new SchemaDesc("drop query q2", "schema1"),
    new SchemaDesc("drop function concat(char, char)", "schema1"),
    new SchemaDesc("drop function concat(char, char)", "schema1"),
    new SchemaDesc("drop function concat(char, char)", "public"),
    new SchemaDesc("create query q1 as select concat(\"abc\", \"def\") from S", "schema1"),
    new SchemaDesc("create function concat(c1 char, c2 char) return char as language java name  \"oracle.cep.test.userfunctions.TkUsrConcat\" ", "schema1"),
    new SchemaDesc("create query q2 as select concat(\"abc\", \"def\") from S", "schema1"),
    new SchemaDesc("create query q3 as select * from v1", "schema1"),
    new SchemaDesc("drop schema schema1", "schema1")
  };
  
  public void testMultiSchema()
  {
    for(SchemaDesc desc: ddls)
    {
      m_execContext.setSchema(desc.schema);
       try{
         String execddl = desc.ddl;
         int index = execddl.indexOf("schema");
         if(index != -1)
           dropSchemaDDL(desc.schema);
         else
           executeDDL(execddl);
       }catch(CEPException e)
       {
    	 System.out.println("GOT EXCEPTION :" + e.getMessage());
       }
    }
  }
  
  public void main(String[] args)
  {
      junit.textui.TestRunner.run(TestSchema.class);
  }
}
