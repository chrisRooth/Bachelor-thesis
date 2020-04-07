package Applications;

import machines.*;
import ECU.*;


import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.IOException;


/**
 * Configurator-tool for the Volvo CE Verification tool. 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-09)
 */
public class Configurator implements VerificationMessageInterface{
	
	private HMIM hmimECU;
	private VOneECU vOneECU;
	private VTwoECU vTwoECU;
	private ArrayList<Machine> ecuList;
	
	private String[] machineModels;
	
	private boolean nodeTemplateFound;
	private boolean downloaderFound;
	private boolean mswFound; 
	private boolean datasetOneFound;
	private boolean datasetTwoFound;
	
	private boolean machinesLocated;
	
	private StringBuilder confVerificationResult;
	private int confFaliureResults;
	
	/**
	 * Function that is called when the configurator file is to be verified. 
	 * 
	 * @param filePathName
	 * @param hmimEcu
	 * @param vOneEcu
	 * @param vTwoEcu
	 * @return
	 */
	public boolean readConfiguratorExcelFile(File configuratorFile, HMIM hmimEcu, VOneECU vOneEcu, VTwoECU vTwoEcu) {
		
		XSSFWorkbook configWb = null;
		variableSetup(hmimEcu, vOneEcu, vTwoEcu);
		
		try {
			
			if(!configuratorFile.getPath().equals("")) {
				
				configWb = new XSSFWorkbook(configuratorFile);
				
				this.addConfVerificationResults(this.confToolHeader());
				
				readThruExcelFile(configWb);
			}
		
			
		} catch (IOException e) {
			addConfVerificationResults("Error with Configurator workbook " + e + "\n");
			this.addVerificationFailureResult(1);
			
		} catch (InvalidFormatException e) {
			addConfVerificationResults("Error with Configurator workbook " + e + "\n");
			this.addVerificationFailureResult(1);
		}
		
		
		return true;
	}

	/**
	 * Function that the workbook created from the excel file 
	 * and starts to iterate thru the sheets, rows and cells. 
	 * 
	 * @param configWb
	 */
	private void readThruExcelFile(XSSFWorkbook configWb) {
		
		Sheet sheet1 = configWb.getSheetAt(0);
		
		for( Iterator<Row> row = sheet1.iterator(); row.hasNext(); ) {
			
			Row nextRow = row.next();
			Iterator<Cell> cell = nextRow.iterator();
			
			while( cell.hasNext() ) {
				
				Cell nextCell = cell.next();
				nextCell.setCellType(Cell.CELL_TYPE_STRING);

				if(this.isNodeTemplateFound()) {
					verifyNodes(nextCell);
				}
				else {
					findNodeTemplate(nextCell);
				}
			}
		}
	}
	
	/**
	 * Function that states what Node that is to be verified.
	 * 
	 * @param cell
	 */
	private void verifyNodes(Cell cell) {
		
		locateNodeCell(cell);
		
		if(this.isDownloaderFound()) {
			readMachineModel(cell, "Down");
		}
		else if(this.isMswFound()) {
			readMachineModel(cell, "MSW");
		}
		else if(this.isDatasetOneFound()) {
			readMachineModel(cell, "DST1");
		}
		else if(this.isDatasetTwoFound()) {
			readMachineModel(cell, "DST2");
		}
		else {
			//Pass	
		}
		
	}
	
	/**
	 * Finds the cells that holds the machine name and extract it as a string
	 * 
	 * @param cell
	 * @param ecuName
	 */
	private void readMachineModel(Cell cell, String ecuName) {
		
		if(cell.getAddress().toString().contains("C")) {
			String machineStr = trimConfMachineField(cell.getStringCellValue());
			String[] machineConcat = concatMachineModels(machineStr);
			this.setMachineModels(machineConcat);
			this.setMachinesLocated(true);
		}
		
		if(this.isMachinesLocated()) {
			findPartNrAndDescNr(cell, ecuName);
		}
		
	}
	
	/**
	 * Function that trim and concat the machine string so 
	 * the machine are formated from MOD-30 to A30. 
	 * 
	 * @param machineStr
	 * @return
	 */
	private String[] concatMachineModels(String machineStr) {
		
		String[] machineConcat = machineStr.split("/");
		String[] temp = new String[machineConcat.length];

		for (int i = 0; i < machineConcat.length; i++) {
			
			String str = machineConcat[i];
			String tempStr = "";
			
			for (int j = 0; j < str.length(); j++) {
				if (Character.isDigit(str.charAt(j))) {
					tempStr = tempStr + str.charAt(j);
				}
			}
			temp[i] = "A" + tempStr;
		}

		return temp;
	}
	
