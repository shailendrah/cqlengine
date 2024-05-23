/* $Header: PatternQuantifier.java5369 05-jan-2007.01:37:16 anasrini Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Enumeration of the supported pattern quantifiers

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    01/05/07 - Creation
    anasrini    01/05/07 - Creation
 */

/**
 *  @version $Header: PatternQuantifier.java5369 05-jan-2007.01:37:16 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.pattern;

/**
 * Enumeration of the supported pattern quantifiers
 *
 * @since 1.0
 */

public enum PatternQuantifier {
  GREEDY_STAR, GREEDY_PLUS, GREEDY_QUESTION;
}


