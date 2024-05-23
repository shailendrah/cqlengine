/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprComplex.java /main/9 2013/01/10 21:48:12 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Complex Expression Physical Operator Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    01/02/13 - fix 13948958 ADD INTERVAL TO TIME QUERY FAILED
 udeshmuk    06/20/11 - support getSQLEquivalent
 udeshmuk    11/08/09 - API to get all referenced attrs
 sborah      04/20/09 - define getSignature
 rkomurav    06/18/07 - cleaunp
 rkomurav    03/06/07 - restructure exprfactorycontext
 parujain    12/21/06 - Fix equal
 rkomurav    10/10/06 - add equals method
 dlenkov     09/27/06 - 
 rkomurav    08/23/06 - add getXMLPlan2
 najain      03/24/06 - cleanup
 anasrini    03/14/06 - add getter methods 
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprComplex.java /main/9 2013/01/10 21:48:12 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.ArithOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.SQLType;
import oracle.cep.common.TimeUnit;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Complex Expression Physical Operator Expression Class Definition
 */
public class ExprComplex extends Expr {
  /** Arithmetic Operator */
  ArithOp oper;

  /** Left Expression */
  Expr    left;

  /** Right Expression */
  Expr    right;
  
  public ExprComplex(ArithOp oper, Expr left, Expr right, Datatype dt)
  {
    super(ExprKind.COMP_EXPR);
    setType(dt);
    
    this.oper  = oper;
    this.left  = left;
    this.right = right;
  }

  // getter methods

  /**
   * Get the arithmetic operator
   * 
   * @return the arithmetic operator
   */
  public ArithOp getOper() {
    return oper;
  }

  /**
   * Get the left operand
   * 
   * @return the left operand
   */
  public Expr getLeft() {
    return left;
  }

  /**
   * Get the right operand
   * 
   * @return the right operand
   */
  public Expr getRight() {
    return right;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * A complex expression contains either binary operators like 
   * AND, OR , XOR or unary operators like NOT, IS NULL , IS NOT NULL
   * 
   * @return 
   *      A concise String representation of the Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     
     // Handle the left and right side of the comparision expression
     // separately.
     Expr left  = this.getLeft();
     Expr right = this.getRight();
     
     regExpression.append("(");
     
     if(right != null)
     {
       // The expression can either be complex or a base expression
       regExpression.append(left.getSignature());

       regExpression.append(this.getOper().getSymbol());

       // The expression can either be complex or a base expression
       regExpression.append(right.getSignature());
     }
     else
     {
       // Unary operators
       regExpression.append(this.getOper().getSymbol());

       // The expression can either be complex or a base expression
       regExpression.append(left.getSignature());
     }
     
     regExpression.append(")");
 
     return regExpression.toString();
   }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorComplexExpression>");
    sb.append(super.toString());
    sb.append("<ArithmeticOperator oper=\"" + oper + "\" />");

    sb.append("<Left>");
    sb.append(left.toString());
    sb.append("</Left>");

    if (right != null) {
      sb.append("<Right>");
      sb.append(right.toString());
      sb.append("</Right>");
    }

    sb.append("</PhysicalOperatorComplexExpression>");
    return sb.toString();
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprComplex other = (ExprComplex) otherObject;
    if(other.oper != null)
    {
      if(right != null)
      {
        if(other.getRight() == null)
          return false;
        else
          return (oper.equals(other.getOper()) && left.equals(other.getLeft())
                  && right.equals(other.getRight()));
      }
      else
      {
        if(other.getRight() != null)
          return false;
        else
          return (oper.equals(other.getOper()) && left.equals(other.getLeft()));
      }
    }

    return false;
    
  }
  
