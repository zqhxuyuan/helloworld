
package com.tc.trinity.core.config;

import static com.tc.trinity.utils.CGLIBHelper.getTargetObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import com.tc.trinity.core.AbstractConfigurable;
import com.tc.trinity.core.ConfigContext;
import com.tc.trinity.core.config.EnhancedPropertyPlaceholderConfigurer.BeanData;
import com.tc.trinity.core.config.EnhancedPropertyPlaceholderConfigurer.SpringInfo;

/**
 * Spring容器配置
 * 
 * @author gaofeng
 * @date Jun 10, 2014 7:36:00 PM
 * @id $Id$
 */
public class SpringContextConfiguration extends AbstractConfigurable {
    
    private Logger logger;
    
    private final String name = "spring";
    
    @Override
    public String getName() {
    
        return this.name;
    }
    
    @Override
    public boolean doInit(ConfigContext context, Properties properties) {
    
        logger = LoggerFactory.getLogger(SpringContextConfiguration.class);
        // 因为Spring作为容器的特殊性，这个方位的逻辑委托给EnhancedPropertiesPlaceholderConfigurer类了
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean doOnChange(String key, String originalValue, String value) {
    
        Collection<SpringInfo> infos = (Collection<SpringInfo>) this.configContext.getAttribute("spring.info");
        if (infos == null || infos.size() == 0) {
            return true;
        }
        for (SpringInfo info : infos) {
            processEvalTag(info, key, originalValue, value);
            processValueAnnotation(info, key, originalValue, value);
            processBean(info, key, originalValue, value);
        }
        return true;
    }
    
    protected void processEvalTag(SpringInfo info, String key, String originalValue, String value) {
    
        ApplicationContext ac = info.getContext();
        String beanName = info.getBeanName();
        if (ac != null && StringUtils.isNotBlank(beanName)) {
            Properties p = (Properties) ac.getBean(beanName);
            p.put(key, value);
        }
    }
    
    @SuppressWarnings("rawtypes")
    protected void processValueAnnotation(SpringInfo info, String key, String originalValue, String value) {
    
        ApplicationContext ac = info.getContext();
        if (ac == null) {
            return;
        }
        
        for (String beanName : ac.getBeanDefinitionNames()) {
            try {
                Object bean = ac.getBean(beanName);
                if (bean == null) { // Explanation needed here
                    continue;
                }
                Class clazz = bean.getClass();
                if (AopUtils.isAopProxy(bean) || AopUtils.isCglibProxy(bean)) {
                    clazz = clazz.getSuperclass();
                }
                for (Field field : clazz.getDeclaredFields()) {
                    if (!field.isAnnotationPresent(Value.class))
                        continue;
                    Value v = field.getAnnotation(Value.class);
                    if (StringUtils.isNotEmpty(v.value()) && v.value().contains(key)) {
                        setBeanValue(field, bean.getClass(), bean, value);
                    }
                }
            } catch (BeansException e) {
                logger.info("cannot get specific bean from ApplicationContext" + beanName);
            }
        }
    }
    
    protected void processBean(SpringInfo info, String key, String originalValue, String value) {
    
        ApplicationContext ac = info.getContext();
        String beanName = info.getBeanName();
        Map<String, BeanData> propertyMap = info.getPropertyMap();
        if (ac == null || StringUtils.isBlank(beanName) || propertyMap == null || propertyMap.size() == 0) {
            return;
        }
        BeanData bd = propertyMap.get(key);
        if (bd != null) {
            Object bean = null;
            try {
                bean = ac.getBean(bd.getBeanName());
                if (bean != null) {
                    String fieldName = bd.getFieldName();
                    Field field = bean.getClass().getField(fieldName);
                    setBeanValue(field, bean.getClass(), bean, value);
                }
            } catch (BeansException e) {
                logger.info("cannot get specific bean from ApplicationContext" + bd.getBeanName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getSetterMethod(String fieldName) {
    
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setBeanValue(Field field, Class clazz, Object bean, String value) {
    
        if (field == null || bean == null || value == null) {
            throw new IllegalArgumentException();
        }
        
        // get the real target object from proxied one
        if (AopUtils.isAopProxy(bean) || AopUtils.isCglibProxy(bean)) {
            bean = getTargetObject(bean);
        }
        
        field.setAccessible(true);
        String fieldName = field.getName();
        String setter = getSetterMethod(fieldName);
        Class type = field.getType();
        try {
            if (type.isPrimitive()) {
                if (type == Boolean.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Boolean.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, Boolean.parseBoolean(value));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, Boolean.parseBoolean(value));
                    }
                } else if (type == Byte.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Byte.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, Byte.parseByte(value));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, Byte.parseByte(value));
                    }
                } else if (type == Character.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Character.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, value.charAt(0));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, value.charAt(0));
                    }
                } else if (type == Double.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Double.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, Double.parseDouble(value));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, Double.parseDouble(value));
                    }
                } else if (type == Float.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Float.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, Float.parseFloat(value));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, Float.parseFloat(value));
                    }
                } else if (type == Integer.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Integer.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, Integer.parseInt(value));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, Integer.parseInt(value));
                    }
                } else if (type == Long.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Long.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, Long.parseLong(value));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, Long.parseLong(value));
                    }
                } else if (type == Short.TYPE) {
                    try {
                        Method m = clazz.getDeclaredMethod(setter, Short.TYPE);
                        m.setAccessible(true);
                        m.invoke(bean, Short.parseShort(value));
                    } catch (Exception e) {
                        logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                        field.set(bean, Short.parseShort(value));
                    }
                } else {
                    throw new IllegalStateException("Unknown primitive type: " + type.getName());
                }
            } else if (type == Boolean.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Boolean.class);
                    m.setAccessible(true);
                    m.invoke(bean, Boolean.parseBoolean(value));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, Boolean.parseBoolean(value));
                }
            } else if (type == Byte.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Byte.class);
                    m.setAccessible(true);
                    m.invoke(bean, Byte.parseByte(value));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, Byte.parseByte(value));
                }
            } else if (type == Character.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Character.class);
                    m.setAccessible(true);
                    m.invoke(bean, value.charAt(0));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, value.charAt(0));
                }
            } else if (type == Double.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Double.class);
                    m.setAccessible(true);
                    m.invoke(bean, Double.parseDouble(value));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, Double.parseDouble(value));
                }
            } else if (type == Float.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Float.class);
                    m.setAccessible(true);
                    m.invoke(bean, Float.parseFloat(value));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, Float.parseFloat(value));
                }
            } else if (type == Integer.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Integer.class);
                    m.setAccessible(true);
                    m.invoke(bean, Integer.parseInt(value));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, Integer.parseInt(value));
                }
            } else if (type == Long.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Long.class);
                    m.setAccessible(true);
                    m.invoke(bean, Long.parseLong(value));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, Long.parseLong(value));
                }
            } else if (type == Short.class) {
                try {
                    Method m = clazz.getDeclaredMethod(setter, Short.class);
                    m.setAccessible(true);
                    m.invoke(bean, Short.parseShort(value));
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, Short.parseShort(value));
                }
            }
            else {
                try {
                    Method m = clazz.getDeclaredMethod(setter, String.class);
                    m.setAccessible(true);
                    m.invoke(bean, value);
                } catch (Exception e) {
                    logger.info("cannot find correspond setter method " + fieldName + ", Field.set will be tried instead.");
                    field.set(bean, value);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.warn("Error when setting value by invoking Field.set(Object) " + fieldName);
            e.printStackTrace();
        }
    }
    
    @Override
    public String getExtensionName() {
    
        return this.getClass().getName();
    }
}
