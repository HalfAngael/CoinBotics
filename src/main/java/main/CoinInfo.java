package main;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.cf.client.poloniex.PoloniexExchangeService;
import com.cf.data.model.poloniex.PoloniexOrderResult;
import com.cf.data.model.poloniex.PoloniexTicker;

public class CoinInfo {
	
	private String coinName;
	private double amountGiven;
	private double originalAmountGiven;
	private PoloniexExchangeService polo;
	private BigDecimal[] last5Transactions = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
	PoloniexTicker ticker = null;
	private BigDecimal amountBoughtFor;
	private boolean bought;
	
	public CoinInfo(PoloniexExchangeService polo, String coinName, double amountGiven) {
		this.coinName = coinName;
		DecimalFormat numberFormat = new DecimalFormat("#.0000000");
		String amountGivenS = numberFormat.format(amountGiven);
		this.amountGiven = Double.parseDouble(amountGivenS);
		this.originalAmountGiven = Double.parseDouble(amountGivenS);
		this.polo = polo;
	}
	
	public String getCoinName() {
		return coinName;
	}
	
	public double getAmountGiven() {
		return amountGiven;
	}
	
	public double getOriginalAmountGiven() {
		return originalAmountGiven;
	}
	
	public void update() {
		System.out.println(amountGiven);
		ticker = polo.returnTicker(coinName);
		for (int i = last5Transactions.length - 2; i >= 0; i--) {
			last5Transactions[i+1] = last5Transactions[i];
		}
		last5Transactions[0] = ticker.last;
		if ( !bought && last5Transactions[0].doubleValue() > last5Transactions[1].doubleValue() && last5Transactions[1].doubleValue() > last5Transactions[2].doubleValue() ) {
			buy();
		}
		else if ( bought ) {
			if ( last5Transactions[0].doubleValue() < last5Transactions[1].doubleValue() && last5Transactions[1].doubleValue() < last5Transactions[2].doubleValue() && amountBoughtFor.doubleValue()*1.1 < last5Transactions[0].doubleValue()) {
				sell();
			}
			else if ( amountBoughtFor.doubleValue() > last5Transactions[0].doubleValue() ) {
				sell();
			}
		}
	}
	
	private void buy() {
		String currencyPair = coinName;
		BigDecimal buyPrice = ticker.last;
		BigDecimal amount = BigDecimal.valueOf(amountGiven / ticker.last.doubleValue());
		boolean fillOrKill = true;
		boolean immediateOrCancel = false;
		boolean postOnly = false;
		PoloniexOrderResult buyOrderResult = polo.buy(currencyPair, buyPrice, amount, fillOrKill, immediateOrCancel, postOnly);
		if ( buyOrderResult.orderNumber != null ) {
			bought = true;
			amountBoughtFor = buyPrice;
			amountGiven -= amountGiven;
			System.out.printf("Bought %f %s for %f each!", amount, coinName.substring(4, coinName.length()), buyPrice.doubleValue());
		}
	}
	
	private void sell() {
		String currencyPair = coinName;
		BigDecimal sellPrice = ticker.last;
		BigDecimal amount = polo.returnBalance(coinName.substring(4, coinName.length())).available;
		boolean fillOrKill = true;
		boolean immediateOrCancel = false;
		boolean postOnly = false;
		PoloniexOrderResult sellOrderResult = polo.sell(currencyPair, sellPrice, amount, fillOrKill, immediateOrCancel, postOnly);
		if ( sellOrderResult.orderNumber != null ) {
			bought = false;
			amountBoughtFor = BigDecimal.ZERO;
			amountGiven += amount.doubleValue() * sellPrice.doubleValue();
			System.out.printf("Sold %f %s for %f each!", amount, coinName.substring(4, coinName.length()), sellPrice.doubleValue());
		}
	}
}