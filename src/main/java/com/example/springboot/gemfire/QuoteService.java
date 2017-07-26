package com.example.springboot.gemfire;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unused")
public class QuoteService {

    protected static final String ID_BASED_QUOTE_SERVICE_URL = "http://gturnquist-quoters.cfapps.io/api/{id}";
    protected static final String RANDOM_QUOTE_SERVICE_URL = "http://gturnquist-quoters.cfapps.io/api/random";

    private volatile boolean cacheMiss = false;

    private RestTemplate quoteServiceTemplate = new RestTemplate();

    public boolean isCacheMiss() {
        boolean cacheMiss = this.cacheMiss;
        this.cacheMiss = false;
        return cacheMiss;
    }

    protected void setCacheMiss() {
        this.cacheMiss = true;
    }

    @Cacheable("Quotes")
    public Quote requestQuote(Long id) {
        setCacheMiss();
        return requestQuote(ID_BASED_QUOTE_SERVICE_URL, Collections.singletonMap("id", id));
    }

    @CachePut(cacheNames = "Quotes", key = "#result.id")
    public Quote requestRandomQuote() {
        setCacheMiss();
        return requestQuote(RANDOM_QUOTE_SERVICE_URL);
    }

    protected Quote requestQuote(String URL) {
        return requestQuote(URL, Collections.emptyMap());
    }

    protected Quote requestQuote(String URL, Map<String, Object> urlVariables) {
        QuoteResponse quoteResponse = quoteServiceTemplate.getForObject(URL, QuoteResponse.class, urlVariables);
        return quoteResponse.getQuote();
    }

}
