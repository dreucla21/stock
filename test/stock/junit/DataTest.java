package stock.junit;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import data.AlgorithmConfig;
import data.Data;
import data.AlgorithmConfig.AlgorithmType;
import data.AlgorithmConfig.AverageType;

public class DataTest 
{
	private static Data data;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.out.println("DataTest");
		data = new Data();
		data.readFromCSV("NFLX.csv");
		
		HashSet<Integer> lengths = new HashSet<Integer>();
		for (int i=0; i < 300; i++) lengths.add(i);
		
		data.initializeMovingAverages(lengths);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}
	
	@Test
	public void testNFLX()
	{
		Date date = Data.parseDate("10/15/2014");

		// TEST SMA
		assertEquals(458.33,data.getSMA(date,30),0.01);
		
		// TEST EMA
		assertEquals(452.26,data.getEMA(date,12),0.02);
		assertEquals(454.89,data.getEMA(date,26),0.02);
		
		// TEST MACD EMA with no signal
		AlgorithmConfig ac = new AlgorithmConfig(AlgorithmType.MACD,12,26);
		ac.setMovingAverageType(AverageType.EMA);
		ac.setMACDSignalType(AverageType.EMA);
		ac.setMACDNumDaysSignal(1);
		assertEquals(-2.63,data.getMACD(date, ac),0.02);
		
		// TEST MACD EMA with EMA signal
		AlgorithmConfig ac2 = new AlgorithmConfig(AlgorithmType.MACD,12,26);
		ac2.setMovingAverageType(AverageType.EMA);
		ac2.setMACDSignalType(AverageType.EMA);
		ac2.setMACDNumDaysSignal(9);
		assertEquals(-2.18,data.getMACD(date, ac2),0.02);
	}
	
}