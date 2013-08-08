package falgout.backup.app;

import java.util.UUID;

public abstract class AbstractDeviceData implements DeviceData {
    protected AbstractDeviceData() {}
    
    @Override
    public abstract UUID getID();
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getID() == null) ? 0 : getID().hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof AbstractDeviceData)) { return false; }
        AbstractDeviceData other = (AbstractDeviceData) obj;
        if (getID() == null) {
            if (other.getID() != null) { return false; }
        } else if (!getID().equals(other.getID())) { return false; }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(" [getID()=");
        builder.append(getID());
        builder.append("]");
        return builder.toString();
    }
}
