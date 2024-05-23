#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/utl/setup_cepproj.pl /main/12 2009/11/09 10:10:58 sborah Exp $
#
# setup_cepproj.pl
# 
# Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      setup_cepproj.pl - setup jdeveloper and eclipse project for CEP
#
#    DESCRIPTION
#      CEP specific view setup
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    07/16/09 - fixing the path variables after directory reorg
#    hopark      05/13/09 - rename jars
#    hopark      10/15/08 - use lib folder
#    hopark      09/08/08 - load25
#    hopark      08/25/08 - load24
#    skmishra    08/27/08 - 
#    sbishnoi    08/13/08 - 
#    hopark      07/22/08 - rename cep.jar
#    hopark      07/15/08 - add evs path
#    hopark      01/29/08 - support netbeans
#    hopark      09/04/07 - ingore cep.jar
#    hopark      11/10/06 - Creation

use strict;    # Keeps silly typos from going unnoticed!
use English;   # Sets $OSNAME, $OS_ERROR, $PERL_VERSION; see "perldoc perlvar"
use File::Copy;
use File::Basename;
use File::Path;

my $PSEP=$ENV{'PSEP'};

my $trace_on = 0;
my $classpath_eclipse = 0;
my $classpath_jdev_ant = 1;
my $classpath_jdev_ref = 2;
my $classpath_jdev_def = 3;
my $classpath_nb = 4;

my @classPaths = ();
my $cp_generated = 0;

sub TRACE {
  my $msg = shift(@_);
  if ($trace_on) {
    print("$msg\n");
  }
}

sub isJar {
  my $path = $_[0];
  #print("$path\n");
  my ($name,$dir,$ext) = fileparse($path,'\.jar');
  if ($ext ne ".jar") {
    print("$path : not a jar\n");
    return 0;
  }
  my $jarname = $name . $ext;
  if ($jarname eq "com.oracle.cep.server_11.1.1.1_0.jar") {
    print("$path : ignore \n");
    return 0;
  }
  if ($jarname eq "cep-test.jar") {
    print("$path : ignore \n");
    return 0;
  }
  if (!(-e $path)) {
    print("$path : does not exist\n");
    return 0;
  }
  return 1;
}

sub genClassPaths {
  print ("getting classpaths\n");
  open (PRINTCP, "ant print.classpath 2>&1 |");
  my $state = 0;
  my $line;
  my $cps = "";
  while ($line = <PRINTCP>) {
    TRACE(">".$line);
    if ($line =~ /print.classpath:/) {
        $state = 1;
    } else {
       if ($state) {
          if ($line =~ /\[echo\] (.*)$/) {
             my $jarfile = $1;
             TRACE ($jarfile."\n");
             my $isjar = isJar($jarfile);
             my ($name,$dir,$ext) = fileparse($jarfile,'\.jar');
             $name = $name . $ext;
             if ($isjar) {
               TRACE ("+ ".$jarfile."\n");
               push(@classPaths, $jarfile);
             }
          }
       }
    }
  }

  my $beahome = $ENV{BEA_HOME};
  if (-e $beahome) {
    my $sharedlib = $beahome . "/modules";
    my $wlrtlib = $beahome . "/ocep_10.3/modules";
      
    push(@classPaths, "$sharedlib/javax.xml.bind_2.1.1.jar" );
    push(@classPaths, "$sharedlib/com.bea.core.datasource6_1.5.0.0.jar" );
    push(@classPaths, "$sharedlib/com.bea.core.servicehelper_6.0.0.0.jar" );
    push(@classPaths, "$wlrtlib/com.bea.wlevs.configuration_3.0.0.0.jar" );
    push(@classPaths, "$wlrtlib/com.bea.wlevs.ede.api_3.0.0.0.jar" );
    push(@classPaths, "$wlrtlib/com.bea.wlevs.processor_3.0.0.0.jar" );
    push(@classPaths, "$wlrtlib/com.bea.wlevs.util_3.0.0.0.jar" );
    push(@classPaths, "$wlrtlib/com.bea.wlevs.spi_3.0.0.0.jar" );
  }
    
  print ("\n");
  close PRINTCP;
}

