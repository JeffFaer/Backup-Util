package falgout.backup.app;

import java.security.MessageDigest;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DeviceModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(Device.class, DefaultDevice.class)
                .implement(DeviceData.class, DefaultDevice.Data.class)
                .build(DeviceFactory.class));
        requireBinding(MessageDigest.class);
    }
}
