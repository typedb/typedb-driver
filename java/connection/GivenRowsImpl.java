package com.typedb.driver.connection;

import com.typedb.driver.common.NativeObject;
import com.typedb.driver.api.GivenRows;

public class GivenRowsImpl extends NativeObject<com.typedb.driver.jni.GivenRows> implements GivenRows {
    protected GivenRowsImpl(com.typedb.driver.jni.GivenRows nativeObject) {
        super(nativeObject);
    }
}
