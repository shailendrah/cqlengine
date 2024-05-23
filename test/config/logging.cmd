wait,30

# dump all queries
runcqlx,"alter system dump logging systemstate level 1"

# dump all operators
runcqlx,"alter system dump logging systemstate level 6"

# dump all operators and underlying data structures stat,stat_ds,ds,vdump
# takes 4 min
#runcqlx,"alter system dump logging operator level 2,3,6,8"

# dump all queues info,stat,dump,vdump
# takes 25 mins
#runcqlx,"alter system dump logging queue level 1,4,5,6"

# dump all stores info,stat,dump,vdump
# takes 6 min
#runcqlx,"alter system dump logging store level 1,4,5,6"

# dump all synopsis info,stat,dump,vdump
#runcqlx,"alter system dump logging synopsis level 1,4,6,7"

# dump all index info,stat,dump,vdump
#runcqlx,"alter system dump logging index level 1,4,5,6"

# dump spill info,stat,dump,vdump
runcqlx,"alter system dump logging spill level 2,3,4,5"

# dump storage info,stat
runcqlx,"alter system dump logging storage level 2,3"

# dump metadata_query info,lock_info
runcqlx,"alter system dump logging metadata_query level 2,3"

# dump metadata_table info,lock_info
runcqlx,"alter system dump logging metadata_table level 2,3"

# dump metadata_window info,lock_info
runcqlx,"alter system dump logging metadata_window level 2,3"

# dump metadata_userfunc info,lock_info
runcqlx,"alter system dump logging metadata_userfunc level 2,3"

# dump metadata_view info,lock_info
runcqlx,"alter system dump logging metadata_view level 2,3"

# dump metadata_system info,lock_info
runcqlx,"alter system dump logging metadata_system level 1,2"

# trace stream source enqueue/dequeue with most info
runcqlx,"alter system enable logging operator type strmsrc event 105 level 6"
wait,10
# dump all log levels
runcqlx,"alter system dump logging systemstate level 7"
runcqlx,"alter system clear logging"

# trace all queues 
runcqlx,"alter system enable logging queue level 0,1,2,3,4,5,6"
wait,10
runcqlx,"alter system clear logging"

# trace all stores 
runcqlx,"alter system enable logging store level 0,1,2,3,4,5,6"
wait,10
runcqlx,"alter system clear logging"

# trace all indexes 
runcqlx,"alter system enable logging index level 0,1,2,3,4,5,6"
wait,10
runcqlx,"alter system clear logging"

# trace all synopsis 
runcqlx,"alter system enable logging synopsis level 0,1,2,3,4,5,6,7"
wait,10
runcqlx,"alter system clear logging"

# trace all operators
runcqlx,"alter system enable logging operator level 0,1,2,3,4,5,6,7,8"
wait,10
runcqlx,"alter system clear logging"

# trace all storage
runcqlx,"alter system enable logging storage level 1,2"
wait,10
runcqlx,"alter system clear logging"

# trace all spill
runcqlx,"alter system enable logging spill level 1,2,3,4"
wait,10
runcqlx,"alter system clear logging"

# trace all metadata query
/*
runcqlx,"alter system enable logging metadata_query level 1,2,3"
runcqlx,"alter system enable logging metadata_table level 1,2,3"
runcqlx,"alter system enable logging metadata_window level 1,2,3"
runcqlx,"alter system enable logging metadata_userfunc level 1,2,3"
runcqlx,"alter system enable logging metadata_view level 1,2,3"
runcqlx,"alter system enable logging metadata_system level 1,2,3"
wait,10
runcqlx,"drop query q3"
runcqlx,"alter system clear logging"
*/

#stop the running instance
#quit 
