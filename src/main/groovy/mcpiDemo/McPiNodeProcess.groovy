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
 
class McPiNodeProcess implements CSProcess, Serializable, NodeConnection, LoaderConstants{
String nodeIP, hostIP
NetLocation toHostLocation
NetChannelInput fromHost
 
def connectFromHost (NetChannelInput fromHost){
this.fromHost = fromHost
}
 
@Override
void run() {


int cores = 2
int clusters = 2
 
long initialTime = System.currentTimeMillis()
// create basic connections for node
NetChannelOutput node2host = NetChannel.one2net(toHostLocation as NetChannelLocation)
node2host.write(nodeProcessInitiation)
// read in application net input channel VCNs [ vcn, ... ]
List inputVCNs = fromHost.read() as List
//@inputVCNs
//nodeInputInsert

 
// acknowledge creation of net input channels
node2host.write(nodeApplicationInChannelsCreated)
 
// read in application net output channel locations [ [ip, vcn], ... ]
List outputVCNs = fromHost.read()
//@outputVCNs
//nodeOutputInsert

 
// acknowledge creation of net output channels
node2host.write(nodeApplicationOutChannelsCreated)
long processStart = System.currentTimeMillis()
// now start the process - inserted by builder
//@nodeProcess
//nodeProcessInsert

 
long processEnd = System.currentTimeMillis()
node2host.write([nodeIP, (processStart - initialTime), (processEnd - processStart)])
}
}
