package com.jkukard.expensetrackercsv;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ExpenseTrackerCsvMain {
	
	private DateFormat df = new SimpleDateFormat("dd MMM yyyy");
	
	public static void main (String [] args) {
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


}
