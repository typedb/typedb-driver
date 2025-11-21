package com.typedb.driver.api.analyze;

public interface NamedRole {

    /**
     * This is an internal variable injected to handle ambiguity in role-names.
     *
     * @return the variable associated with this named role
     */
    com.typedb.driver.jni.Variable variable();

    /**
     * This is the role label specified by the user in the <code>Links</code> constraint.
     *
     * @return the name of this named role
     */
    String name();
}
