package gppClusterBuilder

class CGPPlexingMethods {

  String error = ""
  String network = ""
  String preNetwork = ""

  def hostProcessNames = []
  def nodeProcessNames = []
  def chanNumber = 1
  String currentOutChanName = "chan$chanNumber"
  String currentInChanName = "chan$chanNumber"
  ChanTypeEnum expectedInChan
  String chanSize

  // source script
  List<String> scriptText = []

  // texts of basic codes included directly
  // due to difficulty in loading files as part of the library
  List <String> nodeLoaderText = [
      'package baseFiles',
      ' ',
      'import gppClusterBuilder.LoaderConstants',
      'import groovyJCSP.*',
      'import jcsp.lang.*',
      'import jcsp.net2.*',
      'import jcsp.net2.mobile.*',
      'import jcsp.net2.tcpip.*',
      ' ',
      'class BasicNodeLoader implements LoaderConstants{',
      'static void main(String[] args) {',
      '//@hostIPAddress',
      '// create this node',
      'def nodeAddress = new TCPIPNodeAddress(1000)',
      'Node.getInstance().init(nodeAddress)',
      'String nodeIP = nodeAddress.getIpAddress()',
      'println "Node $nodeIP running"',
      '// create net input channel from host',
      'NetChannelInput fromHost = NetChannel.numberedNet2One(1,',
      'new CodeLoadingChannelFilter.FilterRX() )',
      'def hostAddress = new TCPIPNodeAddress(hostIP, 2000)',
      '// create host request output channel',
      'def toHost = NetChannel.any2net(hostAddress, 1)',
      '// send host the IP of this Node and get response',
      'toHost.write(nodeIP)',
      'assert fromHost.read() == acknowledgeNodeIPRead:',
      '"Node Load - $nodeIP: expected acknowledgement during initialisation not received"',
      '// read in and connect the Application Noide process from host',
      'CSProcess nodeProcess = fromHost.read() as CSProcess',
      'nodeProcess.connectFromHost(fromHost)',
      '// acknowledge receipt of node process',
      'toHost.write(nodeProcessRead)',
      '// wait to receive start signal from host',
      'assert fromHost.read() == startNodeProcess:',
      '"Node Load - $nodeIP: expected start Node Process signal not received"',
      '//  start node process',
      'new PAR([nodeProcess]).run()',
      '}',
      '}'
  ]
  List <String> nodeProcessText = [
      'package baseFiles',
      ' ',
      'import gppClusterBuilder.LoaderConstants',
      'import gppClusterBuilder.NodeConnection',
      'import jcsp.lang.CSProcess',
      'import jcsp.net2.*',
      ' ',
      'class BasicNodeProcess implements CSProcess, Serializable, NodeConnection, LoaderConstants{',
      'String nodeIP, hostIP',
      'NetLocation toHostLocation',
      'NetChannelInput fromHost',
      ' ',
      'def connectFromHost (NetChannelInput fromHost){',
      'this.fromHost = fromHost',
      '}',
      ' ',
      '@Override',
      'void run() {',
      'long initialTime = System.currentTimeMillis()',
      '// create basic connections for node',
      'NetChannelOutput node2host = NetChannel.one2net(toHostLocation as NetChannelLocation)',
      'node2host.write(nodeProcessInitiation)',
      '// read in application net input channel VCNs [ vcn, ... ]',
      'List inputVCNs = fromHost.read() as List',
      '//@inputVCNs',
      ' ',
      '// acknowledge creation of net input channels',
      'node2host.write(nodeApplicationInChannelsCreated)',
      ' ',
      '// read in application net output channel locations [ [ip, vcn], ... ]',
      'List outputVCNs = fromHost.read()',
      '//@outputVCNs',
      ' ',
      '// acknowledge creation of net output channels',
      'node2host.write(nodeApplicationOutChannelsCreated)',
      'println "Node starting application process"',
      'long processStart = System.currentTimeMillis()',
      '// now start the process - inserted by builder',
      '//@nodeProcess',
      ' ',
      'long processEnd = System.currentTimeMillis()',
      'println "Node application component has Finished"\n',
      'node2host.write([nodeIP, (processStart - initialTime), (processEnd - processStart), ])',
      'println " Node $nodeIP has sent times to host"\n',
      '}',
      '}'
  ]
  List <String> hostLoaderText = [
      'package baseFiles',
      ' ',
      'import gppClusterBuilder.LoaderConstants',
      'import groovyJCSP.*',
      'import jcsp.net2.*',
      'import jcsp.net2.mobile.*',
      'import jcsp.net2.tcpip.*',
      ' ',
      'class BasicHostLoader implements LoaderConstants{',
      'static void main(String[] args) {',
      '//@nodeSize',
      '// create node and net input channel used by NodeLoaders',
      'def nodeAddress = new TCPIPNodeAddress(2000)',
      'Node.getInstance().init(nodeAddress)',
      'String hostIP = nodeAddress.getIpAddress()',
      'println "Host $hostIP running with $nodes nodes"',
      'NetChannelInput fromNodes = NetChannel.numberedNet2One(1 )',
      '// wait for all the nodes to send their IP addresses',
      '// create a List for the IPs',
      'List <String> nodeIPs = []',
      '// create list of individual net output channel to each node',
      'List < NetChannelOutput> toNodes = []',
      'for ( n in 0 ..< nodes) {',
      'String nodeIP = (fromNodes.read() as String)',
      'nodeIPs << nodeIP',
      'toNodes << NetChannel.one2net(',
      'new TCPIPNodeAddress(nodeIP,1000),',
      '1,',
      'new CodeLoadingChannelFilter.FilterTX())  // must be code loading because node process will be sent',
      '}',
      '// can now start timing as from now on the interactions are contiguous and',
      '// do not rely on Nodes being started from command line',
      'long initialTime = System.currentTimeMillis()',
      '// acknowledge receipt of NodeIPs to nNodes',
      'for ( n in 0 ..< nodes){',
      'toNodes[n].write(acknowledgeNodeIPRead)',
      '}',
      '// now send the built Node process to each Node - name modified by builder',
      '//@nodeProcess',
      'for ( n in 0 ..< nodes){',
      'toNodes[n].write(new BasicNodeProcess(',
      '    hostIP: hostIP,',
      '    nodeIP: nodeIPs[n],',
      '    toHostLocation: fromNodes.getLocation()',
      '  )',
      '  )',
      '}',
      '// now read acknowledgements from Nodes',
      'for ( n in 0 ..< nodes){',
      'assert  fromNodes.read() == nodeProcessRead :',
      '"Failed to read Node Process read acknowledgement from ${nodeIPs[n]}"',
      '}',
      '// tell nodes to start their processes',
      'for ( n in 0 ..< nodes){',
      'toNodes[n].write(startNodeProcess)',
      '}',
      'long processStart = System.currentTimeMillis()',
      '// builder modifies the name of the host process',
      '//@hostProcess',
      'new PAR([new BasicHostProcess(',
      'hostIP: hostIP,',
      'nodeIPs: nodeIPs,',
      'nodes2host: fromNodes,',
      'host2nodes: toNodes',
      ')]).run()',
      ' ',
      'long processEnd = System.currentTimeMillis()',
      'println "Host Node,${processStart - initialTime},  " +',
      '"${processEnd - processStart}, " +',
      '"${processEnd - initialTime}"',
      '}',
      '}'
  ]
  List <String> hostProcessText = [
      'package baseFiles',
      ' ',
      'import gppClusterBuilder.LoaderConstants',
      'import jcsp.lang.*',
      'import jcsp.net2.*',
      ' ',
      'class BasicHostProcess implements CSProcess, LoaderConstants{',
      'String hostIP',
      'List <String> nodeIPs',
      'NetChannelInput nodes2host',
      'List <NetChannelOutput> host2nodes',
      ' ',
      '@Override',
      'void run() {',
      'int nodes_Number = nodeIPs.size()',
      '// create basic process connections for host',
      'for ( n in 0 ..< nodes_Number) {',
      '  // wait for all nodes to start',
      '  assert nodes2host.read() == nodeProcessInitiation :',
      '    "Node ${nodeIPs[n]} failed to initialise node process"',
      '  // create host2nodes channels - already have node IPs',
      '}',
      'long initialTime = System.currentTimeMillis()',
      '// send application channel data to nodes - inserted by Builder - also those at host',
      'List inputVCNs   // each node gets a list of input VCNs',
      '//@inputVCNs',
      ' ',
      'for ( n in 0 ..< nodes_Number) host2nodes[n].write(inputVCNs[n])',
      ' ',
      '//@hostInputs',
      ' ',
      '// now read acknowledgments',
      'for ( n in 0 ..< nodes_Number){',
      '  assert nodes2host.read() == nodeApplicationInChannelsCreated :',
      '    "Node ${nodeIPs[n]} failed to create node to host link channels"',
      '}',
      '// each node gets a list [IP, vcn] to which it is connected',
      'List outputVCNs',
      '//@outputVCNs',
      ' ',
      'for ( n in 0 ..< nodes_Number) host2nodes[n].write(outputVCNs[n])',
      ' ',
      '//@hostOutputs',
      ' ',
      '// now read acknowledgments',
      'for ( n in 0 ..< nodes_Number){',
      '  assert nodes2host.read() == nodeApplicationOutChannelsCreated :',
      '    "Node ${nodeIPs[n]} failed to create node to host link channels"',
      '}',
      '// all the net application channels have been created',
      'long processStart = System.currentTimeMillis()',
      '// now start the process - inserted by builder',
      '//@hostProcess',
      ' ',
      ' ',
      'long processEnd = System.currentTimeMillis()',
      'List times = [ ',
      '    ["Host         ", (processStart - initialTime), (processEnd - processStart),] ' ,
      ' ]',
      'for ( n in 1 .. nodes_Number){',
      '  times << (List)(nodes2host.read() )',
      '}',
      'times.each {println "$it"}',
      '}',
      '}'
  ]
  // source inserts into node and host processes
  List <String>  nodeInputInsert = ["//node Input Insert\n"]
  List <String>  nodeOutputInsert = ["//node Output Insert\n"]
  List <String>  nodeProcessInsert = ["//node Process Insert\n"]
  List <String>  hostNodeInputInsert = ["//host NodeInput Insert\n"]
  List <String>  hostInputInsert = ["//host Input Insert\n"]
  List <String>  hostNodeOutputInsert = ["//host NodeOutput Insert\n"]
  List <String>  hostOutputInsert = ["//host Output Insert\n"]
  List <String>  hostProcessChannelInsert = ["//host Process Channel Insert\n"]
  List <String>  hostProcessParInsert = ["//host ProcessPar Insert\n"]

