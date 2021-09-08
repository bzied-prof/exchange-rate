package com.csa.exchangerate.controller;

import static com.csa.exchangerate.model.ApiErrorBuilder.newError;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.csa.exchangerate.model.ApiError;
import com.csa.exchangerate.model.ApiErrorBuilder;
import com.csa.exchangerate.model.CurrencyCode;
import com.csa.exchangerate.model.ExchangeRate;
import com.csa.exchangerate.service.ExchangeRateService;

/**
 * @author Brian A Zied
 *
 * NOTE:  Validation framework could be cleaned up, however, this was the quickest solution 
 * given the time constraints.
 * 
 */
@RestController
@RequestMapping("/currency")
@Validated
public class ExchangeRateController {
	
	private static final int TIMEOUT = 10;
	
	@Autowired
	private ExchangeRateService exchageRateService;
	
	/**
	 * This method will set the exchange rate for 2 currency codes for specified date.
	 * It will attempt to lookup the currency codes (or throw an @IllegalArgumentException
	 * if not found), then set the exchange rate.
	 * We have a constraint of 10ms so will run the exchange rate service asynchronously
	 * and throw @TimeoutException if service fails to complete within 10ms constraint
	 * 
	 * @param exchangeRate
	 * @return exchange rate
	 */
	@PostMapping
	public ResponseEntity<ExchangeRate> setExchangeRate(@RequestParam @Valid ExchangeRate exchangeRate)  throws InterruptedException, ExecutionException, TimeoutException {
		
		// Check if from/to equal
		if (exchangeRate.getFrom().equalsIgnoreCase(exchangeRate.getTo())) {
			throw new IllegalArgumentException("From/To must be different currency codes");
		}
			
		// Since it's a single task just use a single thread executor 
		Callable<ExchangeRate> setExchangeRate = () -> { return exchageRateService.setExchangeRate(exchangeRate); };		
		Future<ExchangeRate> future = Executors.newSingleThreadExecutor().submit(setExchangeRate);
		future.get(TIMEOUT, TimeUnit.MILLISECONDS);
		
		// Return saved exchange rate
		return ResponseEntity.ok(exchangeRate);
	}
	
	/**
	 * 
	 * This method will get the latest exchange rate for 2 currency codes.
	 * It will attempt to lookup the currency codes (or throw an @IllegalArgumentException
	 * if not found), then lookup the exchange rate history.
	 * We have a constraint of 10ms so will run the exchange rate service asynchronously
	 * and throw @TimeoutException if service fails to complete within 10ms constraint
	 * 
	 * @param fromCurrencyCode
	 * @param toCurrencyCode
	 * @return Exchange rate history
	 */
	@GetMapping
	public ResponseEntity<List<ExchangeRate>> getExchangeRateHistory(
			@RequestParam @CurrencyCode String fromCurrencyCode, 
			@RequestParam @CurrencyCode String toCurrencyCode
		) throws InterruptedException, ExecutionException, TimeoutException {
		
		// Check if from/to equal
		if (fromCurrencyCode.equalsIgnoreCase(toCurrencyCode)) {
			throw new IllegalArgumentException("From/To must be different currency codes");
		}
			
		// Convert currency
		Currency from = Currency.getInstance(fromCurrencyCode.toUpperCase());
		Currency to = Currency.getInstance(toCurrencyCode.toUpperCase());
		
		// Since it's a single task just use a single thread executor 
		Callable<List<ExchangeRate>> getExchangeRateHistory = () -> { return exchageRateService.getExchangeRateHistory(from, to); };		
		Future<List<ExchangeRate>> future = Executors.newSingleThreadExecutor().submit(getExchangeRateHistory);
		List<ExchangeRate> exchangeRates = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
		
		// Return exchange rates (if any)
		return ResponseEntity.ok(exchangeRates);
	}

