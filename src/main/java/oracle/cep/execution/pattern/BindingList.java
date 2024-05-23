/* $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/BindingList.java /main/1 2009/03/13 13:23:43 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    03/13/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/BindingList.java /main/1 2009/03/13 13:23:43 sborah Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;
  
import java.util.ArrayList;
  
import oracle.cep.dataStructures.internal.IListNodeElem;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;

  public class BindingList 
    extends ArrayList<Binding>
    implements IListNodeElem
  {
    static final long serialVersionUID = -528237316659552210L;

    private ITuplePtr headerTuplePtr;
    
    public BindingList(){
      this.headerTuplePtr = null;  
    }
    
    public BindingList(ITuplePtr hdrPtr) {
      this.headerTuplePtr = hdrPtr;
    }
    
    public ITuplePtr getHeaderTuplePtr(){
      return this.headerTuplePtr;
    }
    
    public boolean evict() throws ExecException
    {
      return false;
    }
  }