  // extracted values from input script
  List <String> commonDeclarations = ["\n"]

  int emitStart, emitEnd, collectStart, collectEnd, clusterStart, clusterEnd, clusterSize

  // names of net channels
  String emitRequestName, emitResponseName, nodeRequestName, nodeResponseName
  String nodeOutputName, collectInputName, hostIPText

  // records if first process in collect part is List or Any
  boolean collectByList = false

  // resultants scripts
  List <String> nodeLoaderOutText = []
  List <String> nodeProcessOutText = []
  List <String> hostLoaderOutText = []
  List <String> hostProcessOutText = []

  String appName    // the name of the application

  // text file pointers
  int scriptCurrentLine = 1
  int nodeLoaderLine = 1
  int nodeProcessLine = 1
  int hostLoaderLine = 1
  int hostProcessLine = 1
  int endLine = 0

  // line numbers in scriptText of key processes
  int emitLastProc
  int clusterFirstProc
  int clusterLastProc
  int collectFirstProc

  boolean pattern = false
//  boolean logging = false

  // file inputs and outputs

  def getInputs(
      FileReader scriptReader,
      String appName) {
    this.appName = appName
    scriptReader.each { String line ->
      if (line.size() == 0) line = " " else line = line.trim()
      scriptText << line
    }
    scriptReader.close()

    // copy package line and jcsp imports to output script texts
    nodeLoaderOutText << scriptText[0] + "\n"
    nodeLoaderOutText << "import jcsp.lang.*\nimport groovyJCSP.*\nimport jcsp.net2.*\n"
    nodeLoaderOutText << "import jcsp.net2.mobile.*\nimport jcsp.net2.tcpip.*\nimport gppClusterBuilder.*\n"
    nodeProcessOutText << scriptText[0] + "\n"
    nodeProcessOutText << "import jcsp.lang.*\nimport groovyJCSP.*\nimport jcsp.net2.*\n"
    nodeProcessOutText << "import jcsp.net2.mobile.*\nimport jcsp.net2.tcpip.*\nimport gppClusterBuilder.*\n"
    hostLoaderOutText << scriptText[0] + "\n"
    hostLoaderOutText << "import jcsp.lang.*\nimport groovyJCSP.*\nimport jcsp.net2.*\n"
    hostLoaderOutText << "import jcsp.net2.mobile.*\nimport jcsp.net2.tcpip.*\nimport gppClusterBuilder.*\n"
    hostProcessOutText << scriptText[0] + "\n"
    hostProcessOutText << "import jcsp.lang.*\nimport groovyJCSP.*\nimport jcsp.net2.*\n"
    hostProcessOutText << "import jcsp.net2.mobile.*\nimport jcsp.net2.tcpip.*\nimport gppClusterBuilder.*\n"

    println "App Name is $appName"
  } // end of getInputs

