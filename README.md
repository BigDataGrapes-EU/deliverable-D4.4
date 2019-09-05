# H2020 RIA BigDataGrapes - Resource Optimization Methods and Algorithms (T4.4)

This document for deliverable D4.4 Resource Optimization Methods and Algorithms describes new tools to manage distributed big data platforms that will be used in the BigDataGrapes (BDG) platform to optimize the resource management of computing elements in large scale distributed infrastructures.

The BigDataGrapes platform aims at providing Predictive Data Analytics tools that go beyond the state-of-the-art for what regards their application to large and complex grapevine-related data assets. Such tools leverage machine learning techniques that largely benefit from the distributed execution paradigm that serves as the basis for addressing efficiently the analytics and scalability challenges of grapevines-powered industries (see Deliverable 4.1). However, distributed architectures can consume a large amount of electricity when operated at large scale. This is the case of OnLine Data Intensive (OLDI) systems, which perform significant computing over massive datasets per user request. They often require responsiveness in the sub-second time scale at high request rates.

In this document we illustrate the main operational characteristics of the OLDI systems and we describe our OLDI Simulator, a java library to simulate OnLine Data-Intensive systems to study their performance in terms of latency and energy consumption. Finally, we show how to use the library by using an example, in which we 1) we describe a system to simulate, 2) we model the system, and 3) we evaluate and compare various solutions by simulation means.

# OLDI Simulator
A library for simulating [O]n[l]ine [D]ata [I]ntensive systems, such as search engines, memory caching systems, and so on. The library simulates both latencies and CPU energy consumption of such systems.

## Introduction

### OLDI systems architecture
OLDI Simulator models online data intensive systems. These systems perform significant computing over massive datasets per user request. They often require responsiveness in the sub-second time scale at high request rates. Some examples of OLDI systems are search engines, memory caching systems, online advertising, machine translation, etc.

