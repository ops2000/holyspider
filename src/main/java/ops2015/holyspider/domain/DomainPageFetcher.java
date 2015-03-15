package ops2015.holyspider.domain;

import java.util.Map;

import ops2015.holyspider.Domain;
import ops2015.holyspider.spi.impl.DefaultFetcher;

/**
 * 页面抓取
 * <p>
 * 通过PageUrlGenerator产生的地址，获取页面的内容，然后使用PageParser解析页面内容生成所有的对象URL
 * 将生成的对象URL加入到Domain的targetUrls中，从而产生某个网站的所有需要获取的对象的地址列表
 * <p>
 * PageFetcher可以多个异步执行，因为PageUrlGenerator总是保持同步的
 * 
 * @author wangs
 *
 */
public class DomainPageFetcher extends DefaultFetcher {
	protected Map<String, ?> currentPageArguments = null;
	protected String lastPageUrl = null;

	public DomainPageFetcher(Domain domain) {
		super(domain);
	}

	@Override
	public void fetch() {
		// 获取域的PageUrlGenerator实例

		// 获取所能取到的下个地址
	}

	protected void fetchOne(String url) {
		// 获取地址的页面内容

		// 使用域的PageContentParser实例解析页面内容获得Target的Url列表
		
		// 将url放入到Domain的TargetUrls中
	}
}
