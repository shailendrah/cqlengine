package oracle.cep.converter;

import org.junit.Test;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.*;
import static oracle.cep.converter.Converters.*;

/**
 * Created by ggeiszte on 9/21/17.
 */
public class ConvertersTest {

    @Test
    public void testConnect() throws Exception {

        List<Converter> converters = Arrays.asList(
            bytes2hexaDump(10, true),
            asConverter(Integer.class, byte[].class, i -> ByteBuffer.allocate(4).putInt(i).array()),
            object2string(),
            object2bytes()
        );

        Converter<Integer, byte[]> c1 = connect(Integer.class, byte[].class, converters);
        System.out.println(c1);
        assertEquals(Integer.class, c1.getFromType());
        assertEquals(byte[].class, c1.getToType());

        Converter<Integer, String> c2 = connect(c1, String.class, converters);
        System.out.println(c2);
        assertEquals(Integer.class, c2.getFromType());
        assertEquals(String.class, c2.getToType());

        System.out.println(c2.convert(15));
        System.out.println(c2.convert(6789));
        System.out.println(c2.convert(232));

        Integer i1 = new Integer(3);
        Converter<Integer, Number> c3 = connect(Integer.class, Number.class, Collections.emptyList());
        assertEquals(Integer.class, c3.getFromType());
        assertEquals(Number.class, c3.getToType());
        assertEquals(i1, c3.convert(i1));
        assertNull(c3.convert(null));

        boolean thrown = false;
        try {
            connect(Number.class, Integer.class, Collections.emptyList());  // this must fail
        } catch(ConverterException cex) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testChains() {

        ConverterFunction<byte[], String> c1 = bytes2base64().chain(object2bytes()).chain(bytes2hexaDump());
        Converter<byte[], byte[]> c2 = chain(bytes2base64(), object2bytes());
        Converter<Serializable, String> c3 = (Converter<Serializable, String>) Converters.chain(
                object2bytes(),
                bytes2base64(),
                string2bytes(),
                bytes2hexaDump()
        );
        assertEquals(Serializable.class, c3.getFromType());
        assertEquals(String.class, c3.getToType());
        System.out.println(c3);
        System.out.println(c3.convert(45));
    }

    @Test
    public void testNull() {

        String s1 = "This is a text";

        ConverterFunction<String, Integer> c1 = Converters.toNull();
        assertNull(c1.convert(s1));
        assertNull(c1.convert(null));

        Converter<String, Integer> c2 = Converters.asConverter(String.class, Integer.class, c1);
        assertNull(c2.convert(s1));
        assertNull(c2.convert(null));
        assertEquals(String.class, c2.getFromType());
        assertEquals(Integer.class, c2.getToType());

        Converter<String, String> c3 = Converters.chain(c2, Converters.object2string());
        assertNull(c3.convert(s1));
        assertNull(c3.convert(null));
        assertEquals(String.class, c3.getFromType());
        assertEquals(String.class, c3.getToType());
    }

    @Test
    public void testIdentity() {

        Integer i1 = new Integer(12);

        ConverterFunction<Integer, Integer> c1 = Converters.identity();
        assertEquals(i1, c1.convert(i1));
        assertEquals(i1, c1.convert(i1.intValue()));
        assertNull(c1.convert(null));

        Converter<Integer, Integer> c2 = identity(Integer.class);
        assertEquals(i1, c2.convert(i1));
        assertNull(c2.convert(null));
        assertEquals(Integer.class, c2.getFromType());
        assertEquals(Integer.class, c2.getToType());

        Converter<Integer, Integer> c3 = asConverter(Integer.class, Integer.class, c1);
        assertEquals(i1, c3.convert(i1));
        assertNull(c3.convert(null));
        assertEquals(Integer.class, c3.getFromType());
        assertEquals(Integer.class, c3.getToType());

        Converter<Integer, Number> c4 = asConverter(Integer.class, Number.class, c2);
        assertEquals(i1, c4.convert(i1));
        assertNull(c4.convert(null));
        assertEquals(Integer.class, c4.getFromType());
        assertEquals(Number.class, c4.getToType());

        Converter<Integer, Number> c5 = chain(c2, c4);
        assertEquals(i1, c5.convert(i1));
        assertNull(c5.convert(null));
        assertEquals(Integer.class, c5.getFromType());
        assertEquals(Number.class, c5.getToType());
    }

    @Test
    public void testConverterRegistry() {
        new ConverterRegistry().register(Converters.class);
    }

    @Test
    public void testConverterRegistryChain() {

        ConverterRegistry registry = new ConverterRegistry();
        registry.register(Converters.class);

        Map<String, Object> params = new HashMap<>();

        Converter c1 = registry.createConverter("object2bytes", params);

        params.put("columns", "10");
        params.put("printChars", "true");
        Converter c2 = registry.createConverter("bytes2hexaDump", params);

        Converter c3 = chain(c1, c2);

        params.clear();
        params.put("logLevel", "INFO");
        params.put("converter", c3);

        Converter c4 = registry.createConverter("logger", params);

        System.out.println(c4.convert("Simple text with hexa dump..."));
    }
    
    @Test
    public void testHexaDump() {
        String s1 = "This is a quite long text to test hexa dump.";
        System.out.println(string2bytes().chain(bytes2hexaDump(16, true)).convert(s1));
    }
}
