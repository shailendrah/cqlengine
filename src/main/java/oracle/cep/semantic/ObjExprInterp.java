package oracle.cep.semantic;

import java.util.Arrays;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.ObjectId;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPFunctionExprNode;
import oracle.cep.parser.CEPObjExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPStringTokenNode;
import static oracle.cep.common.Constants.CQL_THIS_POINTER;

public class ObjExprInterp extends NodeInterpreter
{
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {

    assert node instanceof CEPObjExprNode;
    CEPObjExprNode objExprNode = (CEPObjExprNode) node;

    super.interpretNode(node, ctx);
    
    // Cases:
    // 1) attribute
    // 2) engine-managed function
    // 3) cartridge-managed function
    // 4) variable.attribute
    // 5) attribute.member
    // 6) constructor/static method including type synonym usage
    //
    // The last three are ambiguous and thus have the precedence as ordered
    //  (e.g. variable.attribute has higher precedence over static method)
    //
    //
    
    ValidFunc vfn = null; 
    
    if (objExprNode.getLValue() != null) 
    {
      // case (5)
      vfn = 
        TypeCheckHelper.getTypeCheckHelper().validateObjExpr(
            objExprNode.getMemberName(),
            objExprNode.getIndex(),
            objExprNode.getLValue(),
            objExprNode.getParams(), ctx, true, null, false);
    } 
    else 
    {
      List<CEPStringTokenNode> qualifiedName = objExprNode.getQualifiedName();
      
      // Check if there is a single identifier, or two identifiers where the 2nd one is a link.
      if ((qualifiedName.size() == 1) 
          || (qualifiedName.size() == 2 && qualifiedName.get(1).isLink())) 
      {
        if (objExprNode.getParams() == null) 
        {
          // (1) plain attribute (no relation/stream name prefix name)
          CEPAttrNode attrNode = new CEPAttrNode(qualifiedName.get(0));
          
          NodeInterpreter interp = 
            InterpreterFactory.getInterpreter(attrNode);
          
          try 
          {
            interp.interpretNode(attrNode, ctx);
          }
          catch (CEPException e1)
          {
            // Prefix plain attribute with the _this pointer, as it may be a event property that will get
            //  converted to a method call following the JavaBeans convention.
            
            CEPExprNode newObjExpr =
              new CEPObjExprNode(buildAttrNodeForThisPointer(
                  qualifiedName.get(0).getStartOffset(),
                  qualifiedName.get(0).getEndOffset()), 
                  qualifiedName.get(0), objExprNode.getParams());

            interp = 
              InterpreterFactory.getInterpreter(newObjExpr);
            
            try 
            {
              interp.interpretNode(newObjExpr, ctx);
            }
            catch (CEPException ignore)
            {
              // We don't want to raise an exception informing that '_this' attribute was not found, 
              //  as this is internal to the engine. Instead, raise previous exception.
              throw e1;
            }
          }
          
          return;
        }
        else 
        {
          // (2) function, or constructor of a type under the default package for the default cartridge (i.e. Java cartridge).
          // Note that we allow the overriding between functions and constructors. In other words, you could have a
          //  function named F1 and constructor named F1 with the same or different number of arguments. If the same,
          // then the function is always selected in this situation.
          
          List<CEPExprNode> paramList = 
            Arrays.asList(objExprNode.getParams());
          
          CEPStringTokenNode nameToken = qualifiedName.get(0);
          
          CEPStringTokenNode linkToken = 
            (qualifiedName.size() == 2 ? qualifiedName.get(1) : null);
          
          CEPFunctionExprNode funcNode = 
            new CEPFunctionExprNode(nameToken, paramList, linkToken);
          
          try {
            
            // Check if it is a function
            NodeInterpreter interp = 
              InterpreterFactory.getInterpreter(funcNode);
            interp.interpretNode(funcNode, ctx);

            return;
          }
          catch (CEPException e1) 
          {
            // If the problem is that the cartridge link is not found, then there is 
            //  nothing else to try. Instead if the function name was the symbol not found, 
            //  then try it as constructor.
            if (e1.getErrorCode() == MetadataError.CARTRIDGE_NOT_FOUND) 
            {
              if (LogUtil.isFineEnabled(LoggerType.TRACE))
              {
                LogUtil.fine(LoggerType.TRACE, e1.getMessage());
              }
              
              throw e1;
            }
            else 
            {
              try {
                
                  // Check if it is a method call to an implicit _this on a table.
                  // Note that a 'link' should not be present in this case, as it is an instance method.
                  if (qualifiedName.size() == 1)
                  {
                    CEPExprNode newObjExpr =
                      new CEPObjExprNode(buildAttrNodeForThisPointer(
                          qualifiedName.get(0).getStartOffset(),
                          qualifiedName.get(0).getEndOffset()), 
                          qualifiedName.get(0), objExprNode.getParams());

                    NodeInterpreter interp = 
                      InterpreterFactory.getInterpreter(newObjExpr);
                    
                    try {
                      interp.interpretNode(newObjExpr, ctx);
                    } catch (CEPException ignore) {
                      // Check if it is a constructor under the default namespace.
                      vfn = 
                        TypeCheckHelper.getTypeCheckHelper().validateObjExpr(nameToken, null,
                            null, objExprNode.getParams(), ctx, true, linkToken, false);
                    }
                  } else {
                    // Check if it is a constructor under the default namespace.
                    vfn = 
                      TypeCheckHelper.getTypeCheckHelper().validateObjExpr(nameToken, null,
                          null, objExprNode.getParams(), ctx, true, linkToken, false);
                  }
              } 
              catch (CEPException e2) 
              {
                try
                {
                  //check if it is a synonym and if yes then replace using a synonym
                  CEPStringTokenNode memberNameNode = buildStringTokenNode(qualifiedName);
                  
                  String target = ctx.getExecContext().getSynonymMgr().
                                  getSynonymTypeTarget(memberNameNode.getValue(),
                                  		         ctx.getExecContext().getSchema());
                  if(target != null)
                  {
                    vfn = processSynonym(target, objExprNode.getParams(), ctx);
                  } 
                  else
                  {
                    String cause1 = e1.getCauseMessage();
                    String cause2 = e2.getCauseMessage();
                  
                    if (LogUtil.isWarningEnabled(LoggerType.TRACE)) 
                    {
                      LogUtil.warning(LoggerType.TRACE, 
                          "Invalid call to function or constructor: " + nameToken.getValue()
                          + ". Probable causes are: \"" + cause1 + "\"" +
                              ", or \"" + cause2);
                    }
  
                    throw new MetadataException(MetadataError.INVALID_CALL_TO_FUNCTION_OR_CONSTRUCTOR, 
                        nameToken.getStartOffset(), nameToken.getEndOffset(), 
                        new String[]{ nameToken.getValue(), cause1, cause2});
                  }
                }
                catch(CEPException e3)
                {
                  String cause1 = e1.getCauseMessage();
                  String cause2 = e2.getCauseMessage();
                  String cause3 = e3.getCauseMessage();
                
                  if (LogUtil.isWarningEnabled(LoggerType.TRACE)) 
                  {
                    LogUtil.warning(LoggerType.TRACE, 
                        "Invalid call to function or constructor: " + nameToken.getValue()
                        + ". Probable causes are: \"" + cause1 + "\"" +
                            ", or \"" + cause2 + "\"" + ", or \"" + cause3);
                  }
  
                  throw new MetadataException(MetadataError.INVALID_CALL_TO_FUNCTION_OR_CONSTRUCTOR_OR_SYNONYM, 
                      nameToken.getStartOffset(), nameToken.getEndOffset(), 
                      new String[]{ nameToken.getValue(), cause1, cause2, cause3});
                }
              }
            }
          }
        }
      }
      else if (hasLink(qualifiedName))
      {
        // (6) if a link is present, then assume it is either a constructor or static method/field
        //  as links are not allowed in instance methods, nor fields.
        CEPStringTokenNode memberNameNode = buildStringTokenNode(qualifiedName);

        vfn = 
          TypeCheckHelper.getTypeCheckHelper().validateObjExpr(memberNameNode, null,
              null, objExprNode.getParams(), ctx, true, 
              getLinkNode(qualifiedName), false);
      }
      else 
      {
        vfn = disambiguateName(qualifiedName, objExprNode, ctx);
      }
    }

    if (vfn != null)
      generateFuncExpr(vfn, ctx);
  }

