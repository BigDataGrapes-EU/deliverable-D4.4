package it.cnr.isti.hpclab.example.simple;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.PoissonDistribution;

import eu.nicecode.queueing.event.RequestArrival;
import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.request.Request;
import it.cnr.isti.hpclab.request.RequestSource;

/**
 * A simple request source, which send requests following a Poisson
 * distribution.
 * 
 * @author Matteo Catena
 *
 */
public class RequestSourceImpl extends RequestSource {
	
	Queue<Request> generatedRequest;

	/**
	 * 
	 * @param x      The source will send requests for {@code numHrs} simulated
	 *               hours
	 * @param lambda The source will send {@code requestPerSecond} request(s) per
	 *               second
	 */
	public RequestSourceImpl(int x, double lambda) {

		long currentTime = 0;
		long end = TimeUnit.HOURS.toMicros(x);
		
		PoissonDistribution poissonDistribution = new PoissonDistribution(lambda);
		poissonDistribution.reseedRandomGenerator(42);
		
		generatedRequest = new LinkedList<>(); 
		
		int uid = 0;
		
		while (currentTime <= end) {
			
			int numOfArrivals = poissonDistribution.sample();
			long intervalBetweenArrivals = TimeUnit.SECONDS.toMicros(1) / numOfArrivals;
			for (int i = 0; i < numOfArrivals; i++) {
				currentTime += intervalBetweenArrivals;
				if (currentTime <= end) {
					uid += 1;
					Request request = new Request(new Time(currentTime, TimeUnit.MICROSECONDS), uid, uid);
					generatedRequest.add(request);
				}
			}
		}
	}

	@Override
	public boolean isDone() {

		return generatedRequest.isEmpty();
	}

	@Override
	public Request nextRequest() {

		if (isDone()) {
			
			return null;
			
		} else {
			
			return generatedRequest.poll();
		}
	}

	@Override
	public void generate(Simulator simulator, Agent to) {

		Request request = nextRequest();

		if (request != null) {

			RequestArrival ra = new RequestArrival(request.getArrivalTime(), this, to, request);
			simulator.schedule(ra);
			
		}

	}

}
