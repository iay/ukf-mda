/*
 * Copyright (C) 2015 University of Edinburgh.
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

package uk.org.ukfederation.mda.dom;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.AbstractDOMTraversalStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

/**
 * A stage to examine all the text in {@link Element}-based {@link Item}s (text nodes and attributes) and
 * mark them as being in error if a CR character appears. This can only be the case if the XML
 * document contained an explicit character reference such as <code>&amp;#13;</code>.
 * 
 * This stage is specifically intended to detect metadata which would trigger the SSPCPP-684
 * issue in the Shibboleth SP.
 * 
 * @see <a href="https://issues.shibboleth.net/jira/browse/SSPCPP-684">SSPCPP-684</a>
 */
public class CRDetectionStage extends AbstractDOMTraversalStage {

    /** Character value we are looking for. */
    private static final char CR = '\r';
    
    @Override
    protected boolean applicable(final Element element) {
        // all Elements are applicable
        return true;
    }

    @Override
    protected void visit(final Element element, final TraversalContext context) throws StageProcessingException {
        // Only permit one error; short-circuit any further examinations
        if (!context.getStash().isEmpty()) {
            return;
        }

        final Item<Element> item = context.getItem();

        // Check all text node children of the element
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            /*
             * There are three kinds of node capable of including character data. We only need to
             * check TEXT_NODES: CDATA sections and comments can't include CR characters, as
             * character references are not interpreted in either context.
             */
            if (node.getNodeType() == Node.TEXT_NODE && node.getNodeValue().indexOf(CR) >= 0) {
                addError(item, element, "element text content contains a carriage return character");
                context.getStash().put(Boolean.TRUE);
                return;
            }
        }

        // Also check any attributes on the element
        final NamedNodeMap attributes = element.getAttributes();
        for (int index=0; index<attributes.getLength(); index++) {
            final Node attribute = attributes.item(index);
            if (attribute.getNodeValue().indexOf(CR) >= 0) {
                addError(item, element, "attribute value contains a carriage return character");
                context.getStash().put(Boolean.TRUE);
                return;
            }
        }
    }

}