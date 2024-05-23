package oracle.cep.test.java;

import java.io.Serializable;

public class TypeA1 implements Serializable
{
  public static int i = 3;
  public static char[] ch = "ABC".toCharArray();
  
  public TypeA1() {
  }
  
  public void voidMethod() {
    // do nothing.
  }

  public String toString() 
  {
    return "TypeA1";
  }
    
}
