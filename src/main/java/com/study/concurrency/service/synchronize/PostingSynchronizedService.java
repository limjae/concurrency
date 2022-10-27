package com.study.concurrency.service.synchronize;

import com.study.concurrency.domain.Posting;
import com.study.concurrency.service.PostingService;
import jdk.jfr.StackTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@StackTrace
public class PostingSynchronizedService {
    private final PostingService postingService;

    public synchronized void syncView(long id) {
        System.out.println("postingService.getClass() = " + postingService.getClass());
        postingService.view(id);
    }
}
