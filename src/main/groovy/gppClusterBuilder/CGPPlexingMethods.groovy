package gppClusterBuilder

import GPP_Builder.ChanTypeEnum

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

  // source scripts
  List<String> scriptText = []
  List <String> nodeLoaderText = []
  List <String> nodeProcessText = []
  List <String> hostLoaderText = []
  List <String> hostProcessText = []

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

  // extracted value from input script
  List <String> commonDeclarations = ["\n"]

  int emitStart, emitEnd, collectStart, collectEnd, clusterStart, clusterEnd, clusterSize

  Range emitInRange, nodeInRange, collectInRange

  // resultants scripts
  List <String> nodeLoaderOutText = []
  List <String> nodeProcessOutText = []
  List <String> hostLoaderOutText = []
  List <String> hostProcessOutText = []

  String appName

  int scriptCurrentLine = 1
  int nodeLoaderLine = 1
  int nodeProcessLine = 1
  int hostLoaderLine = 1
  int hostProcessLine = 1
  int endLine = 0

  boolean pattern = false
//  boolean logging = false

  // file inputs and outputs

  def getInputs(
      FileReader scriptReader,
      FileReader nodeLoaderReader,
      FileReader nodeProcessReader,
      FileReader hostLoaderReader,
      FileReader hostProcessReader,
      String appName) {
    this.appName = appName
    scriptReader.each { String line ->
      if (line.size() == 0) line = " " else line = line.trim()
      scriptText << line
    }
    scriptReader.close()

    nodeLoaderReader.each { String line ->
      if (line.size() == 0) line = " " else line = line.trim()
      nodeLoaderText << line
    }
    nodeLoaderReader.close()

    nodeProcessReader.each { String line ->
      if (line.size() == 0) line = " " else line = line.trim()
      nodeProcessText << line
    }
    nodeProcessReader.close()

    hostLoaderReader.each { String line ->
      if (line.size() == 0) line = " " else line = line.trim()
      hostLoaderText << line
    }
    hostLoaderReader.close()

    hostProcessReader.each { String line ->
      if (line.size() == 0) line = " " else line = line.trim()
      hostProcessText << line
    }
    hostProcessReader.close()

    // copy package line and jcsp imports to output script texts
    nodeLoaderOutText << scriptText[0] + "\n\n"
    nodeLoaderOutText << "import jcsp.lang.*\nimport groovyJCSP.*\nimport jcsp.net2.*\n"
    nodeLoaderOutText << "import jcsp.net2.mobile.*\nimport jcsp.net2.tcpip.*\nimport gppClusterBuilder.*\n"
    nodeProcessOutText << scriptText[0] + "\n\n"
    nodeProcessOutText << "import jcsp.lang.*\nimport groovyJCSP.*\nimport jcsp.net2.*\n"
    nodeProcessOutText << "import jcsp.net2.mobile.*\nimport jcsp.net2.tcpip.*\nimport gppClusterBuilder.*\n"
    hostLoaderOutText << scriptText[0] + "\n\n"
    hostLoaderOutText << "import jcsp.lang.*\nimport groovyJCSP.*\nimport jcsp.net2.*\n"
    hostLoaderOutText << "import jcsp.net2.mobile.*\nimport jcsp.net2.tcpip.*\nimport gppClusterBuilder.*\n"
    hostProcessOutText << scriptText[0] + "\n\n"
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

    //TODO modify for only one cluster
    // now search for //@clusters line in script
    int cLine = scriptCurrentLine
    while ( cLine < scriptText.size()){
      if (scriptText[cLine].startsWith("//@cluster")){
        clusterSize = Integer.parseInt(scriptText[cLine].substring(11))
      }
      cLine++
    }
    // now find the start and end line of each section in script
    cLine = scriptCurrentLine
    while (!(scriptText[cLine].startsWith("//@emit"))) cLine++
    emitStart = cLine + 1
    cLine++

    //TODO modify for only one cluster
    while (!(scriptText[cLine].startsWith("//@cluster"))) cLine++
    clusterStart = cLine + 1
    emitEnd = cLine - 1
    cLine++

    while (!(scriptText[cLine].startsWith("//@collect"))) cLine++
    clusterEnd = cLine - 1
    collectStart = cLine+1
    collectEnd = scriptText.size() - 1
    println "Emit: $emitStart, $emitEnd"
    println "Cluster: $clusterStart, $clusterEnd Size is $clusterSize"
    println "Collect: $collectStart, $collectEnd"
    // have now collected the separate parts of the script including multiple clusters
  } //end of processImports

  def processLoaders = {
    // find start of class, replace Basic with appName and copy to nodeLoaderOutText
    while (!(nodeLoaderText[nodeLoaderLine].startsWith("class"))) nodeLoaderLine++
    nodeLoaderOutText << nodeLoaderText[nodeLoaderLine].replace("Basic", appName) + "\n"
    // copy rest of text straight over
    nodeLoaderLine++
    while (nodeLoaderLine < nodeLoaderText.size()){
      nodeLoaderOutText << nodeLoaderText[nodeLoaderLine] + "\n"
      nodeLoaderLine++
    }
    // find start of class, replace Basic with appName and copy to nodeLoaderOutText
    while (!(hostLoaderText[hostLoaderLine].startsWith("class"))) hostLoaderLine++
    hostLoaderOutText << hostLoaderText[hostLoaderLine].replace("Basic", appName) + "\n"
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
    // TODO copy node inputVCNs creation code
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
    // TODO copy node outputVCNs creation code
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
    // TODO copy node nodeProcess creation code
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
    // TODO copy host inputVCNs creation code
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
    // TODO copy host hostInputs creation code
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
    // TODO copy host outputVCNs creation code
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
    // TODO copy host hostOutputs creation code
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
    // TODO copy host hostProcessChannel creation code
    hostProcessChannelInsert.each{ line ->
      hostProcessOutText << line + "\n"
    }

    // TODO copy host hostProcessPAR creation code
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

  //TODO extract emit inserts
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
  //TODO extract cluster inserts
  def createClusterProcessInserts = {
    scriptCurrentLine = clusterStart
    while (!(scriptText[scriptCurrentLine] =~ /Requesting/)){
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
      nodeProcessNames << rvs[2]
      "$processName"(processName, scriptCurrentLine, endLine)
      scriptCurrentLine = endLine + 1
      if ( !findNextProc(clusterEnd) ) processing = false
    }
    nodeProcessInsert << preNetwork + "\n"
    nodeProcessInsert << network + "\n"
    nodeProcessInsert << "\nnew PAR($nodeProcessNames).run()\n"
    network = ""
    preNetwork = ""
  }
  //TODO extract collect process inserts
  def createHostProcessCollectInserts = {
    scriptCurrentLine = collectStart
    while (!(scriptText[scriptCurrentLine] =~ /One/)){  // excludes N-WayMerge as first reducer in Collect
      preNetwork += scriptText[scriptCurrentLine] +"\n"
      scriptCurrentLine++
    }
    // should now have reducer process definition
    boolean processing = true
    while (processing) {
      List rvs = findProcDef(scriptCurrentLine)
      if (rvs == null) break
      endLine = rvs[0]
      String processName = rvs[1]
      hostProcessNames << rvs[2]
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
  }

  // channel processing closures
  def swapChannelNames = { ChanTypeEnum expected ->
    currentInChanName = currentOutChanName
    chanNumber += 1
    currentOutChanName = "chan$chanNumber"
    expectedInChan = expected
  }

  def confirmChannel = { String pName, ChanTypeEnum actualInChanType ->
    if (expectedInChan != actualInChanType) {
      network += "Expected a process with a *$expectedInChan* type input  found $pName with type $actualInChanType \n"
      error += " with errors, see the parsed output file"
    }
  }

  def nextProcSpan = { start ->
    int beginning = start
    while (!(scriptText[beginning] =~ /new/)) beginning++
    int ending = beginning
    while (!scriptText[ending].endsWith(")")) ending++
    return [beginning, ending]
  }

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
  }

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
  }
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
  }

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
  }

  def copyProcProperties = { List rvs, int starting, int ending ->
    if (rvs[2] == null) network += "    ${rvs[1]}\n" else {
      if (rvs[2] != " ") network += "    ${rvs[2]}\n"
      for (i in starting + 1..ending) network += "    " + scriptText[i] + "\n"
    }
  }

  def checkNoProperties = { List rvs ->
    if (rvs[1] != ")") {
      error += "expecting a closing ) on same line  but not found\n"
      network += error
    }
  }

