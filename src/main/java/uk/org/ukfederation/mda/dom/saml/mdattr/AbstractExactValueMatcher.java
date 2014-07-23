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
 * An abstract entity attribute matcher implementation that matches an exact
 * combination of value, name and name format. Optionally, a registration
 * authority value may also be matched against.
 */
@ThreadSafe
public abstract class AbstractExactValueMatcher extends AbstractEntityAttributeMatcher {

    /** The attribute value to match. */
    private final String value;

    /** The attribute <code>Name</code> to match. */
    private final String name;
    
    /** The attribute <code>NameFormat</code> to match. */
    private final String nameFormat;
    
    /** Registration authority to match against, or <code>null</code>. */
    @Nullable
    private final String registrationAuthority;

    /**
     * Constructor.
     * 
     * @param matchValue attribute value to match
     * @param matchName attribute name to match
     * @param matchNameFormat attribute name format to match
     * @param matchRegAuth entity registration authority to match, or <code>null</code>
     */
    public AbstractExactValueMatcher(@Nonnull final String matchValue,
            @Nonnull final String matchName, @Nonnull final String matchNameFormat,
            @Nullable final String matchRegAuth) {
        super();
        value = matchValue;
        name = matchName;
        nameFormat = matchNameFormat;
        registrationAuthority = matchRegAuth;
    }

    @Override
    protected boolean matchAttributeValue(@Nonnull final EntityAttributeContext input) {
        return value.equals(input.getValue());
    }

    @Override
    protected boolean matchAttributeName(EntityAttributeContext input) {
         return name.equals(input.getName());
    }

    @Override
    protected boolean matchAttributeNameFormat(@Nonnull final EntityAttributeContext input) {
        return nameFormat.equals(input.getNameFormat());
    }

    @Override
    protected boolean matchRegistrationAuthority(EntityAttributeContext input) {
        if (registrationAuthority == null) {
            // ignore the context's registration authority value
            return true;
        } else {
            return registrationAuthority.equals(input.getRegistrationAuthority());
        }
    }

}