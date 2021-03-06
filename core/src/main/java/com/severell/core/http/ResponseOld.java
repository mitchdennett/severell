package com.severell.core.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.exceptions.ViewException;
import com.severell.core.view.View;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper around {@link HttpServletResponse} with simpler syntax.
 */
public class ResponseOld extends HttpServletResponseWrapper {

    /**
     * The {@link Container} to be used to resolve any dependencies.
     */
    private final Container c;

    /**
     * This holds any data that is to be passed to every mustache template.
     */
    private HashMap<String, Object> shared;

    /**
     * Constructs a response adaptor wrapping the given response.
     * @param response the {@link HttpServletResponse} to be wrapped.
     * @param c the {@link Container} object to be used
     */
    public ResponseOld(HttpServletResponse response, Container c) {
        super(response);
        this.c = c;
        shared = new HashMap<>();
    }

    /**
     * Renders a mustache template.
     *
     * @param template The path to the template. This is relative to your templates folder. Eg. auth/login.mustache
     * @param data This contains the data to be used in the template.
     * @throws IOException
     */
    public void render(String template, HashMap<String, Object> data) throws IOException, ViewException {
        this.render(template, data, "templates/");
    }

    /**
     * Renders a mustache template. Use this method if template is not located in default "template" directory
     * @param template The path to the template. Relative to the baseDir param.
     * @param data This contains the data to be used in the template.
     * @param baseDir Base directory of the template
     * @throws IOException
     */
    protected void render(String template, HashMap<String, Object> data, String baseDir) throws ViewException, IOException {
        this.setContentType("text/html");
        data.putAll(shared);
        View view = c.make(View.class);
        view.render(template, data, baseDir, this.getWriter());
    }

    /**
     * Writes the object to a JSON String and returns to client
     * @param object Object to be converted to JSON
     * @throws IOException
     */
    public void json(Object object) throws IOException {
        ObjectMapper mapper = c.make(ObjectMapper.class);
        getWriter().write(mapper.writeValueAsString(object));
    }

    /**
     * Writes a file to the client.
     * @param file
     * @param mimeType
     * @throws IOException
     */
    public void download(File file, String mimeType) throws IOException {
        download(file, mimeType, null);
    }

    /**
     * Writes a file to the client.
     * @param file
     * @param mimeType
     * @param name
     * @throws IOException
     */
    public void download(File file, String mimeType, String name) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        setHeader("Content-Type", mimeType);
        setHeader("Content-Length", String.valueOf(bytes.length));
        setHeader("Content-Disposition", "attachment; filename=\"" + name == null ? "generated" : name + "\"");
        getOutputStream().write(bytes);
    }

    /**
     * Use this to share an object with any mustache template. You can then use this object in any template
     * and don't have to explicitly pass it into the render call.
     *
     * @param key The key to be used when using this in a template
     * @param obj Object to share.
     */
    public void share(String key, Object obj) {
        shared.put(key, obj);
    }

    /**
     * Send a redirect
     *
     * @param url URL to redirect to
     * @throws IOException
     */
    public void redirect(String url) throws IOException {
        this.sendRedirect(url);
    }

    /**
     * Set headers on the response object
     *
     * @param headers Headers to set on the response object
     */
    public void headers(Map<String, String> headers) {
        if(headers != null) {
            for(String key : headers.keySet()) {
                String value = headers.get(key);
                addHeader(key, value);
            }
        }
    }

    /**
     * Renders a mustache template string.
     *
     * @param templateString A string containing a mustache template
     * @param templateName A string containing a mustache template name
     * @param data The data to be used in the template string
     * @throws IOException
     */
    public void renderTemplateLiteral(String templateString, String templateName, HashMap<String, Object> data) throws IOException {
        this.setContentType("text/html");
        MustacheFactory mf;
        mf = c.make(DefaultMustacheFactory.class);

        if(mf == null) {
            mf = new DefaultMustacheFactory();
        }

        Mustache m = mf.compile(new StringReader(templateString), templateName);
        PrintWriter writer = this.getWriter();
        data.putAll(shared);
        m.execute(writer, data).flush();
    }
}
