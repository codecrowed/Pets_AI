package jiangxiaopeng.ai.shared.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * @author jiangyangang
 */
@Component
public class SpringContextUtil implements BeanFactoryPostProcessor {

    /**
     * Spring应用上下文环境
     */
    private static volatile ConfigurableListableBeanFactory applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        applicationContext = beanFactory;
    }

    /**
     * 获取对象
     *
     * @param name 对象名称
     * @return Object
     * @throws BeansException
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    /**
     * 获取对象
     *
     * @param classType 对象类型
     * @return T
     * @throws BeansException
     */
    public static <T> T  getBean(Class<T> classType) throws BeansException {
        return  applicationContext.getBean(classType);
    }
}