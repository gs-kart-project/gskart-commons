package com.gskart.commons.caching.autoconfigure;

import com.gskart.commons.caching.LoggingCacheErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsCachingAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsCachingAutoConfiguration.class));

    @Test
    void staysOutOfTheWayUntilAServiceAsksForCaching() {
        runner.run(context -> assertThat(context)
                .doesNotHaveBean(RedisCacheConfiguration.class)
                .doesNotHaveBean(CachingConfigurer.class));
    }

    @Test
    void configuresBoundedJsonEntriesOnceEnabled() {
        runner.withPropertyValues("gskart.commons.caching.enabled=true").run(context -> {
            RedisCacheConfiguration configuration = context.getBean(RedisCacheConfiguration.class);

            assertThat(configuration.getTtlFunction().getTimeToLive(Object.class, null)).hasMinutes(10);
            assertThat(configuration.getAllowCacheNullValues()).isFalse();
        });
    }

    @Test
    void honoursAConfiguredLifetime() {
        runner.withPropertyValues("gskart.commons.caching.enabled=true",
                        "gskart.commons.caching.time-to-live=45s")
                .run(context -> assertThat(context.getBean(RedisCacheConfiguration.class)
                        .getTtlFunction().getTimeToLive(Object.class, null)).hasSeconds(45));
    }

    @Test
    void keepsOneServicesEntriesOutOfAnothers() {
        runner.withPropertyValues("gskart.commons.caching.enabled=true",
                        "spring.application.name=product-service")
                .run(context -> assertThat(context.getBean(RedisCacheConfiguration.class)
                        .getKeyPrefixFor("products")).startsWith("product-service::"));
    }

    @Test
    void canShareAKeyspaceWhenAServiceInsists() {
        runner.withPropertyValues("gskart.commons.caching.enabled=true",
                        "gskart.commons.caching.key-prefixed=false",
                        "spring.application.name=product-service")
                .run(context -> assertThat(context.getBean(RedisCacheConfiguration.class)
                        .getKeyPrefixFor("products")).doesNotContain("product-service"));
    }

    @Test
    void degradesInsteadOfFailingWhenTheCacheIsUnreachable() {
        runner.withPropertyValues("gskart.commons.caching.enabled=true").run(context -> {
            CacheErrorHandler errorHandler = context.getBean(CachingConfigurer.class).errorHandler();

            assertThat(errorHandler).isInstanceOf(LoggingCacheErrorHandler.class);
        });
    }

    @Test
    void standsAsideForAServiceThatConfiguresCachingItself() {
        runner.withPropertyValues("gskart.commons.caching.enabled=true")
                .withUserConfiguration(ServiceOwnCachingConfigurer.class)
                .run(context -> assertThat(context.getBean(CachingConfigurer.class).errorHandler())
                        .isInstanceOf(SimpleCacheErrorHandler.class));
    }

    @Test
    void skipsTheRedisDefaultsWithoutRedisOnTheClasspath() {
        runner.withPropertyValues("gskart.commons.caching.enabled=true")
                .withClassLoader(new FilteredClassLoader(RedisCacheConfiguration.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(RedisCacheConfiguration.class)
                        .hasSingleBean(CachingConfigurer.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class ServiceOwnCachingConfigurer {

        @Bean
        CachingConfigurer serviceCachingConfigurer() {
            return new CachingConfigurer() {
                @Override
                public CacheErrorHandler errorHandler() {
                    return new SimpleCacheErrorHandler();
                }
            };
        }
    }
}
