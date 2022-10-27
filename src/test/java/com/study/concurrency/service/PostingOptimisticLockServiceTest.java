package com.study.concurrency.service;

import com.study.concurrency.domain.Posting;
import com.study.concurrency.repository.PostingRepository;
import com.study.concurrency.service.dblock.PostingOptimisticLockService;
import com.study.concurrency.service.dblock.PostingPessimisticLockService;
import com.study.concurrency.service.dblock.facade.OptimisticFacadeService;
import com.study.concurrency.service.synchronize.PostingSynchronizedService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class PostingOptimisticLockServiceTest {

    @Autowired
    private OptimisticFacadeService postingService;
    @Autowired
    private PostingRepository postingRepository;

    @BeforeEach
    public void init() {
        Posting posting = new Posting(1L, "HelloTest");
        postingRepository.saveAndFlush(posting);
    }

    @AfterEach
    public void finish() {
        postingRepository.deleteAll();
    }

    @Test
    public void view1Time() throws InterruptedException {
        postingService.view(1L);

        Posting posting = postingRepository.findById(1L).orElseThrow(
                () -> new RuntimeException("데이터를 못찾았습니다.")
        );

        Assertions.assertEquals(posting.getViewCount(), 1L);
    }

    /**
     * 재시도를 해야하기 때문에 코드 짜기가 번거로움,<br/>
     * 또한 충돌이 빈번하다면 무한 재시작이 일어나므로 성능상으로 문제가 발생할 수 있음
     * @throws InterruptedException
     */
    @Test
    public void view100Times() throws InterruptedException {
        int threads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    postingService.view(1L);
                } catch (Exception e) {
                    throw new RuntimeException();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Posting posting = postingRepository.findById(1L).orElseThrow(
                () -> new RuntimeException("데이터를 못찾았습니다.")
        );

        //문제 발생!!!!!
        Assertions.assertEquals(100L, posting.getViewCount());
    }


}