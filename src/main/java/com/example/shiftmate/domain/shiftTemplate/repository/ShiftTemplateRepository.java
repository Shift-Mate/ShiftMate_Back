package com.example.shiftmate.domain.shiftTemplate.repository;

import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.shiftTemplate.entity.TemplateType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface ShiftTemplateRepository extends JpaRepository< ShiftTemplate , Long> {


    boolean existsByStoreId(Long storeId);

    Optional<List<ShiftTemplate>> findByStoreId(Long storeId);

    Optional<List<ShiftTemplate>> findByStoreIdAndTemplateType(Long storeId, TemplateType templateType);
}
