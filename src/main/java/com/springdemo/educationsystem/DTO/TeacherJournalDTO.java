package com.springdemo.educationsystem.DTO;

import java.util.ArrayList;
import java.util.List;

public class TeacherJournalDTO {

    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private Integer quarter;
    private List<String> dates = new ArrayList<>();
    private List<StudentRowDTO> students = new ArrayList<>();

    public static class StudentRowDTO {
        private Long studentId;
        private String studentName;
        private List<DayCellDTO> cells = new ArrayList<>();
        private FinalDTO finalGrade;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public List<DayCellDTO> getCells() { return cells; }
        public void setCells(List<DayCellDTO> cells) { this.cells = cells; }

        public FinalDTO getFinalGrade() { return finalGrade; }
        public void setFinalGrade(FinalDTO finalGrade) { this.finalGrade = finalGrade; }
    }

    public static class DayCellDTO {
        private String date;
        private String attendanceCode;
        private String attendanceColor;
        private List<EntryDTO> entries = new ArrayList<>();

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getAttendanceCode() { return attendanceCode; }
        public void setAttendanceCode(String attendanceCode) { this.attendanceCode = attendanceCode; }

        public String getAttendanceColor() { return attendanceColor; }
        public void setAttendanceColor(String attendanceColor) { this.attendanceColor = attendanceColor; }

        public List<EntryDTO> getEntries() { return entries; }
        public void setEntries(List<EntryDTO> entries) { this.entries = entries; }
    }

    public static class EntryDTO {
        private Long id;
        private String type;
        private String label;
        private String displayValue;
        private Double numericValue;
        private Long sourceId;
        private String sourceType;
        private boolean editable;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getDisplayValue() { return displayValue; }
        public void setDisplayValue(String displayValue) { this.displayValue = displayValue; }

        public Double getNumericValue() { return numericValue; }
        public void setNumericValue(Double numericValue) { this.numericValue = numericValue; }

        public Long getSourceId() { return sourceId; }
        public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }

        public boolean isEditable() { return editable; }
        public void setEditable(boolean editable) { this.editable = editable; }
    }

    public static class FinalDTO {
        private Double quarterGrade;
        private Double calculatedQuarterGrade;
        private Double yearGrade;
        private Double calculatedYearGrade;
        private boolean quarterManual;
        private boolean yearManual;

        public Double getQuarterGrade() { return quarterGrade; }
        public void setQuarterGrade(Double quarterGrade) { this.quarterGrade = quarterGrade; }

        public Double getCalculatedQuarterGrade() { return calculatedQuarterGrade; }
        public void setCalculatedQuarterGrade(Double calculatedQuarterGrade) { this.calculatedQuarterGrade = calculatedQuarterGrade; }

        public Double getYearGrade() { return yearGrade; }
        public void setYearGrade(Double yearGrade) { this.yearGrade = yearGrade; }

        public Double getCalculatedYearGrade() { return calculatedYearGrade; }
        public void setCalculatedYearGrade(Double calculatedYearGrade) { this.calculatedYearGrade = calculatedYearGrade; }

        public boolean isQuarterManual() { return quarterManual; }
        public void setQuarterManual(boolean quarterManual) { this.quarterManual = quarterManual; }

        public boolean isYearManual() { return yearManual; }
        public void setYearManual(boolean yearManual) { this.yearManual = yearManual; }
    }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public List<StudentRowDTO> getStudents() { return students; }
    public void setStudents(List<StudentRowDTO> students) { this.students = students; }
}