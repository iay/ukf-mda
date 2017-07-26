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

import net.shibboleth.metadata.StatusMetadata;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * A type of {@link StatusMetadata} indicating that this entity has a clashing display name.
 */
public class DisplayNameClashesStatus extends StatusMetadata {

    /** Serial version UID. */
    private static final long serialVersionUID = 4665290590808269661L;

    /**
     * Constructor.
     *
     * @param componentId ID of the component creating the status metadata, never null or empty
     * @param statusMessage the status message, never null or empty
     */
    public DisplayNameClashesStatus(@Nonnull @NotEmpty String componentId, @Nonnull @NotEmpty String statusMessage) {
        super(componentId, statusMessage);
    }

}
