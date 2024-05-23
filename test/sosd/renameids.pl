#!/usr/local/bin/perl
# 
# $Header: renameids.pl 13-may-2008.08:58:51 hopark Exp $
#
# renameids.pl
# 
# Copyright (c) 2008, Oracle. All rights reserved.  
#
#    NAME
#      renameids.pl - <one-line expansion of the name>
#
#    DESCRIPTION
#      Rename ids in a cqlx file
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
my $prefix = "";
my $stopline = -1;
my $interestedid = "";

my @ids = ();

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

sub wordBegin
{
  my $b = shift;
  if ($b =~ /^\w$/) {
    return 0;
  }
  return 1;
}

sub wordEnd
{
  my $e = shift;
  if ($e =~ /^\w$/) {
    return 0;
  }
  return 1;
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
          push(@ids, $2);
        } elsif ($line =~ /(register|create)\s+relation\s+(\w+)\s*?\(/) {
          TRACE("relation $2"); 
          push(@ids, $2);
        } elsif ($line =~ /create\s+function\s+(\w+)\s*?\((.*?)\)/) {
          my $name = $1;
          TRACE("function $name"); 
          push(@ids, $name);
        } elsif ($line =~ /(register|create)\s+view\s+(\w+)\s*?\(/) {
          TRACE("view $2"); 
          push(@ids, $2);
        } elsif ($line =~ /(register|create)\s+view\s+(\w+)\s+as/) {
          TRACE("view $2"); 
          push(@ids, $2);
        } elsif ($line =~ /create\s+window\s+(\w+)\s*?\(/) {
          TRACE("window $1"); 
          push(@ids, $1);
        } elsif ($line =~ /create\s+query\s+(\w+)\s+as/) {
          TRACE("query $1"); 
          push(@ids, $1);
        }
      }
  }
  close LOGF;
  open(OUTF, ">$outfile") or die "Failed to open $outfile: $!";
  open (LOGF, "<", $inputfile) or die "$inputfile - $!";

  my $lineno = 0;
  while (<LOGF>) {
      $lineno++;
      if ($stopline >= 0 && $lineno > $stopline) { die; }
      my $line = $_;
      chomp($line);
      TRACE("$lineno ========= $line");
      my $cnt = @ids;
      for (my $i = 0; $i <$cnt;$i++) {
        my $id = @ids[$i];
        my $done = 0;
        my $pos = 0;
        while (!$done) {
          $pos = index($line, $id, $pos);
          if ($pos >= 1) {
            if ($interestedid eq "" || $interestedid eq $id)
            {
              TRACE("$i : $id");
            }
            my $idlen = length($id);
            my $b = substr($line, $pos-1, 1);
            my $e = substr($line, $pos + $idlen, 1);
            my $wb = wordBegin($b);
            my $we = wordEnd($e);
            my $t = substr($line, $pos - 1, $pos + $idlen - $pos + 2);
            if (($interestedid eq "") || ($interestedid eq $id))
            {
              TRACE("'$t' b='$b' e='$e'");
            }
            if ($wb && $we) {
                $line = substr($line, 0, $pos) .
                        $prefix . $id .
                        substr($line, $pos + $idlen);
                if (($interestedid eq "") || ($interestedid eq $id))
                {
                  TRACE(": $line" );  
                }
                $pos += ($idlen + length($prefix));      
                if (($interestedid eq "") || ($interestedid eq $id))
                {
                  TRACE(substr($line, $pos));
                }
            } else {
                $pos += $idlen;      
            }
          } else {
            $done = 1;
          }
        }
      }
      print OUTF $line . "\n";
  }
  close LOGF;
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
  } elsif ($arg eq "-stop") {
    $i++;
    $stopline = $ARGV[$i];
    $trace_on = 1;
  } elsif ($arg eq "-id") {
    $i++;
    $interestedid = $ARGV[$i];
    $trace_on = 1;
  } elsif ($arg eq "-p") {
    $i++;
    $prefix = $ARGV[$i];
  } else {
    $inputfile = $arg;
  }
}    

my ($name,$dir,$ext) = fileparse($inputfile,'\..*');
if ($inputfile eq "") {
  die "inputfile is not given";
}
if ($outfile eq "") {
  die "output folder is not given";
} else {
  $outfile .= "/". $name . $ext;
}
if ($prefix eq "") {
  $prefix = $name . "_";
}

process($inputfile, $outfile);

