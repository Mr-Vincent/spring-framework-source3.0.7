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
	//这个是最终调用的构造器 参数是xml文件的字符串数组，也就是说可以多传几个xml文件 也可能是通配符表示的路径
	//这种方式初始化容器是没有父容器的
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
			throws BeansException {
		super(parent);
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();
		}
	}
```

```java
	// 这里对传入的xml文件字符串进行处理，因为穿进来的可能不是标准的文件路径 有可能是ant风格的通配符
	// 暂时不去纠结怎么处理的，只需要知道这里存放的一定是一个xml文件路径
	public void setConfigLocations(String[] locations) {
		if (locations != null) {
			Assert.noNullElements(locations, "Config locations must not be null");
			this.configLocations = new String[locations.length];
			for (int i = 0; i < locations.length; i++) {
				this.configLocations[i] = resolvePath(locations[i]).trim();
			}
		}
		else {
			this.configLocations = null;
		}
	}
```

接下来的重点就是``refresh()``  比较复杂：
```java
public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// Prepare this context for refreshing.
			// setting its startup date and active flag.
			// 启动的时间和active标志设置了一哈
			prepareRefresh();
			// Tell the subclass to refresh the internal bean factory.
			// 重点是这个 获取一个bean的工厂 这个工厂的继承结构会有图展示
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);
			try {
				// Allows post-processing of the bean factory in context subclasses.
				postProcessBeanFactory(beanFactory);
				// Invoke factory processors registered as beans in the context.
				invokeBeanFactoryPostProcessors(beanFactory);
				// Register bean processors that intercept bean creation.
				registerBeanPostProcessors(beanFactory);
				// Initialize message source for this context.
				initMessageSource();
				// Initialize event multicaster for this context.
				initApplicationEventMulticaster();
				// Initialize other special beans in specific context subclasses.
				onRefresh();
				// Check for listener beans and register them.
				registerListeners();
				// Instantiate all remaining (non-lazy-init) singletons.
				finishBeanFactoryInitialization(beanFactory);
				// Last step: publish corresponding event.
				finishRefresh();
			}
			catch (BeansException ex) {
				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();
				// Reset 'active' flag.
				cancelRefresh(ex);
				// Propagate exception to caller.
				throw ex;
			}
		}
	}
```
ConfigurableListableBeanFactory继承结构
![image](http://7xsfwn.com1.z0.glb.clouddn.com/ConfigurableListableBeanFactory.png)
XmlBeanFactory继承结构
![image](http://7xsfwn.com1.z0.glb.clouddn.com/XmlBeanFactory.png)

接着我们重点关注一下这行代码是怎么工作的：
```java
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
```
这个方法名叫做``obtainFreshBeanFactory``顾名思义“获取新鲜的bean工厂”

```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		// 重点是这行代码 调用子类AbstractRefreshableApplicationContext的方法
		refreshBeanFactory();
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (logger.isDebugEnabled()) {
			logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
		}
		return beanFactory;
	}
```

```java
protected final void refreshBeanFactory() throws BeansException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			// 创建DefaultListableBeanFactory实例 这个是ConfigurableListableBeanFactory的默认实现
			// 仅仅是将其new出来了
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			beanFactory.setSerializationId(getId());
			customizeBeanFactory(beanFactory);
			// 加载bean 定义 重点看这个 同样也是调用子类AbstractXmlApplicationContext的实现
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				// beanfactory创建完成猴赋值给成员变量，这样就能通过getBeanFactory获取到了
				this.beanFactory = beanFactory;
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}
```

```java
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		// 用的是这个类来读取xml中的配置
		// 先看将beanfactory传进去干嘛了 其他的不重要 因为我们关注的是beanfactory
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		// 构造器中的初始化loader后来到这里被重新设置了一次
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		// 初始化设置 将验证开关置为true
		initBeanDefinitionReader(beanDefinitionReader);
		// 加载bean定义：将xml中的内容读到内存中
		// 这个是重点了，将要去读取解析xml 将定义的bean转化为对象了
		loadBeanDefinitions(beanDefinitionReader);
	}
```
XmlBeanDefinitionReader 继承结构
![image](http://7xsfwn.com1.z0.glb.clouddn.com/XmlBeanDefinitionReader.png)

```java
	public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		// 将factory通过构造器传到XmlBeanDefinitionReader
		// 原来是交给他父类去处理的，看看他老爹是怎么整的
		super(registry);
		logger.info("=================XmlBeanDefinitionReader constructor=====================");
	}
	// XmlBeanDefinitionReader 的父类构造器
	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		// Determine ResourceLoader to use.
		// 这里传进去的是DefaultListableBeanFactory 然而并没有实现ResourceLoader接口 因此loader采用PathMatchingResourcePatternResolver 这里只是默认实现 如果使用setxxx的话另说
		if (this.registry instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.registry;
		}
		else {
			// 创建资源加载器
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}
	}
```
然后，与beanfactory相关的都转移到与成员变量registry 相关的了。因此去关注XmlBeanDefinitionReader 就能找到与之相关的线索了，毕竟都变成了他的成员变量了。

接着看看``loadBeanDefinitions(beanDefinitionReader);``
```java
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		// 分别去获取资源：字符串数组类型以及resource数组类型的
		// 且不同类型的loadBeanDefinitions处理方式不一样
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		// 我们在创建ClassPathXmlApplicationContext的时候将xml字符串传进去了，因此这里调用的是字符串的conf
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			// 读取xml中的内容 加载bean定义
			// 这段代码贼鸡儿绕
			// 核心在于loadBeanDefinitions的调用
			reader.loadBeanDefinitions(configLocations);
		}
	}
```

XmlBeanDefinitionReader#loadBeanDefinitions(configLocations)实现：
```java
	// 因为我们传进去的是一个new String[]{"services.xml"} 循环去load
	public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
		Assert.notNull(locations, "Location array must not be null");
		int counter = 0;
		for (String location : locations) {
			counter += loadBeanDefinitions(location);
		}
		return counter;
	}
	// 依然是一个重载 有点晕
	public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(location, null);
	}
	// 终级调用的是这个
	public int loadBeanDefinitions(String location, Set<Resource> actualResources) throws BeanDefinitionStoreException {
		// resourceLoader 在new XmlBeanDefinitionReader(beanFactory)的时候创建
		// 如果beanFactory是ResourceLoader的实例 就使用当前实例作为资源加载器 否则使用PathMatchingResourcePatternResolver
		// 这里传的是ClassPathXmlApplicationContext作为资源加载器
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new BeanDefinitionStoreException(
					"Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
		}
		logger.debug(">>>>>>>>>>>>>>>>>>>>最终的resourceLoader到底是-------->" + resourceLoader.toString());
		if (resourceLoader instanceof ResourcePatternResolver) {
			// Resource pattern matching available.
			try {
				// location转化为resource值得关注一下
				// 重点关注resourceLoader#getResources的实现
				// 实际上是PathMatchingResourcePatternResolver#getResources的实现
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
				// 最终同样去调用resource数组的loadBeanDefinitions实现 殊途同归呀
                // 返回的是bean的数量 重点是这段代码了
				int loadCount = loadBeanDefinitions(resources);
				if (actualResources != null) {
					for (Resource resource : resources) {
						actualResources.add(resource);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
				}
				return loadCount;
			}
			catch (IOException ex) {
				throw new BeanDefinitionStoreException(
						"Could not resolve bean definition resource pattern [" + location + "]", ex);
			}
		}
		else {
			// Can only load single resources by absolute URL.
			Resource resource = resourceLoader.getResource(location);
			int loadCount = loadBeanDefinitions(resource);
			if (actualResources != null) {
				actualResources.add(resource);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
			}
			return loadCount;
		}
	}
```

该死的``loadBeanDefinitions`` 绕来绕去
```java
	public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
		Assert.notNull(resources, "Resource array must not be null");
		int counter = 0;
		for (Resource resource : resources) {
            logger.debug(">>>>>>>>>>>>>>>>>>>>location的resources-------->" + resource);
			counter += loadBeanDefinitions(resource);
		}
		return counter;
	}
	
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new EncodedResource(resource));
	}
	
	// 最终调用的方法 EncodedResource 仅仅是对resource的包装
	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		Assert.notNull(encodedResource, "EncodedResource must not be null");
		if (logger.isInfoEnabled()) {
			logger.info("Loading XML bean definitions from " + encodedResource.getResource());
		}
		// 这里为什么用ThreadLocal去保存当前Resource？？
		// 一个奇怪的现象就是创建多个资源文件每次进入这个方法set都是空的  只是针对同一个资源文件才有效
		// 我在一个xml文件里import自己 这样就会报错
		Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
		if (currentResources == null) {
			currentResources = new HashSet<EncodedResource>(4);
			this.resourcesCurrentlyBeingLoaded.set(currentResources);
		}
		if (!currentResources.add(encodedResource)) {
			throw new BeanDefinitionStoreException(
					"Detected cyclic loading of " + encodedResource + " - check your import definitions!");
		}
		try {
			// 将resource转为流
			InputStream inputStream = encodedResource.getResource().getInputStream();
			try {
				InputSource inputSource = new InputSource(inputStream);
				if (encodedResource.getEncoding() != null) {
					inputSource.setEncoding(encodedResource.getEncoding());
				}
				// 去解析xml
				// 重点到这里来了
				int ret = doLoadBeanDefinitions(inputSource, encodedResource.getResource());
				logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ ret +" definitions have been found！<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				return ret;
			}
			finally {
				inputStream.close();
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"IOException parsing XML document from " + encodedResource.getResource(), ex);
		}
		finally {
			currentResources.remove(encodedResource);
			if (currentResources.isEmpty()) {
				this.resourcesCurrentlyBeingLoaded.remove();
			}
		}
	}

```

有必要去了解一下resource到底是哪个实现类：
![enter image description here](http://7xsfwn.com1.z0.glb.clouddn.com/ClassPathContextResource.png)


在Spring中你会发现很多诸如``doLoadBeanDefinitions``这种命名形式的方法:``doXXX``.我觉得还是因为执行某个步骤的时候会有很多准备动作，正真去执行的时候采用``doXXX``.这种命名方式值得借鉴一下。

```java
// 有2个动作，load  register
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {
		try {
			// 判断xml的类型 默认是VALIDATION_AUTO
			int validationMode = getValidationModeForResource(resource);
			Document doc = this.documentLoader.loadDocument(
					inputSource, getEntityResolver(), this.errorHandler, validationMode, isNamespaceAware());
			return registerBeanDefinitions(doc, resource);
		}
		// 省略许多catch
		...
	}

```
``DocumentLoader``是一个接口,返回一个``Document``对象，这个``Document``是w3c的包中的。
```java
Document loadDocument(
			InputSource inputSource, EntityResolver entityResolver,
			ErrorHandler errorHandler, int validationMode, boolean namespaceAware)
			throws Exception;
```
这个接口的默认实现是``DefaultDocumentLoader``，其中内部操作都是调用jdk的API，不必纠结下去了。

接下来重点是``registerBeanDefinitions(doc, resource)``这个方法了。

```java
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
		// Read document based on new BeanDefinitionDocumentReader SPI.
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
		int countBefore = getRegistry().getBeanDefinitionCount();
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
		int countAfter = getRegistry().getBeanDefinitionCount();
		logger.debug(">>>>>>>>>>>>"+ countAfter +" Definitions have found<<<<<<<<<<<<<<<<");
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}
```

重点，妈的重点怎么这么多啊啊啊啊！！！
```java

public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;

		logger.debug("Loading bean definitions");
		// 获取根节点
		Element root = doc.getDocumentElement();
		// 创建解析代理
		BeanDefinitionParserDelegate delegate = createHelper(readerContext, root);
		// 三部曲
		preProcessXml(root);
		parseBeanDefinitions(root, delegate);
		postProcessXml(root);
	}
protected BeanDefinitionParserDelegate createHelper(XmlReaderContext readerContext, Element root) {
		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
		delegate.initDefaults(root);
		return delegate;
	}
```

用于解析xml的方法：
```java
	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 * @param root the DOM root element of the document
	 */
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					if (delegate.isDefaultNamespace(ele)) {
						parseDefaultElement(ele, delegate);
					}
					else {
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			// 假如namespace不是http://www.springframework.org/schema/beans
			// 自定义的标签 这样去解析
			delegate.parseCustomElement(root);
		}
	}
	// 分别处理import标签 alias标签和bean标签
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
	}
```

重点看对bean标签的处理：
```java
	// 处理指定的bean元素，解析bean定义并且通过注册器注册
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		// 解析bean标签元素，将其封装成一个BeanDefinitionHolder
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
		    // 装饰一下 得到最终的BeanDefinitionHolder 
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// Register the final decorated instance.
                // 注册bean 这样我们就能够从context中获取到
                // getReaderContext().getRegistry()的实例是DefaultListableBeanFactory
                BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			// Send registration event.
            // 发送注册事件，观察者模式
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}
```

这段代码很容易去理解，分几步去看。先看第一行如何将一个dom节点封装成BeanDefinitionHolder：
```java
// 贼鸡儿多的重载 烦死人
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
		return parseBeanDefinitionElement(ele, null);
	}
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) {
		String id = ele.getAttribute(ID_ATTRIBUTE);
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
		// 将name属性作为别名存起来
		List<String> aliases = new ArrayList<String>();
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, BEAN_NAME_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}
		// name默认就是id 假如不去指定的话
        // 如果有name属性 那就使用第一个作为beanName
		String beanName = id;
		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
			beanName = aliases.remove(0);
			if (logger.isDebugEnabled()) {
				logger.debug("No XML 'id' specified - using '" + beanName +
						"' as bean name and " + aliases + " as aliases");
			}
		}

		if (containingBean == null) {
			// 检查是否有命名重复
			checkNameUniqueness(beanName, aliases, ele);
		}
		// 将dom元素转化为beanDefinition 这个beanDefinition还不能直接用，还要将其包装为BeanDefinitionHolder
		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
		if (beanDefinition != null) {
			// 再确认一下是不是有name 没有就去生成出来
			if (!StringUtils.hasText(beanName)) {
				try {
					if (containingBean != null) {
						beanName = BeanDefinitionReaderUtils.generateBeanName(
								beanDefinition, this.readerContext.getRegistry(), true);
					}
					else {
						beanName = this.readerContext.generateBeanName(beanDefinition);
						// Register an alias for the plain bean class name, if still possible,
						// if the generator returned the class name plus a suffix.
						// This is expected for Spring 1.2/2.0 backwards compatibility.
						String beanClassName = beanDefinition.getBeanClassName();
						if (beanClassName != null &&
								beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
								!this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
							aliases.add(beanClassName);
						}
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Neither XML 'id' nor 'name' specified - " +
								"using generated bean name [" + beanName + "]");
					}
				}
				catch (Exception ex) {
					error(ex.getMessage(), ele);
					return null;
				}
			}
			String[] aliasesArray = StringUtils.toStringArray(aliases);
			// 包装成BeanDefinitionHolder返回
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
		}

		return null;
	}
```

![GenericBeanDefinition](http://7xsfwn.com1.z0.glb.clouddn.com/GenericBeanDefinition.png)

``parseBeanDefinitionElement``方法：
```java
public AbstractBeanDefinition parseBeanDefinitionElement(
			Element ele, String beanName, BeanDefinition containingBean) {
        // stack结构 将bean名称封装成一个BeanEntry对象，里面就一个beanDefinitionName字符串
        // 发现先push再pop 不知道这样做是干嘛的
		this.parseState.push(new BeanEntry(beanName));
        // 类全路径
		String className = null;
		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
		}

		try {
		    // parent属性 用于继承
			String parent = null;
			if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
				parent = ele.getAttribute(PARENT_ATTRIBUTE);
			}
			// 创建GenericBeanDefinition
			AbstractBeanDefinition bd = createBeanDefinition(className, parent);
            // 解析属性 如：singleton autowire等等
			parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
			// 设置description属性
			bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));

			parseMetaElements(ele, bd);
			// 方法注入
			parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
			parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

			// 构造注入
			parseConstructorArgElements(ele, bd);
			// 属性注入
			parsePropertyElements(ele, bd);
			parseQualifierElements(ele, bd);

			bd.setResource(this.readerContext.getResource());
			bd.setSource(extractSource(ele));

			return bd;
		}
		catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", ele, ex);
		}
		catch (NoClassDefFoundError err) {
			error("Class that bean class [" + className + "] depends on not found", ele, err);
		}
		catch (Throwable ex) {
			error("Unexpected failure during bean definition parsing", ele, ex);
		}
		finally {
			this.parseState.pop();
		}

		return null;
	}
```

接着看第二步装饰模式怎么玩的：
```java
public BeanDefinitionHolder decorateBeanDefinitionIfRequired(Element ele, BeanDefinitionHolder definitionHolder) {
		return decorateBeanDefinitionIfRequired(ele, definitionHolder, null);
	}

public BeanDefinitionHolder decorateBeanDefinitionIfRequired(
			Element ele, BeanDefinitionHolder definitionHolder, BeanDefinition containingBd) {

		BeanDefinitionHolder finalDefinition = definitionHolder;

		// Decorate based on custom attributes first.
        // 优先处理自定义的属性 spring的属性之前已经处理了 自定义的属性是哪些我也不知道
		NamedNodeMap attributes = ele.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node node = attributes.item(i);
			finalDefinition = decorateIfRequired(node, finalDefinition, containingBd);
		}

		// Decorate based on custom nested elements.
		NodeList children = ele.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				finalDefinition = decorateIfRequired(node, finalDefinition, containingBd);
			}
		}
		return finalDefinition;
	}
```
拿到终极 的BeanDefinitionHolder后，最后一步：注册
```java
public static void registerBeanDefinition(
			BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		// Register bean definition under primary name.
		// 注册名字
		String beanName = definitionHolder.getBeanName();
		registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

		// Register aliases for bean name, if any.
		// 注册别名
		String[] aliases = definitionHolder.getAliases();
		if (aliases != null) {
			for (String aliase : aliases) {
				registry.registerAlias(beanName, aliase);
			}
		}
	}
```

看看注册的逻辑，基本上整个流程就算是走完了：
```java
public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		synchronized (this.beanDefinitionMap) {
			Object oldBeanDefinition = this.beanDefinitionMap.get(beanName);
			if (oldBeanDefinition != null) {
				if (!this.allowBeanDefinitionOverriding) {
					throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
							"Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName +
							"': There is already [" + oldBeanDefinition + "] bound.");
				}
				else {
					if (this.logger.isInfoEnabled()) {
						this.logger.info("Overriding bean definition for bean '" + beanName +
								"': replacing [" + oldBeanDefinition + "] with [" + beanDefinition + "]");
					}
				}
			}
			else {
				this.beanDefinitionNames.add(beanName);
				this.frozenBeanDefinitionNames = null;
			}
			this.beanDefinitionMap.put(beanName, beanDefinition);

			resetBeanDefinition(beanName);
		}
	}
```
至此，从Spring初始化到实例化bean整个步骤算是简单的走完了，很多地方很模糊，很多看不懂的地方，不得不说设计者实在是想的很多，让我们用起来很方便。

回头看了一下，以上的仅仅是皮毛。