OLDI systems typically works as follows. Given a datasets, a datastore component is built. In search engines, for instance, the datastore is represented by the [inverted index](https://en.wikipedia.org/wiki/Inverted_index). OLDI systems must manage massive datastores and process billions of *requests* per day with low latencies. Hence, OLDI systems ofted partition into smaller *shards* the datastore used to process user requests. In fact, request processing times often depend on the datastore size. Since shards are smaller than the original datastore, this results in reduced processing times. After partitioning, shards are assigned to different *shard servers*. A shard server is a physical server composed by several *multi-core processors/CPUs* with a shared memory which holds the shard. A *request processing* thread is executed on top of each of the CPU core of the shard server.
When a request is sent to an OLDI system, it is first received by a *request broker* which dispatches the request to every shard server. Each server computes the request results on its shard independently from the others. These partial results are sent back to the broker, which aggregates them. The aggregated results are the same that would be provided by a single datastore; since the computation is now distributed across several servers, request processing times are reduced. The request broker collects and aggregates the partial results from the shards, and the final results are sent to the issuing user. The set of shard servers holding all the shards is a *datastore replica*.

Please notice that, when workloads are intense, all the shard servers within an OLDI system may be busy processing requests. Therefore, new requests may be enqueued by the OLDI system in a queue. The first request in the queue is processed as soon as some computing resource becomes available. Where to place the request queue is an architectural choice. A popular option is the Per-Datastore-Replica-Queue (PDRQ), where each datastore replica has its own request queue. Upon request arrival, the request broker pushes the request in the queue of the least loaded replica, which starts processing it as soon as its shard servers are available. Another popular choice is the Per-Processing-Thread-Queue, where each CPU core of each shard server has its own request queue. In this case, a request is processed as soon as the corresponding CPU core is free. Both architectural choices have their advantages. For instance, PDRQ typically leads to smaller latencies. However, PPTQ permits a fine-grained control of the CPU power management mechanisms [2].

This library permits to simulate an OLDI system to estimate, for instance, its latency under different workloads. To this end, the library models several of the concepts introduced in this section. Requests are represented by the `it.cnr.isti.hpclab.request.Request` class, whose instances can be generated by a `it.cnr.isti.hpclab.request.RequestSource`. A `it.cnr.isti.hpclab.engine.RequestBroker` instance can be used to simulate the dispatching of requests towards various datastore replicas, which are represented by the `it.cnr.isti.hpclab.engine.DatastoreReplica` class. This refers to multiple `it.cnr.isti.hpclab.engine.ShardSever` instances, which represent physical servers. Finally, shard servers hold a shard, which is represented as a `it.cnr.isti.hpclab.engine.Shard` instance, and run several request processing threads, which are represented by `it.cnr.isti.hpclab.engine.RequestProcessingThread` instances. The library currently implements all the aforementioned classes for both the PDRQ and PPTQ architectures. To simulate an OLDI system, instantiate and configure `it.cnr.isti.hpclab.simulator.OLDISimulator`.

### OLDI system energy consumption

OLDI systems are typically composed by thousands of physical servers, hosted in large datacenters. This infrastructure is necessary to guarantee that most users will receive results in sub-second times. At the same time, such many servers consume a significant amount of energy, hindering the profitability of OLDI systems and raising environmental concerns. In fact, datacenters can consume tens of megawatts of electric power and the related expenditure can exceed the original investment cost for a datacenter.
In the past, a large part of a datacenter energy consumption was accounted to inefficiencies in its cooling and power
supply systems. However, modern datacenters have largely reduced the energy wastage of those infrastructures, leaving little room for further improvement. On the contrary, opportunities exist to reduce the energy consumption of the servers hosted in a datacenter. In particular, the CPUs dominate the energy consumption of physical servers.

Modern CPUs usually expose two energy saving mechanism, namely *C-states* and *P-states*. C-states represent CPU
cores idle states and they are typically managed by the operating system. C0 is the operative state in which a CPU core can perform computing tasks. When idle periods occur, i.e., when there are no computing tasks to perform, the core can enter one of the other deeper C-states and become inoperative. However, OLDI systems usually process a large and continuous stream of requests. As a result, their servers rarely inactive and experience particularly short idle times. Consequently, there are little opportunities to exploit deep C-states. When a CPU core is in the active C0 state, it can operate at different frequencies (e.g., 800 MHz, 1.6 GHz, 2.1 GHz, ...). This is possible thanks to the *Dynamic Frequency and Voltage Scaling (DVFS)* technology which permits to adjust the frequency and voltage of a core to vary its performance and power consumption. In fact, higher core frequencies mean faster computations but higher power consumption. Vice versa, lower frequencies lead to slower computations and reduced power consumption. The various configurations of voltage and frequency available to the CPU cores are mapped to different P-states.

DVFS power management mechanism can be effectively simulated, and CPU energy consumption can be reliably estimated [4]. Indeed, this library permits to estimate the CPU energy consumption of an OLDI system via simulation. To this end, it provides the `it.cnr.isti.hpclab.cpu.CPU` class to represent any CPU. Every CPU has one or more cores, which are represented by the `it.cnr.isti.hpclab.cpu.Core` class. CPU instances are obtainable via the `it.cnr.isti.hpclab.cpu.CPUBuilder`.
Currently, this library implements the aforementioned classes for the [Intel i7-4770K](https://ark.intel.com/content/www/us/en/ark/products/75123/intel-core-i7-4770k-processor-8m-cache-up-to-3-90-ghz.html) product, and it models the energy consumption of this CPU when running a search application.

## Installation
```
git clone https://github.com/BigDataGrapes-EU/deliverable-D4.4.git
cd deliverable-D4.4
git submodule init
git submodule update --remote
mvn install package
```

## Usage:
The simulation must be programmed using the library classes to model the desired system. Examples are included in the [example folder](simulator-module/src/example/) of the simulator-module. There are currently two implemented examples: simple, and search. The `jar-with-dependencies` including these demos can be found, after the installation step, in the `target` folder of `simulation-module`. To run the demos, type in a terminal:

`java -cp simulator-module-1.0-jar-with-dependencies.jar it.cnr.isti.hpclab.example.simple.Simulation`

or

`java -cp simulator-module-1.0-jar-with-dependencies.jar it.cnr.isti.hpclab.example.search.Simulation`

### Simple example:
In this example, we run a simple OLDI simulation, simulating a (possibly replicated) datastore with just two shards. In this simple example, we assume that requests arrive to the system following a Poisson distribution. Also, we assume that the service times for such requests follow an exponential distribution.

When starting the simulation, the user can decide:
* the number of replicas in the system,
* mean requests per second,
* mean service times,
* number of hours to simulate,
* system type (PDRQ or PPTQ).

```
	usage: java it.cnr.isti.hpclab.example.simple.Simulation
	       [-h] -o OUTPUT -n NUMREPLICAS -r REQPERSEC -s SERVICETIMES SERVICETIMES -d SIMDURATION -t {pdrq,pptq}
	       -f {800000,1000000,1200000,1400000,1600000,1800000,2000000,2100000,2300000,2500000,2700000,2900000,3100000,3300000,3500000}

	Run a simple OLDI simulation, simulating a (possibly replicated) datastore with two shards

	named arguments:
	  -h, --help             show this help message and exit
	  -o OUTPUT, --output OUTPUT
	                         Output file (gzipped)
	  -n NUMREPLICAS, --numReplicas NUMREPLICAS
	                         Number of datastore replicas
	  -r REQPERSEC, --reqPerSec REQPERSEC
	                         Request per second
	  -s SERVICETIMES SERVICETIMES, --serviceTimes SERVICETIMES SERVICETIMES
	                         The avg. service times (in milliseconds) for the two shards
	  -d SIMDURATION, --simDuration SIMDURATION
	                         The desired duration of the simulation (in hours)
	  -t {pdrq,pptq}, --type {pdrq,pptq}
	                         The system type: Per-Datastore-Replica-Queue (pdrq) or Per-Processing-Thread-Queue (pptq)
	  -f {800000,1000000,1200000,1400000,1600000,1800000,2000000,2100000,2300000,2500000,2700000,2900000,3100000,3300000,3500000}, --frequency {800000,1000000,1200000,1400000,1600000,1800000,2000000,2100000,2300000,2500000,2700000,2900000,3100000,3300000,3500000}
	                         The frequency at which every core of the simulated system must operate
```

### Search example
In this example, we  simulate a distributed Web search engine, serving a day of queries from the MSN2006 query log. The (simulated) inverted index is obtained by indexing the ClueWeb09 document collection, partitioned into five shards.

When starting this other simulation, the user can decide:
* the number of replicas in the system,
* system type (PERF, PEGASUS, or PESOS),
* the desired service level objective (SLO, in milliseconds).

```
usage: java it.cnr.isti.hpclab.example.search.Simulation
       [-h] -o OUTPUT -n NUMREPLICAS -s SLO -t {perf,pegasus,pesos}

Simulate a (possibly replicated) Web search engine with an inverted index composed by 5 shards

named arguments:
  -h, --help             show this help message and exit
  -o OUTPUT, --output OUTPUT
                         Output file (gzipped)
  -n NUMREPLICAS, --numReplicas NUMREPLICAS
                         Number of inverted index replicas
  -s SLO, --slo SLO      Target service level objective (in milliseconds)
  -t {perf,pegasus,pesos}, --type {perf,pegasus,pesos}
                         The system type: Per-Datastore-Replica-Queue (pdrq) or Per-Processing-Thread-Queue (pptq)
```

The PERF configuration imposes to every shard server to operate its CPU at maximum core frequency. Differenlty, the PEGASUS [1] and PESOS [2] configurations apply power management techniques, varying the core frequencies at runtime. Both PEGASUS and PESOS do their best to keep latencies below the SLO parameter. SLO is ignored by PERF. More details can be found in [4].

### Output format:
The output file (gzipped) has two kinds of entries:
* *broker*, and
* *energy*.

For each query in the input, we will have a broker line in the output, like:
```
[broker] 4 14.000
```
This tells us that the system has received a request at second 4 (2nd field) and that its *completion time* was 14 milliseconds (3rd field).

Also, for each (simulated) second we will have an energy line in the output, like:
```
[energy] 86395 47.728
```
This tells us that the system has consumed 47.728 Joules at second 86395 of the simulation.

## How to munge the output file:
To get the energy consumption (1 entry per second, value in Joules):
`zcat output.gz | grep energy | cut -f3 -d' ' > output.energy`

To get the 95th-tile latency (1 entry per second, 30-seconds moving 95th-tile latency in ms):
`python3 mungetime-gzip.py output.gz > output.95th-tile`

Once these two files are generated, information can be plotted using plot_energy.py and plot_times.py:
`python3 plot_energy.py file1.energy file2.energy ...`.

The python scripts can be found in [this](scripts) folder

## Dependencies:
* jades (https://github.com/catenamatteo/jades)
* commons-math3
* fastutil
* guava
* argparse4j

To use the python scripts, python3 is required along with numpy and matplotlib.

## References
1. Lo et al. 2014. Towards energy proportionality for large-scale latency-critical workloads. ISCA'14.
2. Catena and Tonellotto. 2017. Energy-Efficient Query Processing in Web Search Engines. TKDE.
3. Mor Harchol-Balter. 2013. Performance Modeling and Design of Computer Systems: Queueing Theory in Action. Cambridge University Press.
4. Catena et al. 2018. Efficient Energy Management in Distributed Web Search. CIKM'18.
