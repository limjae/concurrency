package com.study.concurrency.service;

import com.study.concurrency.domain.Posting;
import com.study.concurrency.repository.PostingRepository;
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
class PostingServiceTest {

    @Autowired
    private PostingService postingService;
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
    public void view1Time() {
        postingService.view(1L);

        Posting posting = postingRepository.findById(1L).orElseThrow(
                () -> new RuntimeException("데이터를 못찾았습니다.")
        );

        Assertions.assertEquals(posting.getViewCount(), 1L);
    }

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