package oracle.cep.test.embedded;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.server.CEPServer;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.service.CEPManager;

public class test
{
  public static void main(String[] argv)
  {
    try {
      ConfigManager  cfg = new ConfigManager();
      cfg.setSchedOnNewThread(true);
      cfg.setSchedRuntime(Constants.DEFAULT_RUN_TIME);
      cfg.setDirectInterop(true);
      
      CEPServerRegistryImpl reg = new CEPServerRegistryImpl();
      CEPManager cep = CEPManager.getInstance();
      cep.setConfig(cfg);
      cep.setServerRegistry(reg);
      cep.init();
      CEPServer server = reg.getSystemServer();
      
      String schema = "test";
      String streamName = "S";
      server.executeDDL("register stream " + streamName + " (c1 integer, c2 integer)", schema);
      server.executeDDL("alter stream " + streamName + " add source push", schema);

      server.executeDDL("create query q as select * from " + streamName + " [NOW]", schema);
      String dest = "<EndPointReference><Address>java://oracle.cep.test.embedded.OutputDestination</Address></EndPointReference>";
      server.executeDDL("alter query q add destination \""+dest+"\"", schema);
      server.executeDDL("alter query q start", schema);
      server.executeDDL("alter system run", schema);
      
      for (int i = 0; i < 50; i++)
      {
        long tm = System.currentTimeMillis();
        AttributeValue[] attrval = new AttributeValue[2];
        attrval[0] = new IntAttributeValue("c1", i);
        attrval[1] = new IntAttributeValue("c2", i * 2);
        TupleValue tv = new TupleValue(streamName, tm, attrval, false );
        server.executeDML(tv, schema);
      }
    }

    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();
    }
    
  }
}
