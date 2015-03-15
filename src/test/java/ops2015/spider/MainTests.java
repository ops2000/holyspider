package ops2015.spider;

import ops2015.holyspider.domain.DomainFetchEngine;

import org.junit.Test;


public class MainTests {
	@Test
	public void startEngine() {
		DomainFetchEngine engine = DomainFetchEngine.getInstace();
		
		engine.starup();
		
		engine.shutdown();
	}
}
