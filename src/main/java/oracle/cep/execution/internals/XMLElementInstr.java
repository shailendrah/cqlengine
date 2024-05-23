/* $Header: XMLElementInstr.java 06-jun-2008.09:58:48 skmishra Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    06/06/08 - cleanup
    skmishra    06/02/08 - adding ctr with xmldoc
    parujain    05/19/08 - evalname
    parujain    05/02/08 - Creation
 */

/**
 *  @version $Header: XMLElementInstr.java 06-jun-2008.09:58:48 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals;

import oracle.cep.common.Datatype;
import oracle.cep.execution.internals.memory.EvalContext;
import oracle.xml.parser.v2.XMLDocument;

public class XMLElementInstr {

  public Datatype              returnType;
  public String                elemName;
  public int                   elemNameRole;
  public int                   elemNamePos;
  public int                   numAttributs;
  public int[]                 attrRoles;
  public int[]                 attrPos;
  public XmlAttrName[]         attrNames;
  public Datatype[]            attrTypes;
  public int                   numChild;
  public Datatype[]            childTypes;
  public int[]                 childRoles;
  public int[]                 childPos;
  public XMLDocument           doc;
	  
  
  //used by simple pub functions: comment, cdata
  public XMLElementInstr(XMLDocument rootDoc)
  {
    this.doc = rootDoc;
    
    this.elemName = null;
    this.elemNameRole = -1;
    this.elemNamePos = -1;
    this.returnType = Datatype.XMLTYPE;
    this.numAttributs = -1;
    this.attrRoles = null;
    this.attrPos = null;
    this.attrNames = null;
    this.attrTypes = null;

    this.numChild = 0;
    this.childPos = null;
    this.childRoles = null;
    this.childTypes = null;

  }
  
  public XMLElementInstr(String name,Datatype returnType, int numAttrs, int numkids)
  {
    this.elemName = name;
    this.elemNameRole = -1;
    this.elemNamePos = -1;
    this.returnType = returnType;
    this.numAttributs = numAttrs;
    this.attrRoles = new int[numAttrs];
    this.attrPos = new int[numAttrs];
    this.attrNames = new XmlAttrName[numAttrs];
    this.attrTypes = new Datatype[numAttrs];
    
    this.numChild = numkids;
    this.childPos = new int[numkids];
    this.childRoles = new int[numkids];
    this.childTypes = new Datatype[numkids];
    doc = null;
  }
  
  public XMLElementInstr(int nameRole, int namePos, Datatype returnType, int numAttrs, int numkids)
  {
    this.elemName = null;
    this.elemNameRole = nameRole;
    this.elemNamePos = namePos;
    this.returnType = returnType;
    this.numAttributs = numAttrs;
    this.attrRoles = new int[numAttrs];
    this.attrPos = new int[numAttrs];
    this.attrNames = new XmlAttrName[numAttrs];
    this.attrTypes = new Datatype[numAttrs];
    
    this.numChild = numkids;
    this.childPos = new int[numkids];
    this.childRoles = new int[numkids];
    this.childTypes = new Datatype[numkids];
    doc = null;
  }
  
  public int getNumChild()
  {
    return this.numChild;
  }
  
  public int getNumAttributes()
  {
    return this.numAttributs;
  }
  
  public String getElementName()
  {
    return this.elemName;
  }
  
  public void setDocument(XMLDocument document)
  {
    this.doc = document;
  }
  
  public XMLDocument getDocument()
  {
    return doc;
  }
  
//toString
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<XMLElementInstr>");
      sb.append("NAME " +elemName);
    for(int i=0; i<numAttributs; i++)
    {
      if(i == 0)
        sb.append("<XMLATTRIBUTES>");
      sb.append("AttrType :" +attrTypes[i]);
      sb.append("< AttrRole=\"" + EvalContext.getRoleName(attrRoles[i])
              + "\" AttrPos=\"" + attrPos[i] + "\"/>");
      if(i == (numAttributs-1))
          sb.append("</XMLATTRIBUTES>");
    }
    for(int j=0 ;j<numChild; j++)
    {
      sb.append("< ChildNo: " +j +" ChildType: " +childTypes[j] + "ChildRole = " 
    		    +EvalContext.getRoleName(childRoles[j]) + " ChildPos= " +childPos[j] +"\"/>");
    }
    
    sb.append("</XMLElementInstr>");
    return sb.toString();
  }
  

}
