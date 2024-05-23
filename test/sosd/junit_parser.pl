#!/usr/local/bin/perl
# 
# $Header: pcbpel/cep/test/sosd/junit_parser.pl /main/3 2009/02/17 17:42:53 hopark Exp $
#
# junit_parser.pl
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates.All rights reserved. 
#
#    NAME
#      junit_parser.pl - <one-line expansion of the name>
#
#    DESCRIPTION
#      Parse the result from junit and create a suc/dif
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    hopark      02/13/09 - check error
#    hopark      12/13/06 - fix status path
#    hopark      11/10/06 - Adopt to CEP
#    atbarboz    06/07/06 - Generate Suc FIle 
#    atbarboz    05/18/06 - JUnit 4.0 support 
#    atbarboz    05/02/06 - Creation
#

$test_class = $ARGV[0] ;
$test_script = $ARGV[1] ;
$log_dir = $ARGV[2] ;
$res_dir = $ARGV[3] ;

$NT = 0;
if($^O =~ /MSWin32/i){
  $NT = 1;
}
$separator = $NT ? "\\" : "\/";

print ("$test_class : $test_script : $log_dir : $res_dir\n");

$result_file = $log_dir . $separator . "TEST-" . $test_class . ".log";
$output_dif = $res_dir . $separator . $test_script . ".dif";
$output_suc = $res_dir . $separator . $test_script . ".suc";

if( ! -e $result_file)
{
    open(RESFILE, ">$output_dif");

    print RESFILE ("JUnit Execution Summary for Test Suite ".$test_suite."\n");
    print RESFILE ("=============================================================\n");
    print RESFILE ("Tests executed     :  0\n");

    print RESFILE ("\nFailure and Error Details for Test Suite ".$test_suite."\n");
    print RESFILE ("=============================================================\n");
    print RESFILE ("ERROR: $result_file not generated.\n");

    close(RESFILE);
    exit 0;
}

open(LOGFILE,$result_file);

$log_status=0;
$testcase_no=0;
$test_suite = "$test_class";

while(<LOGFILE>)
{
    if(/Testsuite: (.*)\n/)
    {
        $test_suite = $1;
    }
    elsif(/Tests run: (\d+),\s+Failures: (\d+),\s+Errors: (\d+)/)
    {
        if($log_status == 0)
        {
            $total_test_cases = $1;
            $failed_test_cases = $2;
            $error_test_cases = $3;
            if($failed_test_cases>0 || $error_test_cases>0)
            {
              open(RESFILE, ">$output_dif");
            }
            else
            {
              open(RESFILE, ">$output_suc");
            }
            print RESFILE ("JUnit Execution Summary for Test Suite ".$test_suite."\n");
            print RESFILE ("=============================================================\n");
            print RESFILE ("Tests executed     :  ".$total_test_cases."\n");
            print RESFILE ("Tests failed       :  ".$failed_test_cases."\n");
            print RESFILE ("Tests with errors  :  ".$error_test_cases."\n");
    
            print RESFILE ("\nFailure and Error Details for Test Suite ".$test_suite."\n");
            print RESFILE ("=============================================================");

            $log_status=1;
        }
    }
    elsif(/OK \((.*) test(s?)\)/)
    {
        if($log_status == 0)
        {
            $total_test_cases = $1;

            if($total_test_cases == 0)
            {
                open(RESFILE, ">$output_dif");
            }
            else
            {
                open(RESFILE, ">$output_suc");
            }

            print RESFILE ("JUnit Execution Summary for Test Suite ".$test_suite."\n");
            print RESFILE ("=============================================================\n");
            print RESFILE ("Tests executed     :  ".$total_test_cases."\n");

            if($total_test_cases == 0)
            {
                print RESFILE ("\nFailure and Error Details for Test Suite ".$test_suite."\n");
                print RESFILE ("=============================================================\n");
                close(LOGFILE);
                open(LOGFILE,$result_file);
            }

            $log_status=1;
       }
    }
    elsif(/Tests run: (\d*),\s+Failures: (\d*)/)
    {
        if($log_status == 0)
        {
            $total_test_cases = $1;
            $failed_test_cases = $2;
            if($failed_test_cases>0)
            {
               open(RESFILE, ">$output_dif");
            }
            else
            {
               open(RESFILE, ">$output_suc");
            }
            print RESFILE ("JUnit Execution Summary for Test Suite ".$test_suite."\n");
            print RESFILE ("=============================================================\n");
            print RESFILE ("Tests executed     :  ".$total_test_cases."\n");
            print RESFILE ("Tests failed       :  ".$failed_test_cases."\n");
        
            if($failed_test_cases>0)
            {
               print RESFILE ("\nFailure and Error Details for Test Suite ".$test_suite."\n");
               print RESFILE ("=============================================================\n");
               close(LOGFILE);
               open(LOGFILE,$result_file);
            }

            $log_status=1;
        }
    }
    elsif(/Testcase: (.*) took (.*) sec/)
    {
        $test_case = $1;
    }
    elsif(/Caused an ERROR/)
    {
        $testcase_no++;
        print RESFILE ("\nIssue ".$testcase_no.": The Test \"".$test_case."\" has caused an ERROR.\n");
    }
    elsif(/FAILED/)
    {
        $testcase_no++;
        print RESFILE ("\nIssue ".$testcase_no.": The Test \"".$test_case."\" has FAILED.\n");
    }
    elsif(/(.*)\) (.*)\((.*)\)/)
    {
        if($log_status==1)
        {
            print RESFILE ("\nIssue ".$1.": The Test \"".$2."\" of $3 has FAILED.\n");
        }
    }
    elsif(/JUnit version/ || /(^[\.E]*$)/ || /Time:/ || /There were (.*) failures:/ || /FAILURES/)
    {
        
    }
    elsif($log_status == 1)
    {
        print RESFILE ($_);
    }
}

close(LOGFILE);
close(RESFILE);

