package com.community.api.component;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;

public class BufferedServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream inputStream;

    public BufferedServletInputStream(byte[] buffer) {
        this.inputStream = new ByteArrayInputStream(buffer);
    }

    @Override
    public boolean isFinished() {
        return inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read() {
        return inputStream.read();
    }
}
