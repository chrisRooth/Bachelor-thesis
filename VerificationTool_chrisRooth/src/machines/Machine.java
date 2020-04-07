package machines; 


/**
 * Machine class for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class Machine {

	
	private String nodeTemplate, machineName, partNrMSW, descFileMSW, partNrHW, descFileHW,
					partNrDST1, descFileDST1, partNrDST2, descFileDST2, partNrDown, descFileDown;

	public Machine(){ }


	public Machine(String nodeTemplate, String machineName, String partNrMSW, String descFileMSW,
						String partNrHW, String descFileHW, String partNrDST1, String descFileDST1,
							String partNrDST2, String descFileDST2, String partNrDown, String descFileDown){

		this.setNodeTemplate(nodeTemplate); 
		this.setMachineName(machineName); 
		this.setPartNrMSW(partNrMSW); 
		this.setDescFileMSW(descFileMSW); 
		this.setPartNrHW(partNrHW); 
		this.setDescFileHW(descFileHW); 	
		this.setPartNrDST1(partNrDST1); 
		this.setDescFileDST1(descFileDST1); 	
		this.setPartNrDST2(partNrDST2); 
		this.setDescFileDST2(descFileDST2); 	
		this.setPartNrDown(partNrDown); 
		this.setDescFileDown(descFileDown); 	
		
	}


	public String getNodeTemplate() {
		return nodeTemplate;
	}


	public void setNodeTemplate(String nodeTemplate) {
		this.nodeTemplate = nodeTemplate;
	}


	public String getMachineName() {
		return machineName;
	}


	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}


	public String getPartNrMSW() {
		return partNrMSW;
	}


	public void setPartNrMSW(String partNrMSW) {
		this.partNrMSW = partNrMSW;
	}


	public String getDescFileMSW() {
		return descFileMSW;
	}


	public void setDescFileMSW(String descFileMSW) {
		this.descFileMSW = descFileMSW;
	}


	public String getPartNrHW() {
		return partNrHW;
	}


	public void setPartNrHW(String partNrHW) {
		this.partNrHW = partNrHW;
	}


	public String getDescFileHW() {
		return descFileHW;
	}


	public void setDescFileHW(String descFileHW) {
		this.descFileHW = descFileHW;
	}


	public String getPartNrDST1() {
		return partNrDST1;
	}


	public void setPartNrDST1(String partNrDST1) {
		this.partNrDST1 = partNrDST1;
	}


	public String getDescFileDST1() {
		return descFileDST1;
	}


	public void setDescFileDST1(String descFileDST1) {
		this.descFileDST1 = descFileDST1;
	}


	public String getPartNrDST2() {
		return partNrDST2;
	}


	public void setPartNrDST2(String partNrDST2) {
		this.partNrDST2 = partNrDST2;
	}


	public String getDescFileDST2() {
		return descFileDST2;
	}


	public void setDescFileDST2(String descFileDST2) {
		this.descFileDST2 = descFileDST2;
	}


	public String getPartNrDown() {
		return partNrDown;
	}


	public void setPartNrDown(String partNrDown) {
		this.partNrDown = partNrDown;
	}


	public String getDescFileDown() {
		return descFileDown;
	}


	public void setDescFileDown(String descFileDown) {
		this.descFileDown = descFileDown;
	}


} //End bracket