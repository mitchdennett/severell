package com.severell.core.http;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The router holds all the routes and is responsible for returning the route
 * for a given path.
 */
public class Router {

    private HashMap<String, RouteNode> trees;
    private static ArrayList<Route> routes = new ArrayList<Route>();
    private static ArrayList<RouteExecutor> compiledRoutes = new ArrayList<RouteExecutor>();

    /**
     * Register a GET route for the given path
     *
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route get(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, cl, method, "GET");
        Route headRoute = new Route(path, cl, method, "HEAD");
        routes.add(route);
        routes.add(headRoute);
        return route;
    }

    /**
     * Register a POST route for the given path
     *
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route post(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, cl, method, "POST");
        routes.add(route);
        return route;
    }

    /**
     * Register a PUT route for the given path
     *
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route put(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, cl, method, "PUT");
        routes.add(route);
        return route;
    }

    /**
     * Register a Patch route for the given path
     *
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route patch(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, cl, method, "PATCH");
        routes.add(route);
        return route;
    }

    /**
     * Register a DELETE route for the given path
     *
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route delete(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path, cl, method, "DELETE");
        routes.add(route);
        return route;
    }

    /**
     * Register an OPTIONS route for the given path
     *
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Route options(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        Route route = new Route(path,cl, method, "OPTIONS");
        routes.add(route);
        return route;
    }

    /**
     * Register a GET route for the given path
     * @deprecated This will be removed in the next MAJOR version.
     * Please use lowercase get instead.
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public static Route Get(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        return get(path, cl, method);
    }

    /**
     * Register a POST route for the given path
     * @deprecated This will be removed in the next MAJOR version.
     * Please use lowercase post instead.
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public static Route Post(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        return post(path, cl, method);
    }

    /**
     * Register a PUT route for the given path
     * @deprecated This will be removed in the next MAJOR version.
     * Please use lowercase put instead.
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public static Route Put(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        return put(path, cl, method);
    }

    /**
     * Register a Patch route for the given path
     * @deprecated This will be removed in the next MAJOR version.
     * Please use lowercase patch instead.
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public static Route Patch(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        return patch(path, cl, method);
    }

    /**
     * Register a DELETE route for the given path
     * @deprecated This will be removed in the next MAJOR version.
     * Please use lowercase delete instead.
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public static Route Delete(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        return delete(path, cl, method);
    }

    /**
     * Register an OPTIONS route for the given path
     * @deprecated This will be removed in the next MAJOR version.
     * Please use lowercase options instead.
     * @param path
     * @param cl Controller Class
     * @param method
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public static Route Options(String path, Class cl, String method) throws NoSuchMethodException, ClassNotFoundException {
        return options(path, cl, method);
    }

    /**
     * Clear all routes.
     */
    protected static void clearRoutes() {
        routes = new ArrayList<>();
        compiledRoutes = new ArrayList<>();
    }

    /**
     * This is used to retrieve a route for a given path
     *
     * @param path The path
     * @param httpMethod HTTP method (i.e GET)
     * @param req The request object
     * @return
     */
    public RouteExecutor lookup(String path, String httpMethod, Request req)
    {
        RouteNode traverseNode = trees.get(httpMethod);

        if(traverseNode == null){
            return null;
        }

        walk:
        for(;;) {
            String prefix = traverseNode.getPath();

            if(path.length() > prefix.length()) {
                if(path.substring(0, prefix.length()).equals(prefix)) {
                    path = path.substring(prefix.length());

                    if(!traverseNode.isWildCard()) {
                        for (RouteNode child : traverseNode.getChildren()) {

                            if (child.getPath().charAt(0) == path.charAt(0)) {
                                traverseNode = child;
                                continue walk;
                            }
                        }

                        return null;
                    }

                    traverseNode = traverseNode.getChildren().get(0);
                    switch (traverseNode.getType()) {
                        case Param:
                            int end = 0;
                            for(;end < path.length() && path.charAt(end) != '/';) {
                                end++;
                            }

                            req.addParam(traverseNode.getPath().substring(1), path.substring(0, end));

                            if(end < path.length()) {
                                if(traverseNode.getChildren().size() > 0) {
                                    path = path.substring(end);
                                    traverseNode = traverseNode.getChildren().get(0);
                                    continue walk;
                                }

                                return null;
                            }

                           if(traverseNode.getHandle() != null){
                               return traverseNode.getHandle();
                           }
                            break;
                        case CatchAll:
                            req.addParam(traverseNode.getPath().substring(2), path);
                            return traverseNode.getHandle();
                        default:
                            return null;
                    }
                }

            } else if (path.equals(prefix)){
                return traverseNode.getHandle();
            }

            //Could do something here for trailing slashes
            return null;
        }
    }

    /**
     * After the RouteBuilder as compiled the routes we set them on the router
     * here.
     * @param routes Compiled Routes.
     */
    public static void setCompiledRoutes(ArrayList<RouteExecutor> routes) {
        compiledRoutes = routes;
    }

    /**
     * Get a list of all Routes.
     * @return
     */
    public ArrayList<Route> getRoutes() {
        return Router.routes;
    }

    /**
     * Create the Trie and finalize the router.
     * @throws Exception
     */
    public void compileRoutes() throws Exception {
        trees = new HashMap<>();

        for(RouteExecutor r : Router.compiledRoutes) {
            if(trees.containsKey(r.getHttpMethod())) {
                RouteNode tree = trees.get(r.getHttpMethod());
                tree.insert(r.getPath(), r);
            } else {
                RouteNode tree = new RouteNode();
                tree.insert(r.getPath(), r);
                trees.put(r.getHttpMethod(), tree);
            }
        }
    }
}
