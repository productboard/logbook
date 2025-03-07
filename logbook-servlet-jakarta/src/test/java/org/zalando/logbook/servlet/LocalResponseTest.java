package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LocalResponseTest {

    private HttpServletResponse mock;
    private LocalResponse unit;

    @BeforeEach
    void setUp() throws IOException {
        mock = mock(HttpServletResponse.class);
        when(mock.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(final WriteListener listener) {
                // nothing to do here
            }

            @Override
            public void write(final int b) {
                // serves as a null or no-op output stream
            }
        });
        unit = new LocalResponse(mock, "1");
    }

    @Test
    void shouldTeeGetOutputStream() throws IOException {
        unit.withBody();

        final ServletOutputStream os1 = unit.getOutputStream();
        final ServletOutputStream os2 = unit.getOutputStream();

        assertSame(os1, os2);

        verify(mock).getOutputStream();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldDelegateGetOutputStream() throws IOException {
        unit.withoutBody();

        unit.getOutputStream();
        unit.getOutputStream();

        verify(mock, times(2)).getOutputStream();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldDelegateClose() throws IOException {
        unit.withBody();
        unit.getOutputStream().close();
    }

    @Test
    void shouldTeeGetWriter() throws IOException {
        unit.withBody();

        final PrintWriter writer1 = unit.getWriter();
        final PrintWriter writer2 = unit.getWriter();

        assertSame(writer1, writer2);

        verify(mock).getOutputStream();
        verify(mock).getCharacterEncoding();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldDelegateGetWriter() throws IOException {
        unit.withoutBody();

        unit.getWriter();
        unit.getWriter();

        verify(mock, times(2)).getWriter();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldAllowWithBodyAfterWithoutBody() throws IOException {
        unit.withoutBody();
        unit.withBody();
    }

    @Test
    void shouldReturnNullContentTypeWhenNoContentTypeHasBeenSpecified() {
        when(mock.getContentType()).thenReturn(null);

        assertThat(unit.getContentType()).isNull();
    }

    @Test
    void shouldBeReady() throws IOException {
        unit.withBody();
        assertFalse(unit.getOutputStream().isReady());
    }

    @Test
    void shouldNotSupportWriteListener() throws IOException {
        unit.withBody();
        unit.getOutputStream().setWriteListener(mock(WriteListener.class));
    }

}
