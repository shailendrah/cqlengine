package oracle.cep.converter;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ggeiszte on 11/21/17.
 */
public class ConverterRegistry {


    public ConverterRegistry() {

        // initialize the converter functions for converting from String to a specific type
        addStringConverter(String.class,  x -> x);
        addStringConverter(Integer.class, x -> Integer.parseInt(x));
        addStringConverter(int.class,     x -> Integer.parseInt(x));
        addStringConverter(Charset.class, x -> Charset.forName(x));
        addStringConverter(boolean.class, x -> Boolean.parseBoolean(x));
        addStringConverter(Boolean.class, x -> Boolean.parseBoolean(x));
        addStringConverter(Level.class,   x -> Level.parse(x));
    }

    /**
     * Inspects the specified class and registers all the possible Converter providers.
     *
     * @param clazz
     */
    public void register(Class<?> clazz) {
        for(Method m : clazz.getMethods()) {

            Provider provider = m.getDeclaredAnnotation(Provider.class);
            if (provider == null) {
                continue;  // skip if not annotated
            }

            String providerMethod = "'" + m + "'";

            int modifiers = m.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                throw new IllegalArgumentException(providerMethod + " must be static.");
            }
            if (!Modifier.isPublic(modifiers)) {
                throw new IllegalArgumentException(providerMethod + " must be public.");
            }
            if (!Converter.class.isAssignableFrom(m.getReturnType())) {
                throw new IllegalArgumentException(providerMethod + " must return " + Converter.class.getName());
            }

            Set<String> pnames = new TreeSet<>();  // sorted list of parameter names for creating signature
            Annotation[][] at = m.getParameterAnnotations();
            Class<?>[] paramTypes = m.getParameterTypes();

            // While processing the parameters we fill this array with functions,
            // which can convert between context/parameter map and the parameter type
            Function<Map<String, Object>, Object>[] paramSuppliers = new Function[at.length];
            for (int i=0; i<at.length; i++) {

                // check if the parameter has a name
                Param p = findAnnotation(at[i], Param.class);
                if (p == null) {
                    throw new IllegalArgumentException("Each parameter must be annotated with "
                            + Param.class.getName()
                            + " in " + providerMethod);
                }
                String pname = p.value();
                if (pname == null) {
                    throw new IllegalArgumentException("Parameter name not specified by annotation in " + providerMethod);
                }

                // create the supplier method
                paramSuppliers[i] = ((params) -> params.get(pname));
                paramSuppliers[i] = paramSuppliers[i].andThen(findStringConversion(paramTypes[i]));
                pnames.add(pname);  // add the parameter
                try {
                    // apply String to parameter type conversion
                    paramSuppliers[i] = paramSuppliers[i];
                } catch(Exception ex) {
                    throw new IllegalArgumentException("Unable to create parameter converter for "
                            + providerMethod + ": " + ex.getMessage(), ex);
                }
            }

            Signature signature = new Signature(m.getName(), pnames, new StaticMethodConverterProvider(m, paramSuppliers));
            if(signatures.contains(signature)) {
                throw new IllegalArgumentException("Ambiguous provider definition for "
                        + signature + " in " + providerMethod);
            }

            System.out.println("REGISTERING: " + signature);
            signatures.add(signature);
        }
    }

    private <V> void addStringConverter(Class<V> clazz, Function<String, V> func) {
        stringConverters.put(clazz, func);
    }

    private Function findStringConversion(Class paramType) {
        return new Function() {
            @Override public Object apply(Object o) {
                if(o instanceof String) {
                    Function f = stringConverters.get(paramType);
                    if(f == null) {
                        throw new IllegalArgumentException("No converter found between String -> " + paramType.getName());
                    }
                    return f.apply(o);
                } else {
                    return o;
                }
            }
        };
    }

    /**
     * Helper for finding an annotation of type P in an array.
     *
     * @param aarray
     * @param clazz
     * @param <P>
     * @return
     */
    private static <P extends Annotation> P findAnnotation(Annotation[] aarray, Class<P> clazz) {
        for(Annotation a : aarray) {
            if(clazz.isInstance(a)) {
                return clazz.cast(a);
            }
        }
        return null;
    }

    /**
     * Creates the converter for the specified name and with the specified context and parameters,
     * if such converter was registered in this registry.
     *
     * @param name
     * @param config
     * @return
     */
    public Converter createConverter(String name, Map<String, Object> config) {

        // create the union of config and context parameters and keys
        Set<String> keys = config.keySet();

        // find the matching signature
        Signature signature = signatures.stream().filter(e -> e.match(name, keys)).findFirst().orElse(null);
        if(signature == null) {
            throw new ConverterException("No provider found for converter with name: "
                    + name + " and parameters ("
                    + keys.stream().collect(Collectors.joining(", "))
                    +")");
        }
        return signature.getProvider().get(config);
    }

    /**
     * This is an implementation of the ConverterProvider interface, which returns a converter
     * by calling a specific public static method on a class registered in this registry.
     *
     * The method must be annotated with the {@link Provider} annotation and must be public, static.
     *
     * Each method parameter must have a {@link Param} annotation.
     *
     *
     */
    private static class StaticMethodConverterProvider implements ConverterProvider {
        private final Method method;
        private final Function<Map<String, Object>, Object>[] suppliers;

        public StaticMethodConverterProvider(Method m, Function<Map<String, Object>, Object>[] paramSuppliers) {
            this.method = m;
            this.suppliers = paramSuppliers;
        }

        @Override
        public Converter get(Map<String, Object> params) {

            Object[] args  = new Object[method.getParameterCount()];
            for(int i=0; i< args.length; i++) {
                args[i] = suppliers[i].apply(params);
            }

            try {
                return (Converter<?,?>) method.invoke(null, args);
            } catch (Exception e) {
                throw new ConverterException("Unable to create converter for " + method, e);
            }
        }
    }

    /**
     * This class pairs a converter provider with the method signature,
     * and implements match rule and sort order for signatures.
     *
     */
    private static class Signature implements Comparable<Signature> {

        String name;
        Set<String> params;
        private ConverterProvider provider;

        public Signature(String name, Set<String> params, ConverterProvider provider) {
            this.name = name;
            this.params = new TreeSet<>(params);
            this.provider = provider;
        }

        public boolean match(String name, Set<String> paramNames) {
            return this.name.equals(name) && paramNames.containsAll(params);
        }

        ConverterProvider getProvider() {
            return this.provider;
        }

        /**
         * Defines sort order of signatures. Two signatures are equal if they have the
         * same method name, and all parameter name is matching.
         *
         * However this sort order also considers the parameter count of each method,
         * so we can implement the method match order by matching the most specific
         * method (which has more parameters) to less specific.
         *
         * @param o
         * @return
         */
        @Override public int compareTo(Signature o) {
            int res = o.name.compareTo(this.name);
            if(res == 0) {
                res = Integer.compare(o.params.size(), this.params.size());
            }
            if(res == 0) {
                res = o.toString().compareTo(this.toString());
            }
            return res;
        }

        @Override
        public String toString() {
            return name + "(" + params.stream().collect(Collectors.joining(", ")) + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Signature signature = (Signature) o;
            return Objects.equals(name, signature.name)
                && Objects.equals(params, signature.params);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, params);
        }
    }

    Set<Signature> signatures = new TreeSet<>();
    Map<Class, Function> stringConverters = new HashMap<>();
}
