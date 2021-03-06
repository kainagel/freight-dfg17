package solutionTests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import lsp.*;
import lsp.usecase.*;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.carrier.CarrierCapabilities.FleetSize;
import org.matsim.core.config.Config;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import lsp.resources.LSPResource;

public class CompleteSolutionTest {

	private Network network;
	private LogisticsSolutionElement collectionElement;
	private LogisticsSolutionElement firstReloadElement;
	private LogisticsSolutionElement mainRunElement;
	private LogisticsSolutionElement secondReloadElement;
	private LogisticsSolutionElement distributionElement;
	private LogisticsSolution solution;
	
	@Before
	public void initialize() {

		Config config = new Config();
		config.addCoreModules();
		Scenario scenario = ScenarioUtils.createScenario(config);
		new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
		this.network = scenario.getNetwork();

		Id<Carrier> collectionCarrierId = Id.create("CollectionCarrier", Carrier.class);
		Id<VehicleType> collectionVehicleTypeId = Id.create("CollectionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder collectionVehicleTypeBuilder = CarrierVehicleType.Builder
				.newInstance(collectionVehicleTypeId);
		collectionVehicleTypeBuilder.setCapacity(10);
		collectionVehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		collectionVehicleTypeBuilder.setCostPerTimeUnit(0.38);
		collectionVehicleTypeBuilder.setFixCost(49);
		collectionVehicleTypeBuilder.setMaxVelocity(50 / 3.6);
		org.matsim.vehicles.VehicleType collectionType = collectionVehicleTypeBuilder.build();

		Id<Link> collectionLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> collectionVehicleId = Id.createVehicleId("CollectionVehicle");
		CarrierVehicle collectionCarrierVehicle = CarrierVehicle.newInstance(collectionVehicleId, collectionLinkId);
		collectionCarrierVehicle.setType( collectionType );

		CarrierCapabilities.Builder collectionCapabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		collectionCapabilitiesBuilder.addType(collectionType);
		collectionCapabilitiesBuilder.addVehicle(collectionCarrierVehicle);
		collectionCapabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities collectionCapabilities = collectionCapabilitiesBuilder.build();
		Carrier collectionCarrier = CarrierUtils.createCarrier( collectionCarrierId );
		collectionCarrier.setCarrierCapabilities(collectionCapabilities);

		Id<LSPResource> collectionAdapterId = Id.create("CollectionCarrierAdapter", LSPResource.class);
		UsecaseUtils.CollectionCarrierAdapterBuilder collectionAdapterBuilder = UsecaseUtils.CollectionCarrierAdapterBuilder
				.newInstance(collectionAdapterId, network);
		collectionAdapterBuilder.setCollectionScheduler(UsecaseUtils.createDefaultCollectionCarrierScheduler());
		collectionAdapterBuilder.setCarrier(collectionCarrier);
		collectionAdapterBuilder.setLocationLinkId(collectionLinkId);

		Id<LogisticsSolutionElement> collectionElementId = Id.create("CollectionElement",
				LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder collectionBuilder = LSPUtils.LogisticsSolutionElementBuilder
				.newInstance(collectionElementId);
		collectionBuilder.setResource(collectionAdapterBuilder.build());
		collectionElement = collectionBuilder.build();

		UsecaseUtils.ReloadingPointSchedulerBuilder firstReloadingSchedulerBuilder = UsecaseUtils.ReloadingPointSchedulerBuilder.newInstance();
		firstReloadingSchedulerBuilder.setCapacityNeedFixed(10);
		firstReloadingSchedulerBuilder.setCapacityNeedLinear(1);

		Id<LSPResource> firstReloadingId = Id.create("ReloadingPoint1", LSPResource.class);
		Id<Link> firstReloadingLinkId = Id.createLinkId("(4 2) (4 3)");

		UsecaseUtils.ReloadingPointBuilder firstReloadingPointBuilder = UsecaseUtils.ReloadingPointBuilder.newInstance(firstReloadingId,
				firstReloadingLinkId);
		firstReloadingPointBuilder.setReloadingScheduler(firstReloadingSchedulerBuilder.build());

		Id<LogisticsSolutionElement> firstReloadingElementId = Id.create("FiretReloadElement",
				LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder firstReloadingElementBuilder = LSPUtils.LogisticsSolutionElementBuilder
				.newInstance(firstReloadingElementId);
		firstReloadingElementBuilder.setResource(firstReloadingPointBuilder.build());
		firstReloadElement = firstReloadingElementBuilder.build();

		Id<Carrier> mainRunCarrierId = Id.create("MainRunCarrier", Carrier.class);
		Id<VehicleType> mainRunVehicleTypeId = Id.create("MainRunCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder mainRunVehicleTypeBuilder = CarrierVehicleType.Builder
				.newInstance(collectionVehicleTypeId);
		mainRunVehicleTypeBuilder.setCapacity(30);
		mainRunVehicleTypeBuilder.setCostPerDistanceUnit(0.0002);
		mainRunVehicleTypeBuilder.setCostPerTimeUnit(0.38);
		mainRunVehicleTypeBuilder.setFixCost(120);
		mainRunVehicleTypeBuilder.setMaxVelocity(50 / 3.6);
		org.matsim.vehicles.VehicleType mainRunType = collectionVehicleTypeBuilder.build();

		Id<Link> fromLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> mainRunVehicleId = Id.createVehicleId("MainRunVehicle");
		CarrierVehicle mainRunCarrierVehicle = CarrierVehicle.newInstance(mainRunVehicleId, fromLinkId);
		mainRunCarrierVehicle.setType( mainRunType );

		CarrierCapabilities.Builder mainRunCapabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		mainRunCapabilitiesBuilder.addType(mainRunType);
		mainRunCapabilitiesBuilder.addVehicle(mainRunCarrierVehicle);
		mainRunCapabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities mainRunCapabilities = collectionCapabilitiesBuilder.build();
		Carrier mainRunCarrier = CarrierUtils.createCarrier( collectionCarrierId );
		mainRunCarrier.setCarrierCapabilities(mainRunCapabilities);

		Id<LSPResource> mainRunId = Id.create("MainRunAdapter", LSPResource.class);
		UsecaseUtils.MainRunCarrierAdapterBuilder mainRunAdapterBuilder = UsecaseUtils.MainRunCarrierAdapterBuilder.newInstance(mainRunId,
				network);
		mainRunAdapterBuilder.setMainRunCarrierScheduler(UsecaseUtils.createDefaultMainRunCarrierScheduler());
		mainRunAdapterBuilder.setFromLinkId(Id.createLinkId("(4 2) (4 3)"));
		mainRunAdapterBuilder.setToLinkId(Id.createLinkId("(14 2) (14 3)"));
		mainRunAdapterBuilder.setCarrier(collectionCarrier);

		Id<LogisticsSolutionElement> mainRunElementId = Id.create("MainRunElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder mainRunBuilder = LSPUtils.LogisticsSolutionElementBuilder
				.newInstance(mainRunElementId);
		mainRunBuilder.setResource(mainRunAdapterBuilder.build());
		mainRunElement = mainRunBuilder.build();

		UsecaseUtils.ReloadingPointSchedulerBuilder secondSchedulerBuilder = UsecaseUtils.ReloadingPointSchedulerBuilder.newInstance();
		secondSchedulerBuilder.setCapacityNeedFixed(10);
		secondSchedulerBuilder.setCapacityNeedLinear(1);

		Id<LSPResource> secondReloadingId = Id.create("ReloadingPoint2", LSPResource.class);
		Id<Link> secondReloadingLinkId = Id.createLinkId("(14 2) (14 3)");

		UsecaseUtils.ReloadingPointBuilder secondReloadingPointBuilder = UsecaseUtils.ReloadingPointBuilder.newInstance(secondReloadingId,
				secondReloadingLinkId);
		secondReloadingPointBuilder.setReloadingScheduler(secondSchedulerBuilder.build());

		Id<LogisticsSolutionElement> secondReloadingElementId = Id.create("SecondReloadElement",
				LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder secondReloadingElementBuilder = LSPUtils.LogisticsSolutionElementBuilder
				.newInstance(secondReloadingElementId);
		secondReloadingElementBuilder.setResource(secondReloadingPointBuilder.build());
		secondReloadElement = secondReloadingElementBuilder.build();

		Id<Carrier> distributionCarrierId = Id.create("DistributionCarrier", Carrier.class);
		Id<VehicleType> distributionVehicleTypeId = Id.create("DistributionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder dsitributionVehicleTypeBuilder = CarrierVehicleType.Builder
				.newInstance(distributionVehicleTypeId);
		dsitributionVehicleTypeBuilder.setCapacity(10);
		dsitributionVehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		dsitributionVehicleTypeBuilder.setCostPerTimeUnit(0.38);
		dsitributionVehicleTypeBuilder.setFixCost(49);
		dsitributionVehicleTypeBuilder.setMaxVelocity(50 / 3.6);
		org.matsim.vehicles.VehicleType distributionType = dsitributionVehicleTypeBuilder.build();

		Id<Link> distributionLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> distributionVehicleId = Id.createVehicleId("CollectionVehicle");
		CarrierVehicle distributionCarrierVehicle = CarrierVehicle.newInstance(distributionVehicleId,
				distributionLinkId);
		distributionCarrierVehicle.setType( distributionType );

		CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		capabilitiesBuilder.addType(distributionType);
		capabilitiesBuilder.addVehicle(distributionCarrierVehicle);
		capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities distributionCapabilities = capabilitiesBuilder.build();
		Carrier carrier = CarrierUtils.createCarrier( distributionCarrierId );
		carrier.setCarrierCapabilities(distributionCapabilities);

		Id<LSPResource> distributionAdapterId = Id.create("DistributionCarrierAdapter", LSPResource.class);
		UsecaseUtils.DistributionCarrierAdapterBuilder distributionAdapterBuilder = UsecaseUtils.DistributionCarrierAdapterBuilder
				.newInstance(distributionAdapterId, network);
		distributionAdapterBuilder.setDistributionScheduler(UsecaseUtils.createDefaultDistributionCarrierScheduler());
		distributionAdapterBuilder.setCarrier(carrier);
		distributionAdapterBuilder.setLocationLinkId(distributionLinkId);

		Id<LogisticsSolutionElement> distributionElementId = Id.create("DistributionElement",
				LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder distributionBuilder = LSPUtils.LogisticsSolutionElementBuilder
				.newInstance(distributionElementId);
		distributionBuilder.setResource(distributionAdapterBuilder.build());
		distributionElement = distributionBuilder.build();

		collectionElement.setNextElement(firstReloadElement);
		firstReloadElement.setPreviousElement(collectionElement);
		firstReloadElement.setNextElement(mainRunElement);
		mainRunElement.setPreviousElement(firstReloadElement);
		mainRunElement.setNextElement(secondReloadElement);
		secondReloadElement.setPreviousElement(mainRunElement);
		secondReloadElement.setNextElement(distributionElement);
		distributionElement.setPreviousElement(secondReloadElement);

		Id<LogisticsSolution> solutionId = Id.create("SolutionId", LogisticsSolution.class);
		LSPUtils.LogisticsSolutionBuilder completeSolutionBuilder = LSPUtils.LogisticsSolutionBuilder.newInstance(solutionId );
		completeSolutionBuilder.addSolutionElement(collectionElement);
		completeSolutionBuilder.addSolutionElement(firstReloadElement);
		completeSolutionBuilder.addSolutionElement(mainRunElement);
		completeSolutionBuilder.addSolutionElement(secondReloadElement);
		completeSolutionBuilder.addSolutionElement(distributionElement);
		solution = completeSolutionBuilder.build();

	}

	@Test
	public void testCompleteSolution() {
		assertTrue(solution.getEventHandlers() != null);
		assertTrue(solution.getEventHandlers().isEmpty());
		assertTrue(solution.getInfos() != null);
		assertTrue(solution.getInfos().isEmpty());
		assertTrue(solution.getLSP() == null);
		assertTrue(solution.getShipments() != null);
		assertTrue(solution.getShipments().isEmpty());
		assertTrue(solution.getSolutionElements().size() == 5);
		ArrayList<LogisticsSolutionElement> elements = new ArrayList<LogisticsSolutionElement>(solution.getSolutionElements());
		 	for(LogisticsSolutionElement element : elements) {
				if(elements.indexOf(element) == 0) {
					assertTrue(element.getPreviousElement() == null);
				}
				if(elements.indexOf(element) == (elements.size() -1)) {
					assertTrue(element.getNextElement() == null);
				}
				assertTrue(element.getLogisticsSolution() == solution);
			}	
		assertTrue(collectionElement.getPreviousElement() == null);
		assertTrue(collectionElement.getNextElement() == firstReloadElement);
		assertTrue(firstReloadElement.getPreviousElement() == collectionElement);
		assertTrue(firstReloadElement.getNextElement() == mainRunElement);
		assertTrue(mainRunElement.getPreviousElement() == firstReloadElement);
		assertTrue(mainRunElement.getNextElement() == secondReloadElement);
		assertTrue(secondReloadElement.getPreviousElement() == mainRunElement);
		assertTrue(secondReloadElement.getNextElement() == distributionElement);
		assertTrue(distributionElement.getPreviousElement() == secondReloadElement);
		assertTrue(distributionElement.getNextElement() == null);
	}
		
	
}
