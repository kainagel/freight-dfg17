/* *********************************************************************** *
 * project: org.matsim.*
 * ReceiverUtils.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package receiver.usecases.base;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierCapabilities;
import org.matsim.contrib.freight.carrier.CarrierCapabilities.FleetSize;
import org.matsim.contrib.freight.carrier.CarrierImpl;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.CarrierVehicleType;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeWriter;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.VehicleType;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.Receivers;
import receiver.ReceiversWriter;
import receiver.SSReorderPolicy;
import receiver.collaboration.MutableCoalition;
import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * Various utilities for building receiver scenarios (for now).
 * 
 * @author jwjoubert, wlbean
 */
public class ReceiverChessboardScenario {
	private final static Logger LOG = Logger.getLogger(ReceiverChessboardScenario.class);
	

	/**
	 * Build the entire chessboard example.
	 */
	public static Scenario createChessboardScenario( long seed, int run, boolean write) {
		MatsimRandom.reset(seed);
		int numberOfReceivers = 5;
		Scenario sc = setupChessboardScenario(seed, run);
		createChessboardCarriersAndAddToScenario(sc);
		
		ReceiverUtils.setReplanInterval( 50, sc );
//		ReceiverUtils.setReplanInterval( 5, sc );
		
		/* Create the grand coalition receiver members and allocate orders. */
		createAndAddChessboardReceivers(sc, numberOfReceivers);		

		createReceiverOrders(sc);

		/* Let jsprit do its magic and route the given receiver orders. */
		generateCarrierPlan( sc );
		
		
		if(write) {
			writeFreightScenario(sc);
		}
		
		/* Link the carriers to the receivers. */
		ReceiverUtils.getReceivers( sc ).linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( sc ) );
		
		/* Add carrier and receivers to coalition */
		MutableCoalition coalition = new MutableCoalition();
		
		for (Carrier carrier : ReceiverUtils.getCarriers( sc ).getCarriers().values()){
			if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
				coalition.addCarrierCoalitionMember(carrier);
			}
		}
		
		for (Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
			if ((boolean) receiver.getAttributes().getAttribute("collaborationStatus") == true){
				if (!coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.addReceiverCoalitionMember(receiver);
				}
			} else {
				if (coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.removeReceiverCoalitionMember(receiver);
				}
			}
		}
		
		ReceiverUtils.setCoalition( coalition, sc );
		
		return sc;
	}


	/**
	 * FIXME Need to complete this. 
	 * @return
	 */
	public static Scenario setupChessboardScenario(long seed, int run) {
		URL context = ExamplesUtils.getTestScenarioURL( "freight-chessboard-9x9" );;

		Config config = ConfigUtils.createConfig();

		config.setContext( context );

		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(1500);
//		config.controler().setLastIteration(20);
		config.controler().setMobsim("qsim");
		config.controler().setWriteSnapshotsInterval(50);
//		config.controler().setWriteSnapshotsInterval(5);
		config.global().setRandomSeed(seed);
//		config.network().setInputFile("./scenarios/chessboard/network/grid9x9.xml");
		config.network().setInputFile( "grid9x9.xml" );
		config.controler().setOutputDirectory(String.format("./output/base/tw/run_%03d/", run));

		Scenario sc = ScenarioUtils.loadScenario(config);
		return sc;
	}
	
	public static void writeFreightScenario( Scenario sc ) {
		/* Write the necessary bits to file. */
		String outputFolder = sc.getConfig().controler().getOutputDirectory();
		outputFolder += outputFolder.endsWith("/") ? "" : "/";
		new File(outputFolder).mkdirs();
		
		new ConfigWriter(sc.getConfig()).write(outputFolder + "config.xml");
		new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( sc ) ).write(outputFolder + "carriers.xml");
		new ReceiversWriter( ReceiverUtils.getReceivers( sc ) ).write(outputFolder + "receivers.xml");

		/* Write the vehicle types. FIXME This will have to change so that vehicle
		 * types lie at the Carriers level, and not per Carrier. In this scenario 
		 * there luckily is only a single Carrier. */
		new CarrierVehicleTypeWriter(CarrierVehicleTypes.getVehicleTypes( ReceiverUtils.getCarriers( sc ) )).write(outputFolder + "carrierVehicleTypes.xml");
	}

	/**
	 * Route the services that are allocated to the carrier and writes the initial carrier plans.
	 * 
	 * @param carriers
	 * @param network
	 */
	public static void generateCarrierPlan(Scenario sc ) {
		Carrier carrier = ReceiverUtils.getCarriers(sc).getCarriers().get(Id.create("Carrier1", Carrier.class)); 

		VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, sc.getNetwork());

		NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(sc.getNetwork(), carrier.getCarrierCapabilities().getVehicleTypes()).build();
		VehicleRoutingProblem vrp = vrpBuilder.setRoutingCost(netBasedCosts).build();


		//read and create a pre-configured algorithms to solve the vrp
