package com.springdemo.educationsystem.DTO;

import java.util.ArrayList;
import java.util.List;

public class StudentJournalSubjectDTO {

    private Long subjectId;
    private String subjectName;
    private List<String> dates = new ArrayList<>();
    private List<DayCellDTO> cells = new ArrayList<>();
    private FinalDTO finalGrade;

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
        private String type;
        private String label;
        private String displayValue;
        private Double numericValue;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getDisplayValue() { return displayValue; }
        public void setDisplayValue(String displayValue) { this.displayValue = displayValue; }

        public Double getNumericValue() { return numericValue; }
        public void setNumericValue(Double numericValue) { this.numericValue = numericValue; }
    }

    public static class FinalDTO {
        private Double quarterGrade;
        private Double calculatedQuarterGrade;
        private Double yearGrade;
        private Double calculatedYearGrade;

        public Double getQuarterGrade() { return quarterGrade; }
        public void setQuarterGrade(Double quarterGrade) { this.quarterGrade = quarterGrade; }

        public Double getCalculatedQuarterGrade() { return calculatedQuarterGrade; }
        public void setCalculatedQuarterGrade(Double calculatedQuarterGrade) { this.calculatedQuarterGrade = calculatedQuarterGrade; }

        public Double getYearGrade() { return yearGrade; }
        public void setYearGrade(Double yearGrade) { this.yearGrade = yearGrade; }

        public Double getCalculatedYearGrade() { return calculatedYearGrade; }
        public void setCalculatedYearGrade(Double calculatedYearGrade) { this.calculatedYearGrade = calculatedYearGrade; }
    }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public List<DayCellDTO> getCells() { return cells; }
    public void setCells(List<DayCellDTO> cells) { this.cells = cells; }

    public FinalDTO getFinalGrade() { return finalGrade; }
    public void setFinalGrade(FinalDTO finalGrade) { this.finalGrade = finalGrade; }
}