/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanTransformHelper.java /main/15 2015/02/16 09:40:11 udeshmuk Exp $ */

/* Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Logical Plan Generation. Helper related to transformations.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    07/01/13 - remove unused method - setDimensionFlags
    vikshukl    06/11/13 - disable special join for all cases which are not f x
                           d x d
    vikshukl    04/18/13 - detect special join here
    sbishnoi    07/29/10 - XbranchMerge
                           sbishnoi_bug-9947670_ps3_main_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    07/28/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main from main
    sbishnoi    07/28/10 - passing parameter for error
                           BAD_JOIN_WITH_EXTERNAL_RELN
    sborah      12/28/09 - support for multiple external joins
    sbishnoi    12/14/09 - added review comments
    udeshmuk    11/24/09 - don't jump over DISTINCT and GRP_AGGR in
                           t_streamcross
    sbishnoi    09/30/09 - table function support
    parujain    05/28/09 - external relation outer join
    sbishnoi    05/26/09 - dont optimize plan to make streamcross for outerjoin
                           cross
    sborah      05/11/09 - use correct object in t_makeStreamCrossBinary()
    parujain    03/02/09 - outer join for external relations
    parujain    12/18/07 - inner and outer for joins
    parujain    12/13/07 - external relation for binjoin
    najain      10/03/07 - push selects
    anasrini    05/24/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanTransformHelper.java /main/15 2015/02/16 09:40:11 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.logplan;

import oracle.cep.common.Constants;
import oracle.cep.common.OuterJoinType;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * Logical Plan Generation. Helper related to transformations.
 * <p>
 * These transformations may be viewed as optimization of the logical
 * query plan
 */

class LogPlanTransformHelper {

  /**
   * This class should not be instantiated.
   * Contains only static methods.
   */
  private LogPlanTransformHelper() {
  }

  /**
   * Main function that drives all the transformations
   * @param queryPlan the input "naive" plan
   * @return the optimized plan 
   */
  static LogOpt doTransforms(LogOpt queryPlan) 
    throws LogicalPlanException 
  {
    queryPlan = t_rstreamNow(queryPlan);
    queryPlan = t_removeIstream(queryPlan);
    queryPlan = t_streamCross(queryPlan);
    queryPlan = t_removeProject(queryPlan);

    queryPlan = t_makeCrossBinary(queryPlan);
    queryPlan = t_makeStreamCrossBinary(queryPlan);
    queryPlan = t_pushSelect(queryPlan);
    
    return queryPlan;
  }
  
  /**
   * Transform:
   * 
   * Input plan pattern: Cross (inp1, inp2,...,Stream[now] {at ith position}
   * ...inpn) -->
   * (select|project|distinct|aggr|xstream)
   * 
   * Output: Cross (Stream[now], inp2,..., inp1 {at ith position},...inpn) -->
   * (select|project|distinct|aggr|xstream)
   * 
   * It also checks for the existence of at least one Stream[now] operator
   * as an input to the cross operator in case it has an external relation
   * as on of its inputs.
   * 
   * @param opt The plan with a Cross operator as the root 
   * @return the normalized logplan
   * @throws LogicalPlanException
   */
  private static LogOpt normalizeInputsForCross(LogOpt cross)
  throws LogicalPlanException
  {
    assert cross instanceof LogOptCross;
    
    int     streamNowPos = -1;
    boolean hasPullOperator = false;
    int     pullOperatorPos = -1;
    
    for(int i = 0; i < cross.getNumInputs(); i ++)
    {
      LogOpt input = cross.getInput(i);
      if(input.isInstantaneous())
      {
        streamNowPos = i;
        // if the existence of a pull operator and the position of S[now], 
        // both have been determined, then break from the loop.
        if(hasPullOperator)
          break;
      }
      else if(input.isPullOperator())
      {
        hasPullOperator = true;
        pullOperatorPos = i;
      }
    }
    
    // normalize only if there is a pull operator as an input to the 
    // cross operator.
    if(hasPullOperator)
    {
      if(streamNowPos == -1)
      {
        // ERROR : throw bad join operation
        // If the cross contains a pull operator 
        // there must be atleast one S[now] operator
        // as an input to this cross
        String extRelName = "";
        if(pullOperatorPos != -1)
        {
          assert cross != null : "Cross logical operator reference is null";
          extRelName = getExternalRelName(cross.getInput(pullOperatorPos));
        }
        throw new LogicalPlanException(
            LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, extRelName);
      }
      else if(streamNowPos != 0)
      {
        // swap the instantaneous operator with the leftmost
        // input operator in the cross
        LogOpt temp = cross.getInput(0);
        LogOpt streamNowOp = cross.getInput(streamNowPos);
        cross.setInput(0, streamNowOp);
        cross.setInput(streamNowPos, temp);
      }
    }
    
    return cross;
  }
  
  
  /**
   * ----------------------------------------------------------------------
   * 
   * Bunch of transformation rules for query plans: all prefixed with "t_"
   * 
   */
    
