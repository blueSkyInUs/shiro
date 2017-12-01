package com.init.redis;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
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


    @Override
    public void update(Session session) throws UnknownSessionException {

        ByteArrayOutputStream byteArrayOutputStream=null;
        ObjectOutputStream objectOutputStream=null;
        try {
             byteArrayOutputStream=new ByteArrayOutputStream();
             objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(session);
            redisTemplate.opsForValue().set(getSessionKeyInRedis(session), Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            redisTemplate.expire(getSessionKeyInRedis(session), sessionExpireSecond, TimeUnit.SECONDS);
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
    protected Session doReadSession(Serializable sessionId) {
        String result = redisTemplate.opsForValue().get(SESSION_CACHE_PREFIX + sessionId);
        if (Objects.isNull(result)) return null;

        ByteArrayInputStream byteArrayInputStream=null;
        ObjectInputStream objectInputStream=null;
        try {
            byteArrayInputStream=new ByteArrayInputStream(Base64.getDecoder().decode(result));
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


        return JSONObject.parseObject(result, Session.class);
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
