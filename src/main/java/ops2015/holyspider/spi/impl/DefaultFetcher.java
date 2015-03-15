package ops2015.holyspider.spi.impl;

import ops2015.holyspider.Domain;
import ops2015.holyspider.domain.DomainFetchEngine;
import ops2015.holyspider.spi.IFetcher;

import org.apache.http.client.HttpClient;

/**
 * 抓取器的公用方法
 * @author wangs
 *
 */
public abstract class DefaultFetcher implements IFetcher {
	private Domain domain = null;
	private HttpClient domainHttpClient = null;
	private DomainFetchEngine engine = null;
	
	public DefaultFetcher(Domain domain) {
		assert domain != null;
		this.domain = domain;
	}
	
	@Override
	public void setDomainHttpClient(HttpClient httpClient) {
		this.domainHttpClient = httpClient;
	}
	
	@Override
	public void setEngine(DomainFetchEngine engine) {
		this.engine = engine;
	}

	public Domain getDomain() {
		return domain;
	}

	public HttpClient getDomainHttpClient() {
		return domainHttpClient;
	}

	public DomainFetchEngine getEngine() {
		return engine;
	}
}
