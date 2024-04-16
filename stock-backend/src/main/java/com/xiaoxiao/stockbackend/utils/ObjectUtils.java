package com.xiaoxiao.stockbackend.utils;

import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 提供一些对象转数组已经数据转对象的方法
 */
@Component
public class ObjectUtils {

    /**
     * Object[] ---> java Bean
     * @param array 带转换的数组
     * @param clazz Java Bean Class对象
     * @return 转换好的 JavaBean
     * @param <T> Bean 的类型
     */
    public <T> T objectArrayToObject(Object[] array, Class<T> clazz) {
        if (array == null || array.length == 0) {
            return null;
        }
        Class<?>[] tClass = null;
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == array.length) {
                tClass = parameterTypes;
                break;
            }
        }
        try {
            return clazz.getConstructor(tClass).newInstance(array);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
