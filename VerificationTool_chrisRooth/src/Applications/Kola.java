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
 * Kola-tool for the Volvo CE Verification tool. 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 
 */
public class Kola implements VerificationMessageInterface{

	private HMIM hmimCatalog;
	private VOneECU vOneCatalog;
	private VTwoECU vTwoCatalog;
	
	private boolean HMIMLocated;
	private boolean VOneLocated;
	private boolean VTwoLocated;
	
	private String extracedCellValue;
	
	private boolean kolaNumberVerified;
	
	private StringBuilder kolaVerificationResult;
	private int kolaFaliureCount;
	

	/**
	 * Function that is called when KOLA is to be verified.
	 * 
	 * @param filePathName
	 * @param hmimEcu
	 * @param vOneEcu
	 * @param vTwoEcu
	 * @return
	 */
	public boolean readKolaExcelFile(File kolaFile, HMIM hmimEcu, VOneECU vOneEcu, VTwoECU vTwoEcu) {
				
		XSSFWorkbook kolaWb = null;
		variableSetup(hmimEcu, vOneEcu, vTwoEcu);

		try {
			
			if(!kolaFile.getPath().equals("")) {
				
				kolaWb = new XSSFWorkbook(kolaFile);
				this.addKolaVerificationResults(this.kolaToolHeader());
				
				readNodetempInEcxelFile(kolaWb);
				printECUStatus();
				this.addKolaVerificationResults("\n");
				
				readPartAndDescInEcxelFile(kolaWb);
			}
			
			
		} catch (InvalidFormatException e) {
			this.addKolaVerificationResults("Error with Kola workbook " + e + "\n");
			this.addVerificationFailureResult(1);
			
		} catch (IOException e) {
			this.addKolaVerificationResults("Error with Kola workbook " + e + "\n");
			this.addVerificationFailureResult(1);
		}
		
		return true;
	}
	
	
	/**
	 * Function that takes the excel-file as a workbook object 
	 * and iterate thru it to find the nodetemplates- 
	 * 
	 * @param kolaWb
	 */
	private void readNodetempInEcxelFile(XSSFWorkbook kolaWb) {
		
		Sheet sheet1 = kolaWb.getSheetAt(0);
		
		for( Iterator<Row> row = sheet1.iterator(); row.hasNext(); ) {
			
			Row nextRow = row.next();
			Iterator<Cell> cell = nextRow.iterator();
			
			while( cell.hasNext() ) {
				
				Cell nextCell = cell.next();
				nextCell.setCellType(Cell.CELL_TYPE_STRING);

				readCellNodetemplateContent(nextCell);
			}
		}
	}
	
	
	/**
	 * Function that takes a copy of the workbook object and iterate 
	 * thru it to find and check the part nr and desc nr. 
	 * 
	 * @param kolaWb
	 */
	private void readPartAndDescInEcxelFile(XSSFWorkbook kolaWb) {
		
		Sheet sheet1 = kolaWb.getSheetAt(0);
		
		for( Iterator<Row> row = sheet1.iterator(); row.hasNext(); ) {
			
			Row nextRow = row.next();
			Iterator<Cell> cell = nextRow.iterator();
			
			while( cell.hasNext() ) {
				
				Cell nextCell = cell.next();
				nextCell.setCellType(Cell.CELL_TYPE_STRING);

				readCellPartAndDescContent(nextCell);
			}
		}
	}

	/**
	 * Reads the cells address value to see if the if it 
	 * in the number column or in the name column. 
	 * 
	 * @param cell
	 */
	private void readCellPartAndDescContent(Cell cell) {
		
		if(cell.getAddress().toString().contains("A")) {
			this.setExtracedCellValue(cell.getStringCellValue());
		}
		else if(cell.getAddress().toString().contains("B")) {
			readCellContent(cell);
		}
		else {
			//Pass
		}
	}
	
