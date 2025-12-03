package com.tricoq.infrastructure.adapter.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * mybatis自动填充handler
 *
 * @author hx qiang
 * @date 8/11/25
 */
@Component
@RequiredArgsConstructor
public class MybatisPlusFillHandler implements MetaObjectHandler {

    private final static String CREATE_TIME = "createTime";
    private final static String UPDATE_TIME = "updateTime";
    private final static String STATUS = "status";

    /**
     * 插入元对象字段填充（用于插入时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(CREATE_TIME, LocalDateTime.now(), metaObject);
        this.setFieldValByName(STATUS, 1, metaObject);
    }

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(UPDATE_TIME, LocalDateTime.now(), metaObject);
    }
}
