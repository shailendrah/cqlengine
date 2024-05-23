/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/PatternStreamInterp.java /main/36 2013/01/17 09:23:51 udeshmuk Exp $ */
/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPPatternStreamNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/15/13 - XbranchMerge udeshmuk_bug-15962424_ps6 from
                           st_pcbpel_11.1.1.4.0
    vikshukl    08/29/11 - subquery support
    sbishnoi    05/09/11 - Throwing error if variable duration specified with
                           recurring non eventdetection
    udeshmuk    03/16/11 - fix derived timestamp spec propagation
    udeshmuk    03/09/10 - within clause support
    udeshmuk    04/10/09 - make isClassB true always
    parujain    03/12/09 - make interpreters stateless
    udeshmuk    02/02/09 - support for duration arith_expr in pattern
    udeshmuk    10/15/08 - support for xmlagg orderby in pattern
    udeshmuk    09/17/08 - fix bug 7297193.
    parujain    09/08/08 - support offset
    udeshmuk    09/07/08 - 
    parujain    08/27/08 - semantic Offset
    sbishnoi    07/25/08 - support for nanosecond; making duration as
                           nanosecond
    udeshmuk    07/12/08 - 
    rkomurav    07/04/08 - support recurring non event detection
    udeshmuk    06/30/08 - support for quantifiers over group.
    udeshmuk    06/16/08 - create a default subset containing all correlation
                           vars to allow referencing of fields without
                           correlation names.
    rkomurav    05/14/08 - support non event dectection - duration clause
    rkomurav    05/13/08 - add csemantic checks for aggr param exprs
    rkomurav    04/21/08 - restric usage of first and last outside pattern sub
                           clause
    rkomurav    03/21/08 - thrw exception when subset name reuses one of the
                           correlation names from PATTERn clause
    rkomurav    03/14/08 - subset support
    rkomurav    03/12/08 - remove setStreamName
    rkomurav    03/03/08 - add collectCorrAttrs
    udeshmuk    02/05/08 - parameterize errors.
    rkomurav    02/01/08 - handle alternation to classB
    rkomurav    01/14/08 - add alphabetIndex to corrAttr
    rkomurav    11/27/07 - add semantic checks for PREV
    rkomurav    09/28/07 - support non mandatory correlation definition
    anasrini    09/26/07 - ALL MATCHES support
    rkomurav    09/26/07 - support string correlation names
    rkomurav    09/25/07 - prev range support
    rkomurav    09/05/07 - add prev(A.1,n) support
    rkomurav    08/20/07 - check make iscountstart
    anasrini    06/27/07 - support for partition by clause
    rkomurav    06/25/07 - cleanup
    sbishnoi    06/08/07 - support for multi-arg UDAs
    rkomurav    06/13/07 - bug fix
    rkomurav    06/05/07 - cleanup
    rkomurav    05/11/07 - classB detection
    rkomurav    05/30/07 - 
    anasrini    05/27/07 - handle aggregations
    anasrini    05/24/07 - pass baseVarId to PatternSpec
    anasrini    05/23/07 - measures support
    anasrini    05/23/07 - measures support
    rkomurav    03/05/07 - add tabletype to symboltable
    rkomurav    02/06/07 - add datastructures
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/PatternStreamInterp.java /main/36 2013/01/17 09:23:51 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPBooleanExprNode;
import oracle.cep.parser.CEPPatternDurationNode;
import oracle.cep.parser.CEPPatternWithinNode;
import oracle.cep.parser.CEPStreamNode;
import oracle.cep.parser.CEPRecognizeNode;
import oracle.cep.parser.CEPPatternStreamNode;
import oracle.cep.parser.CEPPatternDefinitionNode;
import oracle.cep.parser.CEPCorrNameDefNode;
import oracle.cep.parser.CEPPatternNode;
import oracle.cep.parser.CEPRegexpNode;
import oracle.cep.parser.CEPPatternMeasuresNode;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPSubsetDefNode;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.RegexpOp;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * The interpreter that is specific to the CEPPatternStreamNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class PatternStreamInterp extends NodeInterpreter {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException
  {
    int                      baseVarId;
    int                      corrVarId;
    int                      numExprs;
    int                      maxPrevIndex;
    int                      corrAttrsSize;
    int                      subsetLength;
    int                      alphIndex;
    int                      durationSymbolAlphIndex;
    long                     durationNanoSecs;
    Expr                     expr;
    BExpr                    bExpr;
    Expr[]                   measures;
    Attr[]                   pbySemAttrs;
    Regexp                   regexp;
    String                   streamName;
    String                   attrName;
    String                   patternAlias;
    boolean                  oldval;
    boolean                  isClassB;
    boolean                  oldCountCorrStarAllowed;
    boolean                  oldPrevAllowed;
    boolean                  oldFirstLastAllowed;
    boolean                  duplicates;
    boolean                  isNonEvent;
    boolean                  isDefaultSubsetNeeded;
    boolean                  isRecurringNonEvent;
    AttrExpr                 attrExpr;
    SymbolTable              oldSymbolTable;
    SymbolTable              newSymbolTable;
    PatternSkip              skipClause;
    SubsetCorr[]             subsetCorrs;
    CEPAttrNode[]            pbyAttrs;
    List<String>             corrAttrs;
    CorrNameDef[]            corrDefsDefined;
    CorrNameDef[]            corrDefs;
    CEPExprNode              measureExprNode;
    CEPExprNode[]            measureExprs;
    CEPRegexpNode            regexpNode;
    CEPStreamNode            streamNode;
    CEPPatternNode           patternNode;
    CEPPatternDurationNode   duration;
    CEPPatternWithinNode     withinClause;
    NodeInterpreter          attrInterp;
    NodeInterpreter          streamInterp;
    NodeInterpreter          defInterp;
    NodeInterpreter          regexpInterp;
    NodeInterpreter          exprInterp;
    CEPSubsetDefNode[]       subsetDefs;
    CEPSubsetDefNode[]       subsetDefinitions;
    SymbolTableEntry         entry;
    CEPRecognizeNode         recognizeNode;
    CEPBooleanExprNode       bNode;
    SymbolTableCorrEntry     corrEntry;
    CEPCorrNameDefNode[]     parserCorrDefs;
    CEPPatternStreamNode     patternStreamNode;
    CEPPatternMeasuresNode   measuresNode;
    CEPPatternDefinitionNode patternDefNode;
    SubquerySpec             subquerySpec;
    
    assert node instanceof CEPPatternStreamNode;
    patternStreamNode = (CEPPatternStreamNode)node;

    //Always use PatternStrmClassB operator
    isClassB = true;
    
    // Create a new symbol table for this new scope
    newSymbolTable = new SymbolTable();
    oldSymbolTable = ctx.getSymbolTable();
    ctx.setSymbolTable(newSymbolTable);
    
    super.interpretNode(node, ctx);

    // Process stream: two possibilities here
    // either base stream or a stream derived from subquery.
    streamNode   = patternStreamNode.getStream();
    streamInterp = InterpreterFactory.getInterpreter(streamNode);
    try {
      streamInterp.interpretNode(streamNode, ctx);
    }
    catch (CEPException e) {
      if (e.getErrorCode() == SemanticError.NOT_A_STREAM_ERROR)
        throw new SemanticException(SemanticError.RECOGNIZE_OVER_REL_ERROR,
        		               streamNode.getStartOffset(), streamNode.getEndOffset(),
                               new Object[]{streamNode.getName()});
      else
        throw e;
    }
       
    // Save derived timestamp spec so that it can be propagated to the logical 
    // layer in the end.
    // This step is needed as the resetTransient method in context is called 
    // before interpreting any node. So the subsequent processing in this 
    // method may reset DerivedTimeSpec to null.
    // Ditto for subquery spec.
    DerivedTimeSpec derivedTsSpec = ctx.getDerivedTimeSpec();
    subquerySpec = ctx.getSubquerySpec();

    // Process the MATCH_RECOGNIZE clause
    recognizeNode       = patternStreamNode.getPatternDesc();
    if(recognizeNode.getDuration()!=null)
      isRecurringNonEvent = recognizeNode.getDuration().isMultiples();
    else
      isRecurringNonEvent = false;
    if(isRecurringNonEvent && !recognizeNode.isAllMatches())
      throw new CEPException(SemanticError.INCORRECT_RECURRING_NON_EVENT_USAGE);
    
    if(isRecurringNonEvent && recognizeNode.getDuration().isExpr())
      throw new CEPException(SemanticError.INCORRECT_RECURRING_NON_EVENT_USAGE_1);

    // Process the partition by clause (if present) before processing
    // the DEFINE sub clause, in particular, before the correlation
    // variables are registered in the symbol table
    pbyAttrs    = recognizeNode.getPartitionByAttrs();
    pbySemAttrs = null;
    
    // count_corr_star is allowed here and preserve prev. value.
    oldCountCorrStarAllowed = ctx.isCountCorrStarAllowed();
    ctx.setCountCorrStarAllowed(true);

    if (pbyAttrs != null)
    {
      int  pbyLen = pbyAttrs.length; 
      pbySemAttrs = new Attr[pbyLen];

      for(int ai = 0; ai < pbyLen; ai++)
      {
        attrInterp = InterpreterFactory.getInterpreter(pbyAttrs[ai]); 
        attrInterp.interpretNode(pbyAttrs[ai], ctx); 
        attrExpr   = (AttrExpr)ctx.getExpr();

        pbySemAttrs[ai] = attrExpr.getAttr();
      }
    }

    withinClause = recognizeNode.getWithinClause();
    boolean isWithin = false;
    boolean isWithinInclusive = false;
    long withinDurationNanoSecs = 0;
    
    if(withinClause != null)
    {
      if(withinClause.getIsInclusive())
        isWithinInclusive = true;
      else
        isWithin = true;
      
      withinDurationNanoSecs = oracle.cep.common.RangeConverter.interpRange
                               (withinClause.getWithinDuration().getAmount(), 
                                withinClause.getWithinDuration().getTimeUnit());
    }
    
    duration                = recognizeNode.getDuration();
    boolean isDurationExpr  = false;
    isNonEvent              = false;
    long nonEventDurationNanoSecs   = 0;
    CEPExprNode durationExprNode = null;
    Expr durationExpr       = null;
    TimeUnit durationUnit   = null;
    if(duration != null)
    {
      isNonEvent      = true;
      isDurationExpr  = duration.isExpr();
      if(!isDurationExpr)
        nonEventDurationNanoSecs = oracle.cep.common.RangeConverter.interpRange
                                   (duration.getDuration().getAmount(), 
                                    duration.getDuration().getTimeUnit());
      else
      { //duration arith_expr
        durationExprNode = duration.getDurationExpr();
        //interpret the duration node
        NodeInterpreter durationInterp = InterpreterFactory.getInterpreter(
                                         durationExprNode);
        //aggr is not allowed in duration clause
        boolean oldIsAggr = ctx.isAggrAllowed();
        ctx.setIsAggrAllowed(false);
        durationInterp.interpretNode(durationExprNode, ctx);
        ctx.setIsAggrAllowed(oldIsAggr);
        durationExpr = ctx.getExpr();
        //return type should be int
        if(durationExpr.getReturnType() != Datatype.INT)
          throw new CEPException(SemanticError.DURATION_EXPR_DOES_NOT_EVALUATE_TO_INT,
                                 new Object[]{durationExpr.getReturnType()});
        durationUnit = duration.getDurationUnit();
          
      }
    }
    
    //assign correct value to durationNanoseconds variable
    //non-event and within clause both won't be present in the same query.
    if(duration != null)
      durationNanoSecs = nonEventDurationNanoSecs;
    else if(withinClause != null)
      durationNanoSecs = withinDurationNanoSecs;
    else
      durationNanoSecs = 0;
      
    streamName         = streamNode.getName();
    assert streamName != null;
    patternNode        = recognizeNode.getPatternString();
    subsetDefs         = recognizeNode.getSubsetDefs();
    regexpNode         = patternNode.getPattern();
    patternDefNode     = recognizeNode.getCorrDefinitions();
    parserCorrDefs     = patternDefNode.getCorrNameDefinitions();
    corrDefsDefined    = new CorrNameDef[parserCorrDefs.length];
    isDefaultSubsetNeeded = recognizeNode.isDefaultSubsetNeeded();
    
    // collect all the correlation names in both the define list and
    // in the regular expression(some correlation names may not be defined)
    // taking into account duplicate correlation names in the regular expr and
    // collecting only the uique set
    corrAttrs  = new ArrayList<String>();
    duplicates = collectCorrs(corrAttrs, regexpNode, parserCorrDefs);
   
    corrAttrsSize = corrAttrs.size();
    
    // register all the collected correlation names
    alphIndex = 0;
    try {
      alphIndex = registerCorrs(ctx, corrAttrs, streamName);
    }
    catch(CEPException ce)
    { // setting different because the error occured when registering an attr
      ce.setStartOffset(regexpNode.getStartOffset());
      ce.setEndOffset(parserCorrDefs[parserCorrDefs.length-1].getEndOffset());
      throw ce;
    }
   
    //create a default subset having all the correlation names
    CEPSubsetDefNode defSubset = new CEPSubsetDefNode(Constants.DEFAULT_SUBSET_NAME,
                                                      corrAttrs);
    if(subsetDefs == null && isDefaultSubsetNeeded)
    {
      subsetDefinitions    = new CEPSubsetDefNode[1];
      subsetDefinitions[0] = defSubset;
    }
    else if(isDefaultSubsetNeeded)
    {
      subsetDefinitions = new CEPSubsetDefNode[subsetDefs.length+1];
      System.arraycopy(subsetDefs, 0, subsetDefinitions, 0, subsetDefs.length);
      subsetDefinitions[subsetDefs.length] = defSubset;
    }
    else 
      subsetDefinitions = subsetDefs;
    
    // register all the subset names
    if(subsetDefinitions != null)
    {
      registerSubsets(ctx, subsetDefinitions, streamName, corrAttrsSize);
    }
    
    // interpret the parser level regexp and convert it to semantic regexp
    regexpInterp = InterpreterFactory.getInterpreter(regexpNode);
    regexpInterp.interpretNode(regexpNode, ctx);
    
    regexp = ctx.getRegExp();
    
    // Process the DEFINE sub clause
    try {
    entry     = ctx.getSymbolTable().lookupSource(streamName);
    }catch(CEPException ce)
    {
      ce.setStartOffset(streamNode.getStartOffset());
      ce.setEndOffset(streamNode.getEndOffset());
      throw ce;
    }
    
    baseVarId = entry.getVarId();
    
    // Initialize correlation definitions and
    // Semantically analyze the predicates defining the correlation names
    oldPrevAllowed      = ctx.isPrevAllowed();
    oldFirstLastAllowed = ctx.isFirstLastAllowed();
    oldval              = ctx.isAggrAllowed();
    ctx.setPrevAllowed(true);
    ctx.setFirstLastAllowed(true);
    
    for(int i = 0; i < parserCorrDefs.length; i++)
    {
      try {
      corrEntry          = ctx.getSymbolTable().lookupCorr(parserCorrDefs[i].getCorrName());
      }catch(CEPException ce)
      {
        ce.setStartOffset(parserCorrDefs[i].getStartOffset());
        ce.setEndOffset(parserCorrDefs[i].getEndOffset());
        throw ce;
      }
      corrVarId          = corrEntry.getVarId();
      corrDefsDefined[i] = new CorrNameDef(corrVarId);
      ctx.setIsAggrAllowed(true);
      ctx.setCorrVarId(corrVarId);

      bNode     = parserCorrDefs[i].getDefinition();
      defInterp = InterpreterFactory.getInterpreter(bNode);
      defInterp.interpretNode(bNode, ctx);
      
      bExpr = (BExpr)ctx.getExpr();
      corrDefsDefined[i].setExpr(bExpr);
    }
    
    maxPrevIndex = ctx.getMaxPrevIndex();
    
    ctx.setIsAggrAllowed(oldval);
    ctx.setPrevAllowed(oldPrevAllowed);
    
    // if some correlation names are not explicitly defined, add default
    // correlation defn for them
    // this order is maintained always (in corrAttrs and corrDefs):
    // 1) explicitly defined correlations
    // 2) implicitly defined correlations
    // 3) subset correlations
    if(corrAttrsSize != corrDefsDefined.length)
    {
      assert corrAttrsSize > corrDefsDefined.length;
      corrDefs = new CorrNameDef[corrAttrsSize];
      System.arraycopy(corrDefsDefined, 0, corrDefs, 0, corrDefsDefined.length);
      for(int i = corrDefsDefined.length; i < corrAttrsSize; i++)
      {
        try{
        corrEntry            = ctx.getSymbolTable().lookupCorr(corrAttrs.get(i));
        }catch(CEPException ce)
        {
          ce.setStartOffset(parserCorrDefs[i].getStartOffset());
          ce.setEndOffset(parserCorrDefs[i].getEndOffset());
          throw ce;
        }
        corrVarId            = corrEntry.getVarId();
        corrDefs[i]          = new CorrNameDef(corrVarId, null);
      }
    }
    else
      corrDefs = corrDefsDefined;
    
    //populate subset ids in corr defs
    if(subsetDefinitions != null)
      populateSubsetIds(ctx, subsetDefinitions, corrDefs);
    
    //create subset corrs
    subsetCorrs = null;
    if(subsetDefinitions != null)
    {
      subsetLength = subsetDefinitions.length;
      subsetCorrs  = new SubsetCorr[subsetLength];
      for(int i = 0; i < subsetLength; i++)
      {
    	try{
        corrEntry = ctx.getSymbolTable().lookupCorr(subsetDefinitions[i].getSubsetName());
        subsetCorrs[i] = new SubsetCorr(corrEntry.getVarId());
        }catch(CEPException ce)
        {
          ce.setStartOffset(subsetDefinitions[i].getStartOffset());
          ce.setEndOffset(subsetDefinitions[i].getEndOffset());
          throw ce;
        }
      }
    }
    
    // Process the SKIP clause
    skipClause     = recognizeNode.getSkipClause();
    
    // Process the MEASURES sub clause
    measuresNode = recognizeNode.getMeasures();
    measureExprs = measuresNode.getMeasureListExprs();
    
    assert measureExprs != null;
    numExprs     = measureExprs.length;
    measures     = new Expr[numExprs];
    
    oldval = ctx.isAggrAllowed();
    for (int i=0; i<numExprs; i++) {
      // Aggregation Function is permitted here
      ctx.setIsAggrAllowed(true);
        
      measureExprNode = measureExprs[i];
      exprInterp  = InterpreterFactory.getInterpreter(measureExprNode);
      exprInterp.interpretNode(measureExprNode, ctx);
      expr = ctx.getExpr();
      assert (measureExprNode.getAlias() != null);
      expr.setName(measureExprNode.getAlias(), true);
      measures[i] = expr;
    }
    ctx.setIsAggrAllowed(oldval);
    ctx.setFirstLastAllowed(oldFirstLastAllowed);

    ArrayList<Integer> stateMap = new ArrayList<Integer>();
    ArrayList<RegexpOp> regList = new ArrayList<RegexpOp>();
    ArrayList<Boolean>  isGroup = new ArrayList<Boolean>();
    
    //regList is the simple list of all regexps defined in the pattern
    //it may not correspond to the order in which correlation names are
    //defined
    getRegList(regexp, regList, stateMap, isGroup, false);
    
    // Now handle the aggregations referenced by the MEASURES and DEFINE 
    // This involves validating that each aggregation refers exactly one
    // correlation name and classifying an aggregation by the 
    // correlation name that it references
    try {
      handleAggrs(corrDefs, measures, subsetCorrs, stateMap, isGroup);
    } catch(CEPException ce)
      {
        ce.setStartOffset(patternStreamNode.getStartOffset());
        ce.setEndOffset(patternStreamNode.getEndOffset());
        throw ce;
      }

    //register duration symbol. This is done at the end of pattern, measures
    //clauses processing as any usages of this symbol are caught as semantic
    //checks and it should not be included in the semCorr list. Adding it into
    //the semCorrs would add it to the bindings, which would be a waste of memory
    durationSymbolAlphIndex = alphIndex;
    if(duration != null)  
    {
      try {
      alphIndex = registerCorr(ctx, Constants.durationSymbol, streamName, 
                  alphIndex);
      }catch(CEPException ce)
      {
        ce.setStartOffset(patternStreamNode.getStartOffset());
        ce.setEndOffset(patternStreamNode.getEndOffset());
        throw ce;
      }
    }
    
    // Now restore the old symbol table
    // Also save the reference to current symbol table in semQuery.
    // This symbol table would contain the base stream entry along with
    // the other corr entries. This base stream entry is needed while
    // interpreting the parition parallel expression (if any) is specified 
    // for this query.
    ctx.getSemQuery().setPatternInlineViewSymTable(ctx.getSymbolTable());
    ctx.setSymbolTable(oldSymbolTable);
    ctx.getSemQuery().setSymTable(oldSymbolTable);

    // Add entries in this "parent" symbol table for the inline view
    // corresponding to this PatternStream
    patternAlias = patternStreamNode.getAlias();
    try {
      ctx.getSymbolTable().addInlineSourceEntry(patternAlias, patternAlias, true);
    }catch(CEPException ce)
    {
      ce.setStartOffset(patternStreamNode.getStartOffset());
      ce.setEndOffset(patternStreamNode.getEndOffset());
      throw ce;
    }

    // Also add entries for the attributes of this inline view
    for (int i=0; i<numExprs; i++) {
      measureExprNode = measureExprs[i];
      attrName = measureExprNode.getAlias();
      
      try{
      ctx.getSymbolTable().addAttrEntry(attrName, patternAlias, i,
                            measures[i].getReturnType(), 0);
      }catch(CEPException ce)
      {
        ce.setStartOffset(measureExprNode.getStartOffset());
        ce.setEndOffset(measureExprNode.getEndOffset());
        throw ce;
      }
    }
    
    PatternSpec patternSpec = 
      new PatternSpec(baseVarId, pbySemAttrs, regexp, corrDefs,
                      measures, newSymbolTable, isClassB, maxPrevIndex,
                      ctx.isPrevRangeExists(), ctx.getMaxPrevRange(), 
                      skipClause, corrAttrs.size(), subsetCorrs, regList,
                      stateMap, isGroup, isNonEvent, isWithin, 
		      isWithinInclusive, durationNanoSecs,
                      durationSymbolAlphIndex, isRecurringNonEvent, 
                      isDurationExpr, durationExpr, durationUnit);

    ctx.setPatternSpec(patternSpec);
    ctx.setDerivedTimeSpec(derivedTsSpec);
    ctx.setSubquerySpec(subquerySpec);
    ctx.setCountCorrStarAllowed(oldCountCorrStarAllowed);
  }
  
  private boolean collectCorrs(List<String> corrs,CEPRegexpNode node,
      CEPCorrNameDefNode[] parserCorrDefs) throws CEPException
  {
    //collect corr attrs from PATTERN clause 
    ArrayList<String> patternCorrs = new ArrayList<String>(); 
    node.getAllReferencedCorrNames(patternCorrs);
    //ensure that all the attrs in DEFINE clause are present in PATTERN clause
    for(int i = 0; i < parserCorrDefs.length; i++)
    {
      if(!patternCorrs.contains(parserCorrDefs[i].getCorrName()))
          throw new CEPException(SemanticError.CORR_ATTR_NOT_PRESENT_IN_PATTERN_CLAUSE,
              new Object[] {parserCorrDefs[i].getCorrName()});
      else
        corrs.add(parserCorrDefs[i].getCorrName());
    }
   
    return node.getAllReferencedCorrNames(corrs);
  }
  
  /**
   * Register all the correlations collected by the collectCorrs
   */
  private int registerCorrs(SemContext ctx, List<String> corrAttrs, String streamName)
  throws CEPException
  {
    int                  alphIndex;
    String               corrName;
    ListIterator<String> iter;
    
    alphIndex = 0;
    iter      = corrAttrs.listIterator();
    while(iter.hasNext())
    {
      corrName = iter.next();
      ctx.getSymbolTable().addCorrEntry(corrName, streamName, alphIndex);
      alphIndex++;
    }
    return alphIndex;
  }
  
  /**
   * Register a particular correlation name
   */
  private int registerCorr(SemContext ctx, String corrName, String streamName, int alphIndex)
  throws CEPException
  {
    ctx.getSymbolTable().addCorrEntry(corrName, streamName, alphIndex);
    alphIndex++;
    return alphIndex;
  }
  
  /**
   * Register all the subset names
   */
  private void registerSubsets(SemContext ctx, CEPSubsetDefNode[] subsetDefs, 
      String streamName, int index) throws CEPException
  {
    for(int i = 0; i < subsetDefs.length; i++)
    {
      try
      {
        ctx.getSymbolTable().addCorrEntry(subsetDefs[i].getSubsetName(), streamName, index);
        index++;
      }
      catch(CEPException e)
      {
        if(e.getErrorCode() == SemanticError.CORR_VAR_ALREADY_EXISTS)
          throw new SemanticException(SemanticError.SUBSETNAME_ALREADY_REGISTERED,
              subsetDefs[i].getStartOffset(), subsetDefs[i].getEndOffset(),
              new Object[] {subsetDefs[i].getSubsetName()});
      }
    }
  }
  
  private void populateSubsetIds(SemContext ctx, CEPSubsetDefNode[] subsetDefs, 
      CorrNameDef[] corrDefs) throws CEPException
  {
    int                  varId;
    int                  subsetId;
    boolean              found;
    String[]             corrs;
    SymbolTableCorrEntry corrEntry;
    for(int i = 0; i < subsetDefs.length; i++)
    {
      try {
      corrEntry = ctx.getSymbolTable().lookupCorr(subsetDefs[i].getSubsetName());
      }catch(CEPException ce)
      {
        ce.setStartOffset(subsetDefs[i].getStartOffset());
        ce.setEndOffset(subsetDefs[i].getEndOffset());
        throw ce;
      }
      subsetId  = corrEntry.getVarId();
      corrs     = subsetDefs[i].getCorrs();
      for(int j = 0; j < corrs.length; j++)
      {
        corrEntry = ctx.getSymbolTable().lookupCorr(corrs[j]);
        varId = corrEntry.getVarId();
        found = false;
        for(int k  = 0; k < corrDefs.length; k++)
        {
          if(varId == corrDefs[k].getVarId())
          {
            corrDefs[k].addSubsetId(subsetId);
            found = true;
            break;
          }
        }
        if(!found)
        {
          throw new SemanticException(SemanticError.SUBSETDEF_REFERS_SUBSETNAME,
              subsetDefs[i].getStartOffset(), subsetDefs[i].getEndOffset(),
              new Object[]{subsetDefs[i].getSubsetName(), corrEntry.getVarName()});
        }
      }
    }
  }
  
  @SuppressWarnings("unused")
  private boolean checkClassB(Expr expr, int varId)
  {
    ArrayList<AggrExpr> aggrList = new ArrayList<AggrExpr>();
    expr.getAllReferencedAggrs(aggrList);
    if(aggrList.size() > 0)
      return true;
    ArrayList<Attr> attrList = new ArrayList<Attr>();
    expr.getAllReferencedAttrs(attrList, SemAttrType.CORR);
    for(int i = 0; i < attrList.size(); i++)
    {
      if(attrList.get(i).getVarId() != varId)
        return true;
    }
    return false;
  }

  /**
   * Handle the aggregations referenced by the MEASURES and DEFINE.
   * <p>
   * This involves validating that each aggregation refers exactly one
   * correlation name and classifying an aggregation by the 
   * correlation name that it references
   * Validations also include if aggregate params is a group corr. variable
   * Group Variable is one which either of
   * a) Defined by Subset
   * b) Occurs multiple times in pattern clause (regular expression)
   * c) Is in the scope of alternation or quantification(* and +)
   * @param corrDefs    semantic layer representation of the correlation name
   *                    definitions
   * @param measures    the expressions in the MEASURES clause
   * @param subsetCorrs subset correlations
   * @param stateMap    list of all the unique correlation names in the regexp
   * @param isGroup     list of flags indicating if a particular corr variable
   *                    is group or not
   */
  private void handleAggrs(CorrNameDef[] corrDefs, Expr[] measures,
      SubsetCorr[] subsetCorrs, ArrayList<Integer> stateMap,
      ArrayList<Boolean> isGroup)
    throws CEPException {

    assert corrDefs != null;
    assert measures != null;

    int            corrLen   = corrDefs.length;
    int            measLen   = measures.length;
    List<AggrExpr> aggrs     = new ArrayList<AggrExpr>();
    BExpr          expr;

    // Collect set of all the referenced aggregations
    for (int i=0; i<corrLen; i++) {
      expr = corrDefs[i].getExpr();
      if(expr == null)
        continue;
      expr.getAllReferencedAggrs(aggrs);
    }

    for (int i=0; i<measLen; i++) {
      measures[i].getAllReferencedAggrs(aggrs);
    }

    // Validate that each aggregate refers exactly one correlation name
    // Then, classify that aggregate with that correlation name
    int        numAggrs = aggrs.size();
    List<Attr> refCorrs;
    int        numCorrsRef;
    int        corrRefVarId;
    boolean    found;
    AggrExpr   aggrExpr;

    for (int i=0; i<numAggrs; i++) {
      refCorrs = new ArrayList<Attr>();
      aggrExpr = aggrs.get(i);
      for(int j=0; j<aggrExpr.getNumParamExprs(); j++)
        (aggrExpr.getExprs())[j].getAllReferencedAttrs(refCorrs, SemAttrType.CORR);
      numCorrsRef = refCorrs.size();
      if (numCorrsRef ==0)
        throw new SemanticException(SemanticError.AGGR_DOES_NOT_REF_CORR);
      
      corrRefVarId = refCorrs.get(0).getVarId();

      // Validate that no other correlation name is referenced
      for (int j=1; j<numCorrsRef; j++) {
        if (corrRefVarId != refCorrs.get(j).getVarId())
          throw new SemanticException(SemanticError.AGGR_REFS_MORE_THAN_ONE_CORR);
      }

      // Now, classify the aggr as "belonging" to that correlation name
      found = false;
      for (int j=0; j<corrLen; j++) {
        if (corrRefVarId == corrDefs[j].getVarId()) {
          if(!isGroupQueryOrSet(stateMap, isGroup, corrRefVarId, true))
            throw new SemanticException(SemanticError.AGGR_PARAM_NOT_GROUP_VAR);
          corrDefs[j].addAggr(aggrExpr);
          found = true;
          break;
        }
      }
      if(!found && (subsetCorrs != null)) {
        for(int j=0; j<subsetCorrs.length; j++) {
          if(corrRefVarId == subsetCorrs[j].getVarId()) {
            assert !found;
            subsetCorrs[j].addAggr(aggrExpr);
            found = true;
            break;
          }
        }
      }
      assert found;
    }
  }
  
  //set or query isGroup flag of the given corrname (varId)
  private boolean isGroupQueryOrSet(ArrayList<Integer> stateMap,
                          ArrayList<Boolean> isGroup, int varId, boolean query)
  {
    int                   index;
    boolean               found;
    ListIterator<Integer> iter;
    
    index = 0;
    found = false;
    iter  = stateMap.listIterator();
    while(iter.hasNext())
    {
      if(iter.next().intValue() == varId)
      {
        found = true;
        break;
      }
      else
        index++;
    }
    assert found;
    
    if(query)
      return isGroup.get(index);
    else
      isGroup.set(index, true);
    
    return true;
  }
  
  //get the list of regular expressions from the regular expression tree
  //A group variable is one which
  //  a) Occurs more than once in regular expression
  //  b} Defined by SUBSET
  //  c) Is in the scope of alternation or quantification
  private void getRegList(Regexp regexp, ArrayList<RegexpOp> regList,
                         ArrayList<Integer> stateMap,
                         ArrayList<Boolean> isGroup, boolean alterOrGroupExpr)
  {
    RegexpOp op;
    if(regexp instanceof SimpleRegexp)
    {
      SimpleRegexp simpleReg = (SimpleRegexp)regexp;
      if(stateMap.contains(simpleReg.getVarId()))
      {
        isGroupQueryOrSet(stateMap, isGroup, simpleReg.getVarId(), false);
        return;
      }
      stateMap.add(simpleReg.getVarId());
      regList.add(null);
      if(alterOrGroupExpr)
        isGroup.add(true);
      else
        isGroup.add(false);
    }
    else
    {
      assert regexp instanceof ComplexRegexp;
      ComplexRegexp complexReg = (ComplexRegexp)regexp;
      op = complexReg.getRegexpOp();
      if(op.equals(RegexpOp.CONCAT))
      {
        getRegList(complexReg.getLeftOperand(), regList, stateMap, isGroup, alterOrGroupExpr);
        getRegList(complexReg.getRightOperand(), regList, stateMap, isGroup, alterOrGroupExpr);
      }
      else if(op.equals(RegexpOp.ALTERNATION))
      {
        getRegList(complexReg.getLeftOperand(), regList, stateMap, isGroup, true);
        getRegList(complexReg.getRightOperand(), regList, stateMap, isGroup, true);
      }
      else
      {
        if(complexReg.getUnaryOperand() instanceof SimpleRegexp)
        {
          SimpleRegexp simpleReg1 = (SimpleRegexp)complexReg.getUnaryOperand();
          if(stateMap.contains(simpleReg1.getVarId()))
          {
            isGroupQueryOrSet(stateMap, isGroup, simpleReg1.getVarId(), false);
            return;
          }
          regList.add(op);
          stateMap.add(simpleReg1.getVarId());
          if(op == RegexpOp.GREEDY_STAR || op == RegexpOp.LAZY_STAR || 
             op == RegexpOp.GREEDY_PLUS || op == RegexpOp.LAZY_PLUS || alterOrGroupExpr)
            isGroup.add(true);
          else
            isGroup.add(false);
        }
        else
          getRegList(complexReg.getUnaryOperand(), regList, stateMap, isGroup, true);
      }
    }
  }

}
