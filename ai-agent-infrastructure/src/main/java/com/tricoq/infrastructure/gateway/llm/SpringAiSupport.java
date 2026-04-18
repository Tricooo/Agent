package com.tricoq.infrastructure.gateway.llm;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/18/26
 */
public abstract class SpringAiSupport {

    @Resource
    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    protected synchronized <T> void registerBean(String beanName, Class<T> clazz, T instance) {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz, () -> instance);
        AbstractBeanDefinition rawBeanDefinition = builder.getRawBeanDefinition();
        rawBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (factory.containsBeanDefinition(beanName)) {
            factory.removeBeanDefinition(beanName);
        }

        factory.registerBeanDefinition(beanName, rawBeanDefinition);
    }
}
