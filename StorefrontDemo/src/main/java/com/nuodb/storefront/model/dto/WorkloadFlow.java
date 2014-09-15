/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ FIELD })
@Retention(RUNTIME)
public @interface WorkloadFlow {
    WorkloadStep[] steps() default {};
}
