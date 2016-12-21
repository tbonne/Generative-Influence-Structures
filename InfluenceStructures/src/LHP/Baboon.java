package LHP;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import repast.simphony.random.RandomHelper;
import tools.SimUtils;
import tools.MoveUtils;
import jsc.distributions.Lognormal;
import cern.jet.random.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class Baboon extends Primate{

	/****************************
	 * 							*
	 * Building a baboon     	*
	 * 							*
	 * *************************/

	//initialize a primate agent
	public Baboon(int ID, Coordinate c, int groupSize, boolean isMale, int group){
		this.id = ID;
		this.coordinate = c;
		this.myGroup=group;
		if (isMale == true){
			this.sex =1; 
		} else {
			this.sex = 0;
		}
		destination = null;
		RealVector initialFacing = new ArrayRealVector(2,0);
		initialFacing.addToEntry(0,RandomHelper.nextDoubleFromTo(-1, 1));
		initialFacing.addToEntry(1,RandomHelper.nextDoubleFromTo(-1, 1));
		this.setFacing(initialFacing);
		blocked=false;
		foodTarget=null;
		followMate=null;
		feedingCount=0;

		myVector = new ArrayRealVector(2,0.1);

		//setup my action selection network
		inputLayer = new ArrayList<Node>();
		outputLayer = new ArrayList<Node>();
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();

		//set up nodes
		for(int i=0;i<Parameter.inputLayerSize;i++){
			Node newN = new Node(i,null);
			inputLayer.add(newN);
			nodes.add(newN);
		}

		for(int i=0;i<Parameter.outputLayerSize;i++){
			Node newN = new Node(i,ModelSetup.orderedP.get(i));
			outputLayer.add(newN);
			nodes.add(newN);
		}

		//set up edges: input to output layers
		for(Node start:inputLayer){
			for(Node end:outputLayer){
				Edge newE = new Edge(start,end);
				end.getBackEdges().add(newE);
				edges.add(newE);
			}
		}

		//start off with one for all ideal levels
		level_energy = RandomHelper.nextDouble()*0.3; 
		level_safety = 1;
		sensory_energy=1;
		sensory_safety=1;
		//past_energy_level = 1;
	}


	/************************************
	 * 									*
	 * Stimuli (internal + external) 	*
	 * 									*
	 * *********************************/

	public void getInputs(){
		//update where the agent is on the landscapes
		this.coordinate = ModelSetup.getAgentGeometry(this).getCoordinates()[0];

		//update which patch i'm in... if any
		this.myPatch = this.getCurrentFoodPatch();

		//update which patches i can see
		if(this.myPatch!=null){
			this.visualPatches = this.getVisibleFoodPatches(Parameter.visual_range,myPatch.visibleSites);
		} else {
			this.visualPatches = this.getVisibleFoodPatches(Parameter.visual_range);
		}

		//update my social partner of interest (attraction)
		if(followMate==null){
			Collections.shuffle(this.primateList);
			for(Primate pp : this.primateList){
				if(pp.getMyGroup()==this.getMyGroup() && pp.getId()!=this.getId()){
					followMate=pp;
					break;
				}
			}
		} else {
			followMate=chooseSocialPartner();
		}

		//move based on attraction to food/social partners and repulsion from strangers
		attractionRepulsionRand();

	}

	/****************************
	 * 							*
	 * Behavioural response 	*
	 * 							*
	 * *************************/

	public void behaviouralResponse(){
		if(myPatch!=null){
			if(myPatch.getResourceLevel()>Parameter.biteSize){
				myPatch.eatMe(Parameter.biteSize);
				feedingCount++;
			} else {
				move(myVector,true);
			}
		} else {
			move(myVector,true);
		}
	}

	/****************************
	 * 							*
	 * 		Energy updates		*
	 * 							*
	 * *************************/

	public void energyUpdate(){
		//nothing right now: agents are assumed to be always hungry, and will eat when in a patch
	}


	/****************************
	 * 							*
	 * 	   Following model	    *
	 * 							*
	 * *************************/

	private Primate chooseSocialPartner(){

		Primate followingP = null;



		return followingP;
	}

	public void sensoryPrior(){

		//calculate variables of interest (sensory/motor inputs)
		sensory_energy = this.getEnergy();
		sensory_safety = this.getSafety();
		distIdeal = (1-sensory_energy) + (1-sensory_safety);
		//sensory_energy = level_energy;
		//sensory_safety = level_safety;

		//need to get a better flow to impliment the motivation (targeted selection of stimuli depending on distance from homoestasis)... 
		//i've added in the distinction but it is not well done... must be a smarter way...
		//need to monitor the efficacy of learning... how well are the agents doing? maybe measure total or average distance away from homeostasis? Efficent leanrners would meet all their criteria better than non efficent learners?

		//need to convert the filtered stimulus to only 1 0 not weighted...

		//motivation (targeted sensory attention)
		/*if(level_energy <= 0){
			filtered_safety=past_filtered_safety;
			filtered_energy = 1;
		} else if (level_safety <0.2){
			filtered_energy=past_filtered_energy;
			filtered_safety = 1;
		} else {
			filtered_energy=0;
			filtered_safety=0;
			//sensory_energy=past_level_energy;
		}
		 */
		//new filtered sensory inputs
		int [] mot = motivationalFilter(sensory_energy,sensory_safety);
		filtered_energy = mot[0];
		filtered_safety = mot[1];

		//double beta = 1 / ( (1-level_energy) + (1-level_safety) );
		//level_energy = level_energy * beta;
		//level_safety = level_safety * beta;

		//food_visual = FoodUtils.getVisualFoodSite(this,true); //boolean to return value only
		//food_memory = FoodUtils.getMemoryFoodSite(this,true); //boolean to return value only
		//food_water  = FoodUtils.getWaterSite(this,true);      //boolean to return value only

		//add these variables to the input dataset
		int[] input = {
				filtered_energy,
				filtered_safety};/*,
				currentAction[0],
				currentAction[1],
				currentAction[2],
				currentAction[3],
				currentAction[4],
				currentAction[5]};*/

		//input for first layer
		int id = 0;
		for(Node n:inputLayer){
			n.setActive(input[id]);
			id++;
		}

		//record prior sensory levels
		//recordPreActionLevels();
		past_filtered_safety = filtered_safety;
		past_filtered_energy = filtered_energy;

	}

	public void actionSelection(){
		
		Primate followMate = null;
		
		//sum up contributions from each edge
		int count=0;

		for(Node n:inputLayer){
			if(this.getIdNumber()==1)System.out.println("IN"+count+": "+n.isActive());
			count++;
		}

		count=0;
		for(Node n:outputLayer){
			n.sumEdgeWeightsIN();
			if(this.getIdNumber()==1)System.out.println("L"+count+": "+n.getStrength());
			count++;
		}

		double winnerValue = -999999;
		Node winningNode = null;

		//using winner take all activation
		for(Node n:outputLayer){
			if (n.getStrength()>winnerValue){
				winningNode = n;
				winnerValue = n.getStrength();
			}
		}
		if(winningNode == null){
			System.out.println("wining node is null!");
			winningNode = outputLayer.get(RandomHelper.nextIntFromTo(0, outputLayer.size()-1));
		}

		winningNode.setActive(1);
		action = winningNode.getID();
		if(this.getIdNumber()==1)System.out.println("action chosen: L"+(action)+" : "+winningNode.getStrength());

		//record past levels (prior to action)
		past_energy_level = level_energy;
		past_safety_level = level_safety;

		//perform action
		followMate = winningNode.getMyPrimate();
		
	}



	public void sensoryPost(){
		
		//how did the chosen action affect my sensory inputs
		level_energy = estimateEnergyIntake();
		level_safety = getMeanCV(); //circular variation used as measure of safety
		
		//adapt behaviour selection mechanism
		adjustDecisionWeights();
		resetActionNetwork();
	}

	/**********************/

	private int[] motivationalFilter(double energy, double safety){
		//new filtered inputs: motivation
		int[] mot = new int[3];

		if(energy <= Parameter.energyLowThresh){
			mot[1] = 0;
			mot[0] = 1;
			mot[2] = 0;
		} else if (safety <Parameter.safetyLowThresh){
			mot[1] = 1;
			mot[0] = 0;
			mot[2] = 0;
		} else {
			mot[0] = 0;
			mot[1] = 0;
			mot[2] = 1;
		}

		return mot;
	}
	
	private void resetActionNetwork(){
		for(Node n:this.getAllNodes()){
			n.setActive(0);
			n.setStrength(0);
		} i've stopped here... there is work to be done in setting up the timimg of the pre-act-post aspects to the RL algorithm... parameter definitions... and i'll need to be explicit about the noise and saturation choices...
	}
	
	private void adjustDecisionWeights(){

		//**************************agents seem to get stuck on one outcome? but not always
		double success = this.getTotalChangeInIdeals(); //learning rate

		//adjust edges between output layer and input layer
		if(this.getIdNumber()==1){
			System.out.println("adjusting weights (yes/no): "+success);
		}
		
		Node actionNode = outputLayer.get(getCurrentAction());

		//if start and end node are active increase weight, else decrease weight
		if(success==0){
			for(Edge e:actionNode.getBackEdges()){
				if(e.getStartNode().isActive()==1){
					if(this.getIdNumber()==1)System.out.println("down - start: "+e.getWeight());
					e.adjustWeightDOWN();
					if(this.getIdNumber()==1)System.out.println("down - end: "+e.getWeight());
				}
			}
			
			/*for (Edge edge: this.getAllEdges()){
				if(edge.getStartNode().isActive()==1 && edge.getEndNode().isActive()==1){
					if(this.getIdNumber()==1)System.out.println("up - start: "+edge.getWeight());
					edge.adjustWeightUP(change);
					if(this.getIdNumber()==1)System.out.println("up - end: "+edge.getWeight());
				}
			}*/
		}else{
			for(Edge e:actionNode.getBackEdges()){
				if(e.getStartNode().isActive()==1){
					if(this.getIdNumber()==1)System.out.println("up - start: "+e.getWeight());
					e.adjustWeightUP();
					if(this.getIdNumber()==1)System.out.println("up - end: "+e.getWeight());
				}
			}
			
			/*for (Edge edge: this.getAllEdges()){
				if(edge.getStartNode().isActive()==1 && edge.getEndNode().isActive()==1){
					if(this.getIdNumber()==1)System.out.println("Down - start: "+edge.getWeight());
					edge.adjustWeightDOWN(change);
					if(this.getIdNumber()==1)System.out.println("Down - end: "+edge.getWeight());
				}
			}*/
		}
	}
	
	
	/*********************************************************************************************************/
	
	private double estimateEnergyIntake(){
		
		double energy =  (double)feedingCount / (double)Parameter.post_delta_T;
		
		feedingCount=0;
		
		return energy;
		
	}
	
	public double getMeanCV(){

		double cosAngle=0,sinAngle=0;
		int count=0;
		double mR = 0;

		for (Primate groupMate: ModelSetup.orderedP){

			if(groupMate!=null){
				double angle = getAngle(this.getCoord(),groupMate.getCoord());
				cosAngle = cosAngle + Math.cos(angle);
				sinAngle = sinAngle + Math.sin(angle);
				count++;
			}
		}

		if(count>0){
			mR = Math.pow( Math.pow(cosAngle,2) + Math.pow(sinAngle,2) , 0.5) / ((double)count);
		} else{
			System.out.println("something is wrong with the count in mean resultant... no groupmates");
		}

		return mR;
	}
	
	public static float getAngle(Coordinate s1, Coordinate s2) {

		//get coordinates from simple feature
		Coordinate source = s1;

		//angle
		float angle = (float) (Math.atan2(s2.y - source.y, s2.x - source.x));

		if(angle < 0){
			angle += 2*Math.PI;
		}

		return (float) angle;
	}

	/****************************
	 * 							*
	 * 	   Movement model	    *
	 * 							*
	 * *************************/

	private void attractionRepulsionRand(){

		//// calculate adjustment for previous bearing
		myVector.unitize();
		myVector = myVector.mapMultiply(Parameter.bearingWeight);

		//// calculate adjustment for food environment
		//calculate the weights of each patch
		ArrayList<Double> weights = new ArrayList<Double>();
		ArrayList<RealVector> directions = new ArrayList<RealVector>();
		double sum = 0;

		for(Cell c : this.visualPatches){

			//calculate weight
			double distance = Math.max(Parameter.foodBuffer,  c.getCoord().distance(this.getCoord()));
			double weight = (c.getResourceLevel()) / distance;  //no effect of     *  c.getFamiliarity(this.getId() 
			weights.add(weight);
			sum = sum + weight;

			//calculate the direction
			RealVector patchVector = new ArrayRealVector(2);
			double distX = c.getCoord().x-this.getCoord().x;
			double distY = c.getCoord().y-this.getCoord().y;
			if(distX!=0 && distY!=0){
				patchVector.setEntry(0,distX);
				patchVector.setEntry(1,distY);
				patchVector.unitize();
			}
			directions.add(patchVector);
		}

		//standardize the patch weights to sum to one
		for(Double d : weights){
			d=d/sum;
		}

		//calculate the avg direction (weights*dir to each patch)
		RealVector avgFoodVector = new ArrayRealVector(2);
		boolean foodFound = false;
		for(int i = 0 ; i< weights.size();i++){
			avgFoodVector = avgFoodVector.add(directions.get(i).mapMultiply(weights.get(i)));
			foodFound = true;
		}

		//add to myVector
		if(foodFound==true){
			avgFoodVector.unitize();
			avgFoodVector = avgFoodVector.mapMultiply(Parameter.foodWeight);
			myVector = myVector.add(avgFoodVector);
		}

		////calculate adjustment for attraction
		double distToPartner = followMate.getCoord().distance(this.getCoord());
		double magnitudeAtt = Math.max(Parameter.attractionWeight*(1-(Parameter.attractionDistMax/distToPartner)),0);
		double angleToP = MoveUtils.getAngle(this.getCoord(), followMate.getCoord(),true);
		myVector.addToEntry(0, magnitudeAtt*Math.cos(angleToP));
		myVector.addToEntry(1, magnitudeAtt*Math.sin(angleToP));

		////calculate adjustment for repulsion
		double magnitudeRep = 0;
		for(Primate stranger:getStrangers()){
			double distToStr = stranger.getCoord().distance(this.getCoord());
			magnitudeRep = Math.max(0, Parameter.repulsionWeight*((1-(distToStr/Parameter.repulsionDistMax))));
			double angleToS = MoveUtils.getAngle(this.getCoord(), stranger.getCoord(),true);
			myVector.addToEntry(0, -magnitudeRep*Math.cos(angleToS));
			myVector.addToEntry(1, -magnitudeRep*Math.sin(angleToS));
		}

		//// Choose final vector based on uncertainty around alternative influencing factors
		double u = Math.atan2(myVector.getEntry(1), myVector.getEntry(0));
		double length = Math.pow(Math.pow(myVector.getEntry(0),2)+Math.pow(myVector.getEntry(1), 2),0.5);
		double maxLength = Parameter.bearingWeight + Parameter.foodWeight + magnitudeAtt + magnitudeRep;
		double k = Math.max(-2*Math.log(length/maxLength),0.000001);
		if(k<=0){
			System.out.println("zeero u");
			k=0.0;
		}
		if(u==0)System.out.println("zeero u");
		if(u!=0){
			u = u + VonMises.staticNextDouble(1/k); 
			double deltaX = Math.cos(u);
			double deltaY = Math.sin(u); 
			RealVector finalVector = new ArrayRealVector(2);
			finalVector.setEntry(0, deltaX);
			finalVector.setEntry(1, deltaY);
			finalVector.unitize();
			myVector = finalVector;
		}
	}


	/****************************
	 * 							*
	 * 	   Methods				*
	 * 							*
	 * *************************/


	private ArrayList<Primate> getStrangers(){
		ArrayList<Primate> strangers = new ArrayList<Primate>();
		for(Baboon bab : this.primateList){
			if(this.coordinate.distance(bab.coordinate)<Parameter.repulsionDistMax && bab.myGroup!=this.myGroup){
				strangers.add(bab);
			}
		}
		return strangers;
	}

	private ArrayList<Cell> getVisibleFoodPatches(double f){

		Iterable<Cell> objectsInArea = null;
		Envelope envelope = new Envelope();
		envelope.init(this.getCoord());
		envelope.expandBy(f);
		objectsInArea = ModelSetup.getGeog().getObjectsWithin(envelope,Cell.class);
		envelope.setToNull();

		ArrayList<Cell> obj = new ArrayList<Cell>();
		while(objectsInArea.iterator().hasNext()){
			Cell neigh = objectsInArea.iterator().next();
			if(neigh.getCoord().distance(this.getCoord())<f){
				boolean occupied = SimUtils.occupied(neigh.getCoord(),this.id);
				if(occupied == false){
					obj.add(neigh);
				}
			}
		}

		if(obj.size()<2)obj=getVisibleFoodPatches(2*Parameter.visual_range);

		return obj;
	}

	private ArrayList<Cell> getVisibleFoodPatches(double f, ArrayList<Cell> food){

		ArrayList<Cell> obj = new ArrayList<Cell>();
		for(Cell c: food){
			if(c.getCoord().distance(this.getCoord())<f){
				boolean occupied = SimUtils.occupied(c.getCoord(),this.id);
				if(occupied == false){
					obj.add(c);
				}
			}
		}

		if(obj.size()<2)obj=getVisibleFoodPatches(2*Parameter.visual_range, food);

		return obj;
	}

	private Cell getCurrentFoodPatch(){

		Iterable<Cell> objectsInArea = null;
		Envelope envelope = new Envelope();
		envelope.init(this.getCoord());
		envelope.expandBy(Math.max(Parameter.foodBuffer,4));
		objectsInArea = ModelSetup.getGeog().getObjectsWithin(envelope,Cell.class);
		envelope.setToNull();

		Cell myCell = null;
		double minDist = 999999;
		while(objectsInArea.iterator().hasNext()){
			Cell neigh = objectsInArea.iterator().next();
			if(neigh.getCoord().distance(this.getCoord())<=Parameter.foodBuffer){
				double dist = neigh.getCoord().distance(this.getCoord());
				if(dist<minDist){
					myCell = neigh;
					minDist=dist;
				}	
			}

		}

		return myCell;
	}

	private synchronized void move(RealVector direction,boolean isCellSite){
		//this.setFacing(direction);
		MoveUtils.moveTo((Primate)this, direction);
		this.setCoord(ModelSetup.getAgentGeometry(this).getCoordinates()[0]);
	}


}