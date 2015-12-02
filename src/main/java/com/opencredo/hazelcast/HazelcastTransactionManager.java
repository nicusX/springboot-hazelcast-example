package com.opencredo.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.*;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *    This class is strongly based on HazelcastTransactionManager,
 *    from hazelcastMQ poject by Mike Pilone: https://github.com/mpilone/hazelcastmq
 * </p>
 *
 * <p>
 * A {@link PlatformTransactionManager} implementation for a single {@link HazelcastInstance}.
 * </p>
 * <p>
 * Application code is required to retrieve transactional objects (queues,
 * lists, maps, etc) via the {@link HazelcastUtils} class. Objects retrieved
 * using these utility methods will automatically be enrolled in the current
 * transaction if one is active.
 * </p>
 *
 * @author mpilone
 * @author nicus
 */
public class HazelcastTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HazelcastTransactionManager.class);

    private HazelcastInstance hazelcastInstance;

    /**
     * Constructs the transaction manager with no target Hazelcast instance. An
     * instance must be set before use.
     */
    public HazelcastTransactionManager() {
        setNestedTransactionAllowed(false);
    }

    /**
     * Constructs the transaction manager.
     *
     * @param hzInstance the HazelcastInstance to manage the transactions for
     */
    public HazelcastTransactionManager(HazelcastInstance hzInstance) {
        this();

        setHazelcastInstance(hzInstance);
        afterPropertiesSet();
    }

    /**
     * Returns the Hazelcast instance that this transaction manager is managing
     * the transactions for.
     *
     * @return the Hazelcast instance or null if one has not been set
     */
    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    /**
     * Sets the Hazelcast instance that this transaction manager is managing the
     * transactions for.
     *
     * @param hazelcastInstance the Hazelcast instance
     */
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
           this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void afterPropertiesSet() {
        if (getHazelcastInstance() == null) {
            throw new IllegalArgumentException("Property 'hazelcastInstance' is required");
        }
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        HazelcastTransactionObject txObject = new HazelcastTransactionObject();
//		txObject.setSavepointAllowed(isNestedTransactionAllowed());
        HazelcastTransactionContextHolder conHolder =
                (HazelcastTransactionContextHolder) TransactionSynchronizationManager.getResource(this.hazelcastInstance);
        txObject.setTransactionContextHolder(conHolder, false);
        return txObject;
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        HazelcastTransactionObject txObject = (HazelcastTransactionObject) transaction;
        return (txObject.getTransactionContextHolder() != null && txObject.getTransactionContextHolder().isTransactionActive());
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        LOG.debug("Begin transaction");

        if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
            throw new InvalidIsolationLevelException("Hazelcast does not support an isolation level concept");
        }

        HazelcastTransactionObject txObject = (HazelcastTransactionObject) transaction;
        TransactionContext con = null;

        try {
            if (txObject.getTransactionContextHolder() == null || txObject.getTransactionContextHolder().isSynchronizedWithTransaction()) {

                TransactionOptions txOps = new TransactionOptions();
                txOps.setTransactionType(TransactionOptions.TransactionType.LOCAL);
                if (definition.getTimeout() > 0) {
                    txOps.setTimeout(definition.getTimeout(), TimeUnit.SECONDS);
                }

                TransactionContext newCon = getHazelcastInstance().newTransactionContext(txOps);
                newCon.beginTransaction();

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Acquired TransactionContext {} for Hazelcast transaction.", newCon);
                }
                txObject.setTransactionContextHolder(new HazelcastTransactionContextHolder(newCon), true);
            }

            txObject.getTransactionContextHolder().setSynchronizedWithTransaction(true);
            txObject.getTransactionContextHolder().setTransactionActive(true);
            con = txObject.getTransactionContextHolder().getTransactionContext();

            // Bind the session holder to the thread.
            if (txObject.isNewTransactionContextHolder()) {
                TransactionSynchronizationManager.bindResource(
                        getHazelcastInstance(),
                        txObject.getTransactionContextHolder());
            }
        }
        catch (Exception ex) {
            HazelcastUtils.releaseTransactionContext(con, hazelcastInstance);
            throw new CannotCreateTransactionException("Could not create a Hazelcast transaction", ex);
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws
            TransactionException {
        HazelcastTransactionObject txObject = (HazelcastTransactionObject) status.getTransaction();
        TransactionContext con = txObject.getTransactionContextHolder().getTransactionContext();
        LOG.debug("Commit transaction");

        if (status.isDebug()) {
            LOG.trace("Committing Hazelcast transaction on TransactionContext {}.", con);
        }

        try {
            con.commitTransaction();
        }
        catch (com.hazelcast.transaction.TransactionException ex) {
            throw new TransactionSystemException("Could not commit Hazelcast transaction", ex);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        HazelcastTransactionObject txObject = (HazelcastTransactionObject) status.getTransaction();
        TransactionContext con = txObject.getTransactionContextHolder().getTransactionContext();

        if (status.isDebug()) {
            LOG.debug("Rolling back Hazelcast transaction on TransactionContext {}.", con);
        }

        try {
            con.rollbackTransaction();
        }
        catch (com.hazelcast.transaction.TransactionException ex) {
            throw new TransactionSystemException("Could not roll back Hazelcast transaction", ex);
        }
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        HazelcastTransactionObject txObject = (HazelcastTransactionObject) transaction;

        // Remove the connection holder from the thread, if exposed.
        if (txObject.isNewTransactionContextHolder()) {
            TransactionContext con = txObject.getTransactionContextHolder().
                    getTransactionContext();

            TransactionSynchronizationManager.unbindResource(this.hazelcastInstance);

            LOG.trace("Releasing Hazelcast Transaction {} after transaction.", con);

            HazelcastUtils.releaseTransactionContext(con, this.hazelcastInstance);
        }

        txObject.getTransactionContextHolder().clear();
    }

    /**
     * An object representing a managed Hazelcast transaction.
     */
    private static class HazelcastTransactionObject {

        private HazelcastTransactionContextHolder transactionContextHolder;
        private boolean newTransactionContextHolder;

        /**
         * Sets the resource holder being used to hold Hazelcast resources in the
         * transaction.
         *
         * @param transactionContextHolder the transaction context resource holder
         * @param newHolder true if the holder was created for this transaction,
         * false if it already existed
         */
        private void setTransactionContextHolder(
                HazelcastTransactionContextHolder transactionContextHolder,
                boolean newHolder) {
            this.transactionContextHolder = transactionContextHolder;
            this.newTransactionContextHolder = newHolder;
        }

        /**
         * Returns the resource holder being used to hold Hazelcast resources in the
         * transaction.
         *
         * @return the transaction context resource holder
         */
        private HazelcastTransactionContextHolder getTransactionContextHolder() {
            return transactionContextHolder;
        }

        /**
         * Returns true if the context holder was created for the current
         * transaction and false if it existed prior to the transaction.
         *
         * @return true if the holder was created for this transaction, false if it
         * already existed
         */
        private boolean isNewTransactionContextHolder() {
            return newTransactionContextHolder;
        }
    }

}
