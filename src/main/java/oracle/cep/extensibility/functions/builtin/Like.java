/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Like.java /main/1 2012/09/14 08:11:45 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/11/12 - Creation
 */

package oracle.cep.extensibility.functions.builtin;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Like.java /main/1 2012/09/14 08:11:45 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class Like implements SingleElementFunction
{

  @Override
  public Object execute(Object[] args) throws UDFException
  {
    // There should always be two parameters in the lk() function call.
    // Parser ensures this check
    assert args.length == 2;
    
    // If any argument is null; then the result is unknown.
    // Hence returning false;
    if(args[0] == null || args[1] == null)
      return new Boolean(false);
    
    // If both arguments are non-null;
    // Here arg[0] is search value and
    // arg[1] is pattern.
    
    // Compile the pattern
    Pattern pattern = Pattern.compile((String)args[1]);
    
    // Construct the matcher
    Matcher matcher = pattern.matcher((String)args[0]);
    
    // Always scan from first position of input sequence
    Boolean isLike = matcher.find(0);
    return isLike;
  }
  
}
