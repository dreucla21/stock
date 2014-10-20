package stock;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AlgorithmResults {

	private AlgorithmConfig algorithm_config;
	private ArrayList<RunResult> run_results;
	
	public AlgorithmResults(AlgorithmConfig c) {
		algorithm_config = c;
		run_results = new ArrayList<RunResult>();
	}

	public AlgorithmConfig getAlgorithmConfig() {
		return algorithm_config; 
	}
	
	public void addRunResult(RunResult rr) {
		run_results.add(rr);
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
	public double getTotalScore() {
		double total = 0;
		for (RunResult rr : run_results) {
			
			//if (!Double.isNaN(rr.getStartPrice()) && !Double.isNaN(rr.getEndPrice())) {
			//	if (rr.getOverallProfit() > (rr.getEndPrice()-rr.getStartPrice())) total++;
			//}
			
			if (!Double.isNaN(rr.getStartPrice()) && !Double.isNaN(rr.getEndPrice())) {
				total += (rr.getOverallProfit()-(rr.getEndPrice()-rr.getFirstBuyPrice()));
						//*getDateDiff(rr.getStartDate(),rr.getEndDate(),TimeUnit.DAYS);
			}
		}
		return total;
	}
	
	public ArrayList<RunResult> getRunResults() {
		return run_results;
	}
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
}
