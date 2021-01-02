package mandelbrotClasses

class Mdata extends groovyParallelPatterns.DataClass{

  int []colour		  // array of colour values for this line
  double [][] line 	// array of [x,y] values for this line
  double ly         // y value for this line
  int escapeValue


  int WHITE = 1
  int BLACK = 0
  double minX = -2.5
  double minY = 1.0
  double rangeX = 3.5
  double rangeY = 2.0

  static String initialiseClass = "initClass"
  static String createInstance = "createInstance"
  static String calculate = "calculateColour"

  static int lineY = 0
  static int heightPoints
  static int widthPoints
  static double delta
  static int maxIterations

  int initClass ( List d){
    widthPoints = (int) d[0]
    maxIterations = (int) d[1]
    delta = rangeX / ((double) widthPoints)
    heightPoints = (int) (rangeY / delta )
    println "\nMD-init: width: $widthPoints, height: $heightPoints, delta: $delta, escape: $maxIterations"
    return completedOK
  }

  int createInstance (List d) {
    if (lineY == heightPoints) return normalTermination
    // initialise instance variables
    colour = new int[widthPoints]
    line = new double[widthPoints][2]
    escapeValue = maxIterations
    ly = lineY * delta //y value for this line
    0.upto(widthPoints-1){ int w ->
      line[w][0] = minX + ( w * delta)
      line[w][1] = minY - ly
    }
//    println "Md: $ly"
    lineY = lineY + 1
    return normalContinuation
  }

   int calculateColour (List d) {
    int width = colour.size()
//    println "Calc : $ly"
    0.upto(width-1){ int w->
      double xl = 0.0, yl = 0.0, xtemp = 0.0
      int iterations = 0
      while (((xl * xl)+(yl * yl) < 4) && iterations < escapeValue) {
        xtemp = (xl * xl) - (yl * yl) + line[w][0]
        yl = (2 * xl * yl) + line[w][1]
        xl = xtemp
        iterations = iterations + 1
      }
      colour[w] = (iterations < escapeValue) ? WHITE : BLACK
//      println " C: x = ${line[w][0]}, y = ${line[w][1]}, iter = $iterations, col = ${colour[w]}, w = $w"
    }
    return completedOK
  }

}
