package com.csa.exchangerate.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.csa.exchangerate.model.ExchangeRate;
import com.csa.exchangerate.util.SelfExpiringHashMap;
import com.csa.exchangerate.util.SelfExpiringMap;

/**
 * NOTE:  This isn't the most efficient method for storing exchange rates, however, i
 * this was the best implementable solution given the time constraints.
 * 
 * This repository assumes only a single exchange rate per day.  As a result, given the 3 day TTL of all
 * exchange rates, each exchange rate can only have a maximum of 3 total values.
 * 
 * Each exchange rate is saved in a map using the from->to currency as a key.
 *    (For example: USD->EUR, USD->CAD, EUR->USD and CAD->USD are all keys)
 * Each from->to key saves the exchange rate in another map using the epoch day as a key.  This allows easy
 * comparison for determining the latest exchange rate for a from->to key.
 * 
 * @author Brian A Zied
 *
 */
@Repository
public class InMemoryExchangeRateRepository implements ExchangeRateRepository {

	private Map<ExchangeRateKey, SelfExpiringMap<Long, ExchangeRate>> exchangeRates = new HashMap<>();
	
	/**
	 * This method gets the latest exchange rate history for a given from->to currency.
	 * 
	 * @param from
	 * @param to
	 * @return Exchange rate (if it exists)
	 */
	@Override
	public Optional<ExchangeRate> findLatest(Currency from, Currency to) {
		ExchangeRateKey key = ExchangeRateKey.from(from, to);		
		synchronized (exchangeRates) {
			if (exchangeRates.containsKey(key)) {
				Map<Long, ExchangeRate> rates = exchangeRates.get(key);
				ExchangeRate latestRate = null;
				long latestExchangeRateTimestamp = 0L;
				for (Long currentExchangeRateTimestamp : rates.keySet()) {
					if (currentExchangeRateTimestamp > latestExchangeRateTimestamp) {
						latestRate = rates.get(currentExchangeRateTimestamp);
					}
				}
				return Optional.of(latestRate);			
			}			
		}
		return Optional.empty();
	}
	
	/**
	 * This method gets the exchange rate history for a given from->to currency and date.
	 * 
	 * @param from
	 * @param to
	 * @param date
	 * @return Exchange rate (if it exists)
	 */
	@Override
	public Optional<ExchangeRate> findForDate(Currency from, Currency to, LocalDate date) {
		ExchangeRateKey key = ExchangeRateKey.from(from, to);
		
		synchronized (exchangeRates) {
			if (exchangeRates.containsKey(key)) {
				Map<Long, ExchangeRate> rates = exchangeRates.get(key);
				return Optional.ofNullable(rates.get(date.toEpochDay()));
			}
		}
		return Optional.empty();
	}

	/**
	 * This method gets the unsorted exchange rate history for a given from->to currency.
	 * 
	 * @param from
	 * @param to
	 * @return Exchange rate list (unsorted)
	 */
	@Override
	public List<ExchangeRate> findAll(Currency from, Currency to) {
		ExchangeRateKey key = ExchangeRateKey.from(from, to);
		
		List<ExchangeRate> rates = new ArrayList<>();
		
		synchronized (exchangeRates) {
			if (exchangeRates.containsKey(key)) {
				rates.addAll(exchangeRates.get(key).values());
			}
		}
		return rates;
	}

	/**
	 * This method saves the exchange rate to the in memory data store.
	 * 
	 * @param exchangeRate
	 * @return Exchange rate
	 */
	@Override
	public ExchangeRate save(ExchangeRate exchangeRate) {
		
		// lookup current exchange rates
		Currency from = Currency.getInstance(exchangeRate.getFrom());
		Currency to = Currency.getInstance(exchangeRate.getTo());
		ExchangeRateKey key = ExchangeRateKey.from(from, to);
		
		synchronized (exchangeRates) {
			// exchange rate hasn't been saved, so save it
			if (!exchangeRates.containsKey(key)) {
				exchangeRates.put(key, new SelfExpiringHashMap<>());
			}
			
			// save exchange rate for that day
			Map<Long, ExchangeRate> rateMap =  exchangeRates.get(key);		
			long epochDay = LocalDate.parse(exchangeRate.getReportedOn()).toEpochDay();
			rateMap.put(epochDay, exchangeRate);
		}
		
		return exchangeRate;
	}

	/**
	 * This is the key for all exchange rates.  The key is composed of the from and to
	 * currency code.
	 * 
	 * @author Brian A Zied
	 *
	 */	
	protected static class ExchangeRateKey {
		private String fromCurrencyCode;
		private String toCurrencyCode;
		
		ExchangeRateKey(String fromCurrencyCode, String toCurrencyCode) {
			this.fromCurrencyCode = fromCurrencyCode;
			this.toCurrencyCode = toCurrencyCode;
		}		
		
	    protected String getFromCurrencyCode() {
	    	return this.fromCurrencyCode;
	    }
	 
	    protected String getToCurrencyCode() {
	    	return this.toCurrencyCode;
	    }
	 
	    //Only depends on currency codes
	    @Override
	    public int hashCode() {
	        return Objects.hash(fromCurrencyCode, toCurrencyCode);
	    }
	    
	    //Compare currency codes
	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj)
	            return true;
	        if (obj == null)
	            return false;
	        if (getClass() != obj.getClass())
	            return false;
	        ExchangeRateKey key = (ExchangeRateKey) obj;
	        return fromCurrencyCode.equals(key.getFromCurrencyCode()) 
	        		&& toCurrencyCode.equals(key.getToCurrencyCode());
	    }
	    
	    static ExchangeRateKey from(Currency from, Currency to) {
			return new ExchangeRateKey(from.getCurrencyCode(), to.getCurrencyCode());
		}
		
	}
	
}
