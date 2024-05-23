/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/memory/AEval.java /main/49 2015/11/02 17:02:25 sbishnoi Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares AEval in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  11/01/15 - bug 22105463
 sbishnoi  10/15/13 - bug 17077931
 sbishnoi  10/08/13 - bug 17571566
 sbishnoi  07/31/13 - bug 17180183
 alealves  07/30/13 - Timestamp must be in nano
 sbishnoi  10/09/12 - XbranchMerge
                      sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  09/12/12 - XbranchMerge sbishnoi_bug-14286422_ps6_pt.11.1.1.7.0 from
                      st_pcbpel_pt-11.1.1.7.0
 sbishnoi  10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0 from
                      st_pcbpel_pt-11.1.1.7.0
 sbishnoi  10/01/12 - improving error message
 sbishnoi  09/10/12 - XbranchMerge sbishnoi_bug-14286422_ps6 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  02/23/12 - XbranchMerge sbishnoi_bug-13530508_ps6 from main
 sbishnoi  02/22/12 - fix apple bug
 sbishnoi  01/12/12 - support of timestamp timezone
 alealves  12/20/11 - XbranchMerge alealves_bug-12873645_cep from main
 sbishnoi  09/02/11 - support for interval formats
 anasrini  12/19/10 - remove eval() and setEvalContext
 anasrini  12/13/10 - eval parallelism
 sbishnoi  02/04/10 - adding the cause of user defined execution exception
 sborah    06/30/09 - support for bigdecimal
 alealves  05/27/09 - bug 8553059
 hopark    05/22/09 - fix trim crlf issue on windows
 sborah    05/21/09 - remove max_instrs limit
 hopark    04/09/09 - copy op optimization
 sborah    03/03/09 - log to investigate bug 8270930
 sborah    03/02/09 - fix for bug 8290106
 hopark    02/17/09 - support boolean as external datatype
 hopark    02/16/09 - objtype support
 udeshmuk  01/13/09 - use different error message for wrong format and wrong
                      value in to_timestamp
 udeshmuk  12/30/08 - Add check to see if format is null in to_timestamp
 sbishnoi  12/24/08 - adding support for BIGINT_TO_TIMESTAMP
 sbishnoi  12/07/08 - support for generic data soruce
 hopark    11/29/08 - use CEPDateFormat
 hopark    10/10/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 skmishra  07/18/08 - adding order by to xmlagg
 sbishnoi  08/18/08 - 
 udeshmuk  08/18/08 - fix xmlagg assertion error.
 skmishra  07/22/08 - bug 7243185
 skmishra  06/16/08 - adding xml_agg instr handlers
 skmishra  06/12/08 - adding xmlparse,xmlconcat, xmlcomment, xmlcdata
 parujain  06/06/08 - name_expr null for xmlfns
 parujain  05/19/08 - evalname
 parujain  05/02/08 - XMLElement support
 hopark    03/05/08 - xml spill
 udeshmuk  02/22/08 - 
 hopark    02/05/08 - parameterized error
 najain    02/04/08 - object representation of xml
 udeshmuk  01/31/08 - support for double data type.
 najain    11/06/07 - xquery support
 parujain  11/19/07 - Prepared statement
 hopark    10/22/07 - remove TimeStamp
 udeshmuk  10/24/07 - removing support for void
 udeshmuk  10/18/07 - add check for void in invokeAggrFunction.
 udeshmuk  10/17/07 - restructure code for UDA support on all data types.
 hopark    09/04/07 - optimize
 rkomurav  07/12/07 - add uda related ops
 hopark    06/19/07 - cleanup
 sbishnoi  06/12/07 - support for multi-arg UDAs
 sbishnoi  06/05/07 - support of bigint argument in func expression 
 parujain  06/08/07 - bug fix
 najain    05/22/07 - add cause/action
 hopark    05/16/07 - remove printStackTrace
 parujain  05/02/07 - UserDefined Functions statistics
 hopark    04/20/07 - change pinTuple semantics
 parujain  04/16/07 - Userdefined fns throws error
 parujain  03/30/07 - Case Evaluation
 hopark    03/24/07 - optimize pin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 hopark    03/06/07 - spill-over support
 rkomurav  01/03/07 - null support for UDA
 rkomurav  12/14/06 - implement NULL_CPY, SUM opcodes, COUNT opcodes
 hopark    11/16/06 - add bigint datatype
 dlenkov   10/16/06 - byte datatype support
 parujain  10/12/06 - interval timestamp operations
 parujain  10/05/06 - Generic timestamp datatype
 anasrini  10/09/06 - SYSTIMESTAMP support
 parujain  09/25/06 - NVL implementation
 dlenkov   09/22/06 - conversion support
 parujain  09/21/06 - To_timestamp built-in function
 najain    09/20/06 - suppport for in-built functions
 najain    09/11/06 - add concatenate
 parujain  08/30/06 - Handle null values
 parujain  08/04/06 - Timestamp datastructure
 anasrini  07/17/06 - support for user defined aggregations 
 anasrini  06/20/06 - support for functions 
 najain    04/28/06 - user-defined functions 
 anasrini  03/30/06 - add method noOp 
 anasrini  03/27/06 - add toString 
 anasrini  03/14/06 - add default constructor
 skaluska  02/12/06 - Creation
 skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/memory/AEval.java /main/49 2015/11/02 17:02:25 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals.memory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.common.TimestampFormat;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.CaseInstr;
import oracle.cep.execution.internals.ExternalInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.ISortedArray;
import oracle.cep.execution.internals.SortedTuplePtrArray;
import oracle.cep.execution.internals.XMLElementInstr;
import oracle.cep.execution.xml.IXmlContext;
import oracle.cep.execution.xml.PreparedXQuery;
import oracle.cep.execution.xml.XMLItem;
import oracle.cep.execution.xml.XMLSequence;
import oracle.cep.execution.xml.XmlManager;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.functions.AggrBigDecimal;
import oracle.cep.extensibility.functions.AggrBigInt;
import oracle.cep.extensibility.functions.AggrBoolean;
import oracle.cep.extensibility.functions.AggrByte;
import oracle.cep.extensibility.functions.AggrChar;
import oracle.cep.extensibility.functions.AggrDouble;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrInterval;
import oracle.cep.extensibility.functions.AggrObj;
import oracle.cep.extensibility.functions.AggrTimestamp;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.parser.CartridgeHelper;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLCDATA;
import oracle.xml.parser.v2.XMLComment;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLDocumentFragment;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLNode;
import oracle.xquery.XQMesg;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author skaluska
 */

public class AEval implements IAEval
{
  private ExecContext execContext;
  
  protected int                numInstrs;
  protected AInstr             instrs[];
  protected LinkedList<AInstr> instrsList;

  private boolean isCompiled;
 
  private int[] copySrcAttrs;
  private int[] copyDestAttrs;
  private int   copyFullAttrs = Integer.MIN_VALUE;
  

  /**
   * Default Constructor
   */
  public AEval(ExecContext execContext)
  {
    this.execContext = execContext;
    this.numInstrs   = 0;
    this.instrsList  = new LinkedList<AInstr>();
    this.instrs      = null; 
    this.isCompiled  = false;
    
  }

  public  boolean isCompiled()
  {
    return isCompiled;
  }


  public void addInstr(AInstr instr) throws ExecException
  {
    assert !isCompiled() : "Invalid attempt to add instructions after compilation.";
    
    this.instrsList.add(instr);
    numInstrs++;
  }

  public int getNoInstrs() {return numInstrs;}
  
  public void compile()
  {
    assert !isCompiled() : "Invalid attempt to compile Instructions twice";
    
    // initialize the instrs array with the contents on the arraylist
    if(this.instrs == null || this.instrs.length < numInstrs)
    {
      this.instrs = this.instrsList.toArray(new AInstr[numInstrs]);
    }
    
    //Copy op optimization..
    //Prepare copy optimization to see if we can use Tuple.copy
    if (copyFullAttrs == Integer.MIN_VALUE)
    {
      prepCopyOp();
    }
    
    this.isCompiled = true;
  }

  /**
   * Is this equivalent to a NO-OP
   * 
   * @return true if and only if this is equivalent to a no-op
   */
  public boolean isNoOP()
  {
    return numInstrs == 0;
  }

  /**
   * This method converts the character input to TimeStamp If the user has given
   * format then, it will use the given format else use the default one which is
   * MM/dd/yyyy hh:mm:ss
   * 
   * @param inst
   * @return Long value of the input
   * @throws ExecException
   */
  private CEPDate toTimestampGet(AInstr a, ITuple[] roles) throws ExecException
  {
    CEPDate parsedVal = null;
    
    // Format string
    String formatString = null;
    
    // Get the date/timestamp parser
    CEPDateFormat dateFormatParser = CEPDateFormat.getInstance();
    
    // Extract input datetime string value specified as first parameter
    ITuple t2 = null;
    ITuple t1 = roles[a.r1];
    String val = new String(t1.cValueGet(a.c1));
    
    try
    {
      // If format is mentioned then extract format string and call
      // CEPDateFormat API having both format string and datetime string
      if (a.r2 != 0) 
      {
        t2 = roles[a.r2];
        if(t2.isAttrNull(a.c2)) 
        {          
          return null;
        }
        
        // Extract the format value
        formatString = new String(t2.cValueGet(a.c2));
        
        // Parse the given input string according to the format "formatString"
        parsedVal = dateFormatParser.parse(val, formatString);
        
      } 
      else
      {
        // Parse the given input string according to the default available formats
        parsedVal = dateFormatParser.parse(val);
      }
    } 
    catch (ParseException e)
    {
      //TODO: Error message should be similar to database
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      throw new ExecException(ExecutionError.ILLEGAL_ARGUMENT_PROVIDED, 
                                  val, 
                                  "to_timestamp");
    }    
    return parsedVal;
  }

  public void eval(IEvalContext ec) throws ExecException
  {    
    ITuple    roles[]    = ec.getRoles();
    ITuplePtr rolePtrs[] = ec.getRolePtrs();
    
    eval(ec, roles, rolePtrs);
  }

