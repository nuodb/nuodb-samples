/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dal;

import java.util.concurrent.Callable;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl;

public class BaseDao extends GeneralDAOImpl implements IBaseDao {

    public void runTransaction(TransactionType transactionType, String name, final Runnable r) {
        runTransaction(transactionType, name, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                r.run();
                return null;
            }
        });
    }

    public <T> T runTransaction(TransactionType transactionType, String name, Callable<T> c) {
        long startTime = System.currentTimeMillis();

        Session session = getSession();
        Transaction t;
        try {
            t = session.beginTransaction();
        } catch (RuntimeException e) {
            try {
                session.close();
            } catch (RuntimeException ei) {
            }
            throw e;
        }

        try {
            prepareSession(transactionType);
            T result = c.call();
            t.commit();
            onTransactionComplete(name, startTime, true);
            return result;
        } catch (Exception e) {
            t.rollback();
            onTransactionComplete(name, startTime, false);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    protected void prepareSession(TransactionType transactionType) {
        switch (transactionType) {
            case READ_ONLY:
                Session session = getSession();

                // FIXME: Can't mark transaction as read-only with NuoDB right
                // now, or SQL exceptions get thrown even with select statements
                // session.doWork(new Work() {
                // @Override
                // public void execute(Connection connection) throws
                // SQLException {
                // connection.setReadOnly(true);
                // }
                // });

                session.setFlushMode(FlushMode.MANUAL);
                break;

            default:
                break;
        }
    }

    protected void onTransactionComplete(String transactionName, long startTimeMs, boolean success) {
    }
}
