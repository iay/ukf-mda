/*
 * Copyright (C) 2014 University of Edinburgh.
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

package uk.org.ukfederation.mda.dom.saml.mdattr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An entity attribute matcher which matches a given entity support category.
 */
@ThreadSafe
public class EntityCategorySupportMatcher extends AbstractExactValueMatcher {
    
    /**
     * Constructor.
     * 
     * @param category entity category to match
     * @param regAuth registration authority to match, or <code>null</code>
     */
    public EntityCategorySupportMatcher(@Nonnull final String category, @Nullable final String regAuth) {
        super(category, EntityCategorySupport.EC_SUPPORT_ATTR_NAME,
                EntityCategorySupport.EC_ATTR_NAME_FORMAT, regAuth);
    }
    
    /**
     * Constructor.
     * 
     * @param category entity category to match
     */
    public EntityCategorySupportMatcher(@Nonnull final String category) {
        this(category, null);
    }

}
