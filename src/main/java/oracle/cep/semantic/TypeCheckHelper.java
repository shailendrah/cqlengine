/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TypeCheckHelper.java /main/29 2013/02/25 04:12:40 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/21/13 - bug16188514
    parujain    02/16/09 - fix offset
    parujain    01/28/09 - transaction mgmt
    sbishnoi    01/15/09 - fix isExternal flag
    hopark      12/03/08 - keep the installer in execcontext
    hopark      11/06/08 - lazy seeding
    hopark      11/03/08 - fix schema
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/05/08 - support offset
    parujain    08/26/08 - semantic exception offset
    udeshmuk    03/12/08 - remove returntype in staticmetadata.
    udeshmuk    02/14/08 - support for all nulls in function arguments
    udeshmuk    01/30/08 - support for double data type.
    udeshmuk    01/11/08 - handle NULL arguments.
    rkomurav    05/28/07 - restructure funcaggr
    rkomurav    11/29/06 - fix aggrallowd
    parujain    11/21/06 - Type conversion overloading
    dlenkov     11/09/06 - 
    parujain    11/06/06 - remove tabs
    parujain    10/12/06 - verify signature
    parujain    10/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TypeCheckHelper.java /main/29 2013/02/25 04:12:40 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IFieldMetadata;
import oracle.cep.extensibility.type.IMethodMetadata;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.extensibility.type.IType.Kind;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.ObjectId;
import oracle.cep.metadata.SynthesizedFunctionMetadata;
import oracle.cep.metadata.UserFunction;
import oracle.cep.metadata.UserFunctionManager;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPIntTokenNode;
import oracle.cep.parser.CEPStringTokenNode;
import oracle.cep.parser.CartridgeHelper;
import oracle.cep.service.ExecContext;

public class TypeCheckHelper {

  private static TypeCheckHelper helper;

  static{
    helper = new TypeCheckHelper();
  }

  public static TypeCheckHelper getTypeCheckHelper()
  {
    return helper;
  }
  
  // This method is for internal type conversions by getting functions like to_timestamp
  private UserFunction getFunction(UserFunctionManager ufnmgr, 
      String funcName, Datatype[] dt, 
		  String schema) throws CEPException
  {
      String name = UserFunctionManager.getUniqueFunctionName(funcName, dt);
      return ufnmgr.getFunction(name, schema);
  }
  
