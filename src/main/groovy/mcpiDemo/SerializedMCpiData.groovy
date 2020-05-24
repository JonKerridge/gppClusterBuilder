package mcpiDemo


class SerializedMCpiData extends groovyParallelPatterns.DataClass {
  int iterations = 0
  int within = 0
  int instance = 0
  static String withinOp = "getWithin"



  /**
   * Calculates for each iteration an x and y random value 0.0 <= v < 1.0
   * Then determines if the sum of the squares of x and y are <= 1.0 and adds
   * 1 to within if so.
   *
   * @return completedOK
   */
  int getWithin(List d){
    def rng = new Random()
    float x, y
    for ( i in 1 ..iterations){
      x = rng.nextFloat()
      y = rng.nextFloat()
      if ( ((x*x) + (y*y)) <= 1.0 ) within = within + 1
    }
    return completedOK
  }

  String toString (){
    String s = "SerializedMCpiData: $instance, $iterations, $within "
    return s
  }

}
