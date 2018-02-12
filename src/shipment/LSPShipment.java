package shipment;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.handler.EventHandler;

public interface LSPShipment {

	public Id<LSPShipment> getId();
	
	public Id<Link> getFromLinkId();
	
	public Id<Link> getToLinkId();
	
	public TimeWindow getStartTimeWindow();
	
	public TimeWindow getEndTimeWindow();
	
	public AbstractShipmentPlan getSchedule();
	
	public AbstractShipmentPlan getLog();
	
	public int getCapacityDemand();
	
	public double getServiceTime();
	
	public Collection<EventHandler> getEventHandlers();
	
}
