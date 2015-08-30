package com.jkukard.expensetrackercsv;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class FxmlController implements Initializable {
	
	
	@FXML private Text accountFolder;
	
    @FXML protected void changeAccountAction(ActionEvent event) {
    	DirectoryChooser chooser = new DirectoryChooser();
    	chooser.setTitle("Choose Account Folder");
    	Stage stage = (Stage)accountFolder.getScene().getWindow();
    	File file = chooser.showDialog(stage);
        if (file != null) {
        	accountFolder.setText(file.getAbsolutePath());
        }
    }
    
    @FXML protected void goAction(ActionEvent event) throws IOException {
    	CsvTranHist.getInstance().loadTranHistFiles(false);
    }

	@Override
	public void initialize(URL url, ResourceBundle res) {
		// TODO Auto-generated method stub
		
	}
}
