package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;

public class ServiceStartEventCreator implements EventCreator{

	@Override
	public Event createEvent(Event event, Carrier carrier, Activity activity, ScheduledTour scheduledTour,
			Id<Person> driverId, int activityCounter) {
		if(event instanceof ActivityStartEvent){
			ActivityStartEvent startEvent = (ActivityStartEvent) event;
			if(startEvent.getActType() == "service") {
				TourElement element = scheduledTour.getTour().getTourElements().get(activityCounter);
				if(element instanceof ServiceActivity) {
					ServiceActivity serviceActivity = (ServiceActivity) element;
					return new ServiceStartEvent(startEvent, carrier.getId(), driverId, serviceActivity.getService(), event.getTime(), scheduledTour.getVehicle());
				}
			}	
		}
		return null;
	}
}	
