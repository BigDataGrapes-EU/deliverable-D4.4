package it.cnr.isti.hpclab.example.search;

import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.engine.RequestProcessingThread;
import it.cnr.isti.hpclab.request.Request;
import it.cnr.isti.hpclab.request.RunningRequest;

/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.request.RunningRequest}
 * @author Matteo Catena
 *
 */
public class RunningQuery extends RunningRequest {

	public RunningQuery(Time time, RequestProcessingThread processor, Request originalQuery) {
		super(time, processor, originalQuery);
	}

	/**
	 * Get the completion ratio of the query, i.e., the how much of the query 
	 * has been processed with respect of the total amount of work required to 
	 * complete it.
	 * @return
	 */
	private double getCompletionRatio() {
		
		return ((double) executedTime) / requiredTime;
	}

	/**
	 * Get the predicted processing cost of this query (i.e., the estimated number 
	 * of postings to score for completing its processing) according to some
	 * query performance predictor.
	 * @return
	 */
	public int getPredictedProcessingCost() {
		
		return ((Query) originalRequest).getPredictedProcessingCost((Shard) thread.getShardServer().getShard());
	}

	/**
	 * Get the root-mean-squared-error for the predicted processing cost of this query (i.e., the estimated number 
	 * of postings to score for completing its processing) according to some
	 * query performance predictor.
	 * @return
	 */
	public int getPredictedProcessingCostRMSE() {
		
		return ((Query) originalRequest).getPredictedProcessingCostRMSE((Shard) thread.getShardServer().getShard());
	}

	/**
	 * Estimate how many postings have been already scored for this query, according to
	 * its predicted processing cost and its current completion ratio.
	 * @return
	 */
	public int getProcessedPostings() {
		
		return (int) Math.floor((getPredictedProcessingCost() + getPredictedProcessingCostRMSE()) * getCompletionRatio());
		
	}

	/**
	 * Get the number of terms of this query.
	 * @return
	 */
	public int getNumberOfTerms() {
		
		return ((Query) originalRequest).getNumberOfTerms((Shard) thread.getShardServer().getShard());
	}
}