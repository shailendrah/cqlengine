/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseStreamInterp.java /main/15 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    The interpreter for the CEPBaseStreamNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set ispartitioned
    udeshmuk    04/25/13 - bug 16679693: set isarchived and dimension even if
                           this is not a table, it could be a view
    vikshukl    07/30/12 - archived dimension relation
    pkali       06/27/12 - setting BaseStreamSpec info
    sbishnoi    04/12/11 - support for archived relations
    parujain    05/19/09 - support outer join
    sborah      04/14/09 - use local scope for symbol table
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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseStreamInterp.java /main/15 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Table;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPBaseStreamNode;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * The interpreter that is specific to the CEPBaseStreamNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class BaseStreamInterp extends NodeInterpreter {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    String            streamName;
    String            alias;
    CEPBaseStreamNode streamNode;
    int               objId;
    NodeInterpreter   derivedTsInterp;
    DerivedTimeSpec   dspec = null;
    int               varId;
    
    assert node instanceof CEPBaseStreamNode;
    streamNode = (CEPBaseStreamNode)node;
    ExecContext ec = ctx.getExecContext();
    
    super.interpretNode(node, ctx);

    
    streamName = streamNode.getName();
    
    try
    {
      objId      = ec.getSourceMgr().getId(streamName, ctx.getSchema());
      alias      = streamNode.getAlias();
      if (alias == null)
        alias = streamName;

      // register this name in the Symbol Table
      try
      {
        varId = ctx.getSymbolTable().addPersistentSourceEntry(ec, streamName,
                                                              alias,
                                                              ctx.getSchema());
      }
      catch(CEPException ce)
      {
        ce.setStartOffset(streamNode.getStartOffset());
        ce.setEndOffset(streamNode.getEndOffset());
        throw ce;
      }

      // register this as a referenced table in the query
      ctx.getSemQuery().addReferencedTable(objId);
    
      ctx.setBaseStreamSpec(new BaseStreamSpec(varId));
      
      //If this is a source relation then check if derived ts.
      if (ec.getSourceMgr().isTableObject(objId))
      {
        Table table = ec.getTableMgr().getTable(objId);
        if (table.isDerivedTs())
        {
          SymbolTable oldSymbolTable = ctx.getSymbolTable();

          // make sure that the local varId is same as global varId 
          // Otherwise, the attribute will not match.
          SymbolTable newSymbolTable = new SymbolTable(varId);
          ctx.setSymbolTable(newSymbolTable);
          try
          {
            newSymbolTable.addPersistentSourceEntry(ec, streamName,
                                                    alias,
                                                    ctx.getSchema());
          }
          catch(CEPException ce)
          {
            ce.setStartOffset(streamNode.getStartOffset());
            ce.setEndOffset(streamNode.getEndOffset());
            // Now restore the old symbol table
            ctx.setSymbolTable(oldSymbolTable);
            ctx.getSemQuery().symTable = oldSymbolTable;
            throw ce;
          }

          derivedTsInterp = InterpreterFactory.getInterpreter(table
                             .getDerivedTsExpr());
          derivedTsInterp.interpretNode(table.getDerivedTsExpr(), ctx);
          dspec = new DerivedTimeSpec(objId, ctx.getExpr());

          // Now restore the old symbol table
          ctx.setSymbolTable(oldSymbolTable);
          ctx.getSemQuery().symTable = oldSymbolTable;
        }
        
        /* set derived timestamp */
        ctx.setDerivedTimeSpec(dspec);
      }
    
      if(ec.getSourceMgr().isPartnStream(objId))
        ctx.setIsPartnStream(true); 
      /* archived relation specific stuff */
      if(ec.getSourceMgr().isArchived(objId))
        ctx.setIsArchived(true);
      else
        ctx.setIsArchived(false);
      
      if (ec.getSourceMgr().isDimension(objId))
        ctx.setIsDimension(true);
      else
        ctx.setIsDimension(false);
      
      // Construct one RelationSpec for this stream only if this stream
      // node comes under an OuterJoinRelationNode
      // Otherwise SFWQueryInterp is responsible to create a relSpec
      // for this stream
      if(ctx.isOuterJoinTypeNode())
      {
        RelationSpec relSpec = new RelationSpec(varId);
        ctx.setRelationSpec(relSpec);
      }
      
      // Check if this is a STREAM ?
      if (!(ec.getSourceMgr().isStream(objId)))
        throw new SemanticException(SemanticError.NOT_A_STREAM_ERROR, 
                                    streamNode.getStartOffset(),
                                    streamNode.getEndOffset(),
                                    new Object[] {streamName});
      
      
    }
    catch(MetadataException me)
    {
        me.setStartOffset(streamNode.getStartOffset());
        me.setEndOffset(streamNode.getEndOffset());
        throw me;
    }
  }
}
