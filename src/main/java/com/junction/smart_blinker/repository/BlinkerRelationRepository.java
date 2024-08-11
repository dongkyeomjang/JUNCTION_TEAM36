package com.junction.smart_blinker.repository;

import com.junction.smart_blinker.domain.Blinker;
import com.junction.smart_blinker.domain.BlinkerRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlinkerRelationRepository extends JpaRepository<BlinkerRelation, Long> {
    @Query("select br.toBlinker from BlinkerRelation br where br.fromBlinker.id = ?1 and br.relationType = 'PARALLEL'")
    List<Blinker> findParallelBlinkers(Long blinkerId);

    @Query("select br.toBlinker from BlinkerRelation br where br.fromBlinker.id = ?1 and br.relationType = 'CROSS'")
    List<Blinker> findCrossBlinkers(Long blinkerId);

    @Query("select br.toBlinker from BlinkerRelation br where br.fromBlinker.id = ?1 and br.relationType = 'NEXT'")
    List<Blinker> findNextBlinkers(Long blinkerId);
}
