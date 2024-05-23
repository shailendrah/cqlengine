/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPRelationNode.java /main/3 2011/07/14 11:10:28 vikshukl Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    05/31/11 - subquery support
    parujain    06/02/09 - getter of outerjoin type node
    anasrini    12/20/05 - parse tree node for a relation 
    anasrini    12/20/05 - parse tree node for a relation 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPRelationNode.java /main/2 2009/06/04 17:45:05 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Base class for a relation parse tree node
 */

public abstract class CEPRelationNode extends CEPBaseEntityNode {
	
  public boolean isOuterJoinRelationNode()
  {
    return false;
  }
  public boolean isQueryRelationNode()
  {
    return false;
  }
}
