package com.csa.exchangerate.repository;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import com.csa.exchangerate.model.ExchangeRate;

public interface ExchangeRateRepository {

	List<ExchangeRate> findAll(Currency from, Currency to);
	
	Optional<ExchangeRate> findLatest(Currency from, Currency to);
	
	Optional<ExchangeRate> findForDate(Currency from, Currency to, LocalDate date);
	
	ExchangeRate save(ExchangeRate exchangeRate);
	
}
