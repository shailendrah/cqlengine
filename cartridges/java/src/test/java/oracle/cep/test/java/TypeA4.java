package oracle.cep.test.java;

import java.io.Serializable;

public class TypeA4 implements Serializable
{
  public Integer i1 = 1;
  public Object i2 = 2;
  public int i3 = 3;
  public long i4 = 4;
  
//  public TypeA4 overloadedMethod(Integer i) 
//  {
//    i1 = i;
//    return this;
//  }
  
  public TypeA4 overloadedMethod(Object i) 
  {
    i2 = i;
    return this;
  }
  
//  public TypeA3 overloadedMethod(int i) 
//  {
//    i3 = i;
//    return this;
//  }
//  
//  public TypeA3 overloadedMethod(long l) 
//  {
//    i4 = l;
//    return this;
//  }  
  
  public void voidMethod() {
    // do nothing.
  }

  public Integer getI1()
  {
    return i1;
  }

  public Object getI2()
  {
    return i2;
  }

  public int getI3()
  {
    return i3;
  }
  
  public long getI4()
  {
    return i4;
  }  
  
  public String toString() 
  {
    return "TypeA";
  }
    
}
