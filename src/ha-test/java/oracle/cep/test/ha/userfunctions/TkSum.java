package oracle.cep.test.ha.userfunctions;

import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.AggrFloat;

public class TkSum extends AggrFunctionImpl implements IAggrFnFactory, Cloneable {
    private static final long serialVersionUID = 1567597542478045115L;
    
    float sum;

    public TkSum()
    {}
    
    public IAggrFunction newAggrFunctionHandler() throws UDAException {
        return new TkSum();
    }

    public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
    }

    public void initialize() throws UDAException {
        sum = 0.0f;
    }

    public void handlePlus(AggrValue[] value, 
                           AggrValue result) throws UDAException {
        if (!value[0].isNull()) {
            sum += ((AggrFloat)(value[0])).getValue();
        }
        ((AggrFloat)result).setValue(sum);
    }

    public void handleMinus(AggrValue[] value, 
                            AggrValue result) throws UDAException {
        if (!value[0].isNull()) {
            sum -= ((AggrFloat)(value[0])).getValue();
        }
        ((AggrFloat)result).setValue(sum);
    }
    
    public Object clone()
    {
      TkSum cloned = new TkSum();
      cloned.sum = this.sum;
      return cloned;
    }
}
