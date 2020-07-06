package example.lsp.simulationTrackers;

import lsp.events.TourStartEvent;
import lsp.eventhandlers.TourStartEventHandler;

/*package-private*/ class TourStartHandler implements TourStartEventHandler{

	private double vehicleFixedCosts;
		
	@Override
	public void reset(int iteration) {
		vehicleFixedCosts = 0;
	}

	@Override
	public void handleEvent(TourStartEvent event) {
		vehicleFixedCosts = vehicleFixedCosts + event.getVehicle().getVehicleType().getCostInformation().getFix();
	}

	public double getVehicleFixedCosts() {
		return vehicleFixedCosts;
	}

}