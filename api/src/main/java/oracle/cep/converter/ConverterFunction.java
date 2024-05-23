package oracle.cep.converter;

import java.io.Serializable;

/**
 * ConverterFunction is the core interface of the converter API which
 * encapsulates the functionality that converts from type F to type T.
 *
 * ConverterFunction is similar to java.util.function.Function, except it is
 * Serializable by definition, and the function can throw a ConverterException.
 *
 *
 * Created by ggeiszte on 8/2/17.
 *
 * @param <F> the type this converter converts from
 * @param <T> the type this converter converts to
 */
@FunctionalInterface  public interface ConverterFunction<F,T> extends Serializable {

    /**
     * Performs the conversion from type F into type T.
     *
     * @param fromData
     * @return
     * @throws ConverterException
     */
    T convert(F fromData) throws ConverterException;

    /**
     * Chains the specified converter to this, where the chain converts from F to N through T
     *
     * @param c the converter to be chained, which converts from T to N
     * @param <N> the "to" type of the chain
     * @return the new converter which converts from F to N through T
     */
    default <N> ConverterFunction<F, N> chain(ConverterFunction<? super T, N> c) {
        return x -> c.convert(convert(x));
    }
}
