<h3>This is sample application for akka cluster balancing work across nodes</h3>

<h4>Design</h4>

Seed nodes are starting point for the cluster, and they serve as the first point of contact for Master, Worker and Job Worker nodes. Nodes join the cluster by sending a join message which contains the unique address of the node that joins. Atleast one seed node should be up and running, It is necessary that before a seed node goes down atleast other seed should be up and running. Seed nodes are configured in the application.conf resource file.

Processing Job in the cluster is consist of Seed Nodes, Master, Worker Master, and Job Workers. Master node control and supervise the completion of Job and It queues Jobs if workers are not available. Master node allocate work to Worker Master based on their availability.
The Job workers request work from a Worker Master, process parts of the job, and return the
Partial results to the Worker Master. The worker master reports the result to Master once all Job Worker finishes their allocated work. A job is repeated if any Worker Master or Job worker node fails during the process.


<img src="https://github.com/asethia/akkacluster/blob/master/designdiagram/WorkerMaster.png" border="0">


<h5>Prerequisite</h5>

1. <a href="http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html">JRE 1.8.x</a>
2. <a href="http://www.scala-lang.org/download/all.html">Scala 2.11.5</a>
3. <a href="https://maven.apache.org/download.cgi">Apache Maven</a>

<h5>Install</h5>

The application can be install using following command:

mvn install

This will create akkaclustersampleapp-1.0-jar-with-dependencies.jar in the traget directory.

<h5>Running</h5>

To run the cluster we need to run following command from the target folder in different console window: 

1. <b>Mandatory - Start Seed Node#1</b> - java -jar akkaclustersampleapp-1.0-jar-with-dependencies.jar com.akka.cluster.main.SeedMain 2551
2. <b>Optional - Start Seed Node#2</b> - java -jar akkaclustersampleapp-1.0-jar-with-dependencies.jar com.akka.cluster.main.SeedMain 2552 
3. <b>Mandatory - Start Master Node#1</b> - java -jar akkaclustersampleapp-1.0-jar-with-dependencies.jar com.akka.cluster.main.MasterMain 3551
4. <b>Optional - Start Master Node#2</b> - java -jar akkaclustersampleapp-1.0-jar-with-dependencies.jar com.akka.cluster.main.MasterMain 3552
5. <b>Mandatory - Start WorkerMaster Node#1</b> - java -jar akkaclustersampleapp-1.0-jar-with-dependencies.jar com.akka.cluster.main.WorkerMasterMain 4551
6. <b>Optional - Start WorkerMaster Node#2</b> - java -jar akkaclustersampleapp-1.0-jar-with-dependencies.jar com.akka.cluster.main.WorkerMasterMain 4552

After running above command, seed nodes, master and worker master are ready to start processing the job.

<h5>Submit Multiple Jobs to the Master1</h5>

The TestMain class is using ClusterClient to submit 3 jobs to Master1 (running on port 3551) Node. The following command can be used to start the Job Submission, the output can be seen on Master1 console window:

java -jar akkaclustersampleapp-1.0-jar-with-dependencies.jar com.akka.cluster.main.TestMain