  public ValidFunc validateObjExpr(
      CEPStringTokenNode memberNameNode,
      CEPIntTokenNode indexNode,
      CEPExprNode lvalueNode, 
      CEPExprNode[] paramNodes, 
      SemContext ctx, 
      boolean isChildAggrAllowed, 
      CEPStringTokenNode linkNode,
      boolean isSynonym) throws CEPException {
    
    NodeInterpreter interp = null;
    
    // Member name
    String memberName = (memberNameNode != null) ? memberNameNode.getValue() : null;
    
    // Get lvalue expression and dt
    Expr lvalue = null;
    IType targetType = null;
    
    if (lvalueNode != null)
    {
      interp = 
        InterpreterFactory.getInterpreter(lvalueNode);
      interp.interpretNode(lvalueNode, ctx);
      lvalue = ctx.getExpr();
      targetType = lvalue.getReturnType();
    } 
    else 
    {
      // It must be a static method or constructor
      assert memberNameNode != null && indexNode == null;
      
      // REVIEW we should do this at the parser, seems a bit messy to do it here...
      
      // By default assumes Java type system if linkNode is null
      ITypeLocator typeLocator = 
          CartridgeHelper.findTypeLocator(ctx.getExecContext(), linkNode);
      
      String typeName = memberNameNode.getValue();
      
      if (LogUtil.isFinerEnabled(LoggerType.TRACE))
        LogUtil.finer(LoggerType.TRACE, "locate type = " + typeName);
        
      ICartridgeContext context = 
        CartridgeHelper.createCartridgeContext(ctx.getExecContext());
        
      try
      {
        // First assume it is a constructor.
        IType type = 
          typeLocator.getType(typeName, context);
        
        if (!(type instanceof IComplexType)) 
        {
          throw new MetadataException(MetadataError.TYPE_NOT_OF_COMPLEX_TYPE, 
              memberNameNode.getStartOffset(), memberNameNode.getEndOffset(), 
              new String[]{type.name()}); 
        }
        
        targetType = 
          (IComplexType) type; 
        
        memberName = null;
        
      } catch (CartridgeException e)
      {
        // If we are looking for synonym case then we only allow types for synonyms
    	  // so no need to look for static methods
        if(!isSynonym)
        {
          // Try as a static method/field then...
          int lastDot =
            typeName.lastIndexOf('.');
        
          if (lastDot != -1) 
          {
            memberName = typeName.substring(lastDot + 1); 
            typeName = typeName.substring(0, lastDot);
           
            if (LogUtil.isFinerEnabled(LoggerType.TRACE))
              LogUtil.finer(LoggerType.TRACE, "locate type = " + typeName);
          
            try
            {
              IType type = 
                typeLocator.getType(typeName, context);
            
              if (!(type instanceof IComplexType)) 
              {
                throw new MetadataException(MetadataError.TYPE_NOT_OF_COMPLEX_TYPE, 
                    memberNameNode.getStartOffset(), memberNameNode.getEndOffset(), 
                    new String[]{type.name()});
              }
            
              targetType = 
                (IComplexType) type; 
          
            } catch (CartridgeException e1)
            {
              throw new MetadataException(MetadataError.TYPE_FOR_STATIC_REF_NOT_FOUND,
                  memberNameNode.getStartOffset(), memberNameNode.getEndOffset(),
                  new String[] {memberNameNode.getValue() , typeName});
            }
          }
          else
          {
            if (e instanceof AmbiguousMetadataException) 
            {
              throw new MetadataException(MetadataError.AMBIGUOUS_TYPE,
                  memberNameNode.getStartOffset(), 
                  memberNameNode.getEndOffset(),
                  new String[] {((AmbiguousMetadataException) e).getMetadataName(), e.getMessage()});
            }
            else // MetadataNotFoundException
            { 
              // There is only a single identifier, hence it should have been a constructor under the default name-space
              throw new MetadataException(MetadataError.CONSTRUCTOR_NOT_FOUND,
                  memberNameNode.getStartOffset(), memberNameNode.getEndOffset(),
                  new String[] {typeName, typeName});
            }
          }
        }
        else
        {
          if (e instanceof AmbiguousMetadataException) 
          {
            throw new MetadataException(MetadataError.AMBIGUOUS_TYPE,
                memberNameNode.getStartOffset(), 
                memberNameNode.getEndOffset(),
                new String[] {((AmbiguousMetadataException) e).getMetadataName(), e.getMessage()});
          }
          else // MetadataNotFoundException
          {
            throw new MetadataException(MetadataError.CONSTRUCTOR_NOT_FOUND,
                      memberNameNode.getStartOffset(), memberNameNode.getEndOffset(),
                      new String[] {typeName, typeName});
          }
        }
      }
    }
    
    // Get param expression and dt
    Expr[] params = null;
    Datatype[] paramTypes = null;

    boolean isAggrAllowed = ctx.isAggrAllowed();
    ctx.setIsAggrAllowed(isChildAggrAllowed);
    if (paramNodes != null) 
    {
      params = new Expr[paramNodes.length];
      paramTypes = new Datatype[paramNodes.length];
      for( int i = 0; i < paramNodes.length; i++) {
        interp = 
          InterpreterFactory.getInterpreter( paramNodes[i]);
        interp.interpretNode(paramNodes[i], ctx);
        params[i] = ctx.getExpr();
        paramTypes[i] = params[i].getReturnType();
      }
    }
    ctx.setIsAggrAllowed(isAggrAllowed);
    
    // After type-checking, we need to convert an object access to a function call.
    // We do this by setting the 'this' pointer to as the first parameter. 
    // In the case of a static method or constructor, this is null.
    IUserFunctionMetadata synthesizedFuncMetadata = null;
    try
    {
      if (indexNode != null) 
      {
        // Array access
        if (!(targetType instanceof IArrayType)) 
        {
          throw new MetadataException(MetadataError.NOT_OF_ARRAY_TYPE,
              indexNode.getStartOffset(), indexNode.getEndOffset(),
              new String[] {targetType.name()});
        }
        
        synthesizedFuncMetadata =
          new SynthesizedFunctionMetadata((IArrayType) targetType, indexNode.getValue());
      }
      else {
        // Type Check: 'this' must be an object
        if (!(targetType instanceof IComplexType)) 
        {
          // REVIEW
          // CHAR is a special data-type as it can safely be treated as 
          //  the Java String type without loss of semantic.
          //
          if (targetType == Datatype.CHAR) 
          {
            ITypeLocator typeLocator = 
              CartridgeHelper.findTypeLocator(ctx.getExecContext(), (String) null);
            
            ICartridgeContext context = 
              CartridgeHelper.createCartridgeContext(ctx.getExecContext());
            
            try
            {
              targetType = 
                (Datatype) typeLocator.getType(Datatype.CHAR.getImplementationType().getName(), context);
            } catch (CartridgeException e)
            {
              assert false : "should never happen: " + e;
            }
          }
          else
            throw new MetadataException(MetadataError.TYPE_NOT_OF_COMPLEX_TYPE, 
                lvalueNode.getStartOffset(), lvalueNode.getEndOffset(), 
                new String[]{targetType.name()});
        }
        
        IComplexType complexType = 
          (IComplexType) targetType;
      
        if (memberName == null) 
        {
          // If no member name is present, then it must be a constructor.
          synthesizedFuncMetadata = 
            new SynthesizedFunctionMetadata(complexType.getConstructor(paramTypes));
        }
        else
        {
          // If params is null (which is different than empty list), then it must be a field
          if (paramTypes == null)
          {
            IFieldMetadata field = 
              complexType.getField(memberName);
            
            if (!field.hasGet())
            {
              throw new MetadataException(MetadataError.MISSING_GET_ACCESSOR_FOR_PROPERTY,
                  memberNameNode.getStartOffset(), memberNameNode.getEndOffset(),
                  new String[] {complexType.name(), memberName});
            } 
            else
            {
              synthesizedFuncMetadata = 
                new SynthesizedFunctionMetadata(field);
            }
          }
          else
          {
            // If param list exist, then it must be a method, static or otherwise.
            IMethodMetadata methodMetadata =
              complexType.getMethod(memberName, paramTypes);
            
            // Note that method resolution should not take into account if it is static.
            // The check for static should be done after the most specific method is found (JLS)
            if (lvalue == null && !methodMetadata.isStatic()) 
            {
              throw new MetadataException(MetadataError.METHOD_NOT_STATIC,
                  memberNameNode.getStartOffset(), memberNameNode.getEndOffset(),
                  new String[] {complexType.name(), memberName});
            }
            else 
              synthesizedFuncMetadata = 
                new SynthesizedFunctionMetadata(methodMetadata);
          }
        }
      }
    } catch (MetadataNotFoundException e)
    {
      // By default assume couldn't find field.
      MetadataError error = MetadataError.FIELD_NOT_FOUND;
      
      if (memberName == null) {
        error = MetadataError.CONSTRUCTOR_NOT_FOUND;
      } else if (params != null) {
        error = MetadataError.METHOD_NOT_FOUND;
      }
      
      String underlyingCause = "";
      if (e.getCause() != null)
        underlyingCause = e.getCause().getMessage();
        
      throw new MetadataException(error,
          memberNameNode.getStartOffset(), memberNameNode.getEndOffset(),
          new String[] {targetType.name(), e.getMetadataName(), underlyingCause});
    }

    // REVIEW I don't think we need to do any datatype conversion here, as they are better
    //  served to be done in the cartridge function/method itself.
    
    // These are the cases;
    //  - lvalue with no params (e.g. field)
    //  - lvalue with params (e.g. method)
    //  - no lvalue with no params (e.g. static field)
    //  - no lvalue with params (e.g. constructor, static method)
    
    Expr[] funcParams = params;
    if (lvalue != null) 
    {
      funcParams = new Expr[(params != null ? paramNodes.length : 0) + 1];
      funcParams[0] = lvalue;
      if (params != null) 
        for (int i = 0; i < params.length; i++) 
          funcParams[i+1] = params[i];
    }
    
    return new ValidFunc(synthesizedFuncMetadata, funcParams);
  }
   

