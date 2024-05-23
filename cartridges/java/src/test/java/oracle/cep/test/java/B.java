package oracle.cep.test.java;

public class B extends A
{
  public int i1 = 1;
  
  public B test(int i) {
    i1 = i;
    return this;
  }
  
  public int getI1() {
    return i1;
  }

}
