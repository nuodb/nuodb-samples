/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dal;

import java.util.concurrent.Callable;

import com.googlecode.genericdao.dao.hibernate.GeneralDAO;

public interface IBaseDao extends GeneralDAO {

    /**
     * Invokes the {@link Runnable#run()} method within the context of a transaction. Commits upon completion, or rolls back upon exception (and then
     * throws it).
     * 
     * @param r
     *            The instance to run.
     */
    public void runTransaction(TransactionType transactionType, String name, Runnable r);

    /**
     * Invokes the {@link Callable#call()} method within the context of a transaction. Commits upon completion (and returns call's value), or rolls
     * back upon exception (and then throws it).
     * 
     * @param r
     *            The instance to call.
     */
    public <T> T runTransaction(TransactionType transactionType, String name, Callable<T> c);
}
