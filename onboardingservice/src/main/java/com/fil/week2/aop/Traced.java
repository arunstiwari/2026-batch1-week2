package com.fil.week2.aop;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Traced {

    /**
     * Label used in the log line.
     */
    String value() default "";

    /** whether to log the parameter on entry or not     */
    boolean logArgs() default false;

    /**  Anything slower than this is logged at WARN level instead of INFO    */
    long slowMillis() default 500;
}
