package it.cnr.isti.hpclab.simulator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.zip.GZIPOutputStream;

import eu.nicecode.simulator.Event;
import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.exception.TimeException;
import it.cnr.isti.hpclab.engine.RequestBroker;
import it.cnr.isti.hpclab.request.RequestSource;
import it.cnr.isti.hpclab.util.Clock;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * This class implements a {@link eu.nicecode.simulator.Simulator} for an OnLine Data-Intensive service (e.g., a Web search engine) 
 * 
 * @author Matteo Catena
 */
public class OLDISimulator extends Simulator {

	private RequestSource source;
	private RequestBroker broker;
	
	private int nonClockCnt; /* this counts non-clock events in the event queue */
	
	private PrintWriter pw;
	
	private Int2DoubleMap sec2energyConsumption;

	/**
	 * 
	 * @param output the path for the output file (it will be gzipped)
	 * @param simulationDurationInHours the number of hours to be simulated
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public OLDISimulator(String output, int simulationDurationInHours) throws FileNotFoundException, IOException {
		
		GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(output));	
		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(gzip)));
		sec2energyConsumption = new Int2DoubleOpenHashMap();
		
	}
	
	/**
	 * Set the source of incoming requests to be processed by this simulator
	 * @param source
	 */
	public void setRequestSource(RequestSource source) {

		this.source = source;
	}

	@Override
	public boolean isDone() {

		return source.isDone() && nonClockCnt <= 0;
	}
	
	@Override
	public void doAllEvents() {
		
		try  {
			
			while (true) {
				
				Event e = events.dequeue();
				
				if (time.compareTo(e.getTime()) <= 0)	{
					
					time.setTime(e.getTime());
					
				} else {
					
					throw new TimeException("You can't go back in time!");
					
				}
				
				e.execute(this);
				
				if (!(e instanceof Clock)) nonClockCnt--;
			}
			
		} catch (NoSuchElementException nsee)  {
		
			//end
		}
		
		broker.shutdown(now().getTimeMicroseconds());
		printEnergy();
		pw.close();
	}

	
	private void printEnergy() {
		
		for (int sec = 0; sec < sec2energyConsumption.size(); sec++){
			
			println(String.format("[energy] %d %.3f", sec, sec2energyConsumption.get(sec)));
		}
		
	}

	@Override
	public void schedule(Event e) {
		
		if (!(e instanceof Clock)) nonClockCnt++;
		events.enqueue(e);
	}

	/**
	 * Set the broker which will process the incoming requests
	 * @param broker
	 */
	public void setBroker(RequestBroker broker) {

		this.broker = broker;
	}

	/**
	 * Print any string to the output file
	 * @param string
	 */
	public void println(String string) {

		pw.println(string);
	}
	
	/**
	 * Update the consumption of energy of the simulated service. The update 
	 * starts at time {@code statusChangeTimeInMicros}, and the service is 
	 * consuming {@code power} Watts for {@code second} seconds.
	 * @param power
	 * @param statusChangeTimeInMicros
	 * @param seconds
	 */
	public void updateEnergyConsumption(double power, long statusChangeTimeInMicros, double seconds) {

		long start = statusChangeTimeInMicros;
        int start_sec = (int) (start / 1_000_000); //start in seconds
        int end_sec = (int) ((start + (seconds * 1_000_000)) / 1_000_000); //end in seconds
        double energy_fraction = (power * seconds) / Math.max(1, end_sec - start_sec);
        for (int b = start_sec; b <= end_sec; b++) 
        	sec2energyConsumption.put(b, sec2energyConsumption.get(b)+ energy_fraction);
	}
	
	/**
	 * Set every core operating frequency to {@code frequency}.
	 * 
     * @param frequency The target frequency
	 */
	public void setFrequency(int freq) {
		
		broker.setFrequency(freq, now().getTimeMicroseconds());
	}
}