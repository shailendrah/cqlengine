/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/StatsRow.java /main/2 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/09/13 - enabling jmx framework
    parujain    10/05/07 - ordering
    parujain    09/12/07 - remove logging
    najain      10/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/StatsRow.java /main/2 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jmx.stats;

import java.sql.Types;

/**
 * A statistic row. Every statistic needs to implement the statsRowInterface.
 * To draw a analogy with the RDBMS, a statsRow corresponds to 
 * a row in a x$ fixed table, where the interface is the row source definition
 *
 * @since 1.0
 */

public abstract class StatsRow {
/*
  public int compareTo(StatsRow o2, Column c, int type)
  {
    switch(type)
    {
      case Types.INTEGER:
          int a = getIntValue(c);
          int b = o2.getIntValue(c);
          if(a > b)
            return 1;
          else if(a == b)
            return 0;
          else 
            return -1;
          
      case Types.BIGINT:
    	  long l1 = getLongValue(c);
    	  long l2 = o2.getLongValue(c);
    	  if(l1 > l2)
    	    return 1;
    	  else if(l1 == l2) 
    	    return 0;
    	  else
    	    return -1;
    	  
      case Types.CHAR:
    	  String s1 = getStringValue(c);
    	  String s2 = o2.getStringValue(c);
    	  return (s1.compareToIgnoreCase(s2));
    	  
      case Types.FLOAT:
    	  float f1 = getFloatValue(c);
    	  float f2 = o2.getFloatValue(c);
    	  if(f1 > f2)
      	    return 1;
      	  else if(f1 == f2) 
      	    return 0;
      	  else
      	    return -1;
      	 
      case Types.BOOLEAN:
    	  boolean b1 = getBooleanValue(c);
    	  boolean b2 = o2.getBooleanValue(c);
    	  if(b1 && b2)
    	    return 0;
    	  else if(b1)
    		return 1;
    	  else
    	    return -1;
    	  
      default:
    	  return -1;
         
    		
    }
  }
  
  public boolean getBooleanValue(Column c) {
		return false;
	}

	
  public float getFloatValue(Column c) {
    return Float.MIN_VALUE;
  }
	
  public int getIntValue(Column c) {
    return Integer.MIN_VALUE;
  }


  public long getLongValue(Column c) {
    return Long.MIN_VALUE;
  }

  public String getStringValue(Column c) {
    return null;
  }
 */
}

