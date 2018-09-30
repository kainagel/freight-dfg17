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
package receiver.product;

import org.matsim.api.core.v01.Id;
import org.matsim.utils.objectattributes.attributable.Attributes;

/**
 * This implements the product types of the receiver.
 * 
 * TODO (JWJ, WLB, April 2018: think about how we can/should convert (seemlessly)
 * between volume and weight. Consider the IATA Dimensional Weight Factor. Think
 * about expressing a factor "percentage of cube(meter)-ton". A cubic meter of
 * toilet paper will have a factor of 1.0, and at the same time a brick-size
 * weighing a ton will also have a factor of 1.0. This is necessary as jsprit 
 * can only work with ONE unit throughout its optimisation, i.e. and associate
 * it with vehicle capacity.
 * 
 * @author wlbean
 *
 */
class ProductTypeImpl implements ProductType{
	
	
//	private static ProductType newInstance(Id<ProductType> typeId) {
//		return new ProductTypeImpl(typeId);
//	}
	private Attributes attributes;
	
	/**
	 * Set default values.
	 */
	private String descr = "";
	private double reqCapacity = 1;
	private Id<ProductType> typeId;
	
	ProductTypeImpl( final Id<ProductType> typeId ){
		this.typeId = typeId;
	}

	
	@Override
	public void setDescription(String description){
		this.descr = description;
	}
	
	@Override
	public void setRequiredCapacity(double reqCapacity){
		this.reqCapacity = reqCapacity;
	}
	
	@Override 
	public String getDescription(){
		return descr;
	}
	
	@Override
	public double getRequiredCapacity(){
		return reqCapacity;
	}
	
	@Override
	public Id<ProductType> getId(){
		return typeId;
	}


	@Override
	public Attributes getAttributes() {
		return this.attributes;
	}
	
	
}
