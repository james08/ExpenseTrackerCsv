package com.jkukard.expensetrackercsv;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ExpenseTrackerCsvMain {
	
	private DateFormat df = new SimpleDateFormat("dd MMM yyyy");
	
	public static void main (String [] args) {
		//CsvTranHist.getInstance();
		//launch(args);
		try {
			new ExpenseTrackerCsvMain().doIt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void doIt() throws IOException {
		CsvTranHist.getInstance().loadTranHistFiles(false);
        System.exit(0);
	}

//	@Override
//	public void start(Stage stage) throws Exception {
//		Parent root = FXMLLoader.load(getClass().getResource("TestUI.fxml"));
//
//        Scene scene = new Scene(root, 300, 275);
//
//        stage.setTitle("Expense Tracker");
//        stage.setScene(scene);
//        stage.setMinHeight(100);
//        stage.setMinWidth(150);
//        stage.show();
//
//	}
	
	private void loadAccounts() {
		
	}

}