  def putOutputs(
    FileWriter nodeLoaderWriter,
    FileWriter nodeProcessWriter,
    FileWriter hostLoaderWriter,
    FileWriter hostProcessWriter  ) {
    // now copy all the OutText to their output file
    nodeLoaderOutText.each { line -> nodeLoaderWriter.write(line)
    }
    nodeLoaderWriter.flush()
    nodeLoaderWriter.close()
    nodeProcessOutText.each { line -> nodeProcessWriter.write(line)
    }
    nodeProcessWriter.flush()
    nodeProcessWriter.close()

    hostLoaderOutText.each { line -> hostLoaderWriter.write(line)
    }
    hostLoaderWriter.flush()
    hostLoaderWriter.close()
    hostProcessOutText.each { line -> hostProcessWriter.write(line)
    }
    hostProcessWriter.flush()
    hostProcessWriter.close()
    println "Transformation Completed $error"
  } // end of putOutputs

  // closures used in file processing

  def extractAppStructure = {
    while (scriptText[scriptCurrentLine] == " ") {
      String line = scriptText[scriptCurrentLine]
      nodeProcessOutText << line + "\n"
      hostProcessOutText << line + "\n"
      scriptCurrentLine++
    }
    while (scriptText[scriptCurrentLine].startsWith("import") || scriptText[scriptCurrentLine] == " ") {
      String line = scriptText[scriptCurrentLine]
      nodeProcessOutText << line + "\n"
      hostProcessOutText << line + "\n"
      scriptCurrentLine++
    }
    // current line is now either //@emit or some constant definitions
    while (!(scriptText[scriptCurrentLine] =~ "//@emit")){
      commonDeclarations <<  scriptText[scriptCurrentLine]
      scriptCurrentLine++
    }

    // now search for //@clusters line in script
    int cLine = scriptCurrentLine
    while ( cLine < scriptText.size()){
      if (scriptText[cLine].startsWith("//@cluster")) findClusterSize(cLine)
      cLine++
    }
    // now find the start and end line of each section in script
    cLine = scriptCurrentLine
    while (!(scriptText[cLine].startsWith("//@emit"))) cLine++
    // host IP is on cLine so extract it
    hostIPText = scriptText[cLine].substring(7).trim()
    println "Host IP is $hostIPText"
    emitStart = cLine + 1
    cLine++
// todo make it so that we can detect more than one cluster
    while (!(scriptText[cLine].startsWith("//@cluster"))) cLine++
    clusterStart = cLine + 1
    emitEnd = cLine - 1
    cLine++

    while (!(scriptText[cLine].startsWith("//@collect"))) cLine++
    clusterEnd = cLine - 1
    collectStart = cLine+1
    collectEnd = scriptText.size() - 1
//    println "Emit: $emitStart, $emitEnd"
//    println "Cluster: $clusterStart, $clusterEnd Size is $clusterSize"
//    println "Collect: $collectStart, $collectEnd"
    // have now collected the separate parts of the script
  } //end of extractAppStructure

  def findClusterSize = { int lineNumber ->
    // cluster size could either be an integer of the name of a
    // previously defined integer variable
    String clusterText = scriptText[lineNumber].substring(11).trim()
    if (clusterText =~ /[0-9]{1,4}/) {
      // integer in range 1 .. 9999
      clusterSize = Integer.parseInt(clusterText)
    }
    else {
      // clusterText must be a variable name so go backwards until
      // a line is found that contains int and clusterText
      // line may be in a comment
      boolean notFound = true
      lineNumber--
      while (notFound) {
        while (!(scriptText[lineNumber] =~ clusterText)  && (lineNumber > 1)) lineNumber--
        if (lineNumber == 0) {
          error += "cannot find definition of $clusterText to give number of clusters"
          clusterSize = -1 // to ensure code will fail!
          notFound = false
        }
        else{
          // clusterText has been found  do some checks
          if (!(scriptText[lineNumber].trim().startsWith("//"))){
            // this is not a comment at start of line
            int equalIndex = scriptText[lineNumber].indexOf("=")
            int l = equalIndex + 1
            while (scriptText[lineNumber][l] == " ") l++
            String clusterString = scriptText[lineNumber][l]
            l++
            while ((l < scriptText[lineNumber].size()) && (scriptText[lineNumber][l] =~ "[0-9]")) {
              clusterString = scriptText[lineNumber][l]
              l++
            }
            clusterSize = Integer.parseInt(clusterString)
            notFound = false
          }
          else {
            lineNumber--
          }

        }
      }
    }
    println "Cluster Size is $clusterSize"
  } // end of findClusterSize

