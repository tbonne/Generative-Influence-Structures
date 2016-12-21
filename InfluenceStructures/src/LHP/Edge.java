package LHP;

import repast.simphony.random.RandomHelper;

public class Edge {
	
	double weight;
	Node start, end;
	
	public Edge(Node s, Node e){
		start = s;
		end = e;
		weight = RandomHelper.nextDouble();
	}
	public void adjustWeightUP(){ //change should be within [0,1] learning parameter 
		//weight = weight + (Math.abs(change)*(2-weight));
		weight = weight + Parameter.learningRate * (1 + Math.exp(-weight));
		
		//if(weight<0)weight=0;
	}
	public void adjustWeightDOWN(){
		//weight = weight - (Math.abs(change)*(2-weight));
		weight = weight -Parameter.learningRate * (1 + Math.exp(weight));
		//if(weight<0)weight=0;
	}
	public Node getStartNode(){
		return start;
	}
	public double getWeight(){
		return weight;
	}
	public Node getEndNode(){
		return end;
	}
}