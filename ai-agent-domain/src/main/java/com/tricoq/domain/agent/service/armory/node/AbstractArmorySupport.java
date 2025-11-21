package com.tricoq.domain.agent.service.armory.node;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.types.framework.chain.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;


/**
 * 增强层
 *
 * @author trico qiang
 * @date 10/23/25
 */
@Slf4j
public abstract class AbstractArmorySupport
        extends AbstractMultiThreadStrategyRouter<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 异步加载数据
     */
    @Override
    protected void multiThread(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {

    }

    protected String beanName(String id) {
        throw new UnsupportedOperationException();
    }

    protected String dataName() {
        throw new UnsupportedOperationException();
    }

    protected synchronized <T> void registerBean(String beanName, Class<T> clazz, T instance) {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz, () -> instance);
        AbstractBeanDefinition rawBeanDefinition = builder.getRawBeanDefinition();
        rawBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (factory.containsBeanDefinition(beanName)) {
            factory.removeBeanDefinition(beanName);
        }

//        if (factory.containsSingleton(beanName)) {
//            factory.destroySingleton(beanName);
//        }

        factory.registerBeanDefinition(beanName, rawBeanDefinition);

        log.info("成功注册Bean: {}", beanName);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName){
        return (T)applicationContext.getBean(beanName);
    }
}