  def processLoaders = {
    // find start of class, replace Basic with appName and copy to nodeLoaderOutText
    while (!(nodeLoaderText[nodeLoaderLine].startsWith("class"))) nodeLoaderLine++
    nodeLoaderOutText << nodeLoaderText[nodeLoaderLine].replace("Basic", appName) + "\n"
    nodeLoaderLine++
    nodeLoaderOutText << nodeLoaderText[nodeLoaderLine] + "\n" // static main method line
    nodeLoaderLine++
    // modify the //@hostIPAddress annotation
    nodeLoaderOutText << nodeLoaderText[nodeLoaderLine].replace(
        "//@hostIPAddress","String hostIP = '$hostIPText' ") + "\n"
    // copy rest of text straight over
    nodeLoaderLine++
    while (nodeLoaderLine < nodeLoaderText.size()){
      nodeLoaderOutText << nodeLoaderText[nodeLoaderLine] + "\n"
      nodeLoaderLine++
    }
    // find start of class, replace Basic with appName and copy to nodeLoaderOutText
    while (!(hostLoaderText[hostLoaderLine].startsWith("class"))) hostLoaderLine++
    hostLoaderOutText << hostLoaderText[hostLoaderLine].replace("Basic", appName) + "\n"
    hostLoaderLine++
    hostLoaderOutText << hostLoaderText[hostLoaderLine] +"\n" // static main method line
    hostLoaderLine++
    // modify the //@nodeSize annotation
    hostLoaderOutText << hostLoaderText[hostLoaderLine].replace(
        "//@nodeSize","int nodes = $clusterSize") + "\n"
    // now copy rest of text replacing two instances of Basic with appName
    hostLoaderLine++
    while (hostLoaderLine < hostLoaderText.size()) {
      if (hostLoaderText[hostLoaderLine] =~ /Basic/)
        hostLoaderOutText << hostLoaderText[hostLoaderLine].replace("Basic", appName) + "\n"
      else
        hostLoaderOutText << hostLoaderText[hostLoaderLine] + "\n"
      hostLoaderLine++
    }
  }

  def completeProcesses = {
    // find start of nodeProcess class, replace Basic with appName and copy to nodeProcessOutText
    while (!(nodeProcessText[nodeProcessLine].startsWith("class"))) nodeProcessLine++
    nodeProcessOutText << nodeProcessText[nodeProcessLine].replace("Basic", appName) + "\n"
    // find the run() method declaration
    nodeProcessLine++
    while (!(nodeProcessText[nodeProcessLine].startsWith("void run"))){
      nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
      nodeProcessLine++
    }
    // copy the run() line and the common declarations
    nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
    commonDeclarations.each { line ->
      nodeProcessOutText << line + "\n"
    }
    // find the //@inputVCNs line and copy to output
    nodeProcessLine++
    while (!(nodeProcessText[nodeProcessLine].startsWith("//@inputVCNs"))){
      nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
      nodeProcessLine++
    }
    nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
    // copy node inputVCNs creation code
    nodeInputInsert.each{line ->
      nodeProcessOutText << line + "\n"
    }

    // find the //@outputVCNs line and copy to output
    nodeProcessLine++
    while (!(nodeProcessText[nodeProcessLine].startsWith("//@outputVCNs"))){
      nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
      nodeProcessLine++
    }
    nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
    // copy node outputVCNs creation code
    nodeOutputInsert.each{line ->
      nodeProcessOutText << line + "\n"
    }

    // find the //@nodeProcess line and copy to output
    nodeProcessLine++
    while (!(nodeProcessText[nodeProcessLine].startsWith("//@nodeProcess"))){
      nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
      nodeProcessLine++
    }
    nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
    // copy node nodeProcess creation code
    nodeProcessInsert.each{line ->
      nodeProcessOutText << line + "\n"
    }

    // now copy rest of nodeProcess text
    nodeProcessLine++
    while (nodeProcessLine < nodeProcessText.size()){
      nodeProcessOutText << nodeProcessText[nodeProcessLine] + "\n"
      nodeProcessLine++
    }

    // now do the similar operation for hostProcess

    // find start of class, replace Basic with appName and copy to nodeLoaderOutText
    while (!(hostProcessText[hostProcessLine].startsWith("class"))) hostProcessLine++
    hostProcessOutText << hostProcessText[hostProcessLine].replace("Basic", appName) + "\n"
    // find the run() method declaration
    hostProcessLine++
    while (!(hostProcessText[hostProcessLine].startsWith("void run"))){
      hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
      hostProcessLine++
    }
    // copy the run() line and the common declarations
    hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
    commonDeclarations.each { line ->
      hostProcessOutText << line + "\n"
    }
    // find the //@inputVCNs line and copy to output
    hostProcessLine++
    while (!(hostProcessText[hostProcessLine].startsWith("//@inputVCNs"))){
      hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
      hostProcessLine++
    }
    hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
    // copy host inputVCNs creation code
    hostNodeInputInsert.each{line ->
      hostProcessOutText << line + "\n"
    }

    // find the //@hostInputs line and copy to output
    hostProcessLine++
    while (!(hostProcessText[hostProcessLine].startsWith("//@hostInputs"))){
      hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
      hostProcessLine++
    }
    hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
    // copy host hostInputs creation code
    hostInputInsert.each{line ->
      hostProcessOutText << line + "\n"
    }

    // find the //@outputVCNs line and copy to output
    hostProcessLine++
    while (!(hostProcessText[hostProcessLine].startsWith("//@outputVCNs"))){
      hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
      hostProcessLine++
    }
    hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
    // copy host outputVCNs creation code
    hostNodeOutputInsert.each{line ->
      hostProcessOutText << line + "\n"
    }

    // find the //@hostOutputs line and copy to output
    hostProcessLine++
    while (!(hostProcessText[hostProcessLine].startsWith("//@hostOutputs"))){
      hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
      hostProcessLine++
    }
    hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
    // copy host hostOutputs creation code
    hostOutputInsert.each{line ->
      hostProcessOutText << line + "\n"
    }

    // find the //@hostProcess line and copy to output
    hostProcessLine++
    while (!(hostProcessText[hostProcessLine].startsWith("//@hostProcess"))){
      hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
      hostProcessLine++
    }
    hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
    // copy host hostProcessChannel creation code
    hostProcessChannelInsert.each{ line ->
      hostProcessOutText << line + "\n"
    }

    // copy host hostProcessPAR creation code
    hostProcessParInsert.each {line ->
      hostProcessOutText << line + "\n"
    }

    // now copy rest of hostProcess text
    hostProcessLine++
    while (hostProcessLine < hostProcessText.size()){
      hostProcessOutText << hostProcessText[hostProcessLine] + "\n"
      hostProcessLine++
    }
  } // end of completeProcesses

