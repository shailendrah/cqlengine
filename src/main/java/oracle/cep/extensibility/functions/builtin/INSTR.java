package oracle.cep.extensibility.functions.builtin;
 
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.extensibility.functions.UDFException;
 
/**
 * Represents ORACLE database's scalar character function INSTR.
 * 
 * Takes a string and finds the position of a second string in that string. The
 * function will optionally begin the search at a given start position and
 * return the position of the nth set. If the start position is omitted,
 * discoverer will search from the beginning. If negative number is used for the
 * start, discoverer begins the search at the end of the string and searches
 * backward. Using zero for the start position, the answer will be zero.
 * 
 * @author subhrcho
 *
 */
public class INSTR implements SingleElementFunction
{

  private static final String FUNC_NAME = "INSTR()";

  @Override
  public Object execute(Object[] args) throws UDFException
  {

    // Oracle returns null if any of the parameters are null. So we also return
    // null.
    if (args[0] == null || args[1] == null)
      return null;
    if (args.length >= 3 && args[2] == null)
      return null;
    if (args.length == 4 && args[3] == null)
      return null;

    // Initialize the variables from arguments
    String str = (String) args[0];
    String subStr = (String) args[1];
    int oStartPos, occurence;

    oStartPos = args.length > 2 && args[2] != null ? (Integer) args[2] : 1;
    occurence = args.length > 3 && args[3] != null ? (Integer) args[3] : 1;
    if (occurence < 1)
    {
      throw new SoftUDFException(
          UDFError.ILLEGAL_OCCURENCE_FUNCTION_INSTR, occurence,
          FUNC_NAME);
    }
    int retVal = 0;
    // Oracle returns 0 if startPos i.e args[2] == 0.
    // In that case, we can simply return 0

    boolean proceed = true;
    if (oStartPos == 0)
      proceed = false;
    if (proceed)
    {
      // Flag determines calculate from beginning or from end
      boolean fromBeginning = oStartPos >= 0 ? true : false;

      // In Oracle String chars 1-based and Java is 0-based
      int jStartPos = fromBeginning ? (oStartPos - 1) : str.length()
          - Math.abs(oStartPos);

      if (fromBeginning)
      {
        int begin_temp = ordinalIndexOf(str, subStr, jStartPos, occurence);
        if (begin_temp >= 0)
          retVal = begin_temp + 1;
        else
          retVal = 0; // If sub string not found, Oracle expects 0
      }
      else
      {
        int end_temp = ordinalLastIndexOf(str, subStr, jStartPos, occurence);
        if (end_temp >= 0)
          retVal = end_temp + 1;
        else
          retVal = 0; // If sub string not found, Oracle returns 0
      }
    }
    return retVal;
  }

  /**
   * Helper function to return the index of the nth occurrence of a substring in
   * a string. The function will start the search from the zero-based index
   * position specified by the @{code initPos} parameter.
   * 
   * @param str
   *          the String whose sub String needs to be calculated
   * @param substr
   *          the sub-string to look for
   * @param initPos
   *          non-negative zero-based start position
   * @param occurence
   *          the ordinal number for successful hit
   * @return the zero-based index of the first character of the subStr parameter
   */
  private static final int ordinalIndexOf(String str, String substr,
      int initPos, int occurence)
  {
    // "FOO" as "" + "FOO". So Java returns 0 when substr is empty. This is unintuitive. So special handling needed
    if(substr.isEmpty()) return -1;
    int pos = str.indexOf(substr, initPos);
    while (--occurence > 0 && pos != -1)
      pos = str.indexOf(substr, pos + 1);
    return pos;
  }

  /**
   * Helper function to return the last index of the nth occurrence of a
   * substring in a string . The function will start the search from the
   * zero-based index position specified by the @{code initPos} parameter.
   * 
   * @param str
   *          the String whose sub String needs to be calculated
   * @param substr
   *          the sub-string to look for
   * @param initPos
   *          non-negative zero-based start position
   * @param occurence
   *          the ordinal number for successful hit
   * @return the zero-based index of the first character of the subStr parameter
   *         reached at by searching from the end.
   */
  private static final int ordinalLastIndexOf(String str, String substr,
      int initPos, int occurence)
  {
    // "FOO" as "" + "FOO". So Java returns 0 when the substr is empty. This is unintuitive. So special handling needed
    if(substr.isEmpty()) return -1;
    int pos = str.lastIndexOf(substr, initPos);
    while (--occurence > 0 && pos != -1)
      pos = str.lastIndexOf(substr, pos - 1);
    return pos;
  }

  @Override
  public String toString()
  {
    return FUNC_NAME;
  }
 
}
