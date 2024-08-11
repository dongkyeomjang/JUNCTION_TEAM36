package com.junction.smart_blinker.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BlinkerRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_blinker_id", nullable = false)
    private Blinker fromBlinker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_blinker_id", nullable = false)
    private Blinker toBlinker;

    @Column(name = "relation_type", nullable = false)
    private String relationType;

    @Builder
    public BlinkerRelation(Blinker fromBlinker, Blinker toBlinker, String relationType) {
        this.fromBlinker = fromBlinker;
        this.toBlinker = toBlinker;
        this.relationType = relationType;
    }
}