  // processes that extract source from script
  // create all the net channel inserts
  // uses VCN ranges and names of net channels
  def createAllNetChannelInserts = {
// range of VCNs used by the net channels
    Range emitRange = 100 ..< 100+clusterSize
    int nodeInVCN = 200
    int collectInVCN = 300
    Range collectInRange = 300 ..< 300+clusterSize   // will be used when collect input is a List

    String hostNodeInputInsertString
    hostNodeInputInsertString = "inputVCNs = [ "
    for (n in 0..<clusterSize - 1) hostNodeInputInsertString += "[$nodeInVCN], "
    hostNodeInputInsertString += "[$nodeInVCN] "
    hostNodeInputInsertString += "]\n"
    hostNodeInputInsert << hostNodeInputInsertString

    String hostInputInsertString
    hostInputInsertString = "ChannelInputList emitRequestList = [] \n"
//    println "Emit in range is $emitRange"
    emitRange.each { e -> hostInputInsertString += "emitRequestList.append(NetChannel.numberedNet2One($e)) \n"
    }
    if ( collectByList){
      hostInputInsertString += "ChannelInputList collectListFromNodes = [] \n"
      collectInRange.each(c -> hostInputInsertString += "collectListFromNodes.append(NetChannel.numberedNet2One($c)) \n")
      collectInputName = "collectListFromNodes"
    }
    else {
      hostInputInsertString += "ChannelInput collectFromNodes = NetChannel.numberedNet2One($collectInVCN) \n"
      collectInputName = "collectFromNodes"
    }
    hostInputInsert << hostInputInsertString
    emitRequestName = "emitRequestList"

    String hostNodeOutputInsertString
    List<String> nodeString = []
    hostNodeOutputInsertString = "outputVCNs = [ "
    if ( collectByList){ // collect uses ListFanOne or ListMergeOne
      for ( c in 0 ..< clusterSize) {
        String s = "[ [hostIP, ${emitRange[c]}], [hostIP, ${collectInRange[c]}] ]"
        nodeString << s
      }
    }
    else {
      // collect uses AnyFanOne
      emitRange.each { e ->
        String s = "[ [hostIP, $e], [hostIP, $collectInVCN] ]"
        nodeString << s
      }
    }
    for (n in 0..<clusterSize - 1) hostNodeOutputInsertString += nodeString[n] + ", "
    hostNodeOutputInsertString += nodeString[clusterSize - 1] + " ]\n"
    hostNodeOutputInsert << hostNodeOutputInsertString

    String hostOutputInsertString
    hostOutputInsertString = "ChannelOutputList emitResponseList = [] \n"
    for (i in 0..<clusterSize) {
      hostOutputInsertString += "emitResponseList.append(NetChannel.one2net(new TCPIPNodeAddress(nodeIPs[$i], 1000), 200)) \n"
    }
    hostOutputInsert << hostOutputInsertString
    emitResponseName = "emitResponseList"

    String nodeInputInsertString = "ChannelInput nodeFromEmit = NetChannel.numberedNet2One(inputVCNs[0])\n"
    nodeInputInsert << nodeInputInsertString
    nodeResponseName = "nodeFromEmit"

    String nodeOutputInsertString
    nodeOutputInsertString = "ChannelOutput node2emit = NetChannel.one2net" + "(new TCPIPNodeAddress(outputVCNs[0][0], 2000), outputVCNs[0][1])\n"
    nodeOutputInsertString += "ChannelOutput node2collect = NetChannel.one2net" + "(new TCPIPNodeAddress(outputVCNs[1][0], 2000), outputVCNs[1][1])\n"
    nodeOutputInsert << nodeOutputInsertString
    nodeRequestName = "node2emit"
    nodeOutputName = "node2collect"
  }

  // extract emit inserts
  def createHostProcessEmitInserts = {
    scriptCurrentLine = emitStart
    while (!(scriptText[scriptCurrentLine] =~ /Emit/)){
      preNetwork += scriptText[scriptCurrentLine] +"\n"
      scriptCurrentLine++
    }
    // should now have emit process definition
    boolean processing = true
    while (processing) {
      List rvs = findProcDef(scriptCurrentLine)
      if (rvs == null) break
      endLine = rvs[0]
      String processName = rvs[1]
      hostProcessNames << rvs[2]
      "$processName"(processName, scriptCurrentLine, endLine)
      scriptCurrentLine = endLine + 1
      if ( !findNextProc(emitEnd) ) processing = false
    }
    hostProcessChannelInsert << preNetwork + "\n"
    hostProcessParInsert << network + "\n"
    network = ""
    preNetwork = ""
  }
  // extract cluster inserts
  def createClusterProcessInserts = {
    scriptCurrentLine = clusterStart
    while (!(scriptText[scriptCurrentLine] =~ /Requesting/)){
      preNetwork += scriptText[scriptCurrentLine] +"\n"
      scriptCurrentLine++
    }
    // should now have cluster process definition
    boolean processing = true
    while (processing) {
      List rvs = findProcDef(scriptCurrentLine)
      if (rvs == null) break
      endLine = rvs[0]
      String processName = rvs[1]
      nodeProcessNames << rvs[2]
      if (scriptCurrentLine == clusterLastProc)
        "outNet$processName"(processName, scriptCurrentLine, endLine)
      else
        "$processName"(processName, scriptCurrentLine, endLine)
      scriptCurrentLine = endLine + 1
      if ( !findNextProc(clusterEnd) ) processing = false
    }
    nodeProcessInsert << preNetwork + "\n"
    nodeProcessInsert << network + "\n"
    nodeProcessInsert << "\nnew PAR($nodeProcessNames).run()\n"
    network = ""
    preNetwork = ""
  } // end of createClusterProcessInserts

  // extract collect process inserts
  def createHostProcessCollectInserts = {
    scriptCurrentLine = collectStart
    while (!(scriptText[scriptCurrentLine] =~ /One/)){  // excludes N-WayMerge as first reducer in Collect
      preNetwork += scriptText[scriptCurrentLine] +"\n"
      scriptCurrentLine++
    }
    // should now have collect process definition
    boolean processing = true
    while (processing) {
      List rvs = findProcDef(scriptCurrentLine)
      if (rvs == null) break
      endLine = rvs[0]
      String processName = rvs[1]
      hostProcessNames << rvs[2]
      if ( scriptCurrentLine == collectFirstProc)
        "inNet$processName"(processName, scriptCurrentLine, endLine)
      else
        "$processName"(processName, scriptCurrentLine, endLine)
      scriptCurrentLine = endLine + 1
      if ( !findNextProc(collectEnd) ) processing = false
    }
    hostProcessChannelInsert << preNetwork + "\n"
    hostProcessParInsert << network + "\n"
    network = ""
    preNetwork = ""

  // add PAR to host process PAR statement
    hostProcessParInsert << "\nnew PAR($hostProcessNames).run()\n"

  } //end of createHostProcessCollectInserts

