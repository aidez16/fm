package com.demo.futuremovement.controller;

import com.demo.futuremovement.kafka.FutureMovementProducerService;
import com.demo.futuremovement.service.DailyAggregateStoreService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReplayController.class)
@TestPropertySource(properties = "futuremovement.input-file-path=classpath:data/Input.txt")
class ReplayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FutureMovementProducerService producerService;

    @MockBean
    private DailyAggregateStoreService aggregateStore;

    @Test
    void replayEndpointPublishesTheConfiguredFileAndReportsCount() throws Exception {
        when(producerService.publishFile(any())).thenReturn(717);

        mockMvc.perform(post("/api/v1/future-movements/replay-sample-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedRecords").value(717));

        verify(producerService).publishFile(any());
    }

    @Test
    void replayEndpointReturnsZeroForAnEmptyFile() throws Exception {
        when(producerService.publishFile(any())).thenReturn(0);

        mockMvc.perform(post("/api/v1/future-movements/replay-sample-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedRecords").value(0));
    }

    /** Replay rebuilds from the file, so the aggregate must be cleared first. */
    @Test
    void replayEndpointClearsTheRunningAggregateBeforeRepublishing() throws Exception {
        when(producerService.publishFile(any())).thenReturn(717);

        mockMvc.perform(post("/api/v1/future-movements/replay-sample-data"))
                .andExpect(status().isOk());

        InOrder inOrder = inOrder(aggregateStore, producerService);
        inOrder.verify(aggregateStore).reset();
        inOrder.verify(producerService).publishFile(any());
    }
}
