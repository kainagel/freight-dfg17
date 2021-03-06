package lsp.shipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.matsim.api.core.v01.Id;



/* package-private */ class Schedule implements ShipmentPlan {

	static class ScheduleElementComparator implements Comparator<ShipmentPlanElement>{

		@Override
		public int compare(ShipmentPlanElement o1, ShipmentPlanElement o2) {
			if(o1.getStartTime() > o2.getStartTime()){
				return 1;	
			}
			if(o1.getStartTime() < o2.getStartTime()){
				return -1;
			}
			if(o1.getStartTime() == o2.getStartTime()) {
				if(o1.getEndTime() > o2.getEndTime()) {
					return 1;
				}
				if(o1.getEndTime() < o2.getEndTime()) {
					return -1;
				}
			}
			return 0;	
		}	
	}
	
	private LSPShipment shipment;
	private HashMap<Id<ShipmentPlanElement> , ShipmentPlanElement> scheduleElements;


	/* package-private */ Schedule(LSPShipment shipment){
		this.shipment = shipment;
		this.scheduleElements = new HashMap<Id<ShipmentPlanElement> , ShipmentPlanElement>();
	}
	

	public LSPShipment getShipment() {
		return shipment;
	}

	public HashMap<Id<ShipmentPlanElement> , ShipmentPlanElement> getPlanElements() {
		return scheduleElements;
	}

	public void addPlanElement(Id<ShipmentPlanElement> id, ShipmentPlanElement element) {
		scheduleElements.put(id, element);
	}


	@Override
	public ShipmentPlanElement getMostRecentEntry() {
		ArrayList<ShipmentPlanElement> scheduleList =  new ArrayList<ShipmentPlanElement>(scheduleElements.values());
		Collections.sort(scheduleList, new ScheduleElementComparator());
		Collections.reverse(scheduleList);
		return scheduleList.get(0);
	}
	
	@Override
	public void clear() {
		scheduleElements.clear();
	}

}

	

	