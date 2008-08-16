/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parancoe.web.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.parancoe.web.validation.Validation;
import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.support.HandlerMethodResolver;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartRequest;

/**
 * Support class for invoking an annotated handler method.
 * Operates on the introspection results of a {@link HandlerMethodResolver}
 * for a specific handler type.
 *
 * <p>Used by {@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter}
 * and {@link org.springframework.web.portlet.mvc.annotation.AnnotationMethodHandlerAdapter}.
 *
 * @author Juergen Hoeller
 * @author Lucio Benfante
 * @since 2.5.2
 * @see #invokeHandlerMethod
 */
public class ParancoeHandlerMethodInvoker {

    /**
     * We'll create a lot of these objects, so we don't want a new logger every time.
     */
    private static final Log logger = LogFactory.getLog(
            ParancoeHandlerMethodInvoker.class);
    private final HandlerMethodResolver methodResolver;
    private final WebBindingInitializer bindingInitializer;
    private final SessionAttributeStore sessionAttributeStore;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final WebArgumentResolver[] customArgumentResolvers;
    private final SimpleSessionStatus sessionStatus = new SimpleSessionStatus();
    private final Validator validator;

    public ParancoeHandlerMethodInvoker(HandlerMethodResolver methodResolver) {
        this(methodResolver, null);
    }

    public ParancoeHandlerMethodInvoker(HandlerMethodResolver methodResolver,
            WebBindingInitializer bindingInitializer) {
        this(methodResolver, bindingInitializer,
                new DefaultSessionAttributeStore(), null, null);
    }

    public ParancoeHandlerMethodInvoker(
            HandlerMethodResolver methodResolver,
            WebBindingInitializer bindingInitializer,
            SessionAttributeStore sessionAttributeStore,
            ParameterNameDiscoverer parameterNameDiscoverer,
            Validator validator,
            WebArgumentResolver... customArgumentResolvers) {

        this.methodResolver = methodResolver;
        this.bindingInitializer = bindingInitializer;
        this.sessionAttributeStore = sessionAttributeStore;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        this.validator = validator;
        this.customArgumentResolvers = customArgumentResolvers;
    }

    public final Object invokeHandlerMethod(
            Method handlerMethod, Object handler, NativeWebRequest webRequest,
            ExtendedModelMap implicitModel)
            throws Exception {

        boolean debug = logger.isDebugEnabled();
        for (Method attributeMethod : this.methodResolver.getModelAttributeMethods()) {
            Object[] args = resolveHandlerArguments(attributeMethod, handler,
                    webRequest, implicitModel);
            if (debug) {
                logger.debug("Invoking model attribute method: " +
                        attributeMethod);
            }
            Object attrValue = doInvokeMethod(attributeMethod, handler, args);
            String attrName = AnnotationUtils.findAnnotation(attributeMethod,
                    ModelAttribute.class).value();
            if ("".equals(attrName)) {
                Class resolvedType = GenericTypeResolver.resolveReturnType(
                        attributeMethod, handler.getClass());
                attrName = Conventions.getVariableNameForReturnType(
                        attributeMethod, resolvedType, attrValue);
            }
            implicitModel.addAttribute(attrName, attrValue);
        }

        Object[] args = resolveHandlerArguments(handlerMethod, handler,
                webRequest, implicitModel);
        String afterValidationView = doValidation(handlerMethod, args);
        if (afterValidationView != null) {
            return afterValidationView;
        }
        if (debug) {
            logger.debug("Invoking request handler method: " +
                    handlerMethod);
        }
        return doInvokeMethod(handlerMethod, handler, args);
    }

    private String doValidation(Method handlerMethod, Object[] args) {
        boolean debug = logger.isDebugEnabled();
        if (validator != null) {
            Validation validationAnnotation =
                    AnnotationUtils.findAnnotation(handlerMethod,
                    Validation.class);
            if (validationAnnotation != null) {
                if (debug) {
                    logger.debug("Doing validation before invoking logger handler method: " +
                            handlerMethod);
                }
                Errors result = null;
                for (Object arg : args) {
                    if (arg != null &&
                            (arg instanceof Errors)) {
                        result = (Errors) arg;
                    }
                }
                for (int i = 0; i < args.length; i++) {
                    MethodParameter methodParam =
                            new MethodParameter(handlerMethod, i);
                    methodParam.initParameterNameDiscovery(
                            this.parameterNameDiscoverer);
                    Object[] paramAnns = methodParam.getParameterAnnotations();
                    List<String> validatingAttrs =
                            Arrays.asList(validationAnnotation.attributes());
                    for (int j = 0; j < paramAnns.length; j++) {
                        Object paramAnn = paramAnns[j];
                        if (ModelAttribute.class.isInstance(paramAnn)) {
                            ModelAttribute attr =
                                    (ModelAttribute) paramAnn;
                            if (!validatingAttrs.isEmpty() &&
                                    !validatingAttrs.contains(attr.value())) {
                                continue;
                            }
                            if (debug) {
                                logger.debug("Validating attribute: " +
                                        attr.value());
                            }
                            validator.validate(args[i], result);
                        }
                    }
                }
                if (result.hasErrors() &&
                        !validationAnnotation.continueOnErrors()) {
                    if (debug) {
                        logger.debug("Returning validation view: " +
                                validationAnnotation.view());
                    }
                    return validationAnnotation.view();
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object[] resolveHandlerArguments(
            Method handlerMethod, Object handler, NativeWebRequest webRequest,
            ExtendedModelMap implicitModel)
            throws Exception {

        Class[] paramTypes = handlerMethod.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i <
                args.length; i++) {
            MethodParameter methodParam = new MethodParameter(handlerMethod, i);
            methodParam.initParameterNameDiscovery(this.parameterNameDiscoverer);
            GenericTypeResolver.resolveParameterType(methodParam,
                    handler.getClass());
            String paramName = null;
            boolean paramRequired = false;
            String attrName = null;
            Object[] paramAnns = methodParam.getParameterAnnotations();

            for (int j = 0; j <
                    paramAnns.length; j++) {
                Object paramAnn = paramAnns[j];
                if (RequestParam.class.isInstance(paramAnn)) {
                    RequestParam requestParam = (RequestParam) paramAnn;
                    paramName = requestParam.value();
                    paramRequired = requestParam.required();



                    break;
                } else if (ModelAttribute.class.isInstance(paramAnn)) {
                    ModelAttribute attr = (ModelAttribute) paramAnn;
                    attrName = attr.value();
                }
            }

            if (paramName != null && attrName != null) {
                throw new IllegalStateException("@RequestParam and @ModelAttribute are an exclusive choice -" +
                        "do not specify both on the same parameter: " +
                        handlerMethod);
            }

            Class paramType = methodParam.getParameterType();

            if (paramName == null && attrName == null) {
                Object argValue = resolveCommonArgument(methodParam, webRequest);
                if (argValue != WebArgumentResolver.UNRESOLVED) {
                    args[i] = argValue;
                } else {
                    if (Model.class.isAssignableFrom(paramType) ||
                            Map.class.isAssignableFrom(paramType)) {
                        args[i] = implicitModel;
                    } else if (SessionStatus.class.isAssignableFrom(paramType)) {
                        args[i] = this.sessionStatus;
                    } else if (Errors.class.isAssignableFrom(paramType)) {


                        throw new IllegalStateException(
                                "Errors/BindingResult argument declared " +
                                "without preceding model attribute. Check your handler method signature!");
                    } else if (BeanUtils.isSimpleProperty(paramType)) {
                        paramName = "";
                    } else {
                        attrName = "";
                    }
                }

            }



            if (paramName != null) {
                args[i] = resolveRequestParam(paramName, paramRequired,
                        methodParam, webRequest, handler);
            } else if (attrName != null) {
                WebDataBinder binder = resolveModelAttribute(attrName,
                        methodParam, implicitModel, webRequest, handler);
                if (args.length > i + 1 && Errors.class.isAssignableFrom(
                        paramTypes[i + 1])) {
                    doBind(webRequest, binder,
                            false);
                    args[i] = binder.getTarget();
                    args[i + 1] = binder.getBindingResult();
                    i++;
                } else {
                    doBind(webRequest, binder, true);
                    args[i] = binder.getTarget();
                }

                implicitModel.putAll(binder.getBindingResult().getModel());
            }

        }

        return args;
    }

    private void initBinder(Object handler, String attrName,
            WebDataBinder binder, NativeWebRequest webRequest)
            throws Exception {

        if (this.bindingInitializer != null) {
            this.bindingInitializer.initBinder(binder, webRequest);
        }

        if (handler != null) {
            Set<Method> initBinderMethods =
                    this.methodResolver.getInitBinderMethods();
            if (!initBinderMethods.isEmpty()) {
                boolean debug = logger.isDebugEnabled();
                for (Method initBinderMethod : initBinderMethods) {
                    String[] targetNames = AnnotationUtils.findAnnotation(
                            initBinderMethod, InitBinder.class).value();





                    if (targetNames.length == 0 ||
                            Arrays.asList(targetNames).contains(attrName)) {
                        Object[] initBinderArgs = resolveInitBinderArguments(
                                handler, initBinderMethod, binder, webRequest);
                        if (debug) {
                            logger.debug("Invoking init-binder method: " +
                                    initBinderMethod);
                        }
                        Object returnValue = doInvokeMethod(initBinderMethod,
                                handler, initBinderArgs);
                        if (returnValue != null) {
                            throw new IllegalStateException(
                                    "InitBinder methods must not have a return value: " +
                                    initBinderMethod);
                        }
                    }
                }

            }


        }
    }

    private Object[] resolveInitBinderArguments(Object handler,
            Method initBinderMethod,
            WebDataBinder binder, NativeWebRequest webRequest) throws Exception {

        Class[] initBinderParams = initBinderMethod.getParameterTypes();
        Object[] initBinderArgs = new Object[initBinderParams.length];

        for (int i = 0; i <
                initBinderArgs.length; i++) {
            MethodParameter methodParam = new MethodParameter(initBinderMethod,
                    i);
            methodParam.initParameterNameDiscovery(this.parameterNameDiscoverer);
            GenericTypeResolver.resolveParameterType(methodParam,
                    handler.getClass());
            String paramName = null;
            boolean paramRequired = false;
            Object[] paramAnns = methodParam.getParameterAnnotations();

            for (int j = 0; j <
                    paramAnns.length; j++) {
                Object paramAnn = paramAnns[j];
                if (RequestParam.class.isInstance(paramAnn)) {
                    RequestParam requestParam = (RequestParam) paramAnn;
                    paramName = requestParam.value();
                    paramRequired = requestParam.required();



                    break;
                } else if (ModelAttribute.class.isInstance(paramAnn)) {


                    throw new IllegalStateException(
                            "@ModelAttribute is not supported on @InitBinder methods: " +
                            initBinderMethod);
                }
            }

            if (paramName == null) {
                Object argValue = resolveCommonArgument(methodParam, webRequest);
                if (argValue != WebArgumentResolver.UNRESOLVED) {
                    initBinderArgs[i] = argValue;
                } else {
                    Class paramType = initBinderParams[i];
                    if (paramType.isInstance(binder)) {
                        initBinderArgs[i] = binder;
                    } else if (BeanUtils.isSimpleProperty(paramType)) {
                        paramName = "";
                    } else {
                        throw new IllegalStateException("Unsupported argument [" +
                                paramType.getName() +
                                "] for @InitBinder method: " + initBinderMethod);
                    }

                }
            }

            if (paramName != null) {
                initBinderArgs[i] =
                        resolveRequestParam(paramName, paramRequired,
                        methodParam, webRequest, null);
            }

        }

        return initBinderArgs;
    }

    private Object resolveRequestParam(String paramName, boolean paramRequired,
            MethodParameter methodParam, NativeWebRequest webRequest,
            Object handlerForInitBinderCall)
            throws Exception {

        Class paramType = methodParam.getParameterType();
        if ("".equals(paramName)) {
            paramName = methodParam.getParameterName();
            if (paramName == null) {
                throw new IllegalStateException(
                        "No parameter specified for @RequestParam argument of type [" +
                        paramType.getName() +
                        "], and no parameter name information found in class file either.");
            }

        }
        Object paramValue = null;
        if (webRequest.getNativeRequest() instanceof MultipartRequest) {
            paramValue =
                    ((MultipartRequest) webRequest.getNativeRequest()).getFile(
                    paramName);
        }

        if (paramValue == null) {
            String[] paramValues = webRequest.getParameterValues(paramName);
            if (paramValues != null) {
                paramValue =
                        (paramValues.length == 1 ? paramValues[0] : paramValues);
            }

        }
        if (paramValue == null) {
            if (paramRequired) {
                raiseMissingParameterException(paramName, paramType);
            }

            if (paramType.isPrimitive()) {
                throw new IllegalStateException(
                        "Optional " + paramType + " parameter '" + paramName +
                        "' is not present but cannot be translated into a null value due to being declared as a " +
                        "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.");
            }

        }
        WebDataBinder binder = createBinder(webRequest, null, paramName);
        initBinder(handlerForInitBinderCall, paramName, binder, webRequest);
        return binder.convertIfNecessary(paramValue, paramType, methodParam);
    }

    private WebDataBinder resolveModelAttribute(String attrName,
            MethodParameter methodParam,
            ExtendedModelMap implicitModel, NativeWebRequest webRequest,
            Object handler) throws Exception {

        // Bind request parameter onto object...
        if ("".equals(attrName)) {
            attrName = Conventions.getVariableNameForParameter(methodParam);
        }

        Class paramType = methodParam.getParameterType();
        if (!implicitModel.containsKey(attrName) &&
                this.methodResolver.isSessionAttribute(attrName, paramType)) {
            Object sessionAttr = this.sessionAttributeStore.retrieveAttribute(
                    webRequest, attrName);
            if (sessionAttr == null) {
                raiseSessionRequiredException("Session attribute '" + attrName +
                        "' required - not found in session");
            }

            implicitModel.addAttribute(attrName, sessionAttr);
        }

        Object bindObject = implicitModel.get(attrName);
        if (bindObject == null) {
            bindObject = BeanUtils.instantiateClass(paramType);
        }

        WebDataBinder binder = createBinder(webRequest, bindObject, attrName);
        initBinder(handler, attrName, binder, webRequest);
        return binder;
    }

    @SuppressWarnings("unchecked")
    public final void updateModelAttributes(
            Object handler, Map mavModel, ExtendedModelMap implicitModel,
            NativeWebRequest webRequest)
            throws Exception {

        if (this.methodResolver.hasSessionAttributes() &&
                this.sessionStatus.isComplete()) {
            for (String attrName : this.methodResolver.getActualSessionAttributeNames()) {
                this.sessionAttributeStore.cleanupAttribute(webRequest, attrName);
            }

        }

        // Expose model attributes as session attributes, if required.
        // Expose BindingResults for all attributes, making custom editors available.
        Map<String, Object> model =
                (mavModel != null ? mavModel : implicitModel);
        for (Map.Entry<String, Object> entry : new HashSet<Map.Entry>(
                model.entrySet())) {
            String attrName = entry.getKey();
            Object attrValue = entry.getValue();
            boolean isSessionAttr =
                    this.methodResolver.isSessionAttribute(attrName, (attrValue != null
                    ? attrValue.getClass() : null));
            if (isSessionAttr && !this.sessionStatus.isComplete()) {
                this.sessionAttributeStore.storeAttribute(webRequest, attrName,
                        attrValue);
            }

            if (!attrName.startsWith(BindingResult.MODEL_KEY_PREFIX) &&
                    (isSessionAttr || isBindingCandidate(attrValue))) {
                String bindingResultKey = BindingResult.MODEL_KEY_PREFIX +
                        attrName;
                if (mavModel != null && !model.containsKey(bindingResultKey)) {
                    WebDataBinder binder = createBinder(webRequest, attrValue,
                            attrName);
                    initBinder(handler, attrName, binder, webRequest);
                    mavModel.put(bindingResultKey, binder.getBindingResult());
                }

            }
        }
    }

    /**
     * Determine whether the given value qualifies as a "binding candidate",
     * i.e. might potentially be subject to bean-style data binding later on.
     */
    protected boolean isBindingCandidate(Object value) {
        return (value != null && !value.getClass().isArray() &&
                !(value instanceof Collection) &&
                !(value instanceof Map) && !ClassUtils.isPrimitiveOrWrapper(
                value.getClass()) &&
                !(value instanceof CharSequence) && !(value instanceof Number) &&
                !(value instanceof Date));
    }

    private Object doInvokeMethod(Method method, Object target, Object[] args)
            throws Exception {
        ReflectionUtils.makeAccessible(method);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            ReflectionUtils.rethrowException(ex.getTargetException());
        }

        throw new IllegalStateException("Should never get here");
    }

    protected void raiseMissingParameterException(String paramName,
            Class paramType) throws Exception {
        throw new IllegalStateException("Missing parameter '" + paramName +
                "' of type [" + paramType.getName() + "]");
    }

    protected void raiseSessionRequiredException(String message) throws
            Exception {
        throw new IllegalStateException(message);
    }

    protected WebDataBinder createBinder(NativeWebRequest webRequest,
            Object target, String objectName)
            throws Exception {

        return new WebRequestDataBinder(target, objectName);
    }

    protected void doBind(NativeWebRequest webRequest, WebDataBinder binder,
            boolean failOnErrors)
            throws Exception {

        WebRequestDataBinder requestBinder = (WebRequestDataBinder) binder;
        requestBinder.bind(webRequest);
        if (failOnErrors) {
            requestBinder.closeNoCatch();
        }

    }

    protected Object resolveCommonArgument(MethodParameter methodParameter,
            NativeWebRequest webRequest)
            throws Exception {

        if (this.customArgumentResolvers != null) {
            for (WebArgumentResolver argumentResolver : this.customArgumentResolvers) {
                Object value = argumentResolver.resolveArgument(methodParameter,
                        webRequest);
                if (value != WebArgumentResolver.UNRESOLVED) {
                    return value;
                }

            }
        }
        return resolveStandardArgument(methodParameter.getParameterType(),
                webRequest);
    }

    protected Object resolveStandardArgument(Class parameterType,
            NativeWebRequest webRequest)
            throws Exception {

        if (WebRequest.class.isAssignableFrom(parameterType)) {




            return webRequest;
        }
        return WebArgumentResolver.UNRESOLVED;
    }
}
