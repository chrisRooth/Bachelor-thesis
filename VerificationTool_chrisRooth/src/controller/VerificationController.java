package controller;

import java.io.File;

import model.*;
import view.*;


/**
 * Controller class for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class VerificationController {

	private VerificationTool verificationTool;
	
	private File se_toolFile;
	private File roadmapFile; 
	private File configuratorFile;
	private File kolaFile;

	private String verificationResult;
	private int verificationFailure;
	
	private boolean printSeToolResult;
	
	
	public void fetchSEToolExcelFile(File fetchedSEToolsFile) {
	
		if (fetchedSEToolsFile.getPath().equals("")) {
			System.out.println("No SE-tool is entered!");
		} 
		else {
			this.setSe_toolFile(fetchedSEToolsFile);
			System.out.println("SE Tool File entered");
		}
	}
	
	
	public void fetchRoadmapExcelFile(File fetchedRodmapFile) {
		
		if (fetchedRodmapFile.getPath().equals("")) {
			System.out.println("No Roadmap is entered!");
		} 
		else {
			this.setRoadmapFile(fetchedRodmapFile);
			System.out.println("Roadmap File entered");
		}
		
	}
	
	
	public void fetchConfiguratorExcelFile(File fetchedConfiguratorFile) {
		
		if (fetchedConfiguratorFile.getPath().equals("")) {
			System.out.println("No Configurator is entered!" + fetchedConfiguratorFile.getPath());
		} 
		else {
			this.setConfiguratorFile(fetchedConfiguratorFile);
			System.out.println("Configurator File entered");
		}
	}
	
	
	public void fetchKolaExcelFile(File fetchedKolaFile) {
		
		if (fetchedKolaFile.getPath().equals("")) {
			System.out.println("No Kola is entered!");
		} 
		else {
			this.setKolaFile(fetchedKolaFile);
			System.out.println("Kola File entered");
		}
	}
	
	
	public void runVerificationButtonPressed() {
		
		if (!this.isPrintSeToolResult()) {
			verificationTool.setPrintSeToolResult(false);
		}
		
		this.getVerificationTool().runVerificationTool(
				this.getSe_toolFile(), 
				this.getRoadmapFile(), 										
				this.getConfiguratorFile(), 
				this.getKolaFile()
				);

		this.setVerificationResult(this.getVerificationTool().getVerificationResult());
		this.setVerificationFailure(this.getVerificationTool().getVerificationFailures());
		
	}
	
	
	public void resetVerification() {
		
		this.getVerificationTool().resetVerificationString();
		
		this.setSe_toolFile(new File(""));
		this.setRoadmapFile(new File(""));
		this.setConfiguratorFile(new File(""));
		this.setKolaFile(new File(""));
		
		this.setVerificationFailure(0);
		
	}
	

//===========================//
// Get, set and constructor  //
//===========================//
	
	public VerificationController(VerificationTool verificationTool) {
		
		this.setVerificationTool(verificationTool);

		this.setSe_toolFile(new File(""));
		this.setRoadmapFile(new File(""));
		this.setConfiguratorFile(new File(""));
		this.setKolaFile(new File(""));
		
		this.setVerificationResult("");
		this.setVerificationFailure(0);
		
		this.setPrintSeToolResult(true);
		
	}

	private VerificationTool getVerificationTool() {
		return verificationTool;
	}

	private void setVerificationTool(VerificationTool verificationTool) {
		this.verificationTool = verificationTool;
	}

	private File getSe_toolFile() {
		return se_toolFile;
	}

	private void setSe_toolFile(File se_toolFile) {
		this.se_toolFile = se_toolFile;
	}

	private File getRoadmapFile() {
		return roadmapFile;
	}

	private void setRoadmapFile(File roadmapFile) {
		this.roadmapFile = roadmapFile;
	}

	private File getConfiguratorFile() {
		return configuratorFile;
	}

	private void setConfiguratorFile(File configuratorFile) {
		this.configuratorFile = configuratorFile;
	}

	private File getKolaFile() {
		return kolaFile;
	}
	
	private void setKolaFile(File kolaFile) {
		this.kolaFile = kolaFile;
	}


	public String getVerificationResult() {
		return verificationResult;
	}


	public void setVerificationResult(String verificationResult) {
		this.verificationResult = verificationResult;
	}


	public int getVerificationFailure() {
		return verificationFailure;
	}


	public void setVerificationFailure(int verificationFailure) {
		this.verificationFailure = verificationFailure;
	}


	public boolean isPrintSeToolResult() {
		return printSeToolResult;
	}


	public void setPrintSeToolResult(boolean printSeToolResult) {
		this.printSeToolResult = printSeToolResult;
	}

	
}
