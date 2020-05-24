package gppClusterBuilder

class CGPPlexFileHanding {

	FileReader scriptReader = null
	FileWriter nodeLoaderWriter = null
	FileWriter nodeProcessWriter = null
	FileWriter hostLoaderWriter = null
	FileWriter hostProcessWriter = null
	FileReader nodeLoaderReader = null
	FileReader hostLoaderReader = null
	FileReader nodeProcessReader = null
	FileReader hostProcessReader = null

	String appName

	CGPPlexingMethods gppLex = new CGPPlexingMethods()

	def openFiles ( String fileRoot  ) {
		String dir = System.getProperty("user.dir") + "/src/main/groovy"
//		println "Builder Dir = $dir"
		String scriptFileName = fileRoot + ".cgpp"
		scriptReader = new FileReader(new File(scriptFileName))
		nodeLoaderReader = new FileReader(new File("$dir/baseFiles/BasicNodeLoader.groovy"))
		hostLoaderReader = new FileReader(new File("$dir/baseFiles/BasicHostLoader.groovy"))
		nodeProcessReader = new FileReader(new File("$dir/baseFiles/BasicNodeProcess.groovy"))
		hostProcessReader = new FileReader(new File("$dir/baseFiles/BasicHostProcess.groovy"))
		String nodeLoaderName = fileRoot + "NodeLoader.groovy"
		String nodeProcessName = fileRoot + "NodeProcess.groovy"
		String hostLoaderName = fileRoot + "HostLoader.groovy"
		String hostProcessName = fileRoot + "HostProcess.groovy"
		File nodeLoaderFile = new File(nodeLoaderName)
		if (nodeLoaderFile.exists())nodeLoaderFile.delete()
		nodeLoaderWriter = new FileWriter(new File(nodeLoaderName))
		File nodeProcessFile = new File(nodeProcessName)
		if (nodeProcessFile.exists())nodeProcessFile.delete()
		nodeProcessWriter = new FileWriter(nodeProcessFile)
		File hostLoaderFile = new File(hostLoaderName)
		if (hostLoaderFile.exists())hostLoaderFile.delete()
		hostLoaderWriter = new FileWriter(hostLoaderFile)
		File hostProcessFile = new File(hostProcessName)
		if (hostProcessFile.exists())hostProcessFile.delete()
		hostProcessWriter = new FileWriter(hostProcessFile)

		// extract the name of the application
		int appLen = fileRoot.size() - 1
		int i = appLen - 1
		while ((fileRoot[i] != '/') && (fileRoot[i] != '\\')) i = i-1
		appName = fileRoot[(i+1) .. appLen]
	}

	def readInFiles = {
		gppLex.getInputs(
				scriptReader,
				nodeLoaderReader,
				nodeProcessReader,
				hostLoaderReader,
				hostProcessReader,
				appName)
	}

	def writeOutFiles = {
		gppLex.putOutputs(
				nodeLoaderWriter,
				nodeProcessWriter,
				hostLoaderWriter,
				hostProcessWriter)
	}

	String parse () {
		readInFiles()
		gppLex.with{
			extractAppStructure()
			processLoaders()
//			createNodeInserts()
//			createHostInserts()
			completeProcesses()
		}
		writeOutFiles()
		return gppLex.error
	}

}
