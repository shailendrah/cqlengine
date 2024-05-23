#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/utl/runcep.pl /main/2 2009/07/22 08:50:49 sbishnoi Exp $
#
# runcep
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      runcep - run any class in cep
#
#    DESCRIPTION
#      usage : runcep classfile arguments
# 
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    07/20/09 - incorporating cep directory changes
#    hopark      11/21/06 - Creation
# 
use File::Copy;
use File::Basename;

###############################################################################
# System dependent setup
$NT = 0;
if($^O =~ /MSWin32/i){
  $NT = 1;
}
$separator = $NT ? "\\" : "\/";
###############################################################################
sub printHeader {
  my $linelength = 60;
  my $title = $_[0];
  my $c = $_[1];
  
  my $pos = ($linelength - 2 - length($title) ) / 2;
  my $str = "";
  my $i = 0;
  for (; $i < $pos; $i++) {
    $str .= $c;
  }
  $str .= " " . $title . " ";
  $i += length($title) + 2;
  for (; $i < $linelength; $i++) {
    $str .= $c;
  }
  print($str . "\n");
}

sub TRACE {
  if ($trace) {
    print @_;
    print "\n";
  }
}

sub getClassPath {
  chdir($cep_folder);
  open (PRINTCP, "ant print.classpath 2>&1 |");
  my $state = 0;
  my $classpath = ".:". $cepwork_folder . ":" . $cep_buildclass_folder;
  while ($line = <PRINTCP>) {
    TRACE(">".$line);
    if ($line =~ /print.classpath:/) {
        $state = 1;
    } else {
       if ($state) {
          if ($line =~ /\[echo\] (.*)$/) {
             TRACE("cp : $1");
             $classpath .= ":" . $1;
          }
       }
    }
  }
  close PRINTCP;
  return $classpath;  
}

$ade_view_root = $ENV{ADE_VIEW_ROOT};
$twork = $ENV{ADE_VIEW_ROOT} . $separator . "work";

$cep_folder = $ade_view_root . $separator . "cep" . $separator . "wlevs_cql" . $separator . "modules" . $separator . "cqlengine";
$cepwork_folder = $ade_view_root . $separator . "work" . $separator . "cep";
$ceptest_folder = $cep_folder . $separator ."test";
$ceptest_cql_folder = $ceptest_folder . $separator ."sql";
$ceptest_data_folder = $ceptest_folder . $separator ."data";
$ceptest_config_folder = $ceptest_folder . $separator ."config";
$ceptest_log_folder = $ceptest_folder . $separator ."log";
$cep_buildclass_folder = $cep_folder . $separator . "build" . $separator . "classes";

$cqlx_folder = $cepwork_folder;
$log_folder = $cepwork_folder . $separator . "log";
$input_folder = $cepwork_folder . $separator . "data"; 
$output_folder = $cepwork_folder . $separator . "log";
$diff_folder = $cepwork_folder; 

$classpath = getClassPath;
$memory = 1024;
$opt = "";
$stat = 0;
$ade_view_root = $ENV{ADE_VIEW_ROOT};
$twork = $ENV{T_WORK};

$trace = 0;
my $gencsfb = 0;
my $size = 1000;
my $rate = 1;
my $accDelay = 2;

my $showcp = 0;
my @javaargs = ();
for ($pos = 0; $pos <= $#ARGV; $pos++) {
  my $arg = $ARGV[$pos];
  if ($arg eq "-v") {
    $trace = 1;
  } elsif ($arg eq "-showcp") {
    $showcp = 1;
  } elsif ($arg eq "-m") {
      $pos++;
      $memory = $ARGV[$pos];
  } elsif ($arg eq "-gencsfb") {
    $gencsfb = 1;
  } elsif ($arg eq "-size") {
      $pos++;
      $size = $ARGV[$pos];
  } elsif ($arg eq "-rate") {
      $pos++;
      $rate = $ARGV[$pos];
  } elsif ($arg eq "-delay") {
      $pos++;
      $accDelay = $ARGV[$pos];
  } else {
    push(@javaargs, $arg);
  }
}

if ($showcp) {
   print $classpath;
}

if ($gencsfb) {
    push(@javaargs, "CSFBGenTest");
    push(@javaargs, $size);
    push(@javaargs, $rate);
    push(@javaargs, $accDelay);
    push(@javaargs, $ceptest_data_folder."/inpTIDataSize".$size."Rate".$rate.".txt");
    push(@javaargs, $ceptest_data_folder."/inpTUDataSize".$size."Rate".$rate.".txt");
    push(@javaargs, $ceptest_data_folder."/inpTMDataSize".$size."Rate".$rate.".txt");
    push(@javaargs, $ceptest_log_folder."/outcsfbPR1Size".$size."Rate".$rate.".txt");
    push(@javaargs, $ceptest_log_folder."/outcsfbPR2Size".$size."Rate".$rate.".txt");
    push(@javaargs, $ceptest_log_folder."/outcsfbPR3Size".$size."Rate".$rate.".txt");
    push(@javaargs, $ceptest_log_folder."/outcsfbPR4Size".$size."Rate".$rate.".txt");
}
###########################################################
# Run target
###########################################################
$ENV{CLASSPATH} = $classpath;
my $mem = "";
if ($memory != 0) {
  $mem = "-Xms".$memory."m -Xmx".$memory."m";
}

my $JAVASTR = "java $mem -ea";
foreach (@javaargs) {
  $JAVASTR .= " \"" . $_ . "\"";
}
my $log_file = $log_folder . $separator . @javaargs[0] . ".log";
open (LOG, ">$log_file");
TRACE("invoking $JAVASTR");
print("invoking $JAVASTR\n");
print("log file : $log_file\n");
my $starttime = time();
open (JAVA, "$JAVASTR  2>&1 |");
while ($line = <JAVA>) {
  chomp($line);
  push(@java_outputs, $line);
  TRACE(">".$line);
  print LOG "$line\n";
  if (($line =~ /Exception\:/)  ||
      ($line =~ /AssertionError/)) {
     push(@exceptions, $line);
     print("$line\n");
  }
  if (($line =~ /^out of memory error/)) {
     push(@exceptions, $line);
     print("$line\n");
  }
}
close JAVA;
close LOG;
my $endtime = time();
my $diff = $endtime - $starttime;
my $mDiff = int($diff / 60);
my $sDiff = sprintf("%02d", $diff - 60 * $mDiff);
print "Took $mDiff\:$sDiff\n";



