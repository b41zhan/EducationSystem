package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.GradeTextQuizAnswersDTO;
import com.springdemo.educationsystem.DTO.TeacherQuizAttemptDetailsDTO;
import com.springdemo.educationsystem.Entity.QuizAnswer;
import com.springdemo.educationsystem.Entity.QuizAttempt;
import com.springdemo.educationsystem.Entity.QuizOption;
import com.springdemo.educationsystem.Entity.QuizQuestion;
import com.springdemo.educationsystem.Entity.Teacher;
import com.springdemo.educationsystem.Enum.QuizQuestionType;
import com.springdemo.educationsystem.Repository.QuizAnswerRepository;
import com.springdemo.educationsystem.Repository.QuizAttemptRepository;
import com.springdemo.educationsystem.Repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherQuizReviewService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final TeacherRepository teacherRepository;

    public TeacherQuizReviewService(
            QuizAttemptRepository quizAttemptRepository,
            QuizAnswerRepository quizAnswerRepository,
            TeacherRepository teacherRepository
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional(readOnly = true)
    public TeacherQuizAttemptDetailsDTO getAttemptDetails(Long attemptId, Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getQuizAssignment() == null ||
                attempt.getQuizAssignment().getTeacher() == null ||
                !attempt.getQuizAssignment().getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("You can view only your own students' attempts");
        }

        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attemptId);
        Map<Long, QuizAnswer> answerMap = answers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getId() != null)
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (a, b) -> a));

        TeacherQuizAttemptDetailsDTO dto = new TeacherQuizAttemptDetailsDTO();
        dto.setAttemptId(attempt.getId());
        dto.setAssignmentId(attempt.getQuizAssignment().getId());
        dto.setQuizId(attempt.getQuizAssignment().getQuiz().getId());
        dto.setQuizTitle(attempt.getQuizAssignment().getQuiz().getTitle());
        dto.setStudentId(attempt.getStudent().getId());
        dto.setStudentName(
                attempt.getStudent().getUser().getFirstName() + " " +
                        attempt.getStudent().getUser().getLastName()
        );
        dto.setScore(attempt.getScore());
        dto.setStatus(attempt.getStatus().name());
        dto.setStartTime(attempt.getStartTime());
        dto.setEndTime(attempt.getEndTime());
        dto.setDurationSeconds(attempt.getDurationSeconds());

        List<QuizQuestion> questions = new ArrayList<>(attempt.getQuizAssignment().getQuiz().getQuestions());
        questions.sort(Comparator.comparing(q -> q.getOrderIndex() == null ? 0 : q.getOrderIndex()));

        List<TeacherQuizAttemptDetailsDTO.QuestionReviewDTO> questionDtos = new ArrayList<>();

        for (QuizQuestion question : questions) {
            QuizAnswer answer = answerMap.get(question.getId());

            TeacherQuizAttemptDetailsDTO.QuestionReviewDTO qDto =
                    new TeacherQuizAttemptDetailsDTO.QuestionReviewDTO();

            qDto.setQuestionId(question.getId());
            qDto.setQuestionText(question.getQuestionText());
            qDto.setQuestionType(question.getQuestionType().name());
            qDto.setPoints(question.getPoints());
            qDto.setManuallyGradable(question.getQuestionType() == QuizQuestionType.TEXT_ANSWER);

            if (answer != null) {
                qDto.setStudentAnswerText(answer.getAnswerText());
                qDto.setStudentSelectedOptionIdsJson(answer.getSelectedOptionIdsJson());
                qDto.setIsCorrect(answer.getIsCorrect());
                qDto.setPointsAwarded(answer.getPointsAwarded());
            } else {
                qDto.setStudentAnswerText(null);
                qDto.setStudentSelectedOptionIdsJson(null);
                qDto.setIsCorrect(null);
                qDto.setPointsAwarded(0);
            }

            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                List<TeacherQuizAttemptDetailsDTO.OptionDTO> optionDtos = question.getOptions()
                        .stream()
                        .sorted(Comparator.comparing(o -> o.getOrderIndex() == null ? 0 : o.getOrderIndex()))
                        .map(o -> new TeacherQuizAttemptDetailsDTO.OptionDTO(
                                o.getId(),
                                o.getOptionText(),
                                Boolean.TRUE.equals(o.getIsCorrect())
                        ))
                        .toList();

                qDto.setOptions(optionDtos);
            }

            questionDtos.add(qDto);
        }

        dto.setQuestions(questionDtos);
        return dto;
    }

    @Transactional
    public TeacherQuizAttemptDetailsDTO gradeTextAnswers(GradeTextQuizAnswersDTO request, Long teacherUserId) {
        if (request == null || request.getAttemptId() == null) {
            throw new RuntimeException("attemptId is required");
        }

        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        QuizAttempt attempt = quizAttemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getQuizAssignment() == null ||
                attempt.getQuizAssignment().getTeacher() == null ||
                !attempt.getQuizAssignment().getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("You can grade only your own students' attempts");
        }

        if (request.getGrades() != null) {
            for (GradeTextQuizAnswersDTO.TextQuestionGradeDTO grade : request.getGrades()) {
                if (grade.getQuestionId() == null) {
                    continue;
                }

                QuizAnswer answer = quizAnswerRepository
                        .findByAttemptIdAndQuestionId(request.getAttemptId(), grade.getQuestionId())
                        .orElseThrow(() -> new RuntimeException(
                                "Answer not found for questionId=" + grade.getQuestionId()
                        ));

                if (answer.getQuestion() == null ||
                        answer.getQuestion().getQuestionType() != QuizQuestionType.TEXT_ANSWER) {
                    throw new RuntimeException("Only TEXT_ANSWER can be graded manually");
                }

                int maxPoints = answer.getQuestion().getPoints() != null ? answer.getQuestion().getPoints() : 0;
                int awarded = grade.getPointsAwarded() != null ? grade.getPointsAwarded() : 0;

                if (awarded < 0) {
                    awarded = 0;
                }
                if (awarded > maxPoints) {
                    awarded = maxPoints;
                }

                answer.setPointsAwarded(awarded);

                if (awarded == 0) {
                    answer.setIsCorrect(false);
                } else if (awarded == maxPoints) {
                    answer.setIsCorrect(true);
                } else {
                    answer.setIsCorrect(null);
                }

                quizAnswerRepository.save(answer);
            }
        }

        List<QuizAnswer> allAnswers = quizAnswerRepository.findByAttemptId(request.getAttemptId());
        int totalScore = allAnswers.stream()
                .map(QuizAnswer::getPointsAwarded)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        attempt.setScore(totalScore);
        quizAttemptRepository.save(attempt);

        return getAttemptDetails(request.getAttemptId(), teacherUserId);
    }
}