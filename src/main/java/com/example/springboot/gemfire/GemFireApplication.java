package com.example.springboot.gemfire;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.GemFireCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.support.GemfireCacheManager;

import java.util.Properties;

@SpringBootApplication
@EnableCaching
@EnableGemfireRepositories
@SuppressWarnings("unused")
public class GemFireApplication implements CommandLineRunner {

    @Autowired
    PersonRepository personRepository;

    @Bean
    QuoteService quoteService() {
        return new QuoteService();
    }

    @Bean
    Properties gemfireProperties() {
        Properties gemfireProperties = new Properties();
        gemfireProperties.setProperty("name", "DataGemFireCachingApplication");
        gemfireProperties.setProperty("mcast-port", "0");
        gemfireProperties.setProperty("log-level", "config");
        return gemfireProperties;
    }

    @Bean
    CacheFactoryBean gemfireCache() {
        CacheFactoryBean gemfireCache = new CacheFactoryBean();
        gemfireCache.setClose(true);
        gemfireCache.setProperties(gemfireProperties());
        return gemfireCache;
    }

    @Bean
    LocalRegionFactoryBean<Integer, Integer> quotesRegion(GemFireCache cache) {
        LocalRegionFactoryBean<Integer, Integer> quotesRegion = new LocalRegionFactoryBean<>();
        quotesRegion.setCache(cache);
        quotesRegion.setClose(false);
        quotesRegion.setName("Quotes");
        quotesRegion.setPersistent(false);
        return quotesRegion;
    }

    @Bean
    LocalRegionFactoryBean<String, Person> helloRegion(final GemFireCache cache) {
        LocalRegionFactoryBean<String, Person> helloRegion = new LocalRegionFactoryBean<>();
        helloRegion.setCache(cache);
        helloRegion.setClose(false);
        helloRegion.setName("hello");
        helloRegion.setPersistent(false);
        return helloRegion;
    }

    @Bean
    GemfireCacheManager cacheManager(Cache gemfireCache) {
        GemfireCacheManager cacheManager = new GemfireCacheManager();
        cacheManager.setCache(gemfireCache);
        return cacheManager;
    }

    public static void main(String[] args) {
        SpringApplication.run(GemFireApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Quote quote = requestQuote(12l);
        requestQuote(quote.getId());
        requestQuote(10l);
        requestQuote(null);

        Person alice = new Person("Alice", 40);
        Person bob = new Person("Baby Bob", 1);
        Person carol = new Person("Teen Carol", 13);

        System.out.println("Before linking up with Gemfire...");
        for (Person person : new Person[] { alice, bob, carol }) {
            System.out.println("\t" + person);
        }

        personRepository.save(alice);
        personRepository.save(bob);
        personRepository.save(carol);

        System.out.println("Lookup each person by name...");
        for (String name : new String[] { alice.name, bob.name, carol.name }) {
            System.out.println("\t" + personRepository.findByName(name));
        }

        System.out.println("Adults (over 18):");
        for (Person person : personRepository.findByAgeGreaterThan(18)) {
            System.out.println("\t" + person);
        }

        System.out.println("Babies (less than 5):");
        for (Person person : personRepository.findByAgeLessThan(5)) {
            System.out.println("\t" + person);
        }

        System.out.println("Teens (between 12 and 20):");
        for (Person person : personRepository.findByAgeGreaterThanAndAgeLessThan(12, 20)) {
            System.out.println("\t" + person);
        }

    }

    private Quote requestQuote(Long id) {
        QuoteService quoteService = quoteService();
        long startTime = System.currentTimeMillis();
        Quote quote = (id != null ? quoteService.requestQuote(id) : quoteService.requestRandomQuote());
        long elapsedTime = System.currentTimeMillis();
        System.out.printf("\"%1$s\"%nCache Miss [%2$s] - Elapsed Time [%3$s ms]%n", quote,
                quoteService.isCacheMiss(), (elapsedTime - startTime));
        return quote;
    }
}
