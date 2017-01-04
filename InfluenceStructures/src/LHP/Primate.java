package LHP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import cern.jet.random.VonMises;
import repast.simphony.space.graph.RepastEdge;

import com.vividsolutions.jts.geom.Coordinate;

public class Primate {

	//primate level variables
	Coordinate coordinate,foodTarget;
	ArrayList<Baboon> primateList;
	ArrayList<Double> primateList_familiarity;
	List<Cell> visualPatches;
	List<Primate> visualPartners;
	Primate followMate;
	Cell myPatch;
	int id;
	boolean blocked;
	private RealVector facing;
	int sex;
	Coordinate destination;
	int myGroup;
	int feedingCount,myCount;
	double safetyCount;
	double level_energy;
	double level_safety;
	VonMises vm;
	RealVector myVector;
	RepastEdge myEdgeOut;


	/****************************
	 * 							*
	 * Behaviours				*
	 * 							*
	 ****************************/

	public void getInputs(){

	}

	public void behaviouralResponse(){

	}

	public void energyUpdate(){

	}


	/****************************
	 * 							*
	 * Get and set parameters	*
	 * 							*
	 ****************************/

	public void setPrimateList(ArrayList<Baboon> allPrimates){
		this.primateList=allPrimates;
	}

	public void setBaboonFollowerRand(Primate primate){
		followMate=null;
		System.out.println("I'm "+primate.id+" trying to find someone");
		while(followMate == null){
			followMate = ModelSetup.getAllPrimateAgents().iterator().next();
			System.out.println("...looking at "+followMate.id);
			if(followMate.id==primate.id)followMate=null;
		}
	}
	public void setBaboonFollower(Integer idn){

		Primate pp = null;
		for(Primate p:ModelSetup.orderedP){
			if(p.getId()==idn)pp=p;
		}
		if(pp==null)System.out.println("Something wrong with the assignment of follower");
		followMate = pp; 
	}
	public Primate getBaboonFollower(){
		return followMate;
	}
	public int getId(){
		return id;
	}
	public void setCoord(Coordinate c){
		coordinate = c;
	}
	public Coordinate getCoord(){
		return coordinate;
	}
	public List<Cell> getVisualPatches(){
		return visualPatches;
	}
	public List<Primate> getVisualPartners(){
		return visualPartners;
	}
	public RealVector getFacing(){
		return facing;
	}
	public double getFacingX(){
		return facing.getEntry(0);
	}
	public double getFacingY(){
		return facing.getEntry(1);
	}
	public void setFacing(RealVector rv){
		//facing = new ArrayRealVector(2,0);
		//facing.addToEntry(0, x);
		//facing.addToEntry(1, y);
		facing = rv;
		facing.unitize();
	}
	public boolean blocked(){
		return blocked;
	}
	public void setBlocked(boolean i){
		blocked = i;
	}
	public void setFoodTarget(Coordinate c){
		foodTarget=c;
	}
	public int getMyGroup(){
		return this.myGroup;
	}
	public double getEnergy(){
		return this.level_energy;
	}
	public double getSafety(){
		return this.level_safety;
	}
	public int getIdNumber(){
		return id;
	}
}
