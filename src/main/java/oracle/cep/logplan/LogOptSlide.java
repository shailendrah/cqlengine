/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSlide.java /main/2 2013/06/27 21:07:53 sbishnoi Exp $ */

/* Copyright (c) 2012, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    06/26/13 - bug 16571604
    sbishnoi    05/25/12 - Creation
 */

package oracle.cep.logplan;

import oracle.cep.exceptions.LogicalPlanError;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSlide.java /main/2 2013/06/27 21:07:53 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class LogOptSlide extends LogOpt implements Cloneable
{
  /** specifies the query evaluation interval in nanos */
  private long numSlideNanos;
  
  
  /**
   * Construct a logical operator for slide without window
   * @param input input upstream operator
   * @param numSlideNanos slide value specified as number of nanoseconds
   */
  public LogOptSlide(LogOpt input, long numSlideNanos)
  {
    super(LogOptKind.LO_SLIDE);
    
    // Number of input operator is 1
    setNumInputs(1);    
    setInput(0, input);
    
    // output attributes of slide is same as input as we are not changing the
    // tuple definition in this operator
    copy(input);
    
    // Output will always be a relation
    setIsStream(false);
    
    // Set this operator as output of its upstream operator 
    input.setOutput(this);
    
    // Set the query evaluation interval (slide interval)
    this.numSlideNanos = numSlideNanos;
  }


  /**
   * @return the numSlideNanos
   */
  public long getNumSlideNanos()
  {
    return numSlideNanos;
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.logplan.LogOpt#clone()
   */
  @Override
  public LogOpt clone() throws CloneNotSupportedException
  {
    LogOptSlide op = (LogOptSlide)super.clone();
    
    // Set the slide value
    op.numSlideNanos = this.numSlideNanos;
    
    return op;
  }


  /* (non-Javadoc)
   * @see oracle.cep.logplan.LogOpt#validate()
   */
  @Override
  protected void validate() throws LogicalPlanException
  {
    // Get the input operator, Use index 0 as there is only one input
    LogOpt input = this.getInput(0);
    
    // Evaluate Clause can only be applied on relation
    if(input.getIsStream())
    {
      throw new LogicalPlanException(
        LogicalPlanError.INVALID_EVALUATE_CLAUSE_USAGE);
    }
  }
  
  @Override
  public int canSelectBePushed(LogOptSelect select) 
  {
    // Note: Select can be pushed below the Slide operator, hence return the 
    // child input operator position
    return 0;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<LogicalOperatorSlide>");
    // dump the common fields
    sb.append(super.toString());
    
    // Dump the Slide interval
    sb.append("<SlideInterval>" + this.numSlideNanos + "</SlideInterval>");
    
    sb.append("</LogicalOperatorSlide>");
    return sb.toString();
  }
}
