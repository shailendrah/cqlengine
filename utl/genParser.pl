#!/usr/local/bin/perl
# 
# $Header: cep/wlevs_cql/modules/cqlengine/utl/genParser.pl /main/1 2012/04/18 01:36:16 sbishnoi Exp $
#
# genParser.pl
# 
# Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      genParser.pl - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    03/19/12 - Creation
# 
################# USAGE Style #####################
# genParser.pl input_file output_file array_name number_of_partitions number_of_elements_in_each_partition

$originalParserFile=$ARGV[0];
$outputFile=$ARGV[1];
$currArray = $ARGV[2];
$numPartitions = $ARGV[3];
$numElemInAPartition = $ARGV[4];

# Open the input file
unless(open(ORIGINAL_FILE, $originalParserFile))
{
  die("Cannot open file ".$originalParserFile);
}

# Create new output file
unless(open(NEW_FILE, ">$outputFile"))
{
  die("Cannot create new file");
}

# Read the original parser file line by line
$currLine = <ORIGINAL_FILE>;
while($currLine ne "")
{    
  $isModified = 0;

  # modify declaration
  $position1 = index($currLine, $currArray . "[];");
  if($position1 >= 0)
  {
    $prefix = substr($currLine, 0, $position1);
    $modifiedLine = $prefix;
    $count = 1;
    while($count <= $numPartitions)
    {
      $modifiedLine = $modifiedLine . $currArray . $count . "[]";
      if($count == $numPartitions)
      {
        $modifiedLine = $modifiedLine . ";" . "\n";
      }
      else
      {
        $modifiedLine = $modifiedLine . ",";      
      }
      $count++;
    }
    $isModified = 1;
  }

# modify initialization call
  $position2 = index($currLine, $currArray . "();");
  if($position2 >= 0)
  {
    $prefix = substr($currLine, 0, $position2);
    $modifiedLine = $prefix;
    $count=1;
    while($count <= $numPartitions)
    {
      $modifiedLine = $modifiedLine . $currArray . $count . "();";
      if($count == $numPartitions)
      {
        $modifiedLine = $modifiedLine . "}" . "\n";
      }
      $count++;
    }
    print NEW_FILE ($modifiedLine);

    $methodDefinition = "static short " . $currArray . "(int index) {" . "\n";
    print NEW_FILE ($methodDefinition);
    $count = 1;
    while($count <= $numPartitions)
    {
      $comparisonIndex = "comparisonValue";
      $valueIndex      = "indexValue";
      if($count < $numPartitions)
      {
        $comparisonIndex = $count . " * " . $numElemInAPartition;
        if($count == 1)
        {
          $valueIndex = "index";
        }
        else
        {
          $valueIndex = "index - 1 -" . $numElemInAPartition . " * (" . $count . " - 1)";
        } 
        $modifiedLine = "  if (index <=" . $comparisonIndex . ")" . "\n";
        $modifiedLine = $modifiedLine . "  {" . "\n";
        $modifiedLine = $modifiedLine . "    return " . $currArray . $count . "[" . $valueIndex . "];" . "\n";
        $modifiedLine = $modifiedLine . "  }" . "\n";         
        print NEW_FILE ($modifiedLine);
      }
      elsif($count == $numPartitions)
      {
        $valueIndex = "index - 1 -" . $numElemInAPartition . " * (" . $count . " - 1)";
        $modifiedLine = "  return " . $currArray . $count . "[" . $valueIndex . "];" . "\n"; 
        print NEW_FILE ($modifiedLine);
      }
      $count++;
    }
    $isModified = -1;
    print NEW_FILE ("}". "\n");
  }

# modify initialization
  $position3 = index($currLine, $currArray . "() {");
  if($position3 == -1)
  {
    $position3 = index($currLine, $currArray . "(){");
  }
  if($position3 >= 0)
  {
    $count=1;
    $numElements = 0;
    $initPrefix = substr($currLine, 0, $position3) . $currArray;
    $done = 0;
    while($done != 1)
    {    
      $currLine = <ORIGINAL_FILE>;
      $bracketPos = index($currLine, "}");
      # if array is completely read
      if($bracketPos >=0)
      {
        $currElems = substr($currLine, 0, $bracketPos);
        @arrayElements = (@arrayElements, $currElems);
        $done = 1;
      }
      else
      {
        $bracketPos = index($currLine, "{");      
        if($bracketPos >= 0)
        {
          $currElems = substr($currLine, $bracketPos+1);
          @arrayElements = (@arrayElements, $currElems);
        }
        else
        {
          @arrayElements = (@arrayElements, $currLine);
        }
      }
    }
    # divide array elements into multiple arrays
    $count = 1;
    $numArrayElements = @arrayElements;
    $currIndex = 0;
    while($count <= $numPartitions)
    {
      $modifiedLine = $initPrefix . $count . "() {" . "\n";
      $modifiedLine = $modifiedLine . $currArray . $count . " = new short[] {" . "\n";
      # last partition; so add all elements to this partition
      if($count == $numPartitions)
      {
        while($currIndex < $numArrayElements)
        {
          $modifiedLine = $modifiedLine . $arrayElements[$currIndex];
          $currIndex++;
        }
      }
      else
      {
        $done = 0;
        $currPartitionElemCount = 0;
        while($done == 0)
        {
          if($currIndex == $numArrayElements)
          {
            $done = 1;
          }
          elsif($currPartitionElemCount > 500)
          {
            $done = 1;
          }
          else
          {
            $modifiedLine = $modifiedLine . $arrayElements[$currIndex];
            $currIndex++;
            $currPartitionElemCount++;
          }
        }
      }
      $modifiedLine = $modifiedLine . "};" . "\n";
      $modifiedLine = $modifiedLine . "}" . "\n";   
      print NEW_FILE ($modifiedLine);
      $count++;
    }
    # skip the next line as it is a closing brace for array
    $currLine = <ORIGINAL_FILE>;
    $isModified = -1;  
  }

# modify the array usage 
  $position = index($currLine, $currArray . "[");
  if($position >= 0 && $isModified == 0)
  {
    $leftBracketPos = index($currLine, "[");
    $rightBracketPos = index($currLine , "]");

    $prefix = substr($currLine, 0, $leftBracketPos);
    $postfix = substr($currLine, $rightBracketPos+1);
    $indexVariable = substr($currLine, $leftBracketPos+1, $rightBracketPos-$leftBracketPos-1);
    $modifiedLine = $prefix . "(" . $indexVariable . ")" . $postfix;
    $isModified = 1;
  }
  
  if($isModified == 1)
  {
    print NEW_FILE ($modifiedLine);
  }
  elsif($isModified == 0)
  {
    print NEW_FILE ($currLine);
  }
  $currLine = <ORIGINAL_FILE>;
} 
