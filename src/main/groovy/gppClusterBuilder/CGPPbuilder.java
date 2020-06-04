package gppClusterBuilder;


/**
 * CGPPbuilder provides a means of transforming a file containing the definition of a
 * CGPP defined application, in which, the process definitions omit the communication and
 * parallel composition definitions.  The program constructs the required definitions
 * automatically.  The target application uses a host node and a number of other nodes
 * in the same network to solve the problem.  The host node runs an emit and a collect
 * process while the other nodes run the main application processes used to solve the
 * problem<p>
 *
 * The argument to the program is a \path\filename that has a .cgpp suffix.  The
 * supplied argument should omit the suffix.  The output is the \path\filename now
 * with a .groovy suffix.  The output file can be executed as a groovy script.
 * Any errors will be placed in an output file at the place where they were detected.<p>
 *
 * The structure of a .cgpp file is as follows using an example that calculate the
 * Mandelbrot set, as given in the project cgppDemos
 * see
 * https://github.com/JonKerridge/GPP_Library , for the definition of library processes
 * https://github.com/JonKerridge/gppClusterBuilder , for the associated builder
 * https://github.com/JonKerridge/cgppDemos for an example of their use.<p>
 *
 *     <p>The user is advised to associate the file type .cgpp with groovy so that any IDE can
 *     check that the definition as it is entered.  This will ensure the required imports will be
 *     created automatically.</p>
 *     <p>
 *         Any variable initialisations should be entered immediately after the imports.
 *         Each section of the specification is headed by an annotation;
 *         //@emit, //@cluster and //@collect.
 *         Any variables and object definitions should proceed the definition of the processes
 *         required in that part of the specification, for example emitDetails.
 *     </p>
 * <pre>
 * package demoApplications.mandelbrot
 *
 * import groovyParallelPatterns.DataDetails
 * import groovyParallelPatterns.ResultDetails
 * import groovyParallelPatterns.cluster.connectors.NodeRequestingFanAny
 * import groovyParallelPatterns.cluster.connectors.OneNodeRequestedList
 * import groovyParallelPatterns.connectors.reducers.AnyFanOne
 * import groovyParallelPatterns.functionals.groups.AnyGroupAny
 * import groovyParallelPatterns.terminals.Collect
 * import groovyParallelPatterns.terminals.Emit
 *
 * import demoApplications.mandelbrot.MandelbrotLine as ml
 * import demoApplications.mandelbrot.MandelbrotLineCollect as mlc
 * import demoApplications.mandelbrot.SerializedMandelbrotLine as sml
 *
 * // number of workers on each node
 * int cores = 4
 * // number of clusters
 * int clusters = 1
 *
 * //@emit
 * //application variables
 * int width = 350
 * int height = 200
 * int maxIterations = 100
 * double pixelDelta = 0.01
 *
 * def emitDetails = new DataDetails(dName: ml.getName(),
 *     dInitMethod: ml.init,
 *     dInitData: [width, height, pixelDelta, maxIterations],
 *     dCreateMethod: ml.create
 * )
 * // emit process network
 * def emit = new Emit (
 *     eDetails: emitDetails
 * )
 * def onrl = new OneNodeRequestedList()
 *
 * //@cluster clusters
 * // cluster process networks
 * def nrfa = new NodeRequestingFanAny(
 *     destinations: cores
 * )
 * def group = new AnyGroupAny(
 *     workers: cores,
 *     function: sml.calcColour
 * )
 * def afo1 = new AnyFanOne(
 *     sources: cores
 * )
 *
 * //@collect
 * def resultDetails = new ResultDetails(rName: mlc.getName(),
 *     rInitMethod: mlc.init,
 *     rCollectMethod: mlc.collector,
 *     rFinaliseMethod: mlc.finalise )
 *
 * // collect processes
 * def afo2 = new AnyFanOne(
 *     sources: clusters
 * )
 * def collector = new Collect(
 *     rDetails: resultDetails
 * )
 * </pre>
 */
public class CGPPbuilder {

    /**
     *
     * @param args args[0] contains the full path name of a the file to be converted,
     *             excluding the .cgpp suffix
     *
     */
    public static void main(String[] args) {
        String fileRoot = args[0];
        CGPPlexFileHanding cgppLexer = new CGPPlexFileHanding();
        cgppLexer.openFiles(fileRoot);
        System.out.println( "Transforming: " + fileRoot + ".cgpp");
        String error = cgppLexer.parse();
        if (error.equals("")){
            System.out.println("Build Successful");
        }
        else System.out.println("Build failed:" + error);
    }

    /**
     * A method that calls the GPPbuilder program.
     *
     * @param fileRoot the name of the root of the file to be transformed without the .cgpp suffix, the output
     * is a collection of four files with the a .groovy suffix that can be executed as a groovy script.
     */
    public static void runClusterBuilder(String fileRoot){
//        String inFile = fileRoot +  "_gpp.groovy";
        CGPPlexFileHanding cgppLexer = new CGPPlexFileHanding();
        cgppLexer.openFiles(fileRoot);
        System.out.println( "Transforming: " + fileRoot + ".cggp");
        String error = cgppLexer.parse();
        if (error.equals("")){
            System.out.println("Build Successful");
        }
        else System.out.println("Build failed:" + error);

    }

}
