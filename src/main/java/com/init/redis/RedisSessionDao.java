package com.init.redis;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@ConfigurationProperties(prefix = "session.redis")
@Slf4j
public class RedisSessionDao extends AbstractSessionDAO {

    private long sessionExpireSecond;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String SESSION_CACHE_PREFIX = "SESSION_CACHE_";

    private static final String SESSION_EHCACHE_NAMESPACE="SESSION_CACHE";

    @Autowired
    private EhCacheManager ehCacheManager;


    @Override
    public void update(Session session) throws UnknownSessionException {

        String key=getSessionKeyInRedis(session);
        ByteArrayOutputStream byteArrayOutputStream=null;
        ObjectOutputStream objectOutputStream=null;
        try {
             byteArrayOutputStream=new ByteArrayOutputStream();
             objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(session);
            String result= Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
            redisTemplate.opsForValue().set(key,result);
            redisTemplate.expire(key, sessionExpireSecond, TimeUnit.SECONDS);
            Cache cache=ehCacheManager.getCache(SESSION_EHCACHE_NAMESPACE);
            cache.put(key,result);
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        } finally {
            try {
                objectOutputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void delete(Session session) {
        String key=getSessionKeyInRedis(session);
        Cache cache=ehCacheManager.getCache(SESSION_EHCACHE_NAMESPACE);
        cache.remove(key);

        redisTemplate.delete(getSessionKeyInRedis(session));
    }

    @Override
    public Collection<Session> getActiveSessions() {  //不统计总数  redis keys容易出现性能问题  也没必要统计总数
        return Collections.emptyList();
    }


    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        this.update(session);
        return sessionId;
    }


    @Override
    protected Session doReadSession(Serializable sessionId) {  //规避掉 多次调用此接口 一直去连接redis的问题  加一级本地缓存

        String key=SESSION_CACHE_PREFIX + sessionId;
        String sessionValue=null;
        Cache cache=ehCacheManager.getCache(SESSION_EHCACHE_NAMESPACE);
        if (  (sessionValue=(String)cache.get(key))==null ){
            log.info("get from redis...");
            sessionValue = redisTemplate.opsForValue().get(SESSION_CACHE_PREFIX + sessionId);
        }
        if (Objects.isNull(sessionValue)) return null;

        cache.put(key,sessionValue);
        ByteArrayInputStream byteArrayInputStream=null;
        ObjectInputStream objectInputStream=null;
        try {
            byteArrayInputStream=new ByteArrayInputStream(Base64.getDecoder().decode(sessionValue));
            objectInputStream=new ObjectInputStream(byteArrayInputStream);
            return  (Session)objectInputStream.readObject();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            try {
                objectInputStream.close();
                byteArrayInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }




    private String getSessionKeyInRedis(Session session) {
        return SESSION_CACHE_PREFIX + session.getId();
    }

    public long getSessionExpireSecond() {
        return sessionExpireSecond;
    }

    public void setSessionExpireSecond(long sessionExpireSecond) {
        this.sessionExpireSecond = sessionExpireSecond;
    }
}
