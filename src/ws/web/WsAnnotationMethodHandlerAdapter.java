package ws.web;


import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.util.*;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.support.HandlerMethodInvocationException;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.UrlPathHelper;
import ws.annotation.HttpWebService;
import ws.annotation.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author: jkyang
 */
public class WsAnnotationMethodHandlerAdapter extends WebContentGenerator implements HandlerAdapter, Ordered {

    private final Set<Method> handlerMethods = new LinkedHashSet<Method>();

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    private PathMatcher pathMatcher = new AntPathMatcher();

    private int order = Ordered.LOWEST_PRECEDENCE - 100;

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();


    public boolean supports(Object handler) {
        HttpWebService mapping = AnnotationUtils.findAnnotation(handler.getClass(), HttpWebService.class);
        return mapping != null;
    }


    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Class handlerClass = ClassUtils.getUserClass(handler);
        String lookupPath = urlPathHelper.getLookupPathForRequest(request);
        init(handlerClass);
        for (Method handlerMethod : handlerMethods) {
            Path path = AnnotationUtils.findAnnotation(handlerMethod, Path.class);
            String[] ps = path.value();
            if (ps != null) {
                for (String p : ps) {
                    if (isPathMatchInternal(p, lookupPath)) {
                        ServletWebRequest webRequest = new ServletWebRequest(request, response);
                        ExtendedModelMap implicitModel = new BindingAwareModelMap();
                        Object result = invokeHandlerMethod(handlerMethod, handler, webRequest, implicitModel);
                        if (handlerMethod.getReturnType() != Void.class && handlerMethod.getReturnType() != void.class) {
                            if (result == null) {
                                result = "null";
                            }
                        }
                        ModelAndView mav = getModelAndView(handlerMethod, handler.getClass(), result, implicitModel, webRequest);
                        return mav;
                    }
                }
            }
        }
        return null;
    }

    private boolean isPathMatchInternal(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath) || pathMatcher.match(pattern, lookupPath)) {
            return true;
        }
        boolean hasSuffix = pattern.indexOf('.') != -1;
        if (!hasSuffix && pathMatcher.match(pattern + ".*", lookupPath)) {
            return true;
        }
        boolean endsWithSlash = pattern.endsWith("/");
        if (!endsWithSlash && pathMatcher.match(pattern + "/", lookupPath)) {
            return true;
        }
        return false;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        return 0;
    }

    protected boolean isHandlerMethod(Method method) {
        return AnnotationUtils.findAnnotation(method, Path.class) != null;
    }

    public void init(final Class<?> handlerType) {
        if (handlerMethods.size() > 0) {
            return;
        }
        Set<Class<?>> handlerTypes = new LinkedHashSet<Class<?>>();
        Class<?> specificHandlerType = null;
        if (!Proxy.isProxyClass(handlerType)) {
            handlerTypes.add(handlerType);
            specificHandlerType = handlerType;
        }
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            final Class<?> targetClass = (specificHandlerType != null ? specificHandlerType : currentHandlerType);
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                    if (isHandlerMethod(specificMethod) && (bridgedMethod == specificMethod || !isHandlerMethod(bridgedMethod))) {
                        handlerMethods.add(specificMethod);
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }

    }


    public int getOrder() {
        return order;
    }


    public Object invokeHandlerMethod(Method handlerMethod, Object handler,
                                      NativeWebRequest webRequest, ExtendedModelMap implicitModel) throws Exception {

        Method handlerMethodToInvoke = BridgeMethodResolver.findBridgedMethod(handlerMethod);
        try {
            boolean debug = logger.isDebugEnabled();
            Object[] args = resolveHandlerArguments(handlerMethodToInvoke, handler, webRequest, implicitModel);
            logger.info(args == null ? "null" : Arrays.asList(args));
            if (debug) {
                logger.debug("Invoking request handler method: " + handlerMethodToInvoke);
            }
            ReflectionUtils.makeAccessible(handlerMethodToInvoke);
            return handlerMethodToInvoke.invoke(handler, args);
        } catch (IllegalStateException ex) {
            // Internal assertion failed (e.g. invalid signature):
            // throw exception with full handler method context...
            throw new HandlerMethodInvocationException(handlerMethodToInvoke, ex);
        } catch (InvocationTargetException ex) {
            // User-defined @ModelAttribute/@InitBinder/@RequestMapping method threw an exception...
            ReflectionUtils.rethrowException(ex.getTargetException());
            return null;
        }
    }


    private Object[] resolveHandlerArguments(Method handlerMethod, Object handler, NativeWebRequest webRequest, ExtendedModelMap implicitModel) throws Exception {

        Class[] paramTypes = handlerMethod.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        Path p = AnnotationUtils.findAnnotation(handlerMethod, Path.class);
        if (p.paramNames().length != paramTypes.length) {
            throw new IllegalArgumentException("please check the Path's paramNames.");
        }
        for (int i = 0; i < args.length; i++) {
            //0, 1
            String paramName = null;
            if (p.paramNames().length > i) {
                paramName = p.paramNames()[i];
            }

            MethodParameter methodParam = new MethodParameter(handlerMethod, i);
            methodParam.initParameterNameDiscovery(this.parameterNameDiscoverer);
            GenericTypeResolver.resolveParameterType(methodParam, handler.getClass());
            boolean required = false;
            String defaultValue = null;
            args[i] = resolveRequestParam(paramName, required, defaultValue, methodParam, webRequest, handler);
        }

        return args;
    }

    private Map resolveRequestParamMap(Class<? extends Map> mapType, NativeWebRequest webRequest) {
        Map<String, String[]> parameterMap = webRequest.getParameterMap();
        if (MultiValueMap.class.isAssignableFrom(mapType)) {
            MultiValueMap<String, String> result = new LinkedMultiValueMap<String, String>(parameterMap.size());
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                for (String value : entry.getValue()) {
                    result.add(entry.getKey(), value);
                }
            }
            return result;
        } else {
            Map<String, String> result = new LinkedHashMap<String, String>(parameterMap.size());
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                if (entry.getValue().length > 0) {
                    result.put(entry.getKey(), entry.getValue()[0]);
                }
            }
            return result;
        }
    }

    private Object checkValue(String name, Object value, Class paramType) {
        if (value == null) {
            if (boolean.class.equals(paramType)) {
                return Boolean.FALSE;
            } else if (paramType.isPrimitive()) {
                throw new IllegalStateException("Optional " + paramType + " parameter '" + name +
                        "' is not present but cannot be translated into a null value due to being declared as a " +
                        "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.");
            }
        }
        return value;
    }

    private Object resolveRequestParam(String paramName, boolean required, String defaultValue,
                                       MethodParameter methodParam, NativeWebRequest webRequest, Object handlerForInitBinderCall)
            throws Exception {

        Class<?> paramType = methodParam.getParameterType();
        if (Map.class.isAssignableFrom(paramType) && paramName.length() == 0) {
            return resolveRequestParamMap((Class<? extends Map>) paramType, webRequest);
        }

        Object paramValue = null;
        MultipartRequest multipartRequest = webRequest.getNativeRequest(MultipartRequest.class);
        if (multipartRequest != null) {
            List<MultipartFile> files = multipartRequest.getFiles(paramName);
            if (!files.isEmpty()) {
                paramValue = (files.size() == 1 ? files.get(0) : files);
            }
        }
        if (paramValue == null) {
            String[] paramValues = webRequest.getParameterValues(paramName);
            if (paramValues != null) {
                paramValue = (paramValues.length == 1 ? paramValues[0] : paramValues);
            }
        }
        if (paramValue == null) {

            paramValue = checkValue(paramName, paramValue, paramType);
        }
        //To
        if (paramValue instanceof String) {
            String pv = (String) paramValue;
            logger.trace(paramType);
            if (pv.startsWith("[") || pv.startsWith("{")) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readValue((String) paramValue, paramType);
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            } else if (pv.indexOf(",") > -1) {
                paramValue = StringUtils.commaDelimitedListToStringArray(pv);
            }
        }


        WebDataBinder binder = createBinder(webRequest, null, paramName);

        return binder.convertIfNecessary(paramValue, paramType, methodParam);
    }


    protected WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
            throws Exception {

        return new WebRequestDataBinder(target, objectName);
    }


    public ModelAndView getModelAndView(Method handlerMethod, Class handlerType, Object returnValue,
                                        ExtendedModelMap implicitModel, ServletWebRequest webRequest) throws Exception {
        if (returnValue == null) {
            // Either returned null or was 'void' return.
            if (webRequest.isNotModified()) {
                return null;
            } else {
                WsResult result =  new WsResult(WsResult.ResultStatus.SUCCESS) ;
                result.setMessage("success");
                returnValue = result;
            }
        }
        webRequest.getResponse().getWriter().println(getJson(returnValue));
        return null;
    }


    public static String getJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter s = new StringWriter();
            mapper.writeValue(s, object);
            return s.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
