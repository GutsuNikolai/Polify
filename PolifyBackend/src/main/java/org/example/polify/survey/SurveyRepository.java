package org.example.polify.survey;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {
    @Query("""
        select distinct s
        from SurveyEntity s
        left join fetch s.questions q
        left join fetch q.options o
        where s.id = :id
        """)
    Optional<SurveyEntity> findByIdWithQuestionsAndOptions(@Param("id") long id);
}

