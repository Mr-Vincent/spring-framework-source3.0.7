### Spring3.x源码笔记
#### 生命周期
bean实例化（构造器）--> 属性设置--> BeanNameAware#setBeanName--> 
BeanFactoryAware#setBeanFactoryName--> 
ApplicationContextAware#setApplicationContext--> 
BeanPostProcessor#postProcessBeforeInitialization--> 
InitializingBean#afterPropertiesSet--> 定制的初始化方法（xml中init-method）--> 
BeanPostProcessor#postProcessAfterInitialization--> 完成初始化 --> 
DisposableBean#destroy --> 定制的销毁方法（xml中destory-method）


#### 加载一个xml过程

这个过程比较复杂，继承结构必须得清楚。

通常使用一个Spring容器我们一般会这样做：
```java
ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"services.xml"});
Person p = context.getBean(Person.class);
```
这样context就能去获取xml中定义的bean了。

![image](http://7xsfwn.com1.z0.glb.clouddn.com/ClassPathXmlApplicationContext.png)

现在我们呢所关心的是这个对象的创建是如何new出来的，背后做了哪些工作，是怎样将一个xml文件中描述的bean转化为一个一个的对象的？

```java
    
	public ClassPathXmlApplicationContext(String... configLocations) throws BeansException {
		this(configLocations, true, null);
	}
    public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this(configLocations, refresh, null);
	}
	//这个是最终调用的构造器
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
			throws BeansException {
		super(parent);
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();
		}
	}
```