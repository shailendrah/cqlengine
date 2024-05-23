package com.oracle.cep.cartridge.java.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.oracle.cep.cartridge.java.JavaCartridgeLogger;

class MemberFinder
{
  private static final Log logger = 
    LogFactory.getLog(JavaCartridge.JAVA_CARTRIDGE_LOGGER);
  
  private static final int 
    JAVA_BASE_ASSIGNABLE = 1,
    JAVA_BOX_TYPES_ASSIGABLE = 2,
    JAVA_VARARGS_ASSIGNABLE = 3;

  private static final int
    FIRST_ROUND_ASSIGNABLE = JAVA_BASE_ASSIGNABLE,
    LAST_ROUND_ASSIGNABLE = JAVA_VARARGS_ASSIGNABLE;

  private static Map<Class<?>,Class<?>> wrapperMap = new Hashtable<Class<?>, Class<?>>();
  static {
    wrapperMap.put( Boolean.TYPE, Boolean.class );
    wrapperMap.put( Byte.TYPE, Byte.class );
    wrapperMap.put( Short.TYPE, Short.class );
    wrapperMap.put( Character.TYPE, Character.class );
    wrapperMap.put( Integer.TYPE, Integer.class );
    wrapperMap.put( Long.TYPE, Long.class );
    wrapperMap.put( Float.TYPE, Float.class );
    wrapperMap.put( Double.TYPE, Double.class );
    wrapperMap.put( Boolean.class, Boolean.TYPE );
    wrapperMap.put( Byte.class, Byte.TYPE );
    wrapperMap.put( Short.class, Short.TYPE );
    wrapperMap.put( Character.class, Character.TYPE );
    wrapperMap.put( Integer.class, Integer.TYPE );
    wrapperMap.put( Long.class, Long.TYPE );
    wrapperMap.put( Float.class, Float.TYPE );
    wrapperMap.put( Double.class, Double.TYPE );
  }

  public Method findMethod(Class<?> targetClass, String name, 
      Class<?>[] targetParamTypes) 
    throws NoSuchMethodException {
    
    // We cannot use Class.getMethod() as it arbitrarily chooses one if there are multiple
    //  candidates in the classes' and super-classes.
    
    List<Method> candidates = new LinkedList<Method>(); 
    findCandidateMethods(targetClass, name, targetParamTypes, candidates);
    
    Method method = findMostSpecificMethod(targetParamTypes, 
        candidates.toArray(new Method[0]));
    
    if (method == null) {
      String expected = formatMethodName(name, targetParamTypes);
      String actual = formatMethodCandidateNames(candidates);
    
      String errorMsg = 
        JavaCartridgeLogger.noSuchMethodException(targetClass.getName(), expected, actual);
      
      if (logger.isDebugEnabled()) {
        logger.debug(errorMsg);
      }
      
      throw new NoSuchMethodException(errorMsg);  
    }
    
    return method;
  }

  Method findMostSpecificMethod(Class<?>[] idealMatch, Method[] methods)
  {
    // copy signatures into array for findMostSpecificMethod()
    Class<?> [][] candidateSigs = new Class [ methods.length ][];
    for(int i=0; i<methods.length; i++)
      candidateSigs[i] = methods[i].getParameterTypes();

    int match = findMostSpecificSignature( idealMatch, candidateSigs );
    return match == -1 ? null : methods[match];
  }

  private int findMostSpecificSignature(Class<?> [] idealMatch, Class<?> [][] candidates )
  {
    for (int round = FIRST_ROUND_ASSIGNABLE; round <= LAST_ROUND_ASSIGNABLE; round++ )
    {
      Class<?> [] bestMatch = null;
      int bestMatchIndex = -1;

      for (int i=0; i < candidates.length; i++)
      {
        Class<?>[] targetMatch = candidates[i];

        if (isSignatureAssignable(idealMatch, targetMatch, round)
            && ( (bestMatch == null) ||
                ( isSignatureAssignable( targetMatch, bestMatch,
                    JAVA_BASE_ASSIGNABLE ) &&
                    !areSignaturesEqual(targetMatch, bestMatch) )
            )
        )
        {
          bestMatch = targetMatch;
          bestMatchIndex = i;
        }
      }

      if ( bestMatch != null )
        return bestMatchIndex;
    }

    return -1;
  }
  
  private boolean isSignatureAssignable( Class<?>[] from, Class<?>[] to, int round )
  {
    if ( round != JAVA_VARARGS_ASSIGNABLE && from.length != to.length )
      return false;

    switch ( round )
    {
      case JAVA_BASE_ASSIGNABLE:
        for( int i=0; i<from.length; i++ )
          if ( !isJavaBaseAssignable( to[i], from[i] ) )
            return false;
        return true;
      case JAVA_BOX_TYPES_ASSIGABLE:
        for( int i=0; i<from.length; i++ )
          if ( !isJavaBoxTypesAssignable( to[i], from[i] ) )
            return false;
        return true;
      case JAVA_VARARGS_ASSIGNABLE:
        return isSignatureVarargsAssignable( from, to );
    }
    
    return false;
  }

