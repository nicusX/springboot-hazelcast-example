package com.opencredo.examples.hzchat;

import com.hazelcast.core.HazelcastInstance;
import com.opencredo.examples.hzchat.domain.ChatService;
import com.opencredo.examples.hzchat.domain.TransactionalChatServiceImpl;
import org.mpilone.hazelcastmq.spring.tx.HazelcastTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for transactional polling
 */
@Configuration
@EnableTransactionManagement
@Profile("transactional-polling") // Used only when "transactional-polling profile is defined
public class TransactionalConfig {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionalConfig.class);

    // ChatService implementing transactional polling
    @Autowired
    @Bean
    public ChatService transactionalChatService(HazelcastInstance instance) {
        LOG.info("Starting ChatService implementing transactional polling");
        return new TransactionalChatServiceImpl(instance);
    }

    // Configure the Hazelcast transaction manager
    @Autowired
    @Bean
    public HazelcastTransactionManager hazelcastTransactionManager(HazelcastInstance instance) {
        return new HazelcastTransactionManager(instance);
    }

}
