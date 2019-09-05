package it.cnr.isti.hpclab.util;

import eu.nicecode.simulator.Event;
import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;

/**
 * Tics every period
 * @author Matteo Catena
 *
 */
public abstract class Clock extends Event {

	
	protected Time period;

	/**
	 * 
	 * @param start the start time
	 * @param period the tick period
	 */
	public Clock(Time start, Time period) {
		
		super(start);
		this.period = period;
		
	}
	
	/**
	 * 
	 * @param period the tick period
	 */
	public Clock(Time period) {
		
		this(Time.ZERO.clone(), period);
	}

	@Override
	public void execute(Simulator simulator) {
		
		//Do periodic stuff
		boolean keepGoing = doClock(simulator);
		
		//schedule next clock
		if (keepGoing && !isSimulationDone(simulator)) {
			
			time.addTime(period);
			simulator.schedule(this);
		}
	}
	
	protected boolean isSimulationDone(Simulator simulator) {
		
		return simulator.isDone();
	}
	
	/**
	 * 
	 * @param simulator
	 * @return true if the clock must keep running
	 */
	abstract protected boolean doClock(Simulator simulator);

}
