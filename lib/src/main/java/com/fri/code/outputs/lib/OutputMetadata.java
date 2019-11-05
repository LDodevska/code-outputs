package com.fri.code.outputs.lib;

public class OutputMetadata {
    private Integer ID;
    private String userOutput;
    private String correctOutput;
    private Boolean isHidden;
    private Integer inputID;

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getUserOutput() {
        return userOutput;
    }

    public void setUserOutput(String userOutput) {
        this.userOutput = userOutput;
    }

    public String getCorrectOutput() {
        return correctOutput;
    }

    public void setCorrectOutput(String correctOutput) {
        this.correctOutput = correctOutput;
    }

    public Boolean getHidden() {
        return isHidden;
    }

    public void setHidden(Boolean hidden) {
        isHidden = hidden;
    }

    public Integer getInputID() {
        return inputID;
    }

    public void setInputID(Integer inputID) {
        this.inputID = inputID;
    }
}
