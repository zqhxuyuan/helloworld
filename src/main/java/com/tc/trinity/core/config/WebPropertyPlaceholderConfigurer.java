
package com.tc.trinity.core.config;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;

import com.tc.trinity.core.ConfigContext;
import com.tc.trinity.core.Constants;

/**
 * Web启动环境中的{#link PropertyPlaceholderConfigurer}实现类
 * 
 * @author gaofeng
 * @date Jun 11, 2014 4:32:28 PM
 * @id $Id$
 */
public class WebPropertyPlaceholderConfigurer extends EnhancedPropertyPlaceholderConfigurer implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public ConfigContext getConfigContext() {
    
        if (this.context == null) {
            throw new IllegalStateException();
        }
        return (ConfigContext) ((WebApplicationContext) context).getServletContext().getAttribute(Constants.CONFIG_CONTEXT);
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
    
        this.servletContext = servletContext;
    }
    
    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
            StringValueResolver valueResolver) {
    
        super.doProcessProperties(beanFactoryToProcess, valueResolver);
        this.servletContext.setAttribute("spring.applicationContext", this.context);
        
    }
}