  /**
   * Are the two signatures exactly equal? This is checked for a special
   * case in overload resolution.
   */
  private boolean areSignaturesEqual(Class<?>[] from, Class<?>[] to)
  {
    if (from.length != to.length)
      return false;

    for (int i = 0; i < from.length; i++)
      if (from[i] != to[i])
        return false;

    return true;
  } 
  
  private static boolean isSignatureVarargsAssignable(
    Class<?>[] from, Class<?>[] to )
  {
    return false;
  }
  
  /**
  Test if a conversion of the rhsType type to the lhsType type is legal via
 standard Java assignment conversion rules (i.e. without a cast).
 The rules include Java 5 autoboxing/unboxing.
  <p/>

  For Java primitive TYPE classes this method takes primitive promotion
  into account.  The ordinary Class.isAssignableFrom() does not take
  primitive promotion conversions into account.  Note that Java allows
  additional assignments without a cast in combination with variable
  declarations and array allocations.  Those are handled elsewhere
  (maybe should be here with a flag?)
  <p/>
  This class accepts a null rhsType type indicating that the rhsType was the
  value Primitive.NULL and allows it to be assigned to any reference lhsType
  type (non primitive).
  <p/>

  Note that the getAssignableForm() method is the primary bsh method for
  checking assignability.  It adds additional bsh conversions, etc.

  @see #isBshAssignable( Class, Class )
  @param lhsType assigning from rhsType to lhsType
  @param rhsType assigning from rhsType to lhsType
   */
  static boolean isJavaAssignable( Class<?> lhsType, Class<?> rhsType ) {
    return isJavaBaseAssignable( lhsType, rhsType )
    || isJavaBoxTypesAssignable( lhsType, rhsType );
  }

  /**
  Is the assignment legal via original Java (up to version 1.4)
  assignment rules, not including auto-boxing/unboxing.
 @param rhsType may be null to indicate primitive null value
   */
  static boolean isJavaBaseAssignable(Class<?> lhsType, Class<?> rhsType )
  {
    /*
    Assignment to loose type, defer to bsh extensions
    Note: we could shortcut this here:
    if ( lhsType == null ) return true;
    rather than forcing another round.  It's not strictly a Java issue,
    so does it belong here?
     */
    if ( lhsType == null )
      return false;

    // null rhs type corresponds to type of Primitive.NULL
    // assignable to any object type
    if ( rhsType == null )
      return !lhsType.isPrimitive();

    if ( lhsType.isPrimitive() && rhsType.isPrimitive() )
    {
      if ( lhsType == rhsType )
        return true;

      // handle primitive widening conversions - JLS 5.1.2
      if ( (rhsType == Byte.TYPE) &&
          (lhsType == Short.TYPE || lhsType == Integer.TYPE
              || lhsType == Long.TYPE || lhsType == Float.TYPE
              || lhsType == Double.TYPE))
        return true;

      if ( (rhsType == Short.TYPE) &&
          (lhsType == Integer.TYPE || lhsType == Long.TYPE ||
              lhsType == Float.TYPE || lhsType == Double.TYPE))
        return true;

      if ((rhsType == Character.TYPE) &&
          (lhsType == Integer.TYPE || lhsType == Long.TYPE ||
              lhsType == Float.TYPE || lhsType == Double.TYPE))
        return true;

      if ((rhsType == Integer.TYPE) &&
          (lhsType == Long.TYPE || lhsType == Float.TYPE ||
              lhsType == Double.TYPE))
        return true;

      if ((rhsType == Long.TYPE) &&
          (lhsType == Float.TYPE || lhsType == Double.TYPE))
        return true;

      if ((rhsType == Float.TYPE) && (lhsType == Double.TYPE))
        return true;
    }
    else
      if ( lhsType.isAssignableFrom(rhsType) )
        return true;

    return false;
  }

  /**
  Determine if the type is assignable via Java boxing/unboxing rules.
   */
  static private boolean isJavaBoxTypesAssignable(
      Class<?> lhsType, Class<?> rhsType )
  {
    // Assignment to loose type... defer to bsh extensions
    if ( lhsType == null )
      return false;

    // prim can be boxed and assigned to Object
    if ( lhsType == Object.class )
      return true;

    // prim numeric type can be boxed and assigned to number
    if ( lhsType == Number.class
        && rhsType != Character.TYPE
        && rhsType != Boolean.TYPE
    )
      return true;

    // General case prim type to wrapper or vice versa.
    // I don't know if this is faster than a flat list of 'if's like above.
    // wrapperMap maps both prim to wrapper and wrapper to prim types,
    // so this test is symmetric
    if (wrapperMap.get( lhsType ) == rhsType )
      return true;

    return false;
  }

