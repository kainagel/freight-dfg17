/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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
package receiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.utils.objectattributes.attributable.Attributes;

import receiver.product.ProductType;
import receiver.product.ReceiverProduct;

/**
 * This returns a receiver that has characteristics and orders.
 * 
 * @author wlbean, jwjoubert
 *
 */
public class ReceiverImpl implements Receiver {
	final private Logger log = Logger.getLogger(Receiver.class);
	
	private Attributes attributes = new Attributes();
	private Id<Link> location;
	private List<TimeWindow> timeWindows = new ArrayList<>();
	
	
	
	/*
	 * Create a new instance of a receiver.
	 */
	public static ReceiverImpl newInstance(Id<Receiver> id){
		return new ReceiverImpl(id);
	}
	
	private final Id<Receiver> id;
	private final List<ReceiverPlan> plans;
	private final List<ReceiverProduct> products;
	private ReceiverPlan selectedPlan;
	
	private ReceiverImpl(final Id<Receiver> id){
		super();
		this.id = id;
		this.plans = new ArrayList<ReceiverPlan>();
		this.products = new ArrayList<ReceiverProduct>();
	}
	

	@Override
	public boolean addPlan(ReceiverPlan plan) {
		if(!plans.contains(plan)) {
			plans.add(plan);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ReceiverPlan createCopyOfSelectedPlanAndMakeSelected() {
		ReceiverPlan plan = selectedPlan.createCopy();
		this.setSelectedPlan(plan);
		return plan;
	}
	

	/*
	 * Removes an order from the receiver's list of orders.
	 */

	@Override
	public boolean removePlan(ReceiverPlan plan) {
		return this.plans.remove(plan);
	}

	/*
	 * Returns the receiver id.
	 */
	
	@Override
	public Id<Receiver> getId() {
		return id;
	}

	/*
	 * Returns a list of the receiver's orders.
	 */
	
	@Override
	public List<ReceiverPlan> getPlans() {
		return plans;
	}

	/*
	 * Returns a list of the receiver's products.
	 */
	
	@Override
	public List<ReceiverProduct> getProducts() {
		return products;
	}

	@Override
	public ReceiverPlan getSelectedPlan() {
		return this.selectedPlan;
	}
	
	
	/**
	 * Sets the selected receiver plan.
	 * @param selectedOrder
	 */

	@Override
	public void setSelectedPlan(ReceiverPlan selectedPlan) {
		/* Unselected all other plans. */
		for(ReceiverPlan plan : this.plans) {
			plan.setSelected(false);
		}

		selectedPlan.setSelected(true);
		if(!plans.contains(selectedPlan)) plans.add(selectedPlan);
		this.selectedPlan = selectedPlan;	
	}

	/**
	 * Sets the receiver's characteristics.
	 * @param receiverCharacteristics
	 */
	

	@Override
	public Attributes getAttributes() {
		return this.attributes;
	}

	
	public Receiver setLocation(Id<Link> linkId) {
		this.location = linkId;
		return this;
	}
	
	/**
	 * Returns the link from which the receiver is accessed.
	 * TODO One may consider changing this so that it is a list of links.
	 */
	@Override
	public Id<Link> getLinkId() {
		return this.location;
	}

	
	@Override
	public Receiver addTimeWindow(TimeWindow window) {
		/*TODO May want to check/consolidate time windows. */
		this.timeWindows.add(window);
		return this;
	}
	
	
	/**
	 * Checks if a given time is within the allowable time window(s).
	 * 
	 * @return true if the time is within at leats one of the set time 
	 * window(s), or <i>if no time windows are set</i>.
	 */
	@Override
	public boolean isInTimeWindow(double time) {
		if(this.timeWindows.isEmpty()) {
			log.warn("No time windows are set! Assuming any time is suitable.");
			return true;
		}
		
		boolean inTimeWindow = false;
		Iterator<TimeWindow> iterator = this.timeWindows.iterator();
		
		while(!inTimeWindow & iterator.hasNext()) {
			TimeWindow tw = iterator.next();
			if(time >= tw.getStart() && time <= tw.getEnd()) {
				inTimeWindow = true;
			}
		}
		return false;
	}
	
	
	public List<TimeWindow> getTimeWindows(){
		return this.timeWindows;
	}

	
	@Override
	public Receiver setLinkId(Id<Link> linkId) {
		this.location = linkId;
		return this;
	}

	@Override
	public ReceiverProduct getProduct(Id<ProductType> productType) {
		ReceiverProduct product = null;
		Iterator<ReceiverProduct> iterator = this.products.iterator();
		while(product == null & iterator.hasNext()) {
			ReceiverProduct thisProduct = iterator.next();
			if(thisProduct.getProductTypeId().equals(productType)) {
				product = thisProduct;
			}
		}
		if(product == null) {
			log.warn("Receiver \"" + this.id.toString() 
			+ "\" does not have the requested product type \"" + productType.toString() + "\". Returning null.");
		}
		return product;
	}

}
