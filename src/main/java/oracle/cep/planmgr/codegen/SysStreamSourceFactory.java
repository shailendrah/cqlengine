/* $Header: SysStreamSourceFactory.java 04-dec-2006.05:43:28 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares SysStreamSourceFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    najain    12/04/06 - stores are not storage allocators
    hopark    11/09/06 - bug 5465978 : refactor newExecOpt
    najain    06/18/06 - cleanup
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: SysStreamSourceFactory.java 04-dec-2006.05:43:28 najain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.planmgr.codegen;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;

/**
 * SysStreamSourceFactory
 *
 * @author skaluska
 */
public class SysStreamSourceFactory extends ExecOptFactory
{

    /**
   * Constructor for SysStreamSourceFactory
   */
    public SysStreamSourceFactory()
    {
        // TODO Auto-generated constructor stub
        super();

    }

    /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.planmgr.codegen.CodeGenContext)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
    }
}
