package gppClusterBuilder

int clusters
clusters = 4
int nodes_Number
nodes_Number = clusters

Range emitInRange = 100 ..< 100+nodes_Number
int nodeInRange = 200
int collectInRange = 300

//println "$emitInRange, $nodeInRange, $collectInRange"

List <String>  nodeInputInsert = ["//node Input Insert\n"]
List <String>  nodeOutputInsert = ["//node Output Insert\n"]
List <String>  hostNodeInputInsert = ["//host NodeInput Insert\n"]
List <String>  hostInputInsert = ["//host Input Insert\n"]
List <String>  hostNodeOutputInsert = ["//host NodeOutput Insert\n"]
List <String>  hostOutputInsert = ["//host Output Insert\n"]

String emitRequestName, emitResponseName, nodeRequestName, nodeResponseName
String nodeOutputName, collectInputName

def createAllNetChannelInserts = {
  String hostNodeInputInsertString
  hostNodeInputInsertString = "inputVCNs = [ "
  for (n in 0..<nodes_Number - 1) hostNodeInputInsertString += "[$nodeInRange], "
  hostNodeInputInsertString += "[$nodeInRange] "
  hostNodeInputInsertString += "]\n"
  hostNodeInputInsert << hostNodeInputInsertString

  String hostInputInsertString
  hostInputInsertString = "ChannelInputList emitRequestList = [] \n"
  emitInRange.each { e -> hostInputInsertString += "emitRequestList.append(NetChannel.numberedNet2One($e)) \n"
  }
  hostInputInsertString += "ChannelInput collectFromNodes = NetChannel.numberedNet2One($collectInRange) \n"
  hostInputInsert << hostInputInsertString
  emitRequestName = "emitRequestList"
  collectInputName = "collectFromNodes"

  String hostNodeOutputInsertString
  List<String> nodeString = []
  hostNodeOutputInsertString = "outputVCNs = [ "
  emitInRange.each { e ->
    String s = "[ [hostIP, $e], [hostIP, $collectInRange] ]"
    nodeString << s
  }
  for (n in 0..<nodes_Number - 1) hostNodeOutputInsertString += nodeString[n] + ", "
  hostNodeOutputInsertString += nodeString[nodes_Number - 1] + " ]\n"
  hostNodeOutputInsert << hostNodeOutputInsertString

  String hostOutputInsertString
  hostOutputInsertString = "ChannelOutputList emitResponseList = [] \n"
  for (i in 0..<nodes_Number) {
    hostOutputInsertString += "emitResponseList.append(NetChannel.one2net(new TCPIPNodeAddress(nodeIPs[$i], 1000), 200)) \n"
  }
  hostOutputInsert << hostOutputInsertString
  emitResponseName = "emitResponseList"

  String nodeInputInsertString = "ChannelInput nodeFromEmit = NetChannel.numberedNet2One(inputVCNs[0])\n"
  nodeInputInsert << nodeInputInsertString
  nodeResponseName = "nodeFromEmit"

  String nodeOutputInsertString
  nodeOutputInsertString = "ChannelOutput node2emit = NetChannel.one2net" + "(new TCPIPNOdeAddress(outputVCNs[0][0], 2000), outputVCNs[0][1])\n"
  nodeOutputInsertString += "ChannelOutput node2collect = NetChannel.one2net" + "(new TCPIPNOdeAddress(outputVCNs[1][0], 2000), outputVCNs[1][1])\n"
  nodeOutputInsert << nodeOutputInsertString
  nodeRequestName = "node2emit"
  nodeOutputName = "node2collect"
}

createAllNetChannelInserts()

hostNodeInputInsert.each { println "$it" }
hostInputInsert.each { println "$it" }
hostOutputInsert.each { println "$it" }
hostNodeOutputInsert.each { println "$it" }
nodeInputInsert.each { println "$it" }
nodeOutputInsert.each { println "$it" }