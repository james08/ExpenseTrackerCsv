package com.jkukard.expensetrackercsv;

import au.com.bytecode.opencsv.CSVReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class CsvTranHist extends Application {
	
	private List<TranInfoBean> transactions = new LinkedList<>();
	private static final String DATA_DIR = "~/.expensetrackercsv";
    private static String SOURCE_DIR = "/Users/mac/Documents/BankStatementsLanding";
	private static String OUTPUT_DIR = "/Users/mac/Documents/BankStatementsOutput";
	private static String CATEGORY_FILE1 = SOURCE_DIR + "/categories1.csv";
	private static String CATEGORY_FILE2 = SOURCE_DIR + "/categories2.csv";
	private static String MATCH_STRINGS = SOURCE_DIR + "/categoryMatchStrings.csv";
	private static String CSV_SOURCE_POSITIONS = SOURCE_DIR + "/csvSourcePositions.csv";
	private static String CSV_OUT_POSITIONS = SOURCE_DIR + "/csvOutPositions.csv";
	private static String TOTALS_FILE = OUTPUT_DIR + "/totals.csv";
	private static String SEPARATOR = ",";
	
	private static List<String> CAT1 = new LinkedList<>();
	private static List<String> CAT2 = new LinkedList<>();
	
	private static Map<CatMatchKey, String> CAT1_MATCH = new HashMap<>();
	private static Map<CatMatchKey, String> CAT2_MATCH = new HashMap<>();
	
	public static List<String> INFO = new ArrayList<>();
	public static List<String> ERRORS = new ArrayList<>();
	
	private static Map<String, Map<String, Double>> totals = new LinkedHashMap<>();
	private static String ERROR_MSG = "";
	private static String INFO_MSG = "";
	
	private static CsvTranHist instance; 
	
	private CsvTranHist() {}
	
	public static CsvTranHist getInstance() {
		if (instance == null) {
			instance = new CsvTranHist();
		}
		return instance;
	}
	

	public boolean loadTranHistFiles(boolean testingUi) throws IOException {
		if(testingUi) {
			try {
				start(new Stage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		boolean isSourceFile;
		System.out.println("Loading configuration data...");
		loadConfigData();
		
		System.out.println("Loading current data...");
		isSourceFile = false;
		File dir = new File(OUTPUT_DIR);
		for (String fileName : dir.list()) {
			if (fileName.endsWith(".csv")) {
				File file = new File(OUTPUT_DIR + "/" + fileName);
				loadTranHistFromFile(file, isSourceFile);
				file.delete();
			}
		}
		System.out.println("Finished loading current data...");
		System.out.println("Loading new data...");
		isSourceFile = true;
		dir = new File(SOURCE_DIR);
		for (String fileName : dir.list()) {
			if (fileName.endsWith(".csv")) {
				File file = new File(SOURCE_DIR + "/" + fileName);
				loadTranHistFromFile(file, isSourceFile);
			}
		}
		Collections.sort(transactions);//according to date
        TranInfoBean.setSalaryDates(transactions);
		System.out.println("Finished loading new data...");
//		for (TranInfoBean tib : transactions) {
//			System.out.println(tib.getDate() + " " + tib.getDescription());
//		}
		/* Eliminate duplicates */
		System.out.println("Creating output files...");
		Map<String, LinkedList<TranInfoBean>> outputTransactions = new LinkedHashMap<>();
		for (TranInfoBean tib : transactions) {
			if (!outputTransactions.keySet().contains(tib.getFinMonthYearString())) {
				outputTransactions.put(tib.getFinMonthYearString(), new LinkedList<>());
			}
			outputTransactions.get(tib.getFinMonthYearString()).add(tib);
		}
		for (String s : outputTransactions.keySet()) {
			File f = new File(OUTPUT_DIR + "/" + s + ".csv");
			f.createNewFile();
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			double in = 0;
			double out = 0;
			double diff = 0;
//			for (TranInfoBean tib : outFiles.get(s)) {
//				System.out.println(tib.getDate() + " " + tib.getDescription());
//			}
			for (TranInfoBean tib : outputTransactions.get(s)) {
				addToTotal(tib);
				if (tib.getAmount() < 0) {
					out = out + tib.getAmount();
				} else if (tib.getAmount() > 0) {
					in = in + tib.getAmount();
				}
				String dte = TranInfoBean.DF.format(tib.getDate());
				String amt = String.valueOf(tib.getAmount());
				String bal = String.valueOf(tib.getBalance());
				String des = tib.getDescription();
				categoriseTransaction(tib);
				bw.append(dte + SEPARATOR + amt + SEPARATOR + bal + SEPARATOR 
						+ des + SEPARATOR + tib.getCategory1() + SEPARATOR + tib.getCategory2() +
                        SEPARATOR + tib.getMatch());
				bw.newLine();
			}
			diff = in + out;
			bw.newLine();
			bw.append("In" + SEPARATOR + String.valueOf(in));
			bw.newLine();
			bw.append("Out" + SEPARATOR + String.valueOf(out));
			bw.newLine();
			bw.append("Diff" + SEPARATOR + String.valueOf(diff));
			bw.flush();
			bw.close();
			fw.close();
		}
		System.out.println("Finished creating output files...");
		System.out.println("Creating totals file(s)...");
		generateTotals();
		writeCategoryMatches();
		System.out.println("Done.");
//		printCatKeys();
		return true;
	}

    private void printCatKeys() {
		System.out.println("CAT 1 KEYS");
		CatMatchKey [] cmks = (CatMatchKey[]) CAT1_MATCH.keySet().toArray(new CatMatchKey[0]);
		Arrays.sort(cmks);
		for (CatMatchKey cmk : cmks) {
			System.out.println(cmk.getMatchKey());
		}
		System.out.println("CAT 2 KEYS");
		cmks = (CatMatchKey[]) CAT2_MATCH.keySet().toArray(new CatMatchKey[0]);
		Arrays.sort(cmks);
		for (CatMatchKey cmk : cmks) {
			System.out.println(cmk.getMatchKey());
		}
	}
	
	private void categoriseTransaction(TranInfoBean tib) {
		String description = tib.getDescription().toUpperCase();
		CatMatchKey [] cmks = CAT1_MATCH.keySet().toArray(new CatMatchKey[0]);
		Arrays.sort(cmks);
		for (CatMatchKey key : cmks) {
			String matchString = key.getMatchKey();
			if (description.equals(matchString) || description.contains(matchString)) {
				tib.setCategory1(CAT1_MATCH.get(key));
				tib.setMatch(matchString);
			}
		}
		cmks = (CatMatchKey[]) CAT2_MATCH.keySet().toArray(new CatMatchKey[0]);
		Arrays.sort(cmks);
		for (CatMatchKey key : cmks) {
			String matchString = key.getMatchKey();
			if (description.equals(matchString) || description.contains(matchString)) {
				tib.setCategory2(CAT2_MATCH.get(key));
				tib.setMatch(matchString);
			}
		}
	}

	private void loadTranHistFromFile(File file, boolean isSourceFile) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(file), ',')) {
//			System.out.println("Processing " + file.getName() + "...");
            //reader = new CSVReader(new FileReader(file), ',', '"', '\\', 7);
            String[] nextLine = null;
            List<TranInfoBean> tempTransactions = new LinkedList<>();
            while ((nextLine = reader.readNext()) != null) {
                //System.out.print("Reading line: " + nextLine);
                TranInfoBean tib = null;
                try {
                    tib = new TranInfoBean(nextLine, isSourceFile);
//                    System.out.println(tib.toString());
                    if (!isSourceFile && !tib.getMatch().isEmpty()) {
                        if (!tib.getCategory1().trim().isEmpty() && CAT1.contains(tib.getCategory1().trim())) {
                            CatMatchKey cmk1 = new CatMatchKey(tib.getMatch());
                            CAT1_MATCH.put(cmk1, tib.getCategory1());
                        } else {
                            tib.setCategory1("NONE");
                        }
                        if (!tib.getCategory2().trim().isEmpty() && CAT2.contains(tib.getCategory2().trim())) {
                            CatMatchKey cmk2 = new CatMatchKey(tib.getMatch());
                            CAT2_MATCH.put(cmk2, tib.getCategory2());
                        } else {
                            tib.setCategory2("NONE");
                        }
                    } else {
                        tib.setCategory1("NONE");
                        tib.setCategory2("NONE");
                    }
                } catch (Exception e) {
//					e.printStackTrace();
                    continue;//We don't expect all files to contain transactional data, so just ignore them.
                }
                if (!transactions.contains(tib)) {
                    tempTransactions.add(tib);
                    //System.out.println("----Added");
                } else {
                    //System.out.println();
                }

            }
            if (isSourceFile && tempTransactions.size() > 0) {
                System.out.println("Found new data in: " + file.getName());
            }
            transactions.addAll(tempTransactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
//	private void loadCategories() throws IOException {
//		System.out.println("Loading category file: " + CATEGORY_FILE);
//		categories = new HashMap<>();
//		CSVReader reader = null;
//		try {
//			reader = new CSVReader(new FileReader(CATEGORY_FILE), ',');
//			String []  nextLine = null;
//			while ((nextLine = reader.readNext()) != null) {
//				try {
//					categories.put(nextLine[0], nextLine[1]);
//				} catch (Exception e) {
//					e.printStackTrace();
//					continue;
//				}
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			reader.close();
//		}
//	}
	
//	private void writeMatchStrings() throws IOException {
//		File f = new File(CATEGORY_FILE);
//		f.delete();
//		f.createNewFile();
//		FileWriter fw = new FileWriter(f);
//		BufferedWriter bw = new BufferedWriter(fw);
//		for (String s : categories.keySet()) {
//			bw.append(s + SEPARATOR + categories.get(s));
//			bw.newLine();
//		}
//		bw.flush();
//		bw.close();
//		fw.close();
//	}
	
//	private String getCategoryFromUser(String description) {
//		String id = getTranIdentifierFromUser(description);
//		if (id == null || id.trim().equals("")) {
//			return "NONE";
//		}
//		Set<String> possibilities = new HashSet<>();
//		for (String s : categories.keySet()) {
//			possibilities.add(categories.get(s));
//		}
//		String cat = (String)JOptionPane.showInputDialog(
//		                    this,
//		                    "Select a category for " + id,
//		                    "Category",
//		                    JOptionPane.PLAIN_MESSAGE,
//		                    null,
//		                    possibilities.toArray(),
//		                    "Please select");
//		
//		if (cat == null || cat.trim().equals("")) {
//			cat = "NONE";
//		} else {
//			categories.put(id, cat);
//		}
//
//		return cat;
//	}
	
	private String getTranIdentifierFromUser(String description) {
		String id = "";
		id = (String)JOptionPane.showInputDialog(this, "Type in the portion of this description as an identifier: \n"
				+ description);
		if (id != null) {
			if (id.trim().equals("") || !description.contains(id)) {
				getTranIdentifierFromUser(description);
			}
		}
		return id;
	}
	
	/**
	 * Loads data from config files.
	 */
	private void loadConfigData() {
        Properties configProps = new Properties();
        FileInputStream fis = null;
        try {
        fis = new FileInputStream("/Users/mac/expenseTrackerCsv.config");
        configProps.load(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVReader reader = null;
		List<String> headings = new ArrayList<>();
		try {
			/* CSV Positions */
			reader = new CSVReader(new FileReader(CSV_SOURCE_POSITIONS), ',');
			String []  nextLine = null;
			int count = 0;
			while ((nextLine = reader.readNext()) != null) {
				try {
					for (String heading : nextLine) {
						if (heading.equalsIgnoreCase(TranInfoBean.DATE)) {
							TranInfoBean.datePosSrc = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.AMOUNT)) {
							TranInfoBean.amtPosSrc = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.BALANCE)) {
							TranInfoBean.balPosSrc = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.DESCRIPTION)) {
							TranInfoBean.descPosSrc = count;
						}
						count++;
					}
				} catch (Exception e) {
					e.printStackTrace();
                }
			}
			reader.close();
			reader = new CSVReader(new FileReader(CSV_OUT_POSITIONS), ',');
			nextLine = null;
			count = 0;
			while ((nextLine = reader.readNext()) != null) {
				try {
					for (String heading : nextLine) {
						if (heading.equalsIgnoreCase(TranInfoBean.DATE)) {
							TranInfoBean.datePosOut = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.AMOUNT)) {
							TranInfoBean.amtPosOut = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.BALANCE)) {
							TranInfoBean.balPosOut = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.DESCRIPTION)) {
							TranInfoBean.descPosOut = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.CATEGORY1)) {
							TranInfoBean.cat1PosOut = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.CATEGORY2)) {
							TranInfoBean.cat2PosOut = count;
						}
						else if (heading.equalsIgnoreCase(TranInfoBean.MATCH)) {
							TranInfoBean.matchPosOut = count;
						}
						count++;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
                }
			}
			/* Categories */
			reader.close();
			nextLine = null;
			count = 0;
			try {
				reader = new CSVReader(new FileReader(CATEGORY_FILE1), ',');
				nextLine = null;
				if ((nextLine = reader.readNext()) != null) {
					for (String s : nextLine) {
						CAT1.add(s.toUpperCase());
					}
				}
				reader.close();
				reader = new CSVReader(new FileReader(CATEGORY_FILE2), ',');
				nextLine = null;
				if ((nextLine = reader.readNext()) != null) {
					for (String s : nextLine) {
						CAT2.add(s.toUpperCase());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadCategoryMatches();
		
	}
	
	void addToTotal(TranInfoBean tib) {
		if (!totals.containsKey(tib.getFinMonthYearString())) {
			Map<String, Double> inner = new CategoryTotalsMap<>();
			for (String cat1 : CAT1) {
				inner.put(cat1, new Double(0));
			}
			for (String cat2 : CAT2) {
				inner.put(cat2, new Double(0));
			}
			totals.put(tib.getFinMonthYearString(), inner);
		}
        if (tib.getAmount() >= 0) {
            totals.get(tib.getFinMonthYearString()).put("IN", tib.getAmount());
        } else {
            totals.get(tib.getFinMonthYearString()).put("OUT", tib.getAmount());
        }
        if (tib.getCategory1().equalsIgnoreCase("NONE")
                && tib.getCategory2().equalsIgnoreCase("NONE")) {
		    totals.get(tib.getFinMonthYearString()).put("NONE", tib.getAmount());
        } else {
            if (!tib.getCategory1().equalsIgnoreCase("NONE")) {
                totals.get(tib.getFinMonthYearString()).put(tib.getCategory1(), tib.getAmount());
            }
            if (!tib.getCategory2().equalsIgnoreCase("NONE")) {
                totals.get(tib.getFinMonthYearString()).put(tib.getCategory2(), tib.getAmount());
            }
        }
	}
	
	public static boolean addInfo(String error) {
		return true;
	}
	public static boolean addError(String info) {
		return true;
	}
	
	private void generateTotals() {
		File f = new File(TOTALS_FILE);
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			boolean printHeader = true;
			f.createNewFile();
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			Set<String> months = totals.keySet();
			for (String month : months) {
				Map<String, Double> catTotals = totals.get(month);
				Set<String> catSet = catTotals.keySet();
				if (printHeader) {
					for (String cat : catSet) {
						bw.append(SEPARATOR + cat);
					}
					bw.newLine();
					printHeader = false;
				}
				bw.append(month);
				for (String cat : catSet) {
                    BigDecimal amt = new BigDecimal(catTotals.get(cat));
                    amt = amt.setScale(2, RoundingMode.CEILING);
					bw.append(SEPARATOR + amt);
				}
				bw.newLine();
			}
			bw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeCategoryMatches() {
		File f = new File(MATCH_STRINGS);
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			f.createNewFile();
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			for (CatMatchKey cmk : CAT1_MATCH.keySet()) {
				String match = cmk.getMatchKey();
				String cat1 = CAT1_MATCH.get(cmk);
				String cat2 = CAT2_MATCH.get(cmk);
				bw.append(match + SEPARATOR + cat1 + SEPARATOR + cat2);
				bw.newLine();
			}
			bw.flush();
			bw.close();
			fw.close();
			
//			f = new File(MATCH_STRINGS_CAT2);
//			f.createNewFile();
//			fw = new FileWriter(f);
//			bw = new BufferedWriter(fw);
//			for (CatMatchKey cmk : CAT2_MATCH.keySet()) {
//				String match = cmk.getMatchKey();
//				String cat2 = CAT2_MATCH.get(cmk);
//				bw.append(match + SEPARATOR + cat2);
//				bw.newLine();
//			}
//			bw.flush();
//			bw.close();
//			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadCategoryMatches() {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(MATCH_STRINGS), ',');
			String [] nextLine = null;
			while ((nextLine = reader.readNext()) != null) {
				CatMatchKey cmk = new CatMatchKey(nextLine[0]);
				String cat1 = nextLine[1];
				String cat2 = nextLine[2];
				CAT1_MATCH.put(cmk, cat1);
				CAT2_MATCH.put(cmk, cat2);
			}
			reader.close();
//			reader = new CSVReader(new FileReader(MATCH_STRINGS_CAT2), ',');
//			nextLine = null;
//			while ((nextLine = reader.readNext()) != null) {
//				CatMatchKey cmk = new CatMatchKey(nextLine[0]);
//				String cat = nextLine[1];
//				CAT2_MATCH.put(cmk, cat);
//			}
//			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("TestUI.fxml"));
	    
        Scene scene = new Scene(root, 300, 275);
    
        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
		
	}

	public void loadAccounts() {
		CSVReader reader = null;
		try {
            String ACCOUNTS_FILE = DATA_DIR + "/accounts.csv";
            File accFile = new File(ACCOUNTS_FILE);
			if (!accFile.exists()) {
				appendInfoMsg("Creating accounts file...");
				accFile.createNewFile();
			} else {
				reader = new CSVReader(new FileReader(ACCOUNTS_FILE), ',');
			}
		} catch (IOException e) {
			appendErrorMsg(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void checkDataDir() {
		File accFile = new File(DATA_DIR);
		if (!accFile.exists()) {
			appendInfoMsg("Creating data dir...");
			accFile.mkdir();
		}
	}
	
	void appendInfoMsg(String msg) {
		INFO_MSG += msg;
		INFO_MSG += "<br>";
	}
	void appendErrorMsg(String msg) {
		ERROR_MSG += msg;
		ERROR_MSG += "<br>";
	}
	
}
