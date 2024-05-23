package oracle.cep.test.java;

import java.io.Serializable;

public class TypeA implements Serializable
{
  public Integer i1 = 1;
  public Integer o1 = 1;
  public Object i2 = 2;
  public int i3 = 3;
  public long i4 = 4;
  public char [] i5 = "i5".toCharArray();
  
  public TypeB nestedType = new TypeB();
  
  public TypeA() {
  }
  
  public TypeA(TypeB b) {
    nestedType = b;
  }
  
  public TypeA overloadedMethod(Integer i) 
  {
    o1 = i;
    return this;
  }
  
  public TypeA overloadedMethod(Object i) 
  {
    i2 = i;
    return this;
  }
  
  public TypeA overloadedMethod(int i) 
  {
    i3 = i;
    return this;
  }
  
  public TypeA overloadedMethod(long l) 
  {
    i4 = l;
    return this;
  }  
  
  public void voidMethod() {
    // do nothing.
  }

  public Integer getI1()
  {
    return i1;
  }
  
  public Integer getO1()
  {
    return o1;
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
  
  public char [] getI5() 
  {
    return i5;
  }
  
  public void setI5(char [] a) 
  {
    i5 = a;
  }
  
  public String toString() 
  {
    return "TypeA";
  }
    
}
