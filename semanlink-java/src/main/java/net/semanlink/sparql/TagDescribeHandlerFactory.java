/* Created on 3 janv. 2011 */
package net.semanlink.sparql;

import org.apache.jena.sparql.core.describe.DescribeHandlerFactory;

public class TagDescribeHandlerFactory implements DescribeHandlerFactory {
public TagDescribeHandler create() {
	return new TagDescribeHandler();
}
}
