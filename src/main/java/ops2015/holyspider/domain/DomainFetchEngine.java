package ops2015.holyspider.domain;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ops2015.holyspider.Domain;
import ops2015.holyspider.spi.IUrlGenerator;
import ops2015.holyspider.utils.HttpClientUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.HttpClient;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 * 抓取引擎
 * 
 * @author wangs
 *
 */
public class DomainFetchEngine {
	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DomainFetchEngine.class);

	private static DomainFetchEngine instance = null;

	/**
	 * 
	 */
	private Map<String, Domain> domains = new LinkedHashMap<String, Domain>();
	/**
	 */
	private Map<String, BlockingQueue<String>> domainTargetUrlQueueTable = new HashMap<String, BlockingQueue<String>>();

	// 引擎的全局配置
	Config config = null;

	/**
	 * 初始化过程 1、根据命令行参数读取配置 2、验证配置
	 * 
	 * @param arguments
	 */
	public void init(String[] arguments) {
		PropertySource<?> ps = new SimpleCommandLinePropertySource(arguments);

		// 获取网站抓取的配置参数
		config = new Config().load(ps);

		// 验证配置的正确性
		config.validate();
	}

	/**
	 * 初始化完成后，启动引擎
	 */
	public void starup() {
		for (Domain domain : domains.values()) {
			try {
				BlockingQueue<String> queue = new ArrayBlockingQueue<String>(domain.getQueueLength());
				domainTargetUrlQueueTable.put(domain.getKey(), queue);
				
				HttpClient domainHttpClient = buildDomainHttpClient(domain);

				startPageFetch(domain, domainHttpClient);

				startTargetFetch(domain, domainHttpClient);

				log.info("网站[{}({})]抓取启动成功", domain.getTitle(), domain.getKey());
			} catch (Throwable t) {
				log.info("网站[{}({})]抓取启动失败，继续启动其他网站的抓取程序", domain.getTitle(),
						domain.getKey());
			}

		}
	}

	final HttpClient buildDomainHttpClient(Domain domain) {
		// TODO 根据domain配置Httpclient性能参数
		return HttpClientUtil.createDefaultHttpClient(HttpClientUtil
				.createDefaultHttpClientConfig());
	}

	/**
	 * 启动特定域的页面抓取器 使用一个调度线程池，间隔一定时间抓取一个页面 特别需要注意的是，页面抓取的线程池和目标抓取的线程池的区别：
	 * 因为页面抓取的频率总会远远低于目标抓取（一个页面会有很多目标），所以页面抓取是间隔调度执行
	 * 而目标抓取以队列消耗为主，使用固定数量的线程执行该任务，当队列消耗完成后，所有线程等待
	 * 
	 * @param domain
	 * @param domainHttpClient
	 */
	public void startPageFetch(final Domain domain,
			final HttpClient domainHttpClient) {
		Executors.newScheduledThreadPool(domain.getMaxPageFetchThreadNumber()).scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				DomainPageFetcher fetcher = new DomainPageFetcher(domain);
				fetcher.setDomainHttpClient(domainHttpClient);
				fetcher.setEngine(DomainFetchEngine.this);

				IUrlGenerator generator = domain
						.getPageUrlGeneratorInstance();
				Map<String, Object> context = new HashMap<String, Object>();
				URL pageUrl = null;
				while ((pageUrl = generator.next(pageUrl, context)) != null) {
					fetcher.fetch();
					try {
						Thread.sleep(domain.getDelayPageFetch());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}, 0, domain.getDelayPageFetch(), TimeUnit.MILLISECONDS);

	}

	public void startTargetFetch(Domain domain, HttpClient domainHttpClient) throws InterruptedException {
		ExecutorService service = Executors.newFixedThreadPool(domain
				.getMaxTargetFetchThreadNumber());
		final long fetchTime = domain.getDelayTargetFetch();
		for (int i = 0; i < domain.getMaxTargetFetchThreadNumber(); i++) {
			final DomainTargetFetcher fetcher = new DomainTargetFetcher(domain);
			fetcher.setDomainHttpClient(domainHttpClient);
			fetcher.setEngine(DomainFetchEngine.this);
			
			service.execute(new Runnable() {
				@Override
				public void run() {
					fetcher.fetch();
					try {
						TimeUnit.MILLISECONDS.sleep(fetchTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	}

	public void shutdown() {
		log.info("抓取引擎关闭");
	}

	private DomainFetchEngine() {

	}

	public synchronized static DomainFetchEngine getInstace() {
		if (instance == null)
			instance = new DomainFetchEngine();
		return instance;
	}

	/**
	 * 一个可覆盖的配置模块
	 * 
	 * @author wangs
	 *
	 */
	public class Config {
		// 所有的配置属性，包括domainKey-*形式的配置
		private ConfigurableEnvironment environment = null;

		private PropertiesPropertySource defaultApplicationProperties = null;

		public Config load(PropertySource<?> ps) {
			if (ps == null)
				return this;

			// 载入默认配置
			loadDefaultProperties();

			// 载入外部配置
			PropertiesPropertySource applicationProperties = loadProperties((String) ps
					.getProperty(CONFIG_FILE_KEY));

			// 根据配置生成域和域配置
			updateDomainConfig(defaultApplicationProperties);
			updateDomainConfig(applicationProperties);

			log.debug("配置读取完成");

			return this;
		}

		public ResourcePropertySource loadProperties(String filepath) {
			if (StringUtils.isNotBlank(filepath)) {
				ResourcePropertySource ps = loadResourcePropertySource(
						"project", filepath);
				getEnvironment().getPropertySources().addLast(ps);
				return ps;
			}
			return null;
		}

		protected void updateDomainConfig(PropertiesPropertySource ps) {
			if (ps == null)
				return;

			for (String pName : ps.getPropertyNames()) {
				if (StringUtils.contains(pName, '.')) { // 包含.符号，表示是域的定义，其中第一个.符号以前的为域的key
					String key = StringUtils.substringBefore(pName, ".");
					Domain domain = domains.get(key);
					if (domain == null)
						domain = new Domain(key);

					domain.load(ps, this);
				} else {

				}
			}
		}

		protected ResourcePropertySource loadResourcePropertySource(
				String filepath, String psName) {
			if (StringUtils.isNotBlank(filepath)) {
				try {
					ResourcePropertySource applicationProperties = new ResourcePropertySource(
							psName, filepath);
					getEnvironment().getPropertySources().addLast(
							applicationProperties);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}

		// 读取默认配置
		public void loadDefaultProperties() {
			defaultApplicationProperties = loadResourcePropertySource(
					"project-default", DEFAULT_CONFIG_FILE);
			getEnvironment().getPropertySources().addLast(
					defaultApplicationProperties);
		}

		public void saveProperties() {
			// TODO 将最后访问的信息记录到配置中，便于下次继续访问
		}

		public void validate() throws IllegalArgumentException {
			// TODO 验证参数是否正确，不正确就抛出异常
		}

		public ConfigurableEnvironment getEnvironment() {
			if (environment == null)
				environment = new StandardEnvironment();
			return environment;
		}

		public int getMaxPageFetchThreadNumber() {
			return NumberUtils.toInt(
					getEnvironment().getProperty(
							Domain.PROPERTY_MAX_PAGE_FETCH_NUMBER), 20);
		}

		public int getMaxTargetFetchThreadNumber() {
			return NumberUtils.toInt(
					getEnvironment().getProperty(
							Domain.PROPERTY_DELAYTARGETFETCH), 20);
		}
	}
}
