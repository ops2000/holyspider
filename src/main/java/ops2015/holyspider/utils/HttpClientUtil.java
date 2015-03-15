package ops2015.holyspider.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientUtil {
	public static final int MAX_CONN_PER_ROUTE = 150;
	public static final int MAX_CONN_TOTAL = 2000;
	public static final int MINS = 60 * 1000;
	public static final int SOCKET_TIMEOUT = 5 * MINS;
	public static final int CONNECT_TIMEOUT = 10 * MINS;
	public static final int CONNECTION_REQUEST_TIMEOUT = 5 * MINS;

	public static HttpRequestBase createHttpRequest(HttpMethod method,
			String url) {
		HttpRequestBase httpRequest = null;
		switch (method) {
		case GET:
			httpRequest = new HttpGet(url);
			break;
		case POST:
			httpRequest = new HttpPost(url);
			break;
		case DELETE:
			httpRequest = new HttpDelete(url);
			break;
		case HEAD:
			httpRequest = new HttpHead(url);
			break;
		case OPTIONS:
			httpRequest = new HttpOptions(url);
			break;
		case PUT:
			httpRequest = new HttpPut(url);
			break;
		default:
			throw new java.lang.UnsupportedOperationException("尚不支持的请求方法："
					+ method);
		}
		return httpRequest;
	}

	public static RequestConfig createDefaultHttpClientConfig() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(SOCKET_TIMEOUT)
				.setConnectTimeout(CONNECT_TIMEOUT)
				.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
				.setRedirectsEnabled(true).build();
		return requestConfig;
	}

	public static CloseableHttpClient createDefaultHttpClient(
			RequestConfig config) {
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultRequestConfig(config)
				.setMaxConnPerRoute(MAX_CONN_PER_ROUTE)
				.setMaxConnTotal(MAX_CONN_TOTAL).build();
		return httpclient;
	}
}