//  def getLogData = { int starting, String repeatWord ->
//    String repeats = null   // only null when process is not repeated
//    String phaseName = null // only null if logPhaseName(s) not found
//    int scriptCurrentLine
//    scriptCurrentLine = starting
//    if (repeatWord != null) {
//      // looking for repeatWord
//      while (!(scriptText[scriptCurrentLine] =~ repeatWord)) {
//        scriptCurrentLine++
//      }
//      int colon = scriptText[scriptCurrentLine].indexOf(":") + 1
//      int endLine = scriptText[scriptCurrentLine].indexOf(",")
//      if (endLine == -1) endLine = scriptText[scriptCurrentLine].indexOf(")")
//      if (endLine != -1) repeats = scriptText[scriptCurrentLine].subSequence(colon, endLine).trim()
//    }
//    scriptCurrentLine = starting
//    // look for a line containing logPhaseName(s)
//    while (!(scriptText[scriptCurrentLine] =~ /logPhaseName/)) {
//      scriptCurrentLine++
//    }
//    if (scriptText[scriptCurrentLine] =~ /logPhaseNames/) {
//      // looking for a list of names,  logPhaseNames: [ "phase1", ... , "phase-n" ]
//      int startBracket = scriptText[scriptCurrentLine].indexOf("[") + 1
//      int endBracket = scriptText[scriptCurrentLine].indexOf("]")
//      phaseName = scriptText[scriptCurrentLine].subSequence(startBracket, endBracket)  // remove [ ]
//    } else {
//      // looking for a single name, we have a logPhaseName: "phase"
//      int colon = scriptText[scriptCurrentLine].indexOf(":") + 1
//      int endLine = scriptText[scriptCurrentLine].indexOf(",")
//      if (endLine == -1) endLine = scriptText[scriptCurrentLine].indexOf(")")
//      if (endLine != -1) {
//        // will include quotes if a string constant
//        phaseName = scriptText[scriptCurrentLine].subSequence(colon, endLine).trim()
//        // now remove the quote marks around word
////        phaseName = phaseName.subSequence(1, phaseName.length() - 1)
//      }
//    }
//    if ((repeats == null)&&(phaseName == null)){
//      println " GPP_Builder logging specification inconsistency; check all required elements are present"
//    }
//    return [repeats, phaseName]
//  }

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

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, null)
////      println "oneOne: $returned"
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName :  LogPhaseName not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorker(" + returned[1] + ")) \n"
//      }
//    }
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

