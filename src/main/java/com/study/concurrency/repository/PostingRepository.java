package com.study.concurrency.repository;

import com.study.concurrency.domain.Posting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface PostingRepository extends JpaRepository<Posting, Long> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Posting p where p.id = :id")
    Optional<Posting> findByIdWithPessimisticLock(@Param("id")Long id);

    @Lock(value = LockModeType.OPTIMISTIC)
    @Query("select p from Posting p where p.id = :id")
    Optional<Posting> findByIdWithOptimisticLock(@Param("id")Long id);
}
