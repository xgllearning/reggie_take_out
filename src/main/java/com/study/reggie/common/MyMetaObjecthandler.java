package com.study.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjecthandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充，insert");
        log.info(metaObject.toString());//里面携带当前插入的数据，originalObject
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());

        //方法一：通过RequestContextHolder获取存放在session中的用户ID
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
//        Long employee = (Long) session.getAttribute("employee");
//        System.out.println(employee);
        metaObject.setValue("createUser",BaseContext.getCurrentId());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }

    /**
     * 更新操作，自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充，update");
        log.info(metaObject.toString());////里面携带当前更新的数据
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
