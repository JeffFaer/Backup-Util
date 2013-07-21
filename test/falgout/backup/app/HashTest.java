package falgout.backup.app;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

public class HashTest {
    @Test
    public void HashIsImmutable() {
        byte[] b = { 1, 2, 3, 4, 5 };
        byte[] b2 = Arrays.copyOf(b, 5);
        Hash h = new Hash(b2);
        byte[] h2 = h.getHash();
        b2[0] = 9;
        assertArrayEquals(b, h2);
        h2[0] = 9;
        byte[] h3 = h.getHash();
        assertArrayEquals(b, h3);
    }
}
