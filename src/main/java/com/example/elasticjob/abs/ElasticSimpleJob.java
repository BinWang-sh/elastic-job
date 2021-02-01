package com.example.elasticjob.abs;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSimpleJob {

    @AliasFor("cron")
    public abstract String value() default "";

    @AliasFor("value")
    public abstract String cron() default "";

    public abstract String jobName() default "";

    public abstract int shardingTotalCount() default 1;

    public abstract String shardingItemParameter() default "";

    public abstract String jobParameter() default "";

    public abstract String description() default "";

    public abstract boolean disabled() default false;

    public abstract boolean overwrite() default false;

    public abstract boolean failover() default true;

    public abstract boolean monitorExecution() default true;
}