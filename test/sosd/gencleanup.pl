#!/usr/local/bin/perl
# 
# $Header: gencleanup.pl 13-may-2008.08:58:50 hopark Exp $
#
# gencleanup.pl
# 
# Copyright (c) 2008, Oracle. All rights reserved.  
#
#    NAME
#      gencleanup.pl - <one-line expansion of the name>
#
#    DESCRIPTION
#      Generate cleanup cqlx from a cqlx file
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    hopark      04/22/08 - Creation
# 
use File::Basename;
use File::Path;

my $verbose = 0;
my $trace_on = 0;
my $outfile = "";
my $inputfile = "";

my @drops = ();

sub TRACE {
  my $msg = shift(@_);
  if ($trace_on) {
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
  my ($inputfile, $outfile) = @_;
  local(*LOGF);

  print "processing $inputfile -> $outfile\n";
  open (LOGF, "<", $inputfile) or die "$inputfile - $!";
  my $l = 0;
  my $in_comment = 0;
  while (<LOGF>) {
      $l++;
      my $line = $_;
      chomp($line);
      if ($in_comment) {
        if ($line =~ /-->/) {
            $in_comment = 0;
        }
      } else {
        if ($line =~ /<!--/) {
          $in_comment = 1;
        } elsif ($line =~ /(register|create)\s+stream\s+(\w+)\s*?\(/) {
          TRACE("stream $2"); 
          push(@drops, $2);
          push(@drops, "stream");
        } elsif ($line =~ /(register|create)\s+relation\s+(\w+)\s*?\(/) {
          TRACE("relation $2"); 
          push(@drops, $2);
          push(@drops, "relation");
        } elsif ($line =~ /create\s+function\s+(\w+)\s*?\((.*?)\)/) {
          my $name = $1;
          if ($2 ne "") {
            TRACE($2);
            my @csv = split(/,/, $2);
            my $csvlen = @csv;
            my $types = "";
            for ($i = 0; $i < $csvlen; $i++) {
              my $l = @csv[$i];
              $l = trim($l);
              TRACE($l);
              my @col = split(/\s+/, $l);
              my $n = @col[0];
              my $t = @col[1];
              if ($i > 0) {
                $types .= ", ";
              } 
              if ($t =~ /(.*)\(/ ) {
                 $types .= $1;
              } else {
                 $types .= $t;
              }
            }
            if ($types ne "")  {
              $name .= "(" . $types . ")";
            }
          }
          TRACE("function $name"); 
          push(@drops, $name);
          push(@drops, "function");
        } elsif ($line =~ /(register|create)\s+view\s+(\w+)\s*?\(/) {
          TRACE("view $2"); 
          push(@drops, $2);
          push(@drops, "view");
        } elsif ($line =~ /(register|create)\s+view\s+(\w+)\s+as/) {
          TRACE("view $2"); 
          push(@drops, $2);
          push(@drops, "view");
        } elsif ($line =~ /create\s+query\s+(\w+)\s+as/) {
          TRACE("query $1"); 
          push(@drops, $1);
          push(@drops, "query");
        }
      }
  }
  close LOGF;
  open(OUTF, ">$outfile") or die "Failed to open $outfile: $!";
  my $cnt = @drops;
  print OUTF "<CEP>\n";
  while ($cnt > 0) {
    my $type = pop(@drops);
    my $name = pop(@drops);
    $cnt = @drops;
    print OUTF "<CEP_DDL> ";
    if ($type eq "stream") {
        print OUTF "drop stream " . $name;
    } elsif ($type eq "relation") {
        print OUTF "drop relation " . $name;
    } elsif ($type eq "function") {
        print OUTF "drop function " . $name;
    } elsif ($type eq "view") {
        print OUTF "drop view " . $name;
    } elsif ($type eq "query") {
        print OUTF "alter query " . $name . " stop";
        print OUTF " </CEP_DDL>\n<CEP_DDL> ";
        print OUTF "drop query " . $name;
    }
    print OUTF " </CEP_DDL>\n";
  }
  print OUTF "</CEP>\n";
  close OUTF;
}

my $argc = @ARGV;

for (my $i = 0; $i < $argc; $i++) {
  my $arg = $ARGV[$i];
  if ($arg eq "-v") {
    $verbose = 1;
  } elsif ($arg eq "-d") {
    $trace_on = 1;
  } elsif ($arg eq "-o") {
    $i++;
    $outfile = $ARGV[$i];
  } else {
    $inputfile = $arg;
  }
}    

if ($inputfile eq "") {
  die "inputfile is not given";
}
if ($outfile eq "") {
  my ($name,$dir,$ext) = fileparse($inputfile,'\..*');
  $outfile = $dir . $name . "-drop-" . $ext;
} else {
  my ($name,$dir,$ext) = fileparse($inputfile,'\..*');
  $outfile .= "/". $name . $ext;
}

process($inputfile, $outfile);

