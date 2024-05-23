/
/ $Header: pcbpel/cep/tools/visualizer/README.txt /main/3 2009/01/02 04:54:43 sbishnoi Exp $
/
/ README.txt
/
/ Copyright (c) 2008, Oracle. All Rights Reserved.
/
/   NAME
/     README.txt - <one-line expansion of the name>
/
/   DESCRIPTION
/     <short description of component this file declares/defines>
/     This file contains instructions about how to run CEP visualizer
/
/   NOTES
/     <other useful comments, qualifications, etc.>
/
/   MODIFIED   (MM/DD/YY)
/   sbishnoi    01/02/09 - incorporating latest change to build visualizer
/   hopark      11/06/08 - fix dumpplan
/   sborah      09/26/08 - 
/   sbishnoi    09/26/08 - Creation
/

Visualizer can be invoked by running a simple script from the directory 
$ADE_VIEW_ROOT/pcbpel/cep/tools/visualizer/bin

Visualier reads plan in xml format and displays query components in form of
graphical objects.

Visualizer can read xml plan from either
 a) xml dump file
    OR
 b) running CEP Server


1) To read plan from XML Dump file, Run visfile.sh script.

   $sh visfile.sh <file_name>

   Make sure that a dump file is there in directory $T_WORK/cep.
   Dump file will be of name: <test_file_name>_dump.xml
   (Refer to Appendix A to know about how to generate this dump file)

2) To read plan from a Running CEP Server; run visjdbc.sh script.

    $sh visjdbc.sh jdbc:oracle:cep@hostname:port:service

    Here
      <hostname> is the machine name where CEP server is running.
      <port> is the port number where CEP Server is listening; 
             default is 1199
      <service> is the name of CEP Server's RMI Service
             default is sys (Presently only default service is running)


MANDATORY STEPS:

Prior to Running above mentioned script, ADE environment must be set properly;
Following are the steps to set the env:
1) Enter into the ADE View
   $ade useview <view name>

2) Compile Visualizer Code
   Goto $PCHOME/cep/tools/visualizer
   Run $ant dist


Appendix:A

* To generate a dump file;
* Goto $PCHOME/cep and   Run a test whose plan you want to analyse:
*     $ant tkcqlx.wlevs -Dcqlx.file=<test_file_name>
*
*   Make sure that a dump file has been created in directory $T_WORK/cep
*   dump file will be of name: <test_file_name>_dump.xml
