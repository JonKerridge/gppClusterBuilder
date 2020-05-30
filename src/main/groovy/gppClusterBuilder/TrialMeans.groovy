package gppClusterBuilder

int clusters
clusters = 4
int nodes_Number
nodes_Number = clusters

Range emitInRange = 100 ..< 100+clusters
int nodeInRange = 200
int collectInRange = 300

//println "$emitInRange, $nodeInRange, $collectInRange"

List <String>  nodeInputInsert = ["//node Input Insert\n"]
List <String>  nodeOutputInsert = ["//node Output Insert\n"]
List <String>  hostNodeInputInsert = ["//host NodeInput Insert\n"]
List <String>  hostInputInsert = ["//host Input Insert\n"]
List <String>  hostNodeOutputInsert = ["//host NodeOutput Insert\n"]
List <String>  hostOutputInsert = ["//host Output Insert\n"]

String hostNodeInputInsertString
hostNodeInputInsertString = "inputVCNs = [ "
for ( n in 0 ..< nodes_Number-1) hostNodeInputInsertString += "[$nodeInRange], "
hostNodeInputInsertString += "[$nodeInRange] "
hostNodeInputInsertString += "]\n"
hostNodeInputInsert << hostNodeInputInsertString
hostNodeInputInsert.each {println "$it"}

String hostInputInsertString
hostInputInsertString = "ChannelInputList netInList1 = [] \n"
emitInRange.each {e ->
  hostInputInsertString += "netInList1.append(NetChannel.numberedNet2One($e)) \n"
}
hostInputInsertString += "ChannelInput netIn1 = NetChannel.numberedNet2One($collectInRange) \n"
hostInputInsert << hostInputInsertString
hostInputInsert.each {println "$it"}

String hostNodeOutputInsertString
List <String> nodeString = []
hostNodeOutputInsertString = "outputVCNs = [ "
emitInRange.each {e ->
  String s = "[ [hostIP, $e], [hostIP, $collectInRange] ]"
  nodeString << s
}
for ( n in 0 ..< nodes_Number-1) hostNodeOutputInsertString += nodeString[n] + ", "
hostNodeOutputInsertString += nodeString[nodes_Number-1] + " ]\n"
hostNodeOutputInsert << hostNodeOutputInsertString
hostNodeOutputInsert.each {println "$it"}

String hostOutputInsertString
hostOutputInsertString = "ChannelOutputList netOutList1 = [] \n"
for ( i in 0 ..< nodes_Number) {
  hostOutputInsertString += "netOutList1.append(NetChannel.one2net(new TCPIPNodeAddress(nodeIPs[$i], 1000), 200)) \n"
}
hostOutputInsert << hostOutputInsertString
hostOutputInsert.each {println "$it"}

String nodeInputInsertString = "ChannelInput netIn1 = NetChannel.numberedNet2One(inputVCNs[0])\n"
nodeInputInsert << nodeInputInsertString
nodeInputInsert.each {println "$it"}

String nodeOutputInsertString
nodeOutputInsertString = "ChannelOutput netOut1 = NetChannel.one2net" +
    "(new TCPIPNOdeAddress(outputVCNs[0][0], 2000), outputVCNs[0][1])\n"
nodeOutputInsertString += "ChannelOutput netOut2 = NetChannel.one2net" +
    "(new TCPIPNOdeAddress(outputVCNs[1][0], 2000), outputVCNs[1][1])\n"
nodeOutputInsert << nodeOutputInsertString
nodeOutputInsert.each {println "$it"}

