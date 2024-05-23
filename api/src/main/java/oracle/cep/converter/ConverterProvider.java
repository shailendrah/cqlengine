package oracle.cep.converter;

import java.util.Map;

/**
 * Converter provider creates a specific converter type from context and from
 * configuration parameters. This interface is primarily used by ConverterRegistry.
 *
 * Created by ggeiszte on 11/22/17.
 *
 * @see ConverterRegistry
 *
 */
public interface ConverterProvider {
    Converter get(Map<String, Object> params);
}
