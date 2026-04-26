package org.example.polify.survey;

import java.util.List;
import org.example.polify.survey.dto.SurveyDetailsResponse;
import org.example.polify.survey.dto.SurveyListItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public List<SurveyListItem> list() {
        return surveyService.listSurveys();
    }

    @GetMapping("/{id}")
    public SurveyDetailsResponse get(@PathVariable long id) {
        return surveyService.getSurvey(id);
    }
}

