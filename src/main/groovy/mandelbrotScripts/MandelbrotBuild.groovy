package mandelbrotScripts

import gppBuilder.GPPbuilder
import gppClusterBuilder.CGPPbuilder

def builder = new GPPbuilder()
String fileRoot = "D:\\IJGradle\\gppClusterBuilder\\src\\main\\groovy\\mandelbrotScripts\\"

builder.runBuilder("${fileRoot}RunMandelbrotLine")
builder.runBuilder("${fileRoot}RunMandelbrotLineNoGUI")
