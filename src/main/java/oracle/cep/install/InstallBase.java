/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/install/InstallBase.java /main/9 2012/05/09 06:42:13 udeshmuk Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/30/12 - add bi sql equivalent
    anasrini    03/04/12 - XbranchMerge anasrini_bug-13654756_ps6 from
                           st_pcbpel_pt-ps6
    anasrini    02/02/12 - add addAggrFunc(String, String)
    udeshmuk    06/22/11 - add sqlEquivalent
    sbishnoi    10/06/10 - XbranchMerge sbishnoi_fix_tobigdecimal from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    10/04/10 - adding new API for addFunc
    alealves    02/02/09 - userFunctionManager now throws CEPException 
    parujain    01/28/09 - transaction mgmt
    hopark      01/09/09 - add getFuncs
    hopark      12/03/08 - fix duplicate function creation
    hopark      11/10/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/install/InstallBase.java /main/9 2012/05/09 06:42:13 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.install;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.UserFunctionManager;
import oracle.cep.metadata.UserFunctionManager.AggrFuncDesc;
import oracle.cep.metadata.UserFunctionManager.FuncDesc;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;

public class InstallBase
{
  protected static HashMap<String, List<FuncDesc>> s_fns = new HashMap<String, List<FuncDesc>>();
  protected static HashMap<String, List<String>> s_fns_strddls 
    = new HashMap<String, List<String>>();
  
  BitSet        m_funcInstallState;
  
  public InstallBase()
  {
    m_funcInstallState = new BitSet();
  }
  
  //This will be used by ColtInstall
  public void addFunc(String fName, Datatype[] types, Datatype retType, String clz)
  {
    FuncDesc fdesc = new FuncDesc(fName, types, retType, clz, null, null);
    addFuncDesc(fName, fdesc);
  }
  
  public void addFunc(String fName, Datatype[] types, Datatype retType, String clz,
                      String sqlEquivalent, String biSqlEquivalent)
  {
    FuncDesc fdesc = new FuncDesc(fName, types, retType, clz, sqlEquivalent, biSqlEquivalent);
    addFuncDesc(fName, fdesc);
  }
  
  public void addAggrFunc(String fName, Datatype[] types, Datatype retType, String clz, boolean isIncremental)
  {
    //fName = fName.toUpperCase();        //Somehow we are using uppercase for AggrFuncs. 
    FuncDesc fdesc = new AggrFuncDesc(fName, types, retType, clz, isIncremental);
    addFuncDesc(fName, fdesc);
  }
  
  public void addFunc(String fullName, String cqlDdl)
  {
    List<String> fList = s_fns_strddls.get(fullName);
    if(fList == null)
    {
      fList = new ArrayList<String>();
      s_fns_strddls.put(fullName, fList);
    }
    fList.add(cqlDdl);
  }

  public void addAggrFunc(String fullName, String cqlDdl)
  {
    addFunc(fullName, cqlDdl);
  }
  
  private void addFuncDesc(String fName, FuncDesc fdesc)
  {
    List<FuncDesc> flist = s_fns.get(fName);
    if (flist == null)
    {
      flist = new ArrayList<FuncDesc>();
      s_fns.put(fName, flist);
    }
    flist.add(fdesc);
  }  
 
  protected void installFuncs(ExecContext ec, List<FuncDesc> descs)
  {
    UserFunctionManager ufnMgr = ec.getUserFnMgr();
    ITransaction oldTxn = ec.getTransaction();
    
    for (FuncDesc fndesc: descs)
    {
      int id = fndesc.getId();
      if (!m_funcInstallState.get(id))
      {
        ITransaction txn = ec.getTransactionMgr().begin();
        ec.setTransaction(txn);
        try
        {
          if (fndesc.isAggrFunc())
          {
            ufnMgr.registerAggrFunction((AggrFuncDesc) fndesc, ec.getDefaultSchema());
          }
          else
          {
            ufnMgr.registerSimpleFunction(fndesc, ec.getDefaultSchema());
          }
          txn.commit(ec);
        }
        catch(CEPException e)
        {
          txn.rollback(ec);
          LogUtil.warning(LoggerType.TRACE, e.toString());
          LogUtil.logStackTrace(e);
        }
        m_funcInstallState.set(id);
      }
    }
    ec.setTransaction(oldTxn);
  }
  
  
  
  public boolean installFuncs(ExecContext ec, String fn, Datatype[] types)
  {
    // Get All the functions with name fn which are defined with their FuncDesc
    List<FuncDesc> descs = s_fns.get(fn);
    // Get All the function with name fn which are defined with their DDLs
    List<String> str_ddls = s_fns_strddls.get(fn);
    
    boolean isInstalled = false;
    
    if (descs != null)
    {
      installFuncs(ec, descs);
      isInstalled = true;
    }
    
    if(str_ddls != null)
    {
      installFuncs(str_ddls, ec);
      isInstalled = true;
    }    
    
    return isInstalled;
  }

  private void installFuncs(List<String> strDdls, ExecContext ec)
  {
    ITransaction oldTxn = ec.getTransaction();
    
    String oldSchemaName = ec.getSchemaName();
    
    for(String ddl : strDdls)
    {
      // Sets the current schema prior to execute all DDLs 
      ec.setSchema(Constants.DEFAULT_SCHEMA);
      CommandInterpreter cmd = ec.getCmdInt();
      Command c = new Command();

      // Add view
      c.setCql(ddl);
      cmd.execute(c);
      if (!c.isBSuccess())
      {
        Exception e = c.getException();
        LogUtil.severe(LoggerType.TRACE, "Installation failed with " + ddl + "\n" + 
            e == null ? "" : e.toString() );
      }
    }
    ec.setSchema(oldSchemaName);
    ec.setTransaction(oldTxn);
  } 

  /**
   * Return a string having comma-separated built-in function names.
   * The list will include all aggregate as well as single-element functions.
   * @return a string having comma separated values where each value is a builtin function name
   */
  public String getFuncs()
  {
    StringBuilder b = new StringBuilder();
    Set<String> reserveds = s_fns.keySet();
    int i = 0;
    for (String r : reserveds) {
      if (i > 0) b.append(",");
      i++;
      b.append(r);
    }
    return b.toString();
  }
  
  /**
   * Return a string having comma-separated built-in aggregate function names.
   * @return a string having comma separated values where each value is a builtin function name
   */
  public String getAggrFuncs()
  {
    return getFuncs(true);
  }
  
  /**
   * Return a string having comma-separated built-in aggregate function names.
   * @return a string having comma separated values where each value is a builtin function name
   */
  public String getSingleElementFuncs()
  {
    return getFuncs(false);
  }
  
  /**
   * Helper method return a string having comma-separated built-in function names.
   * @param isAggr If true, method will return list of aggregate functions
   *               otherwise method will return list of single-element functions
   * @return a string having comma separated values where each value is a builtin function name
   */
  private String getFuncs(boolean isAggr)
  {
    StringBuilder b = new StringBuilder();
    Set<String> reserveds = s_fns.keySet();
    int i = 0;
    for (String r : reserveds) {
      List<FuncDesc> funcList = s_fns.get(r);
      boolean isAggrFlag = true;
      if(funcList != null)
      {
        for(FuncDesc fd: funcList)
          isAggrFlag = isAggrFlag && fd.isAggrFunc();
      }
      if(isAggr == isAggrFlag)
      {
        if (i > 0) b.append(",");
        i++;
        b.append(r);
      }
    }
    return b.toString();
  }
}
