package lsp.usecase;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.handler.EventHandler;

import lsp.functions.LSPInfo;
import lsp.LogisticsSolutionElement;
import lsp.resources.LSPCarrierResource;
import lsp.resources.LSPResource;
import lsp.controler.LSPSimulationTracker;

/*package-private*/ class DistributionCarrierAdapter implements LSPCarrierResource {

	private Id<LSPResource>id;
	private Carrier carrier;
	private Id<Link> locationLinkId;
	private ArrayList<LogisticsSolutionElement> clientElements;
	private DistributionCarrierScheduler distributionHandler;
	private Network network;
	private Collection<EventHandler> eventHandlers;
	private Collection<LSPInfo> infos;
	private Collection<LSPSimulationTracker> trackers;
	private EventsManager eventsManager;

	DistributionCarrierAdapter(UsecaseUtils.DistributionCarrierAdapterBuilder builder){
			this.id = builder.id;
			this.locationLinkId = builder.locationLinkId;
			this.distributionHandler = builder.distributionHandler;
			this.clientElements = builder.clientElements;
			this.carrier = builder.carrier;
			this.network = builder.network;
			this.eventHandlers = new ArrayList<EventHandler>();
			this.infos = new ArrayList<LSPInfo>();
			this.trackers = new ArrayList<LSPSimulationTracker>();
		}
	
	@Override
	public Class<? extends Carrier> getClassOfResource() {
		return carrier.getClass();
	}

	@Override
	public Id<Link> getStartLinkId() {
		Id<Link> depotLinkId = null;
		for(CarrierVehicle vehicle : carrier.getCarrierCapabilities().getCarrierVehicles().values()){
			if(depotLinkId == null || depotLinkId == vehicle.getLocation()){
				depotLinkId = vehicle.getLocation();
			}
			
		}
		
		return depotLinkId;
		
	}

	@Override
	public Id<Link> getEndLinkId() {
		Id<Link> depotLinkId = null;
		for(CarrierVehicle vehicle : carrier.getCarrierCapabilities().getCarrierVehicles().values()){
			if(depotLinkId == null || depotLinkId == vehicle.getLocation()){
				depotLinkId = vehicle.getLocation();
			}
			
		}
		
		return depotLinkId;
	
	}

	@Override
	public Id<LSPResource> getId() {
		return id;
	}
	
	@Override
	public Collection<LogisticsSolutionElement> getClientElements() {
		return clientElements;
	}

	@Override
	public void schedule(int bufferTime) {
		distributionHandler.scheduleShipments(this, bufferTime);
		
	}

	public Network getNetwork(){
		return network;
	}
	
	public Carrier getCarrier(){
		return carrier;
	}

	public Collection <EventHandler> getEventHandlers(){
		return eventHandlers;
	}

	@Override
	public Collection<LSPInfo> getInfos() {
		return infos;
	}
	
	@Override
	public void addSimulationTracker( LSPSimulationTracker tracker ) {
		this.trackers.add(tracker);
		this.eventHandlers.addAll(tracker.getEventHandlers());
		this.infos.addAll(tracker.getInfos());	
	}

	@Override
	public Collection<LSPSimulationTracker> getSimulationTrackers() {
		return trackers;
	}

	@Override
	public void setEventsManager(EventsManager eventsManager) {
		this.eventsManager = eventsManager;
	}

}