	/**
	 * Function that reads the content of the cell and 
	 * the passes it for verification
	 * 
	 * @param cell
	 */
	private void readCellContent(Cell cell) {
		
		if(!this.getExtracedCellValue().equals("Number")) {

			String nodeName =  extractNodeName(cell.getStringCellValue());
			
			this.addKolaVerificationResults("------------------------------------------------------\n");
			this.addKolaVerificationResults("          PART NR AND DESC NR VERIFICATION            \n");
			this.addKolaVerificationResults("------------------------------------------------------\n");

			if(nodeName.equals("noNodeName")) {
				String nodeStr = verifyECUNoNodeName(cell);
				verifyECU(cell, nodeStr);
				this.addKolaVerificationResults("\n");
			}
			else if (nodeName.equals("")) {
				//Pass
			}
			else {
				verifyECU(cell, nodeName);
				this.addKolaVerificationResults("\n");
				
			}
		}
	}
	
	/**
	 * Function that checks what ECU's that are present in 
	 * the excelfile. 
	 * 
	 * @param cell
	 * @param nodeName
	 */
	private void verifyECU(Cell cell, String nodeName) {
		
		int index = locateMachineToBeVerified(cell);
		
		if(this.isHMIMLocated() && !this.isKolaNumberVerified()) {
			this.addKolaVerificationResults("Nodename " + nodeName + " || HMIM\n");
			this.addKolaVerificationResults("------------------------------------------------------\n");
			
			verifyHMIMPartNr(index, nodeName);
		}
		
		if(this.isVOneLocated() && !this.isKolaNumberVerified()) {
			this.addKolaVerificationResults("Nodename " + nodeName + " || VECU\n");
			this.addKolaVerificationResults("------------------------------------------------------\n");
			
			verifyVECUPartNr(index, nodeName);
		}
		
		if(this.isVTwoLocated() && !this.isKolaNumberVerified()) {
			this.addKolaVerificationResults("Nodename " + nodeName + " || V2ECU\n");
			this.addKolaVerificationResults("------------------------------------------------------\n");
			
			verifyV2ECUPartNr(index, nodeName);
		}
		
		this.setKolaNumberVerified(false);

	}
	
	
	/**
	 * Function that gets called of a nodename (e.g DST1 or MSW) is missing in the 
	 * name-cell. 
	 * 
	 * @param cell
	 * @return
	 */
	private String verifyECUNoNodeName(Cell cell) {

		if(cell.getStringCellValue().contains("HMIM") || cell.getStringCellValue().contains("I-ECU")) {
			return findNodeName(this.getHmimCatalog().getMachinePark());
		}
		else if(cell.getStringCellValue().contains("VECU") || cell.getStringCellValue().contains("V-ECU")) {
			return findNodeName(this.getvOneCatalog().getMachinePark());
		}
		else if(cell.getStringCellValue().contains("V2ECU") || cell.getStringCellValue().contains("V2-ECU")) {
			return findNodeName(this.getvTwoCatalog().getMachinePark());
		}
		else {
			return "";
		}

	}
	
	
	/**
	 * Function that looks for what Node the extracted number 
	 * belongs to. 
	 * 
	 * @param machineList
	 * @return
	 */
	private String findNodeName(ArrayList<Machine> machineList) {
		
		if(machineList.get(0).getPartNrMSW().equals(this.getExtracedCellValue())) {
			return "MSW";
		}
		else if(machineList.get(0).getDescFileMSW().equals(this.getExtracedCellValue())) {
			return "MSWDesc";
		}
		else if(machineList.get(0).getPartNrDST1().equals(this.getExtracedCellValue())) {
			return "DST1";
		}
		else if(machineList.get(0).getDescFileDST1().equals(this.getExtracedCellValue())) {
			return "DST1Desc";
		}
		else if(machineList.get(0).getPartNrDST2().equals(this.getExtracedCellValue())) {
			return "DST2";
		}
		else if(machineList.get(0).getDescFileDST2().equals(this.getExtracedCellValue())) {
			return "DST2Desc";
		}
		else {
			return "";
		}
	
	}
	
