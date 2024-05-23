#!/usr/local/bin/perl
open(VIEWINFO, "/tmp/adecatcs") || die("View Information can't be retrieved.");
@view_info_array = <VIEWINFO>;
$inpData = $view_info_array[4];
$pos = index($inpData, ": ");
$txnName = substr($inpData,$pos+1);
print($txnName);
