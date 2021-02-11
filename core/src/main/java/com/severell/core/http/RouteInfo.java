package com.severell.core.http;

import com.severell.core.exceptions.MiddlewareException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * This class holds information for a distinct route.
 */
public class RouteInfo {


    private String path;
    private Method method;
    private HttpMethod httpMethod;
    private ArrayList<Class> middlewareClassList;

    /**
     * Create a new route
     * @param path Route path I.E. /users
     * @param cl Controller Class
     * @param method Fully qualified name of the associated controller method.
     * @param httpMethod HTTP Method I.E. POST
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public RouteInfo(String path, Class cl, String method, String httpMethod) throws ClassNotFoundException, NoSuchMethodException {
        this.path = path;
        this.method = getMethodLike(cl, method);
        this.httpMethod = HttpMethod.valueOf(httpMethod);
    }

    /**
     * Create a new route
     * @param path Route path I.E. /users
     * @param method Method.
     * @param httpMethod HTTP Method I.E. POST
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public RouteInfo(String path, Method method, HttpMethod httpMethod) throws ClassNotFoundException, NoSuchMethodException {
        this.path = path;
        this.method = method;
        this.httpMethod = httpMethod;
    }


    /**
     * Add Middleware to this route
     * @param middleware
     */
    public void middleware(Class... middleware) {
        middlewareClassList = new ArrayList<>();
        middlewareClassList.addAll(Arrays.asList(middleware));
    }

    /**
     * Returns the middleware {@link Method}. The method is only used to compile
     * the Routes and not used at Runtime.
     * @return
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Get the Route path. I.E. /users
     * @return
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Get the HTTP Request method. I.E. POST
     * @return
     */
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * Return a list of Middleware associated with
     * this route.
     * @return
     */
    public ArrayList<Class> getMiddlewareClassList() {
        return this.middlewareClassList;
    }

    /**
     * Uses Reflection to retrieve the controller {@link Method}. This is not used
     * at Runtime.
     * @param c
     * @param name
     * @return
     */
    private static Method getMethodLike(Class c, String name) {
        final Optional<Method> matchedMethod = Arrays.asList(c.getDeclaredMethods()).stream().filter(method ->
                method.getName().toLowerCase().equals(name.toLowerCase())).findAny();

        if (!matchedMethod.isPresent()) {
            throw new RuntimeException("No method containing: " + name);
        }

        return matchedMethod.get();
    }
}