  /**
   * Transform:
   * 
   * Input plan pattern: Cross (inp1, inp2, ,...inpn) -->
   * (select|project|distinct|aggr|xstream)
   * 
   * Output: Cross(.. Cross(Cross(inp1,inp2), inp3) ..) --> (select ....
   * xstream)
   */
  private static LogOpt t_makeCrossBinary(LogOpt plan) 
  throws LogicalPlanException
  {
    assert (plan != null);
    assert (plan.getOutput() == null);

    // Walk down the tree to identify the CROSS operator
    LogOpt cross = null;
    LogOpt op = plan;

    while (true)
    {
      assert (op != null);

      if (op.getOperatorKind() == LogOptKind.LO_CROSS)
      {
        cross = op;
        break;
      }

      if ((op.getOperatorKind() == LogOptKind.LO_SELECT)
          || (op.getOperatorKind() == LogOptKind.LO_PROJECT)
          || (op.getOperatorKind() == LogOptKind.LO_GROUP_AGGR)
          || (op.getOperatorKind() == LogOptKind.LO_DISTINCT)
          || (op.getOperatorKind() == LogOptKind.LO_ISTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_DSTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_RSTREAM))
        op = op.getInputs().get(0);
      // Unrecognized pattern
      else
        return plan;
    }

    int numInputs = cross.getNumInputs();
    assert (numInputs > 1);

    // Already binary: no work to do.
    if (numInputs == 2 && (!cross.isExternal())){
    	if(cross.getInput(0) instanceof LogOptCross){
    		LogOptCross cs = (LogOptCross) cross.getInput(0);
    		assert cs.getOuterJoinType() == OuterJoinType.LEFT_OUTER || cs.getOuterJoinType() == OuterJoinType.RIGHT_OUTER 
    		    || cs.getOuterJoinType() == OuterJoinType.FULL_OUTER;
    		cross.setArchivedDim(cross.getInput(0).isArchivedDim() && cross.getInput(1).isArchivedDim());
    		LogUtil.fine(LoggerType.TRACE, "Set the isArchivedDim to TRUE for a join operator");
    	}
    	else if(!(cross.getInput(0).isArchivedDim()) && (cross.getInput(1).isArchivedDim())){
    	  cross.setArchivedDim(true);
    	  LogUtil.fine(LoggerType.TRACE, "Set the isArchivedDim to TRUE for a join operator");
    	}
    	
    	return plan;
    }
      

    // If this cross is for ANSI outer join type; It will be already binary
    // we would have already interchanged the inputs for external relation
    if(((LogOptCross)cross).getOuterJoinType() != null)
    {
      assert numInputs == 2;
      assert cross.isExternal();
      return plan;
    }
    
    // normalize the inputs of the LogOptCross operator.
    // Check if it has an external relation (isPullOperator) as its inputs.
    // if it does, then shift the  Stream[NOW] operator to the leftmost 
    // position.    
    cross = normalizeInputsForCross(cross);

    // first decide whether special join is possible or not.
    boolean disabledimjoin = false;
    LogOpt first = cross.getInput(0);
    
    //first will be logoptcross only in case of outer join combined with normal joins
    if((first instanceof LogOptCross)){
      LogOptCross cs = (LogOptCross) first;
      assert cs.getOuterJoinType() == OuterJoinType.LEFT_OUTER;
      disabledimjoin = !cs.isArchivedDim(); 
    }
    else{
      if (first.isArchivedDim())
        disabledimjoin = true;
    }
    
    // first one is a fact table, now check the rest
    // all of them must be dimensions
    // we will always require the left most relation to be a fact table.
    // though ideally we could support  ( d x f ) x d x d 
    // Only supported pattern is:
    //  (fact x dimension x dimension x dimension)
    //  if any of the tables after fact is not a dimension, we don't mark
    //  the join.
    if (!disabledimjoin)
    {
      for (int k=1; k < cross.getNumInputs(); k++) {
    	  
        LogOpt opt = cross.getInput(k);
        if (!opt.isArchivedDim()) {
          disabledimjoin = true;
          LogUtil.fine(LoggerType.TRACE, 
                       "LogPlanTransformHelper: dimension join is disabled");
          break;     
        }
      }
    }      
    if (disabledimjoin) {
      // clear all flags
      for (int k=0; k < cross.getNumInputs(); k++) {
        LogOpt opt = cross.getInput(k);
        opt.setArchivedDim(false);        
      }
    }
        
    LogOptCross binCross = new LogOptCross();
    
    // left and right input for the cross operator
    LogOpt leftInput     = cross.getInput(Constants.OUTER);    
    LogOpt rightInput    = cross.getInput(Constants.INNER);
    
    // left and right input for the newly constructed cross operator
    LogOpt newLeftInput  = null;
    LogOpt newRightInput = null;
    
    // isExternal flag for the newly constructed corss operator
    boolean isExternal   = false;

    // Action: Interchange the inputs so that external relation will come
    // to right of a join operator
    if(leftInput.isPullOperator())
    {
      // check if left input is external relation, if yes then interchange
      newLeftInput  = rightInput;
      newRightInput = leftInput;      
    }    
    else
    {
      newLeftInput  = leftInput;
      newRightInput = rightInput;      
    }
    
    // new cross operator will be external only if its right side will have
    // a LogOptRelnSrc operator corresponding to an external relation
    isExternal = (newRightInput.isPullOperator());

    // add the input operator to binCross
    binCross.addInput(newLeftInput);
    binCross.addInput(newRightInput);
    
    binCross.setArchivedDim(!disabledimjoin);     
         
    binCross.setExternal(isExternal);
    
    int numInps = cross.getNumInputs();
    for (int i = 2; i < numInps; i++)
    {
      // Remember: We don't need to interchange because left input will always
      // be a join operator. so any new external input will always be on the 
      // right side of this new join node.
      LogOptCross binCrossPar = new LogOptCross();
      leftInput  = binCross;
      rightInput = cross.getInputs().get(i);
      
      assert leftInput instanceof LogOptCross;
      
      // new cross operator will be external only if the right input will 
      // represent an external relation source
      isExternal = rightInput.isPullOperator();
      
      // set the newly created bin-cross operator
      binCrossPar.addInput(leftInput);
      binCrossPar.addInput(rightInput);
      binCrossPar.setExternal(isExternal);
      binCross = binCrossPar;
      binCross.setArchivedDim(!disabledimjoin);
    }

    binCross.setOutput(cross.getOutput());

    if (binCross.getOutput() != null)
    {
      assert (binCross.getOutput().getNumInputs() == 1);
      binCross.getOutput().getInputs().set(Constants.OUTER, binCross);
      binCross.getOutput().updateSchemaStreamCross();
    }    
    else
    {
      // cross was the root of the plan, now binCross is
      plan = binCross;
    }
    return plan;
  }
  

