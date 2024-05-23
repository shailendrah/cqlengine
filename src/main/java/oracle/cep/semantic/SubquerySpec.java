/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SubquerySpec.java /main/2 2011/09/23 11:16:35 vikshukl Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    08/25/11 - add subquery alias to subquery spec
    vikshukl    07/13/11 - subquery support
    vikshukl    07/13/11 - Creation
 */

/**
 *  @version $Header: SubquerySpec.java 13-jul-2011.09:50:40 vikshukl Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

public class SubquerySpec
{
  /* internal id by which this subquery relation is referenced */
  private int varid; 
  
  /* alias by which the subquery is reference else where */
  private String subqname;
  
  /* at this point not 100% sure whether it will be needed for logplan 
   * Ideally, SemQuery should self-sufficient 
   */
  private SymbolTable symtab;
  
  /* post semantic representation of this relation.
   * This should handle SFW, Setop and GenericSetop transparently.
   */
  private SemQuery query;
  
  


  /**
   * SubquerySpec constructor
   * @param varid
   * @param name 
   * @param symtab
   * @param query
   */
  public SubquerySpec(int varid, String name, SymbolTable symtab, SemQuery query) {
    super();
    this.varid = varid;
    this.subqname = name;
    this.symtab = symtab;
    this.query = query;
  }

  public int getVarid() {
    return varid;
  }

  public String getSubqname() {
    return subqname;
  }

  public SymbolTable getSymtab() {
    return symtab;
  } 
  
  public void setVarid(int varid) {
    this.varid = varid;
  }

  public void setSubqname(String subqname) {
    this.subqname = subqname;
  }

  public void setSymtab(SymbolTable symtab) {
    this.symtab = symtab;
  }

  public SemQuery getQuery() {
    return query;
  }

  public void setQuery(SemQuery query) {
    this.query = query;
  }  
}
