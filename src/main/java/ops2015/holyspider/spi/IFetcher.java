package ops2015.holyspider.spi;

import ops2015.holyspider.domain.DomainFetchEngine;

import org.apache.http.client.HttpClient;

public interface IFetcher {
	public void fetch();
	
	public void setDomainHttpClient(HttpClient httpClient);
	
	public void setEngine(DomainFetchEngine engine);
}
