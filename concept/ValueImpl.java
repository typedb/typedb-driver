package com.vaticle.typedb.client.concept;

import com.vaticle.typedb.client.api.concept.Value;
import com.vaticle.typedb.client.common.NativeObject;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_VALUE;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.client.jni.typedb_client.value_equals;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_boolean;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_date_time_as_millis;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_double;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_long;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_string;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_boolean;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_date_time;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_double;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_long;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_string;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_boolean;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_date_time_from_millis;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_double;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_long;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_string;

public class ValueImpl extends NativeObject<com.vaticle.typedb.client.jni.Value> implements Value {
  private int hash = 0;

    public ValueImpl(com.vaticle.typedb.client.jni.Value value) {
        super(value);
    }

    public static Value of(boolean value) {
        return new ValueImpl(value_new_boolean(value));
    }

    public static Value of(long value) {
        return new ValueImpl(value_new_long(value));
    }

    public static Value of(double value) {
        return new ValueImpl(value_new_double(value));
    }

    public static Value of(String value) {
        if (value == null) throw new TypeDBClientException(MISSING_VALUE);
        return new ValueImpl(value_new_string(value));
    }

    public static Value of(LocalDateTime value) {
        if (value == null) throw new TypeDBClientException(MISSING_VALUE);
        return new ValueImpl(value_new_date_time_from_millis(value.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()));
    }

    public boolean isBoolean() {
        return value_is_boolean(nativeObject);
    }

    public boolean isLong() {
        return value_is_long(nativeObject);
    }

    public boolean isDouble() {
        return value_is_double(nativeObject);
    }

    public boolean isString() {
        return value_is_string(nativeObject);
    }

    public boolean isDateTime() {
        return value_is_date_time(nativeObject);
    }

    public boolean asBoolean() {
        return value_get_boolean(nativeObject);
    }

    public long asLong() {
        return value_get_long(nativeObject);
    }

    public double asDouble() {
        return value_get_double(nativeObject);
    }

    public String asString() {
        return value_get_string(nativeObject);
    }

    public LocalDateTime asDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value_get_date_time_as_millis(nativeObject)), ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        if (isBoolean()) return Boolean.toString(asBoolean());
        else if (isLong()) return Long.toString(asLong());
        else if (isDouble()) return Double.toString(asDouble());
        else if (isString()) return asString();
        else if (isDateTime()) return asDateTime().toString();
        throw new TypeDBClientException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValueImpl that = (ValueImpl) obj;
        return value_equals(this.nativeObject, that.nativeObject);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        if (isBoolean()) return Boolean.hashCode(asBoolean());
        else if (isLong()) return Long.hashCode(asLong());
        else if (isDouble()) return Double.hashCode(asDouble());
        else if (isString()) return asString().hashCode();
        else if (isDateTime()) return asDateTime().hashCode();
        return -1;
    }
}
