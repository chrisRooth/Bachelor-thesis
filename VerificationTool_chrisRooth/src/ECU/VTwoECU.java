package ECU;


import java.util.ArrayList;

import machines.Machine;


/**
 * ECU class for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class VTwoECU implements ECUInterface{


	private String nodeTemplate;
	private ArrayList<Machine> machinePark;

	public VTwoECU (){
		
		this.nodeTemplate = "";
		this.machinePark = new ArrayList<Machine>();

	}

	public VTwoECU (String nodeTemplate, ArrayList<Machine> machinePark) {

		this.nodeTemplate = nodeTemplate;
		this.machinePark = machinePark;

	}

	
	public void setNodeTemplate(String nodeTemplate) {
		this.nodeTemplate = nodeTemplate; 
	}

	public String getNodeTemplate() {
		return this.nodeTemplate;
	}

	public void setMachinePark(ArrayList<Machine> machinePark){
		this.machinePark = machinePark; 
	}

	public ArrayList<Machine> getMachinePark() {
		return this.machinePark;
	}



}//CLass Bracket