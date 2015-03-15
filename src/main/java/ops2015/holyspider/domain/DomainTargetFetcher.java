package ops2015.holyspider.domain;

import ops2015.holyspider.Domain;
import ops2015.holyspider.spi.impl.DefaultFetcher;


/**
 * 目标抓取，比如图片、种子等
 * <p>
 * 
 * 不断的消费Domain中的TargetUrls，获取对象的内容，并且保存到目录中
 * 
 * @author wangs
 *
 */
public class DomainTargetFetcher extends DefaultFetcher {
	public DomainTargetFetcher(Domain domain) {
		super(domain);
	}
	
	@Override
	public void fetch() {

	}
	
	protected void fetchOne(String url) {
		
	}
	
	boolean decideDomainTarget(String url) {
		return false;
	}
}
