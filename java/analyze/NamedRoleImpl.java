package com.typedb.driver.analyze;

import com.typedb.driver.api.analyze.NamedRole;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.jni.typedb_driver;

public class NamedRoleImpl extends NativeObject<com.typedb.driver.jni.NamedRole> implements NamedRole {
    NamedRoleImpl(com.typedb.driver.jni.NamedRole nativeObject) {
        super(nativeObject);
    }

    public com.typedb.driver.jni.Variable variable() {
        return typedb_driver.named_role_get_variable(nativeObject);
    }

    public String name() {
        return typedb_driver.named_role_get_name(nativeObject);
    }

    @Override
    public String toString() {
        return typedb_driver.named_role_to_string(nativeObject);
    }
}
