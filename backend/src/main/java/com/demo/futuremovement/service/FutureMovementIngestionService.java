package com.demo.futuremovement.service;

import com.demo.futuremovement.model.ProcessedFutureMovement;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FutureMovementIngestionService {

    List<ProcessedFutureMovement> loadTodaysMovements();

    List<ProcessedFutureMovement> loadMovements(Resource resource);
}
