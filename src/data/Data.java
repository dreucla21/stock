package data;

import historical.Main.TransactionType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import data.AlgorithmConfig.AlgorithmType;
import data.AlgorithmConfig.AverageType;
import data.AlgorithmConfig.MACD;

public class Data 
{	
	public static String[] formatStrings = {"yyyy-MM-dd", "MM-dd-yyyy", "MM/dd/yyyy"};
	
	private final TreeMap<Date,DataPoint> data_point_map;
	
	public synchronized float getPrice(Date d)
	{
		return data_point_map.get(d).getPrice();
	}
	
	public synchronized List<Date> getDateList(Date sd, Date ed)
	{
		ArrayList<Date> list = new ArrayList<Date>();
		for (Entry<Date,DataPoint> entry : data_point_map.entrySet()) {
			if (sd.compareTo(entry.getKey()) <= 0 && ed.compareTo(entry.getKey()) >= 0) {
				list.add(entry.getKey());
			}
		}
		return list;
	}
	
	public synchronized float getMA(Date d, AlgorithmConfig algorithm_config, int num_days)
	{
		if (algorithm_config.getMovingAverageType() == AverageType.SMA) {
			return data_point_map.get(d).getSMA(num_days);
		}
		else if (algorithm_config.getMovingAverageType() == AverageType.EMA) {
			return data_point_map.get(d).getEMA(num_days);
		}
		System.out.println(algorithm_config.getMovingAverageType());
		System.err.println("Data:getMovingAverage: ERROR!!");
		System.exit(-1);
		return -1;
	}
	
	public synchronized float getSMA(Date d, int num_days) {
		return data_point_map.get(d).getSMA(num_days);
	}
	public synchronized float getEMA(Date d, int num_days) {
		return data_point_map.get(d).getEMA(num_days);
	}
	public synchronized boolean isAverageValid(Date d, int num_days) {
		if (data_point_map.get(d).getIndex() >= (num_days-1)) return true;
		return false;
	}
	
	public synchronized ArrayList<Date> getLastNDays(Date last_date, int num_days) throws Exception {
		
		LinkedBlockingQueue<Date> queue = new LinkedBlockingQueue<Date>(num_days);
		for (Date d : data_point_map.keySet()) {
			if (queue.size() == num_days) queue.poll();
			queue.put(d);
			if (d.getTime() == last_date.getTime()) return new ArrayList<Date>(queue);
		}
		return null;
	}
	
	public synchronized void setMACDAlgorithmConfig(AlgorithmConfig ac)
	{
		int macd_num_days_signal = ac.getMACDNumDaysSignal();

		if (ac.getType() != AlgorithmType.MACD) {
			System.err.println("Data::setMACDAlgorithmConfig ERROR!!");
			System.exit(-1);
		}
		
		// SIMPLE CASE
		if (macd_num_days_signal == 1) {
			return;
		}
		
		// Compute EMA and SMA
		float ema_smoothing_factor = 2.0f/(1.0f+(float)macd_num_days_signal);
		float curr_ema = 0.0f;
		LinkedBlockingQueue<Float> sma_queue = new LinkedBlockingQueue<Float>(macd_num_days_signal);
		float sma_moving_count = 0;
		float sma_diff = 0;
		float ema_diff = 0;
		Date date = null;
		DataPoint data_point = null;
		
		for (Entry<Date,DataPoint> entry : data_point_map.entrySet())
		{
			date = entry.getKey();
			data_point = entry.getValue();
			
			sma_diff = getSMA(date, ac.getNumDays1())-getSMA(date, ac.getNumDays2());
			ema_diff = getEMA(date, ac.getNumDays1())-getEMA(date, ac.getNumDays2());

			// Compute SMA
			if (sma_queue.size() == macd_num_days_signal) {
				Float value_to_remove = sma_queue.poll();
				sma_moving_count -= value_to_remove;
			}
			try {
				sma_queue.put(sma_diff);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sma_moving_count += sma_diff;
			
			// Compute EMA
			curr_ema = ema_smoothing_factor*ema_diff + curr_ema*(1-ema_smoothing_factor);
			
			data_point.setMACD_SMA(ac.getMACD(),sma_moving_count/(float)sma_queue.size());
			data_point.setMACD_EMA(ac.getMACD(),curr_ema);
		}
	}
	 
	public synchronized float getMACD(Date d, AlgorithmConfig ac)
	{
		//System.out.println("ENTER GETMACD");
		if (ac.getType() != AlgorithmType.MACD) {
			System.err.println("Data:getMACD ERROR!!");
			System.exit(-1);
			return -1;
		}

		// SIMPLE CASE
		if (ac.getMACDNumDaysSignal() == 1) {
			return getMA(d, ac, ac.getNumDays1())-getMA(d, ac, ac.getNumDays2());
		}
		else {
			MACD macd = ac.getMACD();
			DataPoint data_point = data_point_map.get(d);
			if (ac.getMACDSignalType() == AverageType.EMA) {
				Float f = data_point.getMACD_EMA(macd);
				if (f == null) {
					setMACDAlgorithmConfig(ac);
					f = data_point.getMACD_EMA(macd);
				}
				return f;
			}
			else if (ac.getMACDSignalType() == AverageType.SMA) {
				Float f = data_point.getMACD_SMA(macd);
				if (f == null) {
					setMACDAlgorithmConfig(ac);
					f = data_point.getMACD_SMA(macd);
				}
				return f;
			}
			else {
				System.err.println("Data:getMACD ERROR!!");
				System.exit(-1);
				return -1;
			}
		}
	}

	public synchronized TreeMap<Date,DataPoint> getDataPointMap(Date sd, Date ed)
	{
		TreeMap<Date,DataPoint> map = new TreeMap<Date,DataPoint>();
		for (Entry<Date,DataPoint> entry : data_point_map.entrySet()) {
			if (sd.compareTo(entry.getKey()) <= 0 && ed.compareTo(entry.getKey()) >= 0) {
				map.put(entry.getKey(),entry.getValue());
			}
		}
		return map;
	}
	
	public Data()
	{
		data_point_map = new TreeMap<Date,DataPoint>();
	}

	public synchronized void initializeMovingAverages(HashSet<Integer> lengths) throws Exception
	{
		for (int i : lengths) {
			compute_moving_averages(i);
		}
	}
	
	public synchronized void readFromURL(String url) throws Exception
	{
		readFromISR(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
		System.out.println("DONE READING FROM " + url);
	}
	
	private synchronized void readFromISR(InputStreamReader isr) throws Exception
	{
		// Read in all historical data from a CSV file
		BufferedReader br;
		String line = "";
		String cvsSplitBy = ",";
		br = new BufferedReader(isr);
		while ((line = br.readLine()) != null) 
		{
			// use comma as separator
			// 0 -> date
			// 1 --> open
			// 2 --> high
			// 3 --> low
			// 4 --> close
			// 5 --> volume
			// 6 --> adj close
			String[] values = line.split(cvsSplitBy);

			if (values[0].equals("Date")) continue; // skip line 1

			Date date = parseDate(values[0]);

			float normalization_ratio = Float.valueOf(values[6])/Float.valueOf(values[4]); // normalize low/high values

			// Create data point and add to map
			DataPoint data_point = new DataPoint(date,Float.valueOf(values[6]));
			data_point.setHighPrice(normalization_ratio*Float.valueOf(values[2]));
			data_point.setLowPrice(normalization_ratio*Float.valueOf(values[3]));
			data_point_map.put(date,data_point);

		}
		br.close();
		
		int index = 0;
		for (Entry<Date,DataPoint> entry : data_point_map.entrySet()) {
			entry.getValue().setIndex(index);
			index++;
		}
		
		System.out.println("FIRST DATE = " + data_point_map.firstKey());
	}

	public synchronized void readFromCSV(String csv_file) throws Exception
	{
		readFromISR(new FileReader(new File(csv_file)));
	}
	
	private synchronized void compute_moving_averages(int num_days) throws Exception
	{
		LinkedBlockingQueue<Float> queue = new LinkedBlockingQueue<Float>(num_days);
		float moving_count = 0;
		DataPoint data_point;
		
		float curr_ema = 0.0f;
		float ema_smoothing_factor = 2.0f/(1.0f+(float)num_days);
		
		for (Entry<Date,DataPoint> entry : data_point_map.entrySet())
		{
			data_point = entry.getValue();

			// Compute SMA
			if (queue.size() == num_days) {
				Float value_to_remove = queue.poll();
				moving_count -= value_to_remove;
			}
			queue.put(data_point.getPrice());
			moving_count += data_point.getPrice();
			data_point.addSMA(num_days, moving_count/(float)queue.size());
			
			// Compute EMA
			curr_ema = ema_smoothing_factor*data_point.getPrice() + curr_ema*(1-ema_smoothing_factor);
			data_point.addEMA(num_days, curr_ema);
		}
	}
	
	
	public synchronized Date getMaxDate() {
		return data_point_map.lastKey();
	}
	public synchronized Date getMinDate() {
		return data_point_map.firstKey();
	}
	
	
	public synchronized Boolean get_own_status(AlgorithmConfig algorithm_config, Date date)
	{
		float price = this.getPrice(date);
		if (algorithm_config.getType() == AlgorithmType.MA)
		{
			float moving_avg_buy = getMA(date, algorithm_config, algorithm_config.getNumDays1());
			float moving_avg_sell = getMA(date, algorithm_config, algorithm_config.getNumDays2());
			
			if (price > moving_avg_buy && price > moving_avg_sell) return true; // should be holding stock
			else return false;
		}
		else if (algorithm_config.getType() == AlgorithmType.MACD)
		{
			float macd = getMACD(date,algorithm_config);
			if (macd > 0) return true;
			else return false;
		}
		return null;
	}
	
	
	public synchronized TransactionType what_to_do(boolean currently_hold_stock, AlgorithmConfig algorithm_config, Date date)
	{
		float price = this.getPrice(date);
		if (algorithm_config.getType() == AlgorithmType.MA) 
		{
			float moving_avg_buy = getMA(date,algorithm_config,algorithm_config.getNumDays1());
			float moving_avg_sell = getMA(date,algorithm_config,algorithm_config.getNumDays2());
			
			if (!isAverageValid(date,algorithm_config.getNumDays1()) || 
				!isAverageValid(date,algorithm_config.getNumDays2())) {
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
			if (!isAverageValid(date,algorithm_config.getMACDNumDaysSignal())) {
				return TransactionType.HOLD;
			}

			float macd = getMACD(date,algorithm_config);
			if (!currently_hold_stock && macd > 0) return TransactionType.BUY;
			else if (currently_hold_stock && macd < 0) return TransactionType.SELL;
		}
		return TransactionType.HOLD;
	}
	
	

	public static Date parseDate(String dateString)
	{
		for (String formatString : formatStrings) {
			try {
				return new SimpleDateFormat(formatString).parse(dateString);
			} catch (Exception e) {}
		}
		return null;
	}

	
	private class DataPoint
	{
		private float adjusted_close_price;
		private float low_price;
		private float high_price;
		private int index;
		private Date date;
		private HashMap<Integer,Float> sma_map; // simple moving average
		private HashMap<Integer,Float> ema_map; // exp moving average
		private HashMap<MACD,Float> macd_sma_map;
		private HashMap<MACD,Float> macd_ema_map;
		
		public DataPoint(Date d, float price)
		{
			this.date = d;
			this.adjusted_close_price = price;
			this.sma_map = new HashMap<Integer,Float>();
			this.ema_map = new HashMap<Integer,Float>();
			this.macd_sma_map = new HashMap<MACD,Float>();
			this.macd_ema_map = new HashMap<MACD,Float>();
		}
		
		public int getIndex() {
			return this.index;
		}
		public void setIndex(int i) {
			index = i;
		}
		
		public void setLowPrice(float p) {
			this.low_price = p;
		}
		public float getLowPrice() {
			return this.low_price;
		}
		
		public void setHighPrice(float p) {
			this.high_price = p;
		}
		public float getHighPrice() {
			return this.high_price;
		}
		
		public float getPrice() {
			return adjusted_close_price;
		}
		
		public void addSMA(int num_days, float avg) {
			sma_map.put(num_days,avg);
		}
		public float getSMA(int num_days) {
			return sma_map.get(num_days);
		}
		public void addEMA(int num_days, float avg) {
			this.ema_map.put(num_days, avg);
		}
		public float getEMA(int num_days) {
			return ema_map.get(num_days);
		}

		public void setMACD_SMA(MACD m, float v) {
			macd_sma_map.put(m, v);
		}
		public Float getMACD_SMA(MACD m) {
			return macd_sma_map.get(m);
		}
		public void setMACD_EMA(MACD m,float v) {
			macd_ema_map.put(m, v);
		}
		public Float getMACD_EMA(MACD m) {
			return macd_ema_map.get(m);
		}

	}
	
	
}