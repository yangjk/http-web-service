package ws.web;


import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping;
import ws.annotation.HttpWebService;
import ws.annotation.Path;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: jkyang
 */
public class WsAnnotationHandlerMapping extends AbstractDetectingUrlHandlerMapping {

    private final Map<Class, HttpWebService> cachedMappings = new HashMap<Class, HttpWebService>();

    @Override
    protected String[] determineUrlsForHandler(String beanName) {
        ApplicationContext context = getApplicationContext();
        Class<?> handlerType = context.getType(beanName);
        HttpWebService mapping = context.findAnnotationOnBean(beanName, HttpWebService.class);
        if (mapping != null) {
            // @RequestMapping found at type level
            this.cachedMappings.put(handlerType, mapping);
            Set<String> urls = new LinkedHashSet<String>();
            // actual paths specified by @Path at method level
            return determineUrlsForHandlerMethods(handlerType, false);

        }
        return new String[0];
    }

    protected String[] determineUrlsForHandlerMethods(Class<?> handlerType, final boolean hasTypeLevelMapping) {

        final Set<String> urls = new LinkedHashSet<String>();
        Set<Class<?>> handlerTypes = new LinkedHashSet<Class<?>>();
        handlerTypes.add(handlerType);
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Path mapping = AnnotationUtils.findAnnotation(method, Path.class);
                    if (mapping != null) {
                        String[] mappedPatterns = mapping.value();
                        if (mappedPatterns.length > 0) {
                            for (String mappedPattern : mappedPatterns) {
                                if (!hasTypeLevelMapping && !mappedPattern.startsWith("/")) {
                                    mappedPattern = "/" + mappedPattern;
                                }
                                addUrlsForPath(urls, mappedPattern);
                            }
                        } else if (hasTypeLevelMapping) {
                            // empty method-level RequestMapping
                            urls.add(null);
                        }
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return StringUtils.toStringArray(urls);
    }

    /**
     * Add URLs and/or URL patterns for the given path.
     * @param urls the Set of URLs for the current bean
     * @param path the currently introspected path
     */
    protected void addUrlsForPath(Set<String> urls, String path) {
        urls.add(path);

        if (path.indexOf('.') == -1 && !path.endsWith("/")) {
            urls.add(path + ".*");
            urls.add(path + "/");
        }
    }



}
