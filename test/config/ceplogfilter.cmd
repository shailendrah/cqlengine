#This file has a list of filter.
#format is
# command,regexp
#
# commands are
# remove
# process

# process trc file
process,(/ade/.*/trc/.*\.xml)

#Log Timestamp : Jan 31, 2008 12:30:30 AM/PM
remove,[A-Za-z]* \d+, \d* \d+:\d+:\d+ (A|P)M

#ade path component /ade/hopark_cep5/
remove,/ade/[a-zA-Z_0-9]+/

#Line number : (Thread.java:595)
remove,\(.*\.java:[0-9]*\)

#Run time : 0:0:15
remove,Run time: \d+:\d+:\d+

#Memory usage used=34.517513m init=536.8709m committed=529.46533m max=529.46533m
remove,used=.+m init=.+m committed=.+m max=.+m

#Collection time collection time=0.116 seconds
remove,collection time=.+ seconds

#Number of factories : 56
remove,Number of factories : \d+

#Global Physical Plan is : 25
remove,Global Physical Plan is : \d+

# Hashcode="16019616"
remove, Hashcode=\"\d+\"

# Id="16019616"
remove, Id=\"\d+\"

# Prev="1234567"
remove, Prev=\"\d+\"

# Next="1234567"
remove, Next=\"\d+\"

# Tuple(1027,10923456)
remove,Tuple\(\d+,\d+\)

# refernce @1478a2d
remove,@[0-9a-z]+

# obj=DoublyListNode2678114,
remove,obj=.*,

# <object>DoublyListNode2678114</object>
remove,<object>.*</object>

#BottomUpPolicy total=529m
remove,total=\d+m

#BottomUpPolicy memThreshold0>529m
remove,memThreshold\d>\d+

#BottomUpPolicy memThreshold>529m
remove,memThreshold>\d+

#BottomUpPolicy MemThreshold>529m
remove,MemThreshold>\d+

#BottomUpPolicy totalMem>529m
remove,totalMem>\d+

#<outputElement>27700281  : kind=
remove,<outputElement>\d+ : kind=

#<Arg0>6207304 : kind=
remove,<Arg0>\d+ : kind=
 
#<startTime>1202462673850<
remove,<startTime>\d+<

#<endTime>1202462673850<
remove,<endTime>\d+<

#<totalTime>47</totalTime>
remove,<totalTime>\d+<

#<UsedBuffer>3145728</UsedBuffer>
remove,<UsedBuffer>\d+<

#<CachedData>261128</CachedData>
remove,<CachedData>\d+<

#<CachedTotal>3406856</CachedTotal>
remove,<CachedTotal>\d+<

#<CachedTotal>0</NoCacheMiss>
remove,<CachedTotal>\d+<

# CacheSize="317679206" 
remove,CacheSize=\"\d+\" 

# cacheSize="16019616"
remove,cacheSize=\"\d+\"

#<Arg0>10:16:09 free = 482m count = 55 memThreshold = 185m countThreshold = 300000 useCount = false</Arg0>
remove,<Arg0>\d+:\d+:\d+ free = \d+m count = \d+ memThreshold = \d+m countThreshold = \d+ useCount = false<

# <m_startTime>1202494564534<
remove,<m_startTime>\d+<

# Length="348" 
remove,Length=\"\d+\" 

# Entries="348" 
remove,Entries=\"\d+\" 

#<Arg2>340</Arg2>
remove,<Arg2>\d+<

#<maxLength>340</maxLength>
remove,<maxLength>\d+<

#<Folder></Folder>
remove,<Folder>.+<

# pmId=348 
remove,pmId=\d+ 

#normalThreshold = 264m,partialSpillThreshold = 174m,fullSpillThreshold = 132m,syncSpillThreshold = 79m
remove,= \d+m

