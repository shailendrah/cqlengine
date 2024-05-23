#This file has a list of filter.
#format is
# command,regexp
#
# commands are
# apply  : apply filter for only the specified files
# remove : remove the pattern
# process : process the file found in the log
# idfy : replace the pattern with continuous id

apply,outsys
apply,out-push-systs
apply,out-rel-systs
apply,out-stream-systs
apply,outMultiline5
apply,outMultiline6

#1234567:
remove,^\d+:

