package com.severell.core.http;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheNotFoundException;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.view.View;
import com.severell.core.view.ViewMustacheDriver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ResponseTest {

    @Test
    public void testView() throws Exception {
        HttpServletResponse r = mock(HttpServletResponse.class);
        Container c = mock(Container.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(c.make(DefaultMustacheFactory.class)).willReturn(mf);
        Mustache m = mock(Mustache.class);
        given(mf.compile(any(String.class))).willReturn(m);
        Writer w = mock(Writer.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        given(r.getWriter()).willReturn(printWriter);
        given(m.execute(any(Writer.class), any(HashMap.class))).willReturn(w);

        View view = mock(ViewMustacheDriver.class);
        given(c.make(View.class)).willReturn(view);


        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("something", "else");

        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("something", "else");

        ResponseOld resp = new ResponseOld(r, c);
        resp.render("sometemplate", data);

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HashMap<String, Object>> dataCapt = ArgumentCaptor.forClass(HashMap.class);
        ArgumentCaptor<String> baseDirCapt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Writer> writerArgumentCaptor = ArgumentCaptor.forClass(Writer.class);

        verify(view).render(templateCaptor.capture(), dataCapt.capture(),baseDirCapt.capture(), writerArgumentCaptor.capture());

        assertEquals("sometemplate", templateCaptor.getValue());

        assertEquals(expectedData, dataCapt.getValue());
    }

    @Test
    public void testViewWithShareData() throws Exception {
        HttpServletResponse r = mock(HttpServletResponse.class);
        Container c = mock(Container.class);
        DefaultMustacheFactory mf = mock(DefaultMustacheFactory.class);
        given(c.make(DefaultMustacheFactory.class)).willReturn(mf);
        Mustache m = mock(Mustache.class);
        given(mf.compile(any(String.class))).willReturn(m);
        Writer w = mock(Writer.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        given(r.getWriter()).willReturn(printWriter);
        given(m.execute(any(Writer.class), any(HashMap.class))).willReturn(w);
        HashMap<String, Object> data = new HashMap<String, Object>();


        HashMap<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("otherdata", "moredata");

        View view = mock(ViewMustacheDriver.class);
        given(c.make(View.class)).willReturn(view);

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HashMap<String, Object>> dataCapt = ArgumentCaptor.forClass(HashMap.class);
        ArgumentCaptor<String> baseDirCapt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Writer> writerArgumentCaptor = ArgumentCaptor.forClass(Writer.class);


        ResponseOld resp = new ResponseOld(r, c);
        resp.share("otherdata", "moredata");
        resp.render("sometemplate", data);

        verify(view).render(templateCaptor.capture(), dataCapt.capture(),baseDirCapt.capture(), writerArgumentCaptor.capture());

        assertEquals("sometemplate", templateCaptor.getValue());

        assertEquals(expectedData, dataCapt.getValue());
    }

    @Test
    public void testViewNoMustacheFactory() throws Exception {
        if(!Config.isLoaded()) {
            Config.setDir("src/test/resources");
            Config.loadConfig();
        }
        HttpServletResponse r = mock(HttpServletResponse.class);
        Container c = mock(Container.class);
        View view = new ViewMustacheDriver(c);
        given(c.make(View.class)).willReturn(view);

        ResponseOld resp = new ResponseOld(r, c);
        assertThrows(MustacheNotFoundException.class, () -> {
            resp.render("sometemplate", new HashMap<>());
        });

        Config.unload();
    }


    @Test
    public void responseHeadersTest() {
        HttpServletResponse r = mock(HttpServletResponse.class);
        ResponseOld resp = new ResponseOld(r, null);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Test1", "Value1");
        headers.put("Test2", "Value2");

        resp.headers(headers);

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valCap = ArgumentCaptor.forClass(String.class);

        verify(r, times(2)).addHeader(keyCap.capture(), valCap.capture());

        assertEquals("Test1", keyCap.getAllValues().get(0));
        assertEquals("Test2", keyCap.getAllValues().get(1));

        assertEquals("Value1", valCap.getAllValues().get(0));
        assertEquals("Value2", valCap.getAllValues().get(1));
    }

    @Test
    public void responseNullHeadersTest() {
        HttpServletResponse r = mock(HttpServletResponse.class);
        ResponseOld resp = new ResponseOld(r, null);

        resp.headers(null);

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valCap = ArgumentCaptor.forClass(String.class);

        verify(r, times(0)).addHeader(keyCap.capture(), valCap.capture());
    }

    @Test
    public void testRedirect() throws IOException {
        HttpServletResponse r = mock(HttpServletResponse.class);
        ResponseOld resp = new ResponseOld(r, null);

        resp.redirect("/somewhere");

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);

        verify(r, times(1)).sendRedirect(keyCap.capture());

        assertEquals("/somewhere", keyCap.getValue());
    }
}