	/** 
	 * Function that takes the cell-content and checks what node 
	 * that is to be verified. 
	 * 
	 * @param cellContent
	 * @return
	 */
	private String extractNodeName(String cellContent) {
		
		if(cellContent.contains("SOFTWARE") && cellContent.contains("MSW")) {
			return "MSW";
		}
		else if(cellContent.contains("DATASET") && cellContent.contains("DST1")) {
			return "DST1";
		}
		else if(cellContent.contains("DATASET") && cellContent.contains("DST2")) {
			return "DST2";
		}
		else if(cellContent.contains("DISCRIPTION FILE") || cellContent.contains("DESCRIPTION FILE") 
					&& cellContent.contains("MSW")) {
			return "MSWDesc";
		}
		else if(cellContent.contains("DISCRIPTION FILE") || cellContent.contains("DESCRIPTION FILE")
					&& cellContent.contains("DST1")) {
			return "DST1Desc";
		}
		else if(cellContent.contains("DISCRIPTION FILE") || cellContent.contains("DESCRIPTION FILE")
					&& cellContent.contains("DST2")) {
			return "DST2Desc";
		}
		else {
			return "noNodeName";
		}
		
	}
	
	
	/**
	 * Function that takes the content of the name-cell and look 
	 * for what machine that is to be verified. 
	 * 
	 * @param cell
	 * @return
	 */
	private int locateMachineToBeVerified(Cell cell) {
		
		if(cell.getStringCellValue().contains("A25-A45G") || cell.getStringCellValue().contains("A25G-A45G") ) {
			return 6;
		}
	    else if(cell.getStringCellValue().contains("A25G")) {
			return 0;
		}
		else if(cell.getStringCellValue().contains("A30G")) {
			return 1;
		}
		else if(cell.getStringCellValue().contains("A35G")) {
			return 2;
		}
		else if(cell.getStringCellValue().contains("A40G")) {
			return 3;
		}
		else if(cell.getStringCellValue().contains("A45G")) {
			return 4;
		}
		else if(cell.getStringCellValue().contains("A60G")) {
			return 5;
		}
		else {
			return 6;
		}
		
	}
	
	/**
	 * Function that starts the verification for the HMIM ECU. 
	 * 
	 * @param index
	 * @param nodeName
	 */
	private void verifyHMIMPartNr(int index, String nodeName) {
		
		ArrayList<Machine> tempList = this.getHmimCatalog().getMachinePark();
		String ecuNodetemplate = this.getHmimCatalog().getNodeTemplate();
		
		if(index == 6) {
			verificationMachinesNodePartNr(index, nodeName, ecuNodetemplate, tempList);
		}
		else {
			verificationMachineNodePartNr(index, nodeName, ecuNodetemplate, tempList);
		}
		
		verifyHMIMDescNr(index, nodeName);
	}
	
	/**
	 * Function that starts the verification for the VECU
	 * 
	 * @param index
	 * @param nodeName
	 */
	private void verifyVECUPartNr(int index, String nodeName) {
		
		ArrayList<Machine> tempList = this.getvOneCatalog().getMachinePark();
		String ecuNodetemplate = this.getvOneCatalog().getNodeTemplate();
		
		if(index == 6) {
			verificationMachinesNodePartNr(index, nodeName, ecuNodetemplate, tempList);
		}
		else {
			verificationMachineNodePartNr(index, nodeName, ecuNodetemplate, tempList);
		}
		
		verifyVECUDescNr(index, nodeName);
	}
	
