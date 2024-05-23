/* $Header: pcbpel/cep/test/src/oracle/cep/test/metadata/TestTable.java /main/8 2009/02/17 17:42:53 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Unit test for metadata.Table

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/13/09 - fix NPE
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/24/08 - multiple schema
    hopark      11/02/07 - remove semicolon
    parujain    03/22/07 - handle exception
    hopark      11/10/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/metadata/TestTable.java /main/8 2009/02/17 17:42:53 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.metadata;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.TableManager;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.test.InterpDrv;
import oracle.cep.test.InterpDrv.TableDesc;
import oracle.cep.transaction.ITransaction;
import oracle.cep.common.Constants;

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/metadata/TestTable.java /main/8 2009/02/17 17:42:53 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class TestTable extends TestCase
{
    InterpDrv m_driver = null;
    
    public TestTable(String name)
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

    private static InterpDrv.TableDesc s_tables[] =
    { 
      new InterpDrv.TableDesc("S1", "register stream S1 (c1 integer, c2 float)",
                              "push source"),
      new InterpDrv.TableDesc("S2", "register stream S2 (c1 integer, c2 float)",
                              "push source"),
      new InterpDrv.TableDesc("S3", "register stream S3 (c1 integer, c2 float)",
                              "inpS2.txt"),
      new InterpDrv.TableDesc("S4", "register stream S4 (c1 integer, c2 float)",
                              "inpS2.txt") 
    };

    public void testMultiSource() throws CEPException
    {
        // First call should pass
        m_driver.setCql(s_tables);
        
        // try to add another source, expect to get exception
        ExecContext ec = m_driver.getExecContext();
        TableManager tableMgr = ec.getTableMgr();
        String schema = ec.getSchema();
        int count = 0;
        String sourceFiles[] = {
            "<EndPointReference> <Address>file://test/work/cep/inpfiles/input</Address> </EndPointReference>",
            null
        };
        int i = 0;
        for (InterpDrv.TableDesc td: s_tables)
        {
            String src = sourceFiles[i & 2];
            try
            {
                String ddl = td.getQuery();
                String name = td.getName();
                ITransaction txn = ec.getTransactionMgr().begin();
                ec.setTransaction(txn);
                if ((ddl.startsWith("register stream")) ||
                    (ddl.startsWith("create stream")))
                    tableMgr.addStreamSource(name, schema, src);
                else
                    tableMgr.addRelationSource(name, schema, src);
                txn.commit(ec);
                ec.setTransaction(null);
            } catch (MetadataException e)
            {
                if (e.getErrorCode() == MetadataError.PULL_SRC_EXISTS ||
                    e.getErrorCode() == MetadataError.PUSH_SRC_EXISTS)
                    count++;
                System.out.println(e);
            }
        }
        assertEquals(count, s_tables.length);

        // try to add another push source, expect to get exception
        // The following test need MDS, because the table is already removed
        // from the cache in the above tests.
        if (false) {
        count = 0;
        for (InterpDrv.TableDesc td: s_tables)
        {
            String name = td.getName();
            try
            {
                tableMgr.addStreamPushSource(name, schema);
            } catch (MetadataException e)
            {
                if (e.getErrorCode() == MetadataError.PULL_SRC_EXISTS)
                    count++;
                System.out.println(e);
            }
        }
        assertEquals(count, s_tables.length);
        }
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestTable.class);
    }
}