  private void findCandidateMethods(Class<?> targetClass, String name,
      Class<?>[] targetParamTypes, List<Method> candidates)
  {
    if (isPublic(targetClass))
    {
      Method [] methods = targetClass.getMethods();
      for (Method method : methods) 
      {
        if (method.getName().equals(name) &&
            isPublic(method) && 
            method.getParameterTypes().length == targetParamTypes.length) {
            // FIXME shouldn't this be false for round 3? 
            candidates.add(method);
            
            if (logger.isDebugEnabled())
              logger.debug("Adding candidate method for evaluation = " + method);
        }
      }
    } else {
      if (logger.isInfoEnabled())
        logger.info("Ignoring methods for non-public Java class '" + targetClass.getName() + "'"); 
    }
    
    Class<?> [] interfaces = targetClass.getInterfaces();
    for (Class<?> interfaze : interfaces) 
      findCandidateMethods(interfaze, name, targetParamTypes, candidates);
    
    Class<?> superClass = targetClass.getSuperclass();
    if (superClass != null) 
      findCandidateMethods(superClass, name, targetParamTypes, candidates);
  }
  
  private void findCandidateConstructors(Class<?> targetClass,
      Class<?>[] targetParamTypes, List<Constructor<?>> candidates)
  {
    if (isPublic(targetClass))
    {
      Constructor<?> [] constructors = targetClass.getConstructors();
      for (Constructor<?> constructor : constructors)
      {
        if (isPublic(constructor) && 
            constructor.getParameterTypes().length == targetParamTypes.length) {
            // FIXME shouldn't this be false for round 3? 
            candidates.add(constructor);
            
            if (logger.isDebugEnabled())
              logger.debug("Adding candidate constructor for evaluation = " + constructor);
        }
      }
    } else {
      if (logger.isInfoEnabled())
        logger.info("Ignoring constructors for non-public Java class '" + targetClass.getName() + "'"); 
    }
    
    Class<?> [] interfaces = targetClass.getInterfaces();
    for (Class<?> interfaze : interfaces) 
      findCandidateConstructors(interfaze, targetParamTypes, candidates);
    
    Class<?> superClass = targetClass.getSuperclass();
    if (superClass != null) 
      findCandidateConstructors(superClass, targetParamTypes, candidates);
  }
  
  private boolean isPublic(Class<?> c ) {
    return Modifier.isPublic( c.getModifiers() );
  }
  
  private boolean isPublic(Method m ) {
    return Modifier.isPublic( m.getModifiers() );
  }
  
  private boolean isPublic(Constructor<?> c ) {
    return Modifier.isPublic( c.getModifiers() );
  }
  
  public Constructor<?> findConstructor(Class<?> targetClass, Class<?>[] targetParamTypes) 
  throws NoSuchMethodException {
    
    List<Constructor<?>> candidates = new LinkedList<Constructor<?>>(); 
    findCandidateConstructors(targetClass, targetParamTypes, candidates);
    
    Constructor<?> constructor = 
      findMostSpecificConstructor(targetParamTypes, candidates.toArray(new Constructor[0]));
    
    if (constructor == null) {
      String expected = formatMethodName(targetClass.getName(), targetParamTypes);
      String actual = formatConstructorCandidateNames(candidates);
    
      String errorMsg = 
        JavaCartridgeLogger.noSuchConstructorException(targetClass.getName(), expected, actual);
      
      if (logger.isDebugEnabled()) {
        logger.debug(errorMsg);
      }
      
      throw new NoSuchMethodException(errorMsg);  
    }
    
    return constructor;
  }

  private Constructor<?> findMostSpecificConstructor(Class<?>[] idealMatch, 
      Constructor<?>[] constructors)
  {
    int match = findMostSpecificConstructorIndex(idealMatch, constructors );
    return ( match == -1 ) ? null : constructors[ match ];
  }

  private int findMostSpecificConstructorIndex(
      Class<?>[] idealMatch, Constructor<?>[] constructors)
  {
    Class<?> [][] candidates = new Class [ constructors.length ] [];
    for(int i=0; i< candidates.length; i++ )
      candidates[i] = constructors[i].getParameterTypes();

    return findMostSpecificSignature( idealMatch, candidates );
  }
  
  static String formatMethodName(String name, Class<?>[] targetParamTypes)
  {
    StringBuilder builder = new StringBuilder(name);
    builder.append("(");
    
    for (Class<?> param : targetParamTypes)
    {
      builder.append(param.getName());
      builder.append(",");
    }
    
    // Remove last ',' should it have been added.
    if (targetParamTypes.length > 0)
      builder.deleteCharAt(builder.length()-1);
    
    builder.append(")");
    
    return builder.toString();
  }
  
  static String formatMethodCandidateNames(List<Method> candidates)
  {
    StringBuilder builder = new StringBuilder();
    builder.append("'");
    
    for (Method method : candidates)
    {
      builder.append(method.toGenericString());
      builder.append(",");
    }
    
    // Remove last ',' should it have been added.
    if (candidates.size() > 0)
      builder.deleteCharAt(builder.length()-1);
    
    builder.append("'");
    
    return builder.toString();
  }
  
  static String formatConstructorCandidateNames(List<Constructor<?>> candidates)
  {
    StringBuilder builder = new StringBuilder();
    builder.append("'");
    
    for (Constructor<?> constr : candidates)
    {
      builder.append(constr.getName());
      builder.append(",");
    }
    
    // Remove last ',' should it have been added.
    if (candidates.size() > 0)
      builder.deleteCharAt(builder.length()-1);
    
    builder.append("'");
    
    return builder.toString();
  }
  
}
