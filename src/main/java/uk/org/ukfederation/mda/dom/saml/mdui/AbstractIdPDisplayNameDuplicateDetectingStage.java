/*
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

package uk.org.ukfederation.mda.dom.saml.mdui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.FirstItemIdItemIdentificationStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemIdentificationStrategy;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;
import net.shibboleth.utilities.java.support.logic.Constraint;
import uk.org.ukfederation.mda.validate.mdui.MDUISupport;

/**
 * An abstract base class providing the main logic for {@link Stage} implementations that
 * detect entities with clashing display names.
 *
 * Subclass implementations implement the appropriate action on detected entities.
 */
public abstract class AbstractIdPDisplayNameDuplicateDetectingStage extends BaseStage<Element> {

    /** {@link QName} representing an SAML metadata <code>IDPSSODescriptor</code>. */
    private static final QName MD_IDP_SSO_DESCRIPTOR = new QName(SAMLMetadataSupport.MD_NS, "IDPSSODescriptor");

    /** {@link QName} representing a SAML metadata <code>OrganizationDisplayName</code>. */
    private static final QName MD_ORG_DISPLAY_NAME = new QName(SAMLMetadataSupport.MD_NS, "OrganizationDisplayName");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractIdPDisplayNameDuplicateDetectingStage.class);

    /**
     * Item identification strategy to use for duplicated entities.
     *
     * Default: {@link FirstItemIdItemIdentificationStrategy}.
     */
    @Nonnull
    private ItemIdentificationStrategy identificationStrategy = new FirstItemIdItemIdentificationStrategy();

    /**
     * Gets the item identification strategy to be used.
     *
     * @return the {@link ItemIdentificationStrategy} value
     */
    @Nonnull
    public ItemIdentificationStrategy getIdentificationStrategy() {
        return identificationStrategy;
    }

    /**
     * Sets the item identification strategy to be used.
     *
     * @param strategy the {@link ItemIdentificationStrategy} to use
     */
    public void setIdentificationStrategy(@Nonnull final ItemIdentificationStrategy strategy) {
        Constraint.isNotNull(strategy, "identification strategy may not be null");
        identificationStrategy = strategy;
    }

    /**
     * Determines whether the given <code>element</code> has at least one child named by <code>qname</code>.
     * 
     * @param element {@link Element} whose children should be inspected
     * @param qname {@link QName} to look for
     *
     * @return <code>true</code> if <code>element</code> has at least one child <code>qname</code> element.
     */
    private boolean hasChildElement(@Nonnull final Element element, @Nonnull final QName qname) {
        assert element != null;
        assert qname != null;
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE &&
                    n.getNamespaceURI().equals(qname.getNamespaceURI()) &&
                    n.getLocalName().equals(qname.getLocalPart())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collect the display names introduced by the given {@link QName} by the entity
     * given by {@link Element} into the provided collection.
     *
     * @param entity <code>EntityDescriptor</code> to look for display names inside
     * @param qname element name containing display names
     * @param names collection of names to add into
     */
    private void collectNames(@Nonnull final Element entity, @Nonnull final QName qname,
            @Nonnull final Set<String> names) {
        assert entity != null;
        assert qname != null;
        assert names != null;
        final NodeList elements = entity.getElementsByTagNameNS(qname.getNamespaceURI(), qname.getLocalPart());
        for (int n = 0; n < elements.getLength(); n++) {
            final Element element = (Element)elements.item(n);
            final String textContent = element.getTextContent();
            if (log.isTraceEnabled()) {
                log.trace("seen display name '" + textContent + "'");
            }
            /*
             * We have found a name.  Trim whitespace off it to improve the changes
             * of a match without altering it fundamentally ("Example" and "Example "
             * should be regarded as a match).
             */
            names.add(textContent.trim());
        }
    }

    /**
     * Extract the set of display names used by this entity.
     * 
     * @param element the EntityDescriptor element to extract names from.
     * 
     * @return set of display name strings
     */
    @Nonnull
    private Set<String> extractDisplayNames(@Nonnull final Element element) {
        assert element != null;
        final Set<String> displayNames = new HashSet<>();
        collectNames(element, MDUISupport.MDUI_DISPLAY_NAME, displayNames);
        collectNames(element, MD_ORG_DISPLAY_NAME, displayNames);
        return displayNames;
    }

    /**
     * Detects whether a given EntityDescriptor represents an identity provider by
     * looking for an IDPSSORoleDescriptor.
     * 
     * @param entity {@link Element} representing the EntityDescriptor.
     * 
     * @return true if the entity is an identity provider.
     */
    private boolean isIdentityProvider(@Nonnull final Element entity) {
        assert entity != null;
        return hasChildElement(entity, MD_IDP_SSO_DESCRIPTOR);
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final Collection<Item<Element>> items)
            throws StageProcessingException {

        /*
         * Record of the items corresponding to the first entity seen with each display name.
         */
        final Map<String, Item<Element>> ids = new HashMap<>(items.size());

        /*
         * Remember which entities we have already marked with errors.
         */
        final Set<Item<Element>> markedItems = new HashSet<>();

        for (Item<Element> item : items) {
           final Element entity = item.unwrap();
           final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();

           if (!SAMLMetadataSupport.isEntityDescriptor(entity)) {
               // all items must be EntityDescriptor elements
               metadata.put(new ErrorStatus(getId(), "item was not an EntityDescriptor"));
           } else if (isIdentityProvider(entity)) {
               final Set<String> displayNames = extractDisplayNames(entity);
               for (String name: displayNames) {
                   final String key = name.toLowerCase();
                   final Item<Element> that = ids.get(key);
                   if (that == null) {
                       // all is well
                       ids.put(key, item);
                   } else if (that != item) {
                       final String thisId = identificationStrategy.getItemIdentifier(item);
                       final String thatId = identificationStrategy.getItemIdentifier(that);

                       markEntity(item, name, thisId, thatId);
                       markedItems.add(item);

                       if (!markedItems.contains(that)) {
                           // only label each entity once
                           markEntity(that, name, thatId, thisId);
                           markedItems.add(that);
                       }
                   }
               }
           }

        }
    }

    /**
     * Helper function to mark an entity by adding to its item metadata.
     *
     * @param item      {@link Item} to be marked
     * @param name      display name which is clashing 
     * @param thisId    identifier of the entity the error is being added to
     * @param thatId    identifier of the entity the clash is with
     */
    private void markEntity(@Nonnull final Item<Element> item,
            @Nonnull final String name,
            @Nonnull final String thisId, @Nonnull final String thatId) {
        final String message = "duplicate display name '" + name + "' clashes with " + thatId;
        log.debug("on " + thisId + ": " + message);
        addMarks(item, message);
    }

    /**
     * Add the marks appropriate for the implementing subclass.
     *
     * @param item {@link Item} to be marked
     * @param message message to be put into the item metadata
     */
    protected abstract void addMarks(@Nonnull Item<Element> item, @Nonnull final String message);

    @Override
    protected void doDestroy() {
        super.doDestroy();
        identificationStrategy = null;
    }

}
