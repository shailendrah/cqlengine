#!/usr/local/bin/perl
# 
# $Header: tkcqlxdriver.pl 21-nov-2006.16:24:40 hopark   Exp $
#
# tkcqlxdriver.pl
# 
# Copyright (c) 2006, Oracle.  All rights reserved.  
#
#    NAME
#      tkcqlxdriver.pl - cqlx driver
#
#    DESCRIPTION
#      tkcqlxdriver gets className and cqlx file as the input and
#      invokes CmdActivate to run the cqlx.
#      It automatically pickes up input and output files from cqlx
#      and handles it.
#      usage : tkcqlxdriver cqlx_file [config_file] [-v]
#              tkcqlxdriver directoryname : run all cqlx under the directory
# 
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    hopark      11/21/06 - Creation
# 
use File::Copy;
use File::Basename;

sub TRACE {
  if ($debug) {
    print @_;
    print "\n";
  }
}

my $tmplFilename =  "";
my $genFilename =  "";
$debug = 0;
my $fpos = 0;
my $tagmethod = "";
for ($pos = 0; $pos <= $#ARGV; $pos++) {
  my $arg = $ARGV[$pos];
  if ($arg eq "-debug") {
    $debug = 1;
  } elsif ($arg eq "-tag") {
      $pos++;
      $arg = $ARGV[$pos];
    $tagmethod = $arg;
  } else {
    if ($fpos == 0) {
      $tmplFilename = $arg;
    } else {
      $genFilename = $arg;
    }
    $fpos++;
  }
}

TRACE("tag $tagmethod");

open (FP, $tmplFilename);
my $state = 0;
my $method = "";
my $name = "";
my %table = ();
my @lines = ();
my $lstate = 0;
while ($line = <FP>) {
  #TRACE(">".$line);
  if ($line =~ /void (createMethod_[0-9]*)/) {
      $method = $1;
      $state = 1;
      if ($method eq $tagmethod) {
        TRACE("found $tagmethod");
        $lstate = 1;
      }
  } else{
     if ($line =~ /static void main/) {
        TRACE("found main : $line");
       $lstate = 0;
     }
     if ($state) {
        if ($line =~ /MethodGen.*\"(.*)\"/) {
           $name = $1;
           TRACE("'$method' - '$name'");
           $table{ $name } = $method; 
           $state = 0;
        }
     }
  }
  if ($lstate) {
     TRACE("keep $line");
     push(@lines, $line);
  }
}
close FP;

if ($debug) {
while ( my ($key, $value) = each(%table) ) {
        print "$key => $value\n";
}
}
    
$state = 0;
open (FP1, $genFilename);
my $done = 0;
while (($line = <FP1>) && ($done==0)) {
  if ($line =~ / @([A-Za-z0-9_]*)@ /) {
    $name = $1;
    $method = $table{ $name };
    if ($method eq "") {
      $method = "UNKNOWN";
    }
    $line =~ s/createMethod_[0-9]*/$method/g;
    if ($debug) {
      print("$line");
    }
  }
  if ($debug == 0) {
    print("$line");
  }
  if ( $line =~ /XXXXXX/ ) {
    $done = 1;
  }
}

foreach $line (@lines) {
    print("$line");
}
print "}\n";
close FP1;

