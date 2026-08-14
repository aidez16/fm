package com.demo.futuremovement.controller;

import com.demo.futuremovement.csv.CsvExporter;
import com.demo.futuremovement.dto.DailySummaryRecord;
import com.demo.futuremovement.service.SummaryProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DailySummaryController.class)
class DailySummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SummaryProviderService summaryProvider;

    @MockBean
    private CsvExporter csvExporter;

    private final DailySummaryRecord sampleRow =
            new DailySummaryRecord("CL-4321-0002-0001", "SGX-FU-NK-20100910", new BigDecimal("46"));

    @Test
    void summaryEndpointReturnsJson() throws Exception {
        when(summaryProvider.getSummary()).thenReturn(List.of(sampleRow));

        mockMvc.perform(get("/api/v1/future-movements/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientInformation").value("CL-4321-0002-0001"))
                .andExpect(jsonPath("$[0].productInformation").value("SGX-FU-NK-20100910"))
                .andExpect(jsonPath("$[0].totalTransactionAmount").value(46));
    }

    @Test
    void summaryEndpointReturnsEmptyArrayWhenNoData() throws Exception {
        when(summaryProvider.getSummary()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/future-movements/summary"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void summaryEndpointReturnsMultipleRowsInProviderOrder() throws Exception {
        DailySummaryRecord second = new DailySummaryRecord("CL-1234-0002-0001", "SGX-FU-NK-20100910", new BigDecimal("-52"));
        when(summaryProvider.getSummary()).thenReturn(List.of(sampleRow, second));

        mockMvc.perform(get("/api/v1/future-movements/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].totalTransactionAmount").value(-52));
    }

    @Test
    void csvEndpointReturnsDownloadableFile() throws Exception {
        when(summaryProvider.getSummary()).thenReturn(List.of(sampleRow));
        when(csvExporter.toCsv(any())).thenReturn(
                "Client_Information,Product_Information,Total_Transaction_Amount\r\nCL-4321-0002-0001,SGX-FU-NK-20100910,46\r\n");

        mockMvc.perform(get("/api/v1/future-movements/summary/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Output.csv\""))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CL-4321-0002-0001,SGX-FU-NK-20100910,46")));
    }

    @Test
    void csvEndpointCallsProviderExactlyOnce() throws Exception {
        when(summaryProvider.getSummary()).thenReturn(List.of(sampleRow));
        when(csvExporter.toCsv(any())).thenReturn("header\r\n");

        mockMvc.perform(get("/api/v1/future-movements/summary/csv"))
                .andExpect(status().isOk());

        verify(summaryProvider).getSummary();
        verifyNoMoreInteractions(summaryProvider);
    }

    @Test
    void jsonAndCsvEndpointsUseTheSameUnderlyingSummary() throws Exception {
        when(summaryProvider.getSummary()).thenReturn(List.of(sampleRow));
        when(csvExporter.toCsv(List.of(sampleRow))).thenReturn("Client_Information,Product_Information,Total_Transaction_Amount\r\nCL-4321-0002-0001,SGX-FU-NK-20100910,46\r\n");

        mockMvc.perform(get("/api/v1/future-movements/summary"))
                .andExpect(jsonPath("$[0].totalTransactionAmount").value(46));

        mockMvc.perform(get("/api/v1/future-movements/summary/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("46")));
    }
}
