package gpp_cluster_builder

import jcsp.net2.NetChannelInput

interface NodeConnection {
  abstract connectFromHost(NetChannelInput fromHost)

}