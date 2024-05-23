package oracle.cep.common;

import java.util.List;

/**
 * Ordering Constraint for a query.
 * 
 * @author sbishnoi
 *
 */
public class OrderingConstraint
{
  /** Kind of Ordering Requirement */
  private OrderingKind kind;
  
  /** List of Attributes which are used for ordering */
  private List<String> orderingAttributes;
  
  public OrderingConstraint(OrderingKind kind)
  {
    this.kind = kind;
  }

  public OrderingConstraint(OrderingKind kind, List<String> attrs)
  {
    this.kind = kind;
    this.orderingAttributes = attrs;
  }

  public OrderingKind getKind()
  {
    return kind;
  }

  public void setKind(OrderingKind kind)
  {
    this.kind = kind;
  }

  public List<String> getOrderingAttributes()
  {
    return orderingAttributes;
  }

  public void setOrderingAttributes(List<String> orderingAttributes)
  {
    this.orderingAttributes = orderingAttributes;
  }
  
  @Override
  public String toString()
  {
    return "ordering-type=" + kind + ", ordering-attributes="+ orderingAttributes;
  }
}
