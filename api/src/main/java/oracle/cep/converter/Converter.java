package oracle.cep.converter;

/**
 * Converter extends the ConverterFunction interface with explicit type information
 * which is kept beyond java type erasure, and so it allows to use the converter in a
 * declarative context, such as specifying converter in XML.
 *
 * ConverterFunction can be turned into a Converter by using the Converters.asConverter() method.
 *
 * Created by ggeiszte on 9/21/17.
 */
public interface Converter<F,T> extends ConverterFunction<F,T> {

    Class<F> getFromType();

    Class<T> getToType();
}
