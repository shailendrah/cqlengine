#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/utl/copylib.pl /main/2 2009/07/22 08:50:49 sbishnoi Exp $
#
# runcep
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      copylib - copy libs in class path
#
#    DESCRIPTION
#      usage : copylib dest
# 
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    07/20/09 - icorporate cep directory change
#    hopark      11/21/06 - Creation
# 
use File::Copy;
use File::Basename;
use File::Path;

sub copyLib
{
  my $path = $_[0];
  my $dest = $_[1];
  #print("$path\n");
  my ($name,$dir,$ext) = fileparse($path,'\.jar');
  if ($ext ne ".jar") {
    print("$path : not a jar\n");
    return;
  }
  if (!(-e $path)) {
    print("$path : does not exist\n");
    return;
  }
  my @dircs = split(/\//, $dir);
  
  my $j = 0;
  foreach my $v (@dircs) {
#        print ($j . ":" . $v . "\n");
        $j++;
  }
  
  my $s = 0;
  my $ss = $dircs[1];
  if ($ss eq "ade") {
   $s = 3;
  }
  if ($ss eq "scratch") {
   $s = 5;
  }
  my $len = scalar @dircs;
  my $p = "/";
  my $i = 0;
  for (; $s < $len; $s++) {
    #print($dircs[$s] . " ");
    if ($i > 0) {
      $p = $p . $separator;
    }
    $p = $p . $dircs[$s];
    $i++;
  }
  my $dpath = $dest . $separator . $name . $ext;
  if ($fullpath) {
    my $dpath = $dest . $p;      
    eval { mkpath($dpath) };
    if ($@) {
        print "Couldn't create $dpath: $@";
        return;
    }
  }
  print ("copying to $dpath\n");
  copy($path, $dpath) or print "$path : copy failed : $!";
}

my $fullpath = 0;
my $dest = "";
for ($pos = 0; $pos <= $#ARGV; $pos++) {
  my $arg = $ARGV[$pos];
  if ($arg eq "-fullpath") {
    $fullpath = 1;
  } elsif ($arg eq "-dest") {
    $pos++; 
    $dest = $ARGV[$pos];
  }
}

$NT = 0;
if($^O =~ /MSWin32/i){
  $NT = 1;
}
$separator = $NT ? "\\" : "\/";

$ade_view_root = $ENV{ADE_VIEW_ROOT};
$twork = $ENV{ADE_VIEW_ROOT} . $separator . "work";
$cep_folder = $ade_view_root . $separator . "cep" . $separator . "wlevs_cql" . $separator . "modules" . $separator . "cqlengine";
if ($dest eq "") {
  $dest = $cep_folder . $separator . "dlib";
}

print("Destination - $dest\n");
mkdir($dest);

chdir($cep_folder) or die "failed to chdir $cep_folder";
open (PRINTCP, "ant print.classpath 2>&1 |");
my $state = 0;

while ($line = <PRINTCP>) {
   print $line . "\n";
  if ($line =~ /print.classpath:/) {
      $state = 1;
  } else {
     if ($state) {
        if ($line =~ /\[echo\] (.*)$/) {
           copyLib($1, $dest);
        }
     }
  }
}
close PRINTCP;

