package testLSPWithCostTracker;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.network.Network;

import lsp.events.LSPFreightLinkEnterEvent;
import lsp.events.LSPFreightLinkLeaveEvent;
import lsp.eventhandlers.LSPLinkLeaveEventHandler;
import lsp.events.LSPFreightVehicleLeavesTrafficEvent;
import lsp.eventhandlers.LSPVehicleLeavesTrafficEventHandler;
import lsp.eventhandlers.LSPLinkEnterEventHandler;


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
	}


	@Override
	public void handleEvent(LSPFreightVehicleLeavesTrafficEvent leaveEvent) {
		for(LSPFreightLinkEnterEvent enterEvent : events) {
			if(enterEvent != null) {
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
	}
	

	@Override
	public void handleEvent(LSPFreightLinkLeaveEvent leaveEvent) {
		if(events.size() > 0) {
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
	}

	public double getDistanceCosts() {
		return distanceCosts;
	}

	public double getTimeCosts() {
		return timeCosts;
	}
	
}