	/**
	 * Function that starts the verification for the V2ECU
	 * 
	 * @param index
	 * @param nodeName
	 */
	private void verifyV2ECUPartNr(int index, String nodeName) {
		
		ArrayList<Machine> tempList = this.getvTwoCatalog().getMachinePark();
		String ecuNodetemplate = this.getvTwoCatalog().getNodeTemplate();
		
		if(index == 6) {
			verificationMachinesNodePartNr(index, nodeName, ecuNodetemplate, tempList);
		}
		else {
			verificationMachineNodePartNr(index, nodeName, ecuNodetemplate, tempList);
		}
		
		verifyV2ECUDescNr(index, nodeName);
	
	}
	
	/**
	 * Function that starts the verification for the HMIM Description file
	 * 
	 * @param index
	 * @param nodeName
	 */
	private void verifyHMIMDescNr(int index, String nodeName) {
		
		ArrayList<Machine> tempList = this.getHmimCatalog().getMachinePark();
		String ecuNodetemplate = this.getHmimCatalog().getNodeTemplate();
		
		if(index == 6) {
			verificationMachinesNodeDescNr(index, nodeName, ecuNodetemplate, tempList);
			
		}
		else {
			verificationMachineNodeDescNr(index, nodeName, ecuNodetemplate, tempList);
		}
	}
	
	/**
	 * Function that starts the verification for the VECU Description file
	 * 
	 * @param index
	 * @param nodeName
	 */
	private void verifyVECUDescNr(int index, String nodeName) {
		
		ArrayList<Machine> tempList = this.getvOneCatalog().getMachinePark();
		String ecuNodetemplate = this.getvOneCatalog().getNodeTemplate();
		
		if(index == 6) {
			verificationMachinesNodeDescNr(index, nodeName, ecuNodetemplate, tempList);
			
		}
		else {
			verificationMachineNodeDescNr(index, nodeName, ecuNodetemplate, tempList);
		}
	}
	
	/**
	 * Function that starts the verification for the V2ECU Description file
	 * 
	 * @param index
	 * @param nodeName
	 */
	private void verifyV2ECUDescNr(int index, String nodeName) {
		
		ArrayList<Machine> tempList = this.getvTwoCatalog().getMachinePark();
		String ecuNodetemplate = this.getvTwoCatalog().getNodeTemplate();
		
		if(index == 6) {
			verificationMachinesNodeDescNr(index, nodeName, ecuNodetemplate, tempList);
			
		}
		else {
			verificationMachineNodeDescNr(index, nodeName, ecuNodetemplate, tempList);
		}
	}
	
	/**
	 * Function that runs the verification for the Part number, runs thru the 
	 * machine list. 
	 * 
	 * @param index
	 * @param nodeName
	 * @param ecuNodetemplate
	 * @param machineList
	 */
	private void verificationMachinesNodePartNr(int index, String nodeName, String ecuNodetemplate, ArrayList<Machine> machineList) {
		
		if(nodeName.equals("MSW")) {
			for(int i = 0; i < index-1; i++) {
				runVerificationPartNr(machineList.get(i).getMachineName(),ecuNodetemplate, machineList.get(i).getPartNrMSW());
			}
		}
		else if(nodeName.equals("DST1")) {
			for(int i = 0; i < index-1; i++) {
				runVerificationPartNr(machineList.get(i).getMachineName(), ecuNodetemplate, machineList.get(i).getPartNrDST1());
			}
		}
		else if(nodeName.equals("DST2")) {
			for(int i = 0; i < index-1; i++) {
				runVerificationPartNr(machineList.get(i).getMachineName(), ecuNodetemplate, machineList.get(i).getPartNrDST2());
			}
		}
		else {
			//Pass
		}
		
	}
	
