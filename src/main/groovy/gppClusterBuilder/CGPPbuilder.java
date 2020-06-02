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
 * Any errors will be placed in the output file at the place where they were detected.<p>
 *
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
