package org.example.polify.attempt;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.example.polify.attempt.dto.SubmitAnswerRequest;
import org.example.polify.attempt.dto.AttemptDetailsResponse;
import org.example.polify.attempt.dto.ActiveAttemptResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttemptService {
    private final JdbcTemplate jdbcTemplate;

    public AttemptService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public long startAttempt(long userId, long surveyId) {
        Integer exists = jdbcTemplate.queryForObject(
            "select 1 from surveys where id = ?",
            Integer.class,
            surveyId
        );
        if (exists == null) {
            throw new AttemptSurveyNotFoundException(surveyId);
        }

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "insert into attempts (survey_id, user_id, status, started_at, completed_at) values (?,?, 'IN_PROGRESS', now(), null)",
                    new String[] { "id" }
                );
                ps.setLong(1, surveyId);
                ps.setLong(2, userId);
                return ps;
            }, keyHolder);
            Number id = extractId(keyHolder);
            if (id == null) {
                throw new IllegalStateException("Failed to create attempt");
            }
            return id.longValue();
        } catch (DuplicateKeyException ex) {
            // Uniqueness enforced by partial indexes: already have IN_PROGRESS or already COMPLETED.
            throw new AttemptNotAllowedException("Attempt already in progress or already completed for this survey");
        }
    }

    @Transactional
    public void submitAnswer(long userId, long attemptId, SubmitAnswerRequest request) {
        AttemptRow attempt = loadAttemptForUpdate(userId, attemptId);
        if (!"IN_PROGRESS".equals(attempt.status())) {
            throw statusNotAllowed(attempt.status());
        }

        QuestionRow question = loadQuestion(attempt.surveyId(), request.getQuestionId());
        enforceSequentialRequired(attemptId, attempt.surveyId(), question.position());

        long answerId = upsertAnswerHeader(attemptId, attempt.surveyId(), question.questionId());

        switch (question.type()) {
            case "TEXT" -> submitText(answerId, request.getTextValue(), question.required());
            case "RADIO", "SELECT" -> submitSingleOption(answerId, question.questionId(), request.getOptionId(), request.getOptionIds(),
                request.getTextValue(), request.getPriority(), question.required());
            case "CHECKBOX" -> submitMultiOption(answerId, question.questionId(), request.getOptionIds(), request.getOptionId(),
                request.getTextValue(), request.getPriority(), question.required());
            case "PRIORITY" -> submitPriority(answerId, question.questionId(), request.getPriority(), request.getOptionId(),
                request.getOptionIds(), request.getTextValue(), question.required());
            default -> throw new IllegalStateException("Unsupported question type: " + question.type());
        }
    }

    @Transactional
    public void completeAttempt(long userId, long attemptId) {
        AttemptRow attempt = loadAttemptForUpdate(userId, attemptId);
        if (!"IN_PROGRESS".equals(attempt.status())) {
            throw statusNotAllowed(attempt.status());
        }

        int missingRequired = countMissingRequiredDetails(attemptId, attempt.surveyId());
        if (missingRequired > 0) {
            throw new AttemptValidationException("Missing required answers: " + missingRequired);
        }

        // Mark completed.
        int updated = jdbcTemplate.update(
            "update attempts set status = 'COMPLETED', completed_at = now() where id = ? and user_id = ? and status = 'IN_PROGRESS'",
            attemptId,
            userId
        );
        if (updated != 1) {
            throw new AttemptNotAllowedException("Attempt cannot be completed");
        }

        // Create ledger entry (atomic: amount comes from surveys in the same statement).
        int inserted = jdbcTemplate.update("""
            insert into ledger_entries (attempt_id, user_id, amount_bani, currency, status, created_at)
            select ?, ?, s.reward_amount_bani, 'MDL', 'CREATED', now()
            from surveys s
            where s.id = ?
            """,
            attemptId,
            userId,
            attempt.surveyId()
        );
        if (inserted != 1) {
            throw new IllegalStateException("Failed to create ledger entry");
        }
    }

    @Transactional(readOnly = true)
    public AttemptDetailsResponse getAttempt(long userId, long attemptId) {
        AttemptRow attempt = loadAttempt(userId, attemptId);
        List<AttemptDetailsResponse.AnswerDto> answers = loadAnswers(attemptId);
        Long nextQuestionId = findNextQuestionId(attemptId, attempt.surveyId());
        return new AttemptDetailsResponse(
            attempt.attemptId(),
            attempt.surveyId(),
            attempt.status(),
            attempt.startedAt(),
            attempt.completedAt(),
            answers,
            nextQuestionId
        );
    }

    @Transactional(readOnly = true)
    public List<AttemptDetailsResponse> listAttempts(long userId, Long surveyId) {
        List<AttemptRow> attempts;
        if (surveyId == null) {
            attempts = jdbcTemplate.query("""
                select id, survey_id, user_id, status, started_at, completed_at
                from attempts
                where user_id = ?
                order by started_at desc
                """,
                (rs, rn) -> new AttemptRow(
                    rs.getLong("id"),
                    rs.getLong("survey_id"),
                    rs.getLong("user_id"),
                    rs.getString("status"),
                    toInstant(rs.getTimestamp("started_at")),
                    toInstant(rs.getTimestamp("completed_at"))
                ),
                userId
            );
        } else {
            attempts = jdbcTemplate.query("""
                select id, survey_id, user_id, status, started_at, completed_at
                from attempts
                where user_id = ?
                  and survey_id = ?
                order by started_at desc
                """,
                (rs, rn) -> new AttemptRow(
                    rs.getLong("id"),
                    rs.getLong("survey_id"),
                    rs.getLong("user_id"),
                    rs.getString("status"),
                    toInstant(rs.getTimestamp("started_at")),
                    toInstant(rs.getTimestamp("completed_at"))
                ),
                userId,
                surveyId
            );
        }

        return attempts.stream()
            .map(a -> new AttemptDetailsResponse(
                a.attemptId(),
                a.surveyId(),
                a.status(),
                a.startedAt(),
                a.completedAt(),
                List.of(),
                null
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public ActiveAttemptResponse findActiveAttempt(long userId, long surveyId) {
        List<ActiveAttemptResponse> rows = jdbcTemplate.query("""
            select id, survey_id, status
            from attempts
            where user_id = ?
              and survey_id = ?
              and status = 'IN_PROGRESS'
            order by started_at desc
            limit 1
            """,
            (rs, rn) -> new ActiveAttemptResponse(
                rs.getLong("id"),
                rs.getLong("survey_id"),
                rs.getString("status")
            ),
            userId,
            surveyId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private AttemptRow loadAttemptForUpdate(long userId, long attemptId) {
        List<AttemptRow> rows = jdbcTemplate.query("""
            select id, survey_id, user_id, status
            from attempts
            where id = ? and user_id = ?
            for update
            """,
            (rs, rn) -> new AttemptRow(
                rs.getLong("id"),
                rs.getLong("survey_id"),
                rs.getLong("user_id"),
                rs.getString("status"),
                null,
                null
            ),
            attemptId,
            userId
        );
        if (rows.isEmpty()) {
            throw new AttemptNotFoundException(attemptId);
        }
        return rows.get(0);
    }

    private AttemptRow loadAttempt(long userId, long attemptId) {
        List<AttemptRow> rows = jdbcTemplate.query("""
            select id, survey_id, user_id, status, started_at, completed_at
            from attempts
            where id = ? and user_id = ?
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

    private QuestionRow loadQuestion(long surveyId, long questionId) {
        List<QuestionRow> rows = jdbcTemplate.query("""
            select id, type, is_required, position
            from questions
            where id = ? and survey_id = ?
            """,
            (rs, rn) -> new QuestionRow(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getBoolean("is_required"),
                rs.getInt("position")
            ),
            questionId,
            surveyId
        );
        if (rows.isEmpty()) {
            throw new QuestionNotFoundException(questionId);
        }
        return rows.get(0);
    }

    private long upsertAnswerHeader(long attemptId, long surveyId, long questionId) {
        // Create header if missing.
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int inserted = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "insert into answers (attempt_id, survey_id, question_id, answered_at) values (?,?,?, now()) on conflict (attempt_id, question_id) do nothing",
                new String[] { "id" }
            );
            ps.setLong(1, attemptId);
            ps.setLong(2, surveyId);
            ps.setLong(3, questionId);
            return ps;
        }, keyHolder);

        if (inserted == 1) {
            Number id = extractId(keyHolder);
            if (id == null) {
                // Fallback: select.
                return jdbcTemplate.queryForObject(
                    "select id from answers where attempt_id = ? and question_id = ?",
                    Long.class,
                    attemptId,
                    questionId
                );
            }
            return id.longValue();
        }

        Long existing = jdbcTemplate.queryForObject(
            "select id from answers where attempt_id = ? and question_id = ?",
            Long.class,
            attemptId,
            questionId
        );
        if (existing == null) {
            throw new IllegalStateException("Failed to load existing answer header");
        }

        // Update answered_at on resubmit.
        jdbcTemplate.update("update answers set answered_at = now() where id = ?", existing);
        return existing;
    }

    private static Number extractId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key != null) {
            return key;
        }
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null) {
            return null;
        }
        Object id = keys.get("id");
        return id instanceof Number n ? n : null;
    }

    private void submitText(long answerId, String textValue, boolean required) {
        String value = textValue == null ? null : textValue.trim();
        if (value == null || value.isEmpty()) {
            if (required) {
                throw new AttemptValidationException("Text answer is required");
            }
            // Optional empty: delete any existing details.
            jdbcTemplate.update("delete from answer_text where answer_id = ?", answerId);
            return;
        }

        jdbcTemplate.update(
            "insert into answer_text (answer_id, value_text) values (?, ?) on conflict (answer_id) do update set value_text = excluded.value_text",
            answerId,
            value
        );

        // Ensure no other type details exist.
        jdbcTemplate.update("delete from answer_options where answer_id = ?", answerId);
        jdbcTemplate.update("delete from answer_priority where answer_id = ?", answerId);
    }

    private void submitSingleOption(
        long answerId,
        long questionId,
        Long optionId,
        List<Long> optionIds,
        String textValue,
        List<SubmitAnswerRequest.PriorityItem> priority,
        boolean required
    ) {
        // Strict payload: only optionId allowed.
        if (textValue != null) {
            throw new AttemptValidationException("TEXT value is not allowed for this question type");
        }
        if (priority != null && !priority.isEmpty()) {
            throw new AttemptValidationException("PRIORITY payload is not allowed for this question type");
        }
        if (optionIds != null && !optionIds.isEmpty()) {
            throw new AttemptValidationException("Use optionId (single) for RADIO/SELECT");
        }

        if (optionId == null) {
            if (required) {
                throw new AttemptValidationException("Option answer is required");
            }
            jdbcTemplate.update("delete from answer_options where answer_id = ?", answerId);
            jdbcTemplate.update("delete from answer_text where answer_id = ?", answerId);
            jdbcTemplate.update("delete from answer_priority where answer_id = ?", answerId);
            return;
        }

        ensureOptionBelongsToQuestion(questionId, optionId);

        jdbcTemplate.update("delete from answer_options where answer_id = ?", answerId);
        jdbcTemplate.update(
            "insert into answer_options (answer_id, question_id, option_id) values (?,?,?)",
            answerId,
            questionId,
            optionId
        );

        jdbcTemplate.update("delete from answer_text where answer_id = ?", answerId);
        jdbcTemplate.update("delete from answer_priority where answer_id = ?", answerId);
    }

    private void submitMultiOption(
        long answerId,
        long questionId,
        List<Long> optionIds,
        Long optionId,
        String textValue,
        List<SubmitAnswerRequest.PriorityItem> priority,
        boolean required
    ) {
        // Strict payload: only optionIds allowed.
        if (textValue != null) {
            throw new AttemptValidationException("TEXT value is not allowed for this question type");
        }
        if (priority != null && !priority.isEmpty()) {
            throw new AttemptValidationException("PRIORITY payload is not allowed for this question type");
        }
        if (optionId != null) {
            throw new AttemptValidationException("Use optionIds (list) for CHECKBOX");
        }

        List<Long> ids = optionIds == null ? List.of() : optionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            if (required) {
                throw new AttemptValidationException("At least one option is required");
            }
            jdbcTemplate.update("delete from answer_options where answer_id = ?", answerId);
            jdbcTemplate.update("delete from answer_text where answer_id = ?", answerId);
            jdbcTemplate.update("delete from answer_priority where answer_id = ?", answerId);
            return;
        }

        for (Long id : ids) {
            ensureOptionBelongsToQuestion(questionId, id);
        }

        jdbcTemplate.update("delete from answer_options where answer_id = ?", answerId);
        for (Long id : ids) {
            jdbcTemplate.update(
                "insert into answer_options (answer_id, question_id, option_id) values (?,?,?)",
                answerId,
                questionId,
                id
            );
        }

        jdbcTemplate.update("delete from answer_text where answer_id = ?", answerId);
        jdbcTemplate.update("delete from answer_priority where answer_id = ?", answerId);
    }

    private void submitPriority(
        long answerId,
        long questionId,
        List<SubmitAnswerRequest.PriorityItem> priority,
        Long optionId,
        List<Long> optionIds,
        String textValue,
        boolean required
    ) {
        // Strict payload: only priority allowed.
        if (textValue != null) {
            throw new AttemptValidationException("TEXT value is not allowed for this question type");
        }
        if (optionId != null) {
            throw new AttemptValidationException("optionId is not allowed for PRIORITY");
        }
        if (optionIds != null && !optionIds.isEmpty()) {
            throw new AttemptValidationException("optionIds is not allowed for PRIORITY");
        }

        List<SubmitAnswerRequest.PriorityItem> items = priority == null ? List.of() : priority;
        if (items.isEmpty()) {
            if (required) {
                throw new AttemptValidationException("Priority answer is required");
            }
            jdbcTemplate.update("delete from answer_priority where answer_id = ?", answerId);
            jdbcTemplate.update("delete from answer_text where answer_id = ?", answerId);
            jdbcTemplate.update("delete from answer_options where answer_id = ?", answerId);
            return;
        }

        Set<Long> priorityOptionIds = new HashSet<>();
        Set<Integer> ranks = new HashSet<>();
        for (SubmitAnswerRequest.PriorityItem item : items) {
            if (item.getOptionId() == null || item.getRank() == null || item.getRank() <= 0) {
                throw new AttemptValidationException("Priority items must have optionId and positive rank");
            }
            if (!priorityOptionIds.add(item.getOptionId())) {
                throw new AttemptValidationException("Duplicate optionId in priority");
            }
            if (!ranks.add(item.getRank())) {
                throw new AttemptValidationException("Duplicate rank in priority");
            }
            ensureOptionBelongsToQuestion(questionId, item.getOptionId());
        }

        // For required PRIORITY: must rank ALL active options, with ranks 1..N.
        if (required) {
            List<Long> activeOptions = jdbcTemplate.query(
                "select id from question_options where question_id = ? and is_active = true order by position asc",
                (rs, rn) -> rs.getLong(1),
                questionId
            );
            Set<Long> activeSet = new HashSet<>(activeOptions);
            if (priorityOptionIds.size() != activeSet.size() || !activeSet.equals(priorityOptionIds)) {
                throw new AttemptValidationException("You must rank all options for this question");
            }
            int n = activeSet.size();
            for (int r = 1; r <= n; r++) {
                if (!ranks.contains(r)) {
                    throw new AttemptValidationException("Priority ranks must be 1.." + n);
                }
            }
        }

        jdbcTemplate.update("delete from answer_priority where answer_id = ?", answerId);
        for (SubmitAnswerRequest.PriorityItem item : items) {
            jdbcTemplate.update(
                "insert into answer_priority (answer_id, question_id, option_id, rank) values (?,?,?,?)",
                answerId,
                questionId,
                item.getOptionId(),
                item.getRank()
            );
        }

        jdbcTemplate.update("delete from answer_text where answer_id = ?", answerId);
        jdbcTemplate.update("delete from answer_options where answer_id = ?", answerId);
    }

    private AttemptNotAllowedException statusNotAllowed(String status) {
        if ("COMPLETED".equals(status)) {
            return new AttemptNotAllowedException("You already completed this survey");
        }
        if ("ABANDONED".equals(status)) {
            return new AttemptNotAllowedException("Attempt expired. Start again.");
        }
        return new AttemptNotAllowedException("Attempt is not in progress");
    }

    private void ensureOptionBelongsToQuestion(long questionId, long optionId) {
        Integer ok = jdbcTemplate.queryForObject(
            "select 1 from question_options where id = ? and question_id = ? and is_active = true",
            Integer.class,
            optionId,
            questionId
        );
        if (ok == null) {
            throw new AttemptValidationException("Option does not belong to question: " + optionId);
        }
    }

    private int countMissingRequiredDetails(long attemptId, long surveyId) {
        Integer cnt = jdbcTemplate.queryForObject("""
            select count(*)
            from questions q
            where q.survey_id = ?
              and q.is_required = true
              and (
                q.type = 'TEXT' and not exists (
                  select 1
                  from answers a
                  join answer_text t on t.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                    and length(btrim(t.value_text)) > 0
                )
                or q.type in ('RADIO','SELECT') and not exists (
                  select 1
                  from answers a
                  join answer_options ao on ao.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                  group by ao.answer_id
                  having count(*) = 1
                )
                or q.type = 'CHECKBOX' and not exists (
                  select 1
                  from answers a
                  join answer_options ao on ao.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                )
                or q.type = 'PRIORITY' and not exists (
                  select 1
                  from answers a
                  join answer_priority ap on ap.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                  group by ap.answer_id
                  having
                    count(*) = (
                      select count(*) from question_options qo
                      where qo.question_id = q.id and qo.is_active = true
                    )
                    and count(distinct ap.rank) = count(*)
                    and min(ap.rank) = 1
                    and max(ap.rank) = count(*)
                )
              )
            """,
            Integer.class,
            surveyId,
            attemptId,
            attemptId,
            attemptId,
            attemptId
        );
        return cnt == null ? 0 : cnt;
    }

    private void enforceSequentialRequired(long attemptId, long surveyId, int targetPosition) {
        // Rule: cannot proceed to a later question if there are missing required details earlier.
        Integer missingEarlier = jdbcTemplate.queryForObject("""
            select count(*)
            from questions q
            where q.survey_id = ?
              and q.is_required = true
              and q.position < ?
              and (
                q.type = 'TEXT' and not exists (
                  select 1
                  from answers a
                  join answer_text t on t.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                    and length(btrim(t.value_text)) > 0
                )
                or q.type in ('RADIO','SELECT','CHECKBOX') and not exists (
                  select 1
                  from answers a
                  join answer_options ao on ao.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                )
                or q.type = 'PRIORITY' and not exists (
                  select 1
                  from answers a
                  join answer_priority ap on ap.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                )
              )
            """,
            Integer.class,
            surveyId,
            targetPosition,
            attemptId,
            attemptId,
            attemptId
        );
        int missing = missingEarlier == null ? 0 : missingEarlier;
        if (missing > 0) {
            throw new AttemptValidationException("You must answer required previous questions before continuing");
        }
    }

    private Long findNextQuestionId(long attemptId, long surveyId) {
        // First required question (by position) that doesn't have required details yet.
        return jdbcTemplate.query("""
            select q.id
            from questions q
            where q.survey_id = ?
              and q.is_required = true
              and (
                q.type = 'TEXT' and not exists (
                  select 1
                  from answers a
                  join answer_text t on t.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                    and length(btrim(t.value_text)) > 0
                )
                or q.type in ('RADIO','SELECT','CHECKBOX') and not exists (
                  select 1
                  from answers a
                  join answer_options ao on ao.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                )
                or q.type = 'PRIORITY' and not exists (
                  select 1
                  from answers a
                  join answer_priority ap on ap.answer_id = a.id
                  where a.attempt_id = ?
                    and a.question_id = q.id
                )
              )
            order by q.position asc
            limit 1
            """,
            (rs) -> rs.next() ? rs.getLong(1) : null,
            surveyId,
            attemptId,
            attemptId,
            attemptId
        );
    }

    private List<AttemptDetailsResponse.AnswerDto> loadAnswers(long attemptId) {
        Map<Long, AttemptDetailsResponse.AnswerDto> byAnswerId = new LinkedHashMap<>();

        jdbcTemplate.query(
            """
                select a.id as answer_id, a.question_id, a.answered_at, t.value_text
                from answers a
                left join answer_text t on t.answer_id = a.id
                where a.attempt_id = ?
                order by a.answered_at asc
                """,
            (rs) -> {
                long answerId = rs.getLong("answer_id");
                long questionId = rs.getLong("question_id");
                Instant answeredAt = toInstant(rs.getTimestamp("answered_at"));
                String textValue = rs.getString("value_text");
                byAnswerId.put(answerId, new AttemptDetailsResponse.AnswerDto(
                    answerId,
                    questionId,
                    answeredAt,
                    textValue,
                    List.of(),
                    List.of()
                ));
            },
            attemptId
        );

        if (byAnswerId.isEmpty()) {
            return List.of();
        }

        // Load option ids for all answers.
        List<Map.Entry<Long, Long>> optionRows = jdbcTemplate.query(
            """
                select ao.answer_id, ao.option_id
                from answer_options ao
                join answers a on a.id = ao.answer_id
                where a.attempt_id = ?
                order by ao.option_id asc
                """,
            (rs, rn) -> Map.entry(rs.getLong("answer_id"), rs.getLong("option_id")),
            attemptId
        );
        Map<Long, List<Long>> optionIds = optionRows.stream().collect(Collectors.groupingBy(
            Map.Entry::getKey,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
        ));

        // Load priority items for all answers.
        List<Map.Entry<Long, AttemptDetailsResponse.PriorityDto>> prioRows = jdbcTemplate.query(
            """
                select ap.answer_id, ap.option_id, ap.rank
                from answer_priority ap
                join answers a on a.id = ap.answer_id
                where a.attempt_id = ?
                order by ap.rank asc
                """,
            (rs, rn) -> Map.entry(
                rs.getLong("answer_id"),
                new AttemptDetailsResponse.PriorityDto(rs.getLong("option_id"), rs.getInt("rank"))
            ),
            attemptId
        );
        Map<Long, List<AttemptDetailsResponse.PriorityDto>> priority = prioRows.stream().collect(Collectors.groupingBy(
            Map.Entry::getKey,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
        ));

        // Merge into DTOs (replace with copies to keep them immutable).
        return byAnswerId.values().stream()
            .map(a -> new AttemptDetailsResponse.AnswerDto(
                a.getAnswerId(),
                a.getQuestionId(),
                a.getAnsweredAt(),
                a.getTextValue(),
                optionIds.getOrDefault(a.getAnswerId(), List.of()),
                priority.getOrDefault(a.getAnswerId(), List.of())
            ))
            .toList();
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    private record AttemptRow(
        long attemptId,
        long surveyId,
        long userId,
        String status,
        Instant startedAt,
        Instant completedAt
    ) {}

    private record QuestionRow(long questionId, String type, boolean required, int position) {}
}