  private CEPAttrNode buildAttrNodeForThisPointer(int startOffset, int endOffset)
  {
    CEPStringTokenNode thisTokenNode = new CEPStringTokenNode(CQL_THIS_POINTER);
    thisTokenNode.setStartOffset(startOffset);
    thisTokenNode.setEndOffset(endOffset);
    
    return new CEPAttrNode(thisTokenNode);
  }
  
  private CEPAttrNode buildAttrNodeWithVarAndThisPointer(CEPStringTokenNode varNode)
  {
    CEPStringTokenNode thisTokenNode = new CEPStringTokenNode(CQL_THIS_POINTER);
    thisTokenNode.setStartOffset(varNode.getStartOffset());
    thisTokenNode.setEndOffset(varNode.getEndOffset());
    
    return new CEPAttrNode(varNode, thisTokenNode);
  }

  private boolean hasLink(List<CEPStringTokenNode> qualifiedName)
  {
    return qualifiedName.get(qualifiedName.size()-1).isLink();
  }
  
  private ValidFunc processSynonym(String target, CEPExprNode[] params, 
	                               SemContext ctx)
  throws CEPException
  {
    ValidFunc vfn = null;
    
    boolean isLink = (target.indexOf('@') > -1) ? true : false;

    CEPStringTokenNode memberNameNode = null;
    CEPStringTokenNode linkNode = null;
    if(isLink)
    {
      memberNameNode = new CEPStringTokenNode(target.substring(0, target.indexOf('@')));
      memberNameNode.setEndOffset(target.indexOf('@'));
      linkNode = new CEPStringTokenNode(target.substring(target.indexOf('@')+1, target.length()));
      linkNode.setStartOffset(target.indexOf('@')+1);
      linkNode.setEndOffset(target.length());
      linkNode.setIsLink(true);
    }
    else
    {
      memberNameNode = new CEPStringTokenNode(target);
      memberNameNode.setEndOffset(target.length());
    }
    
    vfn = TypeCheckHelper.getTypeCheckHelper().validateObjExpr(memberNameNode,
                                               null, null, params, ctx, true,
                                               linkNode, true);
      
     return vfn;

  }

