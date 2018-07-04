/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.run;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.analysis.ScoreStatsControlerListener.ScoreItem;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author ikaddoura
 *
 */
public class RunBerlinScenarioTest {
	
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;
	private final double toleranceForRegression = 0.01;
	
	
	// 10pct, testing the scores in iteration 0
	@Test
	public final void test1() {
		try {
			Config config;

			String configFile = "scenarios/berlin-v5.0-10pct-2018-06-18/input/berlin-5.0_config.xml";
			config = ConfigUtils.loadConfig(configFile);
			config.controler().setLastIteration(0);
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			
			RunBerlinScenario berlin = new RunBerlinScenario( config ) ;
			berlin.run() ;
			
			Assert.assertEquals("Wrong avg. AVG score in iteration 0.", 115.776237215495, berlin.getScoreStats().getScoreHistory().get(ScoreItem.average).get(0), MatsimTestUtils.EPSILON);
			
		} catch ( Exception ee ) {
			Logger.getLogger(this.getClass()).fatal("there was an exception: \n" + ee ) ;			
			Assert.fail("Wasn't able to run the berlin scenario.");
		}
	}
	
	// 1pct, testing the scores in the first and second iteration
	@Test
	public final void test2() {
		try {
			Config config;

			String configFile = "scenarios/berlin-v5.0-1pct-2018-06-18/input/berlin-5.0_config_reduced.xml";
			config = ConfigUtils.loadConfig(configFile);
			config.controler().setLastIteration(1);
			config.strategy().setFractionOfIterationsToDisableInnovation(1.);
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			
			RunBerlinScenario berlin = new RunBerlinScenario( config ) ;
			berlin.run() ;
			
			Assert.assertEquals("Wrong avg. AVG score in iteration 0.", 115.2173655596178, berlin.getScoreStats().getScoreHistory().get(ScoreItem.average).get(0), MatsimTestUtils.EPSILON);
			Assert.assertEquals("Wrong avg. AVG score in iteration 1.", 112.29308182114058, berlin.getScoreStats().getScoreHistory().get(ScoreItem.average).get(1), toleranceForRegression);

		} catch ( Exception ee ) {
			Logger.getLogger(this.getClass()).fatal("there was an exception: \n" + ee ) ;			
			Assert.fail("Wasn't able to run the berlin scenario.");
		}
	}
	
	// 1pct, testing the 100th iteration (version 5.0)
	@Test
	public final void test3a() {
		try {
			Config config;

			String configFile = "scenarios/berlin-v5.0-1pct-2018-06-18/input/berlin-5.0_config_reduced.xml";
			config = ConfigUtils.loadConfig(configFile);
			config.controler().setLastIteration(100);
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			
			RunBerlinScenario berlin = new RunBerlinScenario( config ) ;
			berlin.run() ;

			Assert.assertEquals("Major change in the avg. AVG score in iteration 0.", 115.2173655596178, berlin.getScoreStats().getScoreHistory().get(ScoreItem.average).get(0), MatsimTestUtils.EPSILON);
			Assert.assertEquals("Major change in the avg. AVG score in iteration 100.", 115.39338160261939, berlin.getScoreStats().getScoreHistory().get(ScoreItem.average).get(100), toleranceForRegression);

			Map<String, Double> modeCnt = analyzeModeStats(berlin.getPopulation());
			double sum = 0 ;
			for ( Double val : modeCnt.values() ) {
				sum += val ;
			}
			
			Assert.assertEquals("Major change in the car trip share (iteration 100).", 0.41707279676702186, modeCnt.get("car") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the pt trip share (iteration 100)", 0.1932777710849971, modeCnt.get("pt") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the bicycle trip share (iteration 100)", 0.1403804663286204, modeCnt.get("bicycle") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the walk trip share (iteration 100)", 0.15878500476404298, modeCnt.get("walk") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the freight trip share (iteration 100)", 0.0014730201842096616, modeCnt.get("freight") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the ride trip share (iteration 100)", 0.089010940871108, modeCnt.get("ride") / sum, toleranceForRegression);

		} catch ( Exception ee ) {
			Logger.getLogger(this.getClass()).fatal("there was an exception: \n" + ee ) ;			
			Assert.fail("Wasn't able to run the berlin scenario.");
		}
	}

	// 1pct, testing the 100th iteration (version 5.1)
	@Test
	public final void test3b() {
		try {
			Config config;

			String configFile = "scenarios/berlin-v5.0-1pct-2018-06-18/input/berlin-5.1_config_reduced.xml";
			config = ConfigUtils.loadConfig(configFile);
			config.controler().setLastIteration(100);
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			
			RunBerlinScenario berlin = new RunBerlinScenario( config ) ;
			berlin.run() ;

			Assert.assertEquals("Change in the avg. AVG score in iteration 0.", 115.2173655596178, berlin.getScoreStats().getScoreHistory().get(ScoreItem.average).get(0), MatsimTestUtils.EPSILON);
			Assert.assertEquals("Major change in the avg. AVG score in iteration 100.", 115.39338160261939, berlin.getScoreStats().getScoreHistory().get(ScoreItem.average).get(100), toleranceForRegression);

			Map<String, Double> modeCnt = analyzeModeStats(berlin.getPopulation());
			double sum = 0 ;
			for ( Double val : modeCnt.values() ) {
				sum += val ;
			}
			
			Assert.assertEquals("Major change in the car trip share (iteration 100).", 0.41707279676702186, modeCnt.get("car") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the pt trip share (iteration 100)", 0.1932777710849971, modeCnt.get("pt") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the bicycle trip share (iteration 100)", 0.1403804663286204, modeCnt.get("bicycle") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the walk trip share (iteration 100)", 0.15878500476404298, modeCnt.get("walk") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the freight trip share (iteration 100)", 0.0014730201842096616, modeCnt.get("freight") / sum, toleranceForRegression);
			Assert.assertEquals("Major change in the ride trip share (iteration 100)", 0.089010940871108, modeCnt.get("ride") / sum, toleranceForRegression);
			
		} catch ( Exception ee ) {
			Logger.getLogger(this.getClass()).fatal("there was an exception: \n" + ee ) ;			
			Assert.fail("Wasn't able to run the berlin scenario.");
		}
	}
	
	private Map<String, Double> analyzeModeStats(Population population) {
		
		Map<String,Double> modeCnt = new TreeMap<>() ;

		StageActivityTypesImpl stageActivities = new StageActivityTypesImpl(Arrays.asList("pt interaction", "car interaction", "ride interaction", "bicycle interaction", "freight interaction"));
		MainModeIdentifierImpl mainModeIdentifier = new MainModeIdentifierImpl();
		
		for (Person person : population.getPersons().values()) {
			Plan plan = person.getSelectedPlan() ;

			List<Trip> trips = TripStructureUtils.getTrips(plan, stageActivities) ;
			for ( Trip trip : trips ) {
				String mode = mainModeIdentifier.identifyMainMode( trip.getTripElements() ) ;
				
				Double cnt = modeCnt.get( mode );
				if ( cnt==null ) {
					cnt = 0. ;
				}
				modeCnt.put( mode, cnt + 1 ) ;
			}
		}

		Logger.getLogger(modeCnt.toString()) ;			
		return modeCnt;	
	}
}
