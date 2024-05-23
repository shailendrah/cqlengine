package oracle.cep.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.io.Serializable;

public class TestTableFunction implements Serializable
{
  public ArrayList<Integer> intArrayList;
 
  public HashMap<Integer,String> developers;
  public HashMap<Integer,String> qaengineers;
 
  public TestTableFunction(Object o)
  {
    intArrayList = new ArrayList<Integer>();
    intArrayList.add(100);
    intArrayList.add(200);
    
    // Initialize the map
    developers = new HashMap<Integer,String>();
    developers.put(2, "Mohit");
    developers.put(4, "Unmesh");
    developers.put(3, "Sandeep");
    developers.put(1, "Swagat");

    qaengineers = new HashMap<Integer,String>();
    qaengineers.put(4, "Terry");
    qaengineers.put(5, "Tony");
    qaengineers.put(3, "Junger");
    qaengineers.put(1, "Arthur");

  }

  public static ArrayList<Integer> testArrayList(int c1)
  {
    ArrayList<Integer> intList = new ArrayList<Integer>();
    intList.add(c1);
    intList.add(c1*2);
    return intList;
  }

  /**
   * To test array list of complex types
   */

  public static ArrayList<TableFunctionTestData> testComplexArrayList()
  {
    ArrayList<TableFunctionTestData> complexTypeArrayList 
      = new ArrayList<TableFunctionTestData>();
    complexTypeArrayList.add(new TableFunctionTestData("ComplexEvent1"));
    complexTypeArrayList.add(new TableFunctionTestData("ComplexEvent2"));
    complexTypeArrayList.add(new TableFunctionTestData("ComplexEvent3"));
    complexTypeArrayList.add(new TableFunctionTestData("ComplexEvent4"));
    return complexTypeArrayList;
  }

  /**
   * To test array of complex types
   */
  public static TableFunctionTestData[] testComplexArray()
  {
    return testComplexArrayList().toArray(new TableFunctionTestData[4]);
  }
 
  /**
   * To test array of java integer types
   */
  public static Integer[] testArray(int c1)
  {
    return testArrayList(c1).toArray(new Integer[2]);
  }
  
  /**
   * To test NONSTATIC array list of java integer types
   */
  public ArrayList<Integer> testNonStaticArrayList2()
  {
    return intArrayList;
  }

  /**
   * To test non iterable type
   */
  public static int testNonIterable(int c1)
  {return 1;}


  /**
   * To Test Java.lang.Iterator
   */
  public static Iterator<Integer> testIterator()
  {
    ArrayList<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    return list.iterator();
  }

  /**
   * To Test Collections
   */
  public Collection<String> getEmployees(int exp_yrs)
  {
    LinkedList<String> employees = new LinkedList<String>();
    employees.add(developers.get(exp_yrs));    
    employees.add(qaengineers.get(exp_yrs));
    return employees;
  }

  public static LinkedList<Integer> testLinkedList()
  {
    LinkedList<Integer> list = new LinkedList<Integer>();
    list.add(100);
    list.add(5);
    list.add(11);
    list.add(15);
    list.add(5);
    return list;
  }
}
