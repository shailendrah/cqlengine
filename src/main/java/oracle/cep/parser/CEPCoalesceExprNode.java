package oracle.cep.parser;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.common.UnaryOp;
import oracle.cep.exceptions.ParserError;
import oracle.cep.exceptions.CEPException;

public class CEPCoalesceExprNode extends CEPSearchedCaseExprNode 
{
  /**
   * Construct a Parser Node for COALESCE subclause.
   * As COALESCE is similar to SEARCHED-CASE, CQL Engine will transform
   * COALESCE subclause to SearchedCase Expression Parser Node.
   *
   * Transform a COALESCE expression to a Search Case Expression can be done using:
   * COALESCE (expr1, expr2)
   * is equivalent to:
   *
   * CASE WHEN expr1 IS NOT NULL THEN expr1 ELSE expr2 END
   */
  public CEPCoalesceExprNode(List<CEPExprNode> exprs) throws CEPException
  {
    super();

    /** COALESCE supports minimum two arguments */
    if (exprs.size() < 2)
      throw new CEPException(ParserError.NOT_ENOUGH_ARG_FOR_FUNCTION, startOffset,
                            endOffset, new Object[]{"coalesce", "two"});

    List<CEPCaseConditionExprNode> li = new ArrayList<CEPCaseConditionExprNode>();
    CEPExprNode elseExpr = null;
    int idx = 0;

    int numExprs = exprs.size();
    for (CEPExprNode expr : exprs)
    {
      if (idx++ == numExprs-1)
      {
        elseExpr = expr;
        break;
      }
      else
      {
        li.add(new CEPCaseConditionExprNode(new CEPBaseBooleanExprNode(UnaryOp.IS_NOT_NULL, expr), expr));
      }
    }
    constructParams(li, elseExpr);
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("COALESCE()=[");
    sb.append(super.toString());
    sb.append("]");
    return sb.toString();
  }
}
