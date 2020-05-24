package gppClusterBuilder

import jcsp.net2.NetChannelInput

interface NodeConnection {
  abstract connectFromHost(NetChannelInput fromHost)

}