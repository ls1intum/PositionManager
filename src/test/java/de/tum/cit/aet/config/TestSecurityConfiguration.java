package de.tum.cit.aet.config;

import de.tum.cit.aet.core.security.CurrentUserProvider;
import de.tum.cit.aet.usermanagement.domain.User;
import de.tum.cit.aet.usermanagement.domain.UserGroup;
import de.tum.cit.aet.usermanagement.domain.key.UserGroupId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@TestConfiguration
public class TestSecurityConfiguration {

    private static final ThreadLocal<User> currentTestUser = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        currentTestUser.set(user);
    }

    public static void clearCurrentUser() {
        currentTestUser.remove();
    }

    public static User createTestUser(String universityId, String... roles) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUniversityId(universityId);
        user.setEmail(universityId + "@test.tum.de");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setJoinedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        Set<UserGroup> groups = new HashSet<>();
        for (String role : roles) {
            UserGroup group = new UserGroup();
            UserGroupId groupId = new UserGroupId();
            groupId.setUserId(user.getId());
            groupId.setRole(role);
            group.setId(groupId);
            group.setUser(user);
            groups.add(group);
        }
        user.setGroups(groups);

        return user;
    }

    @Bean
    @Primary
    public CurrentUserProvider testCurrentUserProvider() {
        return new CurrentUserProvider(null) {
            @Override
            public User getUser() {
                User user = currentTestUser.get();
                if (user == null) {
                    throw new IllegalStateException("No test user set. Call TestSecurityConfiguration.setCurrentUser() before the test.");
                }
                return user;
            }

            @Override
            public boolean isAnonymous() {
                return getUser().hasNoGroup();
            }

            @Override
            public boolean isEmployee() {
                return getUser().hasAnyGroup("employee");
            }

            @Override
            public boolean isProfessor() {
                return getUser().hasAnyGroup("professor");
            }

            @Override
            public boolean isJobManager() {
                return getUser().hasAnyGroup("job_manager");
            }

            @Override
            public boolean isAdmin() {
                return getUser().hasAnyGroup("admin");
            }

            @Override
            public boolean hasAnyRole() {
                return isAdmin() || isJobManager() || isProfessor() || isEmployee();
            }
        };
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
