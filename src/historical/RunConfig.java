package historical;

import java.util.Date;

import data.AlgorithmConfig;

public class RunConfig 
{
	private AlgorithmConfig config;
	private Date start_date;
	private Date end_date;
	
	public RunConfig(AlgorithmConfig ac, Date sd, Date ed)
	{
		config = ac;
		start_date = sd;
		end_date =ed;
	}
	
	public AlgorithmConfig getAlgorithmConfig() {
		return config;
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