package mcpiDemo

import jcsp.lang.*
import groovyJCSP.*
import jcsp.net2.*
import jcsp.net2.mobile.*
import jcsp.net2.tcpip.*
import gppClusterBuilder.*
 
import groovyParallelPatterns.DataDetails
import groovyParallelPatterns.ResultDetails
import groovyParallelPatterns.cluster.connectors.NodeRequestingFanAny
import groovyParallelPatterns.cluster.connectors.OneNodeRequestedList
import groovyParallelPatterns.connectors.reducers.AnyFanOne
import groovyParallelPatterns.functionals.groups.AnyGroupAny
import groovyParallelPatterns.terminals.Collect
import groovyParallelPatterns.terminals.Emit
 
class McPiHostProcess implements CSProcess, LoaderConstants{
String hostIP
List <String> nodeIPs
NetChannelInput nodes2host
List <NetChannelOutput> host2nodes
 
@Override
void run() {


int cores = 2
int clusters = 2
 
int nodes_Number = nodeIPs.size()
// create basic process connections for host
for ( n in 0 ..< nodes_Number) {
// wait for all nodes to start
assert nodes2host.read() == nodeProcessInitiation :
"Node ${nodeIPs[n]} failed to initialise node process"
// create host2nodes channels - already have node IPs
}
long initialTime = System.currentTimeMillis()
// send application channel data to nodes - inserted by Builder - also those at host
List inputVCNs   // each node gets a list of input VCNs
//@inputVCNs
//hostNodeInputInsert

 
for ( n in 0 ..< nodes_Number) host2nodes[n].write(inputVCNs[n])
 
//@hostInputs
//hostInputInsert

 
// now read acknowledgments
for ( n in 0 ..< nodes_Number){
assert nodes2host.read() == nodeApplicationInChannelsCreated :
"Node ${nodeIPs[n]} failed to create node to host link channels"
}
// each node gets a list [IP, vcn] to which it is connected
List outputVCNs
//@outputVCNs
//hostNodeOutputInsert

 
for ( n in 0 ..< nodes_Number) host2nodes[n].write(outputVCNs[n])
 
//@hostOutputs
//hostOutputInsert

 
// now read acknowledgments
for ( n in 0 ..< nodes_Number){
assert nodes2host.read() == nodeApplicationOutChannelsCreated :
"Node ${nodeIPs[n]} failed to create node to host link channels"
}
// all the net application channels have been created
long processStart = System.currentTimeMillis()
// now start the process - inserted by builder
//@hostProcess
//hostProcessInsert

 
 
long processEnd = System.currentTimeMillis()
List times = [ ["Host", (processStart - initialTime), (processEnd - processStart)] ]
for ( n in 0 ..< nodes_Number){
times << (List)(nodes2host.read() )
}
times.each {println "$it"}
}
}
