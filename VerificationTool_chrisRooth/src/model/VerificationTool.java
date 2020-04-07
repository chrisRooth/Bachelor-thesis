package model;

import java.io.File;
import java.util.ArrayList;

import ECU.*;
import Applications.*;
import machines.Machine;

/**
 * Model class for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class VerificationTool {
	
	private HMIM hmim;
	private VOneECU vecu;
	private VTwoECU v2ecu;
	
	private String ESWDelivery;
	
	private StringBuilder verificationResult;
	private int verificationFailures;
	
	private boolean printSeToolResult;
	
	public void runVerificationTool(File SEToolPath, File roadmapPath, File configuratorPath, File KolaPath) {
	
		runSEToolVerification(SEToolPath);
		System.out.println("Se tool klar");
		runRoadmapVerification(roadmapPath); 
		System.out.println("Roadmap klar");
		runConfiguratorVerification(configuratorPath);
		System.out.println("Configurator klar");
		runKolaVerification(KolaPath);
		System.out.println("Kola klar");
	}
	
	private void runSEToolVerification(File SEToolPath) {
		
		SETool setool = new SETool();
		setool.readSEToolExcelFile(SEToolPath, this.getHmim(), this.getVecu(), this.getV2ecu());
		
		this.setHmim(setool.getHmimECU());
		this.setVecu(setool.getvOneECU());
		this.setV2ecu(setool.getvTwoECU());
		
		this.setESWDelivery(setool.getESWDelivery());
		
		if(this.isPrintSeToolResult()) {
			this.setVerificationResult(setool.getVerificationResult());
			this.addVerificationFailures(setool.getVerificationFailureResult());
		}
	}
	
	private void runRoadmapVerification(File roadmapPath) {
		
		Roadmap roadmap = new Roadmap();
		roadmap.readRoadmapExcelFile(roadmapPath, this.getHmim(), this.getVecu(), this.getV2ecu(), this.getESWDelivery());
		
		
		this.setVerificationResult(roadmap.getVerificationResult());
		this.addVerificationFailures(roadmap.getVerificationFailureResult());
	}
	
	private void runConfiguratorVerification(File configuratorPath) {
		
		Configurator configurator = new Configurator();
		configurator.readConfiguratorExcelFile(configuratorPath, this.getHmim(), this.getVecu(), this.getV2ecu());
		
		
		this.setVerificationResult(configurator.getVerificationResult());
		this.addVerificationFailures(configurator.getVerificationFailureResult());
	}
	
	private void runKolaVerification(File KolaPath) {
		
		Kola kola = new Kola();
		kola.readKolaExcelFile(KolaPath, this.getHmim(), this.getVecu(), this.getV2ecu());
		
		
		this.setVerificationResult(kola.getVerificationResult());
		this.addVerificationFailures(kola.getVerificationFailureResult());
	}
	
	/**
	 * Function that creates five machines and adds them 
	 * to the different ECUs
	 */
	private  ArrayList<Machine> addNewMachinesToECUs() {
		
		ArrayList<Machine> temp = new ArrayList<Machine>();
		
		Machine A25 = new Machine();
		A25.setMachineName("A25");
		Machine A30 = new Machine();
		A30.setMachineName("A30");
		Machine A35 = new Machine();
		A35.setMachineName("A35");
		Machine A40 = new Machine();
		A40.setMachineName("A40");
		Machine A45 = new Machine();
		A45.setMachineName("A45");
		Machine A60 = new Machine();
		A60.setMachineName("A60");
		
		temp.add(A25);
		temp.add(A30);
		temp.add(A35);
		temp.add(A40);
		temp.add(A45);
		temp.add(A60);
		
		return temp;
		
	}
	
	public void addVerificationFailures(int count) {
		
		int temp =  (this.getVerificationFailures() + count);
		
		this.setVerificationFailures(temp);
	}
	
	public void resetVerificationString() {
		
		this.verificationResult.setLength(0);
	}
	
//================================//
//    Get, set and constructor    //
//================================//
	
	public VerificationTool() { 
		
		this.setHmim(new HMIM());
		this.setVecu(new VOneECU());
		this.setV2ecu(new VTwoECU());
		this.setESWDelivery("");
		
		this.getHmim().setMachinePark(addNewMachinesToECUs());
		this.getVecu().setMachinePark(addNewMachinesToECUs());
		this.getV2ecu().setMachinePark(addNewMachinesToECUs());
		
		this.verificationResult = new StringBuilder();
		this.setVerificationFailures(0);
		
		this.setPrintSeToolResult(true);
	}

	private HMIM getHmim() {
		return hmim;
	}

	private void setHmim(HMIM hmim) {
		this.hmim = hmim;
	}

	private VOneECU getVecu() {
		return vecu;
	}
	
	private void setVecu(VOneECU vecu) {
		this.vecu = vecu;
	}
	
	private VTwoECU getV2ecu() {
		return v2ecu;
	}
	
	private void setV2ecu(VTwoECU v2ecu) {
		this.v2ecu = v2ecu;
	}

	private String getESWDelivery() {
		return ESWDelivery;
	}

	private void setESWDelivery(String eSWDelivery) {
		ESWDelivery = eSWDelivery;
	}

	public String getVerificationResult() {
		return this.verificationResult.toString();
	}

	private void setVerificationResult(String verifiResult) {
		
		this.verificationResult.append(verifiResult);
		
	}

	public int getVerificationFailures() {
		return verificationFailures;
	}

	private void setVerificationFailures(int verificationFailures) {
		this.verificationFailures = verificationFailures;
	}

	public boolean isPrintSeToolResult() {
		return printSeToolResult;
	}

	public void setPrintSeToolResult(boolean printSeToolResult) {
		this.printSeToolResult = printSeToolResult;
	}
	
} //End
