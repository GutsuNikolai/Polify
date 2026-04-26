package org.example.polify.survey;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.example.polify.survey.dto.SurveyDetailsResponse;
import org.example.polify.survey.dto.SurveyListItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService {
    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Transactional(readOnly = true)
    public List<SurveyListItem> listSurveys() {
        return surveyRepository.findAll().stream()
            .sorted(Comparator.comparing(SurveyEntity::getId))
            .map(s -> new SurveyListItem(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getRewardAmountBani(),
                s.getTargetCompletions()
            ))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurveyDetailsResponse getSurvey(long id) {
        SurveyEntity survey = surveyRepository.findByIdWithQuestionsAndOptions(id)
            .orElseThrow(() -> new SurveyNotFoundException(id));

        List<SurveyDetailsResponse.QuestionDto> questions = survey.getQuestions().stream()
            .sorted(Comparator.comparingInt(QuestionEntity::getPosition))
            .map(q -> {
                List<SurveyDetailsResponse.OptionDto> options = q.getOptions().stream()
                    .filter(QuestionOptionEntity::isActive)
                    .sorted(Comparator.comparingInt(QuestionOptionEntity::getPosition))
                    .map(o -> new SurveyDetailsResponse.OptionDto(
                        o.getId(),
                        o.getLabel(),
                        o.getValue(),
                        o.getPosition(),
                        o.getMediaUrl()
                    ))
                    .collect(Collectors.toList());

                return new SurveyDetailsResponse.QuestionDto(
                    q.getId(),
                    q.getType().name(),
                    q.getText(),
                    q.getPosition(),
                    q.isRequired(),
                    options
                );
            })
            .collect(Collectors.toList());

        return new SurveyDetailsResponse(
            survey.getId(),
            survey.getTitle(),
            survey.getDescription(),
            survey.getRewardAmountBani(),
            survey.getTargetCompletions(),
            questions
        );
    }
}

