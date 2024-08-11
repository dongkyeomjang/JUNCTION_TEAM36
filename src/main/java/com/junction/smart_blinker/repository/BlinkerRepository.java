package com.junction.smart_blinker.repository;

import com.junction.smart_blinker.domain.Blinker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlinkerRepository extends JpaRepository<Blinker, Long> {
    
}