	/**
	 * Function that locate the cell that holds the 
	 * partnr and the description file nr 
	 * 
	 * @param cell
	 */
	private void findPartNrAndDescNr(Cell cell, String ecuName) {
		
		String partNr = "";
		String descNr = "";
		
		if (cell.getAddress().toString().contains("D")) {
			
			partNr = trimPartNr(cell.getStringCellValue());
			descNr = trimDescNr(cell.getStringCellValue());
			
			verifyPartNrAndDEscNr( partNr,  descNr, ecuName);
			
		}
		
	}
	
	/**
	 * Function that gets what node plus the part and desc numbers. 
	 * 
	 * @param partNr
	 * @param descNr
	 * @param ecuName
	 */
	private void verifyPartNrAndDEscNr(String partNr, String descNr, String ecuName) {
		
		if (ecuName.equals("Down")) {
			this.addConfVerificationResults("\n");
			this.addConfVerificationResults("------------------------------------------------------\n");
			this.addConfVerificationResults("*** DOWNLOADER ***\n");
			
			verifyDownloader( partNr,  descNr);
		}
		else if(ecuName.equals("MSW")) {
			this.addConfVerificationResults("\n");
			this.addConfVerificationResults("------------------------------------------------------\n");
			this.addConfVerificationResults("*** MSW ***\n");
			
			verifyMSW( partNr,  descNr);
		}
		else if(ecuName.equals("DST1")) {
			this.addConfVerificationResults("\n");
			this.addConfVerificationResults("------------------------------------------------------\n");
			this.addConfVerificationResults("*** DST1 ***\n");
			
			verifDatasetOne( partNr,  descNr);
		}
		else if(ecuName.equals("DST2")) {
			this.addConfVerificationResults("\n");
			this.addConfVerificationResults("------------------------------------------------------\n");
			this.addConfVerificationResults("*** DST2 ***\n");
			
			verifyDatasetTwo( partNr,  descNr);
		}
		else {
	
		}
		
	}
	
	/**
	 * Function that verifies the part and decs for the DOWNLOADER
	 * 
	 * @param partNr
	 * @param descNr
	 */
	private void verifyDownloader(String partNr, String descNr) {
		
		String[] machineTemp = this.getMachineModels();
		ArrayList<Machine> machineSETool = this.getEcuList(); 
		
		for(int i = 0; i < machineTemp.length; i++) {
			
			for(int j = 0; j < machineSETool.size(); j++) {
				
				if(machineTemp[i].equals(machineSETool.get(j).getMachineName())) {
					this.addConfVerificationResults("------------------------------------------------------\n");
					this.addConfVerificationResults("Machine " + machineSETool.get(j).getMachineName() + " : \n");
					
					verifyPartNr(partNr, machineSETool.get(j).getPartNrDown());
					verifyDescNr(descNr, machineSETool.get(j).getDescFileDown());
					break;
				}
			}
		}
	}
	
	/**
	 * Function that verifies the part and decs for the MSW
	 * 
	 * @param partNr
	 * @param descNr
	 */
	private void verifyMSW(String partNr, String descNr) {
		
		String[] machineTemp = this.getMachineModels();
		ArrayList<Machine> machineSETool = this.getEcuList(); 
		
		for(int i = 0; i < machineTemp.length; i++) {
			
			for(int j = 0; j < machineSETool.size(); j++) {
				
				if(machineTemp[i].equals(machineSETool.get(j).getMachineName())) {
					this.addConfVerificationResults("------------------------------------------------------\n");
					this.addConfVerificationResults("Machine " + machineSETool.get(j).getMachineName() + " : \n");
					
					verifyPartNr(partNr, machineSETool.get(j).getPartNrMSW());
					verifyDescNr(descNr, machineSETool.get(j).getDescFileMSW());
					break;
				}
			}
		}
	}
	
	/**
	 * Function that verifies the part and decs for the DATASET 1 
	 * 
	 * @param partNr
	 * @param descNr
	 */
	private void verifDatasetOne(String partNr, String descNr) {
		
		String[] machineTemp = this.getMachineModels();
		ArrayList<Machine> machineSETool = this.getEcuList(); 
		
		for(int i = 0; i < machineTemp.length; i++) {
			
			for(int j = 0; j < machineSETool.size(); j++) {
				
				if(machineTemp[i].equals(machineSETool.get(j).getMachineName())) {
					this.addConfVerificationResults("------------------------------------------------------\n");
					this.addConfVerificationResults("Machine " + machineSETool.get(j).getMachineName() + " : \n");
					
					verifyPartNr(partNr, machineSETool.get(j).getPartNrDST1());
					verifyDescNr(descNr, machineSETool.get(j).getDescFileDST1());
					break;
				}
			}
		}
	}
	
	/**
	 * Function that verifies the part and decs for the DATASET 2 
	 * 
	 * @param partNr
	 * @param descNr
	 */
	private void verifyDatasetTwo(String partNr, String descNr) {
		
		String[] machineTemp = this.getMachineModels();
		ArrayList<Machine> machineSETool = this.getEcuList(); 
		
		for(int i = 0; i < machineTemp.length; i++) {
			
			for(int j = 0; j < machineSETool.size(); j++) {
				
				if(machineTemp[i].equals(machineSETool.get(j).getMachineName())) {
					this.addConfVerificationResults("------------------------------------------------------\n");
					this.addConfVerificationResults("Machine " + machineSETool.get(j).getMachineName() + " : \n");
					
					verifyPartNr(partNr, machineSETool.get(j).getPartNrDST2());
					verifyDescNr(descNr, machineSETool.get(j).getDescFileDST2());
					break;
				}
			}
		}
	}
	
	/**
	 * Function that takes the conf part nr and the SE-tool and compares them 
	 * 
	 * @param confPartNr
	 * @param SEToolPartNr
	 */
	private void verifyPartNr(String confPartNr, String SEToolPartNr) {
		
		if (confPartNr.equals(SEToolPartNr)) {
			this.addConfVerificationResults("** Verification for part nr was succsesfull **\n");
			this.addConfVerificationResults("PartNR - SE-Tool || " + confPartNr + "  - " + SEToolPartNr + "\n");
		}
		else {
			this.addConfVerificationResults("Verification Failed, Part NR are not equal: \n");
			this.addConfVerificationResults("PartNR - SE-Tool || " + confPartNr + "  - " + SEToolPartNr + "\n");
			this.addVerificationFailureResult(1);
		} 
		
	}
	
	/**
	 * Function that takes the conf part nr and the SE-tool and compares them 
	 * 
	 * @param confDescNr
	 * @param SEToolDescNr
	 */
	private void verifyDescNr(String confDescNr, String SEToolDescNr) {
		
		if(confDescNr.equals(SEToolDescNr)) {
			this.addConfVerificationResults("** Verification for desc nr was succsesfull **\n");
			this.addConfVerificationResults("DescNr - SE-tool || " + confDescNr + "  - " + SEToolDescNr + "\n");
		}
		else {
			this.addConfVerificationResults("Verification Failed, Desc NR are not equal: \n");
			this.addConfVerificationResults("PartNR - SE-Tool || " + confDescNr + "  - " + SEToolDescNr + "\n");
			this.addVerificationFailureResult(1);
		}
		
	}
	
	/**
	 * Function that extract the part nr. 
	 * 
	 * @param cellCont
	 * @return
	 */
	private String trimPartNr(String cellCont) {
		
		String temp = "";
		int index = 0; 
		
		while (cellCont.charAt(index) != '.' && index < (cellCont.length()-1)) {
			
			temp = temp + cellCont.charAt(index);
			index++;
		}
		
		return temp;
	}
	
	/**
	 * Function that extract the description file nr.
	 * 
	 * @param cellCont
	 * @return
	 */
	private String trimDescNr(String cellCont) {
		
		String temp = "";
		int index = 0; 
		
		while (cellCont.charAt(index) != '.' && index < (cellCont.length()-1)) {
			index++;
		}
		
		while(cellCont.charAt(index) != '\n' && index < (cellCont.length()-1)) {
			index++;
		}
		
		index++;
		
		while(cellCont.charAt(index) != '.' && index < (cellCont.length()-1)) {
			temp = temp + cellCont.charAt(index);
			index++;
		}
		
		return temp;
	}
	
	/**
	 * Function that trim the cell that contains the machine names. 
	 * 
	 * @param cellContent
	 * @return
	 */
	private String trimConfMachineField(String cellContent) {

		String temp = "";
		int index = 0;
		
		while (cellContent.charAt(index) != ' ' && index < (cellContent.length()-1)) {
			index++;
		}
		
		index++;
		
		while(cellContent.charAt(index) != ',' && index < (cellContent.length()-1)) {
			temp = temp + cellContent.charAt(index);
			index++;
		}
		
		return temp;
	}

	/**
	 * Function that is searching for the different nodes in the excel ark.
	 * 
	 * @param cell
	 */
	private void locateNodeCell(Cell cell) {
		
		if (cell.getRowIndex() >= 14) {
			
			String cellAdr = retriveNodename(cell);
			
			if(cellAdr.toString().equals("DL")) {
				this.setDownloaderFound(true);
			}
			else if (cellAdr.toString().equals("MSW")) {
				resetNodeVariables();
				this.setMswFound(true);
			}
			else if (cellAdr.toString().equals("DST1")) {
				resetNodeVariables();
				this.setDatasetOneFound(true);
			}
			else if (cellAdr.toString().equals("DST2")) {
				resetNodeVariables();
				this.setDatasetTwoFound(true);
			}
			else {
				//Pass
			}
		}
		else {
			//Pass
		}
	}
	
	/**
	 * Function that reads the nodes names in the cell and format it 
	 * from DL -DL (542) to DL
	 * 
	 * @param cell
	 * @return
	 */
	private String retriveNodename(Cell cell) {
		
		String temp = "";
		String cellAdr = cell.getStringCellValue();
		
		if(!cellAdr.equals("")) {
			
			int index = 0;
			while( cellAdr.charAt(index) != ' ' && index < (cellAdr.length()-1) ) {
				temp = temp + cellAdr.charAt(index);
				index++;
			}
			return temp;
		}
		else {
			return temp;
		}
	}
	
	/**
	 * Function that looks for the cell with the nodetemplate number
	 * 
	 * @param cell
	 */
	private void findNodeTemplate(Cell cell) {
		
		if(cell.getAddress().toString().equals("D2")) {
			
			whatECUToVerify(cell);
			verifyNodetempForMachines(cell);
			this.setNodeTemplateFound(true);
			
			System.out.println("Nodetemp: " +cell.getStringCellValue());
		}
		else {
			//Pass
		}
	}
	
	/**
	 * Function that checks for what ECU the nodetemplate belong to. 
	 * When an ECU is identified the ECU-machinelist is then used 
	 * to verify the part and desc number. 
	 * 
	 * @param cell
	 */
	private void whatECUToVerify(Cell cell) {
		
		if (cell.getStringCellValue().equals(this.getHmimECU().getNodeTemplate())) {
			this.setEcuList(this.getHmimECU().getMachinePark());
			this.addConfVerificationResults("------------------------------------------------------\n");
			this.addConfVerificationResults("NODETEMPLATE " + cell.getStringCellValue() + " || I-ECU (HMIM)\n");
			this.addConfVerificationResults("\n");
			
		}
		else if (cell.getStringCellValue().equals(this.getvOneECU().getNodeTemplate())) {
			this.setEcuList(this.getvOneECU().getMachinePark());
			this.addConfVerificationResults("------------------------------------------------------\n");
			this.addConfVerificationResults("NODETEMPLATE " + cell.getStringCellValue() + " || V1-ECU\n");
			this.addConfVerificationResults("\n");
			
		}
		else if (cell.getStringCellValue().equals(this.getvTwoECU().getNodeTemplate())) {
			this.setEcuList(this.getvTwoECU().getMachinePark());
			this.addConfVerificationResults("------------------------------------------------------\n");
			this.addConfVerificationResults("NODETEMPLATE " + cell.getStringCellValue() + " || V2-ECU\n");
			this.addConfVerificationResults("\n");
			
		}
		else {
			this.addConfVerificationResults("** Nodetemplate is not active for any of the ECU's ** \n");
			this.addVerificationFailureResult(1);
		}
	}
	
	/**
	 * Function the nodetemplate for all the machine in the list. 
	 * 
	 * @param cell
	 */
	private void verifyNodetempForMachines(Cell cell) {

		for(int i = 0; i < (this.getEcuList().size()-1); i++) {
			
			String machineNode = this.getEcuList().get(i).getNodeTemplate();
			
			if (cell.getStringCellValue().equals(machineNode)) {
				this.addConfVerificationResults("** Nodetemplate Verification Successful **\n");
				this.addConfVerificationResults("Machine: " + this.getEcuList().get(i).getMachineName());
				this.addConfVerificationResults(" || Conf - SE-Tool : " +cell.getStringCellValue() + " - " + machineNode + "\n");
				
			}
			else {
				this.addConfVerificationResults("** ERROR ** \n");
				this.addConfVerificationResults("Machine " + this.getEcuList().get(i).getMachineName() );
				this.addConfVerificationResults(" nodetemplate verification FAILED\n");
				this.addConfVerificationResults("Conf - SE-Tool: " + cell.getStringCellValue() + " - " + machineNode + "\n");
				this.addVerificationFailureResult(1);
			}
		}
		
	}
	
	
	/**
	 * Function that resets some variables. 
	 * 
	 */
	private void resetNodeVariables() {
		
		this.setDownloaderFound(false);
		this.setMswFound(false);
		this.setDatasetOneFound(false);
		this.setDatasetTwoFound(false);

	}
	
	
	/**
	 * Function that sets some variables.
	 * 
	 * @param hmimEcu
	 * @param vOneEcu
	 * @param vTwoEcu
	 */
	private void variableSetup(HMIM hmimEcu, VOneECU vOneEcu, VTwoECU vTwoEcu) {
		
		this.setHmimECU(hmimEcu);
		this.setvOneECU(vOneEcu);
		this.setvTwoECU(vTwoEcu);
		
		this.setNodeTemplateFound(false);
		this.setDownloaderFound(false);
		this.setMswFound(false);
		this.setDatasetOneFound(false);
		this.setDatasetTwoFound(false);
		
		this.setMachinesLocated(false);
		
		this.confVerificationResult = new StringBuilder();
		this.setConfFaliureResults(0);
		
	}

	
	private void addConfVerificationResults(String results) {
	
		this.confVerificationResult.append(results);
	}
	
	private void addVerificationFailureResult(int count) {
		int temp = (this.getConfFaliureResults() + count);
		
		this.setConfFaliureResults(temp);
	}
	
	
	public String getVerificationResult() {
		return this.confVerificationResult.toString();
	}
	
	public int getVerificationFailureResult() {
		return this.getConfFaliureResults();
	}
	
	
	private String confToolHeader() {
		
		
		String headerStr =  "\n---------------------------------------------------------- \n" +
						      "|      *******  CONFIGURATOR VERIFICATION  *******       | \n" +
							  "---------------------------------------------------------- \n\n";
		
		return headerStr;
	}
	
//===========================//
// Get, set and constructor  //
//===========================//
	
	public Configurator() {
		
		this.setEcuList(new ArrayList<Machine>());
		
	}
	
	private boolean isNodeTemplateFound() {
		return this.nodeTemplateFound;
	}


	private void setNodeTemplateFound(boolean nodeTemplateFound) {
		this.nodeTemplateFound = nodeTemplateFound;
	}


	private ArrayList<Machine> getEcuList() {
		return ecuList;
	}


	private void setEcuList(ArrayList<Machine> ecuList) {
		this.ecuList = ecuList;
	}


	private HMIM getHmimECU() {
		return hmimECU;
	}


	private void setHmimECU(HMIM hmimECU) {
		this.hmimECU = hmimECU;
	}


	private VOneECU getvOneECU() {
		return vOneECU;
	}


	private void setvOneECU(VOneECU vOneECU) {
		this.vOneECU = vOneECU;
	}


	private VTwoECU getvTwoECU() {
		return vTwoECU;
	}


	private void setvTwoECU(VTwoECU vTwoECU) {
		this.vTwoECU = vTwoECU;
	}


	private boolean isDownloaderFound() {
		return downloaderFound;
	}


	private void setDownloaderFound(boolean downloaderFound) {
		this.downloaderFound = downloaderFound;
	}


	private boolean isMswFound() {
		return mswFound;
	}


	private void setMswFound(boolean mswFound) {
		this.mswFound = mswFound;
	}


	private boolean isDatasetOneFound() {
		return datasetOneFound;
	}


	private void setDatasetOneFound(boolean datasetOneFound) {
		this.datasetOneFound = datasetOneFound;
	}


	private boolean isDatasetTwoFound() {
		return datasetTwoFound;
	}


	private void setDatasetTwoFound(boolean datasetTwoFound) {
		this.datasetTwoFound = datasetTwoFound;
	}


	private boolean isMachinesLocated() {
		return machinesLocated;
	}


	private void setMachinesLocated(boolean machinesLocated) {
		this.machinesLocated = machinesLocated;
	}


	private String[] getMachineModels() {
		return machineModels;
	}


	private void setMachineModels(String[] machineModels) {
		this.machineModels = machineModels;
	}

	private int getConfFaliureResults() {
		return confFaliureResults;
	}

	private void setConfFaliureResults(int confFaliureResults) {
		this.confFaliureResults = confFaliureResults;
	}
	
} //Class End 
