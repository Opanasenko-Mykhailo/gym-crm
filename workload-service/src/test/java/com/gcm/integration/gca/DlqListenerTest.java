package com.gcm.integration.gca;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DlqListenerTest {

    @Mock
    private Message mockMessage;

    @InjectMocks
    private DlqListener sut;

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(DlqListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldLogErrorMessageWhenDlqMessageReceived() {
        sut.onDlqMessage(mockMessage);

        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getLevel()).hasToString("ERROR");
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .contains("Received message in DLQ:");
    }

    @Test
    void shouldHandleNullMessageGracefully() {
        sut.onDlqMessage(null);

        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getLevel()).hasToString("ERROR");
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .contains("Received message in DLQ: null");
    }
}