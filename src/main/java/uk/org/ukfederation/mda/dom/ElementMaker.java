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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.w3c.dom.Element;

import com.google.common.base.Function;

/**
 * Basic maker class for {@link Element}s for use with the {@link Container} system.
 */
@ThreadSafe
public class ElementMaker implements Function<Container, Element> {

    /** Qualified name for the {@link Element} to be created. */
    @Nonnull private final QName name;
    
    /**
     * Constructor.
     * 
     * @param qname qualified name for the {@link Element} to be created
     */
    public ElementMaker(@Nonnull final QName qname) {
        name = qname;
    }
    
    @Override
    public Element apply(Container input) {
        return ElementSupport.constructElement(input.unwrap().getOwnerDocument(), name);
    }
    
}