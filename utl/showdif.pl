#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/utl/showdif.pl /main/7 2009/07/22 08:50:49 sbishnoi Exp $
#
# setup_log.pl
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      showdif.pl
#
#    DESCRIPTION
#      show new dif/suc
#      usage: showdif [reffolder] [current_view] [-s] [-noref]
#             -s summary only
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    07/20/09 - incorporating cep directory changes
#    hopark      01/03/07 - Creation

use strict;    # Keeps silly typos from going unnoticed!
use English;   # Sets $OSNAME, $OS_ERROR, $PERL_VERSION; see "perldoc perlvar"
use File::Basename;

my $trace_on = 0;
my %tests = ();      # lookup table to tests

sub TRACE {
  my $msg = shift(@_);
  if ($trace_on) {
    print("$msg\n");
  }
}

sub getfiles {
  my $dirname = shift(@_);
  my $type = shift(@_);
  my $spec = $dirname . "/*." . $type;
  my @files_found = glob($spec);
  my $count = @files_found;
  my @files = {};
  my $suffixes = "\.$type";
  for (my $i=0 ; $i < $count; $i++) {
    my $f = @files_found[$i];
    my($filename, $directories, $suffix) = fileparse(@files_found[$i], $suffixes);
    @files[$i] = $filename;
  }
  return @files;
}

sub getCurView {
  my $cview = "";
  print("getting current view\n");
  open (PWV, "ade pwv |");
  while (<PWV>){
    my $line = $_;
    if ($line =~ /^VIEW_LOCATION : (.+)$/) {
      $cview = $1;
    }
  }
  close PWV;
  return $cview;
}

sub compareArray {
    my ($x, $y) = @_;   # copy in the array references
    my @result;

    my %seen = ();      # lookup table to test membership
    # build lookup table
    for (my $i=0; $i < @$y; $i++) {
      my $item = $y->[$i];
      my @tc = getTestcase($item);
      my $suite = @tc[0];
      my $name = @tc[1];
      $seen{$name} = 1;
    }
    
    for (my $i=0; $i < @$x; $i++) {
        my $item = $x->[$i];
        my @tc = getTestcase($item);
        my $suite = @tc[0];
        my $name = @tc[1];
        unless ($seen{$name}) {
            push(@result, $item);
        }
    }
    return @result;
}

sub printdivider {
    print("=============================================\n");
}

sub printarray {
    my ($x) = @_;   # copy in the array references
    my $j = 0;
    my $item;
    for (my $i=0; $i < @$x; $i++) {
        my $item = $x->[$i];
        print("$item ");
        if ($j > 3) {
          print("\n");
          $j = 0;
        }
    }
    print("\n\n");
}

sub makeFileList {
  my $cqlx = shift(@_);
  my $suite = shift(@_);
  
  if (exists $tests{$cqlx}) {
    my $curs = $tests{$cqlx};
    my @suites = split(/,/, $curs );
    my $has = 0;
    my $suitecnts = @suites;
    for (my $x=0; $x < $suitecnts; $x++) {
      my $s = @suites[$x];
      if ($s eq $suite) {
        $has = 1;
      }
    }
    if ($has == 0) {
      $tests{$cqlx} = $curs . "," .$suite;
    }
  } else {
    $tests{$cqlx} = $suite;
  }
}

sub printDiff {
  my ($x) = @_;   # copy in the array references
  my @suites = ();
  my %seen = ();     
  for (my $i=0; $i < @$x; $i++) {
    my $dif = $x->[$i];
    my @tc = getTestcase($dif);
    my $ts = @tc[0];
    my $name = @tc[1];
    unless ($seen{$ts}) {
       $seen{$ts} = 1;
       push(@suites, $ts);
    }
  }
  my %results = ();     
  foreach (@suites) {
    my $ts = $_;
    print("-------- $ts ----------\n");
    for (my $i=0; $i < @$x; $i++) {
      my $dif = $x->[$i];
      my @tc = getTestcase($dif);
      my $suite = @tc[0];
      my $name = @tc[1];
      if ($ts eq $suite) {
        my @res = @results{$name};
        if (@res[0] eq "") {
          @res = getCqlx($::cqlxfolder, $name, $suite);    
          @results{$name} = @res;
        }
        makeFileList(@res[0], $suite);
        print("$name : ". @res[0] . " , " . @res[1] ."\n");
      }
    } 
  }
}

sub getTestcase {
  my $difname = shift(@_);
  my $ts = "";
  my @tc = {};
  @tc[0] = "";
  @tc[1] = $difname;
  if ($difname =~ /(.+?)_(.+)$/) {
    $ts = $2;
    @tc[0] = $2;
    @tc[1] = $1;
  }
  if (@tc[0] eq "") {
      @tc[0] = "default";
  }
  return @tc
}

