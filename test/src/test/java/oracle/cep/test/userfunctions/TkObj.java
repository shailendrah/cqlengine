/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkObj.java /main/2 2009/09/13 23:57:27 sbishnoi Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/13/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkObj.java /main/2 2009/09/13 23:57:27 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import java.io.Serializable;

public strictfp class TkObj implements Serializable 
{
    private static final long serialVersionUID = 6658343275765080565L;

    int ival;
    long lval;
    float fval;
    double dval;
    String sval;
    
    public TkObj()
    {
        ival = 0;
        lval = 0;
        fval = 0;
        dval = 0;
        sval = "unknown";
    }
    
    public int getIVal() {return ival;}
    public long getLVal() {return lval;}
    public float getFVal() {return fval;}
    public double getDVal() {return dval;}
    public String getSVal() {return sval;}
    
    public void setIVal(int v) {ival = v;}
    public void setLVal(long v) {lval = v;}
    public void setFVal(float v) {fval = v;}
    public void setDVal(double v) {dval = v;}
    public void setSVal(String v) {sval = v;}
    
    public String toString()
    {
      StringBuilder b = new StringBuilder();
      b.append("i="); b.append(ival); b.append(", ");
      b.append("l="); b.append(lval); b.append(", ");
      b.append("f="); b.append(fval); b.append(", ");
      b.append("d="); b.append(dval); b.append(", ");
      b.append("s="); b.append(sval);
      return b.toString();
    }
}
