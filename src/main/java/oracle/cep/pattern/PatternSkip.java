/* $Header: pcbpel/cep/server/src/oracle/cep/pattern/PatternSkip.java /main/2 2009/02/19 11:21:29 skmishra Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of valid SKIP clause options

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     02/05/09 - adding toString
    rkomurav    10/18/07 - 
    anasrini    09/24/07 - SKIP clause
    anasrini    09/24/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/pattern/PatternSkip.java /main/2 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.pattern;

/**
 * Enumeration of the supported SKIP clause options
 *
 * @since 1.0
 */

public enum PatternSkip {
  SKIP_PAST_LAST_ROW,
  ALL_MATCHES;
  
  public String toString()
  {
    switch(this)
    {
    case SKIP_PAST_LAST_ROW:
      return "";
    case ALL_MATCHES:
      return " ALL MATCHES ";
    }
    
    //never come here!
    assert(false);
    return "";
  }
  
  public String toVisualizerString()
  {
    switch(this)
    {
    case SKIP_PAST_LAST_ROW:
      return "DEFAULT";
    case ALL_MATCHES:
      return " ALL MATCHES ";
    }
    //never come here!
    assert(false);
    return "";
  }
}

