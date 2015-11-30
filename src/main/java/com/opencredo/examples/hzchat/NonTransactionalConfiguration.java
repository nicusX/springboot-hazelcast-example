package com.opencredo.examples.hzchat;

import com.hazelcast.core.HazelcastInstance;
import com.opencredo.examples.hzchat.domain.ChatService;
import com.opencredo.examples.hzchat.domain.ChatServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration for non-transactional polling
 */
@Configuration
@Profile("!transactional-polling") // Not used when "transactional-polling profile is defined
public class NonTransactionalConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(NonTransactionalConfiguration.class);


    // Non-transactional ChatService implementation
    @Autowired
    @Bean
    public ChatService chatService(HazelcastInstance instance) {
        LOG.info("Starting ChatService implementing non-transactional polling");
        return new ChatServiceImpl(instance);
    }


    @Bean
    public PlatformTransactionManager transactionManager() {
        return new NoopTransactionManager();
    }
}
