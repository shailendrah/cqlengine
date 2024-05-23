#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/test/sosd/logfilter.pl /main/6 2010/10/05 12:03:21 hopark Exp $
#
# logfilter.pl
# 
# Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      logfilter.pl - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    hopark      03/15/08 - support filteroutfiles
#    hopark      01/31/08 - Creation
# 
use strict;
use warnings;
use File::Basename;
use File::Path;

my $verbose = 0;
my $trace_on = 0;
my $preserve = 0;
my $sort_trace = 0;
my %tstable = ();
my $tspos = 0;

my @filters = ();
my @filelist = ();
my @trcfiles = ();

my $log_summary = 0;
my %leveltable = ();
my %targettable = ();

sub TRACE {
  my $msg = shift(@_);
  if ($trace_on) {
    print("$msg\n");
  }
}

sub vprint {
  my $msg = shift(@_);
  if ($verbose) {
    print("$msg\n");
  }
}
    
sub trim($)
{
  my $string = shift;
  $string =~ s/^\s+//;
  $string =~ s/\s+$//;
  return $string;
}

sub getcsv
{
  my $line = shift;
  my @f = ();
  my $idx = index($line, ",");
  if ($idx > 0) {
    my $cmd = substr($line, 0, $idx);
    my $filter = substr($line, $idx + 1);
    $f[0] = $cmd;
    $f[1] = $filter;
  }
  return [ @f ];
}

sub process
{
  my ($logfile, $outfile) = @_;
  local(*LOGF);

  vprint "processing $logfile -> $outfile\n";
  if ($outfile ne "") {
    open(OUTF, ">>$outfile") or die "Failed to open $outfile: $!";
  }
  
  my $filterCount = @filters;
  open (LOGF, "<", $logfile) or die "$logfile - $!";
  my $l = 0;
  while (<LOGF>) {
      $l++;
      my $line = $_;
      chomp($line);
      #TRACE "$line";
      if ( ($l % 10000) == 0) {
        print "$l\n";
      }
      for (my $i=0; $i < $filterCount; $i++) {
        my $f = $filters[$i];
        my $cmd = @$f[0];
        my $filter = @$f[1];
        #TRACE "trying to match  '$cmd' : '$filter'";
        if ($line =~ /$filter/) {
          #TRACE "match  '$line' : $cmd '$filter'";
          if ($cmd eq "remove") {
            $line =~ s/$filter//g;
            #TRACE "remove $line";
          } elsif ($cmd eq "process") {
            my $newfile = $1;
            push(@trcfiles, $newfile);
            my $cnt = @trcfiles;
            TRACE "add $newfile : $cnt";
          } elsif ($cmd eq "idfy") {
            if ($line =~ /($filter)/) {
              my $ts = substr($1,0, -1);
              if (exists $tstable{$ts}) {
                $ts = $tstable{$ts};
              } else {
                $tstable{$ts} = $tspos;
                $ts = $tspos;
                $tspos++;
              }
              $line =~ s/$filter/$ts:/g;
              TRACE "idfy $line";
            }
          }
        }
      }
      print OUTF "$line\n";
  }
  close LOGF;
  if ($outfile ne "") {
    my $done = 0;
    while ($done == 0) {
       my $cnt = @trcfiles;
       if ($cnt > 0) {
         my $newfile = pop(@trcfiles);
         TRACE "process $newfile : $cnt";
         my ($oname,$odir,$oext) = fileparse($newfile,'\..*');
         print OUTF "$oname\n";
         process($newfile, "");
         $cnt = @trcfiles;
       }
       if ($cnt == 0) {
         $done = 1;
       }
    }
    close OUTF;
  }
}

