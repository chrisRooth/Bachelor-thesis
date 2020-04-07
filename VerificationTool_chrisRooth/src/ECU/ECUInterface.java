package ECU;

import java.util.ArrayList;

import machines.Machine;

/**
 * ECI interface for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public interface ECUInterface {
	
	public String nodeTemplate = null;
	public ArrayList<Machine> machinePark = null;
	
	public void setNodeTemplate(String nodeTemplate);

	public String getNodeTemplate();

	public void setMachinePark(ArrayList<Machine> machinePark);

	public ArrayList<Machine> getMachinePark();

}
