package ops2015.holyspider.spi;

import java.net.URL;
import java.util.Map;

import ops2015.holyspider.Domain;

public interface IUrlGenerator {
	/**
	 * 根据当前的地址和参数，获得下一个地址
	 * @param current
	 * @param currentUrl
	 * @return
	 */
	public URL next(URL currentUrl, Map<String, ?> context);
	
	public void restoreFromUrlString(String url);
	
	public void setDomain(Domain domain);
}