  /**
   * Transform:
   * 
   * Input plan pattern: Stream Cross (inp1, inp2, ,...inpn) -->
   * (select|project|distinct|aggr|xstream)
   * 
   * Output: StreamCross(.. StreamCross(Cross(inp1,inp2), inp3) ..) --> (select
   * .... xstream)
   */

  private static LogOpt t_makeStreamCrossBinary(LogOpt plan)
  {
    assert (plan != null);
    assert (plan.getOutput() == null);

    // Walk down the tree to identify the CROSS operator
    LogOpt streamCross = null;
    LogOpt op = plan;

    while (true)
    {
      assert (op != null);

      if (op.getOperatorKind() == LogOptKind.LO_STREAM_CROSS)
      {
        streamCross = op;
        break;
      }

      if ((op.getOperatorKind() == LogOptKind.LO_SELECT)
          || (op.getOperatorKind() == LogOptKind.LO_PROJECT)
          || (op.getOperatorKind() == LogOptKind.LO_GROUP_AGGR)
          || (op.getOperatorKind() == LogOptKind.LO_DISTINCT)
          || (op.getOperatorKind() == LogOptKind.LO_ISTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_DSTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_RSTREAM))
        op = op.getInputs().get(0);
      // Unrecognized pattern
      else
        return plan;
    }

    int numInputs = streamCross.getNumInputs();
    assert (numInputs > 1);
    
    LogOptStrmCross binStreamCross = null;

    // Already binary: no work to do.
    if (numInputs == 2 && !(streamCross.isExternal()))
      return plan;
    
    // NOTE: normalization (shifting the stream operator to the leftmost input 
    // position of the cross operator) is not required for a stream cross 
    // operator because unlike the cross operator, the stream cross operator is 
    // normalized during its creation itself. A Stream cross operator is created
    // only when the cross has a Stream[now] as its input , so no further 
    // check is required.

    // Initialize Left and Right input of the stream cross
    LogOpt leftInput = streamCross.getInputs().get(Constants.OUTER);
    LogOpt rightInput = streamCross.getInputs().get(Constants.INNER);
    
    // New left and right to make the right input EXTERNAL always
    LogOpt newLeftInput = null;
    LogOpt newRightInput = null;
    
    // Action: Interchange the inputs so that external relation will come
    // to right of a join operator
    if(leftInput.isPullOperator())
    {
      // check if left input is external relation, if yes then interchange
      newLeftInput  = rightInput;
      newRightInput = leftInput;      
    }    
    else
    {
      newLeftInput  = leftInput;
      newRightInput = rightInput;      
    }
    
    binStreamCross = new LogOptStrmCross(newLeftInput, 2);    
    
    // Join the first two inputs to get the first of the sequence of
    // binary crosses.
    binStreamCross.add_input(newRightInput, 1);
    
    
    // new cross operator will be external only if any of its side will have
    // a LogOptRelnSrc operator corresponding to an external relation or 
    // in other words ,a pull operator.
    boolean isExternal = newRightInput.isPullOperator();   
    binStreamCross.setExternal(isExternal);
 
    for (int i = 2; i < numInputs; i++)
    {
      LogOptStrmCross binStreamCrossPar = null;
      
      leftInput  = binStreamCross;
      rightInput = streamCross.getInputs().get(i);
      
      binStreamCrossPar = new LogOptStrmCross(leftInput, 2);
      binStreamCrossPar.add_input(rightInput, Constants.INNER);
      
      // new stream cross operator will be external only if the right input will 
      // represent an external relation source
      isExternal 
        = rightInput.isPullOperator();
      
      binStreamCrossPar.setExternal(isExternal);
      binStreamCross = binStreamCrossPar;
    }

    binStreamCross.setOutput(streamCross.getOutput());

    if (binStreamCross.getOutput() != null)
    {
      assert (binStreamCross.getOutput().getNumInputs() == 1);
      binStreamCross.getOutput().getInputs().set(0, binStreamCross);
    }
    else
    {
      // cross was the root of the plan, now binStreamCross is
      plan = binStreamCross;
    }

    return plan;
  }

  

