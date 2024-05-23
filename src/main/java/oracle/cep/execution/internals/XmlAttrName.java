/* $Header: XmlAttrName.java 19-may-2008.17:09:15 parujain Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/19/08 - AttrName
    parujain    05/19/08 - Creation
 */

/**
 *  @version $Header: XmlAttrName.java 19-may-2008.17:09:15 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals;

public class XmlAttrName 
{
  String attrName;
  int    namePos;
  int    nameRole;
  
  public XmlAttrName(String name)
  {
    this.attrName = name;
    this.namePos = -1;
    this.nameRole = -1;
  }
  
  public XmlAttrName(int role, int pos)
  {
    this.attrName = null;
    this.nameRole = role;
    this.namePos = pos;
  }
  
  public boolean isNameExpr()
  {
    return(attrName == null);
  }
  
  public String getName()
  {
    return this.attrName;
  }
  
  public int getAttrRole()
  {
    return this.nameRole;
  }
  
  public int getAttrPos()
  {
    return this.namePos;
  }
}
