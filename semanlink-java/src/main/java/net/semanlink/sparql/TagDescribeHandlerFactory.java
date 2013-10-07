/* Created on 3 janv. 2011 */
package net.semanlink.sparql;

import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerFactory;

public class TagDescribeHandlerFactory implements DescribeHandlerFactory {
public TagDescribeHandler create() {
	return new TagDescribeHandler();
}
}
