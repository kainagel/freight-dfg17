package testMutualreplanningWithOfferUpdate;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.contrib.freight.carrier.CarrierService;

import org.matsim.contrib.freight.events.LSPServiceStartEvent;
import org.matsim.contrib.freight.events.eventhandler.LSPServiceStartEventHandler;
import org.matsim.contrib.freight.events.LSPServiceEndEvent;
import org.matsim.contrib.freight.events.eventhandler.LSPServiceEndEventHandler;
import org.matsim.vehicles.Vehicle;


public class CollectionServiceHandler implements LSPServiceStartEventHandler, LSPServiceEndEventHandler {

	
	
	private class ServiceTuple {
		private CarrierService service;
		private double startTime;
		
		public ServiceTuple(CarrierService service, double startTime) {
			this.service = service;
			this.startTime = startTime;
		}

		public CarrierService getService() {
			return service;
		}

		public double getStartTime() {
			return startTime;
		}
		
	}

	private Collection<ServiceTuple> tuples;
	private double totalLoadingCosts;
	private int totalNumberOfShipments;
	private int totalWeightOfShipments;
	
	public  CollectionServiceHandler() {
		this.tuples = new ArrayList<ServiceTuple>();
	}
	
	@Override
	public void reset(int iteration) {
			tuples.clear();
			totalNumberOfShipments = 0;
			totalWeightOfShipments = 0;
			totalLoadingCosts = 0;
	}

	@Override
	public void handleEvent(LSPServiceEndEvent event) {
		double loadingCosts = 0;
		for(ServiceTuple tuple : tuples) {
			if(tuple.getService() == event.getService()) {
				double serviceDuration = event.getTime() - tuple.getStartTime();
				loadingCosts = serviceDuration * ((Vehicle) event.getVehicle()).getType().getCostInformation().getPerTimeUnit();
				totalLoadingCosts = totalLoadingCosts + loadingCosts;
				tuples.remove(tuple);
				break;
			}
		}
	}

	@Override
	public void handleEvent(LSPServiceStartEvent event) {
		totalNumberOfShipments++;
		totalWeightOfShipments = totalWeightOfShipments + event.getService().getCapacityDemand();
		tuples.add(new ServiceTuple(event.getService(), event.getTime()));
	}

	public double getTotalLoadingCosts() {
		return totalLoadingCosts;
	}

	public int getTotalNumberOfShipments() {
		return totalNumberOfShipments;
	}

	public int getTotalWeightOfShipments() {
		return totalWeightOfShipments;
	}
	
}
