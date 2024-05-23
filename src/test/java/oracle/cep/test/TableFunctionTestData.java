package oracle.cep.test;

import java.io.Serializable;

public class TableFunctionTestData implements Serializable
{
  String data;

  TableFunctionTestData(String val)
  {this.data = val;}

  public String toString()
  {return data;}
}
