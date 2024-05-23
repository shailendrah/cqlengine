package oracle.cep.extensibility.datasource;

import java.io.Serializable;
import java.util.Iterator;

import oracle.cep.dataStructures.external.TupleValue;

/**
 * @author htg
 * Sample converter contract, Any data converters should implement this interface
 * TODO: This may be replaced with actual converter interface once adapter feature is implemented
 *
 */
public interface IExternalConverter extends Serializable{
	public Iterator<TupleValue> toTupleList(Object data);
}