	/**
	 * This method will get the latest exchange rate for 2 currency codes.
	 * It will attempt to lookup the currency codes (or throw an @IllegalArgumentException
	 * if not found), then lookup the exchange rate history.
	 * We have a constraint of 10ms so will run the exchange rate service asynchronously
	 * and throw @TimeoutException if service fails to complete within 10ms constraint
	 * 
	 * @param fromCurrencyCode
	 * @param toCurrencyCode
	 * @return Exchange rate, or HTTP 404
	 */
	@GetMapping("/latest")
	public ResponseEntity<ExchangeRate> getLatestExchangeRate(
			@RequestParam @CurrencyCode String fromCurrencyCode, 
			@RequestParam @CurrencyCode String toCurrencyCode
		) throws InterruptedException, ExecutionException, TimeoutException {
		
		// Check if from/to equal
		if (fromCurrencyCode.equalsIgnoreCase(toCurrencyCode)) {
			throw new IllegalArgumentException("From/To must be different currency codes");
		}
			
		// Convert currency
		Currency from = Currency.getInstance(fromCurrencyCode.toUpperCase());
		Currency to = Currency.getInstance(toCurrencyCode.toUpperCase());
		
		// Since it's a single task just use a single thread executor 
		Callable<Optional<ExchangeRate>> getLatestExchangeRate = () -> { return exchageRateService.getLatestExchangeRate(from, to); };		
		Future<Optional<ExchangeRate>> future = Executors.newSingleThreadExecutor().submit(getLatestExchangeRate);
		Optional<ExchangeRate> exchangeRate = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
		
		// If an exchange rate is found, return it, otherwise return HTTP 404 Not Found
		return exchangeRate.isPresent() ? ResponseEntity.ok(exchangeRate.get()) : ResponseEntity.notFound().build();
	}

	/**
	 * This method will get the exchange rate for 2 currency codes on a specific ISO date (i.e. yyyy-MM-dd ).
	 * It will attempt to lookup the currency codes (or throw an @IllegalArgumentException
	 * if not found), then lookup the exchange rate history.
	 * We have a constraint of 10ms so will run the exchange rate service asynchronously
	 * and throw @TimeoutException if service fails to complete within 10ms constraint
	 * 
	 * @param isoDate
	 * @param fromCurrencyCode
	 * @param toCurrencyCode
	 * @return Exchange rate, or HTTP 404
	 */
	@GetMapping("/{isoDate}")
	public ResponseEntity<ExchangeRate> getExchangeRateForDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate isoDate, 
			@RequestParam @CurrencyCode String fromCurrencyCode, 
			@RequestParam @CurrencyCode String toCurrencyCode
		)  throws InterruptedException, ExecutionException, TimeoutException {
		
		// Check if from/to equal
		if (fromCurrencyCode.equalsIgnoreCase(toCurrencyCode)) {
			throw new IllegalArgumentException("From/To must be different currency codes");
		}
			
		// Convert currency
		Currency from = Currency.getInstance(fromCurrencyCode.toUpperCase());
		Currency to = Currency.getInstance(toCurrencyCode.toUpperCase());
		
		// Since it's a single task just use a single thread executor 
		Callable<Optional<ExchangeRate>> getExchangeRateForDate = () -> { return exchageRateService.getExchangeRateForDate(from, to, isoDate); };		
		Future<Optional<ExchangeRate>> future = Executors.newSingleThreadExecutor().submit(getExchangeRateForDate);
		Optional<ExchangeRate> exchangeRate = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
		
		// If an exchange rate is found, return it, otherwise return HTTP 404 Not Found
		return exchangeRate.isPresent() ? ResponseEntity.ok(exchangeRate.get()) : ResponseEntity.notFound().build();
	}
	
	/**
	 * Exception Handlers.  The methods will package the exceptions into an ApiError
	 */
	
	@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
		ApiErrorBuilder builder = newError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(builder.build());
    }

	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
		ApiErrorBuilder builder = newError("Invalid arguments");
		ex.getBindingResult().getAllErrors().forEach((error) -> {
	        String fieldName = ((FieldError) error).getField();
	        String errorMessage = error.getDefaultMessage();
	        builder.andFieldError(fieldName, errorMessage);
	    });
		return ResponseEntity.badRequest().body(builder.build());
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiError> handleTimeoutException() {
    	ApiError apiError = newError("Service hasn't responded in time, please try again later.").build();
    	return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiError);
    }

    @ExceptionHandler({ InterruptedException.class, ExecutionException.class })
    public ResponseEntity<ApiError> handleInterruptedAndExecutionException() {
    	ApiError apiError = newError("Unexpected error, please try again later.").build();
    	return ResponseEntity.internalServerError().body(apiError);
    }

}
