package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.InvalidRequestException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.exception.RetriableException;
import dev.langchain4j.exception.TimeoutException;
import jakarta.ws.rs.ProcessingException;

import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LlmProviderExceptionClassifier {

    private static final Pattern JSON_TYPE = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern JSON_CODE = Pattern.compile("\"code\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern REQUEST_ID = Pattern.compile("(?i)request[-_ ]?id[:= ]+([a-z0-9_\\-]+)");

    private LlmProviderExceptionClassifier() {
    }

    public static LlmProviderFailure classify(Throwable throwable) {
        if (throwable == null) {
            return LlmProviderFailure.unexpected();
        }
        Throwable root = rootCause(throwable);
        Integer status = firstHttpStatus(throwable);
        String message = safeMessage(throwable, root);
        String lower = message.toLowerCase(Locale.ROOT);
        LlmProviderFailureKind kind = kind(throwable, root, status, lower);
        boolean retryable = retryable(throwable, root, status, kind);
        return new LlmProviderFailure(
                kind,
                status,
                extract(JSON_TYPE, message),
                extract(JSON_CODE, message),
                extract(REQUEST_ID, message),
                retryable);
    }

    public static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current == null ? throwable : current;
    }

    public static String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        String sanitized = message
                .replaceAll("(?i)(api[-_ ]?key|authorization|bearer)\\s*[:=]\\s*[^,\\s}]+", "$1=<redacted>")
                .replaceAll("sk-[A-Za-z0-9_-]{8,}", "sk-<redacted>")
                .replaceAll("\\s+", " ")
                .trim();
        return sanitized.length() > 500 ? sanitized.substring(0, 500) : sanitized;
    }

    private static LlmProviderFailureKind kind(Throwable throwable, Throwable root, Integer status, String lowerMessage) {
        if (contains(throwable, AuthenticationException.class)) {
            return status != null && status == 403
                    ? LlmProviderFailureKind.AUTHORIZATION
                    : LlmProviderFailureKind.AUTHENTICATION;
        }
        if (contains(throwable, RateLimitException.class)) {
            return LlmProviderFailureKind.RATE_LIMIT;
        }
        if (contains(throwable, TimeoutException.class) || contains(root, SocketTimeoutException.class)
                || lowerMessage.contains("timeout") || lowerMessage.contains("timed out")) {
            return LlmProviderFailureKind.TIMEOUT;
        }
        if (contains(throwable, ModelNotFoundException.class)
                || lowerMessage.contains("model_not_found")
                || lowerMessage.contains("model not found")
                || lowerMessage.contains("does not exist")) {
            return LlmProviderFailureKind.MODEL_NOT_FOUND;
        }
        if (contains(throwable, InvalidRequestException.class)) {
            return LlmProviderFailureKind.INVALID_REQUEST;
        }
        if (status != null) {
            return switch (status) {
                case 400 -> LlmProviderFailureKind.INVALID_REQUEST;
                case 401 -> LlmProviderFailureKind.AUTHENTICATION;
                case 403 -> LlmProviderFailureKind.AUTHORIZATION;
                case 404 -> LlmProviderFailureKind.MODEL_NOT_FOUND;
                case 429 -> LlmProviderFailureKind.RATE_LIMIT;
                case 408, 504 -> LlmProviderFailureKind.TIMEOUT;
                case 500, 502, 503 -> LlmProviderFailureKind.PROVIDER_SERVICE_UNAVAILABLE;
                default -> status >= 500
                        ? LlmProviderFailureKind.PROVIDER_SERVICE_UNAVAILABLE
                        : LlmProviderFailureKind.UNEXPECTED_INTERNAL;
            };
        }
        if (contains(throwable, ProcessingException.class)
                || contains(root, ConnectException.class)
                || contains(root, UnknownHostException.class)) {
            return LlmProviderFailureKind.CONNECTION;
        }
        if (lowerMessage.contains("deserialize") || lowerMessage.contains("serialization")
                || lowerMessage.contains("parse")) {
            return LlmProviderFailureKind.RESPONSE_PROCESSING;
        }
        return LlmProviderFailureKind.UNEXPECTED_INTERNAL;
    }

    private static boolean retryable(
            Throwable throwable,
            Throwable root,
            Integer status,
            LlmProviderFailureKind kind) {
        if (contains(throwable, RetriableException.class)) {
            return true;
        }
        if (status != null) {
            return status == 408 || status == 409 || status == 429 || status == 500
                    || status == 502 || status == 503 || status == 504;
        }
        return kind == LlmProviderFailureKind.TIMEOUT
                || kind == LlmProviderFailureKind.CONNECTION
                || kind == LlmProviderFailureKind.PROVIDER_SERVICE_UNAVAILABLE
                || contains(root, SocketTimeoutException.class);
    }

    private static Integer firstHttpStatus(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            Integer status = httpStatus(current);
            if (status != null) {
                return status;
            }
            current = current.getCause();
        }
        return null;
    }

    private static Integer httpStatus(Throwable throwable) {
        if (throwable instanceof HttpException httpException) {
            return httpException.statusCode();
        }
        for (String methodName : java.util.List.of("statusCode", "getStatusCode", "status", "getStatus")) {
            Object value = invokeNoArg(throwable, methodName);
            Integer parsed = integerValue(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Integer integerValue(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean contains(Throwable throwable, Class<?> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static String safeMessage(Throwable throwable, Throwable root) {
        StringBuilder builder = new StringBuilder();
        if (throwable.getMessage() != null) {
            builder.append(throwable.getMessage());
        }
        if (root != null && root != throwable && root.getMessage() != null) {
            builder.append(' ').append(root.getMessage());
        }
        return builder.toString();
    }

    private static String extract(Pattern pattern, String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? sanitizeMessage(matcher.group(1)) : null;
    }
}