  /**
   * Transform: (Stream -> [Now] -> filter -> project -> rstream) to the form
   * (Stream -> filter -> project)
   */
  /**
   * Transform:
   * 
   * Plan pattern:
   * 
   * Stream Source -> [Now] -> (Select|Project|Distinct) * -> Rstream
   * 
   * Output:
   * 
   * Same plan with Now & Rstream removed.
   */
  private static LogOpt t_rstreamNow(LogOpt plan) throws LogicalPlanException
  {

    assert (plan != null);
    assert (plan.getOutput() == null);
    // Pattern failure: top most operator not rstream
    if (plan.getOperatorKind() != LogOptKind.LO_RSTREAM)
      return plan;
    LogOpt rstream = plan;
    // Traverse down the plan upto [Now] operator
    LogOpt op = rstream.getInputs().get(0);
    LogOpt now = null;
    while (true)
    {
      assert (op != null);
      // Found a [Now] operator
      if (op.getOperatorKind() == LogOptKind.LO_NOW_WIN)
      {
        now = op;
        break;
      }
      // Select | Project | Distinct
      if ((op.getOperatorKind() == LogOptKind.LO_SELECT)
          || (op.getOperatorKind() == LogOptKind.LO_PROJECT)
          || (op.getOperatorKind() == LogOptKind.LO_DISTINCT))
        op = op.getInputs().get(0);
      // Pattern failure:
      else
        return plan;
    }

    // The pattern is satisfied:
    // (1) remove rstream
    plan = rstream.getInputs().get(0);
    plan.setOutput(null);
    // (2) remove now.
    now.getInputs().get(0).setOutput(now.getOutput());

    if (now.getOutput() == null)
    {
      assert (plan == now);
      plan = now.getInputs().get(0);
    }
    else
    {
      assert (now.getOutput().getNumInputs() == 1);
      now.getOutput().getInputs().set(0, now.getInputs().get(0));
      // Update all intermediate operators to reflect that they all produce
      // streams
      op = now.getOutput();
      while (op != null)
      {
        assert ((op.getOperatorKind() == LogOptKind.LO_SELECT)
            || (op.getOperatorKind() == LogOptKind.LO_PROJECT) || (op
            .getOperatorKind() == LogOptKind.LO_DISTINCT));

        assert (op.getInputs().get(0).getIsStream());

        op.setIsStream(true);
        op = op.getOutput();
      }
    }

    return plan;
  }

