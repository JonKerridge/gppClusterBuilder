package mandelbrotScripts

import gppBuilder.GPPbuilder
import gppClusterBuilder.CGPPbuilder

def builder = new GPPbuilder()
String fileRoot = "./"

builder.runBuilder("${fileRoot}MandelbrotGUI")
builder.runBuilder("${fileRoot}MandelbrotNoGUI")
