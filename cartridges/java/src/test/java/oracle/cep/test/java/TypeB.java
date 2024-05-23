package oracle.cep.test.java;

import java.io.Serializable;

public class TypeB implements Serializable
{
  public int i1 = 4;
  
  public TypeC nestedType = new TypeC();
  
  public TypeC getNestedType() {
    return nestedType;
  }
  
}
