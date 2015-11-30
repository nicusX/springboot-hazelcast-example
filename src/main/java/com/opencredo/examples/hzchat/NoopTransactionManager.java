package com.opencredo.examples.hzchat;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Dummy transaction manager.
 * It does nothing, but is used for testing the non-transactional version of the application.
 */
public class NoopTransactionManager extends AbstractPlatformTransactionManager {
    private static final long serialVersionUID = 9118946071248553345L;

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new Object();
    }

    @Override
    protected void doBegin(Object o, TransactionDefinition transactionDefinition) throws TransactionException {
        // Do nothing
    }

    @Override
    protected void doCommit(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
        // Do nothing
    }

    @Override
    protected void doRollback(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
        // Do noting
    }
}
