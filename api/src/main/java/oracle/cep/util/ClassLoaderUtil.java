package oracle.cep.util;

/**
 * Created by ggeiszte on 7/19/17.
 */
public class ClassLoaderUtil {

    /**
     * Sets the context class loader object to the specified class loader instance,
     * and returns an AutoCloseable scope object, which should be used to reset
     * the context classloader to its original value at the end.
     *
     * Use this method in try-with-resource as follows:
     *
     * try(AutoCloseable scope = ClassLoaderUtil.enterClassLoaderScope(loader))
     * {
     *     ...
     * }
     *
     *
     * @param classLoader
     * @return
     */
    public static AutoCloseable enterClassLoaderScope(ClassLoader classLoader) {
        return new ClassLoaderScope(classLoader);
    }

    /**
     * Returns the context class loader, or the class loader of ClassLoaderUtil.class
     *
     * @return
     */
    public static ClassLoader ensureClassLoader() {
        return ensureClassLoader(null);
    }

    /**
     * Returns the context class loader or the fallback class loader, if there
     * is no context class loader. If fallback is not specified, the class loader of
     * ClassLoaderUtil.class will be returned.
     *
     * @param fallback
     * @return
     */
    public static ClassLoader ensureClassLoader(ClassLoader fallback) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if(loader != null) {
            return loader;
        } else if(fallback != null) {
            return  fallback;
        } else {
            return ClassLoaderUtil.class.getClassLoader();
        }
    }

    /**
     * AutoCloseable scoped object which replace the context class loader in the scope
     * for the class loader specified in the constructor.
     *
     */
    private static class ClassLoaderScope implements AutoCloseable {

        public ClassLoaderScope(ClassLoader loader) {
            savedClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
        }

        @Override
        public void close() {
            Thread.currentThread().setContextClassLoader(savedClassLoader);
        }

        ClassLoader savedClassLoader;
    }
}