//    //SH added
//    if (logging) {
//      network += "\n    //gppVis command\n"
//      network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER)) \n"
//    }
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
//    //SH added modified by JMK
//    if (logging) {
//      network += "\n    //gppVis command\n"
//      network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.REDUCER)) \n"
//    }
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

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, null)
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : LogPhaseName not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorker(" + returned[1] + ")) \n"
//      }
//    }
  }

  def oneNone = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
    confirmChannel(processName, ChanTypeEnum.one)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    input: ${currentInChanName}.in(),\n"
//    if (logging) network += logChanAdd
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, null)
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : LogPhaseName not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorker(" + returned[1] + ")) \n"
//      }
//    }

  }

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

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, size)
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
//      }
//    }
  }

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
//    println "network so far:\n $network"

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, size)
////      println "aAG: $returned"
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
//      }
//    }
  }

  def listNoneGroup = { String processName, int starting, int ending, String type, String size ->
//		println "$processName: $starting, $ending, $type, $size"
    confirmChannel(processName, ChanTypeEnum.list)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputList: ${currentInChanName}InList,\n"
//    if (logging) network += logChanAdd
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)

    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, size)
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
//      }
//    }
  }

  def anyNoneGroup = { String processName, int starting, int ending, String type , String size->
//		println "$processName: $starting, $ending, $type, $size"
    confirmChannel(processName, ChanTypeEnum.any)
    def rvs = extractProcDefParts(starting)
    network += rvs[0] + "\n"
    network += "    inputAny: ${currentInChanName}.in(),\n"
//    if (logging) network += logChanAdd
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, size)
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
//      }
//    }
  }

