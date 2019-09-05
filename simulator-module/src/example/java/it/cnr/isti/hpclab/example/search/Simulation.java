package it.cnr.isti.hpclab.example.search;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.cpu.impl.Intel_i7_4770K_Builder;
import it.cnr.isti.hpclab.engine.RequestBroker;
import it.cnr.isti.hpclab.request.RequestSource;
import it.cnr.isti.hpclab.simulator.OLDISimulator;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Simulate a distributed Web search engine, serving a day of queries from the 
 * MSN2006 query log. The (simulated) inverted index is obtained by indexing 
 * the ClueWeb09 document collection, partitioned into five shards.
 * 
 * @author Matteo Catena
 *
 */
public class Simulation {

	public static void main(String args[]) throws IOException {


		ArgumentParser parser = ArgumentParsers.newFor("java " + Simulation.class.getName()).build().defaultHelp(true)
				.description(
						"Simulate a (possibly replicated) Web search engine with an inverted index composed by 5 shards");

		parser.addArgument("-o", "--output").help("Output file (gzipped)").required(true);
		parser.addArgument("-n", "--numReplicas").help("Number of inverted index replicas").type(Integer.class)
				.required(true);
		parser.addArgument("-s", "--slo").help("Target service level objective (in milliseconds)").type(Integer.class)
			.required(true);		
		parser.addArgument("-t", "--type")
				.help("The system type: Per-Datastore-Replica-Queue (pdrq) or Per-Processing-Thread-Queue (pptq)")
				.choices("perf", "pegasus", "pesos").required(true);

		Namespace res = null;
		try {
			res = parser.parseArgs(args);

		} catch (ArgumentParserException e) {

			parser.handleError(e);
			System.exit(1);
		}

		String output = res.getString("output");
		int numReplicas = res.getInt("numReplicas");
		Time slo = new Time(res.getInt("slo"), TimeUnit.MILLISECONDS);
		int simDuration = 24;
		String type = res.getString("type");

		OLDISimulator simulator = new OLDISimulator(output, simDuration);

		CPUBuilder cpuBuilder = new Intel_i7_4770K_Builder();
		Shard shardB = new Shard("cw09b.ef.pp", "cw09b.ef.time", cpuBuilder.getFrequencies());
		Shard shardA2 = new Shard("cw09a2.ef.pp", "cw09a2.ef.time", cpuBuilder.getFrequencies());
		Shard shardA3 = new Shard("cw09a3.ef.pp", "cw09a3.ef.time", cpuBuilder.getFrequencies());
		Shard shardA4 = new Shard("cw09a4.ef.pp", "cw09a4.ef.time", cpuBuilder.getFrequencies());
		Shard shardA5 = new Shard("cw09a4.ef.pp", "cw09a5.ef.time", cpuBuilder.getFrequencies());

		RequestBroker broker = null;
		switch (type) {
		case "perf":
			broker = new it.cnr.isti.hpclab.engine.pdrq.RequestBroker(simulator, cpuBuilder, numReplicas, shardB,
					shardA2, shardA3, shardA4, shardA5);
			break;
		case "pegasus":
			broker = new it.cnr.isti.hpclab.example.search.pegasus.QueryBroker(simulator, cpuBuilder, slo, numReplicas,
					shardB, shardA2, shardA3, shardA4, shardA5);
			break;
		case "pesos":
			broker = new it.cnr.isti.hpclab.example.search.pesos.QueryBroker(simulator, cpuBuilder, slo, numReplicas,
					shardB, shardA2, shardA3, shardA4, shardA5);
			break;
		default:
			System.err.println("This is unexpected!");
			throw new RuntimeException();
		}
		RequestSource source = new QuerySource(simulator, "msn.day2.qid.txt");
		simulator.setRequestSource(source);
		simulator.setBroker(broker);

		source.generate(simulator, broker);

		System.out.println("Simulating...");
		simulator.doAllEvents();
		System.out.println("...done!");
	}
}
