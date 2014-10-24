package historical;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RunResult {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private RunConfig run_config;

	private Date start_date;
	private Date first_buy_date;
	private Date end_date;
	
	private float start_price;
	private float first_buy_price;
	private float end_price;
	
	private float overall_profit;
	private int num_transactions;
	
	public RunResult(RunConfig run_config) {
		this.run_config = run_config;
		start_price = Float.NaN;
		end_price = Float.NaN;
		overall_profit = 0f;
		num_transactions = 0;
		first_buy_price = Float.NaN;
		start_date =  null;
		first_buy_date = null;
		end_date = null;
	}
	public void setStartDate(Date d) {
		start_date = d;
	}
	public void setEndDate(Date d) {
		end_date = d;
	}
	public Date getStartDate() {
		return start_date;
	}
	public Date getEndDate() {
		return end_date;
	}
	
	public void setFirstBuyDate(Date d) {
		this.first_buy_date = d;
	}
	public Date getFirstBuyDate() {
		return this.first_buy_date;
	}
	public void setFirstBuyPrice(float f) {
		this.first_buy_price = f;
	}
	public float getFirstBuyPrice() {
		return this.first_buy_price;
	}
	
	public float getStartPrice() {
		return start_price;
	}
	public void setStartPrice(float p) {
		start_price = p;
	}
	public float getEndPrice() {
		return end_price;
	}
	public void setEndPrice(float p) {
		end_price = p;
	}
	public float getOverallProfit() {
		return overall_profit;
	}
	public void setOverallProfit(float p) {
		overall_profit = p;
	}
	public int getNumTransactions() {
		return num_transactions;
	}
	public void setNumTransactions(int n) {
		num_transactions = n;
	}

	public RunConfig getRunConfig() {
		return run_config;
	}

	// TODO: what score metric to use?
	// v = overall_profit - (end_price-start_price)
	// ideas: # of times we beat holding long term (# times that v > 0)
	// weight v based on time range, sum all weighted v?
	// sum all v?
	// avg v?
	// avg weighted v?
	// median v?
	// median weighted v?
	// start_price or first_buy_price??
	public float getScore() {
		//if (!Double.isNaN(rr.getStartPrice()) && !Double.isNaN(rr.getEndPrice())) {
		//	if (rr.getOverallProfit() > (rr.getEndPrice()-rr.getStartPrice())) total++;
		//}
		if (!Float.isNaN(getStartPrice()) && !Float.isNaN(getEndPrice())) 
		{
			if (Float.isNaN(getFirstBuyPrice())) return 0; // never bought!!
			return (getOverallProfit()-(getEndPrice()-getFirstBuyPrice())) / getFirstBuyPrice(); // normalized by first buy price
		}
		
		return Float.NaN;
	}

	@Override
	public String toString() {
		String first_buy_date = null;
		if (getFirstBuyDate() == null) {
			first_buy_date = "null";
		}
		else {
			first_buy_date = sdf.format(getFirstBuyDate());
		}
		
		return String.format("[sd=%s, fbp=%s, ed=%s]: (sp=%.2f, fbp=%.2f, ep=%.2f): num_transactions=%d, overall_profit=%.2f, score=%.2f",sdf.format(getStartDate()),first_buy_date,
				sdf.format(getEndDate()),getStartPrice(),getFirstBuyPrice(),getEndPrice(),getNumTransactions(),getOverallProfit(), getScore());
	}

	
}