package org.fjorum.controllers.filters;


import ninja.*;
import ninja.jpa.UnitOfWork;
import ninja.session.FlashScope;
import ninja.session.Session;
import org.fjorum.models.User;
import org.fjorum.services.UserService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractUserFilter implements Filter {

    @Inject
    private UserService userService;

    private final Predicate<User> condition;
    private final String errorKey;

    protected AbstractUserFilter(Predicate<User> condition, String errorKey) {
        this.condition = condition;
        this.errorKey = errorKey;
    }

    @Override
    @UnitOfWork
    public Result filter(FilterChain filterChain, Context context) {

        Optional<Session> session = Optional.ofNullable(context.getSession());
        Optional<FlashScope> flashScope = Optional.ofNullable(context.getFlashScope());

        return session.
                flatMap(userService::findUserBySession).
                filter(condition).
                map(user -> filterChain.next(context)
                ).orElseGet(() -> {
            flashScope.ifPresent(f -> f.error(errorKey));
            return Results.redirect("/errors");
        });
    }
}
