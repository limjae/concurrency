package com.study.concurrency.service;

import com.study.concurrency.domain.Posting;
import com.study.concurrency.repository.PostingRepository;
import jdk.jfr.StackTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingService {
    private final PostingRepository postingRepository;

    @Transactional
    public void view(long id) {
        Posting posting = postingRepository.findById(id).orElseThrow(
                () -> new RuntimeException("데이터를 못찾았습니다.")
        );

        posting.update();

        postingRepository.saveAndFlush(posting);
    }

    @Transactional
    public synchronized void viewSync(long id) {
        Posting posting = postingRepository.findById(id).orElseThrow(
                () -> new RuntimeException("데이터를 못찾았습니다.")
        );

        posting.update();

        postingRepository.saveAndFlush(posting);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized void viewSyncAndIsolationSerializable(long id) {
        Posting posting = postingRepository.findById(id).orElseThrow(
                () -> new RuntimeException("데이터를 못찾았습니다.")
        );

        posting.update();

        postingRepository.saveAndFlush(posting);
    }
}
