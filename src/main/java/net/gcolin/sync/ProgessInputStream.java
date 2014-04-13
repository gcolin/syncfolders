package net.gcolin.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class ProgessInputStream extends InputStream{

    private long current = 0;
    private InputStream delegate;
    private Consumer<Long> c;
    private long offset;
    
    public ProgessInputStream(long offset,InputStream delgate, Consumer<Long> c)
    {
        this.offset = offset;
        this.c = c;
        this.delegate = delgate;
    }
    
    @Override
    public int read() throws IOException {
        current++;
        if(current%offset==0)
        {
            c.accept(current);
        }
        return delegate.read();
    }
    
    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public long getCurrent() {
        return current;
    }

}
