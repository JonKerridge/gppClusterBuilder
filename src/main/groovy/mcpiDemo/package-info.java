package mcpiDemo;
/*
The McPiDemo package comprises a set of versions used to test gppClusterBuilder.
Each version creates a different version of a cluster that test various combinations
of the processes provided in the groovyParallelPatterns.cluster package together with
other library processes as required.

The abbreviations used are as follows.
For the Emit part of the cluster definition.

ONRL   OneNodeRequestedList
ONRCL  OneNodeRequestedCastList

For the Cluster Definition Input Part.

NRFA   NodeRequestingFanAny
NRFL   NodeRequestingFanList
NRSCL  NodeRequestingSeqCastList
NRPCL  NodeRequestingParCastList

For the Cluster Definition OutputPart

AFO  AnyFanOne
LFO  ListFanOne
LMO  ListMergeOne
L2N  List2Net

For the Collect Input Part

AFO  AnyFanOne
LFO  ListFanOne
LMO  ListMergeOne
N2L  Net2List

The tests follow using abbreviations for other library processes.
          Emit Part     Cluster Part    Collect Part
McPi1     emit ONRL     NRFA AGA AFO    AFO Collect
McPi2     emit ONRL     NRFL LGL LFO    AFO Collect
McPi3     emit ONRL     NRFL LGL LMO    AFO Collect
McPi4     emit ONRL     NRFL LGL LMO    LMO Collect
McPi5     emit ONRL     NRFL LGL LMO    LFO Collect

McPi6     emit ONRCL    NRFA AGA AFO    AFO Collect
McPi7     emit ONRCL    NRFL LGL LFO    AFO Collect

McPi8     emit ONRL     NRSCL LGL LFO   AFO Collect
McPi9     emit ONRCL    NRSCL LGL LFO   AFO Collect

McPi10    emit ONRL     NRPCL LGL LFO   AFO Collect
McPi11    emit ONRCL    NRPCL LGL LFO   AFO Collect


 */