package com.dw.test;/**
 * ━━━━━━神兽出没━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　 ┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　  ┃
 * 　　┃　　　　　　 ┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛Code is far away from bug with the animal protecting
 * 　　　　┃　　　┃    神兽保佑,代码无bug
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * <p>
 * ━━━━━━感觉萌萌哒━━━━━━
 */

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author dongw [dongwei2016@gmail.com]
 * @date 2017/11/24 
 * @description
 */
public class Person implements BeanFactoryAware, BeanNameAware,
		InitializingBean, DisposableBean, ApplicationContextAware,BeanPostProcessor {
	private String name;
	private String id;
	private BeanFactory beanFactory;
	private String beanName;
	private ApplicationContext applicationContext;
	private UserPostProcessor postProcessor;

	public Person() {
        System.out.println(">>>>>>>>Person<<<<<<<<<");
	}

	public Person(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
        System.out.println(">>>>>>>>setName<<<<<<<<<");
        this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
        System.out.println(">>>>>>>>setId<<<<<<<<<");
		this.id = id;
	}

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", beanFactory=" + beanFactory +
                ", beanName='" + beanName + '\'' +
                '}';
    }

    /**
     * 这个首先执行
     * @param name the name of the bean in the factory.
     * Note that this name is the actual bean name used in the factory, which may
     * differ from the originally specified name: in particular for inner bean
     * names, the actual bean name might have been made unique through appending
     * "#..." suffixes. Use the {@link BeanFactoryUtils#originalBeanName(String)}
     */
	public void setBeanName(String name) {
        System.out.println("=========setBeanName============");
		this.beanName = name;
	}

    /**
     * 这个第二执行
     * @param beanFactory owning BeanFactory (never <code>null</code>).
     * The bean can immediately call methods on the factory.
     * @throws BeansException
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("=========setBeanFactory============");
        this.beanFactory = beanFactory;
    }

    /**
     * 这个第三执行
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("=========setApplicationContext============");
        this.applicationContext = applicationContext;
    }

    /**
     * 这个第四执行
     * @throws Exception
     */
	public void afterPropertiesSet() throws Exception {
		System.out.println("=========afterPropertiesSet============");
	}

    /**
     * 最后执行
     * @throws Exception
     */
	public void destroy() throws Exception {
		System.out.println("=========destroy============");
	}

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("=========postProcessBeforeInitialization============");
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("=========postProcessAfterInitialization============");
        return bean;
    }

    public void init(){
        System.out.println("============init===============");
    }
    public void destory(){
        System.out.println("============destory===============");
    }
}
