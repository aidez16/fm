package com.demo.futuremovement.kafka;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaSampleDataSeederTest {

    private static final String INPUT_PATH = "classpath:data/Input.txt";

    private final FutureMovementProducerService producerService = mock(FutureMovementProducerService.class);
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    void publishesTheConfiguredInputFileOnceTheApplicationIsReady() {
        when(producerService.publishFile(any())).thenReturn(717);

        new KafkaSampleDataSeeder(producerService, resourceLoader, INPUT_PATH, true).seedTopicOnStartup();

        ArgumentCaptor<Resource> published = ArgumentCaptor.forClass(Resource.class);
        verify(producerService).publishFile(published.capture());
        assertThat(published.getValue().exists()).isTrue();
    }

    @Test
    void publishesNothingWhenSeedingIsDisabled() {
        new KafkaSampleDataSeeder(producerService, resourceLoader, INPUT_PATH, false).seedTopicOnStartup();

        verify(producerService, never()).publishFile(any());
    }

    /** A broker that isn't reachable must not abort application startup. */
    @Test
    void publishFailureIsSwallowed() {
        when(producerService.publishFile(any())).thenThrow(new IllegalStateException("broker unavailable"));

        KafkaSampleDataSeeder seeder = new KafkaSampleDataSeeder(producerService, resourceLoader, INPUT_PATH, true);

        assertThatCode(seeder::seedTopicOnStartup).doesNotThrowAnyException();
    }
}
