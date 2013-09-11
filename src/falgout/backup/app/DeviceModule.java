package falgout.backup.app;

import java.security.MessageDigest;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DeviceModule extends AbstractModule {
    private final MessageDigest md;
    
    public DeviceModule() {
        this(null);
    }
    
    public DeviceModule(MessageDigest md) {
        this.md = md;
    }
    
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(Device.class, DefaultDevice.class)
                .implement(DeviceData.class, DefaultDevice.Data.class)
                .build(DeviceFactory.class));
        if (md != null) {
            bind(MessageDigest.class).toInstance(md);
        } else {
            requireBinding(MessageDigest.class);
        }
    }
}