//
// define the closures for each process type in the library
//
// cluster connectors
  def NodeRequestingFanAny = { String processName, int starting, int ending ->
    println "$processName: $starting, $ending"
  }

  def NodeRequestingFanList = { String processName, int starting, int ending ->
    println "$processName: $starting, $ending"
  }

  def NodeRequestingParCastList = { String processName, int starting, int ending ->
    println "$processName: $starting, $ending"
  }

  def NodeRequestingSeqCastList = { String processName, int starting, int ending ->
    println "$processName: $starting, $ending"
  }

  def OneNodeRequestedList = { String processName, int starting, int ending ->
    println "$processName: $starting, $ending"
  }

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

//    //SH added modified by JMK
//    if (logging) {
//      network += "\n    //gppVis command\n"
//      network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.REDUCER)) \n"
//    }
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

//    //SH added modified by JMK
//    if (logging) {
//      network += "\n    //gppVis command\n"
//      network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER)) \n"
//    }
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

//    //SH added modified by JMK
//    if (logging) {
//      network += "\n    //gppVis command\n"
//      network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER)) \n"
//    }
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

// patterns
//  def DataParallelCollect = { String processName, int starting, int ending ->
////			println "$processName: $starting, $ending"
//    pattern = true
//    def rvs = extractProcDefParts(starting)
//    network += rvs[0] + "\n"
//    copyProcProperties(rvs, starting, ending)
//  } // end of DataParallelCollect
//
//  def TaskParallelCollect = { String processName, int starting, int ending ->
////			println "$processName: $starting, $ending"
//    pattern = true
//    def rvs = extractProcDefParts(starting)
//    network += rvs[0] + "\n"
//    copyProcProperties(rvs, starting, ending)
//  } // end of TaskParallelCollect
//
//  def TaskParallelOfGroupCollects = { String processName, int starting, int ending ->
////			println "$processName: $starting, $ending"
//    pattern = true
//    def rvs = extractProcDefParts(starting)
//    network += rvs[0] + "\n"
//    copyProcProperties(rvs, starting, ending)
//  } // end of TaskParallelOfGroupCollects

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

//// evolutionary
//
//  def ParallelClientServerEngine = { String processName, int starting, int ending ->
////          println "$processName: $starting, $ending"
//    pattern = true
//    def rvs = extractProcDefParts(starting)
//    network += rvs[0] + "\n"
//    copyProcProperties(rvs, starting, ending)
//  } // end of ParallelClientServerEngine

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

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, "workers")
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : workers and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addGroup(" + returned[0] + ", " + returned[1] + " )) \n"
//      }
//    }

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

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, "workers")
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : workers and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addGroup(" + returned[0] + ", " + returned[1] + " )) \n"
//      }
//    }

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

//    if (logging) {
//      def returned = getLogData(starting, "nodes")
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : stages and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addMCEngine (" + returned[0] + " )) \n"
//      }
//    }
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
//    if (logging) network += logChanAdd
    network += "    // no output channel required\n"
    copyProcProperties(rvs, starting, ending)

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, "stages")
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : stages and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addPipe (" + returned[0] + ", " + returned[1] + " )) \n"
//      }
//    }
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

//    //SH added JMK modified
//    if (logging) {
//      def returned = getLogData(starting, "stages")
//      if (returned == [null, null]) {
//        network += "getLogData returned null in $processName : stages and/or LogPhaseNames not found"
//        error += " with errors, see the parsed output file"
//      } else {
//        network += "\n    //gppVis command\n"
//        network += "    Visualiser.hb.getChildren().add(Visualiser.p.addPipe (" + returned[0] + ", " + returned[1] + " )) \n"
//      }
//    }
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