sub getCqlx {
  my $dirname = shift(@_);
  my $difname = shift(@_);
  my $suite = shift(@_);

  my @cqldesc = {};
  @cqldesc[0] = "";
  @cqldesc[1] = "";
  TRACE("cd $dirname");
  chdir($dirname) or die("failed to cd($dirname)");
  TRACE("grep $difname *.cqlx");
  open (PWV, "grep $difname *.cqlx |");
  my $cqlx = "";
  my $queryid = "";
  while (<PWV>){
    my $line = $_;
    TRACE($line);
    if ($line =~ /^(.+):.*alter[\s]+query[\s]+([\S]+) /) {
      $cqlx = $1;
      $queryid = $2;
    }
  }
  close PWV;
  if ($cqlx ne "" && $queryid ne "") {
    my $query = $queryid;
    TRACE("grep $queryid $cqlx");
    open (PWV, "grep $queryid $cqlx |");
    while (<PWV>){
      my $line = $_;
      TRACE($line);
      if ($line !~ /alter[\s]+query/) {
        if ($line =~ /<OEPS_DDL>[\s]*(.+)[\s]*<\/OEPS_DDL>/) {
          $query = $1;
        } else {
          $query = $line;
        }
      }
    }
    chomp($query);
    @cqldesc[0] = $cqlx;
    @cqldesc[1] = $query;
    close PWV;
  }
  return @cqldesc;
}

my $argc = @ARGV;
$::verbose = 0;
$::cur_view = "";
$::cur_output = "";
$::ref_output = "";
$::cqlxfolder = "";

for (my $i = 0; $i < $argc; $i++) {
  my $arg = $ARGV[$i];
  if ($arg eq "-s") {
    $::verbose = 0;
  } elsif ($arg eq "-noref") {
    $::ref_output = "";
  } elsif ($arg eq "-cql") {
    $i++;
    $::cqlxfolder = $ARGV[$i];
  } elsif ($arg eq "-ref") {
    $i++;
    $::ref_output = $ARGV[$i];
  } elsif ($arg eq "-d") {
    $trace_on = 1;
  } else {
     $::cur_output = $arg;
  }
}
  
if ($::cur_output eq "") {
  $::cur_view = getCurView();
  $::cur_output = $::cur_view . "/oracle/work/cep";
  $::cqlxfolder = $::cur_view . "/cep/wlevs_cql/modules/cqlengine/test/sql";
}
print ("current : $::cur_output\n");
print ("cqlx : $::cqlxfolder\n");
if ($::ref_output ne "") {
  print ("reference    : $::ref_output\n");
}

my @ref_difs = ();
my @ref_sucs = ();
if ($::ref_output ne "") {
  @ref_difs = getfiles($::ref_output, "dif");
  @ref_sucs = getfiles($::ref_output, "suc");
}
my @difs = getfiles($::cur_output, "dif");
my @sucs = getfiles($::cur_output, "suc");
printdivider;
my $count = @difs;
print("diffs : $count\n");
my $count = @sucs;
print("sucs : $count\n");
if ($::ref_output ne "") {
  my $rcount = @ref_difs;
  print("ref-diffs : $rcount\n");
  my $rcount = @ref_sucs;
  print("ref-sucs : $rcount\n");
}

# Get new succs
if ($::ref_output ne "") {
  my @newsucs = compareArray(\@sucs, \@ref_sucs);
  $count = @newsucs;
  printdivider;
  print("new sucs : $count\n");
  if ($count > 0)  {
    printarray (\@newsucs);
  }
}

# Get new diffs
my $item;
my @newdifs = compareArray(\@difs, \@ref_difs);
my $count = @newdifs;
printdivider;
print("new diffs : $count\n");
if ($count > 0)  {
  printDiff(\@newdifs);
}

if ($::ref_output ne "") {
  # Get less sucs
  my $item;
  my @lsucs = compareArray(\@ref_sucs, \@sucs);
  my @lessSucs = compareArray(\@lsucs, \@newdifs);
  my $count = @lessSucs;
  printdivider;
  print("less sucs : $count\n");
  if ($::verbose) {
    printDiff(\@lessSucs);
  }
}

my $failedtests = keys(%tests);
if ($failedtests > 0) {
  printdivider;
  print("failed tests\n");
  while ( my ($key, $value) = each(%tests) ) {
    my $r = sprintf("%-40s%s", $key, $value);
    print "$r\n";
  }  
}

