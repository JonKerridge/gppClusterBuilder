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
		// create readers for all the script file
		String scriptFileName = fileRoot + ".cgpp"
		scriptReader = new FileReader(new File(scriptFileName))
		// create writers for all the output files and delete any pre-existing files
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
		readInFiles.call()
		gppLex.with{
			extractAppStructure.call()
			processLoaders.call()
			extractFirstLastProcs.call()
			createAllNetChannelInserts.call()
			createHostProcessEmitInserts.call()
			createHostProcessCollectInserts.call()
			createClusterProcessInserts.call()
			completeProcesses.call()
		}
		writeOutFiles.call()
		return gppLex.error
	}

}
