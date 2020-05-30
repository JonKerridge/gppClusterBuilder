package gppClusterBuilder

String cgppFileName = "D:\\IJGradle\\gppClusterBuilder\\src\\main\\groovy\\mcpiDemo\\McPi.cgpp"
File cgppFile = new File(cgppFileName)
FileReader cgppReader = new FileReader(cgppFile)
List scriptText = []
cgppReader.each { line ->
  if (line.size() == 0) line = " " else line = line.trim()
  scriptText << line

}
int emitStart = 15
int emitEnd = 29
int clusterStart = 31
int clusterEnd = 42
int collectStart = 44
int collectEnd = 58

int emitLastProc
int clusterFirstProc
int clusterLastProc
int collectFirstProc

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
}

extractFirstLastProcs()

println "$emitLastProc, $clusterFirstProc, $clusterLastProc, $collectFirstProc"
