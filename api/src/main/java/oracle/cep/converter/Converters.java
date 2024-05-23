package oracle.cep.converter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ggeiszte on 9/13/17.
 */
public class Converters
{
    /**
     * Creates a converter with identity transformation for type T.
     *
     * @param <T>
     * @return
     */
    public static <T> ConverterFunction<T,T> identity() {
        return x -> x;
    }

    /**
     * Creates a converter with identity transformation for type T.
     *
     * @param <T>
     * @return
     */
    public static <T> Converter<T,T> identity(Class<T> t) {
        return asConverter(t, t, identity());
    }

    /**
     * Create a null converter which transforms any value of type F into a null value of type T.
     *
     * @param <F>
     * @param <T>
     * @return
     */
    public static <F,T> ConverterFunction<F,T> toNull() {
        return x -> null;
    }

    /**
     * Create a null converter which transforms any value of type F into a null value of type T.
     *
     * @param <F>
     * @param <T>
     * @return
     */
    public static <F,T> Converter<F,T> toNull(Class<F> f, Class<T> t) {
        return asConverter(f, t, toNull());
    }

    /**
     * Creates a converter from the specified function.
     * ConverterFunction wraps the specified function as its convert method.
     *
     * @param func
     * @param <F>
     * @param <T>
     * @return
     */
    public static <F,T> ConverterFunction<F,T> function(Function<F,T> func) {
        return x -> func.apply(x);
    }

    /**
     * Creates a converter from c1 and c2, which implements the chain of transformation A->B->C.
     *
     * @param c1
     * @param c2
     * @param <A>
     * @param <B>
     * @param <C>
     * @return
     */
    public static <A,B,C> Converter<A,C> chain(Converter<A,? extends B> c1, Converter<B,C> c2) {
        return new ConverterBase<A, C>(c1.getFromType(), c2.getToType()) {
            @Override public C convert(A fromData) throws ConverterException {
                return c2.convert(c1.convert(fromData));
            }
            @Override public String toString() {
                return "[" + c1 + " => " + c2 + "]";
            }
        };
    }


    /**
     * Connects multiple converters into a chain.
     * @param items
     * @return
     * @throws ConverterException
     */
    public static Converter<?,?> chain(Converter... items) throws ConverterException {
        Converter result = null;
        for(Converter c : items) {
            if(c == null) {
                continue;    // skip nulls, try next
            } if(result == null) {
                result = c;  // initialize chain with first converter
            } else if(c.getFromType().isAssignableFrom(result.getToType())) {
                result = chain(result, c);  // chain, only if from/to types fit
            } else {
                throw new ConverterException("Incompatible types in chain. Unable to connect " + c + " to " + result);
            }
        }
        return result;
    }

    /**
     * Creates a converter wrapping the specified converter,
     * which logs exceptions coming from the wrapped converter using the specified log level.
     *
     * @param wrapped
     * @param logLevel
     * @param <F>
     * @param <T>
     * @return
     */
    @Provider
    public static <F,T> Converter<F,T> logger(
            @Param("converter") Converter<F,T> wrapped,
            @Param("logLevel") Level logLevel)
    {
        return new Converter<F, T>() {
            @Override public Class<F> getFromType() { return wrapped.getFromType(); }
            @Override public Class<T> getToType() { return wrapped.getToType(); }
            @Override public String toString() { return wrapped.toString(); }
            @Override public T convert(F fromData) throws ConverterException {
                try {
                    return wrapped.convert(fromData);
                } catch(ConverterException cex) {
                    log.log(logLevel, "Converter failed: ", cex);
                    throw cex;
                } catch(Exception ex) {
                    log.log(logLevel, "Converter failed: ", ex);
                    throw new ConverterException(ex);
                }
            }
        };
    }

