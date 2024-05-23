#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/test/sosd/preproccql.pl /main/2 2009/07/11 18:44:41 skmishra Exp $
#
# genoutputs.pl
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      preprocess.pl 
#
#    DESCRIPTION
#      preprocess.pl generats empty output files from a cqlx file.
#      It is used to detect a crashing test cases and generates diffs for
#      expected outputs.
#      usage : [preprocess -file cqlx_file] [-list cqlx_list] k1 v1 k2 v2...
# 
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    hopark      11/21/06 - Creation
# 
use File::Copy;
use File::Basename;

###############################################################################

$trace = 0;
$generate_empty_outputs = 0;

%filters = ();

# System dependent setup
$NT = 0;
if($^O =~ /MSWin32/i){
  $NT = 1;
}
$separator = $NT ? "\\" : "\/";

sub TRACE {
  if ($trace) {
    print @_;
    print "\n";
  }
}

sub applyfilter {
  my $line = shift;
  while ( my ($key, $value) = each(%filters) ) {
    if ($line =~ /\@$key\@/) {
      $line =~ s/\@$key\@/$value/g;
      break;
    }
  }
  return $line;
}

sub processCqlx {
  my $src_cqlx_file = shift;
  my $dest_path = shift;
  my $output_path = shift;

  my ($name,$dir,$ext) = fileparse($src_cqlx_file,'\..*');
  my $dest_cqlx_file = $dest_path . $separator . $name . $ext;;
  TRACE "process  $src_cqlx_file";
  TRACE "dest $dest_cqlx_file";


  open(SRCCQLX, $src_cqlx_file) or die "Failed to open $src_cqlx_file: $!";
  open(DESTCQLX, ">$dest_cqlx_file") or die "Failed to create $dest_cqlx_file: $!";
  while(<SRCCQLX>) {
    my $line = $_;
    if ($line =~ /\@/) {
      $line = applyfilter($line);
      if ($generate_empty_outputs) {
        my $pat = "oracle\/work\/cep\/log\/([^<]*)<";
        if ($line =~ /$pat/) {
           my $outfile = $1;
           my $idx = index($outfile, "?");
           if ($idx > 0) {
             $outfile = substr($outfile, 0, $idx);
           }
           my $dest_file = $output_path . $separator . $outfile;
           TRACE "generating empty $dest_file\n";
           open(OUTFILE, ">$dest_file") or die "Failed to create $dest_file: $!";
           close(OUTFILE);
        }
      }
    }
    print DESTCQLX "$line";
  }
  close(DESTCQLX);
  close(SRCCQLX);
}
 
my $cqlxlist = "";
my $cqlxfile = "";
my $apos = 0;
my $key = "";

my $ade_view_root = $ENV{ADE_VIEW_ROOT};
my $twork = "$ENV{GIT_REPO_ROOT}/out". $separator . "oracle" . $separator . "work";
my $twork_cep = $twork . $separator . "cep";
my $test_root = $ENV{GIT_REPO_ROOT}. $separator . "source"  . $separator . "modules" . $separator . "spark-cql" . $separator .  "cqlengine" . $separator . "test";
my $test_sql = $test_root . $separator . "sql";
my $test_data = $test_root . $separator . "data";
my $test_log = $test_root . $separator . "log";
my $test_output = $twork_cep . $separator . "log";
$filters{"ADE_VIEW_ROOT"} = $ade_view_root;
$filters{"T_WORK"} = $twork;
$filters{"TEST_DATA"} = $test_data;
$filters{"TEST_OUTPUT"} = $test_output;
$filters{"CQL_FOLDER"} = $test_sql;

for ($pos = 0; $pos <= $#ARGV; $pos++) {
  my $arg = $ARGV[$pos];
  if ($arg eq "-list") {
      $pos++;
      $cqlxlist = $ARGV[$pos];
  } elsif ($arg eq "-file") {
      $pos++;
      $cqlxfile = $ARGV[$pos];
  } else {
    if ($key eq "") {
      $key = $arg;
    } else {
      if ($key ne "") {
        if ($arg =~ /\@/) {
          $arg = applyfilter($arg);
        }
        $filters{$key} = $arg;
        $key = "";
      }
    }
  }
}

if (0) {
while ( my ($key, $value) = each(%filters) ) {
  TRACE "$key = $value";
}
}

my @cqlxfiles = ();
if ($cqlxlist ne "" && $cqlxlist ne "null") {
  open(LISTFILES, $cqlxlist) or die "Failed to open $cqlxlist: $!";
  while(<LISTFILES>) {
    my($cqlx_file) = $_;
    chomp($cqlx_file);
    my ($name,$dir,$ext) = fileparse($cqlx_file,'\..*');
    $cqlx_file = $test_sql . $separator . $name . $ext;;
    push(@cqlxfiles, $cqlx_file);
  }
  close(LISTFILES);
} else {
  push(@cqlxfiles, $cqlxfile);
}
foreach $cqlx (@cqlxfiles) 
{
  processCqlx($cqlx, $twork_cep, $test_output);
}

