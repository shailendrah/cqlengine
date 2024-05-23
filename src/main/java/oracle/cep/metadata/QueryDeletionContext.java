/* $Header: QueryDeletionContext.java 26-jun-2006.14:30:58 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      06/20/06 - Creation
 */

/**
 *  @version $Header: QueryDeletionContext.java 26-jun-2006.14:30:58 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

/**
 * Query Deletion Context
 * 
 * @since 1.0
 */

public class QueryDeletionContext
{
  Query                  query;
  LinkedList<Integer>    listQids; // list of deleted query ids

  public QueryDeletionContext(Query query)
  {
    this.query = query;
    listQids   = new LinkedList<Integer>();
  }

  public Query getQuery()
  {
    return query;
  }

  public void setQuery(Query query)
  {
    this.query = query;
  }

  public void addQueryId(int id) 
  {
    listQids.add(new Integer(id));
  }

  public void removeQueryId(int id) 
  {
    Iterator<Integer> iter = listQids.iterator();
    
    while (iter.hasNext())
    {
      int i = iter.next().intValue();
      if (id == i) 
      {
	iter.remove();
	return;
      }
    }
    
    assert false;
  }

  public List<Integer> getListQids() 
  {
    return listQids;
  }
}

