package mandelbrotScripts

import gppClusterBuilder.CGPPbuilder

def builder = new CGPPbuilder()
String fileRoot = "D:\\IJGradle\\gppClusterBuilder\\src\\main\\groovy\\mandelbrotScripts\\"

builder.runClusterBuilder("${fileRoot}MandelbrotNodes1")

