/* $Header: SymbolTableSourceType.java 27-may-2007.23:50:26 rkomurav Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Enumeration of the types for the symbol table entry of type SOURCE

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    05/27/07 - 
    anasrini    05/18/07 - Creation
    anasrini    05/18/07 - Creation
 */

/**
 *  @version $Header: SymbolTableSourceType.java 27-may-2007.23:50:26 rkomurav Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

/**
 * Enumeration of the SOURCE type
 */

public enum SymbolTableSourceType {
  
  PERSISTENT,
  INLINE_VIEW;
}

