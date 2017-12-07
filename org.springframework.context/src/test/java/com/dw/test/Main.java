package com.dw.test;
/*
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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author dongw [dongwei2016@gmail.com]
 * @date 2017/11/24
 * @description
 */
public class Main {
	public static void main(String[] args) throws IOException {
		// create and configure beans
		AbstractRefreshableApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"services.xml"});
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		System.out.println(beanFactory.getBeanDefinitionCount());
		Person p = context.getBean(Person.class);
		System.out.println(p.toString());

        context.registerShutdownHook();
//		System.in.read();
	}
}
