package oracle.cep.test.jmx;
/* $Header: pcbpel/cep/test/src/TkJMXConnector.java /main/4 2008/10/09 16:56:17 skmishra Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    A helper class to analyze the command line and create a JMXServiceURL.
    Allows to pass a JMXServiceURL, a host and port, or a VM PID.
    copied from JVMRuntimeClient.java and modified.


   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/17/08 - stop using sun proprietary api
    hopark      09/17/08 - stop using sun proprietary api
    parujain    09/24/08 - multiple schema
    parujain    04/22/08 - 
    hopark      04/17/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/TkJMXConnector.java /main/4 2008/10/09 16:56:17 skmishra Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.remote.JMXServiceURL;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;

import com.sun.tools.attach.VirtualMachine;

public class TkJMXConnector
{
  // Represents a managed virtual machine 
  private static class ManagedVirtualMachine {
      private int vmid;
      private String address;
      private String cmdLine;
      
      ManagedVirtualMachine(int vmid, String address, String cmdLine) {
          this.vmid = vmid;
          this.address = address;
          this.cmdLine = cmdLine;
      }
      
      public int vmid() {
          return vmid;
      }
      
      public String connectorAddress() {
          return address;
      }       
      
      public String commandLine() {
          return cmdLine;
      }
  }

    private static final String CONNECTOR_ADDRESS =
      "com.sun.management.jmxremote.localConnectorAddress";
    
    ArrayList<ManagedVirtualMachine> vms;
    public final JMXServiceURL jmxURL;
    List<String> otherArgs;
    
    final public String SYNTAX = "JVMRuntimeClient -url <jmx-url> " +
            "| -port <port-number> [-host <host-or-ip] " +
            "| -pid <pid> | -class <class> | -twork <twork> -help";
    
    public TkJMXConnector(String[] args) 
    {
      vms = getManagedVirtualMachines();
      jmxURL = parseArgs(args);
    }
    
    public final JMXServiceURL getJMXServiceURL() 
    {
      return jmxURL;
    }
    
    public String[] getOtherArgs()
    {
      int n = otherArgs.size();
      String[] r = new String[n];
      int p = 0;
      for (String s : otherArgs)
      {
        r[p++] = s;
      }
      return r;
    }
    
    private JMXServiceURL parseArgs(String[] args) 
    {
      String host = null;
      int port = 0;
      int pid = 0;
      String className = null;
      String twork = null;
      JMXServiceURL serviceURL = null;
      otherArgs = new LinkedList<String>();
      for (int i=0;i<args.length;i++) {
          if (args[i].startsWith("-url")) {
          // The '-url' option will let you specify a JMXServiceURL
          // on the command line. This is an URL that begins with
          // service:jmx:<protocol>
          //
              if (++i >= args.length)
                  throwSyntaxError(
                          "missing JMXServiceURL after -url");
              
              try {
                  serviceURL = new JMXServiceURL(args[i]);
              } catch (Exception x) {
                  throwSyntaxError("bad JMXServiceURL after -url: " + x);
              }
              continue;
          } else if (args[i].startsWith("-host")) {
          // The '-host' and '-port' options will let you specify a host
          // and port, and from that will construct the JMXServiceURL of
          // the default RMI connector, that is:
          // service:jmx:rmi:///jndi/rmi://<host>:<port>/jmxrmi"
          //
              if (++i >= args.length)
                  throwSyntaxError("missing host after -host");
              try {
                  InetAddress.getByName(args[i]);
                  host = args[i];
              } catch (Exception x) {
                  throwSyntaxError("bad host after -url: " + x);
              }
              
          } else if (args[i].startsWith("-port")) {
          // The '-host' and '-port' options will let you specify a host
          // and port, and from that will construct the JMXServiceURL of
          // the default RMI connector, that is:
          // service:jmx:rmi:///jndi/rmi://<host>:<port>/jmxrmi"
          //
              if (++i >= args.length)
                  throwSyntaxError("missing port number after -port");
              try {
                  port = Integer.parseInt(args[i]);
                  if (port <= 0)
                      throwSyntaxError("bad port number after -port: " +
                              "must be positive");
              } catch (Exception x) {
                  throwSyntaxError("bad port number after -port: " + x);
              }
          } else if (args[i].startsWith("-pid")) {
          // The '-pid' and option will let you specify the PID of the
          // target VM you want to connect to. It will then use the 
          // attach API to dynamically launch the JMX agent in the target
          // VM (if needed) and to find out the JMXServiceURL of the
          // the default JMX Connector in that VM.
          //
              if (++i >= args.length)
                  throwSyntaxError("missing pid after -pid");
              try {
                  pid = Integer.parseInt(args[i]);
              } catch (Exception x) {
                  throwSyntaxError("bad pid after -pid: " + x);
              }
          } else if (args[i].startsWith("-class")) {
          // The '-className' and option will let you specify the className of the
          // target VM you want to connect to. It will then use the 
          // attach API to dynamically launch the JMX agent in the target
          // VM (if needed) and to find out the JMXServiceURL of the
          // the default JMX Connector in that VM.
          //
              if (++i >= args.length)
                  throwSyntaxError("missing className after -class");
              try {
                  className = args[i];
              } catch (Exception x) {
                  throwSyntaxError("bad className after -class: " + x);
              }
          } else if (args[i].startsWith("-twork")) {
          // The '-twork' and option will let you specify the twork folder of the
          // target VM you want to connect to. It just check if any of argument
          // contains the view name wit hit.
              if (++i >= args.length)
                  throwSyntaxError("missing twork after -twork");
              try {
                  twork = args[i];
              } catch (Exception x) {
                  throwSyntaxError("bad twork after -twork: " + x);
              }
          } else if (args[i].startsWith("-help")) {
              System.err.println(SYNTAX);
              ShowVMs();
              throw new IllegalArgumentException(SYNTAX);
          } else {
              otherArgs.add(args[i]);
          } 
        }
        
        // A JMXServiceURL was given on the command line, just use this.
        //
        if (serviceURL != null)
            return serviceURL;
        
        // A -host -port info was given on the command line. 
        // Construct the default RMI JMXServiceURL from this.
        //
        if (port > 0) {
            if (host == null)
                host = "localhost";
            
            try {
                return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+
                        host+":"+port+"/jmxrmi");
            } catch (Exception x) {
                throwSyntaxError("Bad host or port number: "+x);
            }
        }
        
        // A PID was given on the command line. 
        // Use the attach API to find the target's connector address, and
        // start it if needed.
        //
        if (pid != 0) {
            try {
                ManagedVirtualMachine vm = getVM(pid);
               if (vm == null)
               {
                throwSyntaxError("cannot find vm for "+pid);
               }
                return getURL(vm);
            } catch (Exception x) {
                throwSyntaxError("cannot attach to target vm "+pid+": "+x);
            }
        }
  
        // A className was given on the command line. 
        // Use the attach API to find the target's connector address, and
        // start it if needed.
        //
        if (className != null) {
            try {
               ManagedVirtualMachine vm = getVMForClassName(className, twork);
               if (vm == null)
               {
                throwSyntaxError("cannot find vm for "+className);
               }
               return getURL(vm);
            } catch (Exception x) {
                throwSyntaxError("cannot attach to target vm "+className+": "+x);
            }
        }
  
        System.err.println(SYNTAX);
        ShowVMs();
        throwSyntaxError("missing argument: "+ "-port | -url | -pid | -class | -list\n");
        
        // Unreachable.
        return null;
    }
        
    private ArrayList<ManagedVirtualMachine> getManagedVirtualMachines() 
    {
      int longestCmdLineLength = 0;
      String longestCmdLine = "";
      Set activeVms;
      MonitoredHost host;
      try {
          host = MonitoredHost.getMonitoredHost(new HostIdentifier((String)null));
          activeVms = host.activeVms();
      } catch (java.net.URISyntaxException sx) {
          throw new InternalError(sx.getMessage());
      } catch (sun.jvmstat.monitor.MonitorException mx) {
          throw new InternalError(mx.getMessage());
      }
    
      ArrayList<ManagedVirtualMachine> l = new ArrayList<ManagedVirtualMachine>();
      for (Object vm: activeVms) {
          try { 
        int vmid = (Integer)vm;
        VirtualMachine avm = VirtualMachine.attach(Integer.toString(vmid));
        // get the connector address
        String address =
                avm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
        if (address == null) {
            // not managed
            continue;
        }       
        VmIdentifier vmId = new VmIdentifier(Integer.toString(vmid));
        String cmdLine = 
            MonitoredVmUtil.commandLine(host.getMonitoredVm(vmId));
                    int len = cmdLine.length();
                    if (len > longestCmdLineLength) { 
                        longestCmdLineLength = len;
                        longestCmdLine = cmdLine;
                    }
        l.add(new ManagedVirtualMachine(vmid, address, cmdLine));
          } catch (Exception x) {
          }
      }
      return l;
    }
    
    private void ShowVMs()
    {
      for (ManagedVirtualMachine vm : vms)
      {
        System.out.println(vm.vmid() + " : " + vm.commandLine());
      }  
    }
    

    private ManagedVirtualMachine getVM(int pid)
    {
        for (ManagedVirtualMachine vm : vms)
        {
          if (vm.vmid() == pid)
          {
            return vm;
          }
        }
        return null;
    }
    
    private ManagedVirtualMachine getVMForClassName(String className, String twork)
    {
        for (ManagedVirtualMachine vm : vms)
        {
          String arg = vm.commandLine();
          if (arg.startsWith(className))
          {
            if ((twork == null) || 
                (twork != null && arg.indexOf(twork) > 0))
            {
              System.out.println("Found class=" + className +
               (twork == null ? "" : " twork=" + twork) +
               " pid=" + vm.vmid + " " + arg);
              return vm;
            }
          }
        }
        return null;
    }
        
    private JMXServiceURL getURL(ManagedVirtualMachine vm) throws Exception 
    {
        // get the connector address
        String connectorAddress = vm.connectorAddress();
        return new JMXServiceURL(connectorAddress);
    }
          
    private void throwSyntaxError(String msg) 
    {
      System.err.println(msg);
      System.err.println(SYNTAX);
      throw new IllegalArgumentException(msg);
    }

}
