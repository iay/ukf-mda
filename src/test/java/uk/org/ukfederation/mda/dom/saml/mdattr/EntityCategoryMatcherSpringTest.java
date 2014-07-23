
package uk.org.ukfederation.mda.dom.saml.mdattr;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;

/**
 * This is the same as {@link EntityCategoryMatcherTest}, but pulls the matcher beans
 * from a configured application context. This is just to make sure that Spring can
 * distinguish between the two constructor signatures propertly, so we don't need an
 * equivalent for every class under test.
 */
@ContextConfiguration("EntityCategoryMatcherSpringTest-config.xml")
public class EntityCategoryMatcherSpringTest extends AbstractTestNGSpringContextTests {

    private void test(final boolean expected, final Predicate<EntityAttributeContext> matcher,
            final EntityAttributeContext context) {
        Assert.assertEquals(expected, matcher.apply(context), context.toString());
    }
    
    @Test
    public void testNoRA() {
        final Predicate<EntityAttributeContext> matcher =
                (Predicate<EntityAttributeContext>)applicationContext.getBean("categoryMatcherNoRA");
        
        // all four components match
        test(true, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "whatever"));

        // context has no RA
        test(true, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));

        // these matches should fail because one component differs
        test(false, matcher, new SimpleEntityAttributeContext("category2", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category-support",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", null));
    }

    @Test
    public void testWithRA() {
        final Predicate<EntityAttributeContext> matcher =
                (Predicate<EntityAttributeContext>)applicationContext.getBean("categoryMatcherWithRA");
        
        // all four components match
        test(true, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));

        // context has no RA
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));

        // these matches should fail because one component differs
        test(false, matcher, new SimpleEntityAttributeContext("category2", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category-support",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", "registrar"));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar2"));
    }
    
}