    /**
     * Creates a converter wrapping the specified converter, which ignores exceptions coming from the wrapped
     * converter and returns null instead.
     *
     * @param wrapped
     * @param <F>
     * @param <T>
     * @return
     */
    @Provider
    public static <F,T> Converter<F,T> ignoreFailure(
            @Param("converter") Converter<F,T> wrapped)
    {
        return new Converter<F, T>() {
            @Override public Class<F> getFromType() { return wrapped.getFromType(); }
            @Override public Class<T> getToType() { return wrapped.getToType(); }
            @Override public String toString() { return wrapped.toString(); }
            @Override public T convert(F fromData) throws ConverterException {
                try {
                    return wrapped.convert(fromData);
                } catch(Exception ex) {
                    return null;
                }
            }
        };
    }

    /**
     * Creates a converter which calls the toString() method on objects.
     *
     * @return
     */
    @Provider
    public static Converter<Object, String> object2string() {
        return object2string(Object.class);
    }

    /**
     * Creates a converter which calls the toString() method on objects.
     *
     * @return
     */
    public static <F> Converter<F, String> object2string(Class<F> f) {
        return new ConverterBase<F, String>(f, String.class) {
            @Override public String convert(F fromData) throws ConverterException {
                return fromData != null ? fromData.toString() : null;
            }
        };
    }

    /**
     * Creates a converter which serialize a Serializable object into a byte[] using
     * standard java serialization.
     *
     * @return
     */
    @Provider
    public static Converter<Serializable, byte[]> object2bytes() {
        return object2bytes(Serializable.class);
    }

