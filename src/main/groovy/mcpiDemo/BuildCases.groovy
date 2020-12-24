package mcpiDemo

import gppClusterBuilder.CGPPbuilder

def builder = new CGPPbuilder()
String fileRoot = "D:\\IJGradle\\gppClusterBuilder\\src\\main\\groovy\\mcpiDemo\\"

builder.runClusterBuilder("${fileRoot}McPi1")
builder.runClusterBuilder("${fileRoot}McPi2")
builder.runClusterBuilder("${fileRoot}McPi3")
builder.runClusterBuilder("${fileRoot}McPi4")
builder.runClusterBuilder("${fileRoot}McPi5")
builder.runClusterBuilder("${fileRoot}McPi6")
builder.runClusterBuilder("${fileRoot}McPi7")
builder.runClusterBuilder("${fileRoot}McPi8")
builder.runClusterBuilder("${fileRoot}McPi9")
builder.runClusterBuilder("${fileRoot}McPi10")
builder.runClusterBuilder("${fileRoot}McPi11")
