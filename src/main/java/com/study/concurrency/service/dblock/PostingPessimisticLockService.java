package com.study.concurrency.service.dblock;

import com.study.concurrency.domain.Posting;
import com.study.concurrency.repository.PostingRepository;
import com.study.concurrency.service.PostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostingPessimisticLockService {
    private final PostingRepository postingRepository;

    @Transactional
    public void view(long id) {
        Posting posting = postingRepository.findByIdWithPessimisticLock(id).orElseThrow(
                () -> new RuntimeException("데이터를 못찾았습니다.")
        );

        posting.update();

        postingRepository.saveAndFlush(posting);
    }
}
