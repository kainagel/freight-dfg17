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
package receiver.marginalParallel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlReaderV2;
import org.matsim.contrib.freight.carrier.Carriers;

import com.google.common.io.Files;

import receiver.Receiver;
import receiver.usecases.ReceiverChessboardUtils;

/**
 * Executes a MATSim run for a given (freight) scenario, where the scenario
 * is cognisant of the receiver (being excluded) but calculates the marginal
 * contribution FOR THAT receiver.
 * 
 * @author jwjoubert
 */
public class CalculateMarginalCallable implements Callable<Double> {
	final private Logger log = Logger.getLogger(CalculateMarginalCallable.class);
	final private Id<Receiver> receiverId;
	final private String sourceFolder;
	final private String outputFolder;
	final private String release;
	final private long seed;
	final private double grandCoaltionCost;
	
	public CalculateMarginalCallable(long seed, String sourceFolder, String outputFolder, String release, Id<Receiver> receiverId, double grandCoalitionCost) {
		this.receiverId = receiverId;
		this.sourceFolder = sourceFolder;
		this.outputFolder = outputFolder;
		this.release = release;
		this.seed = seed;
		this.grandCoaltionCost = grandCoalitionCost;
	}

	@Override
	public Double call() throws Exception {

		/* Set up the scenario. This is made up of a couple of steps. */
		String foldername = String.format("%sreceiver_%03d/", outputFolder, Integer.parseInt(receiverId.toString()));
		File folder = new File(foldername);
		boolean createFolder = folder.mkdirs();
		if(!createFolder) {
			throw new RuntimeException("Could not create receiver pipe " + foldername);
		}
		new File(foldername + "input/").mkdirs();
		new File(foldername + "output/").mkdirs();
		ReceiverChessboardUtils.copyFile(
				new File(sourceFolder + "usecases/chessboard/network/grid9x9.xml"),
				new File(foldername + "/input/network.xml"));
		ReceiverChessboardUtils.copyFile(
				new File(sourceFolder + "usecases/chessboard/vrpalgo/initialPlanAlgorithm.xml"),
				new File(foldername + "input/algorithm.xml"));
		ReceiverChessboardUtils.copyFile(
				new File(release),
				new File(foldername + "release.zip"));
		
		/* Unzip the release. */
		ProcessBuilder zipBuilder = new ProcessBuilder(
				"unzip", 
				String.format("%s/release.zip", foldername), 
				"-d", 
				String.format("%s", foldername));
		final Process zipProcess = zipBuilder.start();
		int zipExitCode = zipProcess.waitFor();
		if(zipExitCode != 0) {
			log.error("Could not unzip release for receiver '" + receiverId.toString() + "'. Exit status '" + zipExitCode + "'");
		}

		/* Execute the scenario, i.e. the MATSim run. */
		ProcessBuilder runBuilder = new ProcessBuilder(
				"java",
				"-Xmx512m",
				"-cp",
				"freight-dfg17-0.0.1-SNAPSHOT/freight-dfg17-0.0.1-SNAPSHOT.jar",
				"receiver.marginalParallel.MarginalReceiverClass",
				String.valueOf(seed),
				foldername,
				receiverId.toString(),
				"> log.log",
				"2>&1"
				);
		runBuilder.directory(folder);
		runBuilder.redirectErrorStream(true);
		final Process equilProcess = runBuilder.start();
		log.info("Process started for receiver '" + receiverId.toString() + "'...");
		BufferedReader br = new BufferedReader(new InputStreamReader(equilProcess.getInputStream()));
		String line;
		while((line = br.readLine()) != null) {
			/* Do nothing. */
		}
		int equilExitCode = equilProcess.waitFor();
		log.info("Process ended for receiver '" + receiverId.toString() + ". Exit status '" + equilExitCode + "'");
		if(equilExitCode != 0) {
			log.error("Could not complete run for receiver '" + receiverId.toString() + "'");
		}
		
		/* Calculate the marginal contribution. */
		Carriers outputCarriers = new Carriers();
		new CarrierPlanXmlReaderV2(outputCarriers).readFile(foldername + "output/output_carrierPlans.xml.gz");
		
		double coalitionCost = outputCarriers.getCarriers().get(Id.create("Carrier1", Carrier.class)).getSelectedPlan().getScore();
		double marginal = 0.0;
		
		if(receiverId.equals(Id.create("0", Receiver.class))) {
			marginal = coalitionCost;
		} else {
			marginal = grandCoaltionCost - coalitionCost;
		}
		
		/* Clean up */
		ReceiverChessboardUtils.delete(new File(foldername));
		
		return marginal;
	}

}