sub logSummary
{
  my ($logfile, $outfile) = @_;
  local(*LOGF);

  vprint "processing $logfile -> $outfile\n";
  
  open (LOGF, "<", $logfile) or die "$logfile - $!";
  my $l = 0;
  my $intrace = 0;
  my $inevent = 0;
  my $inlevel = 0;
  my $area = "";
  my $event = "";
  my $target = "";
  my $levels = "";
  my $taglevel = 0;
  my $ftag = "";
  my @elems = ();
  my $curelem = "";
  my $actlevels = "";
  my $dumps = "";
  my $files = 0;
  while (<LOGF>) {
      $l++;
      my $line = $_;
      chomp($line);
      #TRACE "$line";
      if ( ($l % 100000) == 0) {
        print "$l\n";
      }
      if ($intrace == 0) {
        if ($line =~ /FINE: trace : area=(.*) event=(.*) target=(.*) levels=(.*)/) {
          $area = $1;
          $event = $2;
          $target = $3;
          $levels = $4;
          if ($event =~ /DumpEvent/) {
            $event = "Dump";
          }
          $curelem = {
            'area' => $area,
            'event' => $event,
            'target' => $target,
            'levels' => $levels,
          };
          push(@elems, $curelem);
          $intrace = 1;
          $actlevels = "";
          $dumps = "";
          $files = 0;
          TRACE "$area $event $target $levels\n";
        }
      } else {
        if ($inevent == 0) {
          if ($line =~ /<Event.*TargetId="([0-9]*)"/) {
            $$curelem{'targetId'} = $1;
            $inevent = 1;
            $inlevel = 0;
            TRACE "+inevent :$line\n";
          }
        } else {
          if ($line =~ /<\/Event>/) {
            $inevent = 0;
            $inlevel = 0;
            TRACE "-inevent :$line\n";
            $intrace = 0;
            $$curelem{'actlevels'} = $actlevels;
            $$curelem{'dumps'} = $dumps;
            $$curelem{'files'} = $files;
          } else {
            if ($inlevel == 0) {
              if ($line =~ /<Level.* Value="(.*)"/) {
                if ($actlevels ne "") {
                  $actlevels .= ",";
                }
                $actlevels .= $1;
                $inlevel = 1;
                TRACE "+inlevel :$1";
                $taglevel = 0;
                $ftag = "";
              }
            } else {
              if ($line =~ /<\/Level>/) {
                $inlevel = 0;
                #TRACE "-inlevel :$line\n";
              } else {
                if ($line =~ /\/cep\/diag\/(.*)\.xml/ ) {
                  $files++;
                }
                if ($line =~ /<([\/A-Za-z0-9]*)>/) {
                  my $t = $1;
                  my $v = substr($t,0,1);
                  #TRACE "$t\n";
                  if ($v eq "/") {
                    my $vtag = substr($t,1);
                    if ($vtag eq $ftag) {
                      $ftag = "";
                    }
                  } else { 
                    if ($ftag eq "")  {
                      TRACE "### $t\n";
                      if ($dumps ne "") {
                        $dumps .= ",";
                      }
                      $dumps .= $t;
                      $ftag = $t;
                    }
                  }
                }
              }
            }
          }
        }
      }
#      "$line\n";
  }
  close LOGF;
  print "\n";
  my @sorted;
  if ($sort_trace)
  {
    @sorted = sort 
    {
      my $r =  ($$a{'area'} cmp $$b{'area'});
      if ($r != 0)  {
        return $r
      }
      $r =  ($$a{'event'} cmp $$b{'event'});
      if ($r != 0)  {
        return $r
      }
      $r =  ($$a{'target'} cmp $$b{'target'});
      if ($r != 0)  {
        return $r
      }
      $r =  ($$a{'targetId'} cmp $$b{'targetId'});
      if ($r != 0)  {
        return $r
      }
      $r =  ($$a{'levels'} cmp $$b{'levels'});
      if ($r != 0)  {
        return $r
      }
      $r =  ($$a{'actlevels'} cmp $$b{'actlevels'});
      if ($r != 0)  {
        return $r
      }
      return ($$a{'dumps'} cmp $$b{'dumps'});
    } @elems;
  } else {
    @sorted = @elems;
  }
    
  if ($outfile ne "") {
    open(OUTF, ">>$outfile") or die "Failed to open $outfile: $!";
  }
  
  my $elemcnt = @sorted;
  for (my $i = 0; $i < $elemcnt; $i++) {
    my $elem = $sorted[$i];
    print OUTF  "area=" . $$elem{'area'} .
          " event=" . $$elem{'event'} .
          " target=" . $$elem{'target'} .
          " targetId=" . $$elem{'targetId'} .
          " levels=" . $$elem{'levels'} .
          " actlevels=" . $$elem{'actlevels'} .
          " dumps=" . $$elem{'dumps'} .
          " files=" . $$elem{'files'} .
          "\n";
  }
  close OUTF;
  
}

