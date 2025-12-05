package com.springdemo.educationsystem.Service;


import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final UserService userService;


    public SurveyService(SurveyRepository surveyRepository,
                         SurveyResponseRepository surveyResponseRepository,
                         UserService userService) {
        this.surveyRepository = surveyRepository;
        this.surveyResponseRepository = surveyResponseRepository;
        this.userService = userService;
    }

    // --- Создание опроса админом ---
    public Survey createSurvey(SurveyCreateDTO dto, Long adminUserId) {
        User admin = userService.getUserEntityById(adminUserId);

        Survey survey = new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setForStudents(dto.isForStudents());
        survey.setForTeachers(dto.isForTeachers());
        survey.setCreatedBy(admin);
        survey.setActive(true);

        List<SurveyQuestion> questions = new ArrayList<>();
        int qIndex = 0;
        for (SurveyCreateDTO.QuestionDTO qDto : dto.getQuestions()) {
            SurveyQuestion q = new SurveyQuestion();
            q.setSurvey(survey);
            q.setText(qDto.getText());
            q.setType(SurveyQuestionType.valueOf(qDto.getType()));
            q.setOrderIndex(qIndex++);

            if (q.getType() == SurveyQuestionType.MULTIPLE_CHOICE && qDto.getOptions() != null) {
                List<SurveyOption> options = new ArrayList<>();
                int optIndex = 0;
                for (String oText : qDto.getOptions()) {
                    SurveyOption opt = new SurveyOption();
                    opt.setQuestion(q);
                    opt.setText(oText);
                    opt.setOrderIndex(optIndex++);
                    options.add(opt);
                }
                q.setOptions(options);
            }

            questions.add(q);
        }
        survey.setQuestions(questions);

        return surveyRepository.save(survey);
    }

    // --- Список опросов для админа ---
    public List<SurveyShortDTO> getAllSurveysForAdmin() {
        return surveyRepository.findAll().stream()
                .map(s -> {
                    SurveyShortDTO dto = new SurveyShortDTO();
                    dto.setId(s.getId());
                    dto.setTitle(s.getTitle());
                    dto.setDescription(s.getDescription());
                    dto.setQuestionsCount(s.getQuestions() != null ? s.getQuestions().size() : 0);
                    return dto;
                }).collect(Collectors.toList());
    }

    // --- Доступные опросы для текущего юзера ---
    public List<SurveyShortDTO> getAvailableSurveys(Long userId, String role) {
        List<Survey> surveys;

        if ("student".equalsIgnoreCase(role)) {
            surveys = surveyRepository.findByActiveTrueAndForStudentsTrue();
        } else if ("teacher".equalsIgnoreCase(role)) {
            surveys = surveyRepository.findByActiveTrueAndForTeachersTrue();
        } else {
            return Collections.emptyList();
        }

        Set<Long> answered = surveyResponseRepository.findByUserId(userId).stream()
                .map(r -> r.getSurvey().getId())
                .collect(Collectors.toSet());

        return surveys.stream()
                .filter(s -> !answered.contains(s.getId()))
                .map(s -> {
                    SurveyShortDTO dto = new SurveyShortDTO();
                    dto.setId(s.getId());
                    dto.setTitle(s.getTitle());
                    dto.setDescription(s.getDescription());
                    dto.setQuestionsCount(s.getQuestions() != null ? s.getQuestions().size() : 0);
                    return dto;
                }).collect(Collectors.toList());
    }

    // --- Детали опроса для заполнения ---
    public SurveyDetailsDTO getSurveyDetails(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Опрос не найден"));

        SurveyDetailsDTO dto = new SurveyDetailsDTO();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());

        List<SurveyDetailsDTO.QuestionDTO> questions = survey.getQuestions().stream()
                .sorted(Comparator.comparingInt(q -> Optional.ofNullable(q.getOrderIndex()).orElse(0)))
                .map(q -> {
                    SurveyDetailsDTO.QuestionDTO qDto = new SurveyDetailsDTO.QuestionDTO();
                    qDto.setId(q.getId());
                    qDto.setText(q.getText());
                    qDto.setType(q.getType().name());

                    if (q.getType() == SurveyQuestionType.MULTIPLE_CHOICE) {
                        List<SurveyDetailsDTO.QuestionDTO.OptionDTO> opts = q.getOptions().stream()
                                .sorted(Comparator.comparingInt(o -> Optional.ofNullable(o.getOrderIndex()).orElse(0)))
                                .map(o -> {
                                    SurveyDetailsDTO.QuestionDTO.OptionDTO oDto =
                                            new SurveyDetailsDTO.QuestionDTO.OptionDTO();
                                    oDto.setId(o.getId());
                                    oDto.setText(o.getText());
                                    return oDto;
                                }).toList();
                        qDto.setOptions(opts);
                    }
                    return qDto;
                }).toList();

        dto.setQuestions(questions);
        return dto;
    }

    // --- Сохранение ответа пользователя ---
    public void saveResponse(Long userId, SurveyResponseRequest request) {
        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new RuntimeException("Опрос не найден"));

        if (surveyResponseRepository.existsBySurveyIdAndUserId(survey.getId(), userId)) {
            throw new RuntimeException("Вы уже прошли этот опрос");
        }

        User user = userService.getUserEntityById(userId);

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setUser(user);

        List<SurveyAnswer> answers = new ArrayList<>();

        Map<Long, SurveyQuestion> questionMap = survey.getQuestions().stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        for (SurveyResponseRequest.AnswerDTO aDto : request.getAnswers()) {
            SurveyQuestion q = questionMap.get(aDto.getQuestionId());
            if (q == null) continue;

            SurveyAnswer ans = new SurveyAnswer();
            ans.setResponse(response);
            ans.setQuestion(q);

            if (q.getType() == SurveyQuestionType.MULTIPLE_CHOICE && aDto.getOptionId() != null) {
                SurveyOption opt = q.getOptions().stream()
                        .filter(o -> o.getId().equals(aDto.getOptionId()))
                        .findFirst()
                        .orElse(null);
                ans.setSelectedOption(opt);
            } else if (q.getType() == SurveyQuestionType.TEXT) {
                ans.setTextAnswer(aDto.getTextAnswer());
            }

            answers.add(ans);
        }

        response.setAnswers(answers);
        surveyResponseRepository.save(response);
    }

    // --- Результаты для админа ---
    public SurveyResultsDTO getResults(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Опрос не найден"));

        SurveyResultsDTO dto = new SurveyResultsDTO();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());

        List<SurveyResultsDTO.QuestionResultDTO> questionResults = new ArrayList<>();

        for (SurveyQuestion q : survey.getQuestions()) {
            SurveyResultsDTO.QuestionResultDTO qDto = new SurveyResultsDTO.QuestionResultDTO();
            qDto.setId(q.getId());
            qDto.setText(q.getText());
            qDto.setType(q.getType().name());

            if (q.getType() == SurveyQuestionType.MULTIPLE_CHOICE) {
                Map<Long, Long> counts = survey.getResponses().stream()
                        .flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId())
                                && a.getSelectedOption() != null)
                        .collect(Collectors.groupingBy(a -> a.getSelectedOption().getId(), Collectors.counting()));

                List<SurveyResultsDTO.QuestionResultDTO.OptionResultDTO> opts = q.getOptions().stream()
                        .map(o -> {
                            SurveyResultsDTO.QuestionResultDTO.OptionResultDTO oDto =
                                    new SurveyResultsDTO.QuestionResultDTO.OptionResultDTO();
                            oDto.setId(o.getId());
                            oDto.setText(o.getText());
                            oDto.setCount(counts.getOrDefault(o.getId(), 0L));
                            return oDto;
                        }).toList();

                qDto.setOptions(opts);
            } else {
                List<String> texts = survey.getResponses().stream()
                        .flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId())
                                && a.getTextAnswer() != null && !a.getTextAnswer().isBlank())
                        .map(SurveyAnswer::getTextAnswer)
                        .toList();
                qDto.setTextAnswers(texts);
            }

            questionResults.add(qDto);
        }

        dto.setQuestions(questionResults);
        return dto;
    }
}