  /**
   * Remove redundant projects: (useful in Select * queries)
   */
  /**
   * Transform:
   * 
   * Input plan pattern:
   * 
   * (something) --> Project --> (Select|Distinct|XStream)*
   * 
   * Output:
   * 
   * If the project is redundant, remove the project operator.
   */
  private static LogOpt t_removeProject(LogOpt plan) 
    throws LogicalPlanException
  {
    assert (plan != null);
    assert (plan.getOutput() == null);

    // Walk down the plan tree to locate a project operator
    LogOpt project = null;
    LogOpt op = plan;

    while (true)
    {
      assert (op != null);

      // project operator.
      if (op.getOperatorKind() == LogOptKind.LO_PROJECT)
      {
        project = op;
        break;
      }

      if ((op.getOperatorKind() == LogOptKind.LO_SELECT)
          || (op.getOperatorKind() == LogOptKind.LO_DISTINCT)
          || (op.getOperatorKind() == LogOptKind.LO_RSTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_DSTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_ISTREAM))
        op = op.getInputs().get(0);

      // Pattern failure: no transformation
      else
        return plan;
    }

    // Remove the project if it is useless
    if (((LogOptProject) project).isUseless())
    {
      project.getInputs().get(0).setOutput(project.getOutput());

      // Project is the root of the query plan
      if (project.getOutput() == null)
        plan = project.getInputs().get(0);

      // Project is some intermediate operator
      else
      {
        assert (project.getOutput().getNumInputs() == 1);
        project.getOutput().getInputs().set(0, project.getInputs().get(0));
      }
    }

    return plan;
  }

  /**
   * Transform: (Stream -> (implicit unbounded) -> filter -> istream) to
   * (Stream -> filter )
   */
  private static LogOpt t_removeIstream(LogOpt plan) 
    throws LogicalPlanException
  {
    assert (plan != null);
    assert (plan.getOutput() == null);

    if ((plan.getOperatorKind() == LogOptKind.LO_ISTREAM)
        && (plan.getInputs().get(0).getIsStream() == true))
    {
      plan = plan.getInputs().get(0);
      plan.setOutput(null);
    }

    return plan;
  }

  /**
   * Identify and use "Stream Cross Product" operator instead of the normal
   * cross product:
   * 
   * Transform:
   * 
   * Rstream ( Cross Product (S [Now], <rels> )) to
   * 
   * (Stream Cross Product (S, <rels> ))
   * 
   * A stream cross product operation probes the relations for every stream
   * tuple of S and produces a concated stream tuple.
   */
  private static LogOpt t_streamCross(LogOpt plan) throws LogicalPlanException
  {
    assert (plan != null);
    assert (plan.getOutput() == null);

    // Root is not rstream: pattern failure
    if (plan.getOperatorKind() != LogOptKind.LO_RSTREAM)
      return plan;

    LogOpt rstream = plan;

    // Travel down the plan tree to find the CROSS operator
    LogOpt op = plan.getInputs().get(0);

    LogOpt cross = null;
    while (true)
    {
      assert (op != null);

      if (op.getOperatorKind() == LogOptKind.LO_CROSS)
      {
        cross = op;
        break;
      }

      if (op.getOperatorKind() == LogOptKind.LO_PROJECT)
      {
        op = op.getInputs().get(0);
      }
      else if (op.getOperatorKind() == LogOptKind.LO_SELECT)
      {
        LogOptSelect select = (LogOptSelect)op;        
        // Note: This check is meaningful for the Oracle' OuterJoin operator(+)
        // In ANSI outer join, join predicate will be part of LogOptCross.
        
        // If Outer join then pattern failure
        if(select.isOuterJoin())
        {
          return plan;
        }
        op = op.getInputs().get(0);
      }
      // Pattern failure
      else
        return plan;
    }
    
    assert cross instanceof LogOptCross;
    
    // Don't make StreamCross if the LogOptCross represents an outer join  
    if(((LogOptCross)cross).getOuterJoinType() != null)
      return plan;

    LogOpt now = null;
    int nowIndex = 0;
    for (int i = 0; i < cross.getNumInputs(); i++)
    {
      if (cross.getInputs().get(i).getOperatorKind() == LogOptKind.LO_NOW_WIN)
      {
        now = cross.getInputs().get(i);
        nowIndex = i;
        break;
      }
    }

    // No now window below the cross: pattern failure
    if (now == null)
      return plan;

    // Replace the CROSS by a STREAM CROSS operator
    LogOpt streamSource = now.getInputs().get(0);
    assert (streamSource != null);

    LogOptStrmCross streamCross = new LogOptStrmCross(streamSource,
        (LogOptCross) cross);

    int pos = 1;
    for (int i = 0; i < cross.getNumInputs(); i++)
    {
      if (i == nowIndex)
        continue;
      streamCross.add_input(cross.getInputs().get(i), pos);
      pos++;
    }

    // Cross cannot be the toppmost operator: we know rstream is above it.
    assert (cross.getOutput() != null);

    streamCross.setOutput(cross.getOutput());

    // We only jumped over operators with single stream input
    assert (cross.getOutput().getNumInputs() == 1);

    cross.getOutput().getInputs().set(0, streamCross);

    // Since the order of inputs for STREAM CROSS is (possibly) not the
    // same as that of CROSS, we need to update the schema of the above
    // operators .
    cross.getOutput().updateSchemaStreamCross();

    cross.getOutput().updateStreamPropertyStreamCross();

    // Finally remove the rstream
    assert (rstream.getInputs().get(0) != null);
    plan = rstream.getInputs().get(0);
    plan.setOutput(null);

    return plan;
  }

