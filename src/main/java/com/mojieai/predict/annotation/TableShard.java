package com.mojieai.predict.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TableShard {

    String tableName();

    String shardType();

    String shardBy();
}