  private ValidFunc disambiguateName(List<CEPStringTokenNode> qualifiedName,
      CEPObjExprNode objExprNode, SemContext ctx)
      throws CEPException
  {
    assert qualifiedName.size() > 1;

    ValidFunc vfn = null;

    // Try case 4: variable.attribute...
    // This includes var.attr.field.field...field|method().
    
    CEPExprNode newObjExpr = 
      new CEPAttrNode(qualifiedName.get(0), qualifiedName.get(1));
    
    if (qualifiedName.size() > 2)
      newObjExpr = buildNestedObjExpr(newObjExpr, 
          qualifiedName.subList(2, qualifiedName.size()), 
          objExprNode.getParams());

    try
    {
      NodeInterpreter interp = 
        InterpreterFactory.getInterpreter(newObjExpr);
      interp.interpretNode(newObjExpr, ctx);
      
    } catch (CEPException e1) 
    {
      // Try case 4 again, however assuming the presence of an implicit _this pointer.
      // Note that we ignore its exception, as we don't want to show to the user that an implicit 'this'
      //  pointer exists.
      newObjExpr = 
        buildAttrNodeWithVarAndThisPointer(qualifiedName.get(0));
      
      if (qualifiedName.size() > 1)
        newObjExpr = buildNestedObjExpr(newObjExpr, 
            qualifiedName.subList(1, qualifiedName.size()), 
            objExprNode.getParams());
     
      try 
      {
        NodeInterpreter interp = 
          InterpreterFactory.getInterpreter(newObjExpr);
        interp.interpretNode(newObjExpr, ctx);
      } 
      catch (CEPException ignore1) 
      {
        // Case 4 failed, try case 5: attr.field.field...field|method()
        
        newObjExpr = 
          new CEPAttrNode(qualifiedName.get(0));
        
        newObjExpr = buildNestedObjExpr(newObjExpr, 
            qualifiedName.subList(1, qualifiedName.size()), 
            objExprNode.getParams());
        
        try 
        {
          NodeInterpreter interp = 
            InterpreterFactory.getInterpreter(newObjExpr);
          interp.interpretNode(newObjExpr, ctx);
        } 
        catch (CEPException e2) 
        {
          // Try case 5 again, now checking if there is a hidden _this pointer
          newObjExpr = 
            buildAttrNodeForThisPointer(
                qualifiedName.get(0).getStartOffset(),
                qualifiedName.get(0).getEndOffset());
          
          newObjExpr = buildNestedObjExpr(newObjExpr, 
              qualifiedName, objExprNode.getParams());
          
          try 
          {
            NodeInterpreter interp = 
              InterpreterFactory.getInterpreter(newObjExpr);
            interp.interpretNode(newObjExpr, ctx);
          } 
          catch (CEPException ignore2)
          {
            // Finally assume case 6
            try
            {
              CEPStringTokenNode memberNameNode = buildStringTokenNode(qualifiedName);
              
              vfn = TypeCheckHelper.getTypeCheckHelper().
              validateObjExpr(memberNameNode, null,
                  null, objExprNode.getParams(),
                  ctx, true, 
                  getLinkNode(qualifiedName), 
                  false);
            }
            catch(CEPException e3)
            { 
              //check if it is a synonym and if yes then replace using a synonym
              String target = ctx.getExecContext().getSynonymMgr().
              getSynonymTypeTarget(qualifiedName.get(0).getValue(),
                  ctx.getExecContext().getSchema());
              
              if(target != null)
              {
                try
                {
                  vfn = processSynonymStaticMethods(target, 
                      qualifiedName.subList(1, qualifiedName.size()), 
                      objExprNode.getParams(), ctx);
                }
                catch(CEPException e4)
                {
                  // All cases failed. However, which error message do we raise?
                  // We will have to provide all as probable causes.
                  String cause1 = e1.getCauseMessage();
                  String cause2 = e2.getCauseMessage();
                  String cause3 = e3.getCauseMessage();
                  String cause4 = e4.getCauseMessage();
                  
                  if (LogUtil.isWarningEnabled(LoggerType.TRACE)) 
                  {
                    LogUtil.warning(LoggerType.TRACE, 
                        "Invalid symbolic expression. Probable causes are: \"" + cause1 + "\"" +
                        ", or \"" + cause2 + "\", or \"" + cause3 + "\", or \"" + cause4+ "\"");
                  }
                  
                  throw new MetadataException(MetadataError.INVALID_SYMBOLIC_OR_SYNONYM_EXPRESSION, 
                      objExprNode.getStartOffset(), objExprNode.getEndOffset(), 
                      new String[]{ objExprNode.getExpression(), cause1, cause2, cause3, cause4});
                }
              }
              else
              {
                
                // All cases failed. However, which error message do we raise?
                // We will have to provide all as probable causes.
                String cause1 = e1.getCauseMessage();
                String cause2 = e2.getCauseMessage();
                String cause3 = e3.getCauseMessage();
                
                if (LogUtil.isWarningEnabled(LoggerType.TRACE)) 
                {
                  LogUtil.warning(LoggerType.TRACE, 
                      "Invalid symbolic expression. Probable causes are: \"" + cause1 + "\"" +
                      ", or \"" + cause2 + "\", or \"" + cause3 + "\"");
                }
                
                throw new MetadataException(MetadataError.INVALID_SYMBOLIC_EXPRESSION, 
                    objExprNode.getStartOffset(), objExprNode.getEndOffset(), 
                    new String[]{ objExprNode.getExpression(), cause1, cause2, cause3});
              }
            }
          }
        }
      }
    }
    
    return vfn;
  }

