/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

package oracle.cep.service;

import java.util.Map;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.ICartridge;

class QualifiedContext
    implements ICartridgeContext
{
  private String m_appName;
  private String m_contextID;
  private Map<String, Object> m_props;
  private ICartridge m_cartridge;

  public QualifiedContext(String appName,
                          String contextID,
                          Map<String, Object> props,
                          ICartridge cartridge)
  {
    m_appName = appName;
    m_contextID = contextID;
    m_props = props;
    m_cartridge = cartridge;
  }

  public String getApplicationName()
  {
    return m_appName;
  }

  public String getContextID()
  {
    return m_contextID;
  }

  public Map<String, Object> getProperties()
  {
      return m_props;
  }

  public ICartridge getCartridge()
  {
      return m_cartridge;
  }

  public boolean equals(Object o)
  {
      if (!(o instanceof QualifiedContext))
          return false;
      
      QualifiedContext other = (QualifiedContext) o;
      return (((m_appName==null && other.getApplicationName()==null) ||
              (m_appName!=null && m_appName.equals(other.getApplicationName())))
              && m_contextID.equals(other.getContextID()));
  }

  public int hashCode()
  {
      return m_contextID.hashCode();
  }
}
