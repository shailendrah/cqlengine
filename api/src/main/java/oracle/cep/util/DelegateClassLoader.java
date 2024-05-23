package oracle.cep.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classloader implementation for isolating configured packages from current application.
 *
 * The classloader maintains an internal delegation mode list, which specifies if loading a
 * certain class or resource should be delegated first to the parent classloader, or it should be loaded
 * from the own classpath first. The allow/deny list can be created using the
 * {@link #parentFirst(String)}  and {@link #localFirst(String)} methods, the rules
 * are evaluated in the order how they were added by these methods. Initially each class/resource is loaded
 * from our own classpath first, but if there is a parentFirst rule in the list, which
 * is matching the class/resource prefix, than it will be delegated to the parent. If there are multiple
 * rules in the list which is matching the class prefix, the last rule will win.
 *
 * For example, considering this rule set:
 *
 * parentFirst "java."
 * parentFirst "javax."
 * parentFirst "com.oracle.project."
 * localFirst  "com.oracle.project.a."
 *
 * The rule set above will delegate the class com.oracle.project.b.MyClass to the parent classloader,
 * however will load the class com.oracle.project.a.AnotherClass from it own classpath.
 *
 * Note 1: rule prefix should always end with . (dot), there is no wildcard support
 * Note 2: java and javax should be always delegated first
 * Note 3: for better performance, the classloader should be cached
 * Note 4: use this classloader as a context classloader, associated to the current thread
 *   within a well-defined scope only, see {@link ClassLoaderUtil#enterClassLoaderScope(ClassLoader)}
 *
 * Created by ggeiszte on 2/14/18.
 */
public class DelegateClassLoader extends URLClassLoader {

    public DelegateClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        // if it was already loaded by us
        Class<?> clazz = findLoadedClass(name);
        if(clazz != null) {
            return clazz;
        }

        switch (getDelegationMode(name)) {
            case ParentOnly:
                return getParent().loadClass(name);
            case ParentFirst:
                return super.loadClass(name);
            case LocalOnly:
                return findClass(name);
            default:
            case LocalFirst: {
                try {
                    return findClass(name);  // find as local first
                } catch (ClassNotFoundException ex) {
                    return getParent().loadClass(name); // delegate as usual
                }
            }
        }
    }

    @Override
    public URL getResource(String name)
    {
        switch (getDelegationMode(name)) {
            case ParentOnly:
                return getParent().getResource(name);
            case ParentFirst:
                return super.getResource(name);
            case LocalOnly:
                return findResource(name);
            default:
            case LocalFirst: {
                URL url = findResource(name);
                return (url != null) ? url : getParent().getResource(name);
            }
        }
    }


    /**
     * Finds the last matching delegation rule for the specified prefix.
     * This method evaluates the specified parentFirst/localFirst rules in order.
     *
     * @param name
     * @return
     */
    protected DelegationMode getDelegationMode(String name) {
        DelegationMode mode = DelegationMode.LocalFirst;
        for(Map.Entry<String, DelegationMode> rule : rules.entrySet()) {
            if(name.startsWith(rule.getKey())) {
                mode = rule.getValue();
            }
        }
        return mode;
    }

    /**
     * Sets the delegation mode for the prefix to lookup in parent classloader first, than fallback to local.
     *
     * @param prefix
     */
    public void parentFirst(String prefix) {
        parentFirst(prefix, true);
    }

    /**
     * Sets the delegation mode for the prefix to lookup in parent classloader first,
     * and specifies whether the fallback is allowed to lookup as local.
     *
     * @param prefix
     * @param delegate - if true, fallback to local is allowed.
     */
    public void parentFirst(String prefix, boolean delegate) {
        rules.put(prefix, delegate ? DelegationMode.ParentFirst : DelegationMode.ParentOnly);
    }


    /**
     * Sets the delegation mode for the prefix to lookup in local classloader first, than fallback to parent.
     *
     * @param prefix
     */
    public void localFirst(String prefix) {
        localFirst(prefix, true);
    }

    /**
     * Sets the delegation mode for the prefix to lookup in local classloader first,
     * and specifies whether the fallback is allowed to lookup in parent.
     *
     * @param prefix
     * @param delegate - if true, fallback to parent is allowed.
     */
    private void localFirst(String prefix, boolean delegate) {
        rules.put(prefix, delegate ? DelegationMode.LocalFirst : DelegationMode.LocalOnly);
    }

    private Map<String, DelegationMode> rules = new LinkedHashMap<>();  // preserves insertion order!!!

    protected enum DelegationMode {
        ParentFirst,  // prefer parent classes first, than local
        LocalFirst,   // prefer local classes first, than parent
        ParentOnly,   // lookup in parent only
        LocalOnly     // lookup as local only, do not delegate to parent
    }
}