  // extract key process line numbers
  def extractFirstLastProcs = {
    int scriptCurrentLine = emitStart
    while (!(scriptText[scriptCurrentLine] =~ /Emit/)) scriptCurrentLine++
    while (scriptCurrentLine <= emitEnd) {
      if (scriptText[scriptCurrentLine] =~ /new/) emitLastProc = scriptCurrentLine
      scriptCurrentLine++
    }
    scriptCurrentLine = clusterStart
    while (!(scriptText[scriptCurrentLine] =~ /new/)) scriptCurrentLine++
    clusterFirstProc = scriptCurrentLine
    while (scriptCurrentLine <= clusterEnd) {
      if (scriptText[scriptCurrentLine] =~ /new/) clusterLastProc = scriptCurrentLine
      scriptCurrentLine++
    }
    scriptCurrentLine = collectStart
    while (!(scriptText[scriptCurrentLine] =~ /Any/)  &&
        !(scriptText[scriptCurrentLine] =~ /List/)) scriptCurrentLine++
    collectFirstProc = scriptCurrentLine
    if (scriptText[scriptCurrentLine] =~ /List/ ) collectByList = true
//    println "collect: $scriptCurrentLine, $collectByList, ${scriptText[scriptCurrentLine]}"
  } // end of extractFirstLastProcs

  // channel processing closures
  def swapChannelNames = { ChanTypeEnum expected ->
    currentInChanName = currentOutChanName
    chanNumber += 1
    currentOutChanName = "chan$chanNumber"
    expectedInChan = expected
  } //end of swapChannelNames

  def confirmChannel = { String pName, ChanTypeEnum actualInChanType ->
    if (expectedInChan != actualInChanType) {
      network += "Expected a process with a *$expectedInChan* type input  found $pName with type $actualInChanType \n"
      error += " with errors, see the parsed output file"
    }
  } // end of confirmChannel

  def nextProcSpan = { start ->
    int beginning = start
    while (!(scriptText[beginning] =~ /new/)) beginning++
    int ending = beginning
    while (!scriptText[ending].endsWith(")")) ending++
    return [beginning, ending]
  } //end of nextProcSpan

  def scanChanSize = { List l ->
    int line
    line = -1  // just to make sure it has a value
    for (i in (int) l[0]..(int) l[1]) {
      if ((scriptText[i] =~ /workers/) || (scriptText[i] =~ /mappers/) || (scriptText[i] =~ /reducers/) || (scriptText[i] =~ /groups/)) {
        line = i
        break
      }
    }
    // we now know we have found the right line
    int colon = scriptText[line].indexOf(":") + 1
    int end = scriptText[line].indexOf(",")
    if (end == -1) end = scriptText[line].indexOf(")")
//		println "$line, ${scriptText[line]}, $colon, $end"
    if (end != -1) {
      chanSize = scriptText[line].subSequence(colon, end).trim()
      return chanSize
    } else return null
  } // end of scanChanSize

  // closure to find a process def assuming start is the index of a line containing such a def
  def findProcDef = { int start ->
    int ending = start
    while (!scriptText[ending].endsWith(")")) ending++
    int startIndex = scriptText[start].indexOf("new") + 4
    int endIndex = scriptText[start].indexOf("(")
    if (startIndex == -1 || endIndex == -1) {
      error += "string *new* found in an unexpected place\n${scriptText[scriptCurrentLine]}\n"
      network += error
      return null
    } else {
      String processName = scriptText[start].subSequence(startIndex, endIndex).trim()
      startIndex = scriptText[start].indexOf("def") + 4
      endIndex = scriptText[start].indexOf("=")
      String procName = scriptText[start].subSequence(startIndex, endIndex)
      return [ending, processName, procName]
    }
  } // end of findProcDef

  // closure to find the next process definition before scriptText[limit]
  def findNextProc = { int limit ->
    scriptCurrentLine = endLine + 1
    boolean processing = scriptCurrentLine <= limit
    while (!(scriptText[scriptCurrentLine] =~ /new/) && processing) {
      network += scriptText[scriptCurrentLine] + "\n" // add blank and comment lines
      scriptCurrentLine++
      processing = scriptCurrentLine <= limit
    }
    return processing
  }  // end of findNextProc

  def extractProcDefParts = { int line ->
    int len = scriptText[line].size()
    int openParen = scriptText[line].indexOf("(")
    int closeParen = scriptText[line].indexOf(")")  // could be -1
    String initialDef = scriptText[line].subSequence(0, openParen + 1) // includes the (
    String remLine = null
    String firstProperty = null
    if (closeParen > 0) {
      // single line definition
      remLine = scriptText[line].subSequence(openParen + 1, closeParen + 1).trim()
    } else {
      //multi line definition
      if (openParen == (len - 1)) firstProperty = " " // no property specified
      else firstProperty = scriptText[line].subSequence(openParen + 1, len).trim()
    }
    return [initialDef, remLine, firstProperty]    // known as rvs subsequently
  } // end of extractProcDefParts

  def copyProcProperties = { List rvs, int starting, int ending ->
    if (rvs[2] == null) network += "    ${rvs[1]}\n" else {
      if (rvs[2] != " ") network += "    ${rvs[2]}\n"
      for (i in starting + 1..ending) network += "    " + scriptText[i] + "\n"
    }
  } // end of copyProcProperties

  def checkNoProperties = { List rvs ->
    if (rvs[1] != ")") {
      error += "expecting a closing ) on same line  but not found\n"
      network += error
    }
  } // end of checkNoProperties

  /**
   * Define a set of closures that process common combinations
   * of input and output channels some of which are left in line as they are unique
   *  eg Connectors: AnyFanOne, AnyFanAny and OneFanAny
   *  and Groups: AnyGroupList ListGroupAny plus OnePipelineOne and OnePipelineCollect
   */

