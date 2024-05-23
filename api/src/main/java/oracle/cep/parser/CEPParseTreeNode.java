/* $Header: pcbpel/cep/src/oracle/cep/parser/CEPParseTreeNode.java /main/2 2008/08/25 19:27:24 parujain Exp $ */

/* Copyright (c) 2005, 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    08/11/08 - error location
    anasrini    12/20/05 - Interface for a parse tree node 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPParseTreeNode.java /main/2 2008/08/25 19:27:24 parujain Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Marker interface for a CEP Parse tree node
 */

public interface CEPParseTreeNode {
	
  public void setStartOffset(int start);
  
  public void setEndOffset(int end);
  
  public int getStartOffset();
  
  public int getEndOffset();
}
