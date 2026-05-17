package org.example.polify.attempt;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.polify.common.log.AuditLogger;

@Service
@EnableConfigurationProperties(AttemptProperties.class)
public class AttemptStateGuard {
    private final JdbcTemplate jdbcTemplate;
    private final AttemptProperties properties;

    public AttemptStateGuard(JdbcTemplate jdbcTemplate, AttemptProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    @Transactional
    public AttemptRow loadForUpdateAndGuard(long userId, long attemptId) {
        // Expire if needed (use DB clock).
        int expired = jdbcTemplate.update("""
            update attempts
            set status = 'ABANDONED'
            where id = ?
              and user_id = ?
              and status = 'IN_PROGRESS'
              and started_at < (now() - (? * interval '1 second'))
            """,
            attemptId,
            userId,
            properties.getTtlSeconds()
        );
        if (expired > 0) {
            // surveyId unknown here without extra query; keep minimal.
            AuditLogger.info("ATTEMPT_ABANDONED", "Attempt expired and was abandoned", userId, null, attemptId, null, null, "ABANDONED");
        }

        List<AttemptRow> rows = jdbcTemplate.query("""
            select id, survey_id, user_id, status, started_at, completed_at
            from attempts
            where id = ? and user_id = ?
            for update
            """,
            (rs, rn) -> new AttemptRow(
                rs.getLong("id"),
                rs.getLong("survey_id"),
                rs.getLong("user_id"),
                rs.getString("status"),
                toInstant(rs.getTimestamp("started_at")),
                toInstant(rs.getTimestamp("completed_at"))
            ),
            attemptId,
            userId
        );
        if (rows.isEmpty()) {
            throw new AttemptNotFoundException(attemptId);
        }
        return rows.get(0);
    }

    @Transactional
    public void expireAllForUser(long userId) {
        int expired = jdbcTemplate.update("""
            update attempts
            set status = 'ABANDONED'
            where user_id = ?
              and status = 'IN_PROGRESS'
              and started_at < (now() - (? * interval '1 second'))
            """,
            userId,
            properties.getTtlSeconds()
        );
        if (expired > 0) {
            AuditLogger.info("ATTEMPTS_ABANDONED", "Expired attempts abandoned", userId, null, null, null, null, "ABANDONED");
        }
    }

    @Transactional(readOnly = true)
    public boolean hasActiveInProgress(long userId) {
        Integer cnt = jdbcTemplate.queryForObject(
            "select count(*) from attempts where user_id = ? and status = 'IN_PROGRESS'",
            Integer.class,
            userId
        );
        return cnt != null && cnt > 0;
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    public record AttemptRow(
        long attemptId,
        long surveyId,
        long userId,
        String status,
        Instant startedAt,
        Instant completedAt
    ) {}
}
