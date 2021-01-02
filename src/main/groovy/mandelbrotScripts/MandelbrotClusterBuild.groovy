package mandelbrotScripts

import gppClusterBuilder.CGPPbuilder

def builder = new CGPPbuilder()
String fileRoot = "./"

builder.runClusterBuilder("${fileRoot}ClusterMandelbrot1")
builder.runClusterBuilder("${fileRoot}ClusterGUIMandelbrot1")

