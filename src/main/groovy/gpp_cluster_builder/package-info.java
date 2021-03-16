/**
 * The program gpp_cluster_builder takes a source file that uses components from the GPP Library
 * and transforms them into a collection of runnable Groovy scripts
 * to be run on a collection of workstations.<p>
 * The program expects a single argument which is the full path name of the source file
 * excluding the required suffix ".cgpp".  The output from the program is four files with
 * the suffix ".groovy".<p>
 * If any errors are found in the source file the output file will contain an indication
 * of the error at the place where it was identified.<p>
 * The source files contain all the data definition and objects required by the application<p>.
 *
 * The processes that comprise the application process network must be specified in the order in
 * which the data flows through the process network.<p>
 *
 * The four files will be of the form ?HostLoader.groovy, ?NodeLoader.groovy, ?HostProcess.groovy
 * and ?NodeProcess.groovy.
 *
 * The script ?HostLoader should be run first on the intial or host node of the network.
 * This script has an argument; the number of other nodes in the cluster to be used.<p>
 *
 * The ?NodeLoader script should be run on each of the required nodes in the cluster.
 * This script has an argument; the IP address of the Host Node.
 *
 */

package gpp_cluster_builder;