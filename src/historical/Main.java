package historical;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import data.AlgorithmConfig;
import data.Data;
import data.AlgorithmConfig.AlgorithmType;
import data.AlgorithmConfig.AverageType;

public class Main 
{
	public static long SECONDS_IN_A_YEAR = 31556900;
	public static long DAYS_IN_A_YEAR = 365;
	public static Random r;
	
	private Data data;
	private int numThreads;
	private String stockTicker;
	private Integer[] maLengths;
	private Integer[] macdMaLengths;
	private Integer[] macdSignalLengths;
	private int numSimulations;
	private long fixedTimeRangeNumDays;
	
	public enum TransactionType
	{
		BUY,
		SELL,
		HOLD
	}
	
	private static Date generate_random_date(Date d1, Date d2) {
		long t1 = d1.getTime();
		long t2 = d2.getTime();
		long start_time = t1;
		if (t2 < t1) start_time = t2;
		return new Date(start_time + (long)(r.nextDouble()*(float)(Math.abs(t1-t2))));
	}

	public static void main(String[] args)
	{
		// Initialize random # generator
		r = new Random();
		r.setSeed(1);

		// Parameters
		String stock_ticker = "NFLX";
		int num_threads = 8;
		Integer[] ma_lengths = new Integer[]{5,10,15,20,25,30,50,75,100,150,200,250,300};
		Integer[] macd_ma_lengths = new Integer[]{5,10,12,15,18,20,25,26,30,35,40,50};
		Integer[] macd_signal_lengths = new Integer[]{1,2,3,4,5,6,7,8,9,10};
		int num_simulations = 100;
	//	long fixed_time_range_num_days = 3*DAYS_IN_A_YEAR;
		long fixed_time_range_num_days = -1; // time ranges can vary

		try {
			Main main = new Main(stock_ticker);
			main.initializeLearningParameters(ma_lengths,macd_ma_lengths,macd_signal_lengths, num_simulations, fixed_time_range_num_days);
			main.setNumThreads(num_threads);
			main.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	
	public Main(String stock_ticker) throws Exception
	{
		stockTicker = stock_ticker;
		data = new Data();
		data.readFromCSV(stockTicker + ".csv");
		//data.readFromURL("http://real-chart.finance.yahoo.com/table.csv?s=" + stock_ticker);
	}
	
	public void initializeLearningParameters(Integer[] ma_lengths, Integer[] macd_ma_lengths, Integer[] macd_signal_lengths, int num_simulations, long fixed_time_range_num_days) throws Exception
	{
		this.maLengths = ma_lengths;
		this.macdMaLengths = macd_ma_lengths;
		this.macdSignalLengths = macd_signal_lengths;
		this.numSimulations = num_simulations;
		this.fixedTimeRangeNumDays = fixed_time_range_num_days;
		
		HashSet<Integer> lengths = new HashSet<Integer>();
		for (Integer i : ma_lengths) {
			lengths.add(i);
		}
		for (Integer i : macd_ma_lengths) {
			lengths.add(i);
		}
		data.initializeMovingAverages(lengths);
	}
	
	public void setNumThreads(int n) {
		numThreads = n;
	}
	
	public void run() throws Exception
	{
		/*AlgorithmConfig algorithm_config = new AlgorithmConfig(AlgorithmType.MACD,30,10);
		algorithm_config.setMovingAverageType(AverageType.SMA);
		algorithm_config.setMACDSignalType(AverageType.SMA);
		algorithm_config.setMACDNumDaysSignal(4);
		RunConfig run_config = new RunConfig(algorithm_config,Data.parseDate("10/18/2011"),Data.parseDate("10/17/2014"));
		RunThread run_thread = new RunThread(run_config);
		run_thread.run();
		RunResult run_result = run_thread.getRunResult();
		System.out.println(run_result.getOverallProfit());*/

		/*AlgorithmConfig algorithm_config2 = new AlgorithmConfig(AlgorithmType.MACD,12,26);
		algorithm_config2.setMovingAverageType(AverageType.EMA);
		algorithm_config2.setMACDSignalNumDays(1);
		RunConfig run_config2 = new RunConfig(algorithm_config2,Data.parseDate("05/24/2011"),Data.parseDate("10/17/2014"));
		RunResult run_result2 = this.run_once(run_config2); 
		System.out.println(run_result2.getOverallProfit()); */
		//all_results.put(ma_config, Arrays.asList(new RunResult[]{run_result})); 

		ArrayList<AlgorithmResults> top_results = learn(data.getMinDate(),data.getMaxDate(),fixedTimeRangeNumDays,numSimulations);
		System.out.println();
		
		PrintWriter pw = new PrintWriter(stockTicker + "_results.csv");
		pw.println(AlgorithmResults.generateCSVTitle());
		pw.println();
		
		for (AlgorithmResults ar : top_results)
		{
			System.out.println(ar);
			pw.println(ar.generateCSVString());
			
			AlgorithmConfig ac = ar.getAlgorithmConfig();
			Date start_date = new Date(data.getMaxDate().getTime()-3*(SECONDS_IN_A_YEAR*1000));
			RunConfig run_config = new RunConfig(ac,start_date,data.getMaxDate());
			RunResult rr = run_once(run_config);
			System.out.println("TEST RESULT: " + rr);
			System.out.println();
		} 
		pw.close();

	}
	
	public ArrayList<Date[]> generate_random_date_ranges(Date start_date, Date end_date)
	{
		long length_ms = 0;
		if (fixedTimeRangeNumDays > 0) {
			length_ms = fixedTimeRangeNumDays*24*60*60*1000;
		}
		if (length_ms > end_date.getTime()-start_date.getTime()) {
			System.err.println("Main:generate_random_date_ranges: fixed_length_num_days exceeds data range!!");
		}
		
		ArrayList<Date[]> dates = new ArrayList<Date[]>();
		Date sd = null;
		Date ed = null;
		for (int y=0; y < numSimulations; y++) 
		{
			Date d = generate_random_date(start_date,end_date);
			
			if (fixedTimeRangeNumDays < 0) {
				Date d2 = generate_random_date(start_date,end_date);
				sd = d;
				ed = d2;
				if (d.compareTo(d2) > 0) {
					sd = d2;
					ed = d;
				}
			}
			else {
				// Use date d as midpoint between start & end date
				sd = new Date(d.getTime()-(length_ms/2));
				ed = new Date(d.getTime()+(length_ms/2));
				if (sd.getTime() < start_date.getTime()) {
					continue;
				}
				if (ed.getTime() > end_date.getTime()) {
					continue;
				}
			}
			dates.add(new Date[]{sd,ed});
			System.out.println(sd + " " + ed);
		}  
		return dates;
	}
	
	
	

	/**
	 * Return top 10 results
	 * @param start_year
	 * @param end_year
	 * @param num_simulations
	 */
	public ArrayList<AlgorithmResults> learn(Date start_date, Date end_date, Long fixed_length_num_days, int num_simulations) throws Exception
	{
		ArrayList<RunThread> all_run_threads = new ArrayList<RunThread>();
		ArrayList<Date[]> dates = generate_random_date_ranges(data.getMinDate(),data.getMaxDate());

		for (int i : maLengths) {
			for (int j : maLengths) {
				AlgorithmConfig mac = new AlgorithmConfig(AlgorithmType.MA, j,i);
				mac.setMovingAverageType(AverageType.SMA);
				AlgorithmConfig mac2 = new AlgorithmConfig(AlgorithmType.MA,j,i);
				mac2.setMovingAverageType(AverageType.EMA);
				
				all_run_threads.add(new RunThread(mac,dates));
				all_run_threads.add(new RunThread(mac2,dates));
			}
		} 
		for (int i : macdMaLengths) {
			for (int j : macdMaLengths) {
				if (j == i) continue;
				for (int k : macdSignalLengths) { // signal length

					AlgorithmConfig mac = new AlgorithmConfig(AlgorithmType.MACD,j,i);		
					mac.setMovingAverageType(AverageType.EMA);
					mac.setMACDSignalType(AverageType.EMA);
					mac.setMACDNumDaysSignal(k); 
					
					AlgorithmConfig mac2 = new AlgorithmConfig(AlgorithmType.MACD,j,i);
					mac2.setMovingAverageType(AverageType.SMA);
					mac2.setMACDSignalType(AverageType.SMA);
					mac2.setMACDNumDaysSignal(k); 
					
					all_run_threads.add(new RunThread(mac,dates));
					all_run_threads.add(new RunThread(mac2,dates));
				}
			}
		}
		
		System.out.println("TOTAL # of Run Threads: " + all_run_threads.size());
		
		// Run all of the RunConfigs and gather results
		ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads,numThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
		for (RunThread rt : all_run_threads) {
			executor.execute(rt);
		}
		executor.shutdown();
		long count;
		do {
			count = executor.getCompletedTaskCount();
			System.out.println("Completed task count: " + count + " / " + all_run_threads.size());
			Thread.sleep(2000);
		} while (!executor.isTerminated());
		
		// Build up a list of results grouped by Algorithm
		ArrayList<AlgorithmResults> ma_results = new ArrayList<AlgorithmResults>();
		for (RunThread rt : all_run_threads) {
			ma_results.add(rt.getAlgorithmResults());
		}
		
		Collections.sort(ma_results,new Comparator<AlgorithmResults>() {
			@Override
			public int compare(AlgorithmResults o1, AlgorithmResults o2) {
				if (o1.getMedianScore() >= o2.getMedianScore()) return -1;
				return 1;
			}
		});
		return ma_results;

	}
	
	
	public class RunThread implements Runnable 
	{
		private ArrayList<Date[]> dates; // INPUT
		private AlgorithmConfig algorithm_config; // INPUT
		private AlgorithmResults algorithm_results; // OUTPUT

		public RunThread(AlgorithmConfig algorithm_config, ArrayList<Date[]> dates) {
			this.algorithm_config = algorithm_config;
			this.algorithm_results = new AlgorithmResults(this.algorithm_config);
			this.dates = dates;
		}
		
		@Override
		public void run() {
			for (Date[] s : dates) {
				RunConfig rc = new RunConfig(algorithm_config,s[0],s[1]);
				algorithm_results.addRunResult(run_once(rc));
			}
		}

		public AlgorithmResults getAlgorithmResults() {
			return algorithm_results;
		}
	}
	
	
	private RunResult run_once(RunConfig run_config) {	
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

		
		for (Date date : data.getDateList(run_config.getStartDate(), run_config.getEndDate()))
		{
			price = data.getPrice(date);
			
			TransactionType transaction_type = data.what_to_do(hold_stock, run_config.getAlgorithmConfig(), date);
			
			//TransactionType transaction_type = what_to_do(hold_stock,run_config.getAlgorithmConfig(),data, date, price);
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
			if (Float.isNaN(buy_price)) { 
				profit = 0; 
			}
			else { 
				profit = last_price-buy_price;
				num_transactions++;
				
			}
			overall_profit += profit;
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

	
	/*private TransactionType what_to_do(boolean currently_hold_stock, AlgorithmConfig algorithm_config, Data data, Date date ,float price)
	{
		if (algorithm_config.getType() == AlgorithmType.MA) 
		{
			float moving_avg_buy = data.getMA(date,algorithm_config,algorithm_config.getNumDays1());
			float moving_avg_sell = data.getMA(date,algorithm_config,algorithm_config.getNumDays2());
			
			if (!data.isAverageValid(date,algorithm_config.getNumDays1()) || 
				!data.isAverageValid(date,algorithm_config.getNumDays2())) {
				return TransactionType.HOLD;
			}
			
			if (!currently_hold_stock && price > moving_avg_buy && price > moving_avg_sell) {
				return TransactionType.BUY;
			}
			else if (currently_hold_stock && price < moving_avg_sell) {
				return TransactionType.SELL;
			}
		}
		else if (algorithm_config.getType() == AlgorithmType.MACD)
		{
			if (!data.isAverageValid(date,algorithm_config.getMACDNumDaysSignal())) {
				return TransactionType.HOLD;
			}

			float macd = data.getMACD(date,algorithm_config);
			if (!currently_hold_stock && macd > 0) return TransactionType.BUY;
			else if (currently_hold_stock && macd < 0) return TransactionType.SELL;
		}
		return TransactionType.HOLD;
	}*/
	



}