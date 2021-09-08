package com.csa.exchangerate.service;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csa.exchangerate.model.ExchangeRate;
import com.csa.exchangerate.repository.ExchangeRateRepository;

@Service
public class ExchangeRateService {

	@Autowired
	private ExchangeRateRepository exchangeRateRepository;
	
	public ExchangeRate setExchangeRate(ExchangeRate exchangeRate) {
		return exchangeRateRepository.save(exchangeRate);
	}
	
	public Optional<ExchangeRate> getLatestExchangeRate(Currency from, Currency to) {
		return exchangeRateRepository.findLatest(from, to);
	}
	
	public Optional<ExchangeRate> getExchangeRateForDate(Currency from, Currency to, LocalDate date) {
		return exchangeRateRepository.findForDate(from, to, date);
	}
	
	public List<ExchangeRate> getExchangeRateHistory(Currency from, Currency to) {
		return exchangeRateRepository.findAll(from, to);
	}
	
}
