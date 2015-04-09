
package com.tc.trinity.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.tc.trinity.configclient.DynamicConfigurer;
import com.tc.trinity.core.spi.Configurable;
import com.tc.trinity.core.spi.RemoteConfigClient;

/**
 * <p>
 * 抽象的配置引擎初始化工作类。
 * <ul>
 * <li>处理SPI的加载
 * <li>回调所有的Configurable实现类的init动作
 * </ul>
 * 
 * @author gaofeng
 * @date Jun 10, 2014 5:11:18 PM
 * @id $Id$
 */
public abstract class ConfigInitializer implements LifeCycle {
    
    protected RemoteConfigClient configClient;
    
    @Override
    public void start(ConfigContext context) {
    
        try {
            // load all configurable modules from ServiceLoader
            Iterable<Configurable> configModules = ExtensionLoader.loadConfigurable();
            
            Properties globalProperties = mergeProperties();
            // especially useful for LOG module
            for (Configurable configurable : configModules) {
                configurable.fallbackSetting(globalProperties);
            }
            
            this.configClient = initConfigClient(context, globalProperties);
            context.setConfigClient(this.configClient);
            Properties remoteProperties = this.configClient.getConfig();
            
            // override remote settings with local settings(properties defined in config.properties or specified by -D
            // option)
            Object env = globalProperties.get(RemoteConfigClient.ENVIRONMENT);
            // 对product的环境，排除本地配置覆盖
            if (env == null || !"product".equalsIgnoreCase(env.toString())) {
                remoteProperties.putAll(globalProperties);
            }
            globalProperties = remoteProperties;
            
            for (Configurable configModule : configModules) {
                
                if (configModule.checkValidity()) {
                    configModule.init(context, globalProperties);
                    context.register(configModule);
                } else {
                    System.err.println(configModule + " is not valid, and won't be registered.");
                }
                
            }
            
        } catch (IOException | TrinityException e) {
            System.err.println("config envioronment initialization error");
            throw new IllegalStateException(e);
        }
        
    }
    
    protected RemoteConfigClient initConfigClient(ConfigContext context, Properties properties) throws IOException, TrinityException {
    
        this.configClient = ExtensionLoader.loadConfigClient();
        
        if (this.configClient == null) {
            throw new IllegalStateException("No RemoteConfigClient is specified");
        }
        this.configClient.init(properties);
        this.configClient.register(new DynamicConfigurer(context));
        
        return this.configClient;
    }
    
    /**
     * 合并配置信息
     * <p>
     * 优先级：-D参数指定的配置 ==覆盖=> config.properties中的配置
     *
     * @return
     * @throws TrinityException
     */
    protected Properties mergeProperties() throws TrinityException {
    
        Properties properties = new Properties();
        InputStream inputStream = this.getClass().getResourceAsStream(Constants.CONFIG_FILE);
        if (inputStream != null) {
            try {
                properties.load(new LineTrimInputStream(inputStream));
                inputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        } else {
            String errMsg = "trinity - 'config.properties' was not found. ";
            throw new TrinityException(errMsg);
        }
        
        properties.putAll(System.getProperties());
        return properties;
    }
    
}
