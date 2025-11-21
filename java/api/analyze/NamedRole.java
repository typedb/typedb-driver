package com.typedb.driver.api.analyze;

/**
 * 'links' & 'relates' constraints accept unscoped role names.
 * Since an unscoped role-name does not uniquely identify a role-type,
 *  (Different role-types belonging to different relation types may share the same name)
 *  an internal variable is introduced to handle the ambiguity
 */
public interface NamedRole {

    /**
     * The internal variable injected to handle ambiguity in unscoped role names.
     *
     * @return the variable associated with this named role
     */
    Variable variable();

    /**
     * The unscoped role name specified in the query.
     *
     * @return the unscoped name of the role
     */
    String name();
}
