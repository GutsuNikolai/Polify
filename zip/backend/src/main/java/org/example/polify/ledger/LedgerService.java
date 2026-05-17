package org.example.polify.ledger;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.example.polify.ledger.dto.LedgerEntryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {
    private final JdbcTemplate jdbcTemplate;

    public LedgerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> list(long userId, Long attemptId) {
        if (attemptId == null) {
            return jdbcTemplate.query("""
                select id, attempt_id, user_id, amount_bani, currency, status, created_at
                from ledger_entries
                where user_id = ?
                order by created_at desc
                """,
                (rs, rn) -> new LedgerEntryResponse(
                    rs.getLong("id"),
                    rs.getLong("attempt_id"),
                    rs.getLong("user_id"),
                    rs.getInt("amount_bani"),
                    rs.getString("currency"),
                    rs.getString("status"),
                    toInstant(rs.getTimestamp("created_at"))
                ),
                userId
            );
        }

        return jdbcTemplate.query("""
            select id, attempt_id, user_id, amount_bani, currency, status, created_at
            from ledger_entries
            where user_id = ?
              and attempt_id = ?
            order by created_at desc
            """,
            (rs, rn) -> new LedgerEntryResponse(
                rs.getLong("id"),
                rs.getLong("attempt_id"),
                rs.getLong("user_id"),
                rs.getInt("amount_bani"),
                rs.getString("currency"),
                rs.getString("status"),
                toInstant(rs.getTimestamp("created_at"))
            ),
            userId,
            attemptId
        );
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}

