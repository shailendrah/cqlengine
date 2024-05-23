package oracle.cep.converter;

/**
 * Dedicated excpetion type of the converter API.
 *
 * Created by ggeiszte on 8/2/17.
 */
public class ConverterException extends RuntimeException {
    public ConverterException() {
    }

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConverterException(Throwable cause) {
        super(cause);
    }
}
