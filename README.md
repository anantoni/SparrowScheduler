<h1>SparrowScheduler</h1>
<hr>

<ul>
 <li>Implemented Random Scheduling Policy</li>
  <li>Implememented Per Task Sampling Scheduling Policy</li>
  <li>Implemented Batch Sampling Scheduling Policy</li>
  <li>Implemented Late Binding Scheduling Policy</li>
</ul>

<h2>Instructions</h2>
<h4>Client configuration:</h4>
Set list of available schedulers in config/available_schedulers.properties

<h4>Client Execution:</h4>
java -jar dist/SparrowClient

<h4>Scheduler configuration:</h4>
Set list of available workers in config/workers.conf

<h4>Scheduler Execution:</h4>
java -jar dist/SparrowScheduler {<i>port</i>} --{<i>policy</i>}, <b>policy:</b> random/per-task/batch/late<br>
<br>
e.g. java -jar dist/SparrowScheduler 51000 --batch

<h4>Worker Execution:</h4>
java -jar dist/SparrowWorker {<i>port</i>} --{<i>policy</i>}, <b>mode:</b> generic(suitable for random, per-task and batch policies)/late(suitable for late binding policy}<br>
<br>
e.g. java -jar dist/SparrowWorker 52000 --generic





