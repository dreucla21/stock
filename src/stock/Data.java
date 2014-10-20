package stock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import stock.AlgorithmConfig.AlgorithmType;
import stock.AlgorithmConfig.AverageType;

public class Data 
{	
	public static String[] formatStrings = {"yyyy-MM-dd", "MM-dd-yyyy", "MM/dd/yyyy"};
	
	private final TreeMap<Date,DataPoint> data_point_map;
	
	public float getPrice(Date d)
	{
		return data_point_map.get(d).getPrice();
	}
	
	public List<Date> getDateList(Date sd, Date ed)
	{
		ArrayList<Date> list = new ArrayList<Date>();
		for (Entry<Date,DataPoint> entry : data_point_map.entrySet()) {
			if (sd.compareTo(entry.getKey()) <= 0 && ed.compareTo(entry.getKey()) >= 0) {
				list.add(entry.getKey());
			}
		}
		return list;
	}
	
	public float getMA(Date d, AlgorithmConfig algorithm_config, int num_days)
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
	
	public float getSMA(Date d, int num_days) {
		return data_point_map.get(d).getSMA(num_days);
	}
	public float getEMA(Date d, int num_days) {
		return data_point_map.get(d).getEMA(num_days);
	}
	public boolean isAverageValid(Date d, int num_days) {
		if (data_point_map.get(d).getIndex() >= (num_days-1)) return true;
		return false;
	}
	 
	public float getMACD(Date d, AlgorithmConfig ac)
	{
		int num_days1 = ac.getNumDaysBuy();
		int num_days2 = ac.getNumDaysSell();
		int macd_signal_num_days = ac.getMACDSignalNumDays();
		AlgorithmType algorithm_type = ac.getType();
		AverageType signal_type = ac.getSignalType();
		
		if (algorithm_type != AlgorithmType.MACD) {
			System.err.println("Data:getMACD ERROR!!");
			System.exit(-1);
			return -1;
		}
		
		// SIMPLE CASE
		if (macd_signal_num_days == 1) {
			return getMA(d, ac, num_days1)-getMA(d, ac, num_days2);
		}

		// Compute MACD for last (macd_signal_num_days)
		TreeSet<Date> dates = new TreeSet<Date>();
		int i=0;
		while (true)
		{
			//System.out.println(d);
			Calendar cal = Calendar.getInstance(); // creates calendar
			cal.setTime(d); 
			cal.add(Calendar.DATE,-i);
			Date curr_date = cal.getTime();
			if (data_point_map.get(curr_date) != null) dates.add(curr_date);
			if (dates.size() == macd_signal_num_days) break;
			i++;
			if (i > 100000) {
				System.err.println("Data:getMACD Infite LOOP!!!");
				System.exit(-1);
			}
		}
		
		// Compute EMA and SMA
		float ema_smoothing_factor = 2.0f/(1.0f+(float)macd_signal_num_days);
		float curr_ema = 0.0f;
		LinkedBlockingQueue<Float> sma_queue = new LinkedBlockingQueue<Float>(macd_signal_num_days);
		float sma_moving_count = 0;
		float diff = 0;
		for (Date curr_date : dates) // in sorted order
		{
			diff = getMA(curr_date, ac, num_days1)-getMA(curr_date, ac, num_days2);

			// Compute SMA
			if (sma_queue.size() == macd_signal_num_days) {
				float value_to_remove = sma_queue.poll();
				sma_moving_count -= value_to_remove;
			}
			try {
				sma_queue.put(diff);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sma_moving_count += diff;

			// Compute EMA
			curr_ema = ema_smoothing_factor*(diff) + curr_ema*(1-ema_smoothing_factor);
		}
		
		if (signal_type == AverageType.EMA) return curr_ema;
		else if (signal_type == AverageType.SMA) return (sma_moving_count/(float)sma_queue.size());
		else {
			System.out.println(signal_type);
			System.err.println("Data:getMACD ERROR!!");
			System.exit(-1);
			return -1;
		}
	}

	public TreeMap<Date,DataPoint> getDataPointMap(Date sd, Date ed)
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

	public void initializeMovingAverages(int start, int end, int increment) throws Exception
	{
		for (int i=start; i <= end; i+=increment) {
			compute_moving_averages(i);
		}
	}
	
	public void readFromURL(String url) throws Exception
	{
		readFromISR(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
		System.out.println("DONE READING FROM " + url);
	}
	
	private void readFromISR(InputStreamReader isr) throws Exception
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

	public void readFromCSV(String csv_file) throws Exception
	{
		readFromISR(new FileReader(new File(csv_file)));
	}
	
	private void compute_moving_averages(int num_days) throws Exception
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
	
	
	public Date getMaxDate() {
		return data_point_map.lastKey();
	}
	public Date getMinDate() {
		return data_point_map.firstKey();
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

	
	public class DataPoint
	{
		private float adjusted_close_price;
		private float low_price;
		private float high_price;
		private int index;
		private Date date;
		private HashMap<Integer,Float> sma_map; // simple moving average
		private HashMap<Integer,Float> ema_map; // exp moving average
		
		public DataPoint(Date d, float price)
		{
			this.date = d;
			this.adjusted_close_price = price;
			this.sma_map = new HashMap<Integer,Float>();
			this.ema_map = new HashMap<Integer,Float>();
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

		public float getMACD(int n_small, int n_large) {
			return getEMA(n_small)-getEMA(n_large); // TODO: Need 9 day moving average for MACD???
		}

	}
	
	
}
