package com.study.concurrency.service;

import com.study.concurrency.domain.Posting;
import com.study.concurrency.repository.PostingRepository;
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
class PostingServiceTest {

    @Autowired
    private PostingService postingService;
    @Autowired
    private PostingSynchronizedService postingSynchronizedService;
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

    /** 방법 0 - 로직에 synchronized 추가하기(실패)
     * synchronized를 사용해도 문제가 발생하는 이유?
     * transactional annotation을 사용하면 발생하는 트랜잭션 전,후처리에 대해서는 synchronized가 보장되지 않기 때문
     * Transactional annotation을 사용 할 수 없게 된다.
     */
    @Test
    public void viewSynchronized100Times() throws InterruptedException {
        int threads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    postingService.viewSync(1L);
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

        Assertions.assertEquals(100L, posting.getViewCount());
    }

    /** 방법 1-1 - 다른 클래스에서 synchronized로 로직을 호출하는 메소드 구현 (성공)
     * synchronized의 문제점: 스레드 단위에서의 동기화를 지원해주기 때문에 프로세스가 많아지면 보장을 못함
     */
    @Test
    public void viewWrappedSynchronized100Times() throws InterruptedException {
        int threads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    postingSynchronizedService.syncView(1L);
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
        // synchronized를 사용해도 문제가 발생하는 이유?
        // transactional annotation을 사용하면 postingService.viewSync(1L)
        // 발생하는 트랜잭션 전,후처리에 대해서는 synchronized가 보장되지 않기 때문
        Assertions.assertEquals(100L, posting.getViewCount());
    }

    /** 방법 1-2 - synchronized 메소드에 Transactional 옵션 Serializable 설정 (성공)
     *
     */
    @Test
    public void viewSerializable100Times() throws InterruptedException {
        int threads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    postingService.viewSyncAndIsolationSerializable(1L);
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

        //transactional annotation의 isolation level을 설정해주면 해결가능
        //단, 하나의 프로세스 인 경우만 해당 동작이 보장됨
        Assertions.assertEquals(100L, posting.getViewCount());
    }

}