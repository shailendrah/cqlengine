/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/pattern/CorrName.java /main/2 2008/11/07 23:08:44 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/16/08 - support for xmlagg orderby in pattern.
    rkomurav    03/19/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/pattern/CorrName.java /main/2 2008/11/07 23:08:44 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.pattern;

import java.util.ArrayList;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;

public abstract class CorrName
{
  /** position in the binding */
  int                   bindPos;

  /** aggregate functions whose param references this correlation */
  BaseAggrFn[]          aggrFns;

  /** aggregate function params that reference this correlation */
  ArrayList<Expr[]>     aggrParamExprs;
  
  /** orderbyExprs that may be present in the xmlagg function that may be referencing this corrname */
  ArrayList<ExprOrderBy[]> orderByExprs;

  /** Function code for the aggregate functions */
  AggrFunction[]        aggrFnCodes;

  /** Input datatypes to the aggreagtions */
  ArrayList<Datatype[]> aggrInputTypes;

  /** Output datatypes of the aggregations */
  Datatype[]            aggrOutputTypes;
  
  /**
   * Constructor
   * @param bindPos        position in the binding
   * @param aggrFns        aggregate functions
   * @param aggrParamExprs aggregate functions' parameters
   * @param orderExprs     order by expressions
   */
  public CorrName(int bindPos, BaseAggrFn[] aggrFns,
    ArrayList<Expr[]> aggrParamExprs, ArrayList<ExprOrderBy[]> orderExprs)
  {
    this.bindPos        = bindPos;
    this.aggrFns        = aggrFns;
    this.aggrParamExprs = aggrParamExprs;
    this.orderByExprs   = orderExprs;
    
    int numAggrs    = getNumAggrs();
    aggrInputTypes  = new ArrayList<Datatype[]>();
    aggrOutputTypes = new Datatype[numAggrs];
    aggrFnCodes     = new AggrFunction[numAggrs];
    
    Expr[] phyExprs;
    Datatype[] inpTypes;
    for (int i=0; i<numAggrs; i++)
    {
      phyExprs = aggrParamExprs.get(i);
      inpTypes = new Datatype[phyExprs.length];
      for(int j = 0; j < phyExprs.length; j++)
      {
        inpTypes[j] = phyExprs[j].getType();
        aggrOutputTypes[i] = aggrFns[i].getReturnType(inpTypes[j]);
      }
      aggrInputTypes.add(inpTypes);
      aggrFnCodes[i]     = aggrFns[i].getFnCode();
    }
  }
  
  /**
   * @return the bindPos
   */
  public int getBindPos()
  {
    return bindPos;
  }

  /**
   * @return the aggregate functions whose param references this correlation
   */
  public BaseAggrFn[] getAggrFns()
  {
    return aggrFns;
  }

  /**
   * @return the aggregate param expressions that reference this correlation
   */
  public ArrayList<Expr[]> getAggrParamExprs()
  {
    return aggrParamExprs;
  }

  /**
   * Getter for orderByExprs
   * @return the orderBy expressions that may be present in the xmlagg
   *         function that might be referencing this corrname
   */
  public ArrayList<ExprOrderBy[]> getOrderByExprs()
  {
    return orderByExprs;
  }
  
  /**
   * @return the number of aggregate functions whose param references
   *         this correlation
   */
  public int getNumAggrs()
  {
    if (aggrFns == null)
      return 0;

    return aggrFns.length;
  }

  /**
   * @return array of input types to the aggreagte functions
   */
  public ArrayList<Datatype[]> getAggrInputTypes()
  {
    return aggrInputTypes;
  }

  /**
   * @return array of return types of the aggreagte functions
   */
  public Datatype[] getAggrOutputTypes()
  {
    return aggrOutputTypes;
  }

  /**
   * @return the function code of the aggregate functions
   */
  public AggrFunction[] getAggrFunctions()
  {
    return aggrFnCodes;
  }
  
  public boolean equals(CorrName other)
  {
    if(other == null)
      return false;
    
    if(other.getBindPos() != bindPos)
      return false;
    
    if (aggrFns != null)
    {
      if (other.aggrFns == null)
        return false;

      int numAggrs = aggrFns.length;
      if (other.aggrFns.length != numAggrs)
        return false;

      Expr[] phyExprs;
      Expr[] otherExprs;
      ExprOrderBy[] phyOrderExprs;
      ExprOrderBy[] otherOrderExprs;
      for (int i=0; i<numAggrs; i++) 
      {
        if(!aggrFns[i].equals(other.aggrFns[i]))
          return false;
        
        phyExprs   = aggrParamExprs.get(i);
        otherExprs = other.aggrParamExprs.get(i);
        
        if(phyExprs.length != otherExprs.length)
          return false;
        for(int j = 0; j < phyExprs.length; j++)
        {
          if(!phyExprs[j].equals(otherExprs[j]))
            return false;
        }
        
        //Compare orderByExprs
        phyOrderExprs   = orderByExprs.get(i);
        otherOrderExprs = other.orderByExprs.get(i);
        if(((phyOrderExprs != null) && (otherOrderExprs == null)) ||
            ((phyOrderExprs == null) && (otherOrderExprs != null)))
          return false;
        else if((phyOrderExprs == null) && (otherOrderExprs == null))
          return true;
        else 
        { //phyOrderExprs and otherOrderExprs both are non-null 
          if(phyOrderExprs.length != otherOrderExprs.length)
            return false;
          //compare corresponding expressions
          for(int j=0; j < phyOrderExprs.length; j++)
          {
            if(!phyOrderExprs[j].equals(otherOrderExprs[j]))
              return false;
          }
        }          
      }
    }
    else
    {
      if (other.aggrFns != null)
        return false;
    }
    return true;
  }
}
