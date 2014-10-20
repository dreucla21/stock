package stock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import stock.AlgorithmConfig.AlgorithmType;
import stock.AlgorithmConfig.AverageType;

public class Main 
{
	public static Random r;
	// TODO:
	// - Learn over a different time range than test time range
	// - Thread the "run_once" call
	

	// QUERY FOR REALTIME QUOTES: 
	//https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quote%20where%20symbol%20in%20(%22RAS%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=


	public static void main(String[] args)
	{
		// Initialize random # generator
		r = new Random();
		r.setSeed(1);

		// Parameters
		String stock_ticker = "NOV";
		int moving_average_min = 1;
		int moving_average_max = 300;
		int moving_average_inc = 1;
		
		try {
			Main main = new Main(stock_ticker, moving_average_min, moving_average_max,
					moving_average_inc);
			main.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	
	public Main(String stock_ticker, int ma_min, int ma_max, int ma_inc) throws Exception
	{
		data = new Data();
		//data.readFromCSV("ras.csv");
		data.readFromURL("http://real-chart.finance.yahoo.com/table.csv?s=" + stock_ticker);
		data.initializeMovingAverages(ma_min,ma_max,ma_inc);
	}
	
	private Data data;
	
	public enum TransactionType
	{
		BUY,
		SELL,
		HOLD
	}


	public void run() throws Exception
	{
		
		
		/*AlgorithmConfig algorithm_config = new AlgorithmConfig(AlgorithmType.MACD,12,26);
		algorithm_config.setMovingAverageType(AverageType.EMA);
		algorithm_config.setMACDSignalNumDays(2);
		RunConfig run_config = new RunConfig(algorithm_config,Data.parseDate("05/24/2011"),Data.parseDate("10/17/2014"));
		RunResult run_result = this.run_once(run_config); 
		System.out.println(run_result.getOverallProfit());
		
		AlgorithmConfig algorithm_config2 = new AlgorithmConfig(AlgorithmType.MACD,12,26);
		algorithm_config2.setMovingAverageType(AverageType.EMA);
		algorithm_config2.setMACDSignalNumDays(1);
		RunConfig run_config2 = new RunConfig(algorithm_config2,Data.parseDate("05/24/2011"),Data.parseDate("10/17/2014"));
		RunResult run_result2 = this.run_once(run_config2); 
		System.out.println(run_result2.getOverallProfit());
		//all_results.put(ma_config, Arrays.asList(new RunResult[]{run_result})); */
		
		
		Calendar cal = Calendar.getInstance();
	   cal.setTime(data.getMinDate());
	    int min_year = cal.get(Calendar.YEAR);
	    cal = Calendar.getInstance();
	    cal.setTime(data.getMaxDate());
	    int max_year = cal.get(Calendar.YEAR);
		
		ArrayList<AlgorithmResults> top_results = learn(min_year,max_year,100);
		
		for (AlgorithmResults mar : top_results)
		{
			System.out.println(mar.getAlgorithmConfig().getType() + " " 
					+ " mat=" + mar.getAlgorithmConfig().getMovingAverageType() 
					+ " st=" + mar.getAlgorithmConfig().getSignalType()
					+ "(" + mar.getAlgorithmConfig().getMACDSignalNumDays() + ") " 
					+ mar.getAlgorithmConfig().getNumDaysBuy() 
					+ " " + mar.getAlgorithmConfig().getNumDaysSell() + " " + mar.getTotalScore());
			//for (RunResult rr: mar.getRunResults()) {
			//	System.out.println(rr.getStartDate() + " " + rr.getEndDate() + " " 
			//			+ rr.getOverallProfit() + " " + (rr.getEndPrice()-rr.getStartPrice()) 
		//				+ " " + rr.getNumTransactions());
			//}
			//System.out.println(); 
			
			// TEST!!
			//AlgorithmConfig ma_config = new AlgorithmConfig(mar.getAlgorithmType(),mar.getNumDaysBuy(),mar.getNumDaysSell());
			AlgorithmConfig ac = mar.getAlgorithmConfig();
			RunConfig run_config = new RunConfig(ac,Data.parseDate("01/01/" + (max_year-3)),
					Data.parseDate("01/01/" + (max_year+1)));
			RunResult rr = this.run_once(run_config);
			System.out.println("TEST RESULT!!!");
			System.out.println(rr.getStartDate() + " " + rr.getFirstBuyDate() + " " + rr.getEndDate() + ": overall_profit=" 
					+ rr.getOverallProfit() + " (sp=" + rr.getStartPrice() + ",fbp=" + rr.getFirstBuyPrice() +
					",ep=" + rr.getEndPrice() + ") num_trans=" + " " + rr.getNumTransactions());
			System.out.println();

		}
	
		
		

		
	}

	/**
	 * Return top 10 results
	 * @param start_year
	 * @param end_year
	 * @param num_simulations
	 */
	public ArrayList<AlgorithmResults> learn(int start_year, int end_year, int num_simulations)
	{
		ArrayList<Date[]> dates = new ArrayList<Date[]>();
		HashMap<AlgorithmConfig,List<RunResult>> all_results = new HashMap<AlgorithmConfig,List<RunResult>>();
		GregorianCalendar gc = new GregorianCalendar();
		
		for (int y=0; y < num_simulations; y++) {
			int year = randBetween(start_year,end_year);
			gc.set(gc.YEAR, year);
			int dayOfYear = randBetween(1,gc.getActualMaximum(gc.DAY_OF_YEAR));
			gc.set(gc.DAY_OF_YEAR, dayOfYear);
			Date d1 = new Date();
			d1.setTime(gc.getTimeInMillis());
			year = randBetween(start_year,end_year);
			gc.set(gc.YEAR, year);
			dayOfYear = randBetween(1,gc.getActualMaximum(gc.DAY_OF_YEAR));
			gc.set(gc.DAY_OF_YEAR, dayOfYear);
			Date d2 = new Date();
			d2.setTime(gc.getTimeInMillis());
			Date sd = d1;
			Date ed = d2;
			if (d1.compareTo(d2) > 0) {
				sd = d2;
				ed = d1;
			}
			System.out.println(sd + " " + ed);
			dates.add(new Date[]{sd,ed});
		}  
		for (int i : new Integer[]{5,10,15,30,50,100,150,200,250,300}) {
			for (int j : new Integer[]{5,10,15,30,50,100,150,200,250,300}) {
				AlgorithmConfig mac = new AlgorithmConfig(AlgorithmType.MA, j,i);
				mac.setMovingAverageType(AverageType.SMA);
				AlgorithmConfig mac2 = new AlgorithmConfig(AlgorithmType.MA,j,i);
				mac2.setMovingAverageType(AverageType.EMA);
				
				ArrayList<RunResult> run_results = new ArrayList<RunResult>();
				ArrayList<RunResult> run_results2 = new ArrayList<RunResult>();
				for (Date[] s : dates) {
					RunConfig rc = new RunConfig(mac,s[0],s[1]);
					run_results.add(this.run_once(rc));
					
					RunConfig rc2 = new RunConfig(mac2,s[0],s[1]);
					run_results2.add(this.run_once(rc2));
				}
				all_results.put(mac,run_results);
				all_results.put(mac2,run_results2);
			}
		} 
		for (int i : new Integer[]{5,10,12,15,18,20,26,30,35,40,50}) {
			for (int j : new Integer[]{5,10,12,15,18,20,26,30,35,40,50}) {
				if (j == i) continue;
				AlgorithmConfig mac = new AlgorithmConfig(AlgorithmType.MACD,j,i);		
				mac.setMovingAverageType(AverageType.EMA);
				AlgorithmConfig mac2 = new AlgorithmConfig(AlgorithmType.MACD,j,i);
				mac2.setMovingAverageType(AverageType.SMA);
				AlgorithmConfig mac3 = new AlgorithmConfig(AlgorithmType.MACD,j,i);
				mac3.setMovingAverageType(AverageType.EMA);
				mac3.setSignalType(AverageType.EMA);
				mac3.setMACDSignalNumDays(9);
				
				ArrayList<RunResult> run_results = new ArrayList<RunResult>();
				ArrayList<RunResult> run_results2 = new ArrayList<RunResult>();
				ArrayList<RunResult> run_results3 = new ArrayList<RunResult>();
				for (Date[] s : dates) {
					RunConfig rc = new RunConfig(mac,s[0],s[1]);
					run_results.add(this.run_once(rc));
					
					RunConfig rc2 = new RunConfig(mac2,s[0],s[1]);
					run_results2.add(this.run_once(rc2));
					
					RunConfig rc3 = new RunConfig(mac3,s[0],s[1]);
					run_results3.add(this.run_once(rc3));
				}
				all_results.put(mac,run_results);
				all_results.put(mac2,run_results2);
				all_results.put(mac3,run_results3);
			}
		}
	
		
		
		List<AlgorithmResults> ma_results = new ArrayList<AlgorithmResults>();

		for (AlgorithmConfig mac : all_results.keySet())
		{
			AlgorithmResults mar = new AlgorithmResults(mac);
			for (RunResult rr : all_results.get(mac)) {
				//	System.out.println(rr.run_config.start_date + " " + " " + rr.run_config.getNumDaysBuy() + "/" + rr.run_config.getNumDaysSell()
				//			+ ": profit = " + rr.overall_profit + ", long term hold profit = " + (rr.end_price-rr.start_price));
				//	System.out.println("# of transactions: " + rr.getNumTransactions());
				//System.out.println(rr.getScore());
				mar.addRunResult(rr);
			}
			ma_results.add(mar);
		}

		Collections.sort(ma_results,new Comparator<AlgorithmResults>() {
			@Override
			public int compare(AlgorithmResults o1, AlgorithmResults o2) {
				if (o1.getTotalScore() >= o2.getTotalScore()) return -1;
				return 1;
			}
		});
		
		// Return top 10 results
		ArrayList<AlgorithmResults> top_results = new ArrayList<AlgorithmResults>();
		
		for (int i=0; i < 10; i++)
		{
			if (i >= ma_results.size()) continue;
			AlgorithmResults mar = ma_results.get(i);
			top_results.add(mar);
			/*System.out.println(mar.getMAType() + " " + mar.getNumDaysBuy() 
					+ " " + mar.getNumDaysSell() + " " + mar.getTotalScore());
			for (RunResult rr: mar.getRunResults()) {
				System.out.println(rr.getStartDate() + " " + rr.getEndDate() + " " 
						+ rr.getOverallProfit() + " " + (rr.getEndPrice()-rr.getStartPrice()) 
						+ " " + rr.getNumTransactions());
			}
			System.out.println(); */
		}
		
		return top_results;

	}
	


	public RunResult run_once(RunConfig run_config)
	{
		//System.out.println(run_config.getStartDate() + " " + run_config.getEndDate());
		//System.out.println(run_config.getMAType() + " " + run_config.getNumDaysBuy() + " " + run_config.getNumDaysSell());
		RunResult run_result = new RunResult(run_config);

		boolean hold_stock = false;
		float buy_price = Float.NaN;
		float overall_profit = 0.0f;
		int num_transactions = 0;
		boolean first_time = true;
		boolean before_first_buy = true;
		
		Date last_date = null;
		float last_price = Float.NaN;

		float profit = 0.0f;
		float price = Float.NaN;

		
		//for (Entry<Date,DataPoint> entry : data.getDataPointMap(run_config.getStartDate(), run_config.getEndDate()).entrySet())
		for (Date date : data.getDateList(run_config.getStartDate(), run_config.getEndDate()))
		{
			//System.out.println(date);
			//date = entry.getKey();
			//data_point = entry.getValue();
			price = data.getPrice(date);
			
			TransactionType transaction_type = what_to_do(hold_stock,run_config,data, date, price);
			if (first_time)
			{
				if (transaction_type == TransactionType.BUY) {
					hold_stock = true;
					buy_price = Float.NaN;
				}
				else {
					hold_stock = false;
				}
				
				// Initialize start date and price at very first sample
				run_result.setStartDate(date);
				run_result.setStartPrice(price);
				
				first_time = false; 
				continue;
			}


			if (transaction_type == TransactionType.BUY)
			{
				if (before_first_buy) { // Initialize start price & date when we make our first buy!
					before_first_buy = false;
					run_result.setFirstBuyDate(date);
					run_result.setFirstBuyPrice(price);
				}
				//System.out.println("BUY at " + data_point.getPrice() + " on " + date);
				hold_stock = true;
				buy_price = price;
				num_transactions++;
			}
			else if (transaction_type == TransactionType.SELL)
			{			
				hold_stock = false;
				if (Float.isNaN(buy_price)) {
					profit = 0;
				}
				else {
					profit = price-buy_price;
					num_transactions++;					
					//System.out.println("SELL at " + data_point.getPrice() + " on " + date);
				}
				overall_profit += profit;
			}
			
			last_price = price;
			last_date = date;

		}

		// LAST DAY (realize profits if we hold stock)
		if (hold_stock) {
			if (Float.isNaN(buy_price)) { profit = 0; }
			else { profit = last_price-buy_price; }
			overall_profit += profit;
			num_transactions++;
		}

		run_result.setEndPrice(last_price);
		//System.out.println(run_result.getStartPrice() + " " + run_result.getEndPrice());
		run_result.setEndDate(last_date);
		//System.out.println(run_result.getStartDate() + " " + run_result.getEndDate());
		run_result.setOverallProfit(overall_profit);
		//System.out.println("OVERALL PROFIT: " + overall_profit);
		run_result.setNumTransactions(num_transactions);
		//System.out.println("num transactions = " + num_transactions);
	
		return run_result;
	}
	

	private TransactionType what_to_do(boolean currently_hold_stock, RunConfig run_config, Data data, Date date ,float price)
	{
		if (run_config.getMAType() == AlgorithmType.MA) 
		{
			float moving_avg_buy = data.getMA(date,run_config.getAlgorithmConfig(), run_config.getNumDaysBuy());
			float moving_avg_sell = data.getMA(date,run_config.getAlgorithmConfig(),run_config.getNumDaysSell());
			
			if (!data.isAverageValid(date,run_config.getNumDaysBuy()) || 
				!data.isAverageValid(date,run_config.getNumDaysSell())) {
				return TransactionType.HOLD;
			}
			
			if (!currently_hold_stock && price > moving_avg_buy && price > moving_avg_sell) {
				return TransactionType.BUY;
			}
			else if (currently_hold_stock && price < moving_avg_sell) {
				return TransactionType.SELL;
			}
		}
		else if (run_config.getMAType() == AlgorithmType.MACD)
		{
			if (!data.isAverageValid(date,run_config.getMACDSignalNumDays())) {
				return TransactionType.HOLD;
			}

			float macd = data.getMACD(date,run_config.getAlgorithmConfig());
			if (!currently_hold_stock && macd > 0) return TransactionType.BUY;
			else if (currently_hold_stock && macd < 0) return TransactionType.SELL;
		}
		return TransactionType.HOLD;
	}
	

	public static int randBetween(int start, int end) {
		return start + (int)Math.round(r.nextDouble()* (end - start));
	}
}
