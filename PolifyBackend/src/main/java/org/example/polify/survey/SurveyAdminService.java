package org.example.polify.survey;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.example.polify.survey.dto.CreateSurveyRequest;
import org.example.polify.user.UserRoleGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyAdminService {
    private final SurveyRepository surveyRepository;
    private final UserRoleGuard roleGuard;

    public SurveyAdminService(SurveyRepository surveyRepository, UserRoleGuard roleGuard) {
        this.surveyRepository = surveyRepository;
        this.roleGuard = roleGuard;
    }

    @Transactional
    public long createSurvey(long userId, CreateSurveyRequest request) {
        roleGuard.requireModerator(userId);

        List<CreateSurveyRequest.CreateQuestion> qs = request.getQuestions();
        if (qs == null || qs.isEmpty()) {
            throw new IllegalArgumentException("Survey must have at least one question");
        }

        // Validate unique positions and type/options consistency.
        Set<Integer> positions = new HashSet<>();
        for (CreateSurveyRequest.CreateQuestion q : qs) {
            if (!positions.add(q.getPosition())) {
                throw new IllegalArgumentException("Duplicate question position: " + q.getPosition());
            }
            validateQuestion(q);
        }

        Instant now = Instant.now();

        SurveyEntity survey = new SurveyEntity();
        survey.setTitle(request.getTitle().trim());
        survey.setDescription(normalizeOptional(request.getDescription()));
        survey.setRewardAmountBani((short) request.getRewardAmountBani());
        survey.setTargetCompletions(request.getTargetCompletions());
        survey.setCreatedByUserId(userId);
        survey.setCreatedAt(now);

        for (CreateSurveyRequest.CreateQuestion q : qs) {
            QuestionEntity qe = new QuestionEntity();
            qe.setSurvey(survey);
            qe.setType(q.getType());
            qe.setText(q.getText().trim());
            qe.setPosition(q.getPosition());
            qe.setRequired(q.isRequired());
            qe.setCreatedAt(now);

            if (q.getOptions() != null) {
                for (CreateSurveyRequest.CreateOption o : q.getOptions()) {
                    QuestionOptionEntity oe = new QuestionOptionEntity();
                    oe.setQuestion(qe);
                    oe.setLabel(o.getLabel().trim());
                    oe.setValue(o.getValue().trim());
                    oe.setPosition(o.getPosition());
                    oe.setActive(true);
                    oe.setMediaUrl(normalizeOptional(o.getMediaUrl()));
                    oe.setCreatedAt(now);
                    qe.getOptions().add(oe);
                }
            }

            survey.getQuestions().add(qe);
        }

        SurveyEntity saved = surveyRepository.save(survey);
        return saved.getId();
    }

    private static void validateQuestion(CreateSurveyRequest.CreateQuestion q) {
        if (q.getType() == null) {
            throw new IllegalArgumentException("Question type is required");
        }
        if (q.getText() == null || q.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Question text is required");
        }

        boolean expectsOptions = q.getType() != QuestionType.TEXT;
        if (!expectsOptions) {
            if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                throw new IllegalArgumentException("TEXT question cannot have options");
            }
            return;
        }

        if (q.getOptions() == null || q.getOptions().isEmpty()) {
            throw new IllegalArgumentException("Question options are required for type: " + q.getType());
        }

        Set<Integer> pos = new HashSet<>();
        Set<String> values = new HashSet<>();
        for (CreateSurveyRequest.CreateOption o : q.getOptions()) {
            if (!pos.add(o.getPosition())) {
                throw new IllegalArgumentException("Duplicate option position: " + o.getPosition());
            }
            String v = o.getValue() == null ? null : o.getValue().trim();
            if (v == null || v.isEmpty()) {
                throw new IllegalArgumentException("Option value is required");
            }
            if (!values.add(v)) {
                throw new IllegalArgumentException("Duplicate option value: " + v);
            }
        }
    }

    private static String normalizeOptional(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}

