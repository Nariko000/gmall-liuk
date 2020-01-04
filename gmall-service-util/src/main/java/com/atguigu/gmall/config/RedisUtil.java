package com.atguigu.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host, int port, int timeOut){
        //初始化参数配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置最大连接数
        jedisPoolConfig.setMaxTotal(200);
        //如果达到最大连接数，进行排队等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        //设置等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        //设置最小剩余数
        jedisPoolConfig.setMinIdle(10);
        //获取到连接时，自检连接是否可用
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(jedisPoolConfig, host, port, timeOut);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

}
