package com.study.concurrency.domain;

import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Posting {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contents;
    @NotNull
    private long viewCount = 0;


    public Posting(Long id, String contents){
        this.id = id;
        this.contents = contents;
    }
    public void update() {
        this.viewCount += 1;
    }

}
