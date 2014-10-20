package stock;

import java.util.Date;

public class RunResult {

	private RunConfig run_config;
	private float start_price;
	private float end_price;
	private float overall_profit;
	private int num_transactions;
	
	private Date first_buy_date;
	private float first_buy_price;
	
	
	public RunResult(RunConfig run_config) {
		this.run_config = run_config;
		start_price = Float.NaN;
		end_price = Float.NaN;
		overall_profit = 0f;
		num_transactions = 0;
	}
	public void setStartDate(Date d) {
		this.run_config.setStartDate(d);
	}
	public void setEndDate(Date d) {
		this.run_config.setEndDate(d);
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
	public Date getStartDate() {
		return run_config.getStartDate();
	}
	public Date getEndDate() {
		return run_config.getEndDate();
	}

	
}
