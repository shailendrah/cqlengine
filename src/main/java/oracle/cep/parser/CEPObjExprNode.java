package oracle.cep.parser;

import java.util.List;


public class CEPObjExprNode extends CEPExprNode
{
  private CEPExprNode lvalue;
  private CEPStringTokenNode memberName;
  private List<CEPStringTokenNode> qualifiedName;
  private CEPExprNode [] paramList;
  private CEPIntTokenNode index;

  /**
   * Field access
   * 
   * @param lvalue
   * @param memberName
   */
  public CEPObjExprNode(CEPExprNode lvalue, CEPStringTokenNode memberName) 
  {  
    this.lvalue  = lvalue;
    this.memberName = memberName;
    
    setStartOffset(lvalue.getStartOffset());
    setEndOffset(memberName.getEndOffset());
  }
  
  /**
   * Index access
   * 
   * @param lvalue
   * @param index
   */
  public CEPObjExprNode(CEPExprNode lvalue, CEPIntTokenNode index) 
  {  
    this.lvalue  = lvalue;
    this.index = index;
    
    setStartOffset(lvalue.getStartOffset());
    setEndOffset(index.getEndOffset());
  }
  
  /** 
   * Non-static method access
   * 
   * @param lvalue
   * @param memberName
   * @param paramList
   */
  public CEPObjExprNode(CEPExprNode lvalue,
      CEPStringTokenNode memberName,
      List<?> paramList) {
    this.lvalue  = lvalue;
    this.memberName = memberName;
    
    if (paramList != null) 
      this.paramList = 
        (CEPExprNode[]) paramList.toArray(new CEPExprNode[0]);
    
    setStartOffset(lvalue.getStartOffset());
    
    if ((this.paramList != null) && (this.paramList.length > 0))
      setEndOffset(this.paramList[this.paramList.length - 1].getEndOffset());
    else
      setEndOffset(memberName.getEndOffset());
  }
  
  /**
   * Non-static method access
   * 
   * @param lvalue
   * @param memberName
   * @param paramList
   * @param endOffset
   */
  public CEPObjExprNode(CEPExprNode lvalue,
      CEPStringTokenNode memberName,
      CEPExprNode [] paramList) {
    this.lvalue  = lvalue;
    this.memberName = memberName;
    this.paramList = paramList;
    
    setStartOffset(lvalue.getStartOffset());
    
    if ((this.paramList != null) && (this.paramList.length > 0))
      setEndOffset(this.paramList[this.paramList.length - 1].getEndOffset());
    else
      setEndOffset(memberName.getEndOffset());
  }
  
  
  /**
   * Static methods and constructors
   * 
   * @param qualifiedName
   */
  public CEPObjExprNode(List<CEPStringTokenNode> qualifiedName) 
  {  
    this(qualifiedName, null);
  }
  
  /**
   * Static methods and constructors
   * 
   * @param qualifiedName
   * @param paramList
   */
  public CEPObjExprNode(List<CEPStringTokenNode> qualifiedName, List<?> paramList) 
  {  
    // REVIEW uhm, we should probably re-work this...
    assert !qualifiedName.isEmpty() : "qualifiedName must have at least one element";
    
    this.qualifiedName = qualifiedName;
    
    if (paramList != null) 
      this.paramList = 
        (CEPExprNode[]) paramList.toArray(new CEPExprNode[0]);
    
    setStartOffset(qualifiedName.get(0).getStartOffset());
    
    if ((this.paramList != null) && (this.paramList.length > 0))
      setEndOffset(this.paramList[this.paramList.length - 1].getEndOffset());
    else
      setEndOffset(this.qualifiedName.get(this.qualifiedName.size() - 1).getEndOffset());
  }
  
  public void setParams(CEPExprNode[] params)
  {
    this.paramList = params;
  }

  public String toString()
  {
    if (myString != null)
    {
      if (alias != null)
        return myString + " AS " + alias;
      else
        return myString;

    }
    else
    {
      if (alias == null)
        return getExpression();
      else
        return getExpression() + " AS " + alias;
    }
  }

  public String getExpression()
  {
    if(myString!=null)
      return myString;
    else {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append(" ");
      
      if (lvalue != null) 
        buffer.append(lvalue.toString());
      
      if (qualifiedName != null) 
      {
        boolean firstIdentifier = true;
        
        for (CEPStringTokenNode stringNode : qualifiedName)
        {
          if (!firstIdentifier) 
          {
            if (stringNode.isLink())
              buffer.append("@");
            else
              buffer.append(".");
          } 
          else
            firstIdentifier = false;
          
          buffer.append(stringNode.getValue());
        }
      }
      
      if (memberName != null)
        buffer.append(".").append(memberName.getValue().toString());
      
      if (paramList != null) 
      {
        buffer.append("(");
        if (paramList.length > 0)
        {
          for (CEPExprNode param : paramList)
          {
            buffer.append(param + ",");
          }

          buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append(")");
      }
      
      if (index != null) 
      {
        buffer.append("[");
        buffer.append(index.getValue());
        buffer.append("]");
      }
      
      buffer.append(" ");
      
      myString = buffer.toString();
    }
    
    return myString;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEPObjExprNode other = (CEPObjExprNode) obj;
    
    if(!this.getExpression().equals(other.getExpression()))
      return false;
    
    return true;
  }

  public CEPExprNode getLValue()
  {
    return lvalue;
  }

  public CEPStringTokenNode getMemberName()
  {
    return memberName;
  }
  
  public List<CEPStringTokenNode> getQualifiedName()
  {
    return qualifiedName;
  }

  public CEPExprNode [] getParams()
  {
    return paramList;
  }
  
  public CEPIntTokenNode getIndex()
  { 
    return index;
  }
}
