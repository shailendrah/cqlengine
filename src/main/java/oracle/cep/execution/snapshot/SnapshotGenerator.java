/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/SnapshotGenerator.java hopark_cqlsnapshot/4 2016/02/26 11:55:08 hopark Exp $ */

/* Copyright (c) 2015, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/15/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/SnapshotGenerator.java hopark_cqlsnapshot/4 2016/02/26 11:55:08 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.snapshot;

import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;
import oracle.cep.snapshot.SnapshotContext;
import oracle.cep.common.*;
import oracle.cep.dataStructures.external.*;
import oracle.cep.dataStructures.internal.*;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.dataStructures.internal.memory.*;
import oracle.cep.dataStructures.internal.memory.QSinglyList.QSinglyListNode;
import oracle.cep.dataStructures.internal.memory.TupleDoublyList.TupleDoublyListNode;
import oracle.cep.execution.comparator.*;
import oracle.cep.execution.internals.*;
import oracle.cep.execution.operators.*;
import oracle.cep.execution.orderby.PartitionByContext;
import oracle.cep.execution.pattern.*;
import oracle.cep.execution.queues.*;
import oracle.cep.execution.stores.*;
import oracle.cep.execution.synopses.*;

public class SnapshotGenerator extends OperatorTraversal
{
	ObjectOutputStream output;
	boolean fullSnapshot;
	static boolean s_registered = false;
	
	public SnapshotGenerator(ExecContext ec)
	{
		super(ec);
	}
	
	public void createSnapshot(ObjectOutputStream output, boolean fullSnapshot) throws CEPException
	{
		this.output = output;
		this.fullSnapshot = fullSnapshot;
		traverse();
	}

	protected void process(ExecOpt operator) throws CEPException
	{
	  LogUtil.fine(LoggerType.TRACE, "SnapshotGenerator is processing operator:" + operator.getOptName());
		operator.createSnapshot(output, fullSnapshot);
	}

	private static void registerClassByName(int id, String name) throws ClassNotFoundException
	{
	    Class<?> cls = Class.forName(name);
	    SnapshotContext.registerClass(id,cls);
    }

	//Register classes used in snapshot
	public static void registerClasses() throws ClassNotFoundException
	{
	    if (s_registered) return;
	      s_registered = true;
          SnapshotContext.registerClass(5001,byte[].class);
          SnapshotContext.registerClass(5002,char[].class);
          SnapshotContext.registerClass(5003,AtomicLong.class);
          SnapshotContext.registerClass(5004,java.util.ArrayList.class);
          SnapshotContext.registerClass(5005,java.util.LinkedList.class);
          SnapshotContext.registerClass(5006,java.util.Hashtable.class);
          SnapshotContext.registerClass(5007,TimeZone.class);
          SnapshotContext.registerClass(5008,SimpleDateFormat.class);
          SnapshotContext.registerClass(5009,BigDecimal.class);
          SnapshotContext.registerClass(5010,DateFormat.class);
          registerClassByName(5011,"sun.util.calendar.ZoneInfo");
          SnapshotContext.registerClass(5012,java.util.GregorianCalendar.class);
          SnapshotContext.registerClass(5013,java.util.Date.class);
          SnapshotContext.registerClass(5014,java.text.DateFormatSymbols.class);
          SnapshotContext.registerClass(5015,String[].class);
          SnapshotContext.registerClass(5016,String[][].class);
          SnapshotContext.registerClass(5017,java.util.Locale.class);
          SnapshotContext.registerClass(5018,java.text.DecimalFormat.class);
          SnapshotContext.registerClass(5019,java.math.RoundingMode.class);
          SnapshotContext.registerClass(5020,java.text.DecimalFormatSymbols.class);
          SnapshotContext.registerClass(5021,java.util.PriorityQueue.class);
          
          //common
          SnapshotContext.registerClassExternalizable(6001,AttributeMetadata.class);
          SnapshotContext.registerClassExternalizable(6002,Datatype.class);
          SnapshotContext.registerClassExternalizable(6003,IntervalFormat.class);
          SnapshotContext.registerClassExternalizable(6004,TimestampFormat.class);
          SnapshotContext.registerClassExternalizable(6005,TimeUnit.class);
        
          //internal
          SnapshotContext.registerClassExternalizable(6011,AttrSpec.class);
          SnapshotContext.registerClassExternalizable(6012,TupleSpec.class);
        
          //tuples external
          SnapshotContext.registerClassExternalizable(5101,AttributeValue.class);
          SnapshotContext.registerClassExternalizable(5102,BigDecimalAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5103,BigintAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5104,BooleanAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5105,ByteAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5106,CharAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5107,DoubleAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5108,FloatAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5109,IntAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5110,IntervalAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5111,IntervalYMAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5112,ObjAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5113,TimestampAttributeValue.class);
          SnapshotContext.registerClass(5114,TupleKind.class);
          SnapshotContext.registerClassExternalizable(5115,XmltypeAttributeValue.class);
          SnapshotContext.registerClassExternalizable(5116,TupleValue.class);
          //tuples internal
          SnapshotContext.registerClassExternalizable(5200,QueueElement.class);
          SnapshotContext.registerClass(5201,Kind.class);
          SnapshotContext.registerClassExternalizable(5202,Tuple.class);
          SnapshotContext.registerClassExternalizable(5203,TuplePtr.class);
          SnapshotContext.registerClassExternalizable(5204,AttrVal.class);
          SnapshotContext.registerClassExternalizable(5205,BigDecimalAttrVal.class);
          SnapshotContext.registerClassExternalizable(5206,BigintAttrVal.class);
          SnapshotContext.registerClassExternalizable(5207,BooleanAttrVal.class);
          SnapshotContext.registerClassExternalizable(5208,ByteAttrVal.class);
          SnapshotContext.registerClassExternalizable(5209,CharAttrVal.class);
          SnapshotContext.registerClassExternalizable(5210,DoubleAttrVal.class);
          SnapshotContext.registerClassExternalizable(5211,FloatAttrVal.class);
          SnapshotContext.registerClassExternalizable(5212,IntAttrVal.class);
          SnapshotContext.registerClassExternalizable(5213,IntervalAttrVal.class);
          SnapshotContext.registerClassExternalizable(5214,IntervalYMAttrVal.class);
          SnapshotContext.registerClassExternalizable(5215,ObjectAttrVal.class);
          SnapshotContext.registerClassExternalizable(5216,XmltypeAttrVal.class);
          SnapshotContext.registerClass(5217,AttrVal[].class);
          SnapshotContext.registerClassExternalizable(5218,TimestampAttrVal.class);
        
          //operators
          SnapshotContext.registerClassExternalizable(1001,BinJoinState.class);
          SnapshotContext.registerClassExternalizable(1002,BinStreamJoinState.class);
          SnapshotContext.registerClassExternalizable(1003,DistinctState.class);
          SnapshotContext.registerClassExternalizable(1004,ExceptState.class);
          SnapshotContext.registerClassExternalizable(1005,GroupAggrState.class);
          SnapshotContext.registerClassExternalizable(1006,MinusState.class);
          SnapshotContext.registerClassExternalizable(1007,OrderByState.class);
          SnapshotContext.registerClassExternalizable(1008,OutputState.class);
          SnapshotContext.registerClassExternalizable(1009,PartnWinState.class);
          SnapshotContext.registerClassExternalizable(1010,PatternStrmState.class);
          SnapshotContext.registerClassExternalizable(1011,PatternStrmClassBState.class);
          SnapshotContext.registerClassExternalizable(1012,ProjectState.class);
          SnapshotContext.registerClassExternalizable(1013,RangeWindowJournalEntry.class);
          SnapshotContext.registerClassExternalizable(1014,RangeWindowState.class);
          SnapshotContext.registerClassExternalizable(1015,RelSourceState.class);
          SnapshotContext.registerClassExternalizable(1016,RowWindowState.class);
          SnapshotContext.registerClassExternalizable(1017,RowWindowJournalEntry.class);
          SnapshotContext.registerClassExternalizable(1018,RStreamState.class);
          SnapshotContext.registerClassExternalizable(1019,SelectState.class);
          SnapshotContext.registerClassExternalizable(1020,SlideState.class);
          SnapshotContext.registerClassExternalizable(1021,StreamSourceState.class);
          SnapshotContext.registerClassExternalizable(1022,TableFunctionRelSourceState.class);
          SnapshotContext.registerClassExternalizable(1023,UnionState.class);
          SnapshotContext.registerClassExternalizable(1024,ValueRelationWindowState.class);
          SnapshotContext.registerClassExternalizable(1025,ValueWindowState.class);
          SnapshotContext.registerClassExternalizable(1026,VariableRangeWindowState.class);
          SnapshotContext.registerClassExternalizable(1027,ViewRelnSrcState.class);
          SnapshotContext.registerClassExternalizable(1028,ViewStrmSrcState.class);
          SnapshotContext.registerClassExternalizable(1029,XmlTableState.class);
          SnapshotContext.registerClassExternalizable(1030,XStreamState.class);
          SnapshotContext.registerClass(1031, GroupAggrState.PlusState.class);
          SnapshotContext.registerClass(1032, GroupAggrState.MinusState.class);
          SnapshotContext.registerClass(1033, GroupAggrState.DirtyOutputState.class);
          SnapshotContext.registerClassExternalizable(1034,OrderByTopState.class);
          SnapshotContext.registerClassExternalizable(1035,PartitionByContext.class);
          
          //execution internals
          SnapshotContext.registerClass(2000,ExecState.class);
          SnapshotContext.registerClassExternalizable(2001,ComparatorSpecs.class);
          SnapshotContext.registerClassExternalizable(2002,TupleSpec.class);
          SnapshotContext.registerClassExternalizable(2003,AttrSpec.class);
          SnapshotContext.registerClassExternalizable(2004,SharedQueueReader.class);
          SnapshotContext.registerClassExternalizable(2005,SharedQueueWriter.class);
          SnapshotContext.registerClassExternalizable(2006,BindStore.class);
          SnapshotContext.registerClassExternalizable(2007,PatternPartnContext.class);
          SnapshotContext.registerClassExternalizable(2008,Binding.class);
          SnapshotContext.registerClassExternalizable(2009,BindingList.class);
          SnapshotContext.registerClassExternalizable(2010,BindingTreeSet.class);
          SnapshotContext.registerClassExternalizable(2011,ActiveItem.class);
          SnapshotContext.registerClassExternalizable(2012,UnsureItem.class);
          SnapshotContext.registerClassExternalizable(2013,BindingSynopsis.class);
          SnapshotContext.registerClassExternalizable(2014,DirectInteropQueue.class);
          SnapshotContext.registerClassExternalizable(2015,QSinglyListNode.class);
          SnapshotContext.registerClass(2016,TupleDoublyListNode.class);
          SnapshotContext.registerClassExternalizable(2017,QueueElementImpl.class);
	  }	
}
