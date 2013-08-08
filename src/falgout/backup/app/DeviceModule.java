package falgout.backup.app;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DeviceModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(Device.class, DefaultDevice.class).build(DeviceFactory.class));
    }
}
