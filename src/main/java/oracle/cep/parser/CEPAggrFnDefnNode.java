/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAggrFnDefnNode.java /main/14 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
        Parse tree node corresponding to DDL for a user defined 
        aggregation function

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sborah      07/10/09 - support for bigdecimal
    hopark      02/17/09 - support boolean as external datatype
    hopark      02/06/09 - objtype support
    alealves    02/02/09 - add support for function instance in addition to class
    hopark      11/11/08 - use getFuncName insteadd of AggrFunc.name
    parujain    08/21/08 - throw semantic error
    parujain    08/13/08 - error offset
    udeshmuk    02/04/08 - parameterize errors.
    udeshmuk    10/11/07 - Allow timestamp, interval, char, byte as argument
                           and return type for aggr functions.
    anasrini    06/25/07 - fix link warning
    hopark      06/25/07 - fix unchecked warning
    sbishnoi    06/01/07 - support for multiple arguments
    hopark      11/27/06 - add bigint datatype
    rkomurav    07/23/06 - bug-5396879
    anasrini    06/27/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAggrFnDefnNode.java /main/13 2009/11/09 10:10:58 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.SemanticError;
//import oracle.cep.exceptions.ParserError;
import oracle.cep.exceptions.CEPException;
import oracle.cep.semantic.SemanticException;
import java.util.List;
import java.util.ListIterator;
/**
 * Parse tree node corresponding to DDL for a user defined function
 *
 * @since 1.0
 */

public class CEPAggrFnDefnNode extends CEPFunctionDefnNode {

  /** Does it support incremental computation */ 
  protected boolean supportsIncremental;

  /**
   * Constructor for aggregation function
   * @param name name of the user defined aggregation function
   * @param paramSpec single parameter specification
   * @param returnType return type of the function
   * @param className fully qualified java class name of the implementation
   * @param supportsIncremental does it support incremental computation
   */
  public CEPAggrFnDefnNode(String name, CEPAttrSpecNode paramSpec,
                    Datatype returnType, CEPStringTokenNode classNameToken, 
                    boolean supportsIncremental) throws CEPException {
    super(name, paramSpec, returnType, classNameToken);
    String allowedInputTypes = getAllowedInputDataTypes(name);
    String allowedReturnType = getAllowedReturnType(name, paramSpec.getDatatype());
    if( (returnType == Datatype.VOID))
      throw new SemanticException(SemanticError.INVALID_AGGR_FUN_RETURN_TYPE, startOffset,
                            endOffset, new Object[]{returnType, name, allowedReturnType});

    if( (paramSpec.getDatatype() == Datatype.VOID))
      throw new SemanticException(SemanticError.INVALID_AGGR_FUN_INPUT_TYPE,startOffset,
               endOffset,new Object[]{paramSpec.getDatatype(), name, allowedInputTypes});

    this.supportsIncremental = supportsIncremental;
  }
 
  public CEPAggrFnDefnNode(CEPStringTokenNode nameToken, CEPAttrSpecNode paramSpec,
                    Datatype returnType, CEPStringTokenNode classNameToken, 
                    boolean supportsIncremental, NameType nameType) throws CEPException {
    //this(nameToken.getValue(), paramSpec, returnType, classNameToken, supportsIncremental);
    // (ALEALVES) Should not use CEPAggrFnDefnNode(String name, ...) because it considers the start offset 
    //  as the first parameter instead of the name token.
    
    super(nameToken, paramSpec, returnType, classNameToken, nameType);
    String allowedInputTypes = getAllowedInputDataTypes(name);
    String allowedReturnType = getAllowedReturnType(name, paramSpec.getDatatype());
    if( (returnType == Datatype.VOID))
      throw new SemanticException(SemanticError.INVALID_AGGR_FUN_RETURN_TYPE, startOffset,
                            endOffset, new Object[]{returnType, name, allowedReturnType});

    if( (paramSpec.getDatatype() == Datatype.VOID))
      throw new SemanticException(SemanticError.INVALID_AGGR_FUN_INPUT_TYPE,startOffset,
               endOffset,new Object[]{paramSpec.getDatatype(), name, allowedInputTypes});

    this.supportsIncremental = supportsIncremental;
  }

