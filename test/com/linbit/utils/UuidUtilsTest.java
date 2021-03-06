package com.linbit.utils;

import java.util.UUID;

import static org.junit.Assert.*;
import org.junit.Test;

public class UuidUtilsTest
{
    @Test
    public void testIdToBytes()
    {
        long most = 13;
        long least = 42_000_000;

        UUID id = new UUID(most, least);
        byte[] asByteArray = UuidUtils.asByteArray(id);
        byte[] expected = new byte[]
        {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x0D,
            0x00, 0x00, 0x00, 0x00, (byte) 0x02, (byte) 0x80, (byte) 0xDE, (byte) 0x80
        };
        assertArrayEquals(expected,asByteArray);
    }

    @Test
    public void testBytesToId()
    {
        byte[] arr = new byte[]
        {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D,
            0x00, 0x00, 0x00, 0x00, (byte) 0x02, (byte) 0x80, (byte) 0xDE, (byte) 0x80
        };
        UUID id = UuidUtils.asUuid(arr);
        assertEquals(13, id.getMostSignificantBits());
        assertEquals(42_000_000, id.getLeastSignificantBits());
    }
}
