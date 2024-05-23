/* $Header: cep/cqlengine/test/src/main/java/oracle.cep.test.cqlxframework/FileList.java st_pcbpel_hopark_cqlmaven/2 2011/03/28 10:00:22 hopark Exp $ */

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
 *  @version $Header: FileList.java 23-mar-2011.20:26:12 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cqlxframework;

import java.util.List;

public class FileList
{
	List<String> includes;
	List<String> excludes;
	List<String> intermittents;
	List<String>  rules;
	
	public List<String> getRules() {
		return rules;
	}
	public void setRules(List<String> overrides) {
		this.rules = overrides;
	}
	public List<String> getIntermittents() {
		return intermittents;
	}
	public void setIntermittents(List<String> intermittents) {
		this.intermittents = intermittents;
	}
	public List<String> getIncludes() {
		return includes;
	}
	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}
	public List<String> getExcludes() {
		return excludes;
	}
	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}
}
