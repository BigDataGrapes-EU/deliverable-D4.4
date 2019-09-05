package it.cnr.isti.hpclab.example.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import eu.nicecode.simulator.Time;
import it.unimi.dsi.fastutil.longs.Long2LongArrayMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * A query template to generate query instances. Query with the same {@code qid}
 * should have the same query template (i.e., same number of terms, same service 
 * times, etc. etc.)
 * @author Matteo Catena
 */
class QueryTemplate {
	
	int numberOfTerms;
	int predictedProcessingCost;
	int predictedProcessingCostRMSE;
	Long2LongMap serviceTimes;
	
	QueryTemplate() {

		serviceTimes = new Long2LongArrayMap();
		
	}
	
}

/**
 * An inverted index shard implementation.
 * @author Matteo Catena
 */
public class Shard extends it.cnr.isti.hpclab.engine.Shard {

	protected Long2ObjectMap<QueryTemplate> templates;
	
	/**
	 * 
	 * @param filenamePPC the filepath to the predicted processing cost (ppc) data
	 * @param filenameTimes the filepath to the service times per CPU frequency
	 * @param frequencies The available CPU frequencies on the shard servers.
	 * @throws IOException
	 */
	public Shard(String filenamePPC, String filenameTimes, int... frequencies) throws IOException {
		
		templates = new Long2ObjectOpenHashMap<>();
		
		InputStream is1 = Shard.class.getClassLoader().getResourceAsStream(filenamePPC);
		BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));
		String line1 = null;
		while ((line1 = br1.readLine()) != null) {
			String fields[] = line1.split(" ");
			long qid = Long.parseLong(fields[0]);
			int numberOfTerms = Integer.parseInt(fields[1]);
			int predictedProcessingCost = Integer.parseInt(fields[2]);
			int predictedProcessingCostRMSE = Integer.parseInt(fields[3]);
			QueryTemplate qt = new QueryTemplate();
			qt.numberOfTerms = numberOfTerms;
			qt.predictedProcessingCost = predictedProcessingCost;
			qt.predictedProcessingCostRMSE = predictedProcessingCostRMSE;
			templates.put(qid, qt);
		}
		br1.close();
		
		InputStream is2 = Shard.class.getClassLoader().getResourceAsStream(filenameTimes);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(is2)); 
		String line2 = null;
		while ((line2 = br2.readLine()) != null) {
			String fields[] = line2.split(" ");
			long qid = Long.parseLong(fields[0]);
			QueryTemplate qt = templates.get(qid);
			for (int i = 1; i < fields.length; i++) {
				Float t = Float.parseFloat(fields[i]) * 1000; //ms to micros
				qt.serviceTimes.put(frequencies[i-1], t.longValue());
			}
		}
		br2.close();
		
	}
	
	@Override
	public Time getServiceTime(long qid, int frequency) {
		
		return new Time(templates.get(qid).serviceTimes.get(frequency), TimeUnit.MICROSECONDS);
	}

	/**
	 * Get the predicted processing cost (i.e., the predicted number of postings to score), according
	 * to some query efficiency predictor.
	 * @param qid The query identifier
	 * @return
	 */
	public int getPredictedProcessingCost(long qid) {
		
		return templates.get(qid).predictedProcessingCost;
	}

	/**
	 * Get the root-mean-squared-error for predicting the processing costs (i.e., the predicted number of postings to score), according
	 * to some query efficiency predictor.
	 * @param qid The query identifier
	 * @return
	 */
	public int getPredictedProcessingCostRMSE(long qid) {
		
		return templates.get(qid).predictedProcessingCostRMSE;
	}
	
	/**
	 * Get the number of query terms for query {@code qid}.
	 * @param qid Tge query identifier
	 * @return
	 */
	public int getNumberOfTerms(long qid) {
		
		return templates.get(qid).numberOfTerms;
	}
}