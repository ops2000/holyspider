package ops2015.holyspider;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ops2015.holyspider.domain.DomainFetchEngine;
import ops2015.holyspider.domain.DomainPageFetcher;
import ops2015.holyspider.domain.DomainTargetFetcher;
import ops2015.holyspider.spi.IFetcher;
import ops2015.holyspider.spi.IUrlGenerator;
import ops2015.holyspider.spi.impl.OncePageGenerator;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * 网站（或称之为域）的定义和参数配置，以及扩展插件的定义
 * <p>
 * 一个网站的定义在外部的属性文件中定义，总是以网站的唯一标识开头的点号隔开的作为属性名，比如:
 * <code>foo.charset=GBK<code>，表示foo的网站的编码是GBK
 * <p>
 * 
 * @author wangs
 *
 */
public class Domain implements Serializable {
	private static final long serialVersionUID = -2046959630827348800L;

	public final static String DEFAULT_CHARSET = "UTF-8";

	public final static String PROPERTY_FETCH_TIMEOUT = "fetchTimeout"; //抓取超时时间
	public final static String PROPERTY_QUEUE_LENGTH = "queueLength"; //Target的队列长度
	public final static String PROPERTY_MAX_PAGE_FETCH_NUMBER = "maxPageFetchThreadNumber"; //最大抓取页面的线程
	public final static String PROPERTY_MAX_TARGET_FETCH_NUMBER = "maxTargetFetchThreadNumber"; // 最大抓取对象的线程
	
	public final static String PROPERTY_LAST_FETCH_PAGE = "lastFetchPageUrl"; //最后一次抓取的地址
	public final static String PROPERTY_HOST_URL = "host"; // 域的主机地址
	public final static String PROPERTY_TITLE = "title"; // 网站的名称
	public final static String PROPERTY_PROXYIP = "proxyIp"; // 代理的ip地址
	public final static String PROPERTY_CHARSET = "charset"; // 编码
	public final static String PROPERTY_DELAYPAGEFETCH = "delayPageFetch"; // 每页抓取后的停顿时间
	public final static String PROPERTY_DELAYTARGETFETCH = "delayTargetFetch"; // 每个对象后抓取的停顿时间

	// 需要扩展的类的参数名
	public static enum PROPERTY_EXTENSION_CLASS {
		pageUrlGenerator,  // 页面的地址生成器
		pageFetcher,       // 页面抓取器
		targetFetcher	   // 对象抓取器
	}

	/**
	 * 网站的唯一标识
	 */
	private String key;

	/**
	 * 域的主机地址（或域名），一般同域类的地址，实际访问时都会在之前加上这个地址
	 */
	private URL host;

	/**
	 * 最后一次抓取的地址
	 */
	private String lastFetchPageUrl = null;

	private transient Extension extensions = null;
	private transient Config config = null;

	/**
	 * 全局配置
	 */
	private transient DomainFetchEngine.Config parentConfig;
	/**
	 * Spring的Environment，所有配置都放在这里，实现配置的优先级支持
	 */
	private transient Environment environment;

	/**
	 * 以下为插件的实例
	 */
	private transient IUrlGenerator pageUrlGeneratorInstance = null;
	private transient IFetcher pageFetcherInstance = null;
	private transient IFetcher targetFetcherInstance = null;

	public Domain(String key) {
		this.key = key;

		config = new Config();
		extensions = new Extension();
	}

	/**
	 * 根据PropertiesPropertySource装入配置，并且更新FetchEngine.
	 * Config中的ConfigurableEnvironment Domain的Load方法可以多次调用，因为一个域的配置可以写在多个配置文件中
	 * 
	 * @param ps
	 * @param parentConfig
	 * @return
	 */
	public Domain load(PropertiesPropertySource ps,
			DomainFetchEngine.Config parentConfig) {
		if (this.parentConfig == null) {
			this.parentConfig = parentConfig;
			this.environment = this.parentConfig.getEnvironment();
		}

		// 设置基础属性
		if (ps.containsProperty(PROPERTY_HOST_URL)) {
			try {
				this.host = new URL((String) ps.getProperty(PROPERTY_HOST_URL));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (ps.containsProperty(PROPERTY_LAST_FETCH_PAGE)) {
			this.lastFetchPageUrl = (String) ps
					.getProperty(PROPERTY_LAST_FETCH_PAGE);
		}

		// 设置并部署扩展实例
		initExtension(ps);

		return this;
	}

	public void initExtension(PropertiesPropertySource ps) {
		for (PROPERTY_EXTENSION_CLASS clazz : PROPERTY_EXTENSION_CLASS.values()) {
			if (ps.containsProperty(clazz.name())) {
				try {
					extensions.classes.put(clazz, Class.forName((String) ps
							.getProperty(clazz.name())));
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (ps.containsProperty(PROPERTY_LAST_FETCH_PAGE)) {
			this.lastFetchPageUrl = (String) ps
					.getProperty(PROPERTY_LAST_FETCH_PAGE);
		}
		
		// 部署插件
		extensions.deployInstances();
	}

	public void updateLastFetchPageUrl(String url) {
		// TODO 
	}

	public class Config {
		public String getTitle() {
			return getEnvironment().getProperty(PROPERTY_TITLE);
		}

		public String getProxyIp() {
			return getEnvironment().getProperty(PROPERTY_PROXYIP);
		}

		public String getCharset() {
			return getEnvironment().getProperty(PROPERTY_CHARSET);
		}

		public int getQueueLength() {
			return NumberUtils
					.toInt((getEnvironment().getProperty(PROPERTY_QUEUE_LENGTH)), 500);
		}
		
		public long getDelayPageFetch() {
			return NumberUtils
					.toLong((getEnvironment()
							.getProperty(PROPERTY_DELAYPAGEFETCH)), 1000);
		}

		public long getDelayTargetFetch() {
			return NumberUtils.toLong(
					(getEnvironment().getProperty(PROPERTY_DELAYTARGETFETCH)),
					100);
		}
	}

	protected class Extension {
		@SuppressWarnings("serial")
		public Map<PROPERTY_EXTENSION_CLASS, Class<?>> classes = new HashMap<PROPERTY_EXTENSION_CLASS, Class<?>>() {
			{
				put(PROPERTY_EXTENSION_CLASS.pageUrlGenerator, OncePageGenerator.class);
				put(PROPERTY_EXTENSION_CLASS.pageFetcher, DomainPageFetcher.class);
				put(PROPERTY_EXTENSION_CLASS.targetFetcher, DomainTargetFetcher.class);
			}
		};

		protected void deployInstances() {
			try {
				if (classes.get(PROPERTY_EXTENSION_CLASS.pageUrlGenerator) != null)
					pageUrlGeneratorInstance = (IUrlGenerator)classes.get(PROPERTY_EXTENSION_CLASS.pageUrlGenerator).newInstance();
				if (pageFetcherInstance != null)
					pageFetcherInstance = (IFetcher)classes.get(PROPERTY_EXTENSION_CLASS.pageUrlGenerator).newInstance();
				if (targetFetcherInstance != null)
					targetFetcherInstance = (IFetcher)classes.get(PROPERTY_EXTENSION_CLASS.pageUrlGenerator).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getKey() {
		return key;
	}

	public URL getHost() {
		return host;
	}

	public String getTitle() {
		return config.getTitle();
	}

	public String getLastFetchPageUrl() {
		return lastFetchPageUrl;
	}

	public IUrlGenerator getPageUrlGeneratorInstance() {
		return pageUrlGeneratorInstance;
	}

	public IFetcher getPageFetcherInstance() {
		return pageFetcherInstance;
	}

	public IFetcher getTargetFetcherInstance() {
		return targetFetcherInstance;
	}

	public String getProxyIp() {
		return config.getProxyIp();
	}

	public String getCharset() {
		return config.getCharset();
	}

	public long getDelayPageFetch() {
		return config.getDelayPageFetch();
	}

	public long getDelayTargetFetch() {
		return config.getDelayTargetFetch();
	}
	
	public Environment getEnvironment() {
		if (environment == null)
			throw new Error("配置信息无法获取，网站无法正常抓取");
		return environment;
	}

	public DomainFetchEngine.Config getParentConfig() {
		return parentConfig;
	}
	
	public int getMaxPageFetchThreadNumber() {
		return NumberUtils.toInt(getEnvironment().getProperty(Domain.PROPERTY_MAX_PAGE_FETCH_NUMBER), 20);
	}

	public int getMaxTargetFetchThreadNumber() {
		return NumberUtils.toInt(getEnvironment().getProperty(Domain.PROPERTY_DELAYTARGETFETCH), 20);
	}

	public int getQueueLength() {
		return config.getQueueLength();
	}
}