my $argc = @ARGV;
my $outfile = "";
my $filtercmdfile = "";
my $logfile = "";

my $pos = 0;
for (my $i = 0; $i < $argc; $i++) {
  my $arg = $ARGV[$i];
  if ($arg eq "-v") {
    $verbose = 1;
  } elsif ($arg eq "-d") {
    $trace_on = 1;
  } elsif ($arg eq "-p") {
    $preserve = 1;
  } elsif ($arg eq "-summary") {
    $log_summary = 1;
  } elsif ($arg eq "-sort") {
    $sort_trace = 1;
  } elsif ($arg eq "-o") {
    $i++;
    $outfile = $ARGV[$i];
  } elsif ($arg eq "-f") {
    $i++;
    $filtercmdfile = $ARGV[$i];
  } else {
    $logfile = $arg;
  }
}    

if ($log_summary == 0) {
  if ($filtercmdfile eq "") {
    die "filter list is not given";
  }
}
if ($outfile eq "") {
  die "outfile is not given";
}
if ($logfile eq "") {
  die "logfile is not given";
}

# Read filter lines
if ($filtercmdfile ne "") {
  vprint "reading file : $filtercmdfile\n";
  open (F, "<", $filtercmdfile) or die "Failed to open $filtercmdfile - $!";
  my $l = 0;
  while (<F>) {
      $l++;
      my $line = $_;
      chomp($line);
      $line = trim($line);
      if ( ($line =~ /^#/ ) || ($line eq "")) {
         # ignore comment
      } else {
        my $csv = getcsv($line);
        my $csvlen = @$csv;
        if ($csvlen == 2) {
          my $cmd = @$csv[0];
          if ($cmd eq "apply") {
              push(@filelist, @$csv[1]);
          } else {
            if ($cmd ne "remove" || $preserve == 0) {
              push(@filters, $csv);
            }
          }
        } else {
          print "invalid line : $line\n";
        }
      }
  }
  close F;
}

my $fileslen = @filelist;
if ($fileslen > 0) {
  my $found = 0;
  my ($name, $dir, $ext) = fileparse($logfile, '\.txt');
  for (my $i=0; $i < $fileslen; $i++) {
    my $f = $filelist[$i];
    if ($name eq $f) {
      $found = 1;
      if ($verbose) {
        print "$f is in the list, apply filter\n";
      }
    }
  }
  if ($found == 0) {
    print "0\n";
    exit 0;
  }
}
if ($verbose) {
  my $filterCount = @filters;
  for (my $i=0; $i < $filterCount; $i++) {
    my $f = $filters[$i];
    my $cmd = @$f[0];
    my $filter = @$f[1];
    print "'$cmd' : '$filter'\n";
  }
}

my @lfiles = <$logfile.*lck>;
foreach my $lf (@lfiles) {
  vprint "unlink $lf\n";
  unlink $lf;
}
if ($outfile ne "") {
  vprint "unlink $outfile\n";
  unlink $outfile;
}

if ($log_summary) {  
  my @files = <$logfile*>;
  foreach my $f (@files) {
    logSummary($f, $outfile);
  }
} else {
  my @files = <$logfile*>;
  foreach my $f (@files) {
    process($f, $outfile);
  }
}
print "1\n";
exit 1;

