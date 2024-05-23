/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/CustomerLogMsg.java /main/4 2010/05/19 07:12:23 sborah Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Log messages that appear in Customer facing log file.
    All new messages be introduced at the end and no existing message be deleted.
    The last error number is 39.
    
   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      05/19/10 - typo
    hopark      03/25/09 - change active msg
    skmishra    12/29/08 - adding validate action
    hopark      11/16/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/CustomerLogMsg.java /main/4 2010/05/19 07:12:23 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

import java.text.MessageFormat;

public enum CustomerLogMsg implements ErrorCode
{
  FABRIC_CREATE_DRVCONTEXT(
      1,
      "FabricDriver: eventNsp = {0} ; eventName = {1}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Driver context is created.",
      "none"
  ),
  JMSRECV_INIT(
      2,
      "JMSRecver : initialized",
      ErrorType.NOTIFICATION,
      1,
      false,
      "JMSReceiver is initialized.",
      "none"
  ),
  JMSRECV_SHUTDOWN(
      3,
      "JMSRecver - shutdown performed",
      ErrorType.NOTIFICATION,
      1,
      false,
      "JMSReceiver has been shutdown.",
      "none"
  ),
  RECVSERVLET_INIT(
      4,
      "RecvServlet Init",
      ErrorType.NOTIFICATION,
      1,
      false,
      "RecvServlet is initialized.",
      "none"
  ),
  RECVSERVLET_DOGET(
      5,
      "RecvServlet doGet",
      ErrorType.NOTIFICATION,
      1,
      false,
      "RecvServlet.doGet is invoked.",
      "none"
  ),
  RECVSERVLET_RECEIV(
      6,
      "Recvservlet : received : {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "RecvServlet.doGet is invoked.",
      "none"
  ),
  RECVSERVLET_DESTROY(
      7,
      "RecvServlet Destroy",
      ErrorType.NOTIFICATION,
      1,
      false,
      "RecvServlet is destroyed.",
      "none"
  ),
  ACTIVATE_DDL(
      8,
      "Invoking DDL {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "DDL is invoked.",
      "none"
  ),
  ACTIVATE_DDL_SUCCESS(
      9,
      "DDL is successfully invoked. DDL was {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "DDL is successfully invoked.",
      "none"
  ),
  ACTIVATE_DDL_FAILURE(
      10,
      "DDL invocation is failed. DDL was {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "DDL invocation is failed.",
      "none"
  ),
  REGISTER_CEPSTATS_MBEANS(
      11,
      "CEPServiceEngine: Registering CEPStats runtime mbeans",  
      ErrorType.NOTIFICATION,
      1,
      false,
      "Registering CEPStats runtime mbeans.",
      "none"
  ),
  PREPARE_CONFIG_CHANGE(
      12,
      "CEP: prepareConfigChange {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Prepare config change.",
      "none"
  ),
  COMMIT_CONFIG_CHANGE(
      13,
      "CEP: commitConfigChange {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Commit config change.",
      "none"
  ),
  ONEVENT_START(
      14,
      "CEPServiceEngine: onEvent start",  
      ErrorType.NOTIFICATION,
      1,
      false,
      "Start onEvent.",
      "none"
  ),
  ONEVENT_HANDLE(
      15,
      "CEPServiceEngine: handleEvent: tableName: {0} payload: {1}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Handling event.",
      "none"
  ),
  ONEVENT_NODE_FAILED(
      16,
      "CEPServiceEngine: handleEvent: cannot process node: ",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Failed to process node.",
      "none"
  ),
  ONEVENT_INSERT_FAILED(
      17,
      "CEPServiceEngine: handleEvent: cannot insert",  
      ErrorType.NOTIFICATION,
      1,
      false,
      "Failed to insert.",
      "none"
  ),
  ONEVENT_END(
      18,
      "CEPServiceEngine: handleEvent end",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Failed to insert.",
      "none"
  ),
  INIT_COMPONENT(
      19,
      "CEP: Initializing component {0}", 
      ErrorType.NOTIFICATION,
      1,
      false,
      "Initialize component.",
      "none"
  ),
  UNINIT_COMPONENT(
      20,
      "CEP: Uninitializing component {0}", 
      ErrorType.NOTIFICATION,
      1,
      false,
      "Uninitialize component.",
      "none"
  ),
  LOAD_COMPONENT(
      21,
      "CEPServiceEngine: load component {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Load component",
      "none"
  ),
  LOADED_COMPONENT(
      22,
      "CEPServiceEngine: {0} loaded ...",
      ErrorType.NOTIFICATION,
      1,
      false,
      "CEPServiceEngine loaded.",
      "none"
  ),
  UNLOAD_COMPONENT(
      23,
      "CEPServiceEngine: Unload component {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "CEPServiceEngine unloaded.",
      "none"
  ),
  UNLOAD_EXCEPTION(
      24,
      "CEPServiceEngine: unload exceptions ",
      ErrorType.NOTIFICATION,
      1,
      false,
      "CEPServiceEngine unload exception.",
      "none"
  ),
  DEPLOY_COMPONENT(
      25,
      "CEP: Deploying component {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "CEPServiceEngine deploy component.",
      "none"
  ),
  UNDEPLOY_COMPONENT(
      26,
      "CEP: Undeploying component {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "CEPServiceEngine undeploy component.",
      "none"
  ),
  CONFIG_URL(
      27,
      "CEPServiceEngine: ConfigFileURLs: {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "getConfigFileUrl is invoked.",
      "none"
  ),
  FABRICDEST_START(
      28,
      "FabricDestination: start",
      ErrorType.NOTIFICATION,
      1,
      false,
      "FabricDestination start.",
      "none"
  ),
  FABRICDEST_EVENTBUS(
      29,
      "FabricDestination: eventBus = {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "FabricDestination eventbus.",
      "none"
  ),
  FABRICDEST_INSTMANAGER(
      30,
      "FabricDestination: instanceManager = {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "FabricDestination instance manager.",
      "none"
  ),
  FABRICDEST_ENDSTART(
      31,
      "FabricDestination: end start",
      ErrorType.NOTIFICATION,
      1,
      false,
      "FabricDestination end start.",
      "none"
  ),
  FABRICDEST_COMPLETEEND(
      32,
      "FabricDestination: complete end",
      ErrorType.NOTIFICATION,
      1,
      false,
      "FabricDestination complete end .",
      "none"
  ),
  FABRICDEST_PUTNEXT(
      33,
      "FabricDestination: putNext: xmlData = {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "FabricDestination putNext.",
      "none"
  ),
  FABRICDEST_PUTNEXT_PUBLISHED(
      34,
      "FabricDestination: putNext: published ",
      ErrorType.NOTIFICATION,
      1,
      false,
      "FabricDestination putNext published.",
      "none"
  ),
  EXEC_CQL_DDL(
      35,
      "DDL on the jdbc server: {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Execute ddl.",
      "none"
  ),
  EXEC_CQL_DML(
      36,
      "DML on the server",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Execute dml.",
      "none"
  ),
  EXEC_GETNEXT(
      37,
      "getNext() on server",
      ErrorType.NOTIFICATION,
      1,
      false,
      "Execute getNext.",
      "none"
  ),
  VALIDATE_DDL_SUCCESS(
      38,
      "DDL validation complete. Result: SUCCESS. DDL was {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "DDL validation complete. Result: SUCCESS.",
      "none"
  ),
  VALIDATE_DDL_FAILURE(
      39,
      "DDL validation complete. Result: FAILURE. DDL was {0}",
      ErrorType.NOTIFICATION,
      1,
      false,
      "DDL validation complete. Result: FAILURE.",
      "none"
  ),
  SERVER_START_FAILURE(
      40,
      "Problem starting CEPServer({0}) ",
      ErrorType.ERROR,
      1,
      false,
      "Problem starting CEPServer",
      "Contact Oracle Support"
  );
      
  private ErrorDescription ed;

  private static ThreadLocal<MessageFormat> mFormat = new ThreadLocal<MessageFormat>() {
    protected synchronized MessageFormat initialValue() {
      return new MessageFormat("");
    }
  };

  CustomerLogMsg(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(
            ErrorNumberBase.Server_CEPServer + num, text, type, level,
            isDocumented, cause, action, "CustomerLogMsg");
  }

  public ErrorDescription getErrorDescription()
  {
    return ed;
  }

}
