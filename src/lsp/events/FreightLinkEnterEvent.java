package lsp.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.vehicles.Vehicle;


//Achtung: Das darf nicht von LinkEnterEvent erben, weil sonst im CarrierResourceTracker immer wenn ein ein solches event hergestellt wird, der CarrierResourceAgent +
//denkt ein neues LinkEnterEvent w�re geworfen worden -> Endlosschleife

public class FreightLinkEnterEvent extends Event{

	public static final String EVENT_TYPE = "freight vehicle entered link";
	public static final String ATTRIBUTE_VEHICLE = "vehicle";
	public static final String ATTRIBUTE_LINK = "link";
	public static final String ATTRIBUTE_CARRIER = "carrier";
	public static final String ATTRIBUTE_DRIVER = "driver";
	
	private CarrierVehicle carrierVehicle;
	private Id<Carrier> carrierId;
	private Id<Person> driverId;
	private Id<Vehicle> vehicleId; 
	private Id<Link>linkId; 
	
	public FreightLinkEnterEvent(Id<Carrier>carrierId, Id<Vehicle> vehicleId, Id<Person>driverId, Id<Link>linkId, double time, CarrierVehicle vehicle) {
		super(time);
		this.carrierVehicle = vehicle ;
		this.carrierId = carrierId;
		this.driverId = driverId;
		this.vehicleId = vehicleId; 
		this.linkId = linkId; 
	}

	public CarrierVehicle getCarrierVehicle() {
		return carrierVehicle;
	}

	public Id<Carrier> getCarrierId() {
		return carrierId;
	}

	public Id<Person> getDriverId() {
		return driverId;	
	}

	public Id<Vehicle> getVehicleId() {
		return vehicleId;
	}

	public Id<Link> getLinkId() {
		return linkId;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_VEHICLE, this.vehicleId.toString());
		attr.put(ATTRIBUTE_LINK, this.linkId.toString());
		attr.put(ATTRIBUTE_CARRIER, this.carrierId.toString());
		attr.put(ATTRIBUTE_DRIVER, this.driverId.toString());
		return attr;
	}
	
}
