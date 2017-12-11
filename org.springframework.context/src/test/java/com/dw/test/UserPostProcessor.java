package com.dw.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 旧版本没有这个特性 也不知该怎么去用
 */
public class UserPostProcessor implements BeanPostProcessor {

    public void say(){
        System.out.println("=========UserPostProcessor#say=========");
    }
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("=========postProcessBeforeInitialization============");
        if(bean instanceof Person){
            Person person = (Person)bean;
            person.setName("新名字");
            person.setId("2542154115");
            return person;
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("=========postProcessAfterInitialization============");
        return bean;
    }
}
