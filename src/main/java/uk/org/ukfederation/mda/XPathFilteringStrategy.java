/*
 * Copyright (C) 2011 University of Edinburgh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.ukfederation.mda;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.BaseStage.MetadataFilteringStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathFilteringStrategy implements MetadataFilteringStrategy<DomMetadata> {
	
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XPathFilteringStrategy.class);

	private static class SimpleNamespaceContext implements NamespaceContext {
		
		private final Map<String, String> prefixMappings;

		public String getNamespaceURI(String prefix) {
			return prefixMappings.get(prefix);
		}

		public String getPrefix(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		public Iterator<String> getPrefixes(String namespaceURI) {
			throw new UnsupportedOperationException();
		}
		
		public SimpleNamespaceContext(Map<String, String> prefixMappings) {
			this.prefixMappings = prefixMappings;
		}
		
	}
	
	private final String xpathExpression;
	private final NamespaceContext namespaceContext;
	
	public void filterMetadata(Collection<DomMetadata> metadataCollection) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		if (namespaceContext != null) {
			xpath.setNamespaceContext(namespaceContext);
		}
		
		XPathExpression compiledExpression;
		try {
			compiledExpression = xpath.compile(xpathExpression);
		} catch (XPathExpressionException e) {
			log.error("error compiling XPath expression; no filtering performed", e);
			return;
		}
		
		Iterator<DomMetadata> metadataIterator = metadataCollection.iterator();
		while (metadataIterator.hasNext()) {
			DomMetadata metadata = metadataIterator.next();
			try {
				Boolean filterThis = (Boolean)compiledExpression.evaluate(metadata.getMetadata(), XPathConstants.BOOLEAN);
				if (filterThis) {
					log.info("removing metadata matching XPath condition");
					metadataIterator.remove();
				}
			} catch (XPathExpressionException e) {
				log.error("removing metadata due to XPath expression error", e);
				metadataIterator.remove();
			}
		}
	}
	
	public XPathFilteringStrategy(String expression, NamespaceContext context) {
		xpathExpression = expression;
		namespaceContext = context;
	}
	
	public XPathFilteringStrategy(String expression, Map<String, String> prefixMappings) {
		this(expression, new SimpleNamespaceContext(prefixMappings));
	}
}
