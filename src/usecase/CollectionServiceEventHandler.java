package usecase;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.CarrierService;

import events.ServiceCompletedEvent;
import events.ServiceCompletedEventHandler;
import lsp.LogisticsSolutionElement;
import lsp.resources.CarrierResource;
import lsp.resources.Resource;
import shipment.AbstractShipmentPlanElement;
import shipment.LSPShipment;
import shipment.LoggedShipmentLoad;
import shipment.LoggedShipmentTransport;

public class CollectionServiceEventHandler implements ServiceCompletedEventHandler {

	private CarrierService carrierService;
	private LSPShipment lspShipment;
	private LogisticsSolutionElement solutionElement;
	private CarrierResource resource;
	
	public CollectionServiceEventHandler(CarrierService carrierService, LSPShipment lspShipment, LogisticsSolutionElement element, CarrierResource resource){
		this.carrierService = carrierService;
		this.lspShipment = lspShipment;
		this.solutionElement = element;
		this.resource = resource;
	}
	
	
	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(ServiceCompletedEvent event) {
		if(event.getService().getId() == carrierService.getId() && event.getCarrierId() == resource.getCarrier().getId()){
			logTransport(event);
			logLoad(event);
		}	
	}

	private void logLoad(ServiceCompletedEvent event){
		LoggedShipmentLoad.Builder builder  =  LoggedShipmentLoad.Builder.newInstance();
		builder.setStartTime(event.getTime() - event.getService().getServiceDuration());
		builder.setEndTime(event.getTime());
		builder.setLogisticsSolutionElement(solutionElement);
		builder.setResourceId(resource.getId());
		builder.setLinkId(event.getService().getLocationLinkId());
		builder.setCarrierId(event.getCarrierId());
		LoggedShipmentLoad load = builder.build();
		String idString = load.getResourceId() + "" + load.getSolutionElement().getId() + "" + load.getElementType();
		Id<AbstractShipmentPlanElement> loadId = Id.create(idString, AbstractShipmentPlanElement.class);
		lspShipment.getLog().getPlanElements().put(loadId, load);
	}

	private void logTransport(ServiceCompletedEvent event){
		LoggedShipmentTransport.Builder builder  =  LoggedShipmentTransport.Builder.newInstance();
		builder.setStartTime(event.getTime());
		builder.setLogisticsSolutionElement(solutionElement);
		builder.setResourceId(resource.getId());
		builder.setFromLinkId(event.getService().getLocationLinkId());
		builder.setCarrierId(event.getCarrierId());
		LoggedShipmentTransport transport = builder.build();
		String idString = transport.getResourceId() + "" + transport.getSolutionElement().getId() + "" + transport.getElementType();
		Id<AbstractShipmentPlanElement> transportId = Id.create(idString, AbstractShipmentPlanElement.class);
		lspShipment.getLog().getPlanElements().put(transportId, transport);
	}


	public CarrierService getCarrierService() {
		return carrierService;
	}


	public LSPShipment getLspShipment() {
		return lspShipment;
	}


	public LogisticsSolutionElement getElement() {
		return solutionElement;
	}


	public Id<Resource> getResourceId() {
		return resource.getId();
	}


}
