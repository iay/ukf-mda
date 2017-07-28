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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;

/**
 * A stage which, for each <code>EntityDescriptor</code> collection element representing an identity provider,
 * makes sure that the display name or names associated with the entity are not duplicates of any declared by
 * any other identity provider entity.
 */
@ThreadSafe
public class IdPDisplayNameDuplicateDetectingStage extends AbstractIdPDisplayNameDuplicateDetectingStage {

    @Override
    protected void addMarks(@Nonnull final Item<Element> item, @Nonnull final String message) {
        item.getItemMetadata().put(new ErrorStatus(getId(), message));
    }

}