//		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "./scenarios/chessboard/vrpalgo/initialPlanAlgorithm.xml");
		String algoConfigFileName = IOUtils.newUrl( sc.getConfig().getContext(), "initialPlanAlgorithm.xml" ).getFile();
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, algoConfigFileName );

		//solve the problem
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

		//get best (here, there is only one)
		VehicleRoutingProblemSolution solution = null;

		Iterator<VehicleRoutingProblemSolution> iterator = solutions.iterator();

		while(iterator.hasNext()){
			solution = iterator.next();
		}

		//create a carrierPlan from the solution 
		CarrierPlan plan = MatsimJspritFactory.createPlan(carrier, solution);

		//route plan 
		NetworkRouter.routePlan(plan, netBasedCosts);


		//assign this plan now to the carrier and make it the selected carrier plan
		carrier.setSelectedPlan(plan);

		//write out the carrierPlan to an xml-file
		//		new CarrierPlanXmlWriterV2(carriers).write(directory + "/input/carrierPlanned.xml");
	}

	/**
	 * Creates the product orders for the receiver agents in the simulation. Currently (28/08/18) all the receivers have the same orders 
	 * for experiments, but this must be adapted in the future to accept other parameters as inputs to enable different orders per receiver. 
	 * @param fs
	 */
	public static void createReceiverOrders( Scenario sc ) {
		Carriers carriers = ReceiverUtils.getCarriers( sc );
		Receivers receivers = ReceiverUtils.getReceivers( sc );
		Carrier carrierOne = carriers.getCarriers().get(Id.create("Carrier1", Carrier.class));

		/* Create generic product types with a description and required capacity (in kg per item). */
		ProductType productTypeOne = receivers.createAndAddProductType(Id.create("P1", ProductType.class));
		productTypeOne.setDescription("Product 1");
		productTypeOne.setRequiredCapacity(1);

		ProductType productTypeTwo = receivers.createAndAddProductType(Id.create("P2", ProductType.class));
		productTypeTwo.setDescription("Product 2");
		productTypeTwo.setRequiredCapacity(2);
		
		for ( int r = 1 ; r < ReceiverUtils.getReceivers( sc ).getReceivers().size()+1 ; r++){
			int tw = 6;
			String serdur = "02:00:00";
			int numDel = 5;
			
			/* Set the different time window durations for experiments. */
			if (r <= 10){
				tw = 2;
			} else if (r <= 20){
				tw = 4;
			} else if (r <= 30){
				tw = 6;
			} else if (r <= 40){
				tw = 8;
			} else if (r <= 50){
				tw = 10;
			} else if (r<=60){
				tw = 12;
			}
			
//			/* Set the different service durations for experiments. */
//			if (r <= 15){
//				serdur = "01:00:00";
//			} else if (r <= 30){
//				serdur = "02:00:00";
//			} else if (r <= 45){
//				serdur = "03:00:00";
//			} else serdur = "04:00:00";
//			
//			/* Set the different delivery frequencies for experiments. */
//			if (r <= 12){
//				numDel = 1;
//			} else if (r <= 24){
//				numDel = 2;
//			} else if (r <= 36){
//				numDel = 3;
//			} else if (r <= 48){
//				numDel = 4;
//			} else numDel = 5;

			/* Create receiver-specific products */
			Receiver receiver = receivers.getReceivers().get(Id.create(Integer.toString(r), Receiver.class));
			ReceiverProduct receiverProductOne = createReceiverProduct(receiver, productTypeOne, 1000, 5000);
			ReceiverProduct receiverProductTwo = createReceiverProduct(receiver, productTypeTwo, 500, 2500);
			receiver.getProducts().add(receiverProductOne);
			receiver.getProducts().add(receiverProductTwo);

			/* Generate and collate orders for the different receiver/order combination. */
			Order rOrder1 = createProductOrder(Id.create("Order"+Integer.toString(r)+"1",  Order.class), receiver, 
					receiverProductOne, Time.parseTime(serdur));
			rOrder1.setNumberOfWeeklyDeliveries(numDel);
			Order rOrder2 = createProductOrder(Id.create("Order"+Integer.toString(r)+"2",  Order.class), receiver, 
					receiverProductTwo, Time.parseTime(serdur));
			rOrder2.setNumberOfWeeklyDeliveries(numDel);
			Collection<Order> rOrders = new ArrayList<Order>();
			rOrders.add(rOrder1);
			rOrders.add(rOrder2);

			/* Combine product orders into single receiver order for a specific carrier. */
			ReceiverOrder receiverOrder = new ReceiverOrder(receiver.getId(), rOrders, carrierOne.getId());
			ReceiverPlan receiverPlan = ReceiverPlan.Builder.newInstance(receiver)
					.addReceiverOrder(receiverOrder)
					.addTimeWindow(selectRandomTimeStart(tw))
//					.addTimeWindow(TimeWindow.newInstance(Time.parseTime("12:00:00"), Time.parseTime("12:00:00") + tw*3600))
					.build();
			receiver.setSelectedPlan(receiverPlan);

			/* Convert receiver orders to initial carrier services. */
			for(Order order : receiverOrder.getReceiverProductOrders()){
				org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
						Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());

				if(receiverPlan.getTimeWindows().size() > 1) {
					LOG.warn("Multiple time windows set. Only the first is used");
				}
				
				CarrierService newService = serBuilder
						.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
						setServiceStartTimeWindow(receiverPlan.getTimeWindows().get(0)).
						setServiceDuration(order.getServiceDuration()).
						build();
				carriers.getCarriers().get(receiverOrder.getCarrierId()).getServices().add(newService);	
			}
		}

	}

	/**
	 * Creates and adds the receivers that are part of the grand coalition. These receivers are allowed to replan
	 * their orders as well as decided to join or leave the coalition.
	 * @param fs
	 * @param numberOfReceivers
	 */
	public static void createAndAddChessboardReceivers( Scenario sc , int numberOfReceivers) {
		Network network = sc.getNetwork();

		Receivers receivers = new Receivers();
		
		receivers.setDescription("Chessboard");
		
		for (int r = 1; r < numberOfReceivers+1 ; r++){
			Id<Link> receiverLocation = selectRandomLink(network);
			Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(r), Receiver.class))
					.setLinkId(receiverLocation);
			receiver.getAttributes().putAttribute("grandCoalitionMember", true);
			receiver.getAttributes().putAttribute("collaborationStatus", true);			
			receivers.addReceiver(receiver);
		}
		
		ReceiverUtils.setReceivers( receivers, sc );
	}


	/**
	 * Creates the carrier agents for the simulation.
	 * @param sc
	 * @return
	 */
	public static void createChessboardCarriersAndAddToScenario(Scenario sc) {
		Id<Carrier> carrierId = Id.create("Carrier1", Carrier.class);
		Carrier carrier = CarrierImpl.newInstance(carrierId);
		Id<Link> carrierLocation = selectRandomLink(sc.getNetwork());

		org.matsim.contrib.freight.carrier.CarrierCapabilities.Builder capBuilder = CarrierCapabilities.Builder.newInstance();
		CarrierCapabilities carrierCap = capBuilder.setFleetSize(FleetSize.INFINITE).build();
		carrier.setCarrierCapabilities(carrierCap);						
		LOG.info("Created a carrier with capabilities.");	

		/*
		 * Create the carrier vehicle types. 
		 * TODO This might, potentially, be read from XML file. 
		 */

		/* Heavy vehicle. */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderHeavy = CarrierVehicleType.Builder.newInstance(Id.create("heavy", VehicleType.class));
		CarrierVehicleType typeHeavy = typeBuilderHeavy
				.setCapacity(14000)
				.setFixCost(2604)
				.setCostPerDistanceUnit(7.34E-3)
				.setCostPerTimeUnit(0.171)
				.build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierHVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("heavy"), carrierLocation);
		CarrierVehicle heavy = carrierHVehicleBuilder
				.setEarliestStart(Time.parseTime("06:00:00"))
				.setLatestEnd(Time.parseTime("18:00:00"))
				.setType(typeHeavy)
				.setTypeId(typeHeavy.getId())
				.build();

		/* Light vehicle. */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderLight = CarrierVehicleType.Builder.newInstance(Id.create("light", VehicleType.class));
		CarrierVehicleType typeLight = typeBuilderLight
				.setCapacity(3000)
				.setFixCost(1168)
				.setCostPerDistanceUnit(4.22E-3)
				.setCostPerTimeUnit(0.089)
				.build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierLVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("light"), carrierLocation);
		CarrierVehicle light = carrierLVehicleBuilder
				.setEarliestStart(Time.parseTime("06:00:00"))
				.setLatestEnd(Time.parseTime("18:00:00"))
				.setType(typeLight)
				.setTypeId(typeLight.getId())
				.build();

		/* Assign vehicles to carrier. */
		carrier.getCarrierCapabilities().getCarrierVehicles().add(heavy);
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeHeavy);
		carrier.getCarrierCapabilities().getCarrierVehicles().add(light);	
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeLight);
		LOG.info("Added different vehicle types to the carrier.");

		CarrierVehicleTypes types = new CarrierVehicleTypes();
		types.getVehicleTypes().put(typeLight.getId(), typeLight);
		types.getVehicleTypes().put(typeHeavy.getId(), typeHeavy);

		Carriers carriers = new Carriers();
		carriers.addCarrier(carrier);
		ReceiverUtils.setCarriers(carriers, sc);
	}


	/**
	 * Selects a random link in the network.
	 * @param network
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Id<Link> selectRandomLink(Network network){
		Object[] linkIds = network.getLinks().keySet().toArray();
		int sample = MatsimRandom.getRandom().nextInt(linkIds.length);
		Object o = linkIds[sample];
		Id<Link> linkId = null;
		if(o instanceof Id<?>){
			linkId = (Id<Link>) o;
			return linkId;
		} else{
			throw new RuntimeException("Oops, cannot find a correct link Id.");
		}
	}


	/**
	 * This method assigns a specific product type to a receiver, and allocates that receiver's order
	 * policy.
	 * 
	 * TODO This must be made more generic so that multiple policies can be considered. Currently (2018/04
	 * this is hard-coded to be a min-max (s,S) reordering policy.
	 *  
	 * @param receiver
	 * @param productType
	 * @param minLevel
	 * @param maxLevel
	 * @return 
	 */
	private static ReceiverProduct createReceiverProduct(Receiver receiver, ProductType productType, int minLevel, int maxLevel) {
		ReceiverProduct.Builder builder = ReceiverProduct.Builder.newInstance();
		ReceiverProduct rProd = builder
				.setReorderingPolicy(new SSReorderPolicy(minLevel, maxLevel))
				.setProductType(productType)
				.build();
		return rProd;
	}

	/**
	 * Create a receiver order for different products.
	 * @param number
	 * @param receiver
	 * @param receiverProduct
	 * @param serviceTime
	 * @return
	 */
	private static Order createProductOrder(Id<Order> number, Receiver receiver, ReceiverProduct receiverProduct, double serviceTime) {
		Order.Builder builder = Order.Builder.newInstance(number, receiver, receiverProduct);
		Order order = builder
				.calculateOrderQuantity()
				.setServiceTime(serviceTime)
				.build();

		return order;
	}

	private static TimeWindow selectRandomTimeStart(int tw) {
		int min = 06;
		int max = 18;
		Random randomTime = new Random();
		int randomStart =  (min +
				randomTime.nextInt(max - tw - min + 1));
		final TimeWindow randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + tw*3600);
		return randomTimeWindow;
	}

}
