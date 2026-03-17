package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.QuizAttemptResponseDTO;
import com.springdemo.educationsystem.DTO.SaveAnswerDTO;
import com.springdemo.educationsystem.Entity.QuizAnswer;
import com.springdemo.educationsystem.Entity.QuizAssignment;
import com.springdemo.educationsystem.Entity.QuizAttempt;
import com.springdemo.educationsystem.Entity.QuizOption;
import com.springdemo.educationsystem.Entity.QuizQuestion;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Enum.QuizAttemptStatus;
import com.springdemo.educationsystem.Enum.QuizQuestionType;
import com.springdemo.educationsystem.Repository.QuizAnswerRepository;
import com.springdemo.educationsystem.Repository.QuizAssignmentRepository;
import com.springdemo.educationsystem.Repository.QuizAttemptRepository;
import com.springdemo.educationsystem.Repository.QuizQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final QuizAssignmentRepository assignmentRepository;
    private final QuizAnswerRepository answerRepository;
    private final QuizQuestionRepository questionRepository;

    public QuizAttemptService(
            QuizAttemptRepository attemptRepository,
            QuizAssignmentRepository assignmentRepository,
            QuizAnswerRepository answerRepository,
            QuizQuestionRepository questionRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.assignmentRepository = assignmentRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public QuizAttempt startAttempt(Long assignmentId, Long studentId) {

        QuizAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Проверяем существующую попытку
        Optional<QuizAttempt> existing =
                attemptRepository.findByQuizAssignmentIdAndStudentId(assignmentId, studentId);

        if (existing.isPresent()) {
            if (existing.get().getStatus() == QuizAttemptStatus.SUBMITTED) {
                throw new RuntimeException("Вы уже прошли этот квиз");
            }
            return existing.get(); // возвращаем незавершенную попытку
        }

        // Создаем новую попытку, если нет существующей
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizAssignment(assignment);

        Student student = new Student();
        student.setId(studentId);
        attempt.setStudent(student);

        attempt.setStartTime(LocalDateTime.now());
        attempt.setStatus(QuizAttemptStatus.IN_PROGRESS);

        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAnswer saveAnswer(SaveAnswerDTO dto) {

        QuizAttempt attempt = attemptRepository.findById(dto.getAttemptId())
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        QuizQuestion question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QuizAnswer answer = answerRepository
                .findByAttemptIdAndQuestionId(dto.getAttemptId(), dto.getQuestionId())
                .orElse(new QuizAnswer());

        answer.setAttempt(attempt);
        answer.setQuestion(question);

        answer.setAnswerText(dto.getAnswerText());
        answer.setSelectedOptionIdsJson(dto.getSelectedOptionIdsJson());
        answer.setMatchingJson(dto.getMatchingJson());
        answer.setOrderingJson(dto.getOrderingJson());

        return answerRepository.save(answer);
    }

    public QuizAttemptResponseDTO getAttempt(Long attemptId) {

        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        QuizAssignment assignment = attempt.getQuizAssignment();
        Long remainingSeconds = null;

        if (attempt.getStatus() == QuizAttemptStatus.IN_PROGRESS &&
                assignment.getTimeLimitMinutes() != null) {

            LocalDateTime allowedEnd =
                    attempt.getStartTime().plusMinutes(assignment.getTimeLimitMinutes());

            remainingSeconds = Duration.between(LocalDateTime.now(), allowedEnd).getSeconds();

            if (remainingSeconds <= 0) {
                attempt.setStatus(QuizAttemptStatus.TIME_EXPIRED);
                attempt.setEndTime(LocalDateTime.now());
                attemptRepository.save(attempt);
                remainingSeconds = 0L;
            }
        }

        QuizAttemptResponseDTO dto = new QuizAttemptResponseDTO();
        dto.setAttemptId(attempt.getId());
        dto.setStatus(attempt.getStatus().name());
        dto.setStartTime(attempt.getStartTime());
        dto.setRemainingSeconds(remainingSeconds);

        return dto;
    }

    public List<QuizAttempt> getAttemptsForAssignment(Long assignmentId) {
        return attemptRepository.findByQuizAssignmentId(assignmentId);
    }




    @Transactional
    public QuizAttempt finishAttempt(Long attemptId) {

        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        List<QuizAnswer> answers = answerRepository.findByAttemptId(attemptId);

        int score = 0;

        for (QuizAnswer answer : answers) {
            QuizQuestion question = answer.getQuestion();

            if (question == null || question.getQuestionType() == null) {
                continue;
            }

            if (question.getQuestionType() == QuizQuestionType.TEXT_ANSWER) {
                answer.setIsCorrect(null);
                answer.setPointsAwarded(0);
                continue;
            }

            if (question.getQuestionType() == QuizQuestionType.SINGLE_CHOICE ||
                    question.getQuestionType() == QuizQuestionType.MULTIPLE_CHOICE) {

                Set<Long> selectedIds = parseSelectedIds(answer.getSelectedOptionIdsJson());

                Set<Long> correctIds = question.getOptions().stream()
                        .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                        .map(QuizOption::getId)
                        .collect(Collectors.toSet());

                boolean correct = selectedIds.equals(correctIds);

                answer.setIsCorrect(correct);
                answer.setPointsAwarded(correct ? question.getPoints() : 0);

                if (correct) {
                    score += question.getPoints();
                }
            }
        }

        answerRepository.saveAll(answers);

        attempt.setScore(score);
        attempt.setStatus(QuizAttemptStatus.SUBMITTED);
        attempt.setEndTime(LocalDateTime.now());

        if (attempt.getStartTime() != null && attempt.getEndTime() != null) {
            attempt.setDurationSeconds(
                    Duration.between(attempt.getStartTime(), attempt.getEndTime()).getSeconds()
            );
        }

        return attemptRepository.save(attempt);
    }

    private Set<Long> parseSelectedIds(String json) {
        if (json == null || json.isBlank()) {
            return new HashSet<>();
        }

        String cleaned = json.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");

        if (cleaned.isBlank()) {
            return new HashSet<>();
        }

        Set<Long> result = new HashSet<>();
        String[] parts = cleaned.split(",");

        for (String part : parts) {
            String value = part.trim();
            if (!value.isBlank()) {
                try {
                    result.add(Long.parseLong(value));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return result;
    }
}