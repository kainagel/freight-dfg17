package cascadingInfoTest;

import java.util.ArrayList;
import java.util.Collection;

import events.ServiceBeginsEvent;
import events.ServiceBeginsEventHandler;
import events.TourStartEvent;
import events.TourStartEventHandler;



public class TimeSpanHandler implements TourStartEventHandler, ServiceBeginsEventHandler{

	private int numberOfStops;
	private double totalTime;
	
	private Collection<TourStartEvent> startEvents;
	
	public TimeSpanHandler() {
		startEvents = new ArrayList<TourStartEvent>();
	}
	
	@Override
	public void reset(int iteration) {
		totalTime = 0;
		numberOfStops = 0;
	}

	@Override
	public void handleEvent(ServiceBeginsEvent event) {
		numberOfStops++;
		for(TourStartEvent startEvent : startEvents) {
			if(startEvent.getDriverId() == event.getDriverId()) {
				double startTime = startEvent.getTime();
				double serviceTime = event.getTime();
				totalTime = totalTime + (serviceTime - startTime);
				break;
			}
		}
	}

	@Override
	public void handleEvent(TourStartEvent event) {
		startEvents.add(event);
	}

	public int getNumberOfStops() {
		return numberOfStops;
	}

	public double getTotalTime() {
		return totalTime;
	}

}
