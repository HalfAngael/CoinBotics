package main;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import com.cf.client.poloniex.*;
import com.cf.data.model.poloniex.*;

public class Main {
	
	static String apiKey = "B2VGMED6-YSCW3QN2-OCJT275F-PC9ZH0EI";
	static String apiSecret = "79918444b2526789e5530c51d94877ac9205a5d0e7712ec6084f659d0734a3533ddc31bbd9d5d7f69a1ae99f75e29598323fde7cc2b7cd969073e9d782359a9c";
	static int numberOfCoins = 10;
	static PoloniexExchangeService service = new PoloniexExchangeService(apiKey, apiSecret);
	static double startingAmount = service.returnBalance("BTC").available.doubleValue();
	
	public static void main(String[] args) {
		System.out.println(startingAmount);
		
		//Gets all markets into an ArrayList
		List<String> marketsList = service.returnAllMarkets();
		
		//Narrows down to all bitcoin markets
		for (int i = 0; i < marketsList.size(); i++) {
			if(!marketsList.get(i).substring(0,4).equals("BTC_")) {
				marketsList.remove(i);
				i--;
			}
		}
		
		//ArrayList of tickers of all coins
		ArrayList<PoloniexTicker> pTickers = new ArrayList<PoloniexTicker>();
		for ( int i = 0; i < marketsList.size(); i++ ) {
			pTickers.add(service.returnTicker(marketsList.get(i)));
		}
		
		for ( int i = 0; i < numberOfCoins; i++ ) {
			int biggest = 0;
			int biggestIndex = 0;
			for ( int j = i; j < pTickers.size(); j++ ) {
				if ( pTickers.get(j).baseVolume.intValue() > biggest ) {
					biggest = pTickers.get(j).baseVolume.intValue();
					biggestIndex = j;
				}
			}
			PoloniexTicker original = pTickers.get(i);
			pTickers.set(i, pTickers.get(biggestIndex));
			pTickers.set(biggestIndex, original);
			String originalBTC = marketsList.get(i);
			marketsList.set(i, marketsList.get(biggestIndex));
			marketsList.set(biggestIndex, originalBTC);
		}
		
		CoinInfo[] allCoins = new CoinInfo[numberOfCoins];
		for ( int i = 0; i < allCoins.length; i++ ) {
			allCoins[i] = new CoinInfo(service, marketsList.get(i), (Double) (startingAmount / numberOfCoins));
		}
		
		while (true) {
			for ( int j = 0; j < allCoins.length; j++ ) {
				try {
					allCoins[j].update();
				} catch( Exception e ) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double amountTotal = 0;
			for ( int j = 0; j < allCoins.length; j++ ) {
				amountTotal += allCoins[j].getAmountGiven() - allCoins[j].getOriginalAmountGiven();
			}
		}
		
	}

}
