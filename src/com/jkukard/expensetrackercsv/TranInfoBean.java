package com.jkukard.expensetrackercsv;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TranInfoBean implements Comparable<TranInfoBean> {
	
	private double amount;
	private double balance;
	private Date date;
	private Calendar calendar;
	private Calendar finCal;
	private String description;
	private String category1 = "";
	private String category2 = "";
	private String match = "";
	public static DateFormat DF = new SimpleDateFormat("yyyy/MM/dd");
	private DateFormat monthYearDf = new SimpleDateFormat("MMMyyyy");
	private boolean isSourceFile = false;
	
	//CSV source file positions
	public static int datePosSrc = -1;
	public static int amtPosSrc = -1;
	public static int balPosSrc = -1;
	public static int descPosSrc = -1;
	
	//CSV output file positions
	public static int datePosOut = -1;
	public static int amtPosOut = -1;
	public static int balPosOut = -1;
	public static int descPosOut = -1;
	public static int cat1PosOut = -1;
	public static int cat2PosOut = -1;
	public static int matchPosOut = -1; //The substring used to match a category to a full description.
	
	//CSV headings
	public static final String DATE = "Date";
	public static final String AMOUNT = "Amount";
	public static final String BALANCE = "Balance";
	public static final String DESCRIPTION = "Description";
	public static final String CATEGORY1 = "Category1";
	public static final String CATEGORY2 = "Category2";
	public static final String MATCH = "Match";
	
	private int MONTH_START = 20;
	private static final String SALARY_MATCH = "FNB PAYROLSALARY";
	private static final int SALARY_DAY = 20;

    private static List<String> salaryEarlyDates = new ArrayList<>();
	
	public TranInfoBean(String [] tranLine, boolean isSourceFile) throws Exception {
		
		this.isSourceFile = isSourceFile;
		if (isSourceFile) {
			this.date = DF.parse(tranLine[datePosSrc]);
			this.calendar = Calendar.getInstance();
			calendar.setTime(date);
			this.amount = Double.valueOf(tranLine[amtPosSrc]);
			this.balance = Double.valueOf(tranLine[balPosSrc]);
			this.description = tranLine[descPosSrc].trim();
			setFinCal();
		} else {
			this.date = DF.parse(tranLine[datePosOut]);
			this.calendar = Calendar.getInstance();
			this.amount = Double.valueOf(tranLine[amtPosOut]);
			this.balance = Double.valueOf(tranLine[balPosOut]);
			this.description = tranLine[descPosOut].trim();
			this.category1 = tranLine[cat1PosOut].trim().toUpperCase();
			this.category2 = tranLine[cat2PosOut].trim().toUpperCase();
			this.match = tranLine[matchPosOut].trim();
			calendar.setTime(date);
			setFinCal();
		}
	}
	
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * Gets the MonthYear string of the date from
	 * the financial calendar.
	 * @return
	 */
	public String getFinMonthYearString() {
		return monthYearDf.format(finCal.getTime());
	}
    public String getFinDateString(){
        return DF.format(finCal.getTime());
    }
	public String getCategory1() {
		return category1;
	}
	public void setCategory1(String category1) {
		this.category1 = category1;;
	}
	public String getCategory2() {
		return category2;
	}
	public void setCategory2(String category2) {
		this.category2 = category2;
	}
	public String getMatch() {
		return match;
	}
	public void setMatch(String match) {
		this.match = match;
	}

	private void setFinCal() {
		finCal = Calendar.getInstance();
		finCal.setTime(date);
		if ((MONTH_START > 15 && calendar.get(Calendar.DAY_OF_MONTH) >= MONTH_START)
						) {
			finCal.add(Calendar.MONTH, 1);
		}
		if (description.contains(SALARY_MATCH)
				&& calendar.get(calendar.YEAR) == finCal.get(calendar.YEAR)
                && calendar.get(calendar.MONTH) == finCal.get(calendar.MONTH)
				&& calendar.get(Calendar.DAY_OF_MONTH) < SALARY_DAY) {
            salaryEarlyDates.add(getFinDateString());
			moveTranToNextFinancialMonth(this);
		}
	}

    public Calendar getFinCal() {
        return finCal;
    }

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof TranInfoBean) {
			TranInfoBean tib = (TranInfoBean)obj;
			if (tib.getAmount() == amount &&
					tib.getBalance() == balance &&
					tib.getDate().compareTo(date) == 0 &&
					tib.getDescription().equalsIgnoreCase(description)) {
				isEqual = true;
			}
		}
		return isEqual;
	}

	@Override
	public int compareTo(TranInfoBean bean) {
		return bean.getDate().compareTo(date);
	}

	@Override
	public String toString() {
		return date + " " + amount + " " + description;
	}

    /* public static Map getSalaryDates() {
        return salaryDates;
    } */

    /**
     * Saves list of all salary dates. Used to change start of month date to salary date.
     * This helps to give realistic monthly totals when large payments are made on payday.
     * @param transactions
     */
	public static void setSalaryDates(List<TranInfoBean> transactions) {
        Calendar cal = Calendar.getInstance();
        for (TranInfoBean tran : transactions) {
            if (salaryEarlyDates.contains(tran.getFinDateString())) {
                moveTranToNextFinancialMonth(tran);
            }
        }
    }

    private static void moveTranToNextFinancialMonth(TranInfoBean tran) {
        tran.getFinCal().add(Calendar.MONTH, 1);
    }
	
}
