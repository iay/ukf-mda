/*
 * Copyright (C) 2013 University of Edinburgh.
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

import net.shibboleth.metadata.dom.DOMElementItem;

import org.w3c.dom.Node;

/**
 * Provides a variation of the Visitor pattern for performing operations on
 * DOM nodes which are part of {@link DOMElementItem} items.
 */
public interface NodeVisitor {

    /**
     * Called on each {@link Node} visited as part of the processing
     * of a {@link DOMElementItem}.
     * 
     * @param visited the {@link Node} being visited.
     * @param item the {@link DOMElementItem} which is the context for the visit.
     */
    public void visitNode(Node visited, DOMElementItem item);
    
}