  private CEPStringTokenNode getLinkNode(List<CEPStringTokenNode> qualifiedName)
  {
    // INVARIANT must be last identifier in a qualified name
    
    CEPStringTokenNode lastNode = qualifiedName.get(qualifiedName.size() - 1);
    return lastNode.isLink() ? lastNode : null;
  }

  private CEPStringTokenNode buildStringTokenNode(
      List<CEPStringTokenNode> qualifiedName)
  {
    StringBuilder builder = new StringBuilder();
    for (CEPStringTokenNode name : qualifiedName) 
    {
      if (!name.isLink())
      {
        if (builder.length() != 0) 
          builder.append('.');
        builder.append(name.getValue());
      }
    }

    CEPStringTokenNode memberNameNode = new CEPStringTokenNode(builder.toString());
    memberNameNode.setStartOffset(qualifiedName.get(0).getStartOffset());
    memberNameNode.setEndOffset(qualifiedName.get(qualifiedName.size() - 1).
        getEndOffset());
    return memberNameNode;
  }
  
  private ValidFunc processSynonymStaticMethods(String target, 
          List<CEPStringTokenNode> qualifiedName, 
          CEPExprNode[] params, SemContext ctx)
  throws CEPException
  {
    ValidFunc vfn = null;
	    
	boolean isLink = (target.indexOf('@') > -1) ? true : false;

	CEPStringTokenNode memberNameNode = null;
	CEPStringTokenNode linkNode = null;
	String typeName = null;
	if(isLink)
	{
	  typeName = target.substring(0, target.indexOf('@'));
	  linkNode = new CEPStringTokenNode(target.substring(target.indexOf('@')+1, target.length()));
	  linkNode.setStartOffset(target.indexOf('@')+1);
	  linkNode.setEndOffset(target.length());
	  linkNode.setIsLink(true);
	}
	else
	{
	  typeName = target;
	}
	StringBuilder builder = new StringBuilder(typeName);
    for (CEPStringTokenNode name : qualifiedName) 
    {
       if (builder.length() != 0) 
          builder.append('.');
        builder.append(name.getValue());
    }

    memberNameNode = new CEPStringTokenNode(builder.toString());
    memberNameNode.setEndOffset(qualifiedName.get(qualifiedName.size() - 1).
        getEndOffset());
	    
    
    // since we have replace membername with the entire string it is no longer
    // like a synonym, and we want to allow static methods like String.valueOf
    // isSynonym flag will be false
	vfn = TypeCheckHelper.getTypeCheckHelper().validateObjExpr(memberNameNode,
	                                             null, null, params, ctx, true,
	                                             linkNode, false);
	      
	return vfn;
 
  }

