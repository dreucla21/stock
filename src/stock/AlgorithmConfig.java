package stock;

public class AlgorithmConfig {

	public enum AlgorithmType 
	{
		//SMA, // price > buy_sma and sell_sma AND price < sell_sma
		//EMA, // price > buy_ema and sell_ema AND price < sell_ema
		//MACD_SMA, // buy_sma - sell_sma > 0 AND < 0 (TODO: ADD SIGNAL SMA/EMA as parameter)
		//MACD_EMA, // buy_ema - sell_ema > 0 AND < 0 (TODO: ADD SIGNAL SMA/EMA as parameter)
		//MACD // ema(9) of [buy_ema (12) - sell_ema(26)] > 0 AND < 0 (TODO: GET RID OF MACD)
		// TODO: TEST MACD!!!
		
		// TODO: CHANGE TO JUST (MA, MACD)
		MA,
		MACD
	}
	
	public enum AverageType
	{
		SMA,
		EMA
	}
	
	private AlgorithmType type;
	private int num_days_buy;
	private int num_days_sell;
	private int macd_signal_num_days; // only for MACD AlgorithmTypes
	private AverageType moving_average_type; // for ALL
	private AverageType signal_type; // only for MACD AlgorithmTypes

	public AlgorithmConfig(AlgorithmType t, int b, int s)
	{
		num_days_buy = b;
		num_days_sell = s;
		type = t; // default
		macd_signal_num_days = 1;
		signal_type = null;
		moving_average_type = null;
	}
	
	public int getNumDaysBuy() {
		return num_days_buy;
	}
	public int getNumDaysSell() {
		return num_days_sell;
	}
	public void setMACDSignalNumDays(int v) {
		macd_signal_num_days = v;
	}
	public int getMACDSignalNumDays() {
		return macd_signal_num_days; 
	}
	public void setSignalType(AverageType t) {
		signal_type = t;
	}
	public AverageType getMovingAverageType() {
		return moving_average_type;
	}
	public void setMovingAverageType(AverageType t) {
		moving_average_type = t;
	}
	public AverageType getSignalType() {
		return signal_type;
	}
	
	public AlgorithmType getType() {
		return type;
	}
}
