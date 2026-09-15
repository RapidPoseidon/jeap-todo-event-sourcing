package ch.example.todo.task.application;

import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The authenticated principal, used both as the actor recorded on every event and as the owner of new tasks.
 */
@Component
public class CurrentUser {

    public String name() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authenticated user in the security context");
        }
        if (authentication instanceof JeapAuthenticationToken jeapToken) {
            String preferredUsername = jeapToken.getPreferredUsername();
            return preferredUsername != null ? preferredUsername : jeapToken.getTokenSubject();
        }
        return authentication.getName();
    }
}
