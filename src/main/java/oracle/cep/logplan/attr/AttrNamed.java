/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrNamed.java /main/4 2012/05/02 03:05:58 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Named Attribute definitions used by logical operators

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali       04/03/12 - added datatype as arg
 anasrini    05/25/07 - inline view support - remove tableid
 rkomurav    03/05/07 - modify equals for corrattr comparision
 najain      05/30/06 - add check_reference 
 najain      05/26/06 - add isSame 
 anasrini    03/23/06 - minor fix in toString 
 najain      02/14/06 - add constructor etc. 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrNamed.java /main/4 2012/05/02 03:05:58 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.attr;

import oracle.cep.logplan.LogOpt;
import oracle.cep.common.Datatype;

/**
 * Named Attribute Class definitions used by logical operators
 */
public class AttrNamed extends Attr
{
  /** Variable Identifier */
  private int varId;

  /** Attribute Identifier */
  private int attrId;

  public AttrNamed(Datatype dt)
  {
    this.dt  = dt;
    attrKind = AttrKind.NAMED;
  }
  
  public AttrNamed(int varId, int attrId, Datatype dt)
  {
    this.varId  = varId;
    this.attrId = attrId;
    attrKind    = AttrKind.NAMED;
    this.dt     = dt;
  }

  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;
    
    //compare only table and attr ids with corrAttr
    if (otherObject instanceof CorrAttr)
    {
      CorrAttr corr = (CorrAttr) otherObject;
      return (attrId == corr.getAttrId() &&
              varId == corr.getBaseEntityVarId()
             );
    }

    if (getClass() != otherObject.getClass())
      return false;

    AttrNamed other = (AttrNamed) otherObject;

    return ((varId == other.varId) && (attrId == other.attrId));
  }

  public int getAttrId()
  {
    return attrId;
  }

  public int getVarId()
  {
    return varId;
  }

  public void setVarId(int varId)
  {
    this.varId = varId;
  }

  public void setAttrId(int attrId)
  {
    this.attrId = attrId;
  }

  public void setAttrNamedFields(int varId, int attrId)
  {
    this.varId = varId;
    this.attrId = attrId;
  }

  public boolean isSame(Attr input)
  {
    if (!super.isSame(input))
      return false;

    AttrNamed inp = (AttrNamed) input;

    if ((varId == inp.getVarId()) && (attrId == inp.getAttrId()))
      return true;

    return false;
  }

  public boolean check_reference(LogOpt op)
  {
    for (int i = 0; i < op.getNumOutAttrs(); i++)
    {
      Attr attr = op.getOutAttr(i);
      if (attr instanceof AttrNamed)
      {
        AttrNamed attrNam = (AttrNamed) attr;

        if ((varId == attrNam.getVarId()) && (attrId == attrNam.getAttrId()))
          return true;
      }
    }

    return false;
  }

  // toString method override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<NamedAttribute>");

    sb.append(super.toString());

    sb.append("<Ids varId=\"" + varId + "\" attrId=\"" + attrId + "\" />");

    sb.append("</NamedAttribute>");
    return sb.toString();
  }
}
