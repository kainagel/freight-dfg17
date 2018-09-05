/* *********************************************************************** *
 * project: org.matsim.*
 * ReceiversWriterTest.java
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

package receiver.io;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.testcases.MatsimTestUtils;

import receiver.FreightScenario;
import receiver.ReceiversWriter;
import receiver.usecases.base.ReceiverChessboardScenario;

public class ReceiversWriterTest {

	@Rule public MatsimTestUtils utils = new MatsimTestUtils();
	
	@Test
	public void testV1() {
		FreightScenario fs = ReceiverChessboardScenario.createChessboardScenario(1l, 1, false);
		
		/* Now the receiver is 'complete', and we can write it to file. */
		try {
			new ReceiversWriter(fs.getReceivers()).writeV1(utils.getOutputDirectory() + "receivers.xml");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Should write without exception.");
		}
	}

}