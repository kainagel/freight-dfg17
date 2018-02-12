package lsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import shipment.LSPShipment;
import shipment.ShipmentComparator;

public class WaitingShipmentsImpl implements WaitingShipments {

		
	private ArrayList<ShipmentTuple> shipments;
	
	public WaitingShipmentsImpl() {
		this.shipments = new ArrayList<ShipmentTuple>();
	}
	
	
	@Override
	public void addShipment(double time, LSPShipment shipment) {
		ShipmentTuple tuple = new ShipmentTuple(time, shipment);
		this.shipments.add(tuple);
		Collections.sort(shipments, new ShipmentComparator());
	}

	@Override
	public Collection <ShipmentTuple> getSortedShipments() {
		Collections.sort(shipments, new ShipmentComparator());
		return shipments;
	}

	public void clear(){
		shipments.clear();
	}

	@Override
	public Collection<ShipmentTuple> getShipments() {
		return shipments;
	}
		
}
