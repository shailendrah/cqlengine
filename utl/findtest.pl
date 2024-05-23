#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/utl/findtest.pl /main/2 2009/07/22 08:50:49 sbishnoi Exp $
#
# findtest.pl
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      findtest.pl
#
#    DESCRIPTION
#      find a test from a dif file
#      usage: findtest diffile
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    07/20/09 - incorporate cep directory changes
#    hopark      01/03/07 - Creation

use strict;    # Keeps silly typos from going unnoticed!
use English;   # Sets $OSNAME, $OS_ERROR, $PERL_VERSION; see "perldoc perlvar"
use File::Basename;

my $trace_on = 0;

sub TRACE {
  my $msg = shift(@_);
  if ($trace_on) {
    print("$msg\n");
  }
}

sub printdivider {
    print("=============================================\n");
}

sub getTestcase {
  my $difname = shift(@_);
  my $ts = "";
  my @tc = {};
  @tc[0] = "";
  @tc[1] = $difname;
  if ($difname =~ /(.+)_(.+)$/) {
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
  my $result = "cannot find a cqlx";
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
    
    $result = $cqlx . "\n" . $query;
    close PWV;
  }
  return $result;
}

my $argc = @ARGV;

my $cqlxfolder = $ENV{ADE_VIEW_ROOT} . "/cep/wlevs_cql/modules/cqlengine/test/sql";
my $diffile = $ARGV[0];
my @tc = getTestcase($diffile);
my $suite = @tc[0];
my $name = @tc[1];
my $res = getCqlx($cqlxfolder, $name);    
print("$res\n");

