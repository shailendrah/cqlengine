/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SynonymManager.java /main/1 2010/01/06 20:33:11 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/23/09 - synonym manager
    parujain    11/23/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SynonymManager.java /main/1 2010/01/06 20:33:11 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.parser.CEPSynonymDefnNode;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;

@DumpDesc(evPinLevel = LogLevel.MSYNONYM_ARG, dumpLevel = LogLevel.MSYNONYM_INFO, verboseDumpLevel = LogLevel.MSYNONYM_LOCKINFO)
public class SynonymManager extends CacheObjectManager
implements ILoggable
{
  ExecContext execContext;

  public SynonymManager(ExecContext ec, Cache cache)
  {
    super(ec.getServiceManager(), cache);
    this.execContext = ec;
  }

  public void init(ConfigManager cfg)
  {
  }
  
  public int addSynonym(CEPSynonymDefnNode node, 
	                    String cql, String schema) 
  throws MetadataException, CEPException
  {
    int synId = -1;
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    String synName = node.getSynonym();
    Synonym synonym = null;
    
    LogLevelManager.trace(LogArea.METADATA_SYNONYM, LogEvent.MSYNONYM_CREATE, this, cql,
            synName);
    
    // Create the object
    // l will be null if there is already an object with same name in the
    // namespace
    l = createObject(txn, synName, schema, CacheObjectType.SYNONYM, null);
    
    if(l == null)
    {
      throw new MetadataException(MetadataError.SYNONYM_ALREADY_EXISTS,
                node.getStartOffset(), node.getEndOffset(),
                new Object[]
                { synName });
    	
    }
    
    synonym = (Synonym)l.getObj();
    synonym.setTarget(node.getActualName());
    synonym.setSynonymType(node.getSynonymType());
    synId = synonym.getId();
    return synId;
  }

  public void dropSynonym(String name, String schema)
  throws MetadataException, CEPException
  {
	CacheLock l = null;
    ITransaction txn = execContext.getTransaction(); 
    int id;
    
    l = findCache(txn, new Descriptor(name, CacheObjectType.SYNONYM, 
	           schema, null), false);
    
    if(l == null)
    {
      throw new MetadataException(MetadataError.SYNONYM_NOT_FOUND,
                                  new Object[]
                                  { name });
    }
    
    Synonym synonym = (Synonym)l.getObj();
    id = l.getObj().getId();
    release(txn, l);
    l = null;
    
    LogLevelManager.trace(LogArea.METADATA_SYNONYM, LogEvent.MSYNONYM_DELETE, this,
            name);
    
    Locks locks = null;
 // Delete the object
    // l will be null if no object existed
    locks = deleteCache(txn, id);

    // If object not found throw the appropriate exception
    if (locks == null)
    {
      throw new MetadataException(MetadataError.SYNONYM_NOT_FOUND,
                                  new Object[]
                                  { name });
    }
  }
  
  public String getSynonymTypeTarget(String name, String schema)
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction(); 
    
    try{
      l = findCache(txn, new Descriptor(name, CacheObjectType.SYNONYM, 
	             schema, null), false);
      if(l != null)
      {
        Synonym synonym = (Synonym)l.getObj();
        if(synonym.getSynonymType() == SynonymType.TYPE)
          return synonym.getTarget();
        else
          return null;
      }
    }
    finally
    {
      if(l != null)
        release(txn, l);
    }
    return null;
  }
  
  @Override
  public ILogLevelManager getLogLevelManager() {
    return execContext.getLogLevelManager();
  }

  @Override
  public int getTargetId() {
    return 0;
  }

  @Override
  public String getTargetName() {
    return "SynonymManager";
  }

  @Override
  public int getTargetType() {
    return 0;
  }

  @Override
  public void trace(IDumpContext arg0, ILogEvent arg1, int arg2, Object[] arg3) {

  }

  @Override
  public void dump(IDumpContext dumper) {
    super.dump(dumper, LogTags.TAG_SYNONYMS, CacheObjectType.SYNONYM);
  }
  
}
