package com.springdemo.educationsystem.DTO;

public class CreateOptionDTO {

    private String optionText;
    private Boolean isCorrect;
    private Integer orderIndex;

    public CreateOptionDTO() {}

    public String getOptionText() {
        return optionText;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}