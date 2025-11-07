package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.TagDTO;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Entity.Tag;
import com.springdemo.educationsystem.Repository.StudentRepository;
import com.springdemo.educationsystem.Repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final StudentRepository studentRepository;

    // Предопределенные теги
    private static final String[][] PREDEFINED_TAGS = {
            {"Олимпиадник", "Участвует в олимпиадах и конкурсах"},
            {"Юный программист", "Увлекается программированием и IT"},
            {"Начинающий ученый", "Интересуется наукой и исследованиями"},
            {"Творческая личность", "Проявляет творческие способности"},
            {"Публичный оратор", "Умеет выступать перед аудиторией"},
            {"Лидер", "Проявляет лидерские качества"},
            {"Волонтер", "Участвует в волонтерской деятельности"},
            {"Технарь", "Интересуется техникой и технологиями"},
            {"Спортсмен", "Активно занимается спортом"},
            {"Журналист/Блогер", "Пишет статьи или ведет блог"},
            {"Полиглот", "Изучает несколько языков"},
            {"Ментор", "Помогает другим в учебе"},
            {"Ораторское искусство", "Владеет искусством публичных выступлений"},
            {"Тимбилдер", "Умеет работать в команде"},
            {"Критический мыслитель", "Обладает критическим мышлением"},
            {"Цифровой художник", "Занимается цифровым искусством"},
            {"Музыкант", "Играет на музыкальных инструментах"},
            {"Организатор", "Умеет организовывать мероприятия"},
            {"Предприниматель", "Проявляет предпринимательские навыки"},
            {"Эрудит", "Обладает широкими знаниями"}
    };

    public TagService(TagRepository tagRepository, StudentRepository studentRepository) {
        this.tagRepository = tagRepository;
        this.studentRepository = studentRepository;
        this.initializePredefinedTags();
    }

    private void initializePredefinedTags() {
        for (String[] tagData : PREDEFINED_TAGS) {
            if (!tagRepository.findByName(tagData[0]).isPresent()) {
                Tag tag = new Tag(tagData[0], tagData[1]);
                tagRepository.save(tag);
            }
        }
    }

    public List<TagDTO> getAllTags() {
        return tagRepository.findAllOrderedByName()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TagDTO> getStudentTags(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return student.getTags()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TagDTO> getAvailableTagsForStudent(Long studentId) {
        List<Tag> allTags = tagRepository.findAllOrderedByName();
        List<Tag> studentTags = getStudentTags(studentId)
                .stream()
                .map(dto -> tagRepository.findById(dto.getId()).orElse(null))
                .collect(Collectors.toList());

        return allTags.stream()
                .map(tag -> {
                    TagDTO dto = convertToDTO(tag);
                    dto.setSelected(studentTags.contains(tag));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void updateStudentTags(Long studentId, List<Long> tagIds) {
        if (tagIds.size() > 3) {
            throw new RuntimeException("Можно выбрать не более 3 тегов");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Tag> selectedTags = tagRepository.findByIdIn(tagIds);
        student.setTags(selectedTags);
        studentRepository.save(student);
    }

    private TagDTO convertToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setDescription(tag.getDescription());
        return dto;
    }
}