	/**
	 * Function that runs the verification for the Part number, checks a 
	 * specific machine. 
	 * 
	 * @param index
	 * @param nodeName
	 * @param ecuNodetemplate
	 * @param machineList
	 */
	private void verificationMachineNodePartNr(int index, String nodeName, String ecuNodetemplate, ArrayList<Machine> machineList) {
		
		if(nodeName.equals("MSW")) {
			runVerificationPartNr(machineList.get(index).getMachineName(), ecuNodetemplate, machineList.get(index).getPartNrMSW());
		}
		else if(nodeName.equals("DST1")) {
			runVerificationPartNr(machineList.get(index).getMachineName(), ecuNodetemplate, machineList.get(index).getPartNrDST1());
		}
		else if(nodeName.equals("DST2")) {
			runVerificationPartNr(machineList.get(index).getMachineName(), ecuNodetemplate, machineList.get(index).getPartNrDST2());
		}
		else {
			//Pass
		}
		
	}
	
	/**
	 * Function that runs the verification for the Description number, runs thru the 
	 * machine list. 
	 * 
	 * @param index
	 * @param nodeName
	 * @param ecuNodetemplate
	 * @param machineList
	 */
	private void verificationMachinesNodeDescNr(int index, String nodeName, String ecuNodetemplate, ArrayList<Machine> machineList) {
		
		if(nodeName.equals("MSWDesc")) {
			for(int i = 0; i < index-1; i++) {
				runVerificationDescNr(machineList.get(i).getMachineName(), ecuNodetemplate, machineList.get(i).getDescFileMSW());
			}
		}
		
		else if(nodeName.equals("DST1Desc")) {
			for(int i = 0; i < index-1; i++) {
				runVerificationDescNr(machineList.get(i).getMachineName(), ecuNodetemplate, machineList.get(i).getDescFileDST1());
			}
		}
		else if(nodeName.equals("DST2Desc")) {
			for(int i = 0; i < index-1; i++) {
				runVerificationDescNr(machineList.get(i).getMachineName(), ecuNodetemplate, machineList.get(i).getDescFileDST2());
			}
		}
		else {
			//Pass
		}
		
	}
	
	/**
	 * Function that runs the verification for the Description number, checks a 
	 * specific machine. 
	 * 
	 * @param index
	 * @param nodeName
	 * @param ecuNodetemplate
	 * @param machineList
	 */
	private void verificationMachineNodeDescNr(int index, String nodeName,String ecuNodetemplate, ArrayList<Machine> machineList) {
		
		if(nodeName.equals("MSWDesc")) {
			runVerificationDescNr(machineList.get(index).getMachineName(),ecuNodetemplate, machineList.get(index).getDescFileMSW());
		}
		else if(nodeName.equals("DST1Desc")) {
			runVerificationDescNr(machineList.get(index).getMachineName(), ecuNodetemplate, machineList.get(index).getDescFileDST1());
		}
		else if(nodeName.equals("DST2Desc")) {
			runVerificationDescNr(machineList.get(index).getMachineName(), ecuNodetemplate, machineList.get(index).getDescFileDST2());
		}
		else {
			//Pass
		}
		
	}
	
	/**
	 * Function that makes the verification between the KOLA part number and 
	 * SE-Tool part number.
	 * 
	 * @param machineName
	 * @param ecuNodetemplate
	 * @param partNr
	 */
	private void runVerificationPartNr(String machineName, String ecuNodetemplate, String partNr) {
		
		if (this.getExtracedCellValue().equals(partNr)) {
			this.addKolaVerificationResults("VERIFICATION FOR MACHINE SUCCESSFUL: " + machineName + "\n");
			this.addKolaVerificationResults("KOLA - SE-Tool || " +this.getExtracedCellValue() + " - " + partNr + "\n");
			this.addKolaVerificationResults("------------------------------------------------------\n");
			
			this.setKolaNumberVerified(true);
		}
		else {
			
		}
		
	}
	
