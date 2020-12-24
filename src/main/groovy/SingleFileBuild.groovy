

import gppBuilder.GPPbuilder

def build = new GPPbuilder()
String rootPath = "./"  // as required for use in Intellij

build.runBuilder(rootPath + "groovy\\mandelbrotDemo\\RunMandelbrotLine")
build.runBuilder(rootPath + "groovy\\mandelbrotDemo\\RunMandelbrotLineNoGui")