  // Array of Expressions is for flexibility for future.
  // This will allow any function to have any number of parameters.
  
  public ValidFunc validateExpr(String funcName, CEPExprNode[] tnodes,
      SemContext ctx, boolean isChildAggrAllowed) 
  throws CEPException {
    return validateExpr(funcName, tnodes, ctx, isChildAggrAllowed, null);
  }

  public ValidFunc validateExpr(String funcName, CEPExprNode[] tnodes,
				 SemContext ctx, boolean isChildAggrAllowed, CEPStringTokenNode linkNode)
      throws CEPException {

    ExecContext ec = ctx.getExecContext();
    Expr[] params = new Expr[tnodes.length];
    Datatype[] dts = new Datatype[tnodes.length];
    
    boolean isAggrAllowed = ctx.isAggrAllowed();
    ctx.setIsAggrAllowed(isChildAggrAllowed);
    for( int i = 0; i < tnodes.length; i++) {
      NodeInterpreter interp = InterpreterFactory.getInterpreter( tnodes[i]);
      interp.interpretNode( tnodes[i], ctx);
      params[i] = ctx.getExpr();
      dts[i] = params[i].getReturnType();
    }
    ctx.setIsAggrAllowed( isAggrAllowed);
    
    // Check if all inputs to a function are null
    boolean allInputsNull = true;
    for (int i=0; i < dts.length; i++)
    {
      if (dts[i] != Datatype.UNKNOWN) 
      {
        allInputsNull = false;
        break;
      }
    }
   
    UserFunctionManager builtinUserFnMgr = ec.getUserFnMgr();
    IUserFunctionMetadata fn = null;
    boolean isResultNull = false;
    
    if (linkNode != null)
    {
      fn = CartridgeHelper.findFunctionMetadata(ec, linkNode, 
          funcName, dts);
    }
    else 
    {
      // If no extended type is involved, then rely on the (default) built-in function
      //  manager.
      // The function manager does the additional tasks over just the locator:
      //  - dependency manager between functions and queries
      //  - handles aggregation functions
      //  - handles all null case
      //
      try{
        fn = builtinUserFnMgr.getValidFunction(funcName, dts, ctx.getSchema(),
            isAggrAllowed, allInputsNull);
      }
      catch(CEPException e)
      {
        if(e.getErrorCode() == MetadataError.FUNCTION_NOT_FOUND)
        { 
          // Look if it is a built-in function, however first check if any of the arguments 
          //  should be casted to a native type.
          for (int i=0; i < dts.length; i++)
          {
            if (dts[i].getKind() == Kind.OBJECT)
            {
              Expr expr = buildExtensibleOperatorExpression(ec, "to_cql", dts[i], params[i]);
              if (expr != null) 
              {
                params[i] = expr;
                dts[i] = expr.getReturnType();
              } else {
                // Let it go through and fail when searching for the function. 
                //  This provides us with a better error message.
              }
            }
          }
          
          try{
            fn = builtinUserFnMgr.getValidFunction(funcName, dts, ec.getDefaultSchema(), 
                isAggrAllowed, allInputsNull);
          }catch(CEPException ce)
          {
            fn = null;
            boolean b = ec.getBuiltinFuncInstaller().installFuncs(ec, funcName, dts);
            if (!b)
              b = ec.getColtInstaller().installFuncs(ec, funcName, dts);
            if (!b)
              b = ec.getColtAggrInstaller().installFuncs(ec, funcName, dts);
            if (b)
            {
              try{
                fn = builtinUserFnMgr.getValidFunction(funcName, dts, ec.getDefaultSchema(), 
                    isAggrAllowed, allInputsNull);
              }catch(CEPException ce1)
              {
                LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ce1);
                ce = ce1;
                //eat up the exception in lazy seeding and use the original exception
                //for the message..
              }
            }
            if (fn == null)
            {
              if(tnodes.length > 0)
              {
                ce.setStartOffset(tnodes[0].getStartOffset());
                ce.setEndOffset(tnodes[tnodes.length-1].getEndOffset());
              }
              throw ce;
            }    
          }
        }
        else
        {
          if(tnodes.length >= 1)
          {
            e.setStartOffset(tnodes[0].getStartOffset());
            e.setEndOffset(tnodes[tnodes.length-1].getEndOffset());
          }
          throw e;
        }
      }

      if (allInputsNull)
      {
        isResultNull = builtinUserFnMgr.getIsResultNull(funcName, params.length);
      }
    }
    