  protected void eval(IEvalContext ec, ITuple[] roles, ITuplePtr[] rolePtrs)
    throws ExecException
  {
    assert isCompiled() : "Invalid call to eval before compiling instructions.";
    
    IAggrFunction handler    = null;
    Date          start;
    Date          end;

    if (copyFullAttrs >= 0)
    {
      AInstr inst = instrs[0];
      ITuple src = roles[inst.r1];
      ITuple dest = roles[inst.dr];
      dest.copy(src, copyFullAttrs);
      return;
    }
    if (copySrcAttrs != null)
    {
      AInstr inst = instrs[0];
      ITuple src = roles[inst.r1];
      ITuple dest = roles[inst.dr];
      dest.copy(src, copySrcAttrs, copyDestAttrs);
      return;
    }
    
    try
    {
      for (int i = 0; i < numInstrs; i++)
      {
        AInstr inst = instrs[i];
        ITuple dest = roles[inst.dr];
        ITuple src1 = roles[inst.r1];
        ITuple src2 = roles[inst.r2];
        int dcol = inst.dc;
        int col1 =  inst.c1;
        int col2 = inst.c2;
        AOp op = inst.op;

        if (op.op != ITuple.Op.NOOP)
        {
          dest.aeval(op.type, op.op, dcol, 
                     src1, col1, src2, col2);
          continue;
        } 
        switch (op)
        {
        case CHR_TO_TIMESTAMP:
          if (src1.isAttrNull(col1))
            dest.setAttrNull(dcol);
          else 
          {
            CEPDate tsVal = toTimestampGet(inst, roles);
            
            if(tsVal != null)
            {
              dest.tValueSet(dcol, tsVal.getValue());
              dest.tFormatSet(dcol, tsVal.getFormat());
            }
            else 
              dest.setAttrNull(dcol);
          }
          break;
          
        case BIGINT_TO_TIMESTAMP:
          if(src1.isAttrNull(col1))
            dest.setAttrNull(col1);
          else
          {
            long timeValue          = src1.lValueGet(col1);           
            //Note: to_timestamp(bigint) assumes that the argument value is
            // nanosecond unit of time;
            // to_timestamp(bigint) will output a timestamp value based on
            // the offset value calculated on the basis of 1 Jan 1970 UTC.
            // argument must represent number of nanos since the EPOCH
            dest.tValueSet(dcol, timeValue);
            LogUtil.info(LoggerType.TRACE, "BIGINT_TO_TIMESTAMP, Input Value: "
            		        +timeValue+",Output Value: "+timeValue);
          }
          break;
        
        case UDA_HANDLER_CPY:
          if (src1.isAttrNull(col1))
            dest.setAttrNull(dcol);
          else
          {
            handler = (IAggrFunction) src1.oValueGet(col1);
            dest.oValueSet(col1, handler);
          }
          break;
          
          //decides whether to output a zero or one for init eval.
        case COUNT_INIT:
          boolean isNull = inst.countCond.eval(ec);
          if(isNull)
            dest.iValueSet(dcol,  0);
          else
            dest.iValueSet(dcol,  1);
          break;
          
        //increment the count only if the expr is non null.
        case COUNT_ADD:
          boolean isNull1 = inst.countCond.eval(ec);
          if(!isNull1)
            dest.iValueSet(dcol,  src1.iValueGet(col1) + 1);
          else
            dest.iValueSet(dcol,  src1.iValueGet(col1));
          break;
          
        //decrement the count only if the expr is non null.
        case COUNT_SUB:
          boolean isNull2 = inst.countCond.eval(ec);
          if(!isNull2)
            dest.iValueSet(dcol,  src1.iValueGet(col1) - 1);
          else
            dest.iValueSet(dcol,  src1.iValueGet(col1));
          break;
            
        case USR_FNC:
          invokeFunction(inst, roles);
          break;
          
        case XML_CDATA:
          invokeXMLCData(inst, roles);
          break;
          
        case XML_COMMENT:
          invokeXMLComment(inst, roles);
          break;
          
        case XML_CONCAT:
          invokeXMLConcat(inst, roles);
          break;
        
        case XML_PARSE:
          invokeXMLParse(inst, roles);
          break;

        case XML_FOREST:
    	  invokeXMLForest(inst, roles);
    	  break;
    	  
        case XML_COLATTVAL:
          invokeXMLColAttVal(inst, roles);
          break;

        case XML_ELEMENT:
          invokeXMLElement(inst, roles);
          break;
          
        case XQRY_FNC:
          invokeXQryFunction(inst, roles);
          break;

        case XEXISTS_FNC:
          invokeXExistsFunction(inst, roles);
          break;

        case XMLTBL_FNC:
          invokeXmlTblFunction(inst, roles);
          break;
          
        case XML_AGG:
          invokeXmlAgg(inst, roles, rolePtrs);
          break;
          
        case XML_AGG_INIT_GROUP:
          invokeXmlAggInit(inst, roles, rolePtrs);
          break;
        
        case CASE_EXPR:
          evalCaseExpr(inst, ec, roles);
          break;
          
        case RELEASE_AGGR_HANDLERS:
          releaseAggrHandlers(inst, roles);
          break;
          
        case RESET_AGGR_HANDLERS:
          resetAggrHandlers(inst, roles);
          break;
          
        case ALLOC_AGGR_HANDLERS:
          allocAggrHandlers(inst, roles);
          break;
        
        case ALLOC_XMLAGG_INDEX:
          allocXmlIndex(inst, roles);
          break;
        
        case RELEASE_XMLAGG_INDEX:
          releaseXmlIndex(inst, roles);
          break;
          
        case RESET_XMLAGG_INDEX:
          resetXmlIndex(inst, roles);
          break;
          
        case UDA_INIT:
          if (src1.isAttrNull(col1))
            dest.setAttrNull(dcol);
          else
          {
            handler = (IAggrFunction) src1.oValueGet(col1);
            dest.oValueSet(col1, handler);
            // NOTE : In all the UDA's, we have both start and end time. Each are assigned new Date
            // Currently, this is done because System.currentTimeMillis() was not giving incremental values
            // always. i.e. start time was sometimes smaller than end time. This needs to be investigated more
            // and then we can remove this new instantiation
            start = new Date();
            handler.initialize(); 
            end = new Date();
            int fid = inst.getFunctionId();
            execContext.getExecStatsMgr().incrUserFuncStats(fid, (end.getTime() - start.getTime()));
          }
          break;
        case UDA_PLUS_HANDLE:
          invokeAggrFunction(inst, true, roles);
          break;
          
        case UDA_MINUS_HANDLE:
          invokeAggrFunction(inst, false, roles);
          break;
          
        case PREP_STMT:
          invokePreparedStatement(inst, roles);
          break;
          
        default:
          // should never come here
          assert false;
        }
      }
    } catch (UDAException u)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, u);
      throw new ExecException(ExecutionError.USERDEFINED_AGGREGATION_FUNCTION_RUNTIME_ERROR, u,
          (handler == null ? "" : handler.toString()));
    }
 }
  
  private void invokePreparedStatement(AInstr inst, ITuple[] roles)
    throws ExecException
  {
    ExternalInstr        einstr   = inst.extrInstr;    
    LinkedList<Integer>  argPos   = einstr.getArgPos();
    LinkedList<Integer>  argRoles = einstr.getArgRoles();
    LinkedList<Datatype> argTypes = einstr.getArgTypes();
   
    try
    {     
      for(int j=0; j < einstr.numArgs; j++)
      {     
        ITuple t = roles[argRoles.get(j)];        
        if(t.isAttrNull(argPos.get(j)))
        {
          einstr.preparedStmt.setNull(j+1, argTypes.get(j).getSqlType());          
        }        
        else
        {
          switch(argTypes.get(j).getKind())
          {
          case INT:
            einstr.preparedStmt.setInt(j+1,t.iValueGet(argPos.get(j)) );
            break;
          case BIGINT:
            einstr.preparedStmt.setLong(j+1, t.lValueGet(argPos.get(j)));
            break;
          case BOOLEAN:
            einstr.preparedStmt.setBoolean(j+1,t.boolValueGet(argPos.get(j)) ) ;
            break;
          case FLOAT:
            einstr.preparedStmt.setFloat(j+1, t.fValueGet(argPos.get(j)));
            break;
          case DOUBLE:
            einstr.preparedStmt.setDouble(j+1, t.dValueGet(argPos.get(j)));
            break;
          case BIGDECIMAL:
            einstr.preparedStmt.setBigDecimal(j+1, t.nValueGet(argPos.get(j)));
            break;
          case CHAR:
            // bug 22105563
            char[] tempChar = t.cValueGet(argPos.get(j));
            int tempCharLen = t.cLengthGet(argPos.get(j));
            String s = String.valueOf(tempChar, 0, tempCharLen);
            einstr.preparedStmt.setString(j+1, s);
            break;
          case BYTE:
            einstr.preparedStmt.setBytes(j+1, t.bValueGet(argPos.get(j)));
            break;
          case TIMESTAMP: 
            CEPDate argTs = new CEPDate(t.tValueGet(argPos.get(j)), t.tFormatGet(argPos.get(j)));
            einstr.preparedStmt.setTimestamp(j+1, argTs);
            break;
          case INTERVAL: 
            einstr.preparedStmt.setString(
              j+1, 
              IntervalConverter.getDSInterval(
                t.vValueGet(argPos.get(j)),
                t.vFormatGet(argPos.get(j))));
            break;
          case INTERVALYM:
            einstr.preparedStmt.setString(
                j+1, 
                IntervalConverter.getYMInterval(
                  t.vymValueGet(argPos.get(j)),
                  t.vFormatGet(argPos.get(j))));
            break;
           default:
             LogUtil.info(LoggerType.TRACE, "Failed to set the parameter of " +
             		"type " + argTypes.get(j).getKind() + " in external prepared " +
             				"statement. Check the supported types.");
             throw new ExecException(
               ExecutionError.TYPE_NOT_SUPPORTED_IN_EXTERNAL_RELATION, 
               argTypes.get(j).getKind());
          }// end of switch(argtypes)
        }//end of else when arg is non null
      }//end of loop
    }
    catch(Exception e)
    {
      if(e instanceof java.lang.NullPointerException)
      {
        LogUtil.info(LoggerType.TRACE, "ExternalInstr: argPos == null?"
                                        + (argPos == null));
        LogUtil.info(LoggerType.TRACE, "ExternalInstr: argRoles == null?" 
                                        + (argRoles == null));
        LogUtil.info(LoggerType.TRACE, "ExternalInstr: argTypes == null?" 
                                        + (argTypes == null));
        LogUtil.info(LoggerType.TRACE, "ExternalInstr: preparedStmt == null? " 
                                        + (einstr.preparedStmt == null));
      }
      LogUtil.info(LoggerType.TRACE, "A Runtime Exception has occured while " +
       "executing external prepared statement. Check the logs for stack trace.");
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      throw new ExecException(ExecutionError.ERROR_RUNNING_EXTERNAL_QUERY,
          einstr.getExtSourceName()); 
    }
  }
  
  
  private void allocXmlIndex(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    int[]             xmlAggIndexPos = finstr.getXmlAggIndexPos();
    TupleComparator[] tc             = finstr.getComparators();
    int               role           = finstr.r1;
    int               length         = xmlAggIndexPos.length;
    ITuple            dest           = roles[role];

    for(int i = 0; i<length; i++)
    {
      int pos = xmlAggIndexPos[i];
      if(pos != -1)
      {
        assert tc[i] != null;
        
        ISortedArray<ITuplePtr> o = dest.oValueGet(pos); 
        if(o == null) 
        {  
          o = new SortedTuplePtrArray(tc[i]);
          dest.oValueSet(pos, o);
        }
      }
    }
    
  }
  
  private void resetXmlIndex(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    int[]  xmlAggIndexPos = finstr.getXmlAggIndexPos();
    ITuple t              = roles[finstr.r1];
    
    for(int i=0; i < xmlAggIndexPos.length; i++)
    {
      int pos = xmlAggIndexPos[i];
      if(pos != -1)
      {
        t.oValueSet(pos, null);
      }
    }
  }

  private void releaseXmlIndex(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    int[]  xmlAggIndexPos = finstr.getXmlAggIndexPos();
    int    role           = finstr.r1;
    int    length         = xmlAggIndexPos.length;
    ITuple dest           = roles[role];

    for(int i = 0; i<length; i++)
    {
      int pos = xmlAggIndexPos[i];
      if(pos != -1)
      {
        ISortedArray<ITuplePtr> o = dest.oValueGet(pos);
        try
        {
          if(o!=null)
            o.clear();
          dest.oValueSet(pos, null);
        }
        catch(CEPException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
          throw new ExecException(ExecutionError.XML_AGG_RUNTIME_ERROR);
        }
        
      }
    }
  }
  
  // Evaluate Case Expression
  private void evalCaseExpr(AInstr inst, IEvalContext ec, ITuple[] roles) 
    throws ExecException
  {
    ITuple    dest    = roles[inst.dr];
    int       dcol    = inst.dc;

    CaseInstr cinstr = inst.caseCond;
    if(cinstr.compEval != null)
      cinstr.compEval.eval(ec);

    int     num  = cinstr.getNumConditions();
    boolean flag = false;
    int     j    = 0;

    while((j<num)&&(!flag))
    {
      if(cinstr.conditions[j].condition.eval(ec))
      {
        flag = true;
        if(cinstr.conditions[j].result != null)
          cinstr.conditions[j].result.eval(ec);
        else
        {
          dest.setAttrNull(dcol);
        }
      }
      j++;
    }
    if(!flag)
    {
      if(cinstr.elseResult != null)
        cinstr.elseResult.eval(ec);
      else
      {
        dest.setAttrNull(dcol);
      }
    }
  }
  
  /**
   * Method to invoke the function and set the result
   * 
   * @param i
   *          the instruction number for the USR_FNC instruction
   */
  private void invokeFunction(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    ITuple              dest     = roles[finstr.dr];
    int                 dcol     = finstr.dc;
    int                 numArgs  = finstr.numArgs;
    Datatype[]          argTypes = finstr.argTypes;
    int[]               argRoles = finstr.argRoles;
    int[]               argPos   = finstr.argPos;
    UserDefinedFunction f        = finstr.f;
    Object[]            args     = finstr.args;
    Datatype            retType  = finstr.returnType;
    Object              res      = null;
    int                 len;
    Object              arg      = null;
    byte[]              barr;

    for (int j = 0; j < numArgs; j++)
    {
      ITuple t = roles[ argRoles[j] ];
      if (t.isAttrNull(argPos[j]))
       arg = null;
      
      else
      {
        switch (argTypes[j].getKind())
        {
        case INT:
          arg = new Integer(t.iValueGet(argPos[j]));
          break;
        case BIGINT:
          arg = new Long(t.lValueGet(argPos[j]));
          break;
        case FLOAT:
          arg = new Float(t.fValueGet(argPos[j]));
          break;
        case DOUBLE:
          arg = new Double(t.dValueGet(argPos[j]));
          break;
        case BIGDECIMAL:
          arg = t.nValueGet(argPos[j]);
          break;
        case CHAR:
          len = t.cLengthGet(argPos[j]);
          arg = new String(t.cValueGet(argPos[j]), 0, len);
          break;
        case BYTE:
          len = t.bLengthGet(argPos[j]);
          barr = new byte[len];
          System.arraycopy(t.bValueGet(argPos[j]), 0, barr, 0, len);
          arg = barr;
          break;
        case TIMESTAMP:
          long nanoSeconds = t.tValueGet(argPos[j]);
          TimestampFormat tsFormat = t.tFormatGet(argPos[j]);
          CEPDate tsValue = null;
          if(tsFormat != null)
            tsValue = new CEPDate(nanoSeconds, tsFormat);
          else
            tsValue = new CEPDate(nanoSeconds);
          arg = tsValue;
          break;
        case OBJECT:
          arg = t.oValueGet(argPos[j]);
          break;
        case BOOLEAN:
          arg = new Boolean(t.boolValueGet(argPos[j]));
          break;
        case XMLTYPE:
          arg = new String(t.xValueGet(argPos[j]));
          break;
        case INTERVAL:
          arg = new Long(t.vValueGet(argPos[j]));
          break;
        case INTERVALYM:
          arg = new Long(t.vymValueGet(argPos[j]));
          break;
        default:
          assert false : argTypes[j];
          break;
        }
      }
      args[j] = arg;
    }

    // Invoke the function
    try {
     
      Date start = new Date();
      synchronized(f)
      {
        if (f instanceof SingleElementFunction)
          res = ((SingleElementFunction) f).execute(args);
        else
        {
          ICartridgeContext context = CartridgeHelper.createCartridgeContext(execContext);
          res = ((ISimpleFunction) f).execute(args, context);
        }
      }
      Date end = new Date();
      int fid = finstr.getFunctionId();
      execContext.getExecStatsMgr().incrUserFuncStats(fid, (end.getTime() - start.getTime()));
    
    }
    catch(SoftUDFException suf)
    {
      // This is an user-error, not a system error, hence log as warning and 
      // not as emergency
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, suf);
      
      // Throw a soft execution exception if the user defined function throws
      // a SoftUDFException
      throw new SoftExecException(ExecutionError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
          suf,
          f.toString());
    }
    catch(UDFException uf)
    { 
      // This is an user-error, not a system error, hence log as warning and not as emergency
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, uf);
      throw new ExecException(ExecutionError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
          uf,
          f.toString());
    } catch(RuntimeInvocationException rie)
    {   
      // This is an user-error, not a system error, hence log as warning and not as emergency
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, rie);
      throw new ExecException(ExecutionError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
          rie,
          f.toString());
    }
    catch(Exception e)
    {      
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      throw new ExecException(ExecutionError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
          e,
          f.toString());
    }
    

    if (res == null)
      dest.setAttrNull(dcol);
    else
    {
      IntervalFormat destFormat = null;
      switch (retType.getKind())
      {
      case INT:
        dest.iValueSet(dcol,  ((Integer) res).intValue());
        break;
      case BIGINT:
        dest.lValueSet(dcol,  ((Long)res).longValue());
    	break;
      case FLOAT:
        dest.fValueSet(dcol,  ((Float) res).floatValue());
        break;
      case DOUBLE:
        dest.dValueSet(dcol,  ((Double) res).doubleValue());
        break;
      case BIGDECIMAL:
        dest.nValueSet(dcol, (BigDecimal)res, ((BigDecimal)res).precision(), 
                       ((BigDecimal)res).scale());
        break;
      case CHAR:
        dest.cValueSet(dcol, ((String) res).toCharArray(), 
                              ((String) res).length());
        break;
      case BYTE:
        dest.bValueSet(dcol, (byte[]) res, ((byte[]) res).length);
        break;
      case TIMESTAMP:
        dest.tValueSet(dcol, ((Timestamp)res).getTime() * 1000000l);
        break;
      case OBJECT:
        dest.oValueSet(dcol, res);
        break;
      case BOOLEAN:
        dest.boolValueSet(dcol, ((Boolean)res).booleanValue());
        break;
      case XMLTYPE:
        dest.xValueSet(dcol, ((String) res).toCharArray(), 
                              ((String) res).length());
        break;
      case INTERVAL:        
        try
        {
          destFormat = new IntervalFormat(TimeUnit.DAY,
                               TimeUnit.SECOND,
                               9,
                               9);
        } 
        catch (CEPException e)
        {
          // Should be unreachable because we created interval format with
          // valid parameter values
          assert false;
        }        
        dest.vValueSet(dcol, ((Long)res).longValue(), destFormat);
        break;
        
      case INTERVALYM:
        try
        {
          destFormat = new IntervalFormat(TimeUnit.YEAR,
                             TimeUnit.MONTH,
                             9,
                             true);
        } 
        catch (CEPException e)
        {
          // Should be unreachable because we created interval format with
          // valid parameter values
          assert false;
        }     
        dest.vymValueSet(dcol, ((Long)res).longValue(), destFormat);
        break;
        
      default:
        assert false : retType;
        break;
      }
    }
  }

  private XMLSequence execXQry(AInstr finstr, ITuple[] roles) throws Exception
  {
    int        numArgs   = finstr.numArgs;
    int        numParams = (int) ((numArgs - 1) / 2);
    Datatype[] argTypes  = finstr.argTypes;
    int[]      argRoles  = finstr.argRoles;
    int[]      argPos    = finstr.argPos;
    ITuple     t         = roles[argRoles[numArgs - 1]];
    
    PreparedXQuery xq = (PreparedXQuery)t.oValueGet(argPos[numArgs-1]);
    
    for (int i = numParams; i < (numArgs - 1); i++)
    {
      int j = i - numParams;
      t = roles[argRoles[i]];
      int len = t.cLengthGet(argPos[i]);
      char[] name = t.cValueGet(argPos[i]);
      if ((len == 1) && (name[0] == '.'))
      {
	t = roles[argRoles[j]];
	XMLItem item = (XMLItem)t.getItem(argPos[j], xq);
	xq.setContextItem(item);
      }
      else
      {
	t = roles[argRoles[j]];
	
	// nulls handling later
	switch (argTypes[j].getKind())
	{
	  case INT:
	  {
	    int val = t.iValueGet(argPos[j]);
	    xq.setInt(new QName(new String(name, 0, len)), val);
	    break;
	  }
	  case BOOLEAN:
	  {
	    boolean val = t.boolValueGet(argPos[j]);
	    xq.setBoolean(new QName(new String(name, 0, len)), val);
	    break;
	  }
	  case BIGINT:
	  {
	    int val = (int)t.lValueGet(argPos[j]);
	    xq.setInt(new QName(new String(name, 0, len)), val);
	    break;
	  }
	  case FLOAT:
	  {
	    float val = t.fValueGet(argPos[j]);
	    xq.setFloat(new QName(new String(name, 0, len)), val);
	    break;
	  }
	  /* TODO How to support the xq.setDouble() method 
        case DOUBLE:
	{
	  double val = t.dValueGet(argPos[j]);
	  xq.setDouble(new QName(new String(name, 0, len)), val);
	  break;
	} */
	  //TODO : support xq.setBigDecimal too !! 
	  case CHAR:
	  {
	    int dlen = t.cLengthGet(argPos[j]);
	    xq.setString(new QName(new String(name, 0, len)), 
	        new String(t.cValueGet(argPos[j]), 0, dlen));
	    break;
	  }
	  // To the best of my knowledge, BYTE and TIMESTAMP are not currently 
	  // supported by XDK - need to file an enhancement
	  case BYTE:
	  case TIMESTAMP:
	  default:
	    assert false : argTypes[j];
	  break;
	}
      }
    }
    
    XMLSequence res = xq.executeQuery(false);
    return res;
  }

  private void invokeXmlAggInit(AInstr finstr, ITuple[] roles, 
                                ITuplePtr[] rolePtrs)
    throws ExecException
  {
    //destination tuple, column
    ITuple dest = roles[finstr.xmlAggInstr.newOutputRole];
    int dcol = finstr.xmlAggInstr.aggrIndex;
    
    //input tuple, column
    ITuple input = roles[finstr.xmlAggInstr.argRole];
    int argPos = finstr.xmlAggInstr.argPos;
    
    //index tuple (may be null)
    ITuplePtr orderByTuple = rolePtrs[IEvalContext.XML_AGG_INDEX_ROLE];
    int indexPos = finstr.xmlAggInstr.xmlAggIndexPos;
    
    //other necessary stuff
    XMLDocument parent = new XMLDocument();
    Node docFrag = parent.createDocumentFragment();
    XmlManager xmlMgr = execContext.getXmlMgr();
    IXmlContext ctx = xmlMgr.createContext();
    XMLItem inp;
    
    try
    {
      // What do we do if we get a null input? set output to null
      if (input.isAttrNull(argPos))
      {
        dest.setAttrNull(dcol);
      }
      
      // clone the input, append it to docFrag 
      // and set it in the output
      //if there is an order by append to the sorted list
      else
      {
        inp = (XMLItem) input.getItem(argPos, ctx);

        Node n = inp.getNode();
        
        //if n is a document node, then extract child nodes and make a doc frag
        if(n.getNodeType() == Node.DOCUMENT_NODE)
        {
          DocumentFragment tempFrag = parent.createDocumentFragment();
          NodeList nl = n.getChildNodes();
          for(int i=0; i < nl.getLength();i++)
          {
            Node nTemp = parent.importNode(nl.item(i), true);
            tempFrag.appendChild(nTemp);
          }
          
          n = tempFrag;
        }
        
        //else import it with the parent doc
        else
          n = parent.importNode(n, true); //this resolves the inputs parent
        
        
        if(indexPos != -1)
        {
          ISortedArray<ITuplePtr> o = dest.oValueGet(indexPos);  
          o.insert(orderByTuple);
          dest.oValueSet(finstr.xmlAggInstr.xmlAggIndexPos, o);
        }

        docFrag.appendChild(n);
        XMLItem res = new XMLItem(xmlMgr);
        res.setNode((XMLNode) docFrag);
        dest.xValueSet(dcol, res);
      }
    } catch (Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      // eat it for now
      throw new ExecException(ExecutionError.XML_AGG_RUNTIME_ERROR, e);
    }

  }
  
  // Helper method: Inserts newChild at a specific position (insertPos) in frag
  private void insertAt(int insertPos, Node newChild, Node frag)
  {
    assert frag instanceof DocumentFragment;
       
    NodeList nl = frag.getChildNodes();
    // if there are no children
    if(nl.getLength() == 0 || insertPos == nl.getLength())
      frag.appendChild(newChild);
    else
    {
      Node refChild = nl.item(insertPos);
      frag.insertBefore(newChild, refChild);
    }
  }
  
  /**
   * Fetch the input tuple, append to old aggr tuple
   * and set it in new output role. in case order by exists, 
   * append in order
   */
  private void invokeXmlAgg(AInstr finstr, ITuple[] roles, 
                            ITuplePtr[] rolePtrs)
    throws ExecException
  {
    //destination role, pos
    int dcol = finstr.xmlAggInstr.aggrIndex;
    ITuple dest = roles[finstr.xmlAggInstr.newOutputRole];
    
    //input role, pos
    int argPos = finstr.xmlAggInstr.argPos;
    ITuple input = roles[finstr.xmlAggInstr.argRole];
    
    //old aggr role, pos
    int aggrIndex = finstr.xmlAggInstr.aggrIndex;
    ITuple oldOutput = roles[finstr.xmlAggInstr.oldOutputRole];

    //where the order by info is stored.
    ITuplePtr indexTuple = rolePtrs[IEvalContext.XML_AGG_INDEX_ROLE];
    
    XMLItem inp;
    XMLItem old;
    Node inputNode = null;
    Node docFrag      = null;
    Node docFragClone = null;
    Document parent = null;
    
    try
    {
      //What do we do if we get a null input? No-op
      if(input.isAttrNull(argPos))
        return;
        
      try{
        //Get the input tuple
        XmlManager xmlMgr = execContext.getXmlMgr();
        inp = (XMLItem)input.getItem(argPos, xmlMgr.createContext());
        
        if(oldOutput.isAttrNull(aggrIndex)) 
        {
          //this implies we have only received null inputs so far
          parent = new XMLDocument();
          docFrag = parent.createDocumentFragment();
        }
        
        //do we already have an old output tuple?
        else
        {
          try
          {
            old = (XMLItem)oldOutput.getItem(aggrIndex, xmlMgr.createContext());
            docFrag = old.getNode();
          }
          
          //if there is no group by, then the xmldoc was never created.
          catch(Exception e)
          {
            parent = new XMLDocument();
            docFrag = parent.createDocumentFragment();
          }
          
          //ensure that old aggr node is a docfragment
          assert docFrag != null;
          assert docFrag instanceof DocumentFragment;

          parent = docFrag.getOwnerDocument();
        }
        
        //import the input node into the docFrags parent
        inputNode = inp.getNode();
        
        //special handling for import of a Document_Node
        if(inputNode.getNodeType() == Node.DOCUMENT_NODE)
        {
          DocumentFragment tempFrag = parent.createDocumentFragment();
          NodeList nl = inputNode.getChildNodes();
          for(int i=0; i < nl.getLength();i++)
          {
            //this is necessary because i can only append children of the same parent.
            Node nTemp = parent.importNode(nl.item(i), true);
            tempFrag.appendChild(nTemp);
          }
          
          inputNode = tempFrag;
        }

        //this resolves the inputs parent
        else
          inputNode = parent.importNode(inputNode, true); 
        
        //necessary to clone to avoid mem sharing issues.
        docFragClone = docFrag.cloneNode(true);
         
        //handle order by here
        int insertPos = -1; //position in docFrag where new child will go.
        if(finstr.xmlAggInstr.xmlAggIndexPos != -1)
        {
          assert indexTuple != null;
          //assert: there must be a space reserved in the tuple for the index.
          ISortedArray<ITuplePtr> tupleIndex = null;
          try
          {
            tupleIndex = oldOutput.oValueGet(finstr.xmlAggInstr.xmlAggIndexPos);
            assert tupleIndex!= null;
            insertPos = tupleIndex.insert(indexTuple);
            dest.oValueSet(finstr.xmlAggInstr.xmlAggIndexPos, tupleIndex);
          }
          
          //TODO: deal with exceptions well.
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
        
        if(insertPos != -1)
        {
          docFragClone = parent.importNode(docFragClone, true);
          insertAt(insertPos,inputNode, docFragClone);
        }
        
        else
        {
          docFragClone = ((Document)parent).importNode(docFragClone, true);
          docFragClone.appendChild(inputNode);
        }
        
        //set the output tuple and put a clone in the aggr col.
        XMLItem res = new XMLItem(xmlMgr);
        res.setNode((XMLNode)docFragClone);
        dest.xValueSet(dcol, res);
      }catch(Exception e)
      {
        throw new ExecException(ExecutionError.XML_AGG_RUNTIME_ERROR, e);
      }
      
    }
    catch(Exception e)
    {
      //eat it for now
      throw new ExecException(ExecutionError.XML_AGG_RUNTIME_ERROR, e);
    }
    
  }
  
  private void invokeXmlTblFunction(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    int    dcol    = finstr.dc;
    ITuple dest    = roles[finstr.dr];

    try
    {
      XMLSequence res = execXQry(finstr, roles);
      dest.oValueSet(dcol, res);
    } 
    catch (Exception x) 
    {
      System.out.println(x.getMessage());
      x.printStackTrace();
      dest.setAttrNull(dcol);
    }
  }

  private void invokeXExistsFunction(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    int    dcol    = finstr.dc;
    ITuple dest    = roles[finstr.dr];

    try
    {
      XMLSequence res = execXQry(finstr, roles);
      
      if (res.next())
	dest.boolValueSet(dcol, true);
      else
	dest.boolValueSet(dcol, false);
    } 
    catch (Exception x) 
    {
      System.out.println(x.getMessage());
      x.printStackTrace();
      dest.boolValueSet(dcol, false);
    }
  }
  
  private void invokeXMLColAttVal(AInstr einstr, ITuple[] roles)
    throws ExecException
  {
    ITuple          dest     = roles[einstr.dr];
    int             dcol     = einstr.dc;
    XMLElementInstr xmlInstr = einstr.xmlInstr;

    int numChild = xmlInstr.getNumAttributes();
    int[] childRoles = xmlInstr.attrRoles;
    int[] childPos = xmlInstr.attrPos;
    Datatype[] childTypes = xmlInstr.attrTypes;

    XmlManager xmlMgr = execContext.getXmlMgr();
    IXmlContext ctx = xmlMgr.createContext();
    XMLDocument doc = xmlInstr.getDocument();
    XMLDocumentFragment fragment = (XMLDocumentFragment) doc
        .createDocumentFragment();

    for (int i = 0; i < numChild; i++)
    {
      ITuple t = roles[childRoles[i]];
      String childVal = new String();
      String childName;
      if (!xmlInstr.attrNames[i].isNameExpr())
        childName = xmlInstr.attrNames[i].getName();
      else
      {
        ITuple attrTuple = roles[xmlInstr.attrNames[i].getAttrRole()];
        int pos = xmlInstr.attrNames[i].getAttrPos();
        if (attrTuple.isAttrNull(pos))
          throw new ExecException(
              ExecutionError.NAME_EXPR_CANNOT_BE_NULL_FOR_XML_PUB_FUNCS);
        childName = new String(attrTuple.cValueGet(pos), 0, attrTuple
            .cLengthGet(pos));
      }
      if (t.isAttrNull(childPos[i]))
      {
        childVal = "";
      } else
      {
        XMLElement child = (XMLElement) doc.createElement("column");
        child.setAttribute("name", childName);
        switch (childTypes[i].getKind())
        {
        case INT:
          childVal = Integer.toString(t.iValueGet(childPos[i]));
          child.addText(childVal);
          break;
        case FLOAT:
          childVal = Float.toString(t.fValueGet(childPos[i]));
          child.addText(childVal);
          break;
        case BOOLEAN:
          childVal = Boolean.toString(t.boolValueGet(childPos[i]));
          child.addText(childVal);
          break;
        case DOUBLE:
          childVal = Double.toString(t.dValueGet(childPos[i]));
          child.addText(childVal);
          break;
        case BIGDECIMAL:
          childVal = t.nValueGet(childPos[i]).toString();
          child.addText(childVal);
          break;
        case BIGINT:
          childVal = Long.toString(t.lValueGet(childPos[i]));
          child.addText(childVal);
          break;
        case BYTE:
          childVal = new String(t.bValueGet(childPos[i]), 0, t
              .bLengthGet(childPos[i]));
          child.addText(childVal);
          break;
        case XMLTYPE:
          try
          {
            Node tmpnode = (Node) ((XMLItem) t.getItem(childPos[i], ctx))
                .getNode();
            tmpnode = child.getOwnerDocument().importNode(tmpnode, true);
            child.appendChild(tmpnode);
          } catch (Exception e)
          {

          }
          break;
        case CHAR:
          childVal = new String(t.cValueGet(childPos[i]), 0, t
              .cLengthGet(childPos[i]));
          child.addText(childVal);
          break;
        case TIMESTAMP:
          childVal = Long.toString(t.tValueGet(childPos[i]));
          child.addText(childVal);
          break;
          //TODO: For Interval data types, 
          // It is not Sure Whether we should use toString() or call
          // IntervalConvert.getDSInterval or IntervalConverter.getYMInterval
        case INTERVAL:
          childVal = Long.toString(t.vValueGet(childPos[i]));
          child.addText(childVal);
          break;
        case INTERVALYM:
          childVal = Long.toString(t.vymValueGet(childPos[i]));
          child.addText(childVal);          
          break;
        default:
          assert false;
        }
        fragment.appendChild(child);
      }
    }
    XMLItem item = ctx.createItem();
    item.setNode(fragment);
    dest.xValueSet(dcol, item);
  }
  
  private void invokeXMLCData(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    int         dcol        = finstr.dc;
    ITuple      dest        = roles[finstr.dr];
    char[]      arg;
    int         arg_length;
    XmlManager  xmlMgr      = execContext.getXmlMgr();
    XMLItem     result      = new XMLItem(xmlMgr);
    XMLDocument resultDoc   = finstr.xmlInstr.getDocument();

    assert resultDoc != null;

    ITuple t = roles[finstr.argRoles[0]];

    // if arg is null output is null
    if (t.isAttrNull(finstr.argPos[0]))
    {
      dest.setAttrNull(dcol);
      return;
    }

    // else use dom api to create cdata and set output tuple
    else
    {
      arg = t.cValueGet(finstr.argPos[0]);
      arg_length = t.cLengthGet(finstr.argPos[0]);
      String argString = new String(arg, 0, arg_length);
      if (argString.contains("]]>"))
        throw new ExecException(ExecutionError.INVALID_XML_PUB_ARG,
            new Object[]
            { argString, "XMLCDATA" });
      XMLCDATA xres = (XMLCDATA) resultDoc.createCDATASection(argString);
      result.setNode(xres);
      dest.xValueSet(dcol, result);
    }
  }
  
  private void invokeXMLComment(AInstr finstr, ITuple[] roles)
    throws ExecException
  {
    int         dcol        = finstr.dc;
    ITuple      dest        = roles[finstr.dr];
    char[]      arg;
    int         arg_length;
    XmlManager  xmlMgr      = execContext.getXmlMgr();
    XMLItem     result      = new XMLItem(xmlMgr);
    XMLDocument resultDoc   = finstr.xmlInstr.getDocument();

    assert resultDoc != null;

    ITuple t = roles[finstr.argRoles[0]];

    // if arg is null output is null
    if (t.isAttrNull(finstr.argPos[0]))
    {
      dest.setAttrNull(dcol);
      return;
    }

    // else use dom api to create comment and set output tuple
    else
    {
      arg = t.cValueGet(finstr.argPos[0]);
      arg_length = t.cLengthGet(finstr.argPos[0]);
      String argString = new String(arg, 0, arg_length);
      if (argString.contains("--"))
        throw new ExecException(ExecutionError.INVALID_XML_PUB_ARG,
            new Object[]{ argString,"XMLCOMMENT"});
      XMLComment xres = (XMLComment) resultDoc.createComment(argString);
      result.setNode(xres);
      dest.xValueSet(dcol, result);
    }
  }
  
  private void invokeXMLParse(AInstr finstr, ITuple[] roles)
    throws ExecException 
  {
    int         dcol       = finstr.dc;
    ITuple      dest       = roles[finstr.dr];
    XmlManager  xmlMgr     = execContext.getXmlMgr();
    XMLItem     resultItem = new XMLItem(xmlMgr);
    XMLDocument resultDoc  = finstr.xmlParseInstr.getParentDoc();
    char[]      arg;
    int         arg_length;
    DOMParser   dom        = new DOMParser();
    Reader      reader;
    Node        resultNode;

    ITuple t = roles[finstr.xmlParseInstr.getArgRole()];
    if (t.isAttrNull(finstr.xmlParseInstr.getArgPos()))
      dest.setAttrNull(dcol);
    else
    {
      String argString = null;
      try
      {
        arg = t.cValueGet(finstr.xmlParseInstr.getArgPos());
        arg_length = t.cLengthGet(finstr.xmlParseInstr.getArgPos());
        argString = new String(arg,0,arg_length);
          switch (finstr.xmlParseInstr.getKind())
          {
          // since wellformed is set, add a pseudo <tag> to make it wellformed
          case CONTENT:
            resultNode = resultDoc.createDocumentFragment();
            StringBuffer buf = new StringBuffer();
            if(finstr.xmlParseInstr.isWellformed())
            {
              buf.append("<tag>").append(argString).append(
                "</tag>");
            }
            else 
            {
              buf.append(argString);
            }
            reader = new StringReader(buf.toString());
            dom.parse(reader);

            // Since we added a fake tag for the parse, strip it off
            Node child;
            if(finstr.xmlParseInstr.isWellformed())
              child = dom.getDocument().getFirstChild();
            else
              child = dom.getDocument();
            
            Node nextChild;
            // if wellformed, Get all the children of <tag> and append them to a fragment
            // else just get all nodes and do the same.
            child = child.getFirstChild();
            while (child != null)
            {
              nextChild = child.getNextSibling();
              ((DocumentFragment) resultNode).appendChild(resultDoc.importNode(child, true));
              child = nextChild;
            }
            resultItem.setNode((XMLNode) resultNode);
            break;

          // parse the document to ensure its a singly rooted document
          case DOCUMENT:
            reader = new StringReader(argString);
            dom.parse(reader);
            NodeList nl = dom.getDocument().getChildNodes();

            // Must be a singly-rooted document
            if (nl.getLength() != 1)
              throw new ExecException(ExecutionError.INVALID_XML_PUB_ARG, 
                  new Object[]{argString,"XMLPARSE"});

            Node resDocFrag = resultDoc.createDocumentFragment();

            for (int j = 0; j < nl.getLength(); j++)
            {
              resDocFrag.appendChild(resultDoc.importNode(nl.item(j), true));
            }
            resultNode = resDocFrag;
            resultItem.setNode((XMLNode) resultNode);
            break;

          // either content or document must be specified
          default:
            assert false : finstr.xmlParseInstr.getKind();
          }
      } catch (IOException e)
      {
        throw new ExecException(ExecutionError.INVALID_XML_PUB_ARG,
            new Object[]{argString,"XMLPARSE"});
      }

      catch (SAXException e)
      {
        throw new ExecException(ExecutionError.INVALID_XML_PUB_ARG,
            new Object[]{argString,"XMLPARSE"});
      }
    }
    dest.xValueSet(dcol, resultItem);
  }
  
  private void invokeXMLConcat(AInstr finstr, ITuple[] roles)
    throws ExecException 
  {
    int                 dcol      = finstr.dc;
    ITuple              dest      = roles[finstr.dr];
    XMLItem             arg;
    XmlManager          xmlMgr    = execContext.getXmlMgr();
    IXmlContext         ctx       = xmlMgr.createContext();
    XMLItem             result    = new XMLItem(xmlMgr);
    XMLDocument         resultDoc = finstr.xmlConcatInstr.getDocument();
    XMLDocumentFragment resDoc    = 
      (XMLDocumentFragment) resultDoc.createDocumentFragment();

    assert resultDoc != null;

    // Get the XMLNode from each argument and append it to the
    // result document
    for (int i = 0; i < finstr.xmlConcatInstr.getNumargs(); i++)
    {
      ITuple t = roles[finstr.xmlConcatInstr.getArgRoles()[i]];
      if (t.isAttrNull(finstr.xmlConcatInstr.getArgPos()[i]))
        continue;
      else
      {
        try
        {
          arg = (XMLItem) t.getItem(finstr.xmlConcatInstr.getArgPos()[i], ctx);
          XMLNode node = (XMLNode) arg.getNode().cloneNode(true);
          resDoc.appendChild(node);
        } catch (Exception e)
        {
          // TODO: what?
          e.printStackTrace();
        }
      }
    }
    result.setNode(resDoc);
    dest.xValueSet(dcol, result);
  }
  
  private void invokeXMLForest(AInstr einstr, ITuple[] roles)
    throws ExecException 
  {
    ITuple              dest       = roles[einstr.dr];
    int                 dcol       = einstr.dc;
    XMLElementInstr     xmlInstr   = einstr.xmlInstr;
    int                 numChild   = xmlInstr.getNumAttributes();
    int[]               childRoles = xmlInstr.attrRoles;
    int[]               childPos   = xmlInstr.attrPos;
    Datatype[]          childTypes = xmlInstr.attrTypes;
    XmlManager          xmlMgr     = execContext.getXmlMgr();
    IXmlContext         ctx        = xmlMgr.createContext();
    XMLDocument         doc        = xmlInstr.getDocument();
    XMLDocumentFragment fragment   =
      (XMLDocumentFragment) doc.createDocumentFragment();
    
    for(int i=0; i<numChild; i++)
    {
      ITuple t = roles[childRoles[i]];
      String childVal = new String();
      String childName;
      if(!xmlInstr.attrNames[i].isNameExpr())
        childName = xmlInstr.attrNames[i].getName();
      else
      {
        ITuple attrTuple = roles[xmlInstr.attrNames[i].getAttrRole()];
        int pos = xmlInstr.attrNames[i].getAttrPos();
        if(attrTuple.isAttrNull(pos))
          throw new ExecException(ExecutionError.NAME_EXPR_CANNOT_BE_NULL_FOR_XML_PUB_FUNCS);
        childName = new String(attrTuple.cValueGet(pos),0 , attrTuple.cLengthGet(pos));
      }
      if (t.isAttrNull(childPos[i]))
      {
        childVal = "";
      }
      else
      {
        XMLElement child = (XMLElement)doc.createElement(childName);
        switch(childTypes[i].getKind())
        {
          case INT:
            childVal = Integer.toString(t.iValueGet(childPos[i]));
            child.addText(childVal);
            break;
          case FLOAT:
            childVal = Float.toString(t.fValueGet(childPos[i]));
            child.addText(childVal);
            break;
          case BOOLEAN:
            childVal = Boolean.toString(t.boolValueGet(childPos[i]));
            child.addText(childVal);
            break;
          case DOUBLE:
            childVal = Double.toString(t.dValueGet(childPos[i]));
            child.addText(childVal);
            break;
          case BIGDECIMAL:
            childVal = t.nValueGet(childPos[i]).toString();
            child.addText(childVal);
            break;
          case BIGINT:
            childVal = Long.toString(t.lValueGet(childPos[i]));
            child.addText(childVal);
            break;
          case BYTE:
            childVal = new String(t.bValueGet(childPos[i]),0 ,t.bLengthGet(childPos[i]));
            child.addText(childVal);
            break;
          case XMLTYPE:
            try {
              Node tmp = (Node)((XMLItem)t.getItem(childPos[i], ctx)).getNode();
              tmp = child.getOwnerDocument().importNode(tmp, true);
              child.appendChild(tmp);
            }catch(Exception e)
            {
              
            }
            break;
          case CHAR:
            childVal = new String(t.cValueGet(childPos[i]),0, t.cLengthGet(childPos[i]));
            child.addText(childVal);
            break;
          case TIMESTAMP:
            childVal = Long.toString(t.tValueGet(childPos[i]));
            child.addText(childVal);
            break;
            
            //TODO: For Interval data types, 
            // It is not Sure Whether we should use toString() or call
            // IntervalConvert.getDSInterval or IntervalConverter.getYMInterval
          case INTERVAL:
            childVal = Long.toString(t.vValueGet(childPos[i]));
            child.addText(childVal);
            break;
          case INTERVALYM:
            childVal = Long.toString(t.vymValueGet(childPos[i]));
            child.addText(childVal);
            break;
          default: assert false;
        }
        fragment.appendChild(child);
      }
    }
    XMLItem item = ctx.createItem(); 
    item.setNode(fragment);
    dest.xValueSet(dcol, item);
  }
  
  
  private void invokeXMLElement(AInstr einstr, ITuple[] roles)
    throws ExecException 
  {
    ITuple          dest       = roles[einstr.dr];
    int             dcol       = einstr.dc;
    XMLElementInstr xmlInstr   = einstr.xmlInstr;
    
    int             numAttrs   = xmlInstr.getNumAttributes();
    int[]           attrRoles  = xmlInstr.attrRoles;
    int[]           attrPos    = xmlInstr.attrPos;
    Datatype[]      attrTypes  = xmlInstr.attrTypes;
    
    int             numChild   = xmlInstr.getNumChild();
    int[]           childRoles = xmlInstr.childRoles;
    int[]           childPos   = xmlInstr.childPos;
    Datatype[]      childTypes = xmlInstr.childTypes;
    
    XmlManager      xmlMgr     = execContext.getXmlMgr();
    IXmlContext     ctx        = xmlMgr.createContext();
    XMLDocument     doc        = xmlInstr.getDocument();
    XMLElement      root;

    if(xmlInstr.getElementName() != null)
      root = (XMLElement)doc.createElement(xmlInstr.getElementName());
    else
    {
      ITuple tuple = roles[xmlInstr.elemNameRole];
      int position = xmlInstr.elemNamePos;
      if(tuple.isAttrNull(position))
        throw new ExecException(ExecutionError.NAME_EXPR_CANNOT_BE_NULL_FOR_XML_PUB_FUNCS);
      String name = new String(tuple.cValueGet(position), 0 ,tuple.cLengthGet(position));
      root = (XMLElement)doc.createElement(name);
    }
    
    for(int i=0; i<numAttrs; i++)
    {
      ITuple t = roles[attrRoles[i]];
      String attrVal = new String();
      String attrName;
      if(!xmlInstr.attrNames[i].isNameExpr())
        attrName = xmlInstr.attrNames[i].getName();
      else
      {
        ITuple attrTuple = roles[xmlInstr.attrNames[i].getAttrRole()];
        int pos = xmlInstr.attrNames[i].getAttrPos();
        if(attrTuple.isAttrNull(pos))
          throw new ExecException(ExecutionError.NAME_EXPR_CANNOT_BE_NULL_FOR_XML_PUB_FUNCS);
        attrName = new String(attrTuple.cValueGet(pos),0 , attrTuple.cLengthGet(pos));
      }
      if (t.isAttrNull(attrPos[i]))
      {
        attrVal = "";
      }
      else
      {
        switch(attrTypes[i].getKind())
        {
          case INT: attrVal = Integer.toString(t.iValueGet(attrPos[i]));
          break;
          case FLOAT:
            attrVal = Float.toString(t.fValueGet(attrPos[i]));
            break;
          case BOOLEAN:
            attrVal = Boolean.toString(t.boolValueGet(attrPos[i]));
            break;
          case DOUBLE:
            attrVal = Double.toString(t.dValueGet(attrPos[i]));
            break;
          case BIGDECIMAL:
            attrVal = t.nValueGet(attrPos[i]).toString();
            break;
          case BIGINT:
            attrVal = Long.toString(t.lValueGet(attrPos[i]));
            break;
          case BYTE:
            attrVal = new String(t.bValueGet(attrPos[i]),0 ,t.bLengthGet(attrPos[i]));
            break;
          case XMLTYPE:
            try {
              attrVal = ((XMLItem)t.getItem(attrPos[i], ctx)).toString();
            } catch(Exception e)
            {
              attrVal = "";
            }
            break;
          case CHAR:
            attrVal = new String(t.cValueGet(attrPos[i]),0, t.cLengthGet(attrPos[i]));
            break;
          case TIMESTAMP:
            attrVal = Long.toString(t.tValueGet(attrPos[i]));
            break;
            //TODO: For Interval data types, 
            // It is not Sure Whether we should use toString() or call
            // IntervalConvert.getDSInterval or IntervalConverter.getYMInterval
          case INTERVAL:
            attrVal = Long.toString(t.vValueGet(attrPos[i]));            
            break;
          case INTERVALYM:
            attrVal = Long.toString(t.vymValueGet(attrPos[i]));            
            break;
          default: assert false;
        }
      }
      // Do not create attribute if value is null
      if(attrVal.length() != 0)
        root.setAttribute(attrName, attrVal);
    }
    
    for(int j=0; j<numChild; j++)
    {
      ITuple t = roles[childRoles[j]];
      if (t.isAttrNull(childPos[j]))
      {
        
      }
      else
      {
        switch(childTypes[j].getKind())
        {
          case INT: 
            root.addText(Integer.toString(t.iValueGet(childPos[j])));
            break;
          case FLOAT: 
            root.addText(Float.toString(t.fValueGet(childPos[j])));
            break;
          case BOOLEAN: 
            root.addText(Boolean.toString(t.boolValueGet(childPos[j])));
            break;
          case DOUBLE: 
            root.addText(Double.toString(t.dValueGet(childPos[j])));
            break;
          case BIGDECIMAL: 
            root.addText(t.nValueGet(childPos[j]).toString());
            break;
          case BIGINT: 
            root.addText(Long.toString(t.lValueGet(childPos[j])));
            break;
          case BYTE: 
            root.addText(new String(t.bValueGet(childPos[j]), 0 , t.bLengthGet(childPos[j])));
            break;
          case XMLTYPE: 
            try {
              //always import child node into parent document before appending.
              Node xmlnode = (Node)((XMLItem)t.getItem(childPos[j], ctx)).getNode();
              Document parent = root.getOwnerDocument();
              xmlnode = parent.importNode(xmlnode, true);
              root.appendChild(xmlnode);
            } catch(Exception e)
            {
              root.addText("");
            }
            break;
          case CHAR:
            root.addText(new String(t.cValueGet(childPos[j]), 0, t.cLengthGet(childPos[j])));
          break;
          case TIMESTAMP: 
            root.addText(Long.toString(t.tValueGet(childPos[j])));
          break;
          //TODO: For Interval data types, 
          // It is not Sure Whether we should use toString() or call
          // IntervalConvert.getDSInterval or IntervalConverter.getYMInterval
          case INTERVAL: 
            root.addText(Long.toString(t.vValueGet(childPos[j])));
          break;
          case INTERVALYM: 
            root.addText(Long.toString(t.vymValueGet(childPos[j])));
          break;
          default: assert false;
        }
      }
    }
   XMLItem item = ctx.createItem(); 
   item.setNode(root);
   dest.xValueSet(dcol, item);
  }

  /**
   * Method to invoke the xquery function and set the result
   * 
   * @param i
   *          the instruction number for the XQRY_FNC instruction
   */
  private void invokeXQryFunction(AInstr finstr, ITuple[] roles)
    throws ExecException 
  {
    int    dcol    = finstr.dc;
    ITuple dest    = roles[finstr.dr];

    try
    {
      XMLSequence res = execXQry(finstr, roles);
      
      // no arguments for now
      boolean isNull = true;
      String result = null;
      
      // The result needs to be casted into a STRING
      while (res.next())
      {    
        isNull = false;
	StringWriter wr = new StringWriter();
	PrintWriter wrp = new PrintWriter(wr);
	res.printResult(wrp, XQMesg.newInstance(null));
	wrp.flush();
	wr.flush();
	if (result == null)
	  result = wr.toString();
	else
	  result = result.concat(wr.toString());
      }
      if (isNull)
	dest.setAttrNull(dcol);
      else
      {
	// This is a XDK quirk, if the node is a element, a newline
	// is present at the end, otherwise not
        //not sure how XDK adds lineends, add all possibilities..
        String lineends = System.getProperty("line.separator");
        if (lineends.indexOf('\n') < 0)
          lineends += '\n';
        if (lineends.indexOf('\r') < 0)
          lineends += '\r';
        if (lineends.indexOf(Character.LINE_SEPARATOR) < 0)
          lineends += Character.LINE_SEPARATOR;
        char[] lineendchars = lineends.toCharArray();
	int len;
	char[] val = result.toCharArray();
	len = result.length();
	while(len > 0)
	{
	  char v = val[len - 1];
	  boolean has = false;
	  for (int i = 0; i < lineendchars.length; i++)
	  {
	    if (v == lineendchars[i])
	    {
	      has = true;
	      break;
	    }
	  }
	  if (has)
	  { 
	    len--;
	  }
	  else
	  {
	    break;
	  }
        }
	switch (finstr.returnType.getKind())
	{
	  case XMLTYPE:
	  {
	    dest.xValueSet(dcol, val, len);
	    break;
	  }
	  case CHAR:
	  {
	    dest.cValueSet(dcol, val, len);
	    break;
	  }
	  case INT:
	  {
	    Integer ival = Integer.valueOf(new String(val, 0, len));
	    dest.iValueSet(dcol, ival.intValue());
	    break;
	  }  
	  case BIGINT:
	  {
	    Long lval = Long.valueOf(new String(val, 0, len));
	    dest.lValueSet(dcol, lval.longValue());
	    break;
	  }  
	  case FLOAT:
	  {
	    Float fval = Float.valueOf(new String(val, 0, len));
	    dest.fValueSet(dcol, fval.floatValue());
	    break;
	  }  
	  case DOUBLE:
	  {
	    Double dval = Double.valueOf(new String(val, 0, len));
	    dest.dValueSet(dcol, dval.doubleValue());
	    break;
	  } 
	  case BIGDECIMAL:
          {
            BigDecimal nVal = new BigDecimal(new String(val, 0, len));
            dest.nValueSet(dcol, nVal,nVal.precision(), nVal.scale());
            break;
          } 
	  case BOOLEAN:
	  {
	    boolean b = Datatype.strToBoolean(new String(val, 0, len));
	    dest.boolValueSet(dcol, b);
	    break;
	  }
	  case TIMESTAMP:
	  {
	    String v = new String(val, 0, len);
	    CEPDateFormat dateFormatParser = CEPDateFormat.getInstance();
	    try
	    {
	      CEPDate parsedVal = dateFormatParser.parse(v);
	      dest.tValueSet(dcol, parsedVal.getValue());
	      dest.tFormatSet(dcol, parsedVal.getFormat());
	    }
	    catch(ParseException e)
	    {
	      //TODO: Make Error Messages equivalent to database error messages
	      throw new InterfaceException(InterfaceError.INVALID_TIMESTAMP_FORMAT,
	          e,
	          new Object[]{v});
	    }
	    break;
	  }
	  case INTERVAL:
	  {
	    // Call parseDuration with isYearToMonth = false, we will get
	    // total number of nanoseconds represented by this interval
	    long numNanos = parseDuration(val, len, false);
	    
	    IntervalFormat destinationFormat = 
	        new IntervalFormat(TimeUnit.DAY, 
	                           TimeUnit.SECOND,
	                           9,
	                           9);
	    dest.vValueSet(dcol, numNanos, destinationFormat);
	    break;
	  }
	  case INTERVALYM:
    {
      long numMonths = parseDuration(val, len, true);
      
      IntervalFormat destinationFormat = 
          new IntervalFormat(TimeUnit.YEAR, 
                             TimeUnit.MONTH,
                             9,
                             true);
      dest.vymValueSet(dcol, numMonths, destinationFormat);
      break;
    }
	  default: assert false;
	}
      }
    } 
    catch (Exception x) 
    {
      LogUtil.warning(LoggerType.TRACE, x.getMessage());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, x);
      dest.setAttrNull(dcol);
      // throw new ExecException(ExecutionError.XQRY_FUNC_ERROR);
    }
  }

  private void invokeAggrFunction(AInstr finstr, boolean plus, ITuple[] roles)
    throws ExecException
  {
    ITuple        src1     = roles[finstr.r1];
    int           col1     = finstr.c1;
    ITuple        dest     = roles[finstr.dr];
    int           dcol     = finstr.dc;

    int           numArgs  = finstr.numArgs;
    Datatype[]    argTypes = finstr.argTypes;
    int[]         argRoles = finstr.argRoles;
    int[]         argPos   = finstr.argPos;

    Datatype      retType  = finstr.returnType;
    IAggrFunction handler  = null;
    /* Handle Aggr func cannt be null*/

    AggrValue     result;
    AggrValue[]   inputs;
       
    assert(!src1.isAttrNull(col1));
    
    inputs = finstr.getAggrInputs();
    result = finstr.getAggrResult();
        
    for (int j = 0; j < numArgs; j++)
    {
      ITuple t = roles [argRoles[j] ];
      if (t.isAttrNull(argPos[j]))
      {
        inputs[j].setNull(true);
      }
      else
      {
        switch (argTypes[j].getKind())
        {
        case INT:
          inputs[j].setNull(false);
          ((AggrInteger)inputs[j]).setValue(t.iValueGet(argPos[j]));
          break;
        case FLOAT:
          inputs[j].setNull(false);
          ((AggrFloat)inputs[j]).setValue(t.fValueGet(argPos[j]));
          break;
        case DOUBLE:
          inputs[j].setNull(false);
          ((AggrDouble)inputs[j]).setValue(t.dValueGet(argPos[j]));
          break;
        case BIGDECIMAL:
          inputs[j].setNull(false);
          ((AggrBigDecimal)inputs[j]).setValue(t.nValueGet(argPos[j]));
          break;
        case BIGINT:
          inputs[j].setNull(false);
          ((AggrBigInt)inputs[j]).setValue(t.lValueGet(argPos[j]));
          break;
        case CHAR:
          inputs[j].setNull(false);
          ((AggrChar)inputs[j]).setValue(t.cValueGet(argPos[j]));
          break;
        case BYTE:
          inputs[j].setNull(false);
          ((AggrByte)inputs[j]).setValue(t.bValueGet(argPos[j]));
          break;
        case TIMESTAMP:
          inputs[j].setNull(false);
          ((AggrTimestamp)inputs[j]).setValue(t.tValueGet(argPos[j]));
          break;
        case INTERVAL:
          inputs[j].setNull(false);
          ((AggrInterval)inputs[j]).setValue(t.vValueGet(argPos[j]));
          break;
        case INTERVALYM:
          inputs[j].setNull(false);
          ((AggrInterval)inputs[j]).setValue(t.vymValueGet(argPos[j]));
          break;
        case OBJECT:
          inputs[j].setNull(false);
          ((AggrObj)inputs[j]).setValue(t.oValueGet(argPos[j]));
          break;
        case BOOLEAN:
          inputs[j].setNull(false);
          ((AggrBoolean)inputs[j]).setValue(t.boolValueGet(argPos[j]));
          break;
        default:
          assert false : argTypes[j];
          break;
        }
      }
    }

    result.setNull(false);
    
    // Invoke the function
    try {
     
      handler = (IAggrFunction)src1.oValueGet(col1);
      dest.oValueSet(col1, handler);
      Date start = new Date();
      
      if (plus)
        handler.handlePlus(inputs, result);
      else
        handler.handleMinus(inputs, result);
      
      Date end = new Date();
      int fid = finstr.getFunctionId();
      execContext.getExecStatsMgr().incrUserFuncStats(fid, (end.getTime() - start.getTime()));
      
    } catch(UDAException uf)
    {
      throw new ExecException(ExecutionError.USERDEFINED_AGGREGATION_FUNCTION_RUNTIME_ERROR,
            uf,
            (handler == null ? "" : handler.toString()));
    }
    if (result.isNull())
      dest.setAttrNull(dcol); 
    else
    {
      dest.setAttrbNullFalse(dcol);
      IntervalFormat destinationFormat = null;
      switch (retType.getKind())
      {      
      case INT:
        dest.iValueSet(dcol,  ((AggrInteger) result).getValue());
        break;
        
      case FLOAT:
        dest.fValueSet(dcol,  ((AggrFloat) result).getValue());
        break;
        
      case DOUBLE:
        dest.dValueSet(dcol,  ((AggrDouble) result).getValue());
        break;
       
      case BIGDECIMAL:
        dest.nValueSet(dcol,  ((AggrBigDecimal) result).getValue(), 
                       ((AggrBigDecimal) result).getValue().precision(), 
                       ((AggrBigDecimal) result).getValue().scale());
        break;
        
      case BIGINT:
        dest.lValueSet(dcol,  ((AggrBigInt) result).getValue());
        break;
        
      case CHAR:
        int clen = ((((AggrChar) result).getValue()) == null) ?
              0 : ((AggrChar) result).getValue().length;
        dest.cValueSet(dcol,((AggrChar) result).getValue(), clen);
        break;
        
      case BYTE:
        int blen = ((((AggrByte) result).getValue()) == null) ?
            0 : ((AggrByte) result).getValue().length;
        dest.bValueSet(dcol,((AggrByte) result).getValue(), blen);
        break;
        
      case TIMESTAMP:
        dest.tValueSet(dcol, ((AggrTimestamp) result).getValue());
        break;
        
      case INTERVAL:
        try
        {
          destinationFormat = 
            new  IntervalFormat(TimeUnit.DAY,
                                TimeUnit.SECOND,
                                9,
                                9);
        } 
        catch (CEPException e)
        {
          // Should be unreacheable as we are creating interval format using
          // correct parameter values
          assert false;
        }
        dest.vValueSet(dcol, ((AggrInterval) result).getValue(), 
                       destinationFormat);
        break;
              
      case INTERVALYM:
        try
        {
          destinationFormat = 
            new  IntervalFormat(TimeUnit.YEAR,
                                TimeUnit.MONTH,
                                9,
                                true);
        }
        catch (CEPException e)
        {
          // Should be unreacheable as we are creating interval format using
          // correct parameter values
          assert false;
        }
        dest.vymValueSet(dcol, ((AggrInterval) result).getValue(),
                         destinationFormat);
        break;
        
      case OBJECT:

        Object objResultVal = ((AggrObj) result).getValue();
        Object finalResultVal;
        if((objResultVal == null) ||(!(objResultVal instanceof Cloneable)))
        {
          // If the result is not cloneable, the finalResultVal is initialized 
          // to the original returned result value 
          finalResultVal = objResultVal;
        }
        else
        {
          try
          {
            Method m = objResultVal.getClass().getMethod("clone", 
                                                         (Class<?>[])null);
            if(m != null)
            {
              // Clone the result object
              finalResultVal = m.invoke(objResultVal);
            }
            else
            {
              // There is no clone method
              finalResultVal = objResultVal;
            }
          }
          catch(NoSuchMethodException nmse) {
            // In case of Exception, keep the original object
            finalResultVal = objResultVal;
          }
          catch (InvocationTargetException e) {
            if(e.getCause() != null)
              throw new RuntimeException(e.getCause());
            else
              throw new RuntimeException(e);
          }
          catch(IllegalAccessException e) {
            // In case of Exception, keep the original object
            finalResultVal = objResultVal;
          }
          catch(Exception e)
          {
            if(e instanceof CloneNotSupportedException)
            {
              // Even if the Class implements cloneable, It may throw 
              // CloneNotSupportedException
              finalResultVal = objResultVal;
            }
            else
              throw new RuntimeException(e);
          }
        }
        dest.oValueSet(dcol, finalResultVal);
        break;
              
             
      case BOOLEAN:
        dest.boolValueSet(dcol, ((AggrBoolean) result).getValue());
        break;
        
      default:
        assert false : retType;
        break;
      }
    }
  }
  
  private void releaseAggrHandlers(AInstr instr, ITuple[] roles)
    throws ExecException
  {   
    int              pos;
    IAggrFunction    handler;
    int[]            udaPos;
    IAggrFnFactory[] udaFactory;
    int              len;
    ITuple           t       = roles[ instr.r1 ];
    
    udaPos     = instr.getUdaPos();
    udaFactory = instr.getUdaFactory();
    len        = udaPos.length;
    
    for (int i = 0; i < len; i++)
    {
      pos = udaPos[i];
      handler = (IAggrFunction) t.oValueGet(pos);

      try
      {
        udaFactory[i].freeAggrFunctionHandler(handler);
      }
      catch (UDAException u)
      {
        throw new ExecException(ExecutionError.UDA_ERROR, u);
      }

      t.oValueSet(pos, null);
    }
  }
  
  private void resetAggrHandlers(AInstr instr, ITuple[] roles)
    throws ExecException 
  {
    int    pos;
    int    len;
    int[]  udaPos;
    ITuple t = roles[ instr.r1 ];

    udaPos   = instr.getUdaPos();
    len      = udaPos.length;
    
    for (int i = 0; i < len; i++)
    {
      pos = udaPos[i];
      t.oValueSet(pos, null);
    }
  }
  
  /**
   * allocate aggregate handlers
   * @param instrIndex
   * @throws ExecException
   */
  private void allocAggrHandlers(AInstr instr, ITuple[] roles) 
    throws ExecException 
  {
    int              pos;
    IAggrFunction    handler;
    int[]            udaPos;
    IAggrFnFactory[] udaFactory;
    IAggrFunction[]  udaHandler;
    int              len;
    ITuple           t       = roles[ instr.r1 ];
    
    udaPos     = instr.getUdaPos();
    udaFactory = instr.getUdaFactory();
    udaHandler = instr.getUdaHandler();
    len        = udaPos.length;
    
    for (int i = 0; i < len; i++)
    {
      pos = udaPos[i];

      try
      {
        handler = udaHandler[i];
        if (handler == null)
          handler = udaFactory[i].newAggrFunctionHandler();
      }
      catch (UDAException u)
      {
        throw new ExecException(ExecutionError.UDA_ERROR, u);
      }

      t.oValueSet(pos, handler);
    }
  }
  
  /*
   * Check if we can use Tuple Copy
   */
  private void prepCopyOp()
  {
    if (numInstrs == 0)
    {
      copyFullAttrs = -1;
      return;
    }
    copySrcAttrs = new int[numInstrs];
    copyDestAttrs = new int[numInstrs];
    copyFullAttrs = numInstrs;
    int prev = -1;
    int srcRole = -1;
    int destRole = -1;
    for (int i = 0; i < numInstrs; i++)
    {
      AInstr instr = instrs[i];
     
      if (srcRole == -1)
        srcRole = instr.r1;
      if (destRole == -1)
        destRole = instr.dr;

      if ((instr.op != AOp.INT_CPY &&
              instr.op != AOp.BIGINT_CPY &&
              instr.op != AOp.FLT_CPY &&
              instr.op != AOp.DBL_CPY && 
              instr.op != AOp.BIGDECIMAL_CPY && 
              instr.op != AOp.CHR_CPY &&
              instr.op != AOp.BYT_CPY &&
              instr.op != AOp.TIM_CPY &&
              instr.op != AOp.INTERVAL_CPY &&
              instr.op != AOp.INTERVALYM_CPY &&
              instr.op != AOp.XMLT_CPY &&
              instr.op != AOp.OBJ_CPY &&
              instr.op != AOp.BOOLEAN_CPY) ||
         (srcRole != instr.r1 || destRole != instr.dr) )
      {
        copySrcAttrs = null;
        copyDestAttrs = null;
        copyFullAttrs = -1;
        return;
      }
      copySrcAttrs[i] = instr.c1;
      copyDestAttrs[i] = instr.dc;
      if (copyFullAttrs != -1)
      {
        prev++;
        if (instr.c1 != instr.dc || instr.c1 != prev)
        {
          copyFullAttrs = -1;
        }
      }
    }
    if (copyFullAttrs >= 0)
    {
      copySrcAttrs = null;
      copyDestAttrs = null;
    }
  }
 
  /**
   * Parse xsd:duration string and return total number of seconds
   * 
   * 
   * @param duration The duration in XML Schema xsd:duration format
   * @return number of second represented by duration
   * 
   */
  // Number of seconds in a hour
  private static int NUM_SECONDS_IN_HOUR = 3600;
	
  // Number of seconds in a minute
  private static int NUM_SECONDS_IN_MINUTE = 60;
	
  // Number of seconds in a day
  private static int NUM_SECONDS_IN_DAY = NUM_SECONDS_IN_HOUR* 24;
  
  public static long parseDuration(char[] vals, int len, boolean isYearToMonth) 
    throws InterfaceException
  {
  	Double seconds = (double) 0;
  	long minutes = 0;
  	long hours = 0;
  	long days = 0;
    long years = 0;
    long months = 0;
    
  	//[-]PnYnMnDTnHnMnS
  	int state = 0;
  	long sign = 1;
  	StringBuilder val = null;
  	for (int i = 0; (i < len) ; i++)
  	{
  	  char c = vals[i];
  	  switch(state)
  	  {
  	  case 0:    // - or P
  	    if (c == '-')
  	    {
  	      sign = -1;
  	      state++;
  	      break;
  	    } 
  	    //fall through
  	  case 1:  //P
  	    if (c == 'P')
  	    {
  	      val = new StringBuilder();
  	      state = 2;
  	      break;
  	    }
  	    else
  	    {
  	      throw new InterfaceException(InterfaceError.INVALID_DURATION_FORMAT,
                  new Object[]{new String(vals, 0, len)});
  	    }
  	  case 2:    // day
  	  case 3:
  	    if (Character.isDigit(c) || c == '.')
  	    {
  	      val.append(c);
  	    } else {
  	      String vstr = val.toString();
              long v = 0;
              double dv = 0;
              if (vstr.length() == 0)
              {
                if (c != 'T')
                {
                  throw new InterfaceException(InterfaceError.INVALID_DURATION_FORMAT,
                      new Object[]{new String(vals, 0, len)});
                }              }
              else
              {
                try
                {
                  if (vstr.indexOf('.') >= 0)
                  {
                    if (c != 'S')
                    {
                      throw new InterfaceException(InterfaceError.INVALID_DURATION_FORMAT,
                          new Object[]{new String(vals, 0, len)});
                    }
                    dv = Double.valueOf(vstr).doubleValue();
                  }
                  else
                  {
                    v = Long.valueOf(vstr).longValue();
                    dv = v;
                  }
                }
                catch(NumberFormatException e)
                {
                  throw new InterfaceException(InterfaceError.INVALID_DURATION_FORMAT,
                      new Object[]{new String(vals, 0, len)});
                }
              }
              if (state == 2)
              {
                switch(c)
                {
                case 'Y':  years = v; break;    
                case 'M':  months = v; break;
                case 'D':  days = v; break;
                case 'T':  state = 3; break;
                default:
                  throw new InterfaceException(InterfaceError.INVALID_DURATION_FORMAT,
                      new Object[]{new String(vals, 0, len)});
                }
              } else
              {
                switch(c)
                {
                case 'H':  hours = v; break;    
                case 'M':  minutes = v; break;
                case 'S':  seconds = dv; break;
                default:
                  throw new InterfaceException(InterfaceError.INVALID_DURATION_FORMAT,
                      new Object[]{new String(vals, 0, len)});
                }
              }
  	      val = new StringBuilder();
  	    }
  	    break;
  	  }
  	}
  	if (months > 0)
  	  days += months * 30;
  	if (years > 0)
  	  days += years * (30 * 12);
  	
    // Note: 1 MONTH = 2592000 SECONDS
    long numSeconds = days * NUM_SECONDS_IN_DAY +
                      hours * NUM_SECONDS_IN_HOUR +
                      minutes * NUM_SECONDS_IN_MINUTE +
                      seconds.longValue();
    
  	if(isYearToMonth)
  	{
  	  long numMonths = months;
  	  numMonths += years * 12; 	  
  	  numMonths += (numSeconds)/2592000l; 
  	  return sign * numMonths;
  	}
  	else
  	{
  	  long numDays = years * 365l;
  	  numDays += months*30;
  	  numSeconds += numDays * 86400l;
  	  long numNanos = numSeconds * 1000000000l;
  	  return sign * numNanos;
  	}  	
  }
	 
  // toString
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<AEval numInstrs=\"" + numInstrs + "\" >");
    for (int i = 0; i < numInstrs; i++)
    {
      sb.append(instrs[i].toString());
    }
    sb.append("</AEval>");
    return sb.toString();
  }
}