	/**
	 * Function that makes the verification between the KOLA description number and 
	 * SE-Tool description number.
	 * 
	 * @param machineName
	 * @param ecuNodetemplate
	 * @param partNr
	 */
	private void runVerificationDescNr(String machineName, String ecuNodetemplate, String partNr) {
		
		if (this.getExtracedCellValue().equals(partNr)) {
			this.addKolaVerificationResults("DESCRIPTION FILE VERIFICATION FOR MACHINE SUCCESSFUL: " + machineName + "\n");
			this.addKolaVerificationResults("KOLA - SE-Tool || " +this.getExtracedCellValue() + " - " + partNr + "\n");
			this.addKolaVerificationResults("------------------------------------------------------\n");
			
			this.setKolaNumberVerified(true);
		}
		else {
		}
		
	}
	
	/**
	 * Function that reads the cell to locate what ECU the nodetemplate 
	 * belongs to. 
	 * 
	 * @param cell
	 */
	private void readCellNodetemplateContent(Cell cell) {
		
		if(cell.getStringCellValue().equals(this.getHmimCatalog().getNodeTemplate())) {
			this.addKolaVerificationResults("\n");
			this.addKolaVerificationResults("** HMIM ECU **\n");
			
			this.setHMIMLocated(true);
			verifyNodetempForMachines(cell.getStringCellValue(), this.getHmimCatalog().getMachinePark());
		}
		else if(cell.getStringCellValue().equals(this.getvOneCatalog().getNodeTemplate())) {
			this.addKolaVerificationResults("\n");
			this.addKolaVerificationResults("** V ONE ECU **\n");
			
			this.setVOneLocated(true);
			verifyNodetempForMachines(cell.getStringCellValue(), this.getvOneCatalog().getMachinePark());
		}
		else if(cell.getStringCellValue().equals(this.getvTwoCatalog().getNodeTemplate())) {
			this.addKolaVerificationResults("\n");
			this.addKolaVerificationResults("** V TWO ECU **\n");
			
			this.setVTwoLocated(true);
			verifyNodetempForMachines(cell.getStringCellValue(), this.getvTwoCatalog().getMachinePark());
		}
		else {
			//Pass
		}
		
	}
	
	/**
	 * Function that takes the Kola nodetemplate number and verifies 
	 * it with the machine of that ECU
	 * 
	 * @param nodetempKola
	 * @param ecuList
	 */
	private void verifyNodetempForMachines(String nodetempKola, ArrayList<Machine> ecuList) {
		this.addKolaVerificationResults("------------------------------------------------------\n");
		this.addKolaVerificationResults("            NODETEMPLATE VERIFICATION                 \n");
		this.addKolaVerificationResults("------------------------------------------------------\n");
		
		for(int i = 0; i < ecuList.size(); i++) {
			
			if (nodetempKola.equals(ecuList.get(i).getNodeTemplate())) {
				this.addKolaVerificationResults("Nodetemp for machine" + ecuList.get(i).getMachineName() + " is correct\n");
				this.addKolaVerificationResults("Kola - SE-Tool || " + nodetempKola + " - " + ecuList.get(i).getNodeTemplate() + "\n");
				this.addKolaVerificationResults("------------------------------------------------------\n");
				

			}
			else {
				this.addKolaVerificationResults("Nodetemp for machine" + ecuList.get(i).getMachineName() + " is not valid\n");
				this.addKolaVerificationResults("Kola - SE-Tool || " + nodetempKola + " - " + ecuList.get(i).getNodeTemplate() + "\n");
				this.addKolaVerificationResults("------------------------------------------------------\n");
				this.addVerificationFailureResult(1);
			}
		}
		
	}
	
	/**
	 * Function that prints what ECU that was not included in the excel-file.
	 * 
	 */
	private void printECUStatus() {
		this.addKolaVerificationResults("------------------------------------------------------\n");
		printHMIMStatus();
		printVOneStatus();
		printVTwoStatus();
		this.addKolaVerificationResults("------------------------------------------------------\n");
	}
	
	
	private void printHMIMStatus() {
		if(!this.isHMIMLocated()) {
			this.addKolaVerificationResults("HMIM NOT INCLUDED OR WRONG\n");
			this.addVerificationFailureResult(1);
		}
		
	}
	
	
	private void printVOneStatus() {
		if(!this.isVOneLocated()) {
			this.addKolaVerificationResults("V ONE NOT INCLUDED OR WRONG\n");
			this.addVerificationFailureResult(1);
		}

	}

	
	private void printVTwoStatus() {
		if(!this.isVTwoLocated()) {
			this.addKolaVerificationResults("V TWO NOT INCLUDED OR WRONG\n");
			this.addVerificationFailureResult(1);
		}
	}

