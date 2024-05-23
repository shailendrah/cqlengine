/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseRelInterp.java /main/13 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    The interpreter for the CEPBaseRelationNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/18/14 - adding support for isDependentOnPartnStrm
    vikshukl    07/30/12 - archived dimension relation
    udeshmuk    06/05/12 - set isArchived even if FROM clause has view
    udeshmuk    03/21/11 - Checking and propagating isarchived flag
    parujain    05/19/09 - support outer join
    parujain    03/12/09 - make interpreters stateless
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    08/27/08 - semantic offset
    mthatte     04/14/08 - bug
    parujain    03/10/08 - derived timestamp
    anasrini    05/23/07 - symbol table reorg
    najain      05/16/06 - support views 
    najain      04/06/06 - cleanup
    anasrini    02/24/06 - need not set tableId in context 
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseRelInterp.java /main/13 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Table;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPBaseRelationNode;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * The interpreter that is specific to the CEPBaseRelationNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class BaseRelInterp extends NodeInterpreter {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPBaseRelationNode relNode;
    String              relName;
    String              alias;
    int                 objId;
    NodeInterpreter     derivedTsInterp;
    DerivedTimeSpec     dspec = null;
    int                 varId;
    
    assert node instanceof CEPBaseRelationNode;
    relNode = (CEPBaseRelationNode)node;
    ExecContext ec = ctx.getExecContext();
    
    super.interpretNode(node, ctx);

    relName = relNode.getName();
    
    try{
      objId   = ec.getSourceMgr().getId(relName, ctx.getSchema());
      alias   = relNode.getAlias();
      if (alias == null)
        alias = relName;

      // register this name in the Symbol Table
      try 
      {
        varId = ctx.getSymbolTable().addPersistentSourceEntry(ec, relName, alias, ctx.getSchema());
      }
      catch(CEPException ce)
      {
        ce.setStartOffset(relNode.getStartOffset());
        ce.setEndOffset(relNode.getEndOffset());
        throw ce;
      }

      // register this as a referenced table in the query
       ctx.getSemQuery().addReferencedTable(objId);
    
    
     //If this is a source relation then check if derived ts.
    
      if (ec.getSourceMgr().isTableObject(objId))
      {
        Table table = ec.getTableMgr().getTable(objId);
        if (table.isDerivedTs())
        {
          derivedTsInterp = InterpreterFactory.getInterpreter(table
              .getDerivedTsExpr());
          derivedTsInterp.interpretNode(table.getDerivedTsExpr(), ctx);
          dspec = new DerivedTimeSpec(objId, ctx.getExpr());
        }

        ctx.setDerivedTimeSpec(dspec);
      }
      
      // check if source is archived
      if (ec.getSourceMgr().isArchived(objId))
      {
        ctx.setIsArchived(true);
        ctx.setIsDimension(ec.getSourceMgr().isDimension(objId));
      }
      else
      {
        ctx.setIsArchived(false);
        ctx.setIsDimension(false);
      }

      // Setting the context whether the source is a partitioned
      ctx.setIsPartnStream(ec.getSourceMgr().isPartnStream(objId));
      
      // Construct one RelationSpec for this relation only if this relation
      // node comes under an OuterJoinRelationNode
      // Otherwise SFWQueryInterp is responsible to create a relSpec
      // for this relation
      if(ctx.isOuterJoinTypeNode())
      {
        RelationSpec relSpec = new RelationSpec(varId);
        ctx.setRelationSpec(relSpec);
      }
      
      // Check if this is a RELATION ?
      if (ec.getSourceMgr().isStream(objId))
        throw new CEPException(SemanticError.NOT_A_RELATION_ERROR, relNode.getStartOffset(),
                               relNode.getEndOffset(), new Object[] {relName});
    }
    catch(MetadataException me)
    {
      me.setStartOffset(relNode.getStartOffset());
      me.setEndOffset(relNode.getEndOffset());
      throw me;
    }
  }
}