  /**
   * Constructor for aggregation function with multiple arguments
   * @param name name of the user defined aggregation function
   * @param paramSpecList list of parameters
   * @param returnType return type of the function
   * @param className fully qualified java class name of the implementation
   * @param supportsIncremental does it support incremental computation
   * @throws CEPException
   */
  public CEPAggrFnDefnNode(String name, List<CEPAttrSpecNode> paramSpecList,
                    Datatype returnType, CEPStringTokenNode classNameToken,
                    boolean supportsIncremental) 
                    throws CEPException {
    super(name, paramSpecList, returnType, classNameToken);
    
    CEPAttrSpecNode paramSpec;
    ListIterator<CEPAttrSpecNode> iter  = paramSpecList.listIterator();
    String allowedReturnType = "INT, BIGINT, FLOAT, DOUBLE, BIGDECIMAL, BOOLEAN, CHAR, BYTE, TIMESTAMP, INTERVAL and OBJECT";
    String allowedInputTypes = allowedReturnType; //same as the allowed return type in this case
    
    if ((returnType == Datatype.VOID))
      throw new SemanticException(SemanticError.INVALID_AGGR_FUN_RETURN_TYPE,startOffset,
                             endOffset,new Object[]{returnType, name, allowedReturnType});

    while(iter.hasNext())
    {
      paramSpec = iter.next();
      
      if ((paramSpec.getDatatype() == Datatype.VOID))
        throw new SemanticException(SemanticError.INVALID_AGGR_FUN_INPUT_TYPE,startOffset,
                 endOffset,new Object[]{paramSpec.getDatatype(), name, allowedInputTypes});
    }

    this.supportsIncremental = supportsIncremental;
  }

  public CEPAggrFnDefnNode(CEPStringTokenNode nameToken, List<CEPAttrSpecNode> paramSpecList,
                    Datatype returnType, CEPStringTokenNode classNameToken,
                    boolean supportsIncremental, NameType nameType) 
                    throws CEPException {
    //this(nameToken.getValue(), paramSpecList, returnType, classNameToken, supportsIncremental);
    // (ALEALVES) Should not use CEPAggrFnDefnNode(String name, ...) because it considers the start offset 
    //  as the first parameter instead of the name token.

    super(nameToken, paramSpecList, returnType, classNameToken, nameType);

    CEPAttrSpecNode paramSpec;
    ListIterator<CEPAttrSpecNode> iter  = paramSpecList.listIterator();
    String allowedReturnType = "INT, BIGINT, FLOAT, DOUBLE, BIGDECIMAL, BOOLEAN, CHAR, BYTE, TIMESTAMP, INTERVAL and OBJECT";
    String allowedInputTypes = allowedReturnType; //same as the allowed return type in this case

    if ((returnType == Datatype.VOID))
      throw new SemanticException(SemanticError.INVALID_AGGR_FUN_RETURN_TYPE,startOffset,
          endOffset,new Object[]{returnType, name, allowedReturnType});

    while(iter.hasNext())
    {
      paramSpec = iter.next();

      if ((paramSpec.getDatatype() == Datatype.VOID))
        throw new SemanticException(SemanticError.INVALID_AGGR_FUN_INPUT_TYPE,startOffset,
            endOffset,new Object[]{paramSpec.getDatatype(), name, allowedInputTypes});
    }

    this.supportsIncremental = supportsIncremental;
  }


  // Getter methods
  
  /**
   * Does this aggregation function support incremental computation
   * @return true if this aggregation function supports incremental
   *         computation else false
   */
  public boolean supportsIncremental() {
    return supportsIncremental;
  }

  private String getAllowedInputDataTypes(String funcname)
  {
    if ((funcname.equalsIgnoreCase(AggrFunction.SUM.getFuncName())) ||
        (funcname.equalsIgnoreCase(AggrFunction.AVG.getFuncName())))
      return "INT, BIGINT, FLOAT, DOUBLE and BIGDECIMAL";
    else // max, min, count, user defined functions etc.
      return "INT, BIGINT, FLOAT, DOUBLE, BIGDECIMAL, CHAR, BYTE, BOOLEAN, TIMESTAMP, INTERVAL and OBJECT";
  }
  
  private String getAllowedReturnType(String funcname, Datatype inputType)
  {
    if (funcname.equalsIgnoreCase(AggrFunction.AVG.getFuncName()))
      return "FLOAT, DOUBLE or BIGDECIMAL"; //actually should be checking based on input type but double support not added yet
    else if (funcname.equalsIgnoreCase(AggrFunction.COUNT.getFuncName()))
      return "INT";
    else if ((funcname.equalsIgnoreCase(AggrFunction.SUM.getFuncName())) ||
            (funcname.equalsIgnoreCase(AggrFunction.MAX.getFuncName())) ||
            (funcname.equalsIgnoreCase(AggrFunction.MIN.getFuncName())))
      return inputType.name();
    else //user defined functions etc.
      return "INT, BIGINT, FLOAT, DOUBLE, BIGDECIMAL, CHAR, BYTE, BOOLEAN, TIMESTAMP, INTERVAL and OBJECT";
  } 
}

