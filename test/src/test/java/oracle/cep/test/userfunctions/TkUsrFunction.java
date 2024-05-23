package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;


public class TkUsrFunction
    implements SingleElementFunction
{

    public Object execute(Object args[])
        throws UDFException
    {
        float arg = ((Float)args[0]).floatValue();
        return new Float(arg * 2.0F);
    }

    public TkUsrFunction()
    {
    }
}