    for (int i = 0; i < tnodes.length; i++) {

      // type checking of the parameters
      Datatype todt = fn.getParam(i).getType();
      Datatype fromdt = dts[i];

      if (fromdt != todt) 
      {
        if (fromdt == Datatype.UNKNOWN)
        {
          params[i] = Expr.getExpectedExpr(todt);
        }
        else
        {
          String name = TypeConverter.getTypeConverter().TransOp(fromdt, todt);
          // null will be returned in the case where conversion is not possible
          if(name != null)
          {
            try{
              Datatype[] types = new Datatype[1];
              types[0] = fromdt;
              // Here we should use the default schema name as this is default schema
              UserFunction func = getFunction(builtinUserFnMgr, name, types, ec.getDefaultSchema());
              if (func == null)
              {
                boolean b = ec.getBuiltinFuncInstaller().installFuncs(ec, name, types);
                if (!b)
                  b = ec.getColtInstaller().installFuncs(ec, name, types);
                if (!b)
                  b = ec.getColtAggrInstaller().installFuncs(ec, name, types);
                if (b)
                {
                  func = getFunction(builtinUserFnMgr, name, types, ec.getDefaultSchema());
                }
              }
              Expr[] expr = new Expr[1];
              expr[0] = params[i];
              Expr fexpr = new FuncExpr(func.getId(), expr, func.getReturnType());
              String fullName = UserFunctionManager.getUniqueFunctionName(name, types);
              fexpr.setName(fullName, false, expr[0].isExternal);
              params[i] = fexpr;
              dts[i] = todt;

            } catch(CEPException ce)
            {
              ce.setStartOffset(tnodes[i].getStartOffset());
              ce.setEndOffset(tnodes[i].getEndOffset());
              throw ce;
            }
          }
        }
      }
    }

