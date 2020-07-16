package testMutualreplanningWithOfferUpdate;

import org.matsim.contrib.freight.events.eventhandler.LSPLinkLeaveEventHandler;
import org.matsim.contrib.freight.events.eventhandler.LSPVehicleLeavesTrafficEventHandler;
import org.matsim.contrib.freight.events.eventhandler.LSPLinkEnterEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.events.LSPFreightLinkEnterEvent;
import org.matsim.contrib.freight.events.LSPFreightLinkLeaveEvent;
import org.matsim.contrib.freight.events.LSPFreightVehicleLeavesTrafficEvent;

import java.util.ArrayList;
import java.util.Collection;

public class DistanceAndTimeHandler implements LSPLinkEnterEventHandler, LSPVehicleLeavesTrafficEventHandler, LSPLinkLeaveEventHandler{

	private Collection<LSPFreightLinkEnterEvent> events;
	private double distanceCosts;
	private double timeCosts;
	private Network network;
	
	public DistanceAndTimeHandler(Network network) {
		this.network = network;
		this.events = new ArrayList<LSPFreightLinkEnterEvent>();
	}
	
	
	@Override
	public void handleEvent(LSPFreightLinkEnterEvent event) {
		events.add(event);
		
	}

	@Override
	public void reset(int iteration) {
		events.clear();
		distanceCosts = 0;
		timeCosts = 0;	
	}


	@Override
	public void handleEvent(LSPFreightVehicleLeavesTrafficEvent leaveEvent) {
		for(LSPFreightLinkEnterEvent enterEvent : events) {
			if((enterEvent.getLinkId() == leaveEvent.getLinkId()) && (enterEvent.getVehicleId() == leaveEvent.getVehicleId()) && 
			   (enterEvent.getCarrierId() == leaveEvent.getCarrierId())   &&  (enterEvent.getDriverId() == leaveEvent.getDriverId())) {
				double linkDuration = leaveEvent.getTime() - enterEvent.getTime();
				timeCosts = timeCosts + (linkDuration * enterEvent.getCarrierVehicle().getVehicleType().getCostInformation().getPerTimeUnit());
				double linkLength = network.getLinks().get(enterEvent.getLinkId()).getLength();
				distanceCosts = distanceCosts + (linkLength * enterEvent.getCarrierVehicle().getVehicleType().getCostInformation().getPerDistanceUnit());
				events.remove(enterEvent);
				break;
			}		
		}	
	}


	@Override
	public void handleEvent(LSPFreightLinkLeaveEvent leaveEvent) {
		for(LSPFreightLinkEnterEvent enterEvent : events) {
			if((enterEvent.getLinkId() == leaveEvent.getLinkId()) && (enterEvent.getVehicleId() == leaveEvent.getVehicleId()) && 
			   (enterEvent.getCarrierId() == leaveEvent.getCarrierId())   &&  (enterEvent.getDriverId() == leaveEvent.getDriverId())) {
				double linkDuration = leaveEvent.getTime() - enterEvent.getTime();
				timeCosts = timeCosts + (linkDuration * enterEvent.getCarrierVehicle().getVehicleType().getCostInformation().getPerTimeUnit());
				double linkLength = network.getLinks().get(enterEvent.getLinkId()).getLength();
				distanceCosts = distanceCosts + (linkLength * enterEvent.getCarrierVehicle().getVehicleType().getCostInformation().getPerDistanceUnit());
				events.remove(enterEvent);
				break;
			}		
		}	
	}

	public double getDistanceCosts() {
		return distanceCosts;
	}

	public double getTimeCosts() {
		return timeCosts;
	}
	
}
