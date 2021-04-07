package grakn.client.common.collection;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Bytes {

    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public static UUID bytesToUUID(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long firstLong = buffer.getLong();
        long secondLong = buffer.getLong();
        return new UUID(firstLong, secondLong);
    }
}
