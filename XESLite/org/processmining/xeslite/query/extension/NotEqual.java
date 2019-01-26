package org.processmining.xeslite.query.extension;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;

public class NotEqual<O, A> extends SimpleQuery<O, A> {

    private final A value;

    public NotEqual(Attribute<O, A> attribute, A value) {
        super(attribute);
        this.value = value;
    }

    public A getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "notequal(" + super.getAttribute().getObjectType().getSimpleName() + "." + super.getAttributeName() +
                ", " + value +
                ")";
    }

    /* (non-Javadoc)
     * @see com.googlecode.cqengine.query.simple.SimpleQuery#matchesSimpleAttribute(com.googlecode.cqengine.attribute.SimpleAttribute, java.lang.Object, com.googlecode.cqengine.query.option.QueryOptions)
     */
    @Override
    protected boolean matchesSimpleAttribute(SimpleAttribute<O, A> attribute, O object, QueryOptions queryOptions) {
        return !value.equals(attribute.getValue(object, queryOptions));
    }

    /* (non-Javadoc)
     * @see com.googlecode.cqengine.query.simple.SimpleQuery#matchesNonSimpleAttribute(com.googlecode.cqengine.attribute.Attribute, java.lang.Object, com.googlecode.cqengine.query.option.QueryOptions)
     */
    @Override
    protected boolean matchesNonSimpleAttribute(Attribute<O, A> attribute, O object, QueryOptions queryOptions) {
        for (A attributeValue : attribute.getValues(object, queryOptions)) {
            if (!value.equals(attributeValue)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotEqual)) return false;

        NotEqual<?, ?> equal = (NotEqual<?, ?>) o;

        if (!attribute.equals(equal.attribute)) return false;
        if (!value.equals(equal.value)) return false;

        return true;
    }

    /* (non-Javadoc)
     * @see com.googlecode.cqengine.query.simple.SimpleQuery#calcHashCode()
     */
    @Override
    protected int calcHashCode() {
        int result = attribute.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}

