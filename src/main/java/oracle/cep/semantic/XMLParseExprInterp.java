/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLParseExprInterp.java /main/3 2015/03/20 03:04:38 udeshmuk Exp $ */

/* Copyright (c) 2008, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk   03/18/15 - call setName
 parujain   08/26/08 - semantic exception offset
 skmishra   06/12/08 - bug
 skmishra   06/05/08 - cleanup
 skmishra   05/28/08 - 
 mthatte    05/19/08 - Creation
 */

package oracle.cep.semantic;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPXMLParseExprNode;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLParseExprInterp.java /main/3 2015/03/20 03:04:38 udeshmuk Exp $
 * @author mthatte
 * @since release specific (what release of product did this appear in)
 */

public class XMLParseExprInterp extends NodeInterpreter
{

  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    super.interpretNode(node, ctx);
    
    String name = new String("XMLPARSE(");

    assert node instanceof CEPXMLParseExprNode;
    CEPXMLParseExprNode xParseNode;
    CEPExprNode nodeValue;
    NodeInterpreter exprInterp;
    Expr parseExpr = null;
    Expr xExpr = null;

    xParseNode = (CEPXMLParseExprNode) node;
    nodeValue = xParseNode.getValue();
    exprInterp = InterpreterFactory.getInterpreter(nodeValue);
    exprInterp.interpretNode(nodeValue, ctx);
    parseExpr = ctx.getExpr();

    if (parseExpr.getReturnType() != Datatype.CHAR)
      throw new SemanticException(SemanticError.XML_PUB_FUNC_ARG_ERROR, 
      xParseNode.getStartOffset(), xParseNode.getEndOffset(),
      new Object[]{ parseExpr.getName(), "XMLPARSE" });

    if (parseExpr.getExprType() == ExprType.E_CONST_VAL)
    {
      XMLDocument resultDoc = null;
      Node resultNode;
      String charValue = ((ConstCharExpr) parseExpr).getValue();
      DOMParser dom = new DOMParser();
      Reader reader = new StringReader(charValue);

      try
      {
        switch (xParseNode.getKind())
        {
        //if wellformed is set add a fake tag
        case CONTENT:
          StringBuffer buf = new StringBuffer();
          if (xParseNode.isWellFormed())
          {
            buf.append("<tag>").append(charValue).append("</tag>");
          } else
          {
            buf.append(charValue);
          }

          reader = new StringReader(buf.toString());
          dom.parse(reader);
          resultDoc = dom.getDocument();
          resultNode = resultDoc.createDocumentFragment();

          //Since we added a fake tag for the parse, strip it off
          //If not, get all child nodes and append to result
          Node child;
          
          if(xParseNode.isWellFormed())
            child = resultDoc.getFirstChild();
          else
            child = resultDoc;
          
          Node nextChild = null;

          child = child.getFirstChild();

          while (child != null)
          {
            nextChild = child.getNextSibling();
            ((DocumentFragment) resultNode).appendChild(child);
            child = nextChild;
          }
          xExpr = new ConstXmltypeExpr(resultNode);
          break;

        //parse the document to ensure its a singly rooted document
        case DOCUMENT:
          dom.parse(reader);
          resultDoc = dom.getDocument();
          if (resultDoc.getChildNodes().getLength() != 1)
            throw new SemanticException(SemanticError.INVALID_XML_PARSE_EXPR,
                xParseNode.getStartOffset(), xParseNode.getEndOffset(),
                new Object[]{ charValue });
          xExpr = new ConstXmltypeExpr(resultDoc);
          break;

        //either content or document must be specified
        default:
          assert false : xParseNode.getKind();
        }
        
      } catch (SAXException se)
      {
        throw new SemanticException(SemanticError.XML_PUB_FUNC_ARG_ERROR,
            xParseNode.getStartOffset(), xParseNode.getEndOffset(),
            new Object[]
            { charValue, "XMLPARSE" });
      } catch (IOException ioe)
      {
        throw new SemanticException(SemanticError.XML_PUB_FUNC_ARG_ERROR,
                  xParseNode.getStartOffset(), xParseNode.getEndOffset(),
                  new Object[]
            { charValue, "XMLPARSE" });
      }

      assert xExpr != null;
      name = name + xParseNode.getKind()+ " "+ xExpr.getName() +")";
      xExpr.setName(name,  false);
      ctx.setExpr(xExpr);
    }
    else
    {
      xExpr = new XMLParseExpr(parseExpr, xParseNode.isWellFormed(), xParseNode
          .getKind());
      name = name + xParseNode.getKind()+ " "+parseExpr.getName()+")";
      xExpr.setName(name, false);
      ctx.setExpr(xExpr);
    }
  }
}
