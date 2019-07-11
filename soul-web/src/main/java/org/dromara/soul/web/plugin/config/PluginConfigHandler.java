/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.soul.web.plugin.config;

import com.google.common.base.Splitter;
import org.dromara.soul.common.config.MonitorConfig;
import org.dromara.soul.common.config.RateLimiterConfig;
import org.dromara.soul.common.dto.PluginData;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.utils.GsonUtils;
import org.influxdb.dto.Point;
import org.springframework.data.influxdb.InfluxDBConnectionFactory;
import org.springframework.data.influxdb.InfluxDBProperties;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.data.influxdb.converter.PointConverter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The enum Plugin config init.
 *
 * @author xiaoyu(Myth)
 */
public enum PluginConfigHandler {

    /**
     * Ins plugin config init.
     */
    INS;


    /**
     * Init plugin config.
     *
     * @param pluginDataList the plugin data list
     */
    public void initPluginConfig(final List<PluginData> pluginDataList) {
        pluginDataList.stream()
                .filter(PluginData::getEnabled)
                .forEach(pluginData -> {
                    if (PluginEnum.MONITOR.getName().equals(pluginData.getName())) {
                        MonitorConfig monitorConfig = GsonUtils.getInstance().fromJson(pluginData.getConfig(), MonitorConfig.class);
                        if (Objects.isNull(Singleton.INST.get(InfluxDBTemplate.class))
                                || Objects.isNull(Singleton.INST.get(MonitorConfig.class))
                                || !monitorConfig.equals(Singleton.INST.get(MonitorConfig.class))) {
                            InfluxDBConnectionFactory connectionFactory = new InfluxDBConnectionFactory(buildByConfig(monitorConfig));
                            InfluxDBTemplate<Point> influxDBTemplate = new InfluxDBTemplate<>(connectionFactory, new PointConverter());
                            Singleton.INST.single(InfluxDBTemplate.class, influxDBTemplate);
                            Singleton.INST.single(MonitorConfig.class, monitorConfig);
                        }
                    } else if (PluginEnum.RATE_LIMITER.getName().equals(pluginData.getName())) {
                        //初始化redis
                        RateLimiterConfig rateLimiterConfig = GsonUtils.getInstance().fromJson(pluginData.getConfig(), RateLimiterConfig.class);
                        //出来转换成spring data redisTemplate
                        if (Objects.isNull(Singleton.INST.get(ReactiveRedisTemplate.class))
                                || Objects.isNull(Singleton.INST.get(RateLimiterConfig.class))
                                || !rateLimiterConfig.equals(Singleton.INST.get(RateLimiterConfig.class))) {
                            LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(rateLimiterConfig);
                            RedisSerializer<String> serializer = new StringRedisSerializer();
                            RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                                    .<String, String>newSerializationContext()
                                    .key(serializer)
                                    .value(serializer)
                                    .hashKey(serializer)
                                    .hashValue(serializer)
                                    .build();
                            ReactiveRedisTemplate<String, String> reactiveRedisTemplate = new ReactiveRedisTemplate<>(lettuceConnectionFactory, serializationContext);
                            Singleton.INST.single(ReactiveRedisTemplate.class, reactiveRedisTemplate);
                            Singleton.INST.single(RateLimiterConfig.class, rateLimiterConfig);
                        }
                    }
                });

    }

    public static void main(String[] args) {
        RateLimiterConfig config = new RateLimiterConfig();
        config.setSentinel(true);
        config.setMaster("mymaster");
        config.setPassword("foobaredbbexONE123");
        config.setSentinelUrl("192.168.1.91:26379;192.168.1.92:26379;192.168.1.93:26379");
        System.out.println(GsonUtils.getInstance().toJson(config));
    }

    private LettuceConnectionFactory createLettuceConnectionFactory(final RateLimiterConfig rateLimiterConfig) {
        if (rateLimiterConfig.getSentinel()) {
            return new LettuceConnectionFactory(redisSentinelConfiguration(rateLimiterConfig));
        } else if (rateLimiterConfig.getCluster()) {
            return new LettuceConnectionFactory(redisClusterConfiguration(rateLimiterConfig));
        }
        return new LettuceConnectionFactory(redisStandaloneConfiguration(rateLimiterConfig));
    }

    protected final RedisStandaloneConfiguration redisStandaloneConfiguration(final RateLimiterConfig rateLimiterConfig) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(rateLimiterConfig.getHost());
        config.setPort(rateLimiterConfig.getPort());
        if (rateLimiterConfig.getPassword() != null) {
            config.setPassword(RedisPassword.of(rateLimiterConfig.getPassword()));
        }
        config.setDatabase(rateLimiterConfig.getDatabase());
        return config;
    }

    private RedisClusterConfiguration redisClusterConfiguration(final RateLimiterConfig rateLimiterConfig) {
        RedisClusterConfiguration config = new RedisClusterConfiguration();
        config.setClusterNodes(createRedisNode(rateLimiterConfig.getClusterUrl()));
        if (rateLimiterConfig.getPassword() != null) {
            config.setPassword(RedisPassword.of(rateLimiterConfig.getPassword()));
        }
        return config;
    }

    private RedisSentinelConfiguration redisSentinelConfiguration(RateLimiterConfig rateLimiterConfig) {
        RedisSentinelConfiguration config = new RedisSentinelConfiguration();
        config.master(rateLimiterConfig.getMaster());
        config.setSentinels(createRedisNode(rateLimiterConfig.getSentinelUrl()));
        if (rateLimiterConfig.getPassword() != null) {
            config.setPassword(RedisPassword.of(rateLimiterConfig.getPassword()));
        }
        config.setDatabase(rateLimiterConfig.getDatabase());
        return config;
    }

    private List<RedisNode> createRedisNode(String url) {
        List<RedisNode> redisNodes = new ArrayList<>();
        List<String> nodes = Splitter.on(";").splitToList(url);
        for (String node : nodes) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(Objects.requireNonNull(parts).length == 2, "Must be defined as 'host:port'");
                redisNodes.add(new RedisNode(parts[0], Integer.valueOf(parts[1])));
            } catch (RuntimeException ex) {
                throw new IllegalStateException(
                        "Invalid redis sentinel " + "property '" + node + "'", ex);
            }
        }
        return redisNodes;
    }

    private InfluxDBProperties buildByConfig(final MonitorConfig monitorConfig) {
        InfluxDBProperties influxDBProperties = new InfluxDBProperties();
        influxDBProperties.setDatabase(monitorConfig.getDatabase());
        influxDBProperties.setUrl(monitorConfig.getUrl());
        influxDBProperties.setUsername(monitorConfig.getUserName());
        influxDBProperties.setPassword(monitorConfig.getPassword());
        influxDBProperties.setConnectTimeout(monitorConfig.getConnectTimeout());
        influxDBProperties.setReadTimeout(monitorConfig.getReadTimeout());
        influxDBProperties.setRetentionPolicy(monitorConfig.getRetentionPolicy());
        influxDBProperties.setWriteTimeout(monitorConfig.getWriteTimeout());
        influxDBProperties.setGzip(monitorConfig.isGzip());
        return influxDBProperties;
    }
}