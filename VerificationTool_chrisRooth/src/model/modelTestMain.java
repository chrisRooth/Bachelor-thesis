package model;

import java.io.File;
import java.util.ArrayList;

import machines.*;
import ECU.*;
import Applications.*;

/**
 * Test model class for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class modelTestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//Used for debugging
		File testSEToolPath = new File("C:\\Users\\A287222\\Desktop\\SETool.xlsx");
		String testConfPath = "C:\\Users\\A287222\\Desktop\\ConfiguratorV2.xlsx";
		String testKolaPath ="C:\\Users\\A287222\\Desktop\\kola_w1813.xlsx"; 
		String testRoadmapPath = "C:\\Users\\A287222\\Desktop\\ROADMAP_ART- HH_U2016.xlsx";
		File testWorkRoadmapPath = new File("C:\\Users\\A287222\\Desktop\\WorkRoadMap_ART.xlsx");
	
		
		HMIM hmimEcu = new HMIM();
		VOneECU vOneEcu = new VOneECU();
		VTwoECU vTwoEcu = new VTwoECU();
		
		hmimEcu.setMachinePark(addNewMachinesToECUs());
		vOneEcu.setMachinePark(addNewMachinesToECUs());
		vTwoEcu.setMachinePark(addNewMachinesToECUs());
		
		
		
//		//SE-TOOL dev
//		SETool testSETool = new SETool();
//		testSETool.readSEToolExcelFile(testSEToolPath, hmimEcu, vOneEcu, vTwoEcu);
//		hmimEcu.setNodeTemplate(testSETool.getHMIMNodeTemp());
//		vOneEcu.setNodeTemplate(testSETool.getVOneNodeTemp());
//		vTwoEcu.setNodeTemplate(testSETool.getVTwoNodeTemp());
////
//		System.out.println("*** HMIM ***");
//		printMachines(hmimCatalog.getMachinePark());
//		System.out.println("*** V ONE ***");
//		printMachines(vOneEcu.getMachinePark());
//		System.out.println("*** V TWO ***");
//		printMachines(vTwoEcu.getMachinePark());
//		
//		
//		//Configurator
//		Configurator testConfTool = new Configurator();
//		testConfTool.readConfiguratorExcelFile(testConfPath, hmimCatalog, vOneEcu, vTwoEcu);
//		
//		
//		//KOLA
//		Kola testKola = new Kola();
//		testKola.readKolaExcelFile(testKolaPath, hmimCatalog, vOneEcu, vTwoEcu);
//		
//		
//		//Roadmap
//		Roadmap testRoadMap = new Roadmap();
//		testRoadMap.readRoadmapExcelFile(testRoadmapPath, hmimCatalog, vOneEcu, vTwoEcu, testSETool.getESWDelivery());
		
//		//Workroadmap
//		WorkRoadmap testWorkRoadmap = new WorkRoadmap();
//		testWorkRoadmap.readWorkRoadmapExcelFile(testWorkRoadmapPath, hmimEcu, vOneEcu, vTwoEcu, "U16 4.4 D1");

		
	}
	
	/**
	 * Function that creates five machines and adds them 
	 * to the different ECUs
	 */
	private static ArrayList<Machine> addNewMachinesToECUs() {
		
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
	
	private static void printMachines(ArrayList<Machine> tempList) {
		
		System.out.println();
		for (int i = 0; i < tempList.size(); i++ ) {
			
			Machine temp = tempList.get(i);
			
			System.out.println(temp.getMachineName() + 
								" \n NTP: " + temp.getNodeTemplate() +
								" \n MSW: " + temp.getPartNrMSW() + 
								" - Dec: " + temp.getDescFileMSW() +
								" \n HW: " + temp.getPartNrHW() +
								" - Dec: " + temp.getDescFileHW() +
								" \n DST1: " + temp.getPartNrDST1() + 
								" - Dec: " + temp.getDescFileDST1() +
								" \n DST2: " + temp.getPartNrDST2() + 
								" - Dec: " + temp.getDescFileDST2() +
								" \n Down: " + temp.getPartNrDown() +
								" - Dec: " + temp.getDescFileDown()); 
		}
		
	}

}
