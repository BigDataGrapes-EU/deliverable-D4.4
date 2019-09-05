package it.cnr.isti.hpclab.example.search.pesos;


import eu.nicecode.queueing.Request;
import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.cpu.Core;
import it.cnr.isti.hpclab.engine.pptq.RequestProcessingThread;
import it.cnr.isti.hpclab.example.search.Query;
import it.cnr.isti.hpclab.example.search.RunningQuery;
import it.cnr.isti.hpclab.example.search.Shard;


/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.engine.pptq.RequestProcessingThread}
 * for the PESOS algorithm [1].
 * 
 * [1] Catena and Tonellotto. 2017. Energy-Efficient Query Processing in Web Search Engines. TKDE.
 * 
 * @author Matteo Catena
 */
public class QueryMatcher extends RequestProcessingThread {

	/**
	 * A reference to the query which is currently processed by the thread
	 */
	protected RunningQuery currentRunningQuery;
	
	public QueryMatcher(ShardServer shardServer, Core core) {
		super(shardServer, core);
		core.setMinFrequency(
				server.getDatastoreReplica().getBroker().getSimulator().now().getTimeMicroseconds());
	}
	
	@Override
	public void receiveRequest(Request request, Agent from, Simulator simulator) {
			
		if (!core.isBusy() && queue.isEmpty()) {

			processRequest(request, simulator);
			
		} else {
			
			queue.offer(request);
			runFrequencyScheduler(simulator.now().getTimeMicroseconds());
		}
	}
	
	@Override
	public void processRequest(Request query, Simulator simulator) {

		RunningQuery rq = new RunningQuery(simulator.now().clone(), this, (it.cnr.isti.hpclab.request.Request) query);
		simulator.schedule(rq);
		core.busy(simulator.now().getTimeMicroseconds());
		currentRunningQuery = rq;
		runFrequencyScheduler(simulator.now().getTimeMicroseconds());
	}
		
	
	@Override
	public void afterRequestCompletion(Request request, Simulator simulator) {
		
		currentRunningQuery = null;
		core.setMinFrequency(simulator.now().getTimeMicroseconds());
		super.afterRequestCompletion(request, simulator);
	}

	/*
	 * This method implements the PESOS algorithm described in [1]
	 */
	private void runFrequencyScheduler(long now) {
		
		Time timeBudget = ((QueryBroker)server.getDatastoreReplica().getBroker()).getTimeBudget();
		
		long volume = 0;
		double maxDensity = 0;
		
		double lateness = getLateness(now); 
		
		Query currentOriginalQuery = (Query) currentRunningQuery.getOriginalRequest();
		int pcost = Math.max(0, (currentRunningQuery.getPredictedProcessingCost() + currentRunningQuery.getPredictedProcessingCostRMSE()) - currentRunningQuery.getProcessedPostings()); 		
		volume+=pcost;
		double currentDeadline = currentOriginalQuery.getArrivalTime().getTimeMicroseconds() + timeBudget.getTimeMicroseconds() - lateness;
		if (currentDeadline <= now) {
			
			core.setMaxFrequency(now);
			return;
		
		} else {
		
			maxDensity = ((double)volume) / (currentDeadline - now);
		}
		
		for (Request r : queue) {
			
			Query q = (Query) r;
			volume += q.getPredictedProcessingCost((Shard) server.getShard()) + q.getPredictedProcessingCostRMSE((Shard) server.getShard());
			
			double deadline = q.getArrivalTime().getTimeMicroseconds() + timeBudget.getTimeMicroseconds() - lateness;
			double density;
			if (deadline <= now) {
				
				core.setMaxFrequency(now);
				return;
				
			} else {
				
				density = ((double)volume) / (deadline - now);
			}
			
			if (density > maxDensity) 
				maxDensity = density;
		}
		
		double targetTime = Math.min((pcost/maxDensity) / 1000.0,//ms
				timeBudget.getTimeMicroseconds() / 1000.0);
		int frequency = identifyTargetFrequency(currentRunningQuery.getNumberOfTerms(), pcost, targetTime);
		core.setFrequency(frequency, now);		
	}
	
	private int identifyTargetFrequency(int numOfTerms, int postings, double targetTime) {
	
		QueryEfficiencyPredictors qep = ((QueryBroker)server.getDatastoreReplica().getBroker()).getQueryEfficiencyPredictors();
		
		for (int frequency : core.getCpu().getFrequencies()) {
			
			double time = qep.regress(numOfTerms, postings, frequency);
			if (time <= targetTime) return frequency;
		}		
		return core.getCpu().getMaxFrequency();
	}

	private double getLateness(long now) {

		Time timeBudget = ((QueryBroker)server.getDatastoreReplica().getBroker()).getTimeBudget();
		
		double lateness = 0;
		int cnt = 0;
		
		long generalBudget = timeBudget.getTimeMicroseconds();
		
		Query sqCurrent = (Query) currentRunningQuery.getOriginalRequest();
		int pcost = Math.max(0, (currentRunningQuery.getPredictedProcessingCost() + currentRunningQuery.getPredictedProcessingCostRMSE()) - currentRunningQuery.getProcessedPostings()); 		
		double currentRemaining = predictServiceTimeAtMaxFreq(currentRunningQuery.getNumberOfTerms(), pcost) * 1000.0;
		double currentBudget = Math.max(0, generalBudget - (now - sqCurrent.getArrivalTime().getTimeMicroseconds()));
		
		if (currentRemaining > currentBudget) {
			
			lateness += currentRemaining - currentBudget;
		
		} else {
			
			cnt++;
		}
		
		Shard shard = (Shard) server.getShard();
		
		for (Request r : queue) {
			
			Query q = (Query) r;
			double remainingTime4q = predictServiceTimeAtMaxFreq(q.getNumberOfTerms(shard), q.getPredictedProcessingCost(shard)+q.getPredictedProcessingCostRMSE(shard)) * 1000;
			double budget = Math.max(0, generalBudget - (now - q.getArrivalTime().getTimeMicroseconds()));
			if (remainingTime4q > budget) {
				
				lateness += remainingTime4q - budget;
			
			} else {
				
				cnt++;
			}
		}
		
		
		return Math.ceil(lateness / cnt);
	}

	private double predictServiceTimeAtMaxFreq(int numOfTerms, int postings) {
		
		QueryEfficiencyPredictors qep = ((QueryBroker)server.getDatastoreReplica().getBroker()).getQueryEfficiencyPredictors();
		
		int max = core.getCpu().getMaxFrequency();
		return qep.regress(numOfTerms, postings, max); 
	}

	@Override
	protected Request dequeueRequest() {
		
		Request rtn = super.dequeueRequest();
		runFrequencyScheduler(server.getSimulator().now().getTimeMicroseconds());
		return rtn;
	}

}