/*
 * Copyright (C) 2011 University of Edinburgh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.ukfederation.mda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.ClassToInstanceMultiMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A stage which removes all evidence of a given XML namespace from each metadata item.
 */
@ThreadSafe
public class NamespaceStrippingStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(NamespaceStrippingStage.class);

    /**
     * XML namespace to remove.
     */
    private String namespace;

    /**
     * Gets the namespace being checked for.
     * 
     * @return namespace URI
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace to check for.
     * 
     * @param ns namespace URI as a string
     */
    public void setNamespace(final String ns) {
        if (isInitialized()) {
            return;
        }

        namespace = ns;
    }

    /**
     * Processes the given {@link DomElementItem}.
     * 
     * @param item {@link DomElementItem} to process.
     */
    private void processItem(final DomElementItem item) {
        Element element = item.unwrap();

        /*
         * We can't, by definition, remove the document element from a DomElementItem, so fail quickly if the document
         * element is in the target namespace.
         */
        if (namespace.equals(element.getNamespaceURI())) {
            ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
            metadata.put(new ErrorStatus(getId(), "can't strip namespace from document element"));
            return;
        }

        processElement(element, 0);
    }

    /**
     * Process the attributes on an element.
     * 
     * Assumes that the element itself does not reside in the target namespace,
     * and that all child elements have already been processed.
     * 
     * @param element the {@link Element} to process
     */
    private void processAttributes(final Element element) {
        /*
         * Process the attributes on this element.  Because the NamedNodeMap
         * associated with an element is "live", we need to collect the attributes
         * we want to remove and do that at the end.
         */
        NamedNodeMap attributes = element.getAttributes();
        List<Attr> removeTarget = new ArrayList<Attr>();
        List<Attr> removePrefix = new ArrayList<Attr>();
        for (int aIndex = 0; aIndex < attributes.getLength(); aIndex++) {
            Attr attribute = (Attr) attributes.item(aIndex);
            String attrNamespace = attribute.getNamespaceURI();
            String attrLocalName = attribute.getLocalName();
            log.debug("checking attribute {{}}:{}", attrNamespace, attrLocalName);
            if (namespace.equals(attrNamespace)) {
                // remove attribute in target namespace
                log.debug("   in target namespace; will remove");
                removeTarget.add(attribute);
            } else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrNamespace)
                    && namespace.equals(attribute.getTextContent())) {
                // remove prefix definition
                log.debug("   prefix {} definition; will remove", attrLocalName);
                removeTarget.add(attribute);
            }
        }
        
        /*
         * Actually remove attributes we don't want any more.
         * 
         * Remove the prefix declarations last, just in case that matters.
         */
        for (Attr a: removeTarget) {
            element.removeAttributeNode(a);
        }
        for (Attr a: removePrefix) {
            element.removeAttributeNode(a);
        }
    }
    
    /**
     * Process an individual DOM element.
     * 
     * @param element element to process
     * @param depth processing depth, starting with 0 for the document element.
     */
    private void processElement(final Element element, final int depth) {
        
        log.debug("{}: element {}", depth, element.getLocalName());

        /*
         * If this element is in the target namespace, remove it from the DOM entirely and we're done.
         */
        if (namespace.equals(element.getNamespaceURI())) {
            log.debug("{}: removing element entirely", depth);
            element.getParentNode().removeChild(element);
            return;
        }

        /*
         * Recursively process the DOM below this element.
         */
        NodeList children = element.getChildNodes();
        for (int eIndex = 0; eIndex < children.getLength(); eIndex++) {
            Node child = children.item(eIndex);
            if (child instanceof Element) {
                processElement((Element) child, depth+1);
            }
        }

        /*
         * Process the attribute collection on this element,
         * including attributes acting as namespace prefix definitions.
         */
        processAttributes(element);
    }

    /** {@inheritDoc} */
    protected void doExecute(final Collection<DomElementItem> items) throws StageProcessingException {
        for (DomElementItem item : items) {
            processItem(item);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (namespace == null) {
            throw new ComponentInitializationException("target namespace can not be null or empty");
        }
    }

}