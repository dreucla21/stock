package real_time;

import historical.Main.TransactionType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import data.AlgorithmConfig;
import data.AlgorithmConfig.AlgorithmType;
import data.AlgorithmConfig.AverageType;
import data.Data;


public class RealTime {

	
	public static void main(String[] args)
	{
		String stock_ticker = "NFLX";
		
		AlgorithmConfig a1 = new AlgorithmConfig(AlgorithmType.MA, AverageType.SMA, null, -1, 10, 30);
		AlgorithmConfig a2 = new AlgorithmConfig(AlgorithmType.MACD, AverageType.SMA, AverageType.SMA, 8,35,50);
		ArrayList<AlgorithmConfig> algorithms = new ArrayList<AlgorithmConfig>();
		algorithms.add(a1); algorithms.add(a2);
		
		try {
			RealTime real_time = new RealTime(stock_ticker);
			real_time.setAlgorithms(algorithms);
			real_time.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	private String stockTicker;
	private Data data;
	private ArrayList<AlgorithmConfig> algorithms;
	
	public RealTime(String stock_ticker) throws Exception
	{
		stockTicker = stock_ticker;
		data = new Data();
		data.readFromCSV(stockTicker + ".csv");
		//data.readFromURL("http://real-chart.finance.yahoo.com/table.csv?s=" + stock_ticker);
		
		algorithms = new ArrayList<AlgorithmConfig>();
	}
	
	public void setAlgorithms(ArrayList<AlgorithmConfig> as) throws Exception
	{
		this.algorithms = as;
		HashSet<Integer> average_lengths = new HashSet<Integer>();
		for (AlgorithmConfig ac : this.algorithms) 
		{
			average_lengths.add(ac.getNumDays1());
			average_lengths.add(ac.getNumDays2());
		}
		data.initializeMovingAverages(average_lengths);
	}
	
	
	public void run() throws Exception
	{
		Date today_date = Data.parseDate("2014-09-08");
		
		ArrayList<Date> dates = data.getLastNDays(today_date,2);
		
		System.out.println("Today date: " + today_date);
		System.out.println("Current stock price: " + data.getPrice(today_date));
		System.out.println();
		// For a given algorithm we're interested in:
		// 1) Get averages of interest (for MA: get 2 averages; for MACD: get MACD value)
		// 2) Knowing these averages, should we be holding stock or not?
		// 3) Use data from yesterday to determine if we triggered a buy or sell signal (use low/high values for day)
		for (AlgorithmConfig ac : algorithms) 
		{
			System.out.println("Algorithm: " + ac);
			boolean currently_hold_stock = false;
			
			for (Date d : dates)
			{
				System.out.println("DATE: " + d + ", PRICE: " + data.getPrice(d));
				
				TransactionType t = data.what_to_do(currently_hold_stock, ac, d);
				System.out.println("Transaction to perform: " + t);
				
				if (ac.getType() == AlgorithmType.MA){
					System.out.println("MA values: " + data.getMA(d, ac, ac.getNumDays1()) + " " + data.getMA(d, ac, ac.getNumDays2()));
				}
				if (ac.getType() == AlgorithmType.MACD) {
					System.out.println("MACD value: " + data.getMACD(d, ac));
				}
				currently_hold_stock = data.get_own_status(ac,d);

				System.out.println("Should we currently own the stock? " + currently_hold_stock);
				
				System.out.println();
			}
		}
		
		
		
	}
}
