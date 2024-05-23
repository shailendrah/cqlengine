#!/usr/local/bin/perl
# 
# $Header: pcbpel/cep/utl/cepserverchgs.pl /main/1 2009/05/19 21:52:06 hopark Exp $
#
# cepserverchgs.pl
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      cepserverchgs.pl
#
#    DESCRIPTION
#      get the list of txns in the cep.server
#      usages:
#      cepserverchgs [options] -oldlabel <label> -newjar <jarfile>
#      cepserverchgs [options] -oldload <loadno/loadfile> -newload <loadno/loadfile>
#      cepserverchgs [options] -oldlabel <label> -newlabel <label>
#      cepserverchgs [options] -oldlabel <label> -newjar <jarfile>
#       options:
#         -loadfolder <loadfoler> 
#                change load folder(default: /net/stadf71/scratch/cc/wlevs_cql_cc/repo/11g/loads)
#         -txndetails
#                display transaction details 
#         -labelonly
#                show label only 
#      
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    hopark      02/25/08 - Creation

use strict;    # Keeps silly typos from going unnoticed!
use English;   # Sets $OSNAME, $OS_ERROR, $PERL_VERSION; see "perldoc perlvar"
use File::Copy;
use File::Basename;
use File::Path;
use Cwd;
use FindBin;

my $trace_on = 0;
my $oldlabel = "";
my $newlabel = "";
my $newjar = "";
my $oldload = "";
my $newload = "";
my $loadfolder = "/net/stadf71/scratch/cc/wlevs_cql_cc/repo/11g/loads";
my $showlabelonly = 0;
my $showtxndetails = 0;

my $ade_view_root = $ENV{ADE_VIEW_ROOT};
if ($ade_view_root eq "") {
  die "You need to run this script within an ade view";
}

my $scriptfolder = $FindBin::Bin;
my $getscmf = "$scriptfolder/getscmf.sh";
if (!(-e $getscmf)) {
  my $cwd = &Cwd::cwd();
  $getscmf = "$cwd/getscmf.sh";
  if (!(-e $getscmf)) {
    die "cannot find getscmf.sh in $cwd or $scriptfolder.";
  }
}
  
my @txns = ();
my %txns_hash = ();
  
my @users = ("skmishra", "hopark", "parujain", "mthatte", "anasrini", "sbishnoi", "sborah", "udeshmuk", "alealves");

my %users_hash = ();
my $ulen = @users;
for (my $i = 0; $i < $ulen; $i++)  {
  my $u = @users[$i];
  $users_hash { $u } = 1;
}

sub TRACE {
  my $msg = shift(@_);
  if ($trace_on) {
    print("$msg\n");
  }
}

sub isCEPTransaction {
  my $txn = $_[0];
  my $i = index($txn, "_");
  my $user = substr($txn, 0, $i);
  #print "$user\n";
  return (exists $users_hash {$user });
}

sub addtxn {
  my $txn =  $_[0];
  
  TRACE "addtxn : $txn";
  if (!(exists $txns_hash {$txn} ) ) {
    $txns_hash { $txn } = 1;
    push(@txns, $txn);
  }
}

sub getTransactions {
  my $oldlabel =  $_[0];
  my $newlabel =  $_[1];
  print ("--- getting transactions between $oldlabel , $newlabel\n");
  open (LABELS, "ade difflabels -old_label $oldlabel -new_label $newlabel  2>&1 |");
  my $has_cep = 0;
  my $state = 0;
  my $line;
  while (($line = <LABELS>) && ($state <= 2)) {
    #remove newline
    chomp($line);
    #trim whitespaces
    $line =~ s/^\s+//;
    $line =~ s/\s+$//;
    TRACE "$line";
    if ($line =~ /ERROR/) {
      print $line . "\n";
    }
    if ($state == 0) {
      if ($line =~ /TRANSACTIONS/) {
        TRACE "found $line : $state";
        $state = 1;
      }
    } else {
      if ($line eq "") {
        $state++;
  	TRACE "found : $state\n";
      } else {
        if ($state == 2) {
          if (isCEPTransaction($line)) {
            TRACE "$line";
            addtxn($line);
            $has_cep = 1;
            #$state = 3;
          }
        }
      }
    }
  }
  close LABELS;
  return $has_cep;
}

sub getTxnDetails {
  my $txn =  $_[0];
  open (DESTR, "ade describetrans -short $txn  2>&1 |");
  my $line;
  my $txndetail = "";
  while ($line = <DESTR>) {
    $txndetail .= $line;
  }
  close DESTR;
  return $txndetail;
}

sub getADELabel {
  my $load =  $_[0];
  
  print "--- getting adelabel for $load\n";
  open (SCMF, "sh $getscmf $load 2>&1 |");

  my $has_cep = 0;
  my $state = 0;
  my $line;
  my $label="";
  my $lines = "";
  while (($line = <SCMF>) && ($state <= 2)) {
    $lines .= $line;
    #remove newline
    chomp($line);
    TRACE $line;
    if ($line =~ /ADE Label: (.*)/) {
      $label = $1;
      TRACE "label : $label";
    }
    if ($line =~ /Txn: (.*)/) {
      my $txn = $1;
      addtxn($txn);
      TRACE "txn : $txn";
    }
  }
  close SCMF;
  if ($label eq "") {
    print $lines;
    die "Failed to get label for $load";
  }
  print $label . "\n";
  return $label;
}

my $argc = @ARGV;
my $i = 0;
while ($i < $argc)
{
  my $arg = $ARGV[$i];
  $i++;
  if ($arg eq "-test") {
    $trace_on = 1;
  } elsif ($arg eq "-labelonly") {
    $showlabelonly = 1;
  } elsif ($arg eq "-txndetails") {
    $showtxndetails = 1;
  } elsif ($arg eq "-loadfolder") {
    $loadfolder = $ARGV[$i];
    $i++;
  } elsif ($arg eq "-oldload") {
    $oldload = $ARGV[$i];
    $i++;
  } elsif ($arg eq "-newload") {
    $newload = $ARGV[$i];
    $i++;
  } elsif ($arg eq "-newjar") {
    $newjar = $ARGV[$i];
    $i++;
  } elsif ($arg eq "-oldlabel") {
    $oldlabel = $ARGV[$i];
    $i++;
  } elsif ($arg eq "-newlabel") {
    $newlabel = $ARGV[$i];
    $i++;
  }
}

if ($oldlabel eq "") {
  if ($oldload eq "") {
    die "No oldlabel specified. Use either -oldlabel or -oldload.";
  }
  $oldlabel = getADELabel($oldload);
}
if ($newlabel eq "") {
  if ($newload ne "") {
    $newlabel = getADELabel($newload);
  } elsif ($newjar ne "") {
    $newlabel = getADELabel($newjar);
  }
  if ($newlabel eq "") {
    die "No newlabel specified. Use either -newlabel, -newjar or -newload.";
  }
}

if ($showlabelonly) {
  exit;
}

getTransactions($oldlabel, $newlabel);

my $ntxns = @txns;
for (my $j = 0; $j < $ntxns; $j++) {
  my $txn = $txns[$j];
  print $txn . "\n";
  if ($showtxndetails) {
    my $txndetail = getTxnDetails($txn);
    print $txndetail . "\n\n";
  }
}