  def patternProcess = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    pattern = true
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    copyProcProperties(rvs, starting, ending)
  }

  def oneOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  } // end of OneOne

  def oneList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    outputList: ${currentOutChanName}OutList )\n"
    checkNoProperties(rvs)
    rvs = nextProcSpan(ending + 2)
    String returnedChanSize = scanChanSize(rvs)
    if (returnedChanSize != null) chanSize = returnedChanSize
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
    swapChannelNames(ChanTypeEnum.list)
  } // end of oneList

  def oneListPlus = { String processName, int starting, int ending ->
    // needed because some oneList spreaders have properties
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    outputList: ${currentOutChanName}OutList,\n"
    copyProcProperties(rvs, starting, ending)
    rvs = nextProcSpan(ending + 2)
    String returnedChanSize = scanChanSize(rvs)
    if (returnedChanSize != null) chanSize = returnedChanSize
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
    swapChannelNames(ChanTypeEnum.list)
  }  // end of oneListPlus


  def listOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    checkNoProperties(rvs)
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  } //end of ListOne

  def noneOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    // input channel not required\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  }  //end of noneOne

  def oneNone = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)
  }// end of oneNone

  def listListGroup = { String processName, int starting, int ending, String type, String size ->
    // type is used only for logging to ensure correct shape is produced
//		println "$processName: $starting, $ending, $type, $size"
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
    network += "    outputList: ${currentOutChanName}OutList,\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
    swapChannelNames(ChanTypeEnum.list)
  } // end of listListgroup

  def anyAnyGroup = { String processName, int starting, int ending, String type, String size ->
//		println "$processName: $starting, $ending, $type $size"
    confirmChannel(processName, ChanTypeEnum.any)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${currentInChanName}.in(),\n"
    network += "    outputAny: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.any2any()\n"
    swapChannelNames(ChanTypeEnum.any)
  } // end of anyAnyGroup

  def listNoneGroup = { String processName, int starting, int ending, String type, String size ->
//		println "$processName: $starting, $ending, $type, $size"
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)
  } // end of listNoneGroup

  def anyNoneGroup = { String processName, int starting, int ending, String type , String size->
//		println "$processName: $starting, $ending, $type, $size"
    confirmChannel(processName, ChanTypeEnum.any)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${currentInChanName}.in(),\n"
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)
  } // end of anyNoneGroup

//
// define the closures for each process type in the library
//
// cluster connectors and processes
  def NodeRequestingFanAny = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
//    println "request ouput is $nodeRequestName"
//    println "response input is $nodeResponseName"
//    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    request: ${nodeRequestName},\n"
    network += "    response: ${nodeResponseName},\n"
    network += "    outputAny: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
    swapChannelNames(ChanTypeEnum.any)  }

  def requestingList = { String processName, int starting, int ending ->
//    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    request: ${nodeRequestName},\n"
    network += "    response: ${nodeResponseName},\n"
    network += "    outList: ${currentOutChanName}OutList )\n"
    checkNoProperties(rvs)
    rvs = nextProcSpan(ending + 2)
    String returnedChanSize = scanChanSize(rvs)
    if (returnedChanSize != null)
      chanSize = returnedChanSize
    else
      error = error + "chan size for $processName is null"
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
    swapChannelNames(ChanTypeEnum.list)
  }

  def NodeRequestingFanList = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    requestingList(processName, starting, ending)
  }

  def NodeRequestingParCastList = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    requestingList(processName, starting, ending)
  }

  def NodeRequestingSeqCastList = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    requestingList(processName, starting, ending)
  }

  def OneNodeRequestedList = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
//    println "request list name is $emitRequestName"
//    println "response list name is $emitResponseName"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    request: ${emitRequestName},\n"
    network += "    response: ${emitResponseName},\n"
    copyProcProperties(rvs, starting, ending)
  }

  def OneNodeRequestedCastList = { String processName, int starting, int ending ->
    OneNodeRequestedList(processName, starting, ending)
  }

  // List2Net and NetInputs2List processes
  /*
  The intended use of these processes is that each Cluster will output using a List2Net process
  netChannelOutputList having the same size as the number of workers in preceding ListGroupList.

  Conversely a NetInputs2List process will input using clusters x workers netChannelInputList,
  which can then be processed by a following ListFanOne ListMergeOne or ListGroupCollect.

  It is anticipated that it will be better to do the merging of output streams in the
  Collect process, which is essentially waiting while the clusters are busy!

  This supposition may have to change if we permit a sequence of clusters with data
  passed over cluster netChannelList instances by use of a different process, say Net2List.
   */
//todo
  def outNetList2Net = { String processName, int starting, int ending ->
    println "$processName: $starting, $ending"
    // copied from ListListGroup needs modifying to include chanoutname
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
    network += "    outputList: ${currentOutChanName}OutList,\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
    swapChannelNames(ChanTypeEnum.list)

  }

  def inNetNetInputs2List = { String processName, int starting, int ending ->
    println "$processName: $starting, $ending"
  }

  // net specialisations of pre-existing spreaders and reducers
  def inNetAnyFanOne = { String processName, int starting, int ending ->
//    println "inNet$processName: $starting, $ending"
//    println "net input name is $collectInputName"
//    confirmChannel(processName, ChanTypeEnum.any)  // cannot do this confirmation
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${collectInputName},\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  }

  def inNetListOne = {String processName, int starting, int ending ->
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${collectInputName},\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  }

  def inNetListFanOne = {String processName, int starting, int ending ->
//    println "inNet$processName: $starting, $ending"
    inNetListOne(processName, starting, ending)
  }

  def inNetListMergeOne = {String processName, int starting, int ending ->
//    println "inNet$processName: $starting, $ending"
    inNetListOne(processName, starting, ending)
  }

  def outNetAnyFanOne = { String processName, int starting, int ending ->
//    println "outNet$processName: $starting, $ending"
//    println "net output name is $nodeOutputName"
    confirmChannel(processName, ChanTypeEnum.any)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${currentInChanName}.in(),\n"
    network += "    output: ${nodeOutputName},\n"
    copyProcProperties(rvs, starting, ending)
//    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  }

  def outNetListOne = { String processName, int starting, int ending ->
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
    network += "    output: ${nodeOutputName},\n"
    copyProcProperties(rvs, starting, ending)
    swapChannelNames(ChanTypeEnum.one)
  }

  def outNetListFanOne = {  String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    outNetListOne(processName, starting, ending)
  }

  def outNetListMergeOne = {  String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    outNetListOne(processName, starting, ending)
  }

  def outNetListParOne = {  String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    outNetListOne(processName, starting, ending)
  }

  def outNetListSeqOne = {  String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    outNetListOne(processName, starting, ending)
  }
//  connectors not networked
// reducers
  def AnyFanOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.any)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${currentInChanName}.in(),\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  }  // end of AnyFanOne

  def ListFanOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listOne(processName, starting, ending)
  }

  def ListMergeOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listOne(processName, starting, ending)
  }

  def ListParOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listOne(processName, starting, ending)
  }

  def ListSeqOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listOne(processName, starting, ending)
  }

  def N_WayMerge = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    // cannot be listOne because it is expecting properties
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
//    //SH added modified by JMK
//    if (logging) {
//      network += "\n    //gppVis command\n"
//      network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.REDUCER)) \n"
//    }
  } // end of N_WayMerge


