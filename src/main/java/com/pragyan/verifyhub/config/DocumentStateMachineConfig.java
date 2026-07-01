package com.pragyan.verifyhub.config;

import com.pragyan.verifyhub.document.DocumentEvent;
import com.pragyan.verifyhub.document.DocumentStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class DocumentStateMachineConfig extends StateMachineConfigurerAdapter<DocumentStatus, DocumentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<DocumentStatus, DocumentEvent> states) throws Exception {
        states
                .withStates()
                .initial(DocumentStatus.PENDING)
                .states(EnumSet.allOf(DocumentStatus.class))
                .end(DocumentStatus.VERIFIED)
                .end(DocumentStatus.REJECTED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<DocumentStatus, DocumentEvent> transitions) throws Exception {
        transitions

                .withExternal()
                .source(DocumentStatus.PENDING).target(DocumentStatus.UNDER_REVIEW)
                .event(DocumentEvent.START_REVIEW)
                .and()
                .withExternal()
                .source(DocumentStatus.PENDING).target(DocumentStatus.REJECTED)
                .event(DocumentEvent.REJECT)

                .and()
                .withExternal()
                .source(DocumentStatus.UNDER_REVIEW).target(DocumentStatus.VERIFIED)
                .event(DocumentEvent.APPROVE)
                .and()
                .withExternal()
                .source(DocumentStatus.UNDER_REVIEW).target(DocumentStatus.REJECTED)
                .event(DocumentEvent.REJECT);
    }
}