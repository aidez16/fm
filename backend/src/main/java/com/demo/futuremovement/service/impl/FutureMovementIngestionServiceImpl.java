package com.demo.futuremovement.service.impl;

import com.demo.futuremovement.mapper.ProcessedFutureMovementMapper;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import com.demo.futuremovement.parser.FixedWidthFileReader;
import com.demo.futuremovement.service.FutureMovementIngestionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FutureMovementIngestionServiceImpl implements FutureMovementIngestionService {

    private final FixedWidthFileReader fileReader;
    private final ProcessedFutureMovementMapper mapper;
    private final ResourceLoader resourceLoader;
    private final String inputFilePath;

    public FutureMovementIngestionServiceImpl(FixedWidthFileReader fileReader,
                                              ProcessedFutureMovementMapper mapper,
                                              ResourceLoader resourceLoader,
                                              @Value("${futuremovement.input-file-path}") String inputFilePath) {
        this.fileReader = fileReader;
        this.mapper = mapper;
        this.resourceLoader = resourceLoader;
        this.inputFilePath = inputFilePath;
    }

    @Override
    public List<ProcessedFutureMovement> loadTodaysMovements() {
        return loadMovements(resourceLoader.getResource(inputFilePath));
    }

    @Override
    public List<ProcessedFutureMovement> loadMovements(Resource resource) {
        List<Map<String, String>> rawRecords = fileReader.readAll(resource);
        return rawRecords.stream().map(mapper::map).toList();
    }
}