  private static LogOptSelect findFirstSelect(LogOpt plan)
  {
    LogOpt op = plan;

    while (true)
    {
      assert (op != null);

      if (op.getOperatorKind() == LogOptKind.LO_SELECT)
        return (LogOptSelect)op;

      if ((op.getOperatorKind() == LogOptKind.LO_ISTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_DSTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_RSTREAM)
          || (op.getOperatorKind() == LogOptKind.LO_PROJECT)
          || (op.getOperatorKind() == LogOptKind.LO_DISTINCT)
          || (op.getOperatorKind() == LogOptKind.LO_GROUP_AGGR))
      {
        assert (op.getNumInputs() == 1);
        op = op.getInputs().get(0);
      }
      // Pattern does not match the required pattern
      else
        return null;
    }
  }

  /**
   * Transform: push down selects below CROSS / Stream CROSS operator Input
   * plan:
   * 
   * --> Cross ( ) --> (select) --> (select) --> ... --> -->
   * (project|distinct|aggr|xstream)
   */
  private static LogOpt t_pushSelect(LogOpt plan) throws LogicalPlanException
  {
    assert (plan != null);
    assert (plan.getOutput() == null);

    // Get the top most select operator
    LogOptSelect select = findFirstSelect(plan);
    if (select == null)
      return plan;

    // Try to push each select operator as much as possible.
    while (true)
    {
      // Try to push the select below the cross. For coding convenience
      // the pushSelect function pushes a copy of the select operator,
      // while retaining the original select operator in its initial
      // position.
      boolean bPushed = select.pushSelect();

      // We manage to push this one: delete the existing select
      if (bPushed)
      {
        assert (select.getNumInputs() == 1);
        assert (select.getInputs().get(0) != null);

        select.getInputs().get(0).setOutput(select.getOutput());

        if (select.getOutput() != null)
        {
          assert (select.getOutput().getNumInputs() == 1);
          select.getOutput().getInputs().set(0, select.getInputs().get(0));
        }
        else
        {
          assert (plan == select);
          plan = select.getInputs().get(0);
        }
      }

      select = findFirstSelect(select.getInputs().get(0));
      if (select == null)
	break;
    }

    return plan;
  }
  
  /**
   * Helper function to get the external relation name
   * @param inpRelOp input logical operator corresponding to external relation
   * @return name of external relation
   */
  private static String getExternalRelName(LogOpt inpRelOp)
  {
    if(inpRelOp == null)
      return "";
    else if(inpRelOp instanceof LogOptRelnSrc)
    {
      return ((LogOptRelnSrc)inpRelOp).getRelationName(); 
    }
    else if(inpRelOp instanceof LogOptTableFunctionRelSource)
    {
      return ((LogOptTableFunctionRelSource)inpRelOp).getTableAlias(); 
    }
    else
      return "";
  }
}
