package it.cnr.isti.hpclab.example.search;

import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.request.Request;

/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.request.Request}
 * @author Matteo Catena
 *
 */
public class Query extends Request {

	public Query(Time arrivalTime, long rid, long uid) {
		super(arrivalTime, rid, uid);
	}

	/**
	 * Get the predicted processing cost of this query (i.e., the estimated number 
	 * of postings to score for completing its processing) according to some
	 * query performance predictor.
	 * 
	 * @param shard The shard on which the query is being processed (different shards lead to different costs)
	 * 
	 * @return
	 */
	public int getPredictedProcessingCost(Shard shard) {
		
		return shard.getPredictedProcessingCost(rid);
	}

	/**
	 * Get the root-mean-squared-error for the predicted processing cost of this query (i.e., the estimated number 
	 * of postings to score for completing its processing) according to some
	 * query performance predictor.
	 * 
     * @param shard The shard on which the query is being processed (different shards lead to different costs)
	 * 
	 * @return
	 */
	public int getPredictedProcessingCostRMSE(Shard shard) {
		
		return shard.getPredictedProcessingCostRMSE(rid);
	}

	/**
	 * Get the number of terms of this query.
	 * 
     * @param shard The shard on which the query is being processed
	 * @return
	 */
	public int getNumberOfTerms(Shard shard) {
		
		return shard.getNumberOfTerms(rid);
	}
}