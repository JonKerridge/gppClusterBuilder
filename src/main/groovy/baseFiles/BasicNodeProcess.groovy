package baseFiles

import gppClusterBuilder.LoaderConstants
import gppClusterBuilder.NodeConnection
import jcsp.lang.CSProcess
import jcsp.net2.*

class BasicNodeProcess implements CSProcess, Serializable, NodeConnection, LoaderConstants{
  String nodeIP, hostIP
  NetLocation toHostLocation
  NetChannelInput fromHost

  def connectFromHost (NetChannelInput fromHost){
    this.fromHost = fromHost
  }

  @Override
  void run() {
    long initialTime = System.currentTimeMillis()
    // create basic connections for node
    NetChannelOutput node2host = NetChannel.one2net(toHostLocation as NetChannelLocation)
    node2host.write(nodeProcessInitiation)
    // read in application net input channel VCNs [ vcn, ... ]
    List inputVCNs = fromHost.read() as List
    //@inputVCNs

    // acknowledge creation of net input channels
    node2host.write(nodeApplicationInChannelsCreated)

    // read in application net output channel locations [ [ip, vcn], ... ]
    List outputVCNs = fromHost.read()
    //@outputVCNs

    // acknowledge creation of net output channels
    node2host.write(nodeApplicationOutChannelsCreated)
    long processStart = System.currentTimeMillis()
    // now start the process - inserted by builder
    //@nodeProcess

    long processEnd = System.currentTimeMillis()
    node2host.write([nodeIP, (processStart - initialTime), (processEnd - processStart)])
  }
}
