package com.init.config;

import com.init.redis.RedisSessionDao;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.ShiroWebConfiguration;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Import(ShiroWebConfiguration.class)
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shirFilter( @Qualifier("shiroSecurityManager") SecurityManager securityManager) {
        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/read", "anon");
        filterChainDefinitionMap.put("/logout", "anon");
        filterChainDefinitionMap.put("/**", "user");
        filterFactoryBean.setLoginUrl("/login");
        filterFactoryBean.setSuccessUrl("/read");
        filterFactoryBean.setUnauthorizedUrl("/login");
        filterFactoryBean.setSecurityManager(securityManager);
        filterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return filterFactoryBean;
    }

    @Bean
    public EhCacheManager getCacheManager(){
        EhCacheManager ehCacheManager=new EhCacheManager();
        ehCacheManager.setCacheManagerConfigFile("classpath:ehcache-local.xml");
        return ehCacheManager;
    }

    @Bean
    public RememberMeManager getRemember(){
        CookieRememberMeManager cookieRememberMeManager= new CookieRememberMeManager();
        cookieRememberMeManager.setCipherKey(Base64.getDecoder().decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }

    @Bean("redisSessionManager")
    public SessionManager getSessionManager(RedisSessionDao sessionDAO){
        /*
        java.lang.IllegalStateException: The org.apache.shiro.web.session.mgt.DefaultWebSessionManager implementation only supports validating Session implementations of the org.apache.shiro.session.mgt.ValidatingSession interface.  Please either implement this interface in your session implementation or override the org.apache.shiro.session.mgt.AbstractValidatingSessionManager.doValidate(Session) method to perform validation.

         */
        DefaultWebSessionManager defaultSessionManager=new DefaultWebSessionManager(){
            protected void doValidate(Session session) throws InvalidSessionException {
                //TODO  需要实现 测试先忽略掉  后遗症未知
            }
        };
        defaultSessionManager.setSessionIdUrlRewritingEnabled(false); //禁止url重写 将sessionid 显示在url中
        defaultSessionManager.setSessionDAO(sessionDAO);
        return defaultSessionManager;
    }


    @Bean("shiroSecurityManager")
    public SecurityManager securityManager(Realm realm,EhCacheManager ehCacheManager,RememberMeManager rememberMeManager, @Qualifier("redisSessionManager") SessionManager  sessionManager){
        DefaultWebSecurityManager securityManager =  new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setCacheManager(ehCacheManager);
        securityManager.setRememberMeManager(rememberMeManager);
        securityManager.setSessionManager(sessionManager);
        return securityManager;
    }

}
