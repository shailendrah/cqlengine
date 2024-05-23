/* $Header: PhyPlanAttrFactory.java 05-mar-2007.05:59:41 rkomurav Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Factory producing a physical representation of an attribute by 
    transforming from a logical representation

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    03/05/07 - make this as base class
    rkomurav    10/05/06 - remove newAttr method
    anasrini    05/03/06 - Creation
    anasrini    05/03/06 - Creation
    anasrini    05/03/06 - Creation
 */

/**
 *  @version $Header: PhyPlanAttrFactory.java 05-mar-2007.05:59:41 rkomurav Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.attr.Attr;

public abstract class PhyPlanAttrFactory
{
  public abstract Attr newAttr(LogOpt logop, 
                             oracle.cep.logplan.attr.Attr logattr);
}
