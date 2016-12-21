package LHP;

import java.util.ArrayList;


public class Node {
	
	ArrayList<Edge> backEdges;
	ArrayList<Edge> forwardEdges;
	int active,id;
	double strength;
	boolean win;
	Primate myP;
	
	public Node(int i,Primate p) {
		backEdges = new ArrayList<Edge>();
		forwardEdges = new ArrayList<Edge>();
		active = 0;
		id = i;
		win = false;
		strength = 0;
		myP = p;
	}
	
	public void setActive(int i){
		//used for to determine the output layer winner
		active = i;
	}
	
	public int getID(){
		return id;
	}
	
	public int isActive(){
		return active;
	}
	
	public void sumEdgeWeightsIN(){
		double value = 0;
		for(Edge e: backEdges){
			if(e.getStartNode().isActive()==1)value = value + e.getWeight();
		}
		strength = value;
	}
	
	public void rewardEdges(double change){
		for(Edge e: backEdges){
			e.adjustWeightUP();
		}
	}
	
	public void punishEdges(double change){
		for(Edge e: backEdges){
			e.adjustWeightDOWN();
		}
	}
	
	public ArrayList<Edge> getBackEdges(){
		return backEdges;
	}
	
	public double getStrength(){
		return strength;
	}
	public void setStrength(double i){
		strength = i;
	}
	public Primate getMyPrimate(){
		return myP;
	}

}
