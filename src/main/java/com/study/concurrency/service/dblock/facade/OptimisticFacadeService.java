package com.study.concurrency.service.dblock.facade;

import com.study.concurrency.service.dblock.PostingOptimisticLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptimisticFacadeService {

    private final PostingOptimisticLockService service;

    public void view(Long id) throws InterruptedException {
        while (true) {
            try {
                service.view(1L);
                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
