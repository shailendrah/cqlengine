package oracle.cep.extensibility.functions.builtin;

import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.extensibility.functions.UDFException;

/**
 * Represents ORACLE database's scalar character function REPLACE.
 *
 * Takes three Strings as input, first string is the original string, second
 * string is search string and third is replacement string. The function
 * searches for substrings in original string, replaces all occurrences with
 * replacement string and returns modified string.
 * 
 * @author kmulay
 *
 */
public class REPLACE implements SingleElementFunction 
{
  @Override
  public Object execute(Object[] args) throws UDFException 
  {
    // throw error if number of arguments are not equal to 3
    if (args.length < 2 || args.length > 3) {
      throw new SoftUDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
    }

    // REPLACE(null,<any>,<any>) is always null
    if(args[0] == null)
      return null;

    // Initialize the variables from arguments
    String original = (String) args[0];
    String search_string = (String) args[1];
    String replacement_string = null;

    if( args.length == 3)
      replacement_string = (String) args[2];

    original = calculate(original, search_string, replacement_string);  
    return original;
  }

  // Helper function to generate replaced string
  public String calculate(String original, String search_string, String replacement_string)
  {
     if(search_string == null || search_string.isEmpty() || search_string.equals(""))
     {
       return original;
     }
     else if(replacement_string == null || replacement_string.isEmpty() || replacement_string.equals("")) 
     {
       original = original.replace(search_string, "");
     }
     else
     {
       original = original.replace(search_string, replacement_string);
     }
    return original;
  }
}
