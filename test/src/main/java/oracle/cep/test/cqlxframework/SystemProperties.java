package oracle.cep.test.cqlxframework;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

public class SystemProperties
{
  public static String getProperty(String key, String defval)
  {
    // env variable has a priority over the property value
    String val = System.getenv(key);
    if (val == null)
      val = System.getProperty(key);
    return (val == null) ? defval : val;
  }

  public static boolean getProperty(String key, boolean defval)
  {
    String val = getProperty(key, null);
    if (val == null)
      return defval;
    return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("1");
  }

  public static int getProperty(String key, int defval)
  {
    String val = getProperty(key, null);
    if (val == null)
      return defval;
    int ival = 0;
    try
    {
      ival = Integer.parseInt(val);
    } catch (NumberFormatException e)
    {
      throw new RuntimeException(e);
    }
    return ival;
  }

  public static long getProperty(String key, long defval)
  {
    String val = getProperty(key, null);
    if (val == null)
      return defval;
    long ival = 0;
    try
    {
      ival = Long.parseLong(val);
    } catch (NumberFormatException e)
    {
      throw new RuntimeException(e);
    }
    return ival;
  }

  public static double getProperty(String key, double defval)
  {
    String val = getProperty(key, null);
    if (val == null)
      return defval;
    double ival = 0;
    try
    {
      ival = Double.parseDouble(val);
    } catch (NumberFormatException e)
    {
      throw new RuntimeException(e);
    }
    return ival;
  }

}
