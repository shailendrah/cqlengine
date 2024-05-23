#This file has a list of filter.
#format is
# command,regexp
#
# commands are
# remove
# process

#Found class=oracle.cep.test.TestServer twork=oracle/work ...
remove,Found.*

#Log Timestamp : Jan 31, 2008 12:30:30 AM/PM
remove,[A-Za-z]* \d+, \d* \d+:\d+:\d+ (A|P)M

#ade path component /ade/hopark_cep5/
remove,/ade/[a-zA-Z_0-9]+/

#Process id: (pid=595)
remove,pid=[0-9]*

#StartTime = 
remove,startTime = [0-9]*

#EndTime = 
remove,endTime = [0-9]*

#usedMemory = 
remove,usedMemory = [0-9]*

#freeMemory = 
remove,freeMemory = [0-9]*

#maxMemory = 
remove,maxMemory = [0-9]*

#totalMemory = 
remove,totalMemory = [0-9]*

#time = 
remove,time = [0-9]*

#numberOfThreads = 
remove,numberOfThreads = [0-9]*

#StreamId = 
remove,streamId = [0-9]*

#StreamId = 
remove,operatorId = [0-9]*

#functionId = 
remove,functionId = [0-9]*

#inputRate = 
remove,inputRate = [0-9\.]*

#percent = 
remove,percent = [0-9\.]*

#Connected to service:
remove,Connected to service:.*