  private CEPExprNode buildNestedObjExpr(CEPExprNode newObjExpr,
      List<CEPStringTokenNode> qualifiedName, CEPExprNode [] params)
  {
    for (int i = 0; i < qualifiedName.size(); i++)
    {
      // The qualified name is guaranteed to have no function calls "()" up to the last
      //  qualifier
      if (i == (qualifiedName.size() - 1))
      {
        newObjExpr = 
          new CEPObjExprNode(newObjExpr, qualifiedName.get(i),
            params);
      }
      else
        newObjExpr = new CEPObjExprNode(newObjExpr, qualifiedName.get(i));
    }
    return newObjExpr;
  }

  private void generateFuncExpr(ValidFunc vfn, SemContext ctx)
  {
    Expr[] params = vfn.getExprs();
    Expr expr = null;
    String fullName = null;
    boolean isExternal = false;
    
    if (params != null && params.length > 0)
    {
      isExternal = params[0].isExternal; // FIXME!
    }
    
    // REVIEW ignore the all params, return is null check.

    // This function is owned and managed by an external system (i.e. cartridge),
    //  hence we don't have an ID from our cache. However, we would still like to
    //  associate an ID so that is easier to identify the function. Therefore
    //  we will rely on ObjectId, as our common ID factory.
    int id = ObjectId.getNextId();
    
    expr = new FuncExpr(id, params, 
        vfn.getFn().getReturnType());

    if (vfn.getFn() instanceof ISimpleFunctionMetadata) 
    {
      ISimpleFunctionMetadata sfm = 
        (ISimpleFunctionMetadata) vfn.getFn();

      // INVARIANT: simple function retrieved for object access should never be
      //  a built-in function!
      ((FuncExpr) expr).setFuncImpl(sfm.getImplClass());
    }
      
    fullName = vfn.getFn().getName();
    
    // Check if we are dealing with a Java-Bean property target directly to a _this attribute.
    // If it is, then it should be similar to a function expression with a user-defined alias to an attribute name.
    boolean isJavaBeanProperty = isThisAttrExpr(params);
    
    expr.setName(fullName, isJavaBeanProperty);
    expr.setIsExternal(isExternal);
    ctx.setExpr(expr);
  }

  private boolean isThisAttrExpr(Expr[] params)
  {
    boolean isProperty = false;
    
    if (params != null && params.length == 1 && (params[0] instanceof AttrExpr))
    {
      AttrExpr attrExpr = (AttrExpr) params[0];
      String attrName = attrExpr.getName();
      
      // We may need to remove the varible name from the attribute name.
      int index = attrName.indexOf('.');
      if (index != -1)
        attrName = attrName.substring(index+1); // jump the '.' char
      
      if (attrName.equals(CQL_THIS_POINTER))
        isProperty = true;
    }
    
    return isProperty;
  }

}