	/**
	 * Function that runs setups
	 * 
	 * @param hmimEcu
	 * @param vOneEcu
	 * @param vTwoEcu
	 */
	private void variableSetup(HMIM hmimEcu, VOneECU vOneEcu, VTwoECU vTwoEcu) {
	
		this.setHmimCatalog(hmimEcu);
		this.setvOneCatalog(vOneEcu);
		this.setvTwoCatalog(vTwoEcu);
		
		
		this.setHMIMLocated(false);
		this.setVOneLocated(false);
		this.setVTwoLocated(false);
		
		this.setExtracedCellValue("");
		
		this.setKolaNumberVerified(false);
		
		this.kolaVerificationResult = new StringBuilder();
		this.setKolaFaliureCount(0);
		
	}

	private void addKolaVerificationResults(String results) {
		
		this.kolaVerificationResult.append(results);
		
	}
	
	public String getVerificationResult() {
		return this.kolaVerificationResult.toString();
	}
	
	public int getVerificationFailureResult() {
		return this.getKolaFaliureCount();
	}
	
	private void addVerificationFailureResult(int count) {
		int temp = (this.getKolaFaliureCount() + count);
		
		this.setKolaFaliureCount(temp);
	}
	
	
	private String kolaToolHeader() {
		
		
		String headerStr =  "\n---------------------------------------------------------- \n" +
						    "|          *******  KOLA VERIFICATION  *******           | \n" +
							"---------------------------------------------------------- \n\n";
		
		return headerStr;
	}
	
//===========================//
// Get, set and constructor  //
//===========================//
	
	
	public Kola() {
		
	}
	
	private HMIM getHmimCatalog() {
		return hmimCatalog;
	}

	private void setHmimCatalog(HMIM hmimCatalog) {
		this.hmimCatalog = hmimCatalog;
	}

	private VOneECU getvOneCatalog() {
		return vOneCatalog;
	}

	private void setvOneCatalog(VOneECU vOneCatalog) {
		this.vOneCatalog = vOneCatalog;
	}

	private VTwoECU getvTwoCatalog() {
		return vTwoCatalog;
	}

	private void setvTwoCatalog(VTwoECU vTwoCatalog) {
		this.vTwoCatalog = vTwoCatalog;
	}

	private boolean isHMIMLocated() {
		return HMIMLocated;
	}

	private void setHMIMLocated(boolean hMIMLocated) {
		HMIMLocated = hMIMLocated;
	}


	private boolean isVOneLocated() {
		return VOneLocated;
	}


	private void setVOneLocated(boolean vOneLocated) {
		VOneLocated = vOneLocated;
	}


	private boolean isVTwoLocated() {
		return VTwoLocated;
	}


	private void setVTwoLocated(boolean vTwoLocated) {
		VTwoLocated = vTwoLocated;
	}


	private String getExtracedCellValue() {
		return extracedCellValue;
	}


	private void setExtracedCellValue(String extracedCellValue) {
		this.extracedCellValue = extracedCellValue;
	}


	private boolean isKolaNumberVerified() {
		return kolaNumberVerified;
	}


	private void setKolaNumberVerified(boolean kolaNumberVeified) {
		this.kolaNumberVerified = kolaNumberVeified;
	}


	private int getKolaFaliureCount() {
		return kolaFaliureCount;
	}


	private void setKolaFaliureCount(int faliureCount) {
		this.kolaFaliureCount = faliureCount;
	}

	
}//End bracket
