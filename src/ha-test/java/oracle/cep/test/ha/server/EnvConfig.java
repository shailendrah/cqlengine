package oracle.cep.test.ha.server;

import java.io.File;
import java.util.logging.Logger;
import java.util.HashMap;

import oracle.cep.logging.ILoggerFactory;
import oracle.cep.service.CEPDefaultEnvConfig;
import oracle.cep.service.IArchiverFinder;
import oracle.cep.service.ICartridgeLocator;
import oracle.cep.service.IDataSourceFinder;
import oracle.cep.service.IQueryDestLocator;
import oracle.cep.util.StringUtil;
import oracle.cep.service.IFaultHandler;

/**
 * EnvConfig provides a runtime environment for CQL engine.
 * 
 * @author hopark
 * @since   12c
 */
public class EnvConfig extends CEPDefaultEnvConfig 
{
    private static Logger logger = Logger.getLogger(EnvConfig.class.getName());

  IDataSourceFinder m_dsFinder = null;
  IQueryDestLocator m_queryDestLocator = null;
  ICartridgeLocator m_cartridgeLocator = null;
  IArchiverFinder   m_archiverLocator = null;
  
  String            m_homeFolder;
  
  // Storage
  String            m_storageName;
  boolean           m_restoreMetadata;
  String            m_storageFolder;
  String            m_traceFolder;
  
  /* Fault Handler - the runtime exceptions from CQL will be caught 
   * by the fault handler instance if it is set in the env config */
  IFaultHandler     m_faultHandler;
  
  private static EnvConfig s_instance;  
  public static synchronized EnvConfig getInstance()
  {
    if (s_instance == null)
    {
      s_instance = new EnvConfig();
    }
    return s_instance;
  }
  
  private EnvConfig()
  {
    m_homeFolder  = CQLProperties.getProperty(CQLProperties.WORK_FOLDER, null);
    if (m_homeFolder == null)
      m_homeFolder = Util.getTempPath(null  , null);
    if (m_homeFolder.endsWith(File.separator))
      m_homeFolder.substring(0, m_homeFolder.length()-1);
    m_storageFolder  = CQLProperties.getProperty(CQLProperties.STORAGE_FOLDER, 
                                          Defaults.METADATA_STORAGE_FOLDER);
    m_storageFolder = getFolder("storageFolder", m_storageFolder);
    m_traceFolder = getFolder("traceFolder", Defaults.TRACE_FOLDER);
    m_storageName = CQLProperties.getProperty(
         CQLProperties.METADATA_STORAGE_NAME, Defaults.METADATA_STORAGE_NAME);
    m_restoreMetadata =  CQLProperties.getProperty(
        CQLProperties.METADATA_RESTORE_PROP, Defaults.METADATA_RESTORE);
    m_faultHandler = CQSFaultHandler.getInstance();
  }
  
  private String getFolder(String name, String path)
  {
    HashMap<String,String> valMap = new HashMap<String,String>();
    valMap.put("HOME", m_homeFolder);
    path = StringUtil.expand(path, valMap);
    try
    {
      //File f = new File(path);
      //f.mkdirs();
    }
    catch(Exception e)
    {
    	logger.severe("Failed to create "+ name + " at " + path + "\ngetFolder\n" + e);
    }
    return path;
  }
  
  public void setDataSourceFinder(IDataSourceFinder v){m_dsFinder = v;}

  @Override
  public IDataSourceFinder getDataSourceFinder(){return m_dsFinder;}

  public void setQueryDestLocator(IQueryDestLocator v) {m_queryDestLocator = v;}
  
  @Override
  public IQueryDestLocator getQueryDestLocator() {return m_queryDestLocator;}
  
  @Override
  public ILoggerFactory getLoggerFactory()
  {
    return CQLLoggerFactory.getInstance();
  }
  
  public void setStorageFolder(String v) {m_storageFolder = getFolder("storageFolder", v);}
  
  @Override
  public String getStorageFolder() {return m_storageFolder;}

  @Override
  public ICartridgeLocator getCartridgeLocator() {return m_cartridgeLocator;}
  public void setCartridgeLocator(ICartridgeLocator locator) {m_cartridgeLocator = locator;}

  public String getStorageName() {return m_storageName;}
  
  public boolean isRestoreMetadata()
  {
    return m_restoreMetadata;
  }

  public void setArchiverLocator(IArchiverFinder locator) {m_archiverLocator = locator;}
  
  @Override
  public IArchiverFinder getArchiverFinder() 
  {
	return m_archiverLocator;
  }

  @Override
  public boolean getUseLogXMLTag() {return false;}

  @Override
  public String getTraceFolder() {return m_traceFolder;}

  @Override
  public IFaultHandler getFaultHandler() 
  {
    return m_faultHandler;
  }

  @Override
  public boolean getUseMillisTs()
  {
    return true;
  }
}
