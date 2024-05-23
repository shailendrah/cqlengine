/* $Header: DumpDesc.java 19-jan-2008.00:08:48 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    DumpDesc annotation is to specify instructions for default dumps.
    It has following parameters:
    boolean ignore - set true to ignore the target object or field.
    String tag - tag name for the target object or field
                 default is "" and it means either java class name or field name
                 will be used as the tag.
    String[] attribTags - tag names for attributes of object
    String[] attribVals - getters for attributes of object. 
                if it starts with "@", it's considered as a field name.
    String[] valueTags - tag names for values of object
    String[] values - getters for values of object
    
    ex)
    class BinJoinState
    {
      ITuplePtr output;
      @DumpDesc(ignore=true) IQueueElement queueBuf;
    }
    queueBuf will not be dumped.
      
    @DumpDesc(attribTags={"Id", "Length"}, 
              attribVals={"getId", "getLength"})
    class DoublyList
    {
    }
    
    @DumpDesc(attribTags={"Id", "Prev", "Next"}, 
              attribVals={"getId", "@prevId", "@nextId"},
              valueTags={"Tuple"},
              values={"getNodeElem"})
    class DoublyListNode
    {
    }
    
    access
    DoublyList.Id = invoke "getId" method
    DoublyList.Length = invoke "getLength" method
    DoublyListNode.Id = invoke "getId" method
    DoublyListNode.Prev = get "prevId" field
    DoublyListNode.Next = get "nextId" field
    DoublyListNode.Tuple = invoke "getNodeElem" method

    xml output
      <DoublyList Id="1" Length="1">
         <DoublyListNode Id="1" Prev="-1" Next="-1">
            <Tuple>...</Tuple>
         </DoublyListNode>
      </DoublyList>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/28/07 - Creation
 */

/**
 *  @version $Header: DumpDesc.java 19-jan-2008.00:08:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface DumpDesc 
{
  boolean ignore() default false;
  boolean autoFields() default false;
  String tag() default "";
  String[] attribTags() default "";
  String[] attribVals() default "";
  String[] valueTags() default "";
  String[] values() default "";
  int infoLevel() default -1;
  int evPinLevel() default -1;
  int evUnpinLevel() default -1;
  int dumpLevel() default -1;
  int verboseDumpLevel() default -1;
}
