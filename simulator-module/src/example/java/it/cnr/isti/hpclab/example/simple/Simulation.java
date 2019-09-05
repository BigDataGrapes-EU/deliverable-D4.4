package it.cnr.isti.hpclab.example.simple;

import java.io.IOException;
import java.util.List;

import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.cpu.impl.Intel_i7_4770K_Builder;
import it.cnr.isti.hpclab.engine.RequestBroker;
import it.cnr.isti.hpclab.engine.Shard;
import it.cnr.isti.hpclab.request.RequestSource;
import it.cnr.isti.hpclab.simulator.OLDISimulator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Run a simple OLDI simulation, simulating a (possibly replicated) datastore with two shards.
 * @author Matteo Catena
 *
 */
public class Simulation {

	public static void main(String args[]) throws IOException {

		CPUBuilder cpuBuilder = new Intel_i7_4770K_Builder();
		List<Integer> frequencies = new IntArrayList(cpuBuilder.getFrequencies());

		ArgumentParser parser = ArgumentParsers.newFor("java " + Simulation.class.getName()).build().defaultHelp(true)
				.description("Run a simple OLDI simulation, " + "simulating a (possibly replicated) "
						+ "datastore with two shards");

		parser.addArgument("-o", "--output").help("Output file (gzipped)").required(true);
		parser.addArgument("-n", "--numReplicas").help("Number of datastore replicas").type(Integer.class)
				.required(true);
		parser.addArgument("-r", "--reqPerSec").help("Request per second").type(Integer.class).required(true);
		parser.addArgument("-s", "--serviceTimes").help("The avg. service times (in milliseconds) for the two shards")
				.nargs(2).type(Integer.class).required(true);
		parser.addArgument("-d", "--simDuration").help("The desired duration of the simulation (in hours)")
				.type(Integer.class).required(true);
		parser.addArgument("-t", "--type")
				.help("The system type: Per-Datastore-Replica-Queue (pdrq) or Per-Processing-Thread-Queue (pptq)")
				.choices("pdrq", "pptq").required(true);
		parser.addArgument("-f", "--frequency")
				.help("The frequency at which every core of the simulated system must operate")
				.type(Integer.class)
				.choices(frequencies).required(true);					

		Namespace res = null;
		try {
			res = parser.parseArgs(args);

		} catch (ArgumentParserException e) {

			parser.handleError(e);
			System.exit(1);
		}

		String output = res.getString("output");
		int numReplicas = res.getInt("numReplicas");
		int reqPerSec = res.getInt("reqPerSec");
		List<Integer> serviceTimes = res.get("serviceTimes");
		int simDuration = res.getInt("simDuration");
		String type = res.getString("type");
		int frequency = res.getInt("frequency");

		OLDISimulator simulator = new OLDISimulator(output, simDuration);

		Shard shard0 = new ShardImpl(cpuBuilder, serviceTimes.get(0));
		Shard shard1 = new ShardImpl(cpuBuilder, serviceTimes.get(1));

		RequestBroker broker = null;
		switch (type) {
		case "pdrq":
			broker = new it.cnr.isti.hpclab.engine.pdrq.RequestBroker(simulator, cpuBuilder, numReplicas,
					shard0, shard1);
			break;
		case "pptq":
			broker = new it.cnr.isti.hpclab.engine.pptq.RequestBroker(simulator, cpuBuilder, numReplicas,
					shard0, shard1);
			break;
		default:
			System.err.println("This is unexpected!");
			throw new RuntimeException();
		}
		RequestSource source = new RequestSourceImpl(simDuration, reqPerSec);
		simulator.setRequestSource(source);
		simulator.setBroker(broker);
		simulator.setFrequency(frequency);
		
		source.generate(simulator, broker);

		System.out.println("Simulating...");
		simulator.doAllEvents();
		System.out.println("...done!");
	}
}
