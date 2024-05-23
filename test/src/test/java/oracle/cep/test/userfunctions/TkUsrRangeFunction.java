package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;


public class TkUsrRangeFunction
    implements SingleElementFunction
{

    public Object execute(Object args[])
        throws UDFException
    {
        int arg = ((Integer)args[0]).intValue();
        return new Integer(arg);
    }

    public TkUsrRangeFunction()
    {
    }
}