// spreaders
  def AnyFanAny = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.any)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${currentInChanName}.in(),\n"
    network += "    outputAny: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
    swapChannelNames(ChanTypeEnum.any)
  } //end of AnyFanAny

  def OneFanAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    outputAny: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
    swapChannelNames(ChanTypeEnum.any)
  }// end of OneFanAny

  def OneDirectedList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneListPlus(processName, starting, ending)
  } //end of OneDirectedList

  def OneFanList = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
    oneList(processName, starting, ending)
  } //end of OneFanList

  def OneIndexedList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneListPlus(processName, starting, ending)
  }

  def OneParCastList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneList(processName, starting, ending)
  }

  def OneSeqCastList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneList(processName, starting, ending)
  }

  def TaskParallelPattern = { String processName, int starting, int ending ->
    patternProcess ( processName, starting, ending )
  }

  def DataParallelPattern = { String processName, int starting, int ending ->
    patternProcess ( processName, starting, ending )
  }

  def PipelineOfGroupsPattern = { String processName, int starting, int ending ->
    patternProcess ( processName, starting, ending )
  }

  def GroupOfPipelinesPattern = { String processName, int starting, int ending ->
    patternProcess ( processName, starting, ending )
  }

  def PipelineOfGroupCollectPattern = { String processName, int starting, int ending ->
    patternProcess ( processName, starting, ending )
  }

  def GroupOfPipelineCollectPattern = { String processName, int starting, int ending ->
    patternProcess ( processName, starting, ending )
  }

// composites
  def AnyGroupOfPipelineCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    anyNoneGroup(processName, starting, ending, "GoP", "groups")
  } // end of AnyGroupOfPipelineCollects

  def AnyGroupOfPipelines = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    anyAnyGroup(processName, starting, ending,"GoP", "groups")
  } //end of AnyGroupOfPipelines

  def AnyPipelineOfGroupCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    anyNoneGroup(processName, starting, ending, "PoG", "workers")
  } //end of AnyPipelineOfGroupCollects

  def AnyPipelineOfGroups = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    anyAnyGroup(processName, starting, ending, "PoG", "workers")
  } // end of AnyPipelineOfGroups

  def ListGroupOfPipelineCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listNoneGroup(processName, starting, ending, "GoP", "groups")
  } // end of ListGroupOfPipelineCollects

  def ListGroupOfPipelines = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listListGroup(processName, starting, ending, "GoP", "groups")
  } // end of ListGroupOfPipelines

  def ListPipelineOfGroups = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listListGroup(processName, starting, ending, "PoG", "workers")
  } // end of ListPipelineOfGroups

  def ListPipelineOfGroupCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listNoneGroup(processName, starting, ending, "PoG", "workers")
  } //end of ListPipelineOfGroupCollects


// groups
  def AnyGroupAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    anyAnyGroup(processName, starting, ending, "Group", "workers")
  } //end of AnyGroupAny

  def AnyGroupCollect = { String processName, int starting, int ending ->
//      println "$processName: $starting, $ending"
    anyNoneGroup(processName, starting, ending, "Group", "collectors")
  } // end of AnyGroupCollect

  def AnyGroupList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.any)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${currentInChanName}.in(),\n"
    network += "    outputList: ${currentOutChanName}OutList,\n"
    copyProcProperties(rvs, starting, ending)
    rvs = nextProcSpan(ending + 2)
    String returnedChanSize = scanChanSize(rvs)
    if (returnedChanSize != null) chanSize = returnedChanSize
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
    preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
    swapChannelNames(ChanTypeEnum.list)
  } //end of AnyGroupList

  def ListGroupAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
    network += "    outputAny: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    rvs = nextProcSpan(ending + 2)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.any2any()\n"
    swapChannelNames(ChanTypeEnum.any)
  } // end of ListGroupAny

  def ListGroupCollect = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listNoneGroup(processName, starting, ending, "Group", "collectors")
  } // end of ListGroupCollect

  def ListGroupList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    listListGroup(processName, starting, ending, "Group","workers")
  }  //end of ListGroupList


  def ListThreePhaseWorkerList = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
    listListGroup(processName, starting, ending, "Group", "workers")
  } //end ListThreePhaseWorkerList

//matrix
  def MultiCoreEngine = { String processName, int starting, int ending ->
//      println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  }  // end of MultiCoreEngine

  def StencilEngine = { String processName, int starting, int ending ->
    MultiCoreEngine(processName, starting, ending)
  }  // end of StencilEngine

// pipelines
  def OnePipelineCollect = { String processName, int starting, int ending ->
    //println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)
  } // end of OnePipelineCollect

  def OnePipelineOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
    network += "    output: ${currentOutChanName}.out(),\n"
    copyProcProperties(rvs, starting, ending)
    preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
    swapChannelNames(ChanTypeEnum.one)
  } // end of OnePipelineOne

// terminals
  def Collect = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
    oneNone (processName, starting, ending)
  }

  def CollectUI = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
    oneNone (processName, starting, ending)
  }

  def Emit = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    noneOne(processName, starting, ending)
  }

  def EmitFromInput = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneOne(processName, starting, ending)
  }
  def EmitSingle = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    noneOne(processName, starting, ending)
  }

  def EmitWithLocal = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
    noneOne(processName, starting, ending)
  } //end of Emit with Local

  def TestPoint = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
    oneNone(processName, starting, ending)
  }

// transformers
  def CombineNto1 = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneOne(processName, starting, ending)
  }  //end of CombineNto1

// workers
  def ThreePhaseWorker = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneOne(processName, starting, ending)
  }  //end of ThreePhaseWorker

  def Worker = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    oneOne(processName, starting, ending)
  } //end of worker

} // end of class definition
