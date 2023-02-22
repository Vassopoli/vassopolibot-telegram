package com.alivassopoli.service;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.function.Predicate.not;

@ApplicationScoped
public class VassopoliServiceFactory {
    private static final Logger LOG = Logger.getLogger(VassopoliServiceFactory.class);

    private final Instance<VassopoliService> vassopoliServiceList;
    private final MessageSender fallbackService;

    public VassopoliServiceFactory(final Instance<VassopoliService> vassopoliServiceList, final MessageSender messageSender) {
        validateCommandUniqueness(vassopoliServiceList);
        this.vassopoliServiceList = vassopoliServiceList;
        this.fallbackService = messageSender;
    }

    public VassopoliService execute(final String textMessage) {
        final VassopoliService vassopoliService = vassopoliServiceList
                .stream()
                .filter(s -> doesMessageStartsWithValuesFromCommandList(textMessage, s))
                .findFirst().orElseGet(() -> {
                    LOG.infof("User intention could not be identified for message %s. Setting default to %s", textMessage, fallbackService.getClass().getSimpleName());
                    return fallbackService;
                });

            LOG.infof("User intention is %s", vassopoliService.getClass().getSimpleName());
            return vassopoliService;
    }

    private boolean doesMessageStartsWithValuesFromCommandList(final String textMessage, final VassopoliService i) {
        return i.getCommand()
                .stream()
                .anyMatch(c-> textMessage.toLowerCase().startsWith(c.toLowerCase()));
    }

    private void validateCommandUniqueness(final Instance<VassopoliService> vassopoliServiceList) {
        final Set<String> uniqueVassopoliServiceCommands = new HashSet<>();

        vassopoliServiceList.stream()
                .map(VassopoliService::getCommand)
                .flatMap(Collection::stream)
                .filter(not(uniqueVassopoliServiceCommands::add))
                .findFirst()
                .ifPresent(this::throwCommandRepeatedException);
    }

    private void throwCommandRepeatedException(final String command) {
        throw new IllegalStateException("VassopoliService command can not be repeated. Command " + command + " repeated");
    }
}
