package adapterTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;
import usecase.ReloadingPoint;
import usecase.ReloadingPointScheduler;


public class FirstReloadAdapterTest {

	private  Id<Resource> reloadingId;
	private Id<Link> reloadingLinkId;
	private ReloadingPoint reloadingPoint;
	private ReloadingPointScheduler scheduler;
	
	@Before
	public void initialize(){
		
        
        ReloadingPointScheduler.Builder schedulerBuilder =  ReloadingPointScheduler.Builder.newInstance();
        schedulerBuilder.setCapacityNeedFixed(10);
        schedulerBuilder.setCapacityNeedLinear(1);
        scheduler = schedulerBuilder.build();
        
        reloadingId = Id.create("ReloadingPoint1", Resource.class);
        reloadingLinkId = Id.createLinkId("(4 2) (4 3)");
        
        ReloadingPoint.Builder reloadingPointBuilder = ReloadingPoint.Builder.newInstance(reloadingId, reloadingLinkId);
        reloadingPointBuilder.setReloadingScheduler(scheduler);
        reloadingPoint = reloadingPointBuilder.build();
	}
	
	@Test
	public void reloadingPointTest() {
		assertTrue(reloadingPoint.getCapacityNeedFixed() == 10);
		assertTrue(reloadingPoint.getCapacityNeedLinear() == 1);
		assertFalse(CarrierResource.class.isAssignableFrom(reloadingPoint.getClass()));
		assertTrue(reloadingPoint.getClassOfResource() == ReloadingPoint.class);
		assertTrue(reloadingPoint.getClientElements() != null);
		assertTrue(reloadingPoint.getClientElements().isEmpty());
		assertTrue(reloadingPoint.getEndLinkId() == reloadingLinkId);
		assertTrue(reloadingPoint.getStartLinkId() == reloadingLinkId);
		assertTrue(reloadingPoint.getEventHandlers() != null);
		assertFalse(reloadingPoint.getEventHandlers().isEmpty());
		assertTrue(reloadingPoint.getEventHandlers().size() == 1);
		assertTrue(reloadingPoint.getInfos() != null);
		assertTrue(reloadingPoint.getInfos().isEmpty());
	}
}
