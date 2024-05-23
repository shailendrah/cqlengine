#!/usr/local/bin/perl
# 
# $Header: setup_log.pl 17-mar-2007.16:41:12 hopark Exp $
#
# setup_log.pl
# 
# Copyright (c) 2006, 2007, Oracle. All rights reserved.  
#
#    NAME
#      setup_log.pl
#
#    DESCRIPTION
#      turn on/off log
#      usage: setup_log filename keyword on/off
#      This scripts comments out or removes comment inside the keyword block.
#      e.g.)
#      //BEGIN REFCOUNT_DEBUG
#      logger.finest("storageId="+element);
#      //END REFCOUNT_DEBUG
#      logger.finest("id="+id); //REFCOUNT_DEBUG
#      The line with logger is either commented out or removed comments.
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    hopark      11/10/06 - Creation

use strict;    # Keeps silly typos from going unnoticed!
use English;   # Sets $OSNAME, $OS_ERROR, $PERL_VERSION; see "perldoc perlvar"
use File::Copy;

sub logOnOff {
  my $filename = shift(@_);
  my $keyword = shift(@_);
  my $on = shift(@_);

#  my $temp_file = $filename . ".t";
#  copy($filename, $temp_file) or die "$filename cannot be copied.";
#  my $filename = $temp_file;
  
  my $temp_file = $filename . ".tmp";
  copy($filename, $temp_file) or die "$filename cannot be copied.";
  
  if ($on) {
    print "on";
  } else {
    print "off";
  }
  print " - $filename\n";
  open(NEWFILE, ">$filename");
  open(TEMPFILE, $temp_file);
  my $state = 0;
  while(<TEMPFILE>)
  {
      my($line) = $_;
      my $doreplace = 0;
      my $sline = 0;
      if ($line =~ "BEGIN $keyword") {
        $state = 1;
      } elsif ($line =~ "END $keyword") {
          $state = 0;
      } elsif ($line =~ "$keyword") {
        $sline = 1;
        $doreplace = 1;
        $state = 1;
      } else {
        $doreplace = 1;
      }
      if ($doreplace) {
        if ($state) {
          if ($on) {
            # check if the line is not commented out
            $line =~ s/^\/\///g;
          } else {
            # check if the line is commented out
            if (!($line =~ "^//")) {
              $line = "//" . $line;
            }
          }           
        }
        if ($sline) {
          $state = 0;
        }
      }
      print NEWFILE "$line";
  }
  close(TEMPFILE);
  close(NEWFILE);
  
  unlink($temp_file)  or die "$temp_file cannot be deleted.";
}

my $srcfile = $ARGV[0];
my $key = $ARGV[1];
my $flag = $ARGV[2];

my $line = "";
if ($srcfile == "-q") {
  while (defined($line = <STDIN>)) {
    chomp($line);
    logOnOff($line, $key, $flag);
  }
} else {
  logOnOff($srcfile, $key, $flag);
}

