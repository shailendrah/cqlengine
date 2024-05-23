/* $Header: SemQueryInterpreterFactory.java 13-apr-2006.17:26:19 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      03/01/06 - Creation
 */

/**
 *  @version $Header: SemQueryInterpreterFactory.java 13-apr-2006.17:26:19 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.factory;

import java.util.HashMap;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogicalPlanException;

/**
 * Factory for the parse tree node specific interpreters
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

public class SemQueryInterpreterFactory {

  private static HashMap<Boolean, LogOptFactory> interpMap;

  static {
    populateInterpMap();
  }

  static void populateInterpMap() {
    interpMap = new HashMap<Boolean, LogOptFactory>();

    interpMap.put(new Boolean(true), new LogOptStrmSrcFactory());
    interpMap.put(new Boolean(false), new LogOptRelnSrcFactory());
  }

  public static LogOpt getInterpreter(boolean bool, SemQueryInterpreterFactoryContext ctx) throws LogicalPlanException {

    LogOptFactory o = interpMap.get(new Boolean(bool));
    assert o != null;

    return o.newLogOpt(ctx);
  }
}

