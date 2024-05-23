/* $Header: XMLParseInstr.java 11-jun-2008.17:07:06 skmishra Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    06/11/08 - Creation
 */

package oracle.cep.execution.internals;

import oracle.cep.common.XMLParseKind;
import oracle.xml.parser.v2.XMLDocument;

/**
 *  @version $Header: XMLParseInstr.java 11-jun-2008.17:07:06 skmishra Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class XMLParseInstr {
  
  int argRole;
  int argPos;
  boolean isWellformed;
  XMLParseKind kind;
  XMLDocument parentDoc;
  
  public XMLParseInstr(int argRole, int argPos, boolean isWellformed, XMLParseKind kind, XMLDocument parentDoc)
  {
    this.argRole = argRole;
    this.argPos = argPos;
    this.isWellformed = isWellformed;
    this.kind = kind;
    this.parentDoc = parentDoc;
  }

  public int getArgRole()
  {
    return argRole;
  }

  public void setArgRole(int argRole)
  {
    this.argRole = argRole;
  }

  public int getArgPos()
  {
    return argPos;
  }

  public void setArgPos(int argPos)
  {
    this.argPos = argPos;
  }

  public boolean isWellformed()
  {
    return isWellformed;
  }

  public void setWellformed(boolean isWellformed)
  {
    this.isWellformed = isWellformed;
  }

  public XMLParseKind getKind()
  {
    return kind;
  }

  public void setKind(XMLParseKind kind)
  {
    this.kind = kind;
  }

  public XMLDocument getParentDoc()
  {
    return parentDoc;
  }

  public void setParentDoc(XMLDocument parentDoc)
  {
    this.parentDoc = parentDoc;
  }
  
}

