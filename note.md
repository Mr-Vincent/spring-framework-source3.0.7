### Spring3.x源码笔记
#### 生命周期
bean实例化（构造器）--> 属性设置--> BeanNameAware#setBeanName--> 
BeanFactoryAware#setBeanFactoryName--> 
ApplicationContextAware#setApplicationContext--> 
BeanPostProcessor#postProcessBeforeInitialization--> 
InitializingBean#afterPropertiesSet--> 定制的初始化方法（xml中init-method）--> 
BeanPostProcessor#postProcessAfterInitialization--> 完成初始化 --> 
DisposableBean#destroy --> 定制的销毁方法（xml中destory-method）