sub getCP {
  my $FILE = $_[0];
  my $outtype = $_[1];
  my $cps = "";
  foreach my $jarfile (@classPaths) {
     my ($name,$dir,$ext) = fileparse($jarfile,'\.jar');
     $name = $name . $ext;
     if ($outtype == $classpath_eclipse) {
       print $FILE "<classpathentry kind=\"lib\" path=\"$jarfile\"/>\n";
     } elsif ($outtype == $classpath_jdev_ant) {
       print $FILE "<url path=\"$jarfile\" jar-entry=\"\"/>\n";
     } elsif ($outtype == $classpath_jdev_ref) {
       print $FILE "<hash>\n";
       print $FILE "<value n=\"id\" v=\"$name\"/>\n";
       print $FILE "<value n=\"isJDK\" v=\"false\"/>\n";
       print $FILE "</hash>\n";
     } elsif ($outtype == $classpath_jdev_def) {
       print $FILE "<hash>\n";
       print $FILE "<list n=\"classPath\">\n";
       print $FILE " <url path=\"$jarfile\" jar-entry=\"\"/>\n";
       print $FILE "</list>\n";
       print $FILE "<value n=\"deployedByDefault\" v=\"true\"/>\n";
       print $FILE "<value n=\"description\" v=\"$name\"/>\n";
       print $FILE "<value n=\"id\" v=\"$name\"/>\n";
       print $FILE "<value n=\"locked\" v=\"true\"/>\n";
       print $FILE "</hash>\n";
     } elsif ($outtype == $classpath_nb) {
       if ($cps ne "") {
         $cps .= ":";
       }
       $cps .= $jarfile;
     }
  }
  return $cps;
}

sub personalizeFile {
  my $old_file = $_[0];
  my $new_file = $_[1];

  my $ade_view_root = $ENV{ADE_VIEW_ROOT};
  my $ade_view_name = $ENV{ADE_VIEW_NAME};
  my $twork = $ENV{T_WORK};
  my $home = $ENV{HOME};
  my $bea_home = $ENV{BEA_HOME};
  my $cep_root = $ade_view_root . "/cep/wlevs_cql/modules/cqlengine";
  
  my $jmslib = $home . "/libs/jms.jar";
  
  chdir($cep_root) or die "failed to cd $cep_root";
  if (!(-e $new_file)) {
    print "creating $new_file\n";
    open(NEWFILE, ">$new_file");
    open(OLDFILE, $old_file);
    while(<OLDFILE>)
    {
      my($line) = $_;
      if ($line =~ /\@CLASS_PATHS\@/) {
        if ($cp_generated == 0) {
          genClassPaths();
          $cp_generated = 1;
        }
        getCP(*NEWFILE, $classpath_eclipse);
      } elsif ($line =~ /\@CLASS_PATHS_JDEV_ANT\@/) {
        if ($cp_generated == 0) {
          genClassPaths();
          $cp_generated = 1;
        }
        getCP(*NEWFILE, $classpath_jdev_ant);
      } elsif ($line =~ /\@CLASS_PATHS_JDEV_DEF\@/) {
        if ($cp_generated == 0) {
          genClassPaths();
          $cp_generated = 1;
        }
        getCP(*NEWFILE, $classpath_jdev_def);
      } elsif ($line =~ /\@CLASS_PATHS_JDEV_REF\@/) {
        if ($cp_generated == 0) {
          genClassPaths();
          $cp_generated = 1;
        }
        getCP(*NEWFILE, $classpath_jdev_ref);
      } elsif ($line =~ /\@CLASS_PATHS_NB\@/) {
        if ($cp_generated == 0) {
          genClassPaths();
          $cp_generated = 1;
        }
        my $cbs = getCP(*NEWFILE, $classpath_nb);
        $line =~ s/\@CLASS_PATHS_NB\@/$cbs/g;
        print NEWFILE "$line";
      } else {
        $line =~ s/\@ADE_VIEW_ROOT\@/$ade_view_root/g;
        $line =~ s/\@ADE_VIEW_NAME\@/$ade_view_name/g;
        $line =~ s/\@T_WORK@/$twork/g;
        $line =~ s/\@BEA_HOME@/$bea_home/g;
        $line =~ s/..\/..\/j2ee\/j2ee1.4\/libs\/jms.jar/$jmslib/g;
        print NEWFILE "$line";
      }
    }
    close(OLDFILE);
    close(NEWFILE);
  }
}

#############################################################################  
# Make personalized Jdeveloper/Eclipse project file
#############################################################################  
my $jdev = 0;
my $nb = 0;
for (my $pos = 0; $pos <= $#ARGV; $pos++) {
  my $arg = $ARGV[$pos];
  if ($arg eq "-jdev") {
    $jdev = 1;
  } elsif ($arg eq "-nb") {
    $nb = 1;
  }
}

my $old = "";
my $new = "";

$old = "cep.project";
$new = ".project";
personalizeFile($old, $new);
$old = "cep.classpath";
$new = ".classpath";

personalizeFile($old, $new);
if ($jdev) {
  $old = "cep.jpr";
  $new = $ENV{ADE_VIEW_NAME} . ".jpr";
  personalizeFile($old, $new);
}
if ($nb) {
  $old = "nbproject.xml";
  $new = $ENV{ADE_VIEW_ROOT} . "/cep/wlevs_cql/modules/cqlengine/nbproject";
  mkpath($new);
  $new .= "/project.xml";
  personalizeFile($old, $new);
}

