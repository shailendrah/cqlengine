package oracle.cep.extensibility.functions.builtin;

import java.util.EnumSet;

import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * Implements Oracle SQL's TRIM function behavior.
 * <p>
 * Takes a String and a TRIM character(optional, default will be whitespace) and
 * strips the trim character from the source String. It also exposes the
 * capability to optionally accept a trimMode parameter which basically controls
 * how the stripping will take place. It can be any one of the three values in
 * the set {"LEADING","TRAILING","BOTH"}. The default trimMode will be "BOTH".
 * <p>
 * If either the input String or the trim character is specified as null, the
 * function will also return null.
 * 
 * @author subhrcho
 *
 */
public class TRIM implements SingleElementFunction
{
  private static final String FUNC_NAME = "TRIM()"; 
     
  @Override
  public Object execute(Object[] args) throws UDFException
  {
    String trimSrc = (String) args[0];
    String trimChar = null;
    //Indicates second argument is passed to the function from user
    boolean hasTrimCharArg = false;
    if(args.length >=2 ){
      trimChar = (String)args[1];
      hasTrimCharArg = true;
    }
    String trimMode = args.length == 3 ? (String) args[2] : TrimMode.BOTH.name();
    if(trimMode == null){
      trimMode = TrimMode.BOTH.name();
    }
    if (trimSrc == null || (hasTrimCharArg && trimChar == null))
      return null;
    if(trimSrc.isEmpty() || (hasTrimCharArg && trimChar.isEmpty()))
      return null;
    
    // trim set should have only one character   
    if(hasTrimCharArg && trimChar.length() != 1 ){
      LogUtil
          .fine(
              LoggerType.TRACE,
              "Invalid trim character "
                  + trimChar
                  + " specified for the function TRIM(). The length of trim character should be 1.");
      
      throw new SoftUDFException(
          UDFError.ILLEGAL_TRIM_CHAR_FUNCTION_TRIM, trimChar,
          FUNC_NAME);
    }
     
    if (!TrimMode.contains(trimMode))
    {
      LogUtil.fine(LoggerType.TRACE, "Invalid trim mode " + trimMode
          + " specified for the function TRIM(). Valid values are "
          + "LEADING, TRAILING, BOTH");
      throw new SoftUDFException(
          UDFError.ILLEGAL_TRIM_MODE_FUNCTION_TRIM, trimMode,
          FUNC_NAME);
    }
   
    String retVal;    
    switch (TrimMode.valueOf(trimMode))
    {
    case LEADING:
      retVal = stripStart(trimSrc, trimChar);
      break;
    case TRAILING:
      retVal = stripEnd(trimSrc, trimChar);
      break;
    default:
      String str = stripStart(trimSrc, trimChar);
      retVal = stripEnd(str, trimChar);
      break;
    }  
    return retVal;
  }

  /**
   * Helper function to strip the start of input String the character(s)
   * specified by {@code stripChar}
   * 
   * @param str
   *          the source String to be stripped
   * @param stripChar
   *          the character to be stripped from the start of the source String
   *          {@code str}
   * @return the String after doing the strip operation
   */
  public static String stripStart(String str, String stripChar)
  {
    int strLen;
    if (str == null || (strLen = str.length()) == 0)
    {
      return str;
    }
    int start = 0;
    // This block trims for whitespace
    // This will be called if user do not explicitly pass null for strip char
    if (stripChar == null)
    {
      while (start != strLen && Character.isWhitespace(str.charAt(start)))
      {
        start++;
      }
    }else
    {
      while (start != strLen
          && stripChar.charAt(0) == str.charAt(start))
      {
        start++;
      }
    }
    return str.substring(start);
  }

  /**
   * Helper function to strip from the end of input String the character(s)
   * specified by {@code stripChar}
   * 
   * @param str
   *          the source string to be stripped
   * @param stripChar
   *          the character to be stripped from the end of the source string
   * @return the String after doing the strip operation
   */
  public static String stripEnd(String str, String stripChar)
  {
    int end=0;
    if (str == null || (end = str.length()) == 0)
    {
      return str;
    }
    // This block trims for whitespace
    // This will be called if user do not explicitly pass null for strip char
    if (stripChar == null)
    {
      while (end != 0 && Character.isWhitespace(str.charAt(end - 1)))
      {
        end--;
      }
    }else
    {
      while (end != 0
          && stripChar.charAt(0) == str.charAt(end - 1))
      {
        end--;
      }
    }
    return str.substring(0, end);
  }
  
  

  @Override
  public String toString()
  {
    return FUNC_NAME;
  }

  /**
   * Represents the various modes for TRIMMING.
   * 
   * @author subhrcho
   *
   */
  private enum TrimMode
  {
    LEADING, TRAILING, BOTH;
    
    /**
     * Utility method to check for the existence of a String value within the
     * values in an Enum.
     * 
     * @param trimMode
     *          the String to be searched within the {@code TrimMode} enum
     * @return true if the value is contained in the {@code TrimMode} enum, otherwise false
     */
    static boolean contains(String trimMode)
    {
      for (TrimMode mode : TrimMode.values())
      {
        if (mode.name().equals(trimMode))
        {
          return true;
        }
      }
      return false;
    }
  }
}
