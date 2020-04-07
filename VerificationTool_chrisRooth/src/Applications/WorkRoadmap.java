package Applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ECU.HMIM;
import ECU.VOneECU;
import ECU.VTwoECU;
import machines.Machine;

/**
 * WorkRoadmap reader for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class WorkRoadmap {
	
	
	private HMIM hmim;
	private VOneECU vOne;
	private VTwoECU vTwo;
	
	private String ESWDelivArt;
	private String readExcelCol;
	private String sheetName;
	
	private boolean I_ECU;
	private boolean VOne_ECU;
	private boolean VTwo_ECU;
	
	private boolean nodeTemp;
	private boolean hw;
	private boolean Downloader;
	private boolean DSTOne;
	private boolean DSTTwo;
	private boolean MSW;
	private boolean decsFile;
	private boolean ESWArtValid;
	
	private boolean startVerification;
	
	private StringBuilder workRoadmapVerificationResult;
	
	
	/**
	 * Function that gets called from the verification tool. This function 
	 * opens the excel-file and gets the ECU-lists that is to be matched 
	 * against. 
	 * 
	 * @param filePathName
	 * @param hmimEcu
	 * @param vOneEcu
	 * @param vTwoEcu
	 * @param ESWDelivery
	 * @return
	 */
	public boolean readWorkRoadmapExcelFile(File workRoadmapFile, HMIM hmimEcu, VOneECU vOneEcu, VTwoECU vTwoEcu, String ESWDelivery) {
		
		
		XSSFWorkbook workRoadmapWb = null;
		varSetup(workRoadmapFile.getPath(), hmimEcu, vOneEcu, vTwoEcu, ESWDelivery);
		
		try {
			
			if(!workRoadmapFile.getPath().equals("")) {
				
				ZipSecureFile.setMinInflateRatio(0);
				workRoadmapWb = new XSSFWorkbook(workRoadmapFile);
				
				addWorkRoadmapVerificationResults(this.workRoadmapHeader());
				
				listSheets(workRoadmapWb);
				
			}

		} catch (InvalidFormatException e) {
//			System.out.println("Error with WorkRoadmap workbook " + e);
			addWorkRoadmapVerificationResults("Error with WorkRoadmap workbook " + e);
			
		} catch (IOException e) {
//			System.out.println("Error with WorkRoadmap workbook " + e);
			addWorkRoadmapVerificationResults("Error with WorkRoadmap workbook " + e);
		}
		
		return true;
	}
	
	/**
	 * Function that iterates thru the sheets (machines) in the excel-file.
	 * 
	 * @param roadmapWb
	 */
	private void listSheets(XSSFWorkbook roadmapWb) {
		
		for ( Iterator<Sheet> sheetIter = roadmapWb.iterator(); sheetIter.hasNext(); ) {
						
			Sheet sheet = sheetIter.next();
			this.setReadExcelCol("");
			this.setESWArtValid(true);

			if(sheet.getSheetName().contains("SEMS") || sheet.getSheetName().equals("A25G Stage5 - T2")) {
				this.setStartVerification(false);
				break;
			}
			else if (sheet.getSheetName().equals("A25G U16 - T2")){
				this.setStartVerification(true);
			}
			else {
				
			}
			
			if (this.isStartVerification()) {
				
				this.setSheetName(sheet.getSheetName());

				addWorkRoadmapVerificationResults("Machine in roadmap: |" + this.getSheetName() + "|\n");
				addWorkRoadmapVerificationResults("\n");
				System.out.println("Machine in roadmap: |" + this.getSheetName() + "|");
				System.out.println();
				
				locateESWDevArtNr(sheet);
			}
		}
	}
	
	
	/**
	 * Function that looks in the excel sheet and checks the 
	 * after the ESW Art for the current machine and the ECUs. 
	 * 
	 * @param sheet
	 */
	private void locateESWDevArtNr(Sheet sheet) {
		
		for ( Iterator<Row> row = sheet.iterator(); row.hasNext(); ) {
			
			Row nextRow = row.next();
			Iterator<Cell> cell = nextRow.iterator();
			
			while( cell.hasNext() ) {
				
				Cell nextCell = cell.next();
				nextCell.setCellType(Cell.CELL_TYPE_STRING);
				
				if(nextCell.getStringCellValue().equals(this.getESWDelivArt()) && this.isESWArtValid()) {
					
					if(this.getReadExcelCol().equals("")) {
						this.setReadExcelCol(nextCell.getAddress().toString());
					}
				}
				
				else if(nextCell.getStringCellValue().equals("I-ECU") && this.isESWArtValid()) {
					
					if (this.getReadExcelCol().equals("")) {
						printESWErrorMsg(sheet.getSheetName());
						this.setESWArtValid(false);
					}
					else {
						
						this.addWorkRoadmapVerificationResults("*** I-ECU ***\n");
						this.addWorkRoadmapVerificationResults("----------------------------------------------------\n");
						
						System.out.println("*** I-ECU ***");
						System.out.println("----------------------------------------------------");
						
						this.setI_ECU(true);
					}
					
				}
				
				else if(nextCell.getStringCellValue().equals("V-ECU") && this.isESWArtValid()) {
		
					addWorkRoadmapVerificationResults("\n*** V-ECU ***\n");
					addWorkRoadmapVerificationResults("----------------------------------------------------\n");
					
					System.out.println("\n*** V-ECU ***");
					System.out.println("----------------------------------------------------");
					
					this.setI_ECU(false);
					this.setVOne_ECU(true);
					
				}
				
				else if(nextCell.getStringCellValue().equals("V2-ECU") && this.isESWArtValid()) {

					addWorkRoadmapVerificationResults("\n*** V2-ECU ***\n");
					addWorkRoadmapVerificationResults("----------------------------------------------------\n");
					
					System.out.println("\n*** V2-ECU ***");
					System.out.println("----------------------------------------------------");
					
					this.setVOne_ECU(false);
					this.setVTwo_ECU(true);
					
				}
				
				else if(nextCell.getStringCellValue().contains("W-ECU") && this.isESWArtValid()) {
					
					addWorkRoadmapVerificationResults("\n");
					System.out.println();
					
					this.setVTwo_ECU(false);
					this.setReadExcelCol("");
					
				}
				
				else {
					//Default
				}
				
				whatECUtoVerify(nextCell);
			
			}
			
		}
		
	}
	
	/**
	 * Function that checks what ECU that is currently to be 
	 * verified. 
	 * 
	 * @param cell
	 */
	private void whatECUtoVerify(Cell cell) {
		
		if (this.isI_ECU()) {
			whatTypeToRead(cell);
			verifyECUValues(cell, this.getHmim().getMachinePark());
		}
		else if (this.isVOne_ECU()){
			whatTypeToRead(cell);
			verifyECUValues(cell, this.getvOne().getMachinePark());
		}
		else if(this.isVTwo_ECU()) {
			whatTypeToRead(cell);
			verifyECUValues(cell, this.getvTwo().getMachinePark());
		}
		else {
			
		}
		
	}
	
	
	/**
	 * Function that checks what node that is to be checked. 
	 * 
	 * @param cell
	 */
	private void whatTypeToRead(Cell cell) {
		
		if (cell.getStringCellValue().contains("Node Template")) {
			this.setNodeTemp(true);
			
		}
		else if (cell.getStringCellValue().contains("Hardware")) {
			this.setHw(true);
			
		}
		else if (cell.getStringCellValue().contains("Main software")) {		
			this.setMSW(true);
			
		}
		else if (cell.getStringCellValue().contains("Downloader")) {
			this.setDownloader(true);
			
		}
		else if (cell.getStringCellValue().contains("Dataset 1")) {
			this.setDSTOne(true);
			
		}
		else if (cell.getStringCellValue().contains("Dataset 2")) {
			this.setDSTTwo(true);
			
		}
		else {
			
		}
		
	}
	
	/**
	 * Checking the art numbers for all the ECU's. 
	 * 
	 * This then send a message back to the user to notify the result
	 * 
	 * @param cell
	 */
	private void verifyECUValues(Cell cell, ArrayList<Machine> machineList) {

		ArrayList<Machine> tempMachineList = machineList;
		String cellColArd = extractCellColLetters(cell.getAddress().toString());
		int machineIndex = whatMachineToCheck(tempMachineList);
		
		if (this.isNodeTemp() && cellColArd.equals(this.getReadExcelCol())) {	
			
			verifyNodeTemp(cell, machineList);
			
		}
		else if (this.isHw() && cellColArd.equals(this.getReadExcelCol()) ) {
	
			String machineHW = tempMachineList.get(machineIndex).getPartNrHW();
			String machineHWDesc = tempMachineList.get(machineIndex).getDescFileHW();
			verifyPartNr(cell, machineIndex, machineHW, machineHWDesc, "Hardware");
		
		}
		else if (this.isMSW() && cellColArd.equals(this.getReadExcelCol())) {
			
			String machineMSW = tempMachineList.get(machineIndex).getPartNrMSW();
			String machineMSWDesc = tempMachineList.get(machineIndex).getDescFileMSW();
			verifyPartNr(cell, machineIndex, machineMSW, machineMSWDesc, "MSW");
			
		}
		else if (this.isDownloader() && cellColArd.equals(this.getReadExcelCol()) ) {
			
			String machineDown = tempMachineList.get(machineIndex).getPartNrDown();
			String machineDownDesc = tempMachineList.get(machineIndex).getDescFileDown();
			verifyPartNr(cell, machineIndex, machineDown, machineDownDesc, "Downloader");
			
		}
		else if (this.isDSTOne() && cellColArd.equals(this.getReadExcelCol()) ) {
			
			String machineDST1 = tempMachineList.get(machineIndex).getPartNrDST1();
			String machineDST1Desc = tempMachineList.get(machineIndex).getDescFileDST1();
			verifyPartNr(cell, machineIndex, machineDST1, machineDST1Desc, "Dataset 1");
			
		}
		else if (this.isDSTTwo() && cellColArd.equals(this.getReadExcelCol()) ) {
			
			String machineDST2 = tempMachineList.get(machineIndex).getPartNrDST2();
			String machineDST2Desc = tempMachineList.get(machineIndex).getDescFileDST2();
			verifyPartNr(cell, machineIndex, machineDST2, machineDST2Desc, "Dataset 2");
			
		}
		else {
			//Default
			System.out.println("Cell address: " + cell.getAddress() + " and cell value: " + cell.getStringCellValue());
		}
		
	}
	
	/**
	 * Function that verifies the nodetemplate number, sends error message if 
	 * not valid. 
	 * 
	 * @param cell
	 * @param machineList
	 */
	private void verifyNodeTemp(Cell cell, ArrayList<Machine> machineList) {
		
		int machineIndex = whatMachineToCheck(machineList);
		String machineNodeTemp = machineList.get(machineIndex).getNodeTemplate();
		
		if(machineIndex < 6 && cell.getStringCellValue().equals(machineNodeTemp)) {
			printVerificationResult("Nodetemplate", cell.getStringCellValue() , machineNodeTemp);
			this.setNodeTemp(false);
		} 
		else {
			printVerificationErrorMsg("Nodetemplate", cell.getStringCellValue() , machineNodeTemp);
			this.setNodeTemp(false);
		}
		
	}
	
	/**
	 * Function that verifies the part numbers for the ECU's. Sends error message if
	 * not valid. 
	 * 
	 * @param cell
	 * @param machineIndex
	 * @param partNr
	 * @param decsNr
	 * @param ECUName
	 */
	private void verifyPartNr(Cell cell, int machineIndex, String partNr, String decsNr, String ECUName) {
		
		String cellColArd = extractCellColLetters(cell.getAddress().toString());
		
		if(machineIndex < 6 && cell.getStringCellValue().equals(partNr) && !this.isDecsFile()) {
			printVerificationResult(ECUName, cell.getStringCellValue() , partNr);
			this.setDecsFile(true);
			
		}
		else if (this.isDecsFile() && cellColArd.equals(this.getReadExcelCol())) {
			
			verifyDescFile(cell, machineIndex, decsNr, ECUName);
			resetECUtoFalse();
			
		}
		else {
			System.out.println("Cell address is: " + cell.getAddress() + " and cell value: " + cell.getStringCellValue());
			printVerificationErrorMsg(ECUName, cell.getStringCellValue() , partNr);
			resetECUtoFalse();
			this.setDecsFile(false);
		}
		
	}
	
	/**
	 * Function that verifies the description file number, Send error message of 
	 * not valid. 
	 * 
	 * @param cell
	 * @param machineIndex
	 * @param descNr
	 * @param ECUName
	 */
	private void verifyDescFile(Cell cell, int machineIndex, String descNr, String ECUName) {

		if (machineIndex < 6 && cell.getStringCellValue().equals(descNr)) {
			printVerificationResult((ECUName + " Desc"), cell.getStringCellValue() , descNr);
			resetECUtoFalse();
			this.setDecsFile(false);
		}
		else {
			printVerificationErrorMsg((ECUName + " Desc"), cell.getStringCellValue() , descNr);
			resetECUtoFalse(); 
			this.setDecsFile(false);
		}
		
	}
	
	/**
	 * Function that sets all the ECU's to false
	 * 
	 */
	private void resetECUtoFalse() {
		
		this.setHw(false);
		this.setMSW(false);
		this.setNodeTemp(false);
		this.setDownloader(false);
		this.setDSTOne(false);
		this.setDSTTwo(false);
		
	}
	
	
	/**
	 * Function that checks what machine that is to be checked. 
	 * Returns an index so the correct machine is picked in 
	 * the machine lists. 
	 * 
	 * @param machineList
	 * @return integer
	 */
	private int whatMachineToCheck(ArrayList<Machine> machineList) {
		
		int index = 0;
		
		while(index < 6) {
			
			Machine machine = machineList.get(index);
			
			if(this.getSheetName().contains(machine.getMachineName())) {
				return index;
			}
			else {
				index++;
			}
		
		}
		
		return index;
	}
	
	
	/**
	 * Takes the cells address (e.g AU12) and returns only the 
	 * colum value (e.g AU)
	 * 
	 * @param cellAdr
	 * @return
	 */
	private String extractCellColLetters(String cellAdr) {
		
		String temp = "";
		int index = 0; 
		
		while(index < cellAdr.length()) {
			if (Character.isLetter(cellAdr.charAt(index))) {
				temp = temp + cellAdr.charAt(index);
			}
			
			index++;
		}

		return temp;
	}
	
	
	
	
	/**
	 * Function that prints the results from the verifications that are done. 
	 * 
	 * @param ECUName
	 * @param roadmapValue
	 * @param machineArt
	 */
	private void printVerificationResult(String ECUName, String roadmapValue, String machineArt) {

		addWorkRoadmapVerificationResults("        **** VERIFICATION SUCCESSFUL *****\n");
		addWorkRoadmapVerificationResults("  **Verification for " + ECUName + " is successful**  \n" );
		addWorkRoadmapVerificationResults("Roadmap value - SE Tool value : " + roadmapValue + " - " + machineArt + "\n");
		addWorkRoadmapVerificationResults("----------------------------------------------------\n");
		
		System.out.println("        **** VERIFICATION SUCCESSFUL *****");
		System.out.println("  **Verification for " + ECUName + " is successful**  " );
		System.out.println("Roadmap value - SE Tool value : " + roadmapValue + " - " + machineArt);
		System.out.println("----------------------------------------------------");
		
	}
	
	
	/**
	 * Function that prints if an error from the verification occurs.
	 * 
	 * @param ECUName
	 * @param roadmapValue
	 * @param machineArt
	 */
	private void printVerificationErrorMsg(String ECUName, String roadmapValue, String machineArt) {

		addWorkRoadmapVerificationResults("        **** VERIFICATION FAILED *****\n");
		addWorkRoadmapVerificationResults("  **Verification for " + ECUName + " has FAILED**  \n");
		addWorkRoadmapVerificationResults("Roadmap value - SE Tool value : " + roadmapValue + " - " + machineArt + "\n");
		addWorkRoadmapVerificationResults("----------------------------------------------------\n");
		
		System.out.println("        **** VERIFICATION FAILED *****");
		System.out.println("  **Verification for " + ECUName + " has FAILED**  " );
		System.out.println("Roadmap value - SE Tool value : " + roadmapValue + " - " + machineArt);
		System.out.println("----------------------------------------------------");
		
	}
	
	
	/**
	 * Function that prints the error message when ESW ART is not 
	 * active no the given machine model
	 * 
	 * @param sheetName
	 */
	private void printESWErrorMsg(String sheetName) {

		addWorkRoadmapVerificationResults("------------------------------------------------------------------\n");
		addWorkRoadmapVerificationResults("Machine " + sheetName + " is not included in: EWS " + this.getESWDelivArt() + "\n");
		addWorkRoadmapVerificationResults("------------------------------------------------------------------\n");
		
		System.out.println("------------------------------------------------------------------");
		System.out.println("Machine " + sheetName + " is not included in: EWS " + this.getESWDelivArt());
		System.out.println("------------------------------------------------------------------");
	}
	
	/**
	 * Function that setup some variables. 
	 * 
	 * @param filePathName
	 * @param hmimEcu
	 * @param vOneEcu
	 * @param vTwoEcu
	 * @param ESWDelivery
	 */
	private void varSetup(String filePathName, HMIM hmimEcu, VOneECU vOneEcu, VTwoECU vTwoEcu, String ESWDelivery) {
		
		this.setHmim(hmimEcu);
		this.setvOne(vOneEcu);
		this.setvTwo(vTwoEcu);
		
		this.setESWDelivArt(ESWDelivery);
		this.setReadExcelCol("");
		this.setSheetName("");
		
		this.setI_ECU(false);
		this.setVOne_ECU(false);
		this.setVTwo_ECU(false);
		
		this.setDecsFile(false);
		
		this.setESWArtValid(true);
		
		this.setStartVerification(false);
		
		this.workRoadmapVerificationResult = new StringBuilder();
	}

	
	private void addWorkRoadmapVerificationResults(String results) {
		
		this.workRoadmapVerificationResult.append(results);
		
	}
	
	public String getWorkRoadmapVerificationResults() {
		return this.workRoadmapVerificationResult.toString();
	}
	
	
	private String workRoadmapHeader() {
		
		String headerStr =  "\n---------------------------------------------------------- \n" +
						    "|        *******  WORKROADMAP VERIFICATION  *******         | \n" +
							"---------------------------------------------------------- \n\n";
		
		System.out.println("\n---------------------------------------------------------- \n" +
							"|        *******  WORKROADMAP VERIFICATION  *******         | \n" +
							"---------------------------------------------------------- \n\n");
		
		return headerStr;
	}
	

	//===========================//
	// Get, set and constructor  //
	//===========================//

	public WorkRoadmap() {

	}

	private String getESWDelivArt() {
		return ESWDelivArt;
	}


	private void setESWDelivArt(String eSWDelivArt) {
		ESWDelivArt = ("ART " + eSWDelivArt);
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

	private boolean isI_ECU() {
		return I_ECU;
	}


	private void setI_ECU(boolean i_ECU) {
		I_ECU = i_ECU;
	}


	private boolean isVOne_ECU() {
		return VOne_ECU;
	}


	private void setVOne_ECU(boolean vOne_ECU) {
		VOne_ECU = vOne_ECU;
	}


	private boolean isVTwo_ECU() {
		return VTwo_ECU;
	}


	private void setVTwo_ECU(boolean vTwo_ECU) {
		VTwo_ECU = vTwo_ECU;
	}


	private boolean isNodeTemp() {
		return nodeTemp;
	}


	private void setNodeTemp(boolean nodeTemp) {
		this.nodeTemp = nodeTemp;
	}


	private boolean isHw() {
		return hw;
	}


	private void setHw(boolean hw) {
		this.hw = hw;
	}


	private boolean isDownloader() {
		return Downloader;
	}


	private void setDownloader(boolean downloader) {
		Downloader = downloader;
	}


	private boolean isDSTOne() {
		return DSTOne;
	}


	private void setDSTOne(boolean dSTOne) {
		DSTOne = dSTOne;
	}


	private boolean isDSTTwo() {
		return DSTTwo;
	}


	private void setDSTTwo(boolean dSTTwo) {
		DSTTwo = dSTTwo;
	}


	private boolean isMSW() {
		return MSW;
	}


	private void setMSW(boolean mSW) {
		MSW = mSW;
	}


	private String getSheetName() {
		return sheetName;
	}


	private void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}


	private boolean isDecsFile() {
		return decsFile;
	}


	private void setDecsFile(boolean decsFile) {
		this.decsFile = decsFile;
	}


	private boolean isESWArtValid() {
		return ESWArtValid;
	}


	private void setESWArtValid(boolean eSWArtValid) {
		ESWArtValid = eSWArtValid;
	}

	private HMIM getHmim() {
		return hmim;
	}

	private void setHmim(HMIM hmim) {
		this.hmim = hmim;
	}

	private VOneECU getvOne() {
		return vOne;
	}

	private void setvOne(VOneECU vOne) {
		this.vOne = vOne;
	}

	private VTwoECU getvTwo() {
		return vTwo;
	}

	private void setvTwo(VTwoECU vTwo) {
		this.vTwo = vTwo;
	}

	private boolean isStartVerification() {
		return startVerification;
	}

	private void setStartVerification(boolean startVerification) {
		this.startVerification = startVerification;
	}


}//END CLASS