    ValidFunc func = new ValidFunc(fn, params, isResultNull);
    return func;
  }

  public Expr buildExtensibleOperatorExpression(ExecContext ec, String name, Datatype fromdt, Expr param) 
    throws CEPException
  {
    String linkName = CartridgeHelper.getCartridgeNameFromType(fromdt);
  
    if (linkName == null)
      return null;
    
    Datatype[] types = new Datatype[] {fromdt};
    IUserFunctionMetadata extendedFunc = null;
    
    try 
    {
      extendedFunc = CartridgeHelper.findFunctionMetadata(ec, 
          new CEPStringTokenNode(linkName), name, types);
    } catch (CEPException e) {
      // Just return null if no extensible function has been found, an exception should be throw
      //  at a higher level.
      return null;
    }
              
    Expr[] expr = new Expr[] { param };
    
    // This function is owned and managed by an external system (i.e. cartridge),
    //  hence we don't have an ID from our cache. However, we would still like to
    //  associate an ID so that is easier to identify the function. Therefore
    //  we will rely on ObjectId, as our common ID factory.
    int id = ObjectId.getNextId();
    FuncExpr fexpr = new FuncExpr(id, expr, extendedFunc.getReturnType(), name, linkName);
    
    String fullName = UserFunctionManager.getUniqueFunctionName(name, types);
    fexpr.setName(fullName, false, expr[0].isExternal);
    
    if (extendedFunc instanceof ISimpleFunctionMetadata) 
    {
      ISimpleFunctionMetadata sfm = 
        (ISimpleFunctionMetadata) extendedFunc;
      
      // INVARIANT: built-in functions do not have a impl class, hence the reason
      //  code-gen checks for them later, so that it can set its own code.
      fexpr.setFuncImpl(sfm.getImplClass());
    }
    
    return fexpr;
  }

}
