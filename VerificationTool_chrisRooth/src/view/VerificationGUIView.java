package view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import controller.*;
import model.VerificationTool;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * View class building the GUI for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public class VerificationGUIView extends Application{
	
	static VerificationTool verificationTool;
	static VerificationController verificationController;
	static Stage stage;
	static TextArea consoleText; 
	static Label failureLabel;
	
	TextField seToolTextField;
	TextField roadmapTextField;
	TextField configuratorTextField;
	TextField kolaTextField;
	
	CheckBox cbox;

	public static void main(String[] args) {
		
		verificationTool = new VerificationTool();
		verificationController = new VerificationController(verificationTool);
		
		launch(args);

	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub

		primaryStage.setTitle("Volvo CE, Bra�s");
		stage = primaryStage;

		//Layout
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(10);
		gridPane.setPadding(new Insets(25, 25, 25, 25));
	
		gridPane.add(this.addTitleField(), 0, 0);
		gridPane.add(this.addVboxFileChooser(), 0, 1);
		gridPane.add(this.addFailureLabel(), 1, 0);
		gridPane.add(this.addVboxConsole(), 1, 1);
		gridPane.add(this.addRunButtonLayout(), 0 , 2);
		gridPane.add(this.addSaveButtons(primaryStage), 1, 2);
	
//		gridPane.setGridLinesVisible(true);
		
		primaryStage.setScene(new Scene(gridPane, 1024, 800));
		primaryStage.show();

	}
	
	
	private VBox addVboxFileChooser() {

		VBox vBox = new VBox();

		//Buttons
		Button seToolBtn = new Button();
		seToolBtn.setText("Open SE-Tool file");
		Button roadmapBtn = new Button();
		roadmapBtn.setText("Open Roadmap file");
		Button configuratorBtn = new Button();
		configuratorBtn.setText("Open Configurator file");
		Button kolaBtn = new Button();
		kolaBtn.setText("Open Kola file");
		
		
		//TextFields
		seToolTextField = new TextField();
		textFieldSetup(seToolTextField);
		roadmapTextField = new TextField();
		textFieldSetup(roadmapTextField);
		configuratorTextField = new TextField();
		textFieldSetup(configuratorTextField);
		kolaTextField = new TextField();
		textFieldSetup(kolaTextField);

		
        //Button events
        seToolBtnEvent(seToolBtn, seToolTextField);
        roadmapBtnEvent(roadmapBtn, roadmapTextField);
        configuratorBtnEvent(configuratorBtn, configuratorTextField);
        kolaBtnEvent(kolaBtn, kolaTextField);
        
        cbox = new CheckBox();
        cbox.setText("Print SE-Tool content");
        cbox.setSelected(true);
       
        vBox.setSpacing(10);
                
		vBox.getChildren().addAll(
				cbox,
				seToolBtn,
				seToolTextField,
				roadmapBtn,
				roadmapTextField,
				configuratorBtn,
				configuratorTextField, 
				kolaBtn, 
				kolaTextField
				);

		vBox.setAlignment(Pos.CENTER_LEFT);
		
		return vBox;
	}
	
	private VBox addVboxConsole() {

		VBox vBox = new VBox();
		
		Label label = new Label(); 
		label.setText("Results");
		label.setFont(new Font("Arial", 25));
		label.setAlignment(Pos.TOP_CENTER);
		
		consoleText = new TextArea();
		consoleText.setPrefHeight(1000);
		consoleText.setPrefWidth(500);
		consoleText.setEditable(false);
		
		vBox.getChildren().addAll(label, consoleText);

		return vBox;	
	}
	

	
	private GridPane addTitleField() {
		
		GridPane pane = new GridPane();
		
		Label label = new Label(); 
		label.setText(" Volvo CE, Bra�s");
		label.setFont(new Font("Arial", 30));
		label.setAlignment(Pos.CENTER);
		
		//Image
		FileInputStream imageStream = null;
		
		try {
			imageStream = new FileInputStream("A60H100x100.jpg");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Image image = new Image(imageStream);
		
		
		pane.setPrefHeight(50);
		pane.setStyle("-fx-background-color: #FFFFFF;");
		
//		hbox.getChildren().addAll(
//				new ImageView(image),
//				label
//				);
		
		pane.add(new ImageView(image), 0, 0);
		pane.add(label, 1, 0);
		
		pane.setAlignment(Pos.CENTER);
		
//		pane.setGridLinesVisible(true);
		
		return pane;
		
	}
	
	private VBox addRunButtonLayout() {
		
		VBox vbox = new VBox();
		
		Button runBtn = new Button();
		runBtn.setText("Run verification");
		
		Button clearBtn = new Button();
		clearBtn.setText("Clear all textfields");
		clearBtn.setVisible(false);
		
		runBtnEvent(runBtn, clearBtn);
		clearBtnEvent(clearBtn, runBtn);
		
		vbox.setSpacing(10);
		vbox.getChildren().addAll(runBtn, clearBtn);
		vbox.setAlignment(Pos.CENTER);
		
		return vbox;
	}
	
	private HBox addSaveButtons(Stage primaryStage) {
		
		HBox hbox = new HBox();
	
		Button txtBtn = new Button();
		txtBtn.setText("Save as .rtf");
		
		this.txtBtnEvent(txtBtn, primaryStage);
		
		hbox.setSpacing(10);
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(txtBtn);
		
		return hbox;
	}
	
	private VBox addFailureLabel() {
		
		VBox vbox = new VBox();
		
		failureLabel = new Label();
		failureLabel.setAlignment(Pos.CENTER);
		failureLabel.setVisible(false);
		failureLabel.setFont(Font.font(18));
		
		vbox.getChildren().add(failureLabel);
		vbox.setAlignment(Pos.CENTER);
		
		return vbox;
		
	}
	
	/**
	 * Event handles for the SE-Tool button
	 * 
	 * @param btn
	 * @param textField
	 */
	private void seToolBtnEvent(Button btn, TextField textField) {
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
        	 
            @Override
            public void handle(ActionEvent event) {       
            	textField.setText(selectFile(stage, "SE-TOOL"));
            }    
        });
		
	}
	
	/**
	 * Event handles for the roadmap button
	 * 
	 * @param btn
	 * @param textField
	 */
	private void roadmapBtnEvent(Button btn, TextField textField) {
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
        	 
            @Override
            public void handle(ActionEvent event) {         
            	textField.setText(selectFile(stage, "Roadmap"));
            }
        });
		
	}
	
	/**
	 * Event handles for the configurator button
	 * 
	 * @param btn
	 * @param textField
	 */
	private void configuratorBtnEvent(Button btn, TextField textField) {
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
        	 
            @Override
            public void handle(ActionEvent event) {       
            	textField.setText(selectFile(stage, "Configurator"));
            } 
        });
		
	}
	
	/**
	 * Event handles for the kola button
	 * 
	 * @param btn
	 * @param textField
	 */
	private void kolaBtnEvent(Button btn, TextField textField) {
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
        	 
            @Override
            public void handle(ActionEvent event) {       
            	textField.setText(selectFile(stage, "Kola"));
            }    
        });
		
	}
	
	private void runBtnEvent(Button runBtn, Button clrBtn) {
		
		runBtn.setOnAction(new EventHandler<ActionEvent>() {
       	 
            @Override
            public void handle(ActionEvent event) {       
            	System.out.println("Running verification");
            	consoleText.setText("Running verification, please wait");
            	
            	if(seToolTextField.getText().equals("")) {
            		
            		consoleText.setText("No SE-Tool file is referenced");
            	}
            	else {
            		
            		if(!cbox.isSelected()) {
            			verificationController.setPrintSeToolResult(false);
            		}
            		
                	verificationController.runVerificationButtonPressed();
                	consoleText.setText(verificationController.getVerificationResult());
                	failureLabel.setText("Numbers of verification failures: " + verificationController.getVerificationFailure());
                	failureLabel.setVisible(true);
                	clrBtn.setVisible(true);
            	}
            }    
        });
		
		
	}
	
	private void clearBtnEvent(Button clrBtn, Button runBtn) {
		
		clrBtn.setOnAction(new EventHandler<ActionEvent>() {
	       	 
            @Override
            public void handle(ActionEvent event) {       
            	System.out.println("Clearing textfeilds");
            	
            	consoleText.setText("");
            	seToolTextField.setText("");
            	roadmapTextField.setText("");
            	configuratorTextField.setText("");
            	kolaTextField.setText("");
            	
            	verificationController.resetVerification();
            	
            	clrBtn.setVisible(false);
            	
            }    
        });
		
	}
	
	private void txtBtnEvent(Button btn, Stage primaryStage) {
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
       	 
			@Override
            public void handle(ActionEvent event) {   
            	
            	if(consoleText.getText().equals("") ||  consoleText.getText().equals("Nothing to store, run a verification")) {
            		consoleText.setText("Nothing to store, run a verification");
            	}
            	else {
            		System.out.println("FILE CREATED");
            		
            		saveFile(primaryStage);
            	}
            }  
            
        });
		
	}
	
	/**
	 * Function that opens the file chooser window
	 * 
	 * @param primaryStage
	 * @param ECUName
	 * @return
	 */
	private String selectFile(Stage primaryStage, String ECUName) {
		
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Resource File");
      File selectedFile = fileChooser.showOpenDialog(primaryStage);
      
      sendFileToController(selectedFile, ECUName);
      
      return selectedFile.getPath();
		
	}
	
	private void saveFile(Stage primaryStage) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Verification Results");

		File saveFile = fileChooser.showSaveDialog(primaryStage);
		System.out.println("File saved to: " + saveFile.getPath());
		
		FileOutputStream fileOutput;
		try {
			
			fileOutput = new FileOutputStream(saveFile);
			fileOutput.write(consoleText.getText().getBytes());
			
			fileOutput.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e ) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Depending on the button and the file is sent to different 
	 * functions in the controller.
	 * 
	 * @param file
	 * @param ECUName
	 */
	private void sendFileToController(File file, String ECUName) {
	
		if (ECUName.equals("SE-TOOL")) {
			verificationController.fetchSEToolExcelFile(file);
		}
		else if(ECUName.equals("Roadmap")) {
			verificationController.fetchRoadmapExcelFile(file);
		}
		else if(ECUName.equals("Configurator")) {
			verificationController.fetchConfiguratorExcelFile(file);
		}
		else if(ECUName.equals("Kola")) {
			verificationController.fetchKolaExcelFile(file);
		}
		else {
			//Pass
		}
		
	}

	/**
	 * Function that sets up the textfields that shows the file paths. 
	 * 	
	 * @param textField
	 */
	private void textFieldSetup(TextField textField) {
		
		textField.setAlignment(Pos.TOP_LEFT);
		textField.setPrefSize(400, 20);
		textField.setMaxWidth(400);
		textField.setMaxHeight(20);
		textField.setEditable(false);
		textField.setText("");

	}
	
	
}// End
