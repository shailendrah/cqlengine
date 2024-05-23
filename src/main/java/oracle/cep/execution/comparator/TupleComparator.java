/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/comparator/TupleComparator.java /main/10 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/30/09 - support for bigdecimal
    hopark      02/17/09 - support boolean as external datatype
    udeshmuk    11/02/08 - change comment in compare
    skmishra    08/11/08 - adding getter for compSpec
    skmishra    07/18/08 - compareTuples public for xmlagg
    udeshmuk    01/31/08 - support for double data type.
    hopark      12/06/07 - cleanup spill
    hopark      07/13/07 - dump stack trace on exception
    parujain    06/29/07 - Tuple Comparator
    parujain    06/29/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/comparator/TupleComparator.java /main/10 2009/11/09 10:10:58 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.comparator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;

public class TupleComparator implements Comparator<ITuplePtr>, Externalizable {
    private static final long serialVersionUID = 3448730365852996394L;

    ComparatorSpecs[] specs;

    TupleSpec ts;

    public TupleComparator() {
    }

    public TupleComparator(ComparatorSpecs[] cspec, TupleSpec spec) {
        this.specs = cspec;
        this.ts = spec;
    }

    public int compare(ITuplePtr o1, ITuplePtr o2) {
        try {
            ITuple t1 = o1.pinTuple(IPinnable.READ);
            ITuple t2 = o2.pinTuple(IPinnable.READ);
            if (t1.isBNull() ^ t2.isBNull()) {
                int isnull = specs[0].isNullsFirst ? 1 : -1;
                // if t1 is null and nulls should come first then return 1
                // if t2 is null and nulls should come first then return -1
                if (t1.isBNull())
                    return isnull;
                else if (t2.isBNull())
                    return -1 * isnull;

            } else if (t1.isBNull() && t2.isBNull())
                return 0;

            // both tuples are not null
            int retVal = compareTuples(t1, t2);
            return retVal;
        } catch (ExecException e) {
            LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        } finally {
            try {
                o1.unpinTuple();
                o2.unpinTuple();
            } catch (ExecException e) {
                LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
            }
        }
        return 0;
    }

    public int compareTuples(ITuple t1, ITuple t2) throws ExecException {
        for (int i = 0; i < specs.length; i++) {
            int ret = specs[i].isAscending ? 1 : -1;
            int isnull = specs[i].isNullsFirst ? -1 : 1;

            // if one of the attr is null
            if (t1.isAttrNull(specs[i].colNum) ^ t2.isAttrNull(specs[i].colNum)) {
                if (t1.isAttrNull(specs[i].colNum))
                    return isnull;
                else if (t2.isAttrNull(specs[i].colNum))
                    return -1 * isnull;
            }
            // if both are not null
            else if (!(t1.isAttrNull(specs[i].colNum) && t2
                    .isAttrNull(specs[i].colNum))) {
                int comp = compare(t1, t2, specs[i].colNum);
                // if a1>a2 and is descending then return -1 so that a2 should
                // come after a1.
                if (comp != 0)
                    return comp * ret;
            }
            // if both attrs are null the go to next attrval
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private int compare(ITuple t1, ITuple t2, int pos) throws ExecException {
        int comp = 0;
        switch (ts.getAttrType(pos).getKind()) {
        case INT:
            comp = Comparison.intCompare(t1.iValueGet(pos), t2.iValueGet(pos));
            break;
        case FLOAT:
            comp = Comparison
                    .floatCompare(t1.fValueGet(pos), t2.fValueGet(pos));
            break;
        case DOUBLE:
            comp = Comparison.doubleCompare(t1.dValueGet(pos),
                    t2.dValueGet(pos));
            break;
        case BIGDECIMAL:
            comp = Comparison.bigDecimalCompare(t1.nValueGet(pos),
                    t2.nValueGet(pos));
            break;
        case BIGINT:
            comp = Comparison.bigintCompare(t1.lValueGet(pos),
                    t2.lValueGet(pos));
            break;
        case CHAR:
            comp = Comparison.charCompare(t1.cValueGet(pos),
                    t1.cLengthGet(pos), t2.cValueGet(pos), t2.cLengthGet(pos));
            break;
        case TIMESTAMP:
            comp = Comparison.timestampCompare(t1.tValueGet(pos),
                    t2.tValueGet(pos));
            break;
        case BYTE:
            comp = Comparison.byteCompare(t1.bValueGet(pos),
                    t1.bLengthGet(pos), t2.bValueGet(pos), t2.bLengthGet(pos));
            break;
        case INTERVAL:
            comp = Comparison.intervalCompare(t1.vValueGet(pos),
                    t2.vValueGet(pos));
            break;
        case BOOLEAN:
            comp = (t1.boolValueGet(pos) == t2.boolValueGet(pos)) ? 0 : -1;
            break;
        case OBJECT:
            Object o1 = t1.oValueGet(pos);
            Object o2 = t2.oValueGet(pos);

            // FIXME provide IType.isComparable
            if (o1 instanceof Comparable) {
                Comparable co1 = (Comparable) o1;
                return co1.compareTo(o2);
            } else {
                assert false; // this should have been verified while compiling.
            }
            break;
        default:
            assert false;
        }
        return comp;
    }

    public ComparatorSpecs[] getSpecs() {
        return specs;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(specs);
        out.writeObject(ts);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        specs = (ComparatorSpecs[]) in.readObject();
        ts = (TupleSpec) in.readObject();
    }
}
