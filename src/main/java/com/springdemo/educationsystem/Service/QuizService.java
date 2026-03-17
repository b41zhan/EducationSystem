package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.CreateOptionDTO;
import com.springdemo.educationsystem.DTO.CreateQuestionDTO;
import com.springdemo.educationsystem.DTO.CreateQuizDTO;
import com.springdemo.educationsystem.Entity.Quiz;
import com.springdemo.educationsystem.Entity.QuizOption;
import com.springdemo.educationsystem.Entity.QuizQuestion;
import com.springdemo.educationsystem.Entity.Subject;
import com.springdemo.educationsystem.Entity.Teacher;
import com.springdemo.educationsystem.Repository.QuizOptionRepository;
import com.springdemo.educationsystem.Repository.QuizQuestionRepository;
import com.springdemo.educationsystem.Repository.QuizRepository;
import com.springdemo.educationsystem.Repository.SubjectRepository;
import com.springdemo.educationsystem.Repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public QuizService(
            QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            QuizOptionRepository optionRepository,
            SubjectRepository subjectRepository,
            TeacherRepository teacherRepository
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional
    public Quiz createQuiz(CreateQuizDTO dto, Long teacherId) {

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTeacher(teacher);
        quiz.setActive(true);

        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found with id: " + dto.getSubjectId()));
            quiz.setSubject(subject);
        }

        return quizRepository.save(quiz);
    }

    @Transactional
    public QuizQuestion addQuestion(Long quizId, CreateQuestionDTO dto, Long teacherId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        if (quiz.getTeacher() == null || !quiz.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You can edit only your own quizzes");
        }

        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setPoints(dto.getPoints() != null ? dto.getPoints() : 1);
        question.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0);
        question.setRequired(true);

        if (dto.getQuestionType() != null
                && dto.getQuestionType().name().equals("TEXT_ANSWER")
                && dto.getOptions() != null
                && !dto.getOptions().isEmpty()
                && dto.getOptions().get(0).getOptionText() != null) {
            question.setCorrectTextAnswer(dto.getOptions().get(0).getOptionText());
        }

        QuizQuestion savedQuestion = questionRepository.save(question);

        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (CreateOptionDTO optionDTO : dto.getOptions()) {
                QuizOption option = new QuizOption();
                option.setQuestion(savedQuestion);
                option.setOptionText(optionDTO.getOptionText());
                option.setIsCorrect(optionDTO.getIsCorrect() != null ? optionDTO.getIsCorrect() : false);
                option.setOrderIndex(optionDTO.getOrderIndex() != null ? optionDTO.getOrderIndex() : 0);
                optionRepository.save(option);
            }
        }

        return savedQuestion;
    }

    public List<Quiz> getTeacherQuizzes(Long teacherId) {
        return quizRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }
}