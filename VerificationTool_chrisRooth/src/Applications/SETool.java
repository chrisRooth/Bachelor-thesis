package Applications;

import machines.*;
import ECU.*;


import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.IOException;


/**
 * SE-tool reader for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class SETool implements VerificationMessageInterface{
	
	private HMIM hmimECU;
	private VOneECU vOneECU;
	private VTwoECU vTwoECU;

	private String HMIMNodeTemp;
	private String VOneNodeTemp;
	private String VTwoNodeTemp;
	
	private int counter;
	
	private String ESWDelivery;
	
	private boolean ECU_NTP; 
	private boolean ECU_MSW; 
	private boolean ECU_HW;
	private boolean ECU_DST1;
	private boolean ECU_DST2;
	private boolean ECU_Down;
	
	private boolean readDescFileNr;
	
	private StringBuilder seVerificationResult;
	private int seToolFaliureCount;
	
	private String readExcelCol;
	
	/**
	 * Function that is called from the main verification tool when the SE-tool
	 * excel file is to be imported
	 * 
	 * @param filePathName
	 * @param hmimCatalog
	 * @param vOneEcu
	 * @param vTwoEcu
	 * @return boolean
	 */
	public boolean readSEToolExcelFile(File setoolFile, HMIM hmim, VOneECU vOne, VTwoECU vTwo) {
		
		XSSFWorkbook SEToolWb = null;
		variableSetup(hmim, vOne, vTwo);
		
		try {
			
			if(!setoolFile.getPath().equals("")) {
				
				SEToolWb = new XSSFWorkbook(setoolFile);
				locateECUInExcelFile(SEToolWb);
			}
			
			
		} catch (InvalidFormatException e) {
			this.addSEVerificationResults("Error with SETool workbook " + e + "\n");
			this.addVerificationFailureResult(1);
			
		} catch (IOException e) {
			this.addSEVerificationResults("Error with SETool workbook " + e +"\n");
			this.addVerificationFailureResult(1);
		}
		
		return true;
	}
	
	/**
	 * Function that takes the excel-workbook and begins the first sort. 
	 * This sort locates where in the excel-doc the HMIM, VOne and VTwo 
	 * fields are. 
	 * 
	 * @param SEToolWb
	 */
	private void locateECUInExcelFile(XSSFWorkbook SEToolWb) {
		
		Sheet sheet1 = SEToolWb.getSheetAt(0);
		
		//Print out
		this.addSEVerificationResults(this.seToolHeader());
		
		boolean hmimList = false; 
		boolean vOneList = false; 
		boolean vTwoList = false; 
		
		for(Iterator<Row> row = sheet1.iterator(); row.hasNext();) {
			
			Row nextRow = row.next();
			Iterator<Cell> cell = nextRow.iterator();
			
			while( cell.hasNext() ) {
				
				Cell nextCell = cell.next();
				nextCell.setCellType(Cell.CELL_TYPE_STRING);
			
				if(nextCell.getAddress().toString().equals("C3")) {
					
					
					this.setESWDelivery(nextCell.getStringCellValue());
					
					
				}
				else if (nextCell.getStringCellValue().equals("I-ECU (HMIM)")) {
					hmimList = true;
				}
				
				else if(nextCell.getStringCellValue().equals("V-ECU")) {
					hmimList = false;
					vOneList = true;
				}
				else if(nextCell.getStringCellValue().equals("V2-ECU")) {
					vOneList = false;
					vTwoList = true;
				}
				else {
					//Default value
				}
				
				whatListToRead(nextCell, hmimList, vOneList, vTwoList);
				
			}
			
		}
		
			this.printMachinesAndVerifiResults();
	}
	
	
	/**
	 * Function that runs boolean checks to see what ECU list to read to.
	 * 
	 * @param cell
	 * @param hmimList
	 * @param vOneList
	 * @param vTwoList
	 */
	private void whatListToRead(Cell cell, boolean hmimList, boolean vOneList, boolean vTwoList) {
		
		
		if(hmimList) {
			readToList(cell, this.getHmimECU().getMachinePark());
		}
		else if (vOneList) {
			readToList(cell, this.getvOneECU().getMachinePark());
		}
		else if (vTwoList) {
			readToList(cell, this.getvTwoECU().getMachinePark());
		}
		else {
			//Default state
		}
		
	}
	
	
	/**
	 * Function that conducts the second sort. Here the program is pointed to 
	 * what ecu that is to be read. 
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void readToList(Cell cell, ArrayList<Machine> machineList) {
	
		setWhatECUToReadFrom(cell);
		
		if(this.isECU_NTP()) {
			
			setNodeTemplateToMachine(cell, machineList);
			
		}
		else if(this.isECU_MSW()) {
			
			setPartNrAndDescNrToMachine(cell, machineList, "msw");
			
		}
		else if(this.isECU_HW()) {

			setPartNrAndDescNrToMachine(cell, machineList, "hw");
			
		}
		else if(this.isECU_DST1()) {
			
			setPartNrAndDescNrToMachine(cell, machineList, "dst1");
			
		}
		else if(this.isECU_DST2()) {
			
			setPartNrAndDescNrToMachine(cell, machineList, "dst2");
			
		}
		else if(this.isECU_Down()) {
			
			setPartNrAndDescNrToMachine(cell, machineList, "down");
			
		}
		else {
			//Default state 
		}
			
	}	
	
	/**
	 * Takes the cell value and checks what ecu 
	 * that is being read from.
	 * 
	 * @param cell
	 */
	private void setWhatECUToReadFrom(Cell cell) {
	
		if(cell.getStringCellValue().contains("Node Template NTP")) {
			this.setECU_NTP(true); 
			this.setCounter(0);
		}
		else if (cell.getStringCellValue().contains("MSW")) {
			this.setECU_NTP(false);
			this.setECU_MSW(true);
			this.setCounter(0);
		}
		else if (cell.getStringCellValue().contains("HW")) {
			this.setECU_MSW(false);
			this.setECU_HW(true);
			this.setCounter(0);
		}
		else if (cell.getStringCellValue().contains("DST1")) {
			this.setECU_HW(false);
			this.setECU_DST1(true);
			this.setCounter(0);
		}
		else if (cell.getStringCellValue().contains("DST2")) {
			this.setECU_DST1(false);
			this.setECU_DST2(true);
			this.setCounter(0);
		}
		else if (cell.getStringCellValue().contains("Downloader")) {
			this.setECU_DST2(false);
			this.setECU_Down(true);
			this.setCounter(0);
		}
		else {
			//Default state
		}
	}
	
	/**
	 * Function that sets the node template for the machines. 
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void setNodeTemplateToMachine(Cell cell, ArrayList<Machine> machineList) {
		
		if(counter != 0) {
			
			if(counter == 1) {
				int index = counter - 1;
				machineList.get(index).setNodeTemplate(cell.getStringCellValue());
				counter++;
			}
			else {
				int index = counter - 1;
				String nodeStr = machineList.get(0).getNodeTemplate();
				machineList.get(index).setNodeTemplate(nodeStr);
				counter++;
			}
		
		}
		
		else {
			counter++;
		}
	
	}

	
	/**
	 * Function that takes the cell and list and then directs the program to the 
	 * correct ecu. 
	 * 
	 * @param cell
	 */
	private void setPartNrAndDescNrToMachine(Cell cell, ArrayList<Machine> machineList, String ecu) {
		
		if(counter != 0) {
			
			if(ecu.equals("msw")) {
				setMSWArtNr(cell, machineList);
			}
			else if(ecu.equals("hw")) {
				setHWArtNr(cell, machineList);
			}
			else if(ecu.equals("dst1")) {
				setDSTOneArtNr(cell, machineList);
			}
			else if(ecu.equals("dst2")) {
				setDSTTwoArtNr(cell, machineList);
			}
			else if(ecu.equals("down")) {
				setDownArtNr(cell, machineList);
			}
			else {
				//Default state
			}
			
		}
		else {
			counter++;
		}
		
	}
	
	/**
	 * Function that takes the cell and adds the artnr for the MSW to the machines.
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void setMSWArtNr(Cell cell, ArrayList<Machine> machineList) {
		
		int index = counter - 1;
		
		if (cell.getStringCellValue().equals("") || cell.getStringCellValue().contains("description file")) {
			if(isCellValueWrong(cell)) {
				machineList.get(index).setPartNrMSW("");
				counter++;
			}
		}
		else if(counter < 7){
			machineList.get(index).setPartNrMSW(cell.getStringCellValue());
			counter++;
		}
		else if(counter >= 7) {
			machineList.get((index-6)).setDescFileMSW(cell.getStringCellValue());
			counter++;
		}
		else {
			counter = 0; 
		}
		
	}
	
	/**
	 * Function that takes the cell and adds the artnr for the HW to the machines.
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void setHWArtNr(Cell cell, ArrayList<Machine> machineList) {
		
		int index = counter - 1;
		
		if (cell.getStringCellValue().equals("") || cell.getStringCellValue().contains("description file")) {
			if(isCellValueWrong(cell)) {
				machineList.get(index).setPartNrHW("");
				counter++;
			}
		}
		else if(counter < 7){
			machineList.get(index).setPartNrHW(cell.getStringCellValue());
			counter++;
		}
		else if(counter >= 7) {
			machineList.get((index-6)).setDescFileHW(cell.getStringCellValue());
			counter++;
		}
		else {
			counter = 0; 
		}
		
	}
	
	/**
	 * Function that takes the cell and adds the artnr for the DSTOne to the machines.
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void setDSTOneArtNr(Cell cell, ArrayList<Machine> machineList) {
		
		int index = counter - 1;
		
		if (cell.getStringCellValue().equals("") || cell.getStringCellValue().contains("description file")) {
			if(isCellValueWrong(cell)) {
				machineList.get(index).setPartNrDST1("");
				counter++;
			}
		}
		else if(counter < 7){
			machineList.get(index).setPartNrDST1(cell.getStringCellValue());
			counter++;
		}
		else if(counter >= 7) {
			machineList.get((index-6)).setDescFileDST1(cell.getStringCellValue());
			counter++;
		}
		else {
			counter = 0; 
		}
		
	}
	
	/**
	 * Function that takes the cell and adds the artnr for the DSTTwo to the machines.
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void setDSTTwoArtNr(Cell cell, ArrayList<Machine> machineList) {
		
		int index = counter - 1;
		
		if (cell.getStringCellValue().equals("") || cell.getStringCellValue().contains("description file")) {
			if(isCellValueWrong(cell)) {
				machineList.get(index).setPartNrDST2("");
				counter++;
			}
		}
		else if(counter < 7){
			machineList.get(index).setPartNrDST2(cell.getStringCellValue());
			counter++;
		}
		else if(counter >= 7) {
			machineList.get((index-6)).setDescFileDST2(cell.getStringCellValue());
			counter++;
		}
		else {
			counter = 0; 
		}
		
	}
	
	/**
	 * Function that takes the cell and adds the artnr for the Downloader to the machines.
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void setDownArtNr(Cell cell, ArrayList<Machine> machineList) {
		
		int index = counter - 1;
		
		if (cell.getStringCellValue().equals("") || cell.getStringCellValue().contains("description file")) {
			if(isCellValueWrong(cell)) {
				machineList.get(index).setPartNrDown("");
				counter++;
			}
		}
		else if(counter < 7){
			machineList.get(index).setPartNrDown(cell.getStringCellValue());
			counter++;
		}
		else if(counter >= 7 && counter < 13) {
			machineList.get((index-6)).setDescFileDown(cell.getStringCellValue());
			counter++;
		}
		else {
			counter = 0; 
		}
		
	}
	

	/**
	 * Function that check is a artnr is missing in the excel-file
	 * a error-msg is sent to the user.
	 * 
	 * @param cell
	 * @return boolean
	 */
	private boolean isCellValueWrong(Cell cell) {
		
		if (counter < 7 && cell.getStringCellValue().equals("")) {
			
			//SEND ERROR-MESSAGE TO USER, SEND CELL-REFERENCE IN ERROR MSG
			
			String errStr = "Error, cell value not valid. Cell: " + cell.getAddress() + "\n";
			this.addVerificationFailureResult(1);
			
			addSEVerificationResults(errStr);
			
			return true;
		}
		else if(counter > 7 && counter < 12 && cell.getStringCellValue().equals("")) {
			
			//SEND ERROR-MESSAGE TO USER, SEND CELL-REFERENCE IN ERROR MSG
			
			String errStr = "Error, cell value not valid. Cell: " + cell.getAddress() + "\n";
			
			addSEVerificationResults(errStr);
			this.addVerificationFailureResult(1);
			
			return true;
		}
		else {
			return false;
		}
		
	}
	
	private void variableSetup(HMIM hmim, VOneECU vOne, VTwoECU vTwo) {
		
		this.setHmimECU(hmim);
		this.setvOneECU(vOne);
		this.setvTwoECU(vTwo);
		
		this.setESWDelivery("");
		
		this.seVerificationResult = new StringBuilder();
		this.setSeToolFaliureCount(0);
		
		this.setCounter(0);
		
		this.setECU_NTP(false);
		this.setECU_MSW(false);
		this.setECU_HW(false);
		this.setECU_DST2(false);
		this.setECU_DST1(false);
		this.setECU_Down(false);
		
		this.setReadExcelCol("");
		this.setReadDescFileNr(false);
		
	}
	
	
	private void addSEVerificationResults(String results) {
		
		this.seVerificationResult.append(results);
		
	}
	
	public String getVerificationResult() {
		return this.seVerificationResult.toString();
	}
	
	public int getVerificationFailureResult() {
		return this.getSeToolFaliureCount();
	}
	
	private void addVerificationFailureResult(int count) {
		int temp = (this.getSeToolFaliureCount() + count);
		
		this.setSeToolFaliureCount(temp);
	}
	
	
	/**
	 * Function that prints the machines to the verification string.
	 * 
	 */
	private void printMachinesAndVerifiResults() {

		this.getHmimECU().setNodeTemplate(this.getHmimECU().getMachinePark().get(0).getNodeTemplate());
		this.getvOneECU().setNodeTemplate(this.getvOneECU().getMachinePark().get(0).getNodeTemplate());
		this.getvTwoECU().setNodeTemplate(this.getvTwoECU().getMachinePark().get(0).getNodeTemplate());

		this.addSEVerificationResults("***** HMIM I-ECU *****\n");
		printMachines(this.getHmimECU().getMachinePark());

		this.addSEVerificationResults("---------------------------------------------------------- \n");
		this.addSEVerificationResults("\n*****   V-ECU    *****\n");
		printMachines(this.getvOneECU().getMachinePark());

		this.addSEVerificationResults("---------------------------------------------------------- \n");
		this.addSEVerificationResults("\n*****   V2-ECU   *****\n");
		printMachines(this.getvTwoECU().getMachinePark());

	}

	private String seToolHeader() {
		
		
		String headerStr =  "---------------------------------------------------------- \n" +
						    "|        *******  SE TOOL VERIFICATION  *******          | \n" +
							"---------------------------------------------------------- \n\n";
		
		return headerStr;
	}
	
	
	private void printMachines(ArrayList<Machine> tempList) {
	
		String tempStr = "";
		
		for (int i = 0; i < tempList.size(); i++ ) {
			
			Machine temp = tempList.get(i);
			
			tempStr =   "\n Machine: " + temp.getMachineName() + " | NTP: " + temp.getNodeTemplate() +
						"\n MSW  Part nr | MSW  Description file: " + temp.getPartNrMSW() + " | " +  temp.getDescFileMSW() +
						"\n HW   Part nr | HW   Description file: " + temp.getPartNrHW() + " | " + temp.getDescFileHW() +
						"\n DST1 Part nr | DST1 Description file: " + temp.getPartNrDST1() + " | " +temp.getDescFileDST1() +
						"\n DST2 Part nr | DST2 Description file: " + temp.getPartNrDST2() + " | " + temp.getDescFileDST2() +
						"\n Down Part nr | Down Description file: " + temp.getPartNrDown() + " | " + temp.getDescFileDown() + "\n";
						
			addSEVerificationResults(tempStr);
		}
	}
	
	
//===========================//
// Get, set and constructor  //
//===========================//
	
	public SETool() { 
		
	}
	
	public String getESWDelivery() {
		return ESWDelivery;
	}

	private void setESWDelivery(String eSWDelivery) {
		ESWDelivery = eSWDelivery;
	}
	
	public String getHMIMNodeTemp() {
		return HMIMNodeTemp;
	}

	private void setHMIMNodeTemp(String hMIMNodeTemp) {
		HMIMNodeTemp = hMIMNodeTemp;
	}

	public String getVOneNodeTemp() {
		return VOneNodeTemp;
	}

	private void setVOneNodeTemp(String vOneNodeTemp) {
		VOneNodeTemp = vOneNodeTemp;
	}

	public String getVTwoNodeTemp() {
		return VTwoNodeTemp;
	}

	private void setVTwoNodeTemp(String vTwoNodeTemp) {
		VTwoNodeTemp = vTwoNodeTemp;
	}

	public HMIM getHmimECU() {
		return hmimECU;
	}

	private void setHmimECU(HMIM hmimECU) {
		this.hmimECU = hmimECU;
	}

	public VOneECU getvOneECU() {
		return vOneECU;
	}

	private void setvOneECU(VOneECU vOneECU) {
		this.vOneECU = vOneECU;
	}

	public VTwoECU getvTwoECU() {
		return vTwoECU;
	}

	private void setvTwoECU(VTwoECU vTwoECU) {
		this.vTwoECU = vTwoECU;
	}

	private int getSeToolFaliureCount() {
		return seToolFaliureCount;
	}

	private void setSeToolFaliureCount(int seToolFaliureCount) {
		this.seToolFaliureCount = seToolFaliureCount;
	}

	private boolean isECU_NTP() {
		return ECU_NTP;
	}

	private void setECU_NTP(boolean eCU_NTP) {
		ECU_NTP = eCU_NTP;
	}

	private boolean isECU_MSW() {
		return ECU_MSW;
	}

	private void setECU_MSW(boolean eCU_MSW) {
		ECU_MSW = eCU_MSW;
	}

	private boolean isECU_HW() {
		return ECU_HW;
	}

	private void setECU_HW(boolean eCU_HW) {
		ECU_HW = eCU_HW;
	}

	private boolean isECU_DST1() {
		return ECU_DST1;
	}

	private void setECU_DST1(boolean eCU_DST1) {
		ECU_DST1 = eCU_DST1;
	}

	private boolean isECU_DST2() {
		return ECU_DST2;
	}

	private void setECU_DST2(boolean eCU_DST2) {
		ECU_DST2 = eCU_DST2;
	}

	private boolean isECU_Down() {
		return ECU_Down;
	}

	private void setECU_Down(boolean eCU_Down) {
		ECU_Down = eCU_Down;
	}

	private boolean isReadDescFileNr() {
		return readDescFileNr;
	}

	private void setReadDescFileNr(boolean readDescFileNr) {
		this.readDescFileNr = readDescFileNr;
	}
	
	private int getCounter() {
		return this.counter;
	}
	
	private void setCounter(int counter) {
		this.counter = counter;
	}
	
	private void incrementCounter(int counter) {
		this.counter = (this.getCounter()+counter);
	}
	
	private String getReadExcelCol() {
		return readExcelCol;
	}
	
	private void setReadExcelCol(String readExcelCol) {
		
		if(readExcelCol.equals("")) {
			this.readExcelCol = readExcelCol;
		}
		else {
			
			String temp = "";
			int index = 0; 
			
			while(index < readExcelCol.length()) {
				if (Character.isLetter(readExcelCol.charAt(index))) {
					temp = temp + readExcelCol.charAt(index);
				}
				index++;
			}
			this.readExcelCol = temp;
		}
	}

	
}//CLass Bracket
