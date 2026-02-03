package com.example.shiftmate.domain.shiftTemplate.repository;

import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface ShiftTemplateRepository extends JpaRepository< ShiftTemplate , Long> {


    boolean existsByStoreId(Long storeId);
}
