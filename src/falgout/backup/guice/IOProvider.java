package falgout.backup.guice;

import java.io.IOException;

import com.google.inject.throwingproviders.CheckedProvider;

public interface IOProvider<T> extends CheckedProvider<T> {
    @Override
    public T get() throws IOException;
}
