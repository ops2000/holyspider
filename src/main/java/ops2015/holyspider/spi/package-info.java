/**
 * 扩展程序需要实现的接口
 */
/**
 * 整个的过程包括两个阶段：
 * <ol>
 * 	<li>获取页面的URL->根据页面URL抓取页面内容->根据页面内容生成抓取对象的队列
 * 	<li>根据抓取队列获取对象的URL->根据对象的URL获取对象内容->根据对象内容生成文件
 * </ol>
 * 根据以上的过程，可以抽象为三个接口的组合：
 * <ul>
 * 	<li>{@code IUrlGenerator}：URL生成器
 * 	<li>{@code IFetcher}：URL的抓取器
 * 	<li>{@code IProcessor}：抓取内容的处理器
 * </ul>
 * 
 * @author wangs
 *
 */
package ops2015.holyspider.spi;