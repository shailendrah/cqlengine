/* $Header: BindingComparator.java 22-oct-2007.17:31:35 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/22/07 - remove TimeStamp
    rkomurav    08/07/07 - Creation
 */

/**
 *  @version $Header: BindingComparator.java 22-oct-2007.17:31:35 hopark Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.comparator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import oracle.cep.execution.pattern.Binding;

public class BindingComparator implements Comparator<Binding>, Externalizable {
    private static final long serialVersionUID = -1488930546554235335L;

    public BindingComparator() {
    }

    public int compare(Binding b1, Binding b2) {
        long ts1 = b1.getMatchedTs();
        long ts2 = b2.getMatchedTs();

        if (ts1 < ts2)
            return -1;

        else if (ts1 == ts2)
            return 0;

        else
            return 1;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
    }
}