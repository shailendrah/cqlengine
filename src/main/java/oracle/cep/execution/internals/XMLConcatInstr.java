/* $Header: XMLConcatInstr.java 11-jun-2008.17:07:07 skmishra Exp $ */

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

import oracle.xml.parser.v2.XMLDocument;

/**
 *  @version $Header: XMLConcatInstr.java 11-jun-2008.17:07:07 skmishra Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */
public class XMLConcatInstr 
{
  int numargs;
  int[] argRoles;
  int[] argPos;
  XMLDocument parentDoc;
  
  //creates an instr and allocates arrays for arg roles, pos
  public XMLConcatInstr(XMLDocument parentDoc, int numargs)
  {
    this.numargs = numargs;
    this.parentDoc = parentDoc;
    argRoles = new int[numargs];
    argPos = new int[numargs];
  }
  
  //Adds role, pos of concat expression
  public void addArgument(int argNo, int role, int pos)
  {
    assert argNo < numargs : argNo;

    argRoles[argNo] = role;
    argPos[argNo]   = pos;
  }

  public XMLDocument getDocument()
  {
    return parentDoc;
  }
  
  public int getNumargs()
  {
    return numargs;
  }

  public void setNumargs(int numargs)
  {
    this.numargs = numargs;
  }

  public int[] getArgRoles()
  {
    return argRoles;
  }

  public void setArgRoles(int[] argRoles)
  {
    this.argRoles = argRoles;
  }

  public int[] getArgPos()
  {
    return argPos;
  }

  public void setArgPos(int[] argPos)
  {
    this.argPos = argPos;
  }
  
}
