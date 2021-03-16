The gpp_cluster_builder package takes a script with the extension *.cgpp 
that converts components that use the groovy_parallel_patterns library and creates
scripts that can be run on a cluster of workstations.  The builder takes a script that
has the basic form, where //@ introduce definitional annotations into the script :  
<pre>
... constant definitions

//@emit emit-IP-address
... the Emit process definition

//@cluster #clusters
... cluster process definition

//@collect
... the Collect process definition
</pre>

The script does not contain any detailed information concerning the connections required between
the host and the workstations in the cluster.  This data is created by the builder.  The IP-addresses 
of the cluster workstations are not required as the workstations determine their own IP-address 
which are then communicated to the controlling workstation, called the Host.

It is assumed the Emit and Collect processes will run on the same host workstation 
that has the emit-IP-address.  The cluster will comprise #cluster workstations all connected
on the same network.  The IP-addresses of the cluster workstations are not required.

The builder creates two Groovy scripts comprising the HostLoader and HostProcess that 
run on the host machine comprising the Emit and Collect processes.  A further two scripts, 
the NodeLoader and the NodeProcess are used on each workstation in the cluster.

Once the scripts have been created, the next task will be to create a runnable jar artifact
from the NodeLoader script.  This will have to be copied onto each of the cluster workstations,
probably using some form of remote desktop application.  The NodeLoader script is not dependent 
on the application and thus once present on the workstation can be used to 
load and run any application, provided the same Host workstation is always used.

To invoke the application, the HostLoader script is run on the host workstation, typically from the
IDE where the codes are held.  The previously copied NodeLoader jar artifact is then run on 
each of the workstations in the cluster.  This will cause the specific version of the NodeProcess 
script to be sent to each workstation.  These scripts vary with the IP-address of the workstation node.
The HostProcess script is then loaded into the host node and at this point the application can start.

At the end of processing all resources are recovered, and a set of timing information is returned by 
each cluster workstation giving the load and run times separately.

It is NOT necessary to copy any application class files from the Host to the other workstations in the 
cluster as this is done automatically.  When the application terminates all class files are recovered 
and no space is required for their storage.

Examples of the use the gpp_cluster_builder can be found at https://github.com/JonKerridge/ClusterDemos  

In order to use the library the following repository and dependency definitions 
are required in a build.gradle file.
<pre>
repositories {
    maven { // to download the jonkerridge.groovy_parallel_patterns library
        name = "GitHub"
        url = "https://maven.pkg.github.com/JonKerridge/GPP_Library"
        credentials {
            username = project.findProperty("gpr.user")
            password = project.findProperty("gpr.key")
        }
    }
    maven { // to download the jonkerridge.gpp_cluster_builder library
        name = "GitHub"
        url = "https://maven.pkg.github.com/JonKerridge/gppClusterBuilder"
        credentials {
            username = project.findProperty("gpr.user")
            password = project.findProperty("gpr.key")
        }
    }
}
dependencies {
   compile "jonkerridge:groovy_parallel_patterns:1.1.12"
   compile "jonkerridge:gpp_cluster_builder:1.1.0"
}
</pre>

The libraries will be obtained from the Github Packages Repository.

In addition, an application, called gppTransform, is provided in the form of a runnable jar file at:
https://github.com/JonKerridge/gppTransform/releases/tag/1.1.0  
that will transform any script that can be built using gppClusterBuilder or the 
single machine version gppBuilder into the required groovy scripts.


**Please Note**
In order to download Github Packages a user requires to have a Github Personal Access Token.  
See https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token

A gradle.properties file is required at the same directory level as the build.gradle file that contains

<pre>
gpr.user=userName
gpr.key=userPersonalAccessToken
</pre>



