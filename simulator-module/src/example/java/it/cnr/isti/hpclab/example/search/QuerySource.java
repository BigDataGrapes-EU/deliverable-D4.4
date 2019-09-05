package it.cnr.isti.hpclab.example.search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import eu.nicecode.queueing.Request;
import eu.nicecode.queueing.event.RequestArrival;
import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.request.RequestSource;

/**
 * 
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.request.RunningRequest}
 * 
 * @author Matteo Catena
 *
 */
public class QuerySource extends RequestSource {

	private BufferedReader brQueries;
	private boolean isBrQueriesClosed;
	private int uidCnt = 0;
		
	/**
	 * 
	 * @param simulator The simulator
	 * @param queriesFilePath Path to the file containing the query ids and their arrivals
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public QuerySource(Simulator simulator, String queriesFilePath) throws FileNotFoundException, IOException {
						
		// open files
		InputStream is = Shard.class.getClassLoader().getResourceAsStream(queriesFilePath);
		brQueries = new BufferedReader(new InputStreamReader(is));
		
	}

	@Override
	public void generate(Simulator simulator, Agent to) {

		Request request = nextRequest();

		if (request != null) {

			RequestArrival ra = new RequestArrival(request.getArrivalTime(), this, to, request);
			simulator.schedule(ra);
			
		}
	}

	@Override
	public Request nextRequest() {

		if (isBrQueriesClosed) {
			
			return null;
		}
		
		String queryString = null;
		try {

			queryString = brQueries.readLine();

		} catch (IOException e) {

			//logger.error("Error generating next request", e);
		}

		if (queryString == null) {

			try {
				
				brQueries.close();
				isBrQueriesClosed = true;
			
			} catch (IOException e) {
				
				//logger.error("Error while closing a BufferedReader in {}", QuerySource.class.getSimpleName(), e);
			}
			
			return null;

		} else {

			String fields[] = queryString.split(" ");
			
			
			Time arrivalTime = new Time(Long.parseLong(fields[0]), TimeUnit.MILLISECONDS);
			
			long qid = Long.parseLong(fields[1]);
			long uid = uidCnt++;
			
					
			return new Query(arrivalTime, qid, uid);
			
		}
		
	}
	
	@Override
	public boolean isDone() {
		
		return isBrQueriesClosed;
	}
}