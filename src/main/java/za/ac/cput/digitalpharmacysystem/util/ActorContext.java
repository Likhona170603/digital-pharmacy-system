package za.ac.cput.digitalpharmacysystem.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Tells the audit log who is performing an action.
 *
 * TEMPORARY: until login/JWT exists this reads the optional X-User-Id and X-User-Role request
 * headers, which anyone can fake. When JWT security is added, only this class changes: it will
 * read the authenticated user from the security context instead. Outside a web request
 * (for example in service tests) both values are null.
 */
@Component
public class ActorContext {

    public Long getActorId() {
        String value = header("X-User-Id");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getActorRole() {
        String value = header("X-User-Role");
        return (value == null || value.isBlank()) ? null : value.trim().toUpperCase();
    }

    private String header(String name) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(name);
        }
        return null;
    }
}
