package oracle.cep.driver.net;

import oracle.cep.driver.util.FatalException;
import oracle.cep.driver.util.InitManager;
//import cep.oracle.HttpSoap11Client;
import java.io.*;
import java.net.*;

public class ServerConnection extends IServerConnection{

    private static class HttpClient {
        public void setEndpoint(String s) throws Exception {}
        public int execute(String s) throws IOException { return 0;}
        public void startNamedQuery(String s) throws IOException { }
        public void setStreamSource2(String name, String schema, String path) throws IOException { }
        public void setRelationSource2(String name, String schema, String path) throws IOException { }
        public String explainPlan() throws IOException  { return null;}
        public void runScheduler() throws IOException  {}
    }
    HttpClient cmd;
  
    public ServerConnection(String host, int port) throws FatalException {
      this.host = host;
      this.port = port;
      try {
        cmd = new HttpClient();
	    String oc4j  = InitManager.getOc4jPath();
        cmd.setEndpoint(oc4j);
      }
      catch (Exception err) {
        System.out.println("HttpSoap11Client failed");
      }
    }
    
    public RegInputRet registerInput (String regInputStr) 
      throws FatalException {	
      RegInputRet ret = new RegInputRet ();
	
      try {
	    ret.inputId = cmd.execute (regInputStr);
        return ret;
      }
      catch (IOException e) {
	    throw new FatalException (e.getMessage());
      }
    }
    
    public void startNamedQuery(String queryName) throws FatalException {
      try {
        cmd.startNamedQuery(queryName);
      }
      catch (IOException e) {
        throw new FatalException (e.getMessage());
      }
    }
    
    public void bindSrcDest(String name, String scheme, String path, int type)
      throws FatalException {
      try {
        if(type == 1) {
          cmd.setStreamSource2(name, scheme, path);
        }
        else if(type == 2) {
          cmd.setRelationSource2(name, scheme, path);
        }
        else if(type == 3) {
          StringBuilder sb = new StringBuilder();
          sb.append("alter query ");
          sb.append(name);
          sb.append(" add destination  \"<EndPointReference> <Address>file://");
          sb.append(path);
          sb.append("</Address> </EndPointReference>\";");
          cmd.execute(sb.toString());
        }
      }
      catch (IOException e) {
        throw new FatalException (e.getMessage());
      }
    }
    
    /** 
     * Register a query for which we want an output.
     */ 
    
    public RegQueryRet registerOutQuery (String query) 
      throws FatalException {
      RegQueryRet ret = new RegQueryRet ();
      try {
		ret.queryId = cmd.execute(query);
		return ret;
	  }
      catch (IOException e) {
	    throw new FatalException (e.getMessage());
      }
    }
    
    /**
     * Register a query for which we do not need an output (pure view)
     */
    
    public RegQueryRet registerQuery (String query) 
      throws FatalException {
      RegQueryRet ret = new RegQueryRet ();
      try {
		ret.queryId = cmd.execute(query);
		return ret;
	  }
      catch (IOException e) {
	    throw new FatalException (e.getMessage());
      }
    }
    
    public GenPlanRet genPlan () throws FatalException {
      GenPlanRet ret = new GenPlanRet ();
      try {
	    ret.errorCode = getErrorCode ();
	    ret.planString = cmd.explainPlan();
	    return ret;
      }
      catch (IOException e) {
	    throw new FatalException (e.getMessage());
      }
    }
    
    public ExecRet execute () throws FatalException {
      ExecRet ret = new ExecRet ();
	
      try {
        cmd.runScheduler();
        ret.errorCode = getErrorCode ();
        return ret;
      }
      catch (IOException e) {
        throw new FatalException (e.getMessage());
      }
    }
    
}
