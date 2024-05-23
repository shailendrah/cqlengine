package oracle.cep.test.java;

public class TypeConverter
{
  // Java primitives
  boolean bo = true;
  char ch = 'a';
  byte by = 1;
  short sh = 2;
  int in = 3;
  long lo = 4;
  float fl = 5;
  double du = 6.0;
  String st = "bb";
  char [] chA = new char [] {'c', 'c' };
  int [] inA = new int [] {7,7};
  String [] stA = new String [] {"de", "fg"};
  
  // Additional CQL primitives
  
  // Special cases
  
  public TypeConverter() {
  }
  
  public TypeConverter(boolean b) {
    bo = b;
  }
  
  public TypeConverter(char c) {
    ch = c;
  }
  
  public TypeConverter(byte b) {
    by = b;
  }
  
  public TypeConverter(short s) {
    sh = s;
  }
  
  public TypeConverter(int i) {
    in = i;
  }
  
  public TypeConverter(long l) {
    lo = l;
  }
  
  public TypeConverter(float f) {
    fl = f;
  }
  
  public TypeConverter(double d) {
    du = d;
  }
  
  public TypeConverter(String s) {
    st = s;
  }
  
  public TypeConverter(char [] c) {
    chA = c;
  }
  
  public boolean isBo()
  {
    return bo;
  }
  
  public TypeConverter setBo(boolean bo)
  {
    this.bo = bo;
    return this;
  }
  
  public char getCh()
  {
    return ch;
  }
  
  public TypeConverter setCh(char ch)
  {
    this.ch = ch;
    return this;
  }
  
  public byte getBy()
  {
    return by;
  }
  
  public TypeConverter setBy(byte by)
  {
    this.by = by;
    return this;
  }
  
  public short getSh()
  {
    return sh;
  }
  
  public TypeConverter setSh(short sh)
  {
    this.sh = sh;
    return this;
  }
  
  public int getIn()
  {
    return in;
  }
  
  public TypeConverter setIn(int in)
  {
    this.in = in;
    return this;
  }
  
  public long getLo()
  {
    return lo;
  }
  
  public TypeConverter setLo(long lo)
  {
    this.lo = lo;
    return this;
  }
  
  public float getFl()
  {
    return fl;
  }
  
  public TypeConverter setFl(float fl)
  {
    this.fl = fl;
    return this;
  }
  
  public double getDu()
  {
    return du;
  }
  
  public TypeConverter setDu(double du)
  {
    this.du = du;
    return this;
  }

  public String getSt()
  {
    return st;
  }

  public TypeConverter setSt(String st)
  {
    this.st = st;
    return this;
  }

  public char[] getChA()
  {
    return chA;
  }

  public TypeConverter setChA(char[] chA)
  {
    this.chA = chA;
    return this;
  }

  public int[] getInA()
  {
    return inA;
  }
  
  public String getInAAsString()
  {
    StringBuilder builder = new StringBuilder();
    for (int i : inA)
    {
      builder.append(i);
    }
    
    return builder.toString();
  }

  public TypeConverter setInA(int[] inA)
  {
    this.inA = inA;
    return this;
  }

  public String[] getStA()
  {
    return stA;
  }

  public void setStA(String[] stA)
  {
    this.stA = stA;
  }
  
}