    /**
     * Creates a converter which serialize a Serializable object into a byte[] using
     * standard java serialization.
     *
     * @return
     */
    @Provider
    public static <T extends Serializable> Converter<T, byte[]> object2bytes(@Param("class") Class<T> clazz) {
        return new ConverterBase<T, byte[]>(clazz, byte[].class) {
            @Override public byte[] convert(T obj) throws ConverterException {
                if(obj == null) {
                    return null;
                }

                try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos))
                {
                    oos.writeObject(obj);
                    oos.close();
                    return bos.toByteArray();
                } catch (Exception e) {
                    throw new ConverterException(e);
                }
            }
        };
    }

    /**
     * Creates a converter which deserialize a serializable object from byte[] using
     * standard java serialization.
     *
     * @return
     */
    @Provider
    public static Converter<byte[], Serializable> bytes2object() {
        return bytes2object(Serializable.class);
    }

    /**
     * Creates a converter which deserialize a serializable object from byte[] using
     * standard java serialization.
     *
     * @return
     */
    @Provider
    public static <T extends Serializable> Converter<byte[], T> bytes2object(@Param("class") Class<T> clazz) {
        return new ConverterBase<byte[], T>(byte[].class, clazz) {
            @Override public T convert(byte[] bytes) throws ConverterException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }

                try(ByteArrayInputStream b = new ByteArrayInputStream(bytes);
                    ObjectInputStream o = new ObjectInputStream(b))
                {
                    return (T) o.readObject();
                }
                catch (Exception e)
                {
                    throw new ConverterException(e);
                }
            }
        };
    }

    /**
     * Creates a converter which transforms a String into its UTF-8 bytes.
     *
     * @return
     */
    @Provider
    public static Converter<String, byte[]> string2bytes() {
        return string2bytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates a converter which transforms a String into the byte array encoded by "encoding".
     *
     * @param encoding
     * @return
     */
    @Provider
    public static Converter<String, byte[]> string2bytes(
            @Param("encoding") Charset encoding)
    {
        String encodingName = encoding.name();
        return new ConverterBase<String, byte[]>(String.class, byte[].class) {
            @Override public byte[] convert(String fromData) throws ConverterException {
                try {
                    return fromData != null ? fromData.getBytes(encodingName) : null;
                } catch (UnsupportedEncodingException e) {
                    throw new ConverterException(e);
                }
            }
        };
    }

    /**
     * Creates a converter which transforms UTF-8 bytes into a String.
     *
     * @return
     */
    @Provider
    public static Converter<byte[], String> bytes2string() {
        return bytes2string(StandardCharsets.UTF_8);
    }

    /**
     * Creates a converter which transforms a byte array to String using the specified encoding.
     *
     * @param encoding
     * @return
     */
    @Provider
    public static Converter<byte[], String> bytes2string(
            @Param("encoding") Charset encoding)
    {
        String encodingName = encoding.name();
        return new ConverterBase<byte[], String>(byte[].class, String.class) {
            @Override public String convert(byte[] fromData) throws ConverterException {
                try {
                    return fromData != null ? new String(fromData, encodingName) : null;
                } catch (UnsupportedEncodingException e) {
                    throw new ConverterException(e);
                }
            }
        };
    }


    /**
     * Creates a converter which transforms a byte array into its base64 encoded string.
     *
     * @return
     */
    @Provider
    public static Converter<byte[], String> bytes2base64() {
        return new ConverterBase<byte[], String>(byte[].class, String.class) {
            @Override public String convert(byte[] fromData) throws ConverterException {
                return fromData != null ? Base64.getEncoder().encodeToString(fromData) : null;
            }
        };
    }

    @Provider
    public static Converter<byte[], String> bytes2hexaDump() {
        return bytes2hexaDump(16, true);
    }

    /**
     * Creates a converter for writing byte[] data into hexa-decimal tabular format,
     * optionally with the respective characters at the end of each line.
     *
     * This converter is mainly for debugging purposes when serializing data to bytes.
     *
     * @param columns
     * @param printChars
     * @return
     */
    @Provider
    public static Converter<byte[], String> bytes2hexaDump(
            @Param("columns") int columns,
            @Param("printChars") boolean printChars)
    {
        return new ConverterBase<byte[], String>(byte[].class, String.class) {
            @Override public String convert(byte[] bytes) throws ConverterException {
                if(bytes == null) {
                    return null;
                }

                char[] digits = "0123456789ABCDEF".toCharArray();
                StringBuilder sb = new StringBuilder();
                int cols = Math.max(Math.min(columns, bytes.length), 1);  // align column count
                char[] line = new char[(4 * cols) + 4];
                int hexpos = 0;
                int chpos = line.length - cols;
                int appendLength = printChars ? line.length : 3 * cols;
                Arrays.fill(line, ' ');

                for(int i=0; i<bytes.length; i++) {

                    // line feed
                    if(i % cols == 0 && i > 0) {
                        sb.append(line, 0, appendLength).append(System.lineSeparator());
                        Arrays.fill(line, ' ');
                        hexpos = 0;
                        chpos = line.length - cols;
                    }

                    // write the data
                    char ch = (char) bytes[i];
                    line[chpos++]  = Character.isISOControl(ch) ? '.' : ch;
                    line[hexpos++] = digits[ch >> 4 & 0xF];
                    line[hexpos++] = digits[ch & 0xF];
                    hexpos++; // space
                }

                // last line
                if(hexpos > 0) {
                    sb.append(line, 0, appendLength);
                }
                return sb.toString();
            }
        };
    }


    /**
     * Creates Converter from a simple ConverterFunction by adding explicit type info.
     *
     * @param c
     * @param f
     * @param t
     * @param <F>
     * @param <T>
     * @return
     */
    public static <F,T> Converter<F,T> asConverter(Class<F> f, Class<T> t, ConverterFunction<? super F, ? extends T > c) {
        return (c instanceof Converter) ?  narrow(f, t, (Converter) c) :  new ConverterBase<F, T>(f, t) {
            @Override public T convert(F fromData) throws ConverterException {
                return c.convert(fromData);
            }
        };
    }

    /**
     * Checks if c.getFromType() and c.getToType() of the specified c converter
     * are the same as the expected F and T types.
     *
     * If that's not the case, the original converter will be wrapped so it returns exactly the
     * expected types.
     *
     * In order to do here a semantically correct narrowing, the following prerequisites
     * must be fulfilled:
     *   1. c.getFromType() must be assignable from type F
     *   2. T must be assignable from type c.getToType()
     *
     * If these conditions are not met, a ConverterException is thrown.
     *
     * @param f
     * @param t
     * @param c
     * @param <F>
     * @param <T>
     * @return
     * @throws ConverterException
     */
    private static <F,T> Converter<F,T> narrow(Class<F> f, Class<T> t, Converter<F,T> c) throws ConverterException {
        if(c.getToType().equals(t) && c.getFromType().equals(f))
        {
            // return converter as is
            return c;
        }
        else if(c.getFromType().isAssignableFrom(f) && t.isAssignableFrom(c.getToType()))
        {
            // narrow converter from/to types by wrapping original converter
            return new ConverterBase<F, T>(f,t) {
                @Override public T convert(F fromData) throws ConverterException {
                    return c.convert(fromData);
                }
                @Override public String toString() {
                    return "[" + c + " as " + super.toString() + "]";
                }
            };
        }
        else
        {
            throw new ConverterException("Unable to narrow converter [" + c + "] to "
                    + f.getTypeName() + "->" + t.getTypeName());
        }
    }


    /**
     * Finds the first matching converter from the provided converter list,
     * which converts from type F into type T.
     *
     * If there is no converter can be found, but type T can be assigned from F via cast,
     * this method returns null.
     *
     * Otherwise the method throws an exception.
     *
     * @param from
     * @param to
     * @param converters
     * @param <F>
     * @param <T>
     * @return
     * @throws ConverterException
     */
    private static <F,T> Converter<F,T> find(Class<F> from, Class<T> to, Iterable<Converter> converters) throws ConverterException {

        // Iterate the converters and find first matching
        for(Converter c : converters) {
            if(c.getFromType().isAssignableFrom(from) && to.isAssignableFrom(c.getToType())) {
                return narrow(from, to, c);  // narrow for F and T
            }
        }

        if(to.isAssignableFrom(from)) {
            return null;  // no converter found, but cast could be applied
        } else {
            // Could not found a converter between type F and T, report exception
            throw new ConverterException("Unable to convert from type "
                    + from.getTypeName() + " to type " + to.getTypeName()
                    + ". No converter available.");
        }
    }

    /**
     * Connect type T1 and T2 with a converter selected from the specified converters.
     *
     * @param c1
     * @param c2
     * @param converters
     * @param <T1>
     * @param <T2>
     * @return
     */
    public static <T1,T2> Converter<T1,T2> connect(Class<T1> c1, Class<T2> c2, Iterable<Converter> converters) {
        Converter<T1, T2> converter = find(c1, c2, converters);
        return converter != null ? converter : asConverter(c1, c2, x->c2.cast(x));  // converter or explicit cast
    }
    public static <T1,T2> Converter<T1,T2> connect(Converter <T1,?> c1, Class<T2> c2, Iterable<Converter> converters) {
        return narrow(c1.getFromType(), c2, (Converter<T1,T2>) chain(c1, find(c1.getToType(), c2, converters)));
    }
    public static <T1,T2> Converter<T1,T2> connect(Class<T1> c1, Converter<?,T2> c2, Iterable<Converter> converters) {
        return narrow(c1, c2.getToType(), (Converter<T1,T2>) chain(find(c1, c2.getFromType(), converters), c2));
    }
    public static <T1,T2> Converter<T1,T2> connect(Converter<T1,?> c1, Converter<?,T2> c2, Iterable<Converter> converters) {
        return narrow(c1.getFromType(), c2.getToType(),
                (Converter<T1,T2>) chain(c1, find(c1.getToType(), c2.getFromType(), converters), c2));
    }

    // base class for implementing a Converter
    private static abstract class ConverterBase<F,T> implements Converter<F,T> {
        private Class<T> to;
        private Class<F> from;

        @Override public Class<F> getFromType() { return from; }
        @Override public Class<T> getToType() { return to; }

        public ConverterBase(Class<F> from, Class<T> to) {
            this.to = to;
            this.from = from;
        }

        @Override public String toString() {
            return new StringBuilder(from.getTypeName())
                    .append("->")
                    .append(to.getTypeName())
                    .toString();
        }
    }

    private static Logger log = Logger.getLogger(Converters.class.getName());
}