//Generate and return visualiser compatible xml plan
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append(left.getXMLPlan2());
    xml.append(oper.getExpression());
    if (right != null)
      xml.append(right.getXMLPlan2());
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    left.getAllReferencedAttrs(attrs);
    if(right != null)
      right.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
    SQLType targetSQLType = 
        ec.getServiceManager().getConfigMgr().getTargetSQLType();
    
    // Handle the left and right side of the comparision expression
    // separately.
    Expr left  = this.getLeft();
    Expr right = this.getRight();
    
    /*
     * It looks like Arithop other than +, -, *, / and concat are not
     * used. They are referenced in the code at some places but probably
     * the code path never gets executed.
     */
    
    /**
     * TODO: Need a better approach where the mapping between the expressions
     * are calculated in a separate class.
     * ###########  For BI Mode ##############
     * If the arithmetic operation is datetime operation, then BI mode doesn't
     * allow the plain expr1+expr2 or expr1 - expr2 syntax.
     * Instead of that, we have to use library functions TIMESTAMPADD and 
     * TIMESTAMPDIFF
     * Here is the transformation plan:
     * Case 1: timestamp + interval is transformed to TIMESTAMPADD(...)
     * Case 2: interval + timestamp is transformed to TIMESTAMPADD(...)
     * Case 3: interval + interval  is regular arithmetic addition on long type
     * Case 4: timestamp - timestamp is transformed to TIMESTAMPDIFF(...)
     * Case 5: timestamp - interval  is transformed to TIMESTAMPADD(...)
     * Case 6: interval - interval is regular arithmetic subtraction on long dt.
     * 
     * Please refer to the link below for TIMESTAMPADD and TIMESTAMPDIFF:
     * http://docs.oracle.com/cd/E23943_01/bi.1111/e10544/appsql.htm#CHDJDGBE
     *
     * TIMESTAMPADD(interval, intExpr, timestamp)
     * Where:
     * interval is the specified interval. Valid values are:
     *  SQL_TSI_SECOND
     *  SQL_TSI_MINUTE
     *  SQL_TSI_HOUR
     *  SQL_TSI_DAY
     *  SQL_TSI_WEEK
     *  SQL_TSI_MONTH
     *  SQL_TSI_QUARTER
     *  SQL_TSI_YEAR
     *  
     * intExpr is any expression that evaluates to an integer value.
     * timestamp is any valid timestamp. This value is used as the base in the calculation.
     *
     * TIMESTAMPDIFF(interval, timestamp1, timestamp2)
     * Where:
     * interval is the specified interval. Valid values are:
     *  SQL_TSI_SECOND
     *  SQL_TSI_MINUTE
     *  SQL_TSI_HOUR
     *  SQL_TSI_DAY
     *  SQL_TSI_WEEK
     *  SQL_TSI_MONTH
     *  SQL_TSI_QUARTER
     *  SQL_TSI_YEAR
     *
     *  timestamp1 and timestamp2 are any valid timestamps.
     */
    if(targetSQLType == SQLType.BI)
    {
      // Parser only allows addition and subtraction on timestamp and interval
      // datatypes.
      assert this.getOper() == ArithOp.ADD ||
             this.getOper() == ArithOp.SUB;
      
      String transformedExpr = null;
      if(this.getOper() == ArithOp.ADD)
      {
        transformedExpr = checkAndTransformDateTimeAddition(left, right, ec);
      }
      else if(this.getOper() == ArithOp.SUB)
      {
        transformedExpr = checkAndTransformDateTimeSubtraction(left, right, ec);
      }
      
      if(transformedExpr != null)
        return transformedExpr;
    }
    
    regExpression.append(" (");
    if(right != null)
    {
      // The expression can either be complex or a base expression
      String temp = left.getSQLEquivalent(ec);
      if(temp == null)
        return temp;
      regExpression.append(temp);
      
      regExpression.append(" "+this.getOper().getExpression());
      
      temp = right.getSQLEquivalent(ec);
      if(temp == null)
        return null;

      // The expression can either be complex or a base expression
      regExpression.append(temp);
    }
    else
    {
      // Unary operators
      regExpression.append(this.getOper().getExpression());

      String temp = left.getSQLEquivalent(ec);
      if(temp == null)
        return null;
      // The expression can either be complex or a base expression
      regExpression.append(temp);
    }
    
    regExpression.append(") ");

    return regExpression.toString();
  }
  
  /**
   * A Method to transform the arithmetic addition expression for datetime type.
   * This method will handle the following cases.
   * Case 1: timestamp + interval is transformed to TIMESTAMPADD(...)
   * Case 2: interval + timestamp is transformed to TIMESTAMPADD(...)
   * Case 3: interval + interval  is regular arithmetic addition on long type
   * @param left
   * @param right
   * @param ec
   * @return
   */
  private String checkAndTransformDateTimeAddition(Expr left, Expr right, 
                                           ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
    regExpression.append(" (");
    
    // CASE-2 
    // interval + timestamp => TIMESTAMPADD()
    // This case is similar to CASE 1 except that left and right expr types are
    // exchanged; so we will call the method again with reversed params
    if(right.getType() == Datatype.TIMESTAMP)
    {
      // timestamp + timestamp is not supported
      assert left.getType() != Datatype.TIMESTAMP;
      return checkAndTransformDateTimeAddition(right, left, ec);
    }
    
    // CASE 3
    // interval + interval shouldn't be supported in BI mode.
    if((left.getType() == Datatype.INTERVAL ||
        left.getType() == Datatype.INTERVALYM) &&
        (right.getType() == Datatype.INTERVAL ||
         right.getType() == Datatype.INTERVALYM))
    {
      // Code shouldn't reach here as the semantic check will fail the query.
      assert false;
      return null;
    }
    
    // CASE-1
    // timestamp + interval => TIMESTAMPADD(...)
    if((left.getType() == Datatype.TIMESTAMP) 
        && (right.getType() == Datatype.INTERVAL
            || right.getType() == Datatype.INTERVALYM))
    {
      // If the interval expression is CONSTANT, then convert the value
      // into number of seconds
      if(right.kind == ExprKind.CONST_VAL)
      {
        ExprInterval constRightExpr = (ExprInterval)right;
        if(right.getType() == Datatype.INTERVAL)
        {
          long numNanos = constRightExpr.getVValue();
          long numSecs = numNanos/IntervalConverter.getDToSMultiplicationFactor(
                                  TimeUnit.SECOND);
          regExpression.append("TIMESTAMPADD(SQL_TSI_SECOND, " + 
                                             numSecs + ", " +
                                             left.getSQLEquivalent(ec) + ")");
        }
        else if(right.getType() == Datatype.INTERVALYM)
        {
          long numMonths = constRightExpr.getVValue();
          regExpression.append("TIMESTAMPADD(SQL_TSI_MONTH, " + 
                                             numMonths + ", " + 
                                             left.getSQLEquivalent(ec) + ")");
        }             
      }
      else
      {
        if(right.getType() == Datatype.INTERVAL)
        {
          // Assumption: The INTERVAL should be mentioned in the unit of SECOND
          regExpression.append("TIMESTAMPADD(SQL_TSI_SECOND, " + 
              right.getSQLEquivalent(ec) + " ,"
            + left.getSQLEquivalent(ec) + ")");
        }
        else if(right.getType() == Datatype.INTERVALYM)
        {
          // Assumption: The INTERVAL should be mentioned in the unit of MONTH
          regExpression.append("TIMESTAMPADD(SQL_TSI_MONTH, " + 
                                             right.getSQLEquivalent(ec) + " ,"
                                           + left.getSQLEquivalent(ec) + ")");
        }           
       
      }
      regExpression.append(") ");
      return regExpression.toString();
    }
    // Return Null if the addition is not a datetime addition.    
    return null;
  }
  
  /**
   * A Method to transform the arithmetic addition expression for datetime type.
   * This method will handle the following cases.
   * Case 4: timestamp - timestamp is transformed to TIMESTAMPDIFF(...)
   * Case 5: timestamp - interval  is transformed to TIMESTAMPADD(...)
   * Case 6: interval - interval  is regular arithmetic addition on long type
   * @param left
   * @param right
   * @param ec
   * @return
   */
  private String checkAndTransformDateTimeSubtraction(Expr left, Expr right,
                                              ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
    regExpression.append(" (");
    
    // CASE-6
    // interval - interval => NOT SUPPORTED by parser
    if(left.getType() == Datatype.INTERVAL ||
       left.getType() == Datatype.INTERVALYM)
    {
      // Code shouldn't reach here as the semantic check fails the query.
      assert false;
      return null;      
    }
    
    // CASE 5
    // timestamp - interval => TIMESTAMPADD(..) where timestamp will be added
    // to a NEGATIVE interval.
    if((left.getType() == Datatype.TIMESTAMP) && 
        (right.getType() == Datatype.INTERVAL || 
         right.getType() == Datatype.INTERVALYM))
    {
      // If the interval expression is CONSTANT, then convert the value
      // into number of seconds
      if(right.kind == ExprKind.CONST_VAL)
      {
        ExprInterval constRightExpr = (ExprInterval)right;
        if(right.getType() == Datatype.INTERVAL)
        {
          long numNanos = constRightExpr.getVValue();
          long numSecs = numNanos/IntervalConverter.getDToSMultiplicationFactor(
                                  TimeUnit.SECOND);
          // timestamp - interval is equivalent to timestamp+(-interval)
          regExpression.append("TIMESTAMPADD(SQL_TSI_SECOND, -(" + 
                                             numSecs + "), " +
                                             left.getSQLEquivalent(ec) + ")");
        }
        else if(right.getType() == Datatype.INTERVALYM)
        {
          long numMonths = constRightExpr.getVValue();
          regExpression.append("TIMESTAMPADD(SQL_TSI_MONTH, -(" + 
                                             numMonths + "), " + 
                                             left.getSQLEquivalent(ec) + ")");
        }             
      }
      else
      {
        // Assumption: The INTERVAL should be mentioned in the unit of SECOND
        regExpression.append("TIMESTAMPADD(SQL_TSI_SECOND, -(" + 
                                           right.getSQLEquivalent(ec) + ") ,"
                                         + left.getSQLEquivalent(ec) + ")");
      }
      regExpression.append(") ");
      return regExpression.toString();
    }
    
    // CASE-4
    // timestamp - timestamp => TIMESTAMPDIFF()
    if(left.getType()== Datatype.TIMESTAMP && 
       right.getType() == Datatype.TIMESTAMP)
    {
      // Result should be in nanoseconds
      regExpression.append("TIMESTAMPDIFF(SQL_TSI_SECOND, " + 
                                          left.getSQLEquivalent(ec) + ", " +
                                          right.getSQLEquivalent(ec) + ") " +
                                          " * 1000000000 ");
      regExpression.append(") ");
      return regExpression.toString();
    }
        
    return null;
  }

}
