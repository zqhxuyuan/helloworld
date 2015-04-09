
package com.tc.trinity.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringValueResolver;

import com.tc.trinity.core.ConfigContext;
import com.tc.trinity.core.TrinityException;

/**
 * <p>
 * 增强的占位符变量替换Bean
 * 
 * <p>
 * 合并了原先PropertyPlaceholderConfigurer和PropertiesFactoryBean的功能，对应于XSD 3.1之后，
 * context:property-placeholder和util:properties配置项的功能。
 * 
 * <p>
 * 增强的特性包含：
 * <ul>
 * <li>1. 可以在原来本地变量注入基础上，以fallback方式追加从远端配置服务器获取变量的能力
 * <li>2. 对以_dynamic结尾的占位符变量（i.e. ${property_key<em>_dynamic</em> ），增加动态属性变化效果
 * </ul>
 * 
 * @author kozz.gaof
 * @date May 14, 2014 8:55:43 PM
 * @id $Id$
 */
public abstract class EnhancedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer
        implements FactoryBean<Properties>, InitializingBean, BeanNameAware, ApplicationContextAware {
    
    protected ApplicationContext context;
    
    protected String beanName;
    
    private Properties innerProp;
    
    private Properties externalProp;
    
    private ConfigContext configContext;
    
    private Map<String, BeanData> propertyMap = new HashMap<String, BeanData>();
    
    @Override
    public final String resolvePlaceholder(String placeholder, Properties props) {
    
        try {
            Object value = props.get(placeholder);
            if (value == null) {
                return this.configContext.getConfigClient().getValue(placeholder);
            }
            return value.toString();
        } catch (TrinityException e) {
            throw new BeanCreationException("Failed to resolve property, key: " + placeholder, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
            StringValueResolver valueResolver) {
    
        BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);
        
        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (String curName : beanNames) {
            // Check that we're not parsing our own bean definition,
            // to avoid failing on unresolvable placeholders in properties file locations.
            if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(context))) {
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
                try {
                    // cache beanName and fieldName
                    MutablePropertyValues mpvs = bd.getPropertyValues();
                    PropertyValue[] pvs = mpvs.getPropertyValues();
                    if (pvs != null) {
                        for (PropertyValue pv : pvs) {
                            Object value = pv.getValue();
                            if (value instanceof TypedStringValue) {
                                String value_ = ((TypedStringValue) value).getValue();
                                if (value_.startsWith(this.placeholderPrefix)
                                        && value_.endsWith(this.placeholderSuffix)) {
                                    value_ = value_.substring(2);
                                    value_ = value_.substring(0, value_.length() - 1);
                                    this.propertyMap.put(value_, new BeanData(curName, pv.getName()));
                                }
                            }
                        }
                    }
                    visitor.visitBeanDefinition(bd);
                } catch (Exception ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex);
                }
            }
        }
        
        // New in Spring 2.5: resolve placeholders in alias target names and aliases as well.
        beanFactoryToProcess.resolveAliases(valueResolver);
        
        // New in Spring 3.0: resolve placeholders in embedded values such as annotation attributes.
        beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
        
        // to support multiple Spring ApplicationContext containers
        synchronized (this.configContext) {
            Collection<SpringInfo> info = (Collection<SpringInfo>) this.configContext.getAttribute("spring.info");
            if (info == null) {
                info = new ArrayList<SpringInfo>();
            }
            info.add(new SpringInfo(this.context, this.beanName, this.propertyMap));
            configContext.setAttribute("spring.info", info);
        }
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
    
        // innerProp = this.mergeProperties();
        configContext = getConfigContext();
        if(configContext == null){
            throw new TrinityException("No ConfigContext be set!");
        }
        externalProp = configContext.getConfigClient().getConfig();
    }
    
    @Override
    public Properties getObject() throws Exception {
    
        Properties prop = externalProp;
        prop.putAll(this.mergeProperties());
        return prop;
    }
    
    public abstract ConfigContext getConfigContext();
    
    @Override
    public Class<?> getObjectType() {
    
        return Properties.class;
    }
    
    @Override
    public boolean isSingleton() {
    
        return true;
    }
    
    @Override
    public void setBeanName(String beanName) {
    
        super.setBeanName(beanName);
        this.beanName = beanName;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    
        super.setBeanFactory(applicationContext);
        this.context = applicationContext;
    }
    
    class BeanData {
        
        private String beanName;
        private String fieldName;
        
        public BeanData(String beanName, String fieldName) {
        
            this.beanName = beanName;
            this.fieldName = fieldName;
        }
        
        /**
         * @return the beanName
         */
        public String getBeanName() {
        
            return beanName;
        }
        
        /**
         * @param beanName
         *            the beanName to set
         */
        public void setBeanName(String beanName) {
        
            this.beanName = beanName;
        }
        
        /**
         * @return the fieldName
         */
        public String getFieldName() {
        
            return fieldName;
        }
        
        /**
         * @param fieldName
         *            the fieldName to set
         */
        public void setFieldName(String fieldName) {
        
            this.fieldName = fieldName;
        }
    }
    
    class SpringInfo {
        
        private ApplicationContext context;
        private String beanName;
        private Map<String, BeanData> propertyMap = new HashMap<String, BeanData>();
        
        public ApplicationContext getContext() {
        
            return context;
        }
        
        public void setContext(ApplicationContext context) {
        
            this.context = context;
        }
        
        public String getBeanName() {
        
            return beanName;
        }
        
        public void setBeanName(String beanName) {
        
            this.beanName = beanName;
        }
        
        public Map<String, BeanData> getPropertyMap() {
        
            return propertyMap;
        }
        
        public void setPropertyMap(Map<String, BeanData> propertyMap) {
        
            this.propertyMap = propertyMap;
        }
        
        SpringInfo(ApplicationContext context, String beanName, Map<String, BeanData> propertyMap) {
        
            this.context = context;
            this.beanName = beanName;
            this.propertyMap = propertyMap;
        }
    }
}
