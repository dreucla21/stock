package historical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import data.AlgorithmConfig;
import data.AlgorithmConfig.AlgorithmType;

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
		Collections.sort(run_results, new Comparator<RunResult>() {
			@Override
			public int compare(RunResult o1, RunResult o2) {
				if (o1.getScore() >= o2.getScore()) return 1;
				return -1;
			}
			
		});
	}
	
	public float getTotalScore() {
		float total = 0;
		float score = Float.NaN;
		for (RunResult rr : run_results) {
			score = rr.getScore();
			if (!Float.isNaN(score)) total += rr.getScore();
		}
		return total;
	}
	
	public float getAverageScore() {
		if (run_results.size() > 0) return getTotalScore()/run_results.size();
		return Float.NaN;
	}
	
	public float getMinScore() {
		if (run_results.size() > 0) return run_results.get(0).getScore();
		return Float.NaN;
	}
	
	public float getMaxScore() {
		if (run_results.size() > 0) return run_results.get(run_results.size()-1).getScore();
		return Float.NaN;
		
	}
	
	public float getMedianScore() {
		float median = Float.NaN;
		if (run_results.size() % 2 == 0)
		    median = run_results.get(run_results.size()/2).getScore() + run_results.get(run_results.size()/2 - 1).getScore();
		else
			median = run_results.get(run_results.size()/2).getScore();
		return median;
	}
	
	public ArrayList<RunResult> getRunResults() {
		return run_results;
	}
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	public static String generateCSVTitle() {
		return "algorithm_type, moving_average_type, macd_signal_type, macd_signal_length, num_days1, num_days2,,min,max,avg,med,total";
	}
	
	public String generateCSVString() {
		return 	String.format("%s,%s,%s,%s,%s,%s,,%.2f,%.2f,%.2f,%.2f,%.2f",
				getAlgorithmConfig().getType(),
				getAlgorithmConfig().getMovingAverageType(),
				getAlgorithmConfig().getMACDSignalType(),
				getAlgorithmConfig().getMACDNumDaysSignal(),
				getAlgorithmConfig().getNumDays1(),
				getAlgorithmConfig().getNumDays2(),
				getMinScore(),
				getMaxScore(),
				getAverageScore(),
				getMedianScore(),
				getTotalScore());
	}
	
	@Override
	public String toString() {
		if (getAlgorithmConfig().getType() == AlgorithmType.MACD)
		{
			return String.format("%s(moving_average_type=%s, signal_type=%s, signal_length=%s, num_days1=%d, num_days2=%d): scores=(min=%.2f, max=%.2f, avg=%.2f, med=%.2f, total=%.2f)",
					getAlgorithmConfig().getType(),getAlgorithmConfig().getMovingAverageType(),
					getAlgorithmConfig().getMACDSignalType(),getAlgorithmConfig().getMACDNumDaysSignal(),
					getAlgorithmConfig().getNumDays1(),getAlgorithmConfig().getNumDays2(),
					getMinScore(), getMaxScore(), getAverageScore(), getMedianScore(), getTotalScore());
		}
		else
		{
			return String.format("%s(moving_average_type=%s, num_days1=%d, num_days2=%d): scores=(min=%.2f, max=%.2f, avg=%.2f, med=%.2f, total=%.2f)",
					getAlgorithmConfig().getType(),getAlgorithmConfig().getMovingAverageType(),
					getAlgorithmConfig().getNumDays1(),getAlgorithmConfig().getNumDays2(),
					getMinScore(), getMaxScore(), getAverageScore(), getMedianScore(), getTotalScore());
		}
	}
	
}