package data;

public class AlgorithmConfig {

	public enum AlgorithmType 
	{
		MA,
		MACD
	}
	
	public enum AverageType
	{
		SMA,
		EMA
	}
	
	private AlgorithmType type;
	private int num_days1;
	private int num_days2;
	private AverageType moving_average_type; // for ALL
	
	private int macd_num_days_signal; // only for MACD AlgorithmTypes
	private AverageType macd_signal_type; // only for MACD AlgorithmTypes
	
	public AlgorithmConfig(AlgorithmType t, AverageType mat, AverageType macd_signal_type, int macd_num_days_signal, int n1, int n2)
	{
		this.type = t;
		this.moving_average_type = mat;
		this.macd_signal_type = macd_signal_type;
		this.macd_num_days_signal = macd_num_days_signal;
		this.num_days1 = n1;
		this.num_days2 = n2;
	}

	public AlgorithmConfig(AlgorithmType t, int n1, int n2)
	{
		num_days1 = n1;
		num_days2 = n2;
		type = t; // default
		macd_num_days_signal = -1;
		macd_signal_type = null;
		moving_average_type = null;
	}
	
	public int getNumDays1() {
		return num_days1;
	}
	public int getNumDays2() {
		return num_days2;
	}
	public void setMACDNumDaysSignal(int v) {
		macd_num_days_signal = v;
	}
	public int getMACDNumDaysSignal() {
		return macd_num_days_signal; 
	}
	public void setMACDSignalType(AverageType t) {
		macd_signal_type = t;
	}
	public AverageType getMovingAverageType() {
		return moving_average_type;
	}
	public void setMovingAverageType(AverageType t) {
		moving_average_type = t;
	}
	public AverageType getMACDSignalType() {
		return macd_signal_type;
	}
	public AlgorithmType getType() {
		return type;
	}
	public MACD getMACD() {
		if (type == AlgorithmType.MACD) return new MACD(num_days1,num_days2,macd_num_days_signal);
		else return null;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s",getType(),getMovingAverageType(),getMACDSignalType(),getMACDNumDaysSignal(),
								getNumDays1(), getNumDays2());
	}
	
	
	public static class MACD
	{
		public int sta_length;
		public int lta_length;
		public int signal_length;
		
		public MACD(int s, int l, int signal)
		{
			sta_length = s;
			lta_length = l;
			signal_length = signal;
		}
		
		@Override
	    public int hashCode() {
			return sta_length+lta_length+signal_length;
	    }
		
		@Override
	    public boolean equals(Object o) {
			MACD m = (MACD)o;
			if (this.lta_length == m.lta_length && this.sta_length == m.sta_length && this.signal_length == m.signal_length) {
				return true;
			}
			return false;
	    }
	}
}