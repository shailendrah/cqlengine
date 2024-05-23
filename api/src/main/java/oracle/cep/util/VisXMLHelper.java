/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/util/VisXMLHelper.java /main/5 2009/12/29 20:26:09 parujain Exp $ */

/* Copyright (c 2009, Oracle and/or its affiliates.All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    12/21/09 - outer join
 skmishra    04/21/09 - adding xml version and encoding
 mthatte     03/13/09 - refactoring
 mthatte     03/12/09 - adding orderByAttrTag
 mthatte     02/19/09 - adding tags
 mthatte     01/23/09 - helper class to create xml represn of ddl's
 mthatte     01/23/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/util/VisXMLHelper.java /main/5 2009/12/29 20:26:09 parujain Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPQueryNode;

public class VisXMLHelper
{
  static int           operatorID  = 0;

  public static String aliasTag = "alias";
  public static String argumentNameTag = "argument-name";
  public static String ascendingTag = "ascending";
  public static String corrAttrNamesTag = "corr-attr-names";
  public static String cqlPropertyTag = "cql-property";
  public static String defineListTag = "define-list";
  public static String defineAttrTag = "define-attr";
  public static String distinctTag = "distinct";
  public static String dstreamOperator = "DStream";
  public static String durationTag = "duration";
  public static String istreamOperator = "IStream"; 
  public static String filterOperator = "Filter";
  public static String groupByTag = "group-by";
  public static String groupListTag = "group-list";
  public static String havingClauseTag = "having-clause";
  public static String inputsTag = "inputs";
  public static String inputTag ="input";
  public static String intersectOperator = "Intersect";
  public static String joinOperator ="Join";
  public static String measureAttrTag = "measure-attr";
  public static String measuresListTag ="measures-list";
  public static String minusOperator = "Minus";
  public static String multipleDurationTag = "multiple-duration";
  public static String nullsFirstTag = "nulls-first";
  public static String onClauseTag = "on-clause";
  public static String operatorTag = "Operator";
  public static String operatorIdAttr = "ID";
  public static String operatorTypeAttr = "type";
  public static String orderByAttrTag = "order-by-attr";
  public static String orderBySymbolTag =  "order-by-symbol";
  public static String orderByListTag =  "order-by-list";
  public static String outerJoinOperator = "OuterJoin";
  public static String outerJoinTypeTag = "outer-join-type";
  public static String outputOperator =  "Output";
  public static String outputNameTag = "output-name";
  public static String outputTypeQuery = "Query";
  public static String outputTypeTag = "output-type";
  public static String outputTypeView= "View";
  public static String partitionAttrTag = "partition-attr";
  public static String partitionByTag = "partition-by";
  public static String partitionByListTag = "partition-by-list";
  public static String patternAttrTag = "pattern-attr";
  public static String patternListTag = "pattern-list";
  public static String patternOperator = "Pattern";
  public static String patternSkipTag = "pattern-skip";
  public static String predicateTag = "predicate";
  public static String predicatesTag = "predicates";
  public static String rstreamOperator = "RStream";
  public static String ruleRootAttr = "root";
  public static String ruleTag = "Rule";
  public static String selectAttrTag = "select-attr";
  public static String selectExpressionTag = "select-expression";
  public static String selectListTag = "select-list";
  public static String selectOperator = "Select";
  public static String sourceOperator = "Source";
  public static String sourceTypeTag = "source-type";
  public static String sourceNameTag = "source-name";
  public static String subsetNameTag = "subset-name";
  public static String subsetTag = "subset";
  public static String subsetsTag = "subsets";
  public static String timerEventTag = "timer-event";
  public static String unionAllTag = "union-all";
  public static String unionOperator = "Union";
  public static String viewAttrNameTag = "name";
  public static String viewAttrTag = "view-attr";
  public static String viewAttrTypeTag = "type";
  public static String viewOutputType = "view-output-type";
  public static String viewSchemaListTag = "view-schema-list";
  public static String windowOperator = "Window";
  public static String windowRangeParamsTag = "range-params";
  public static String windowRowsValueTag = "rows";
  public static String windowSlideValueTag = "slidevalue";
  public static String windowSlideUnitTag = "slideunit";
  public static String windowTypeExtensible = "extensible";
  public static String windowTypeTag = "type";
  public static String windowTypeRows = "rows";
  public static String windowTypeNow = "now";
  public static String windowTypePartition = "partition";
  public static String windowTypeRange = "range-time";
  public static String windowTypeUnbounded = "range-unbounded";
  public static String windowTypeRowTime = "row-time";
  public static String windowTimeValueTag = "timevalue";
  public static String windowTimeUnitTag = "timeunit";
  public static String xmlVersionEncodingTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

 
  public static String createCqlPropertyXml(String cqlProperty)
  {
    assert cqlProperty != null;
    return "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.cqlPropertyTag, cqlProperty, null, null);
  }
  
  public static String createOperatorTag(String operatorType, int operatorId, String innerXml)
  {
    return XMLHelper.buildElement(true, operatorTag, innerXml, new String[]{operatorIdAttr,operatorTypeAttr}, new String[]{String.valueOf(operatorId),operatorType});
  }
  
  public static String createInputsXml(int[] inputs)
  {
    if(inputs == null)
      return "";
    StringBuilder inputXml = new StringBuilder(20);
    for(int inpId : inputs)
      inputXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.inputTag, String.valueOf(inpId), null, null));
    return "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.inputsTag, inputXml.toString().trim(), null, null);
  }
  
  
  private static void createOutputOperator(StringBuffer queryXml, int outputId,
      String qName, String ddl, boolean isView)
  {
    StringBuilder outputXml = new StringBuilder(50);
    String outType;

    if (isView)
      outType = outputTypeView;
    else {
      outType = outputTypeQuery;
      //strip the create query as part of the ddl
      ddl = ddl.toLowerCase().replaceFirst("create query " + qName + " as " , "");
    }
    String inputXml = "\n\t\t"
        + XMLHelper.buildElement(true, inputTag, String.valueOf(outputId - 1),
            null, null);

    outputXml.append("\n\t"
        + XMLHelper.buildElement(true, inputsTag, inputXml, null, null));
    outputXml.append("\n\t"
        + XMLHelper.buildElement(true, outputTypeTag, outType, null, null));
    outputXml.append("\n\t"
        + XMLHelper.buildElement(true, cqlPropertyTag, XMLHelper.toHTMLString(ddl), null,
            null));
    outputXml.append("\n\t"
        + XMLHelper.buildElement(true, outputNameTag, qName, null, null));

    queryXml.append("\n"
        + XMLHelper.buildElement(true, operatorTag,
            outputXml.toString().trim(), new String[]
            { operatorIdAttr, operatorTypeAttr }, new String[]
            { String.valueOf(outputId), outputOperator }));
  }

  // surrounds qc xml with <Rule> tag
  private static String createOuterTag(StringBuffer queryXml, int outputId)
  {
    return XMLHelper.buildElement(true, ruleTag, "\n"
        + queryXml.toString().trim() + "\n", new String[]
    { ruleRootAttr }, new String[]
    { String.valueOf(outputId)});
  }

  /**
   * This method walks a parseTree created from a ddl and generates an xml
   * representation for the query constructor in the visualizer
   * 
   * @param qName
   *                The name of the query
   * @param ddl
   *                query text
   * @param isView
   *                whether it is a view or a query
   * @return xml representation of the ddl
   * @throws CEPException
   */
  public static String getQCXML(String qName, String ddl, boolean isView, CEPQueryNode queryTree)
      throws Exception
  {
    // create a 1kb buffer for xml
    StringBuffer queryXml = new StringBuffer(1024);

    int outputId = 1;

    outputId = queryTree.toQCXML(queryXml, 1);
    createOutputOperator(queryXml, outputId, qName, ddl, isView);
    String xml = createOuterTag(queryXml, outputId);
    return xml;
  }
}
