package falgout.backup.app;

import java.io.Serializable;
import java.util.Arrays;

public final class Hash implements Serializable {
    private static final long serialVersionUID = 9093137937410642697L;
    
    private final byte[] hash;
    
    public Hash(byte[] hash) {
        this.hash = Arrays.copyOf(hash, hash.length);
    }
    
    public byte[] getHash() {
        return Arrays.copyOf(hash, hash.length);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(hash);
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof Hash)) { return false; }
        Hash other = (Hash) obj;
        if (!Arrays.equals(hash, other.hash)) { return false; }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Hash [hash=");
        builder.append(Arrays.toString(hash));
        builder.append("]");
        return builder.toString();
    }
}
