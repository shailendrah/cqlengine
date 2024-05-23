/* $Header: cep/cqlengine/test/src/main/java/oracle.cep.test.cqlxframework/EnvConfigOverride.java st_pcbpel_hopark_cqlmaven/1 2011/03/25 20:11:19 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/23/11 - Creation
 */

/**
 *  @version $Header: EnvConfigOverride.java 23-mar-2011.20:24:11 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cqlxframework.env;

import java.util.Map;

public class EnvConfigOverride
{
	Map<String, Object> m_overrides;
	
	public void setOverride(Map<String, Object> v)
	{
		m_overrides = v;
	}
	
	public Map<String, Object> getOverride()
	{
		return m_overrides;
	}
	  
}