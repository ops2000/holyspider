package ops2015.holyspider;

import java.io.IOException;

import ops2015.holyspider.domain.DomainFetchEngine.Config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

public class HSConfigurator {
	protected final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(this.getClass());
	
	// 默认配置文件地址，总是读取，提供默认值
		static final String DEFAULT_CONFIG_FILE = "classpath:/spider-default.properties";
		// 命令行配置外部的配置文件，可以覆盖和扩展默认配置
		static final String CONFIG_FILE_KEY = "configFile";

	
	// 所有的配置属性，包括domainKey-*形式的配置
			private ConfigurableEnvironment environment = null;

			private PropertiesPropertySource defaultApplicationProperties = null;

			public HSConfigurator load(PropertySource<?> ps) {
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
