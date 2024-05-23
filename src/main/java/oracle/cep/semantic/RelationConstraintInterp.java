/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/RelationConstraintInterp.java /main/3 2009/02/23 06:47:36 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/21/08 - handle constants
    parujain    09/08/08 - support offset
    sbishnoi    12/18/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/RelationConstraintInterp.java /main/3 2009/02/23 06:47:36 sborah Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.metadata.MetadataException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPRelationConstraintNode;

public class RelationConstraintInterp extends NodeInterpreter
{
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    
    CEPRelationConstraintNode outputConstraintNode =
      (CEPRelationConstraintNode)node;
    
    SemQuery semQuery = ctx.getSemQuery();
    
    assert outputConstraintNode != null;
    assert semQuery != null;
    
    // Obtain string of output constraint attribute from parse tree node
    String[] outputConstraintAttrs = outputConstraintNode.getColumns();
    String[] modifiedConstraintAttrs = null;
    try{
    // Check that No Duplicate Attributes inside Attribute List
    checkDuplicates(outputConstraintAttrs);
    
    // Check whether Constraint Column Exists
    modifiedConstraintAttrs = 
      checkIsConstraintColumnsExist(outputConstraintAttrs, ctx);
    }catch(CEPException ce)
    {
      ce.setStartOffset(outputConstraintNode.getStartOffset());
      ce.setEndOffset(outputConstraintNode.getEndOffset());
      throw ce;
    }
    
    semQuery.setIsPrimaryKeyExists(true);
    semQuery.setOutputConstraintAttrs(modifiedConstraintAttrs);
    
  }
  
  /**
   * Check If Any Duplicate column name in primary key attributes
   * @param columns
   * @throws MetadataException
   */
  public void checkDuplicates(String[] columns)
    throws CEPException
  {
    LinkedHashSet<String> tempListSet = new LinkedHashSet<String>();
    for(int i = 0; i < columns.length; i++)
      tempListSet.add(columns[i]);
    if(tempListSet.size() < columns.length)
      throw new SemanticException(SemanticError.DUPLICATE_COLUMN_NAME);
  }
  
  public String[] getSelectListExprs(SemQuery semQuery)
  {
    ArrayList<Expr> projExprs = semQuery.getSelectListExprs();
    int numProjExprs = semQuery.getSelectListSize();

    String[] names = new String[numProjExprs];

    for(int i = 0; i < numProjExprs; i++)
    {
      names[i] = projExprs.get(i).getName();
    }
    
    return names;
  }
  
  
  public String[] checkIsConstraintColumnsExist(String[] outputConstraintAttrs, 
                                            SemContext ctx)
  throws CEPException
  {
    String             constraintColumnName;
    String             queryColumnName;
    String[]           names;
    LinkedList<String> modifiedConstraintAttrs;
    
    names                   = this.getSelectListExprs(ctx.getSemQuery());
    modifiedConstraintAttrs = new LinkedList<String>();
    
    boolean found  = false;
    int j;
    
    for(int i =0 ; i < outputConstraintAttrs.length; i++)
    {
      found = false;
      constraintColumnName = getColumnName(outputConstraintAttrs[i]);
      for(j = 0; j < names.length; j++)
      {
        queryColumnName = getColumnName(names[j]);
        if(constraintColumnName.equals(queryColumnName))
        {
          found = true;
          break;
        }
      }
      
      if(found)
        modifiedConstraintAttrs.add(names[j]);
      else
        throw new SemanticException(SemanticError.UNKNOWN_ATTR_ERROR, 
                               new Object[]{constraintColumnName});
    }
    
    return modifiedConstraintAttrs.toArray(new String[0]);
    
  }
  
  public String getColumnName(String colName)
  {
    int dotPos = colName.lastIndexOf('.');
    if(dotPos == -1)
      return colName;
    else
      return colName.substring(dotPos + 1);
  }
}
