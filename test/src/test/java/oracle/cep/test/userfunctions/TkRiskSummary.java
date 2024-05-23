/* (c) 2006-2009 Oracle.  All rights reserved. */
package oracle.cep.test.userfunctions;

import java.io.Serializable;

public class TkRiskSummary implements Serializable, Cloneable
{
  private String total;

  public TkRiskSummary(Object o)
  {
    total = o.toString();
  }

  public String getTotal() 
  {
    return total;
  }

  public void setTotal (String total) 
  {
    this.total = total;
  }

  public String toString()
  {
    return "Total Risk Summary:= " + total;
  }

  public void add(TkRiskSummary param)
  {
    Float fTotal = Float.valueOf(total);
    Float fParamTotal = Float.valueOf(param.getTotal());
    long newTotal = fTotal.longValue() + fParamTotal.longValue();
    total = String.valueOf(newTotal);
  }

  public Object clone()
  {
    // this.total is String; so immutable
    TkRiskSummary cloned = new TkRiskSummary(this.total);
    return cloned;
  }
}

