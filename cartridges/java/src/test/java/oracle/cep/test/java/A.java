package oracle.cep.test.java;

public class A
{
  public Object i2 = new Integer(2);
  
  public static int testStaticField = 3;
  
  public A test(Object i) {
    i2 = i;
    return this;
  }
  
  public Object getI2() {
    return i2;
  }
  
  public static int testStatic() {
    return 1;
  }
  
  public int testOverloadedStatic(int i) {
    return i;
  }
  
  public static long testOverloadedStatic(long l) {
    return l;
  }
  
}
