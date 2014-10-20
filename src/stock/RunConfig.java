package stock;

import java.util.Date;

import stock.AlgorithmConfig.AlgorithmType;

public class RunConfig 
{
	private AlgorithmConfig config;
	private Date start_date;
	private Date end_date;
	
	public RunConfig(AlgorithmConfig mac, Date sd, Date ed)
	{
		config = mac;
		start_date = sd;
		end_date =ed;
	}
	
	public AlgorithmConfig getAlgorithmConfig() {
		return config;
	}
	public int getNumDaysBuy() {
		return config.getNumDaysBuy();
	}
	public int getNumDaysSell() {
		return config.getNumDaysSell();
	}
	public AlgorithmType getMAType() {
		return config.getType();
	}
	public int getMACDSignalNumDays() {
		return config.getMACDSignalNumDays();
	}
	
	public Date getStartDate() {
		return start_date;
	}
	public Date getEndDate() {
		return end_date;
	}
	public void setStartDate(Date d) {
		start_date = d;
	}
	public void setEndDate(Date d) {
		end_date = d;
	}
}
