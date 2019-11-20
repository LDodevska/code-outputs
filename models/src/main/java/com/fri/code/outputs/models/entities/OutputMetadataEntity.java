package com.fri.code.outputs.models.entities;

import javax.persistence.*;

@Entity
@Table(name = "outputs")
@NamedQueries(
        value = {
                @NamedQuery(name = "OutputMetadataEntity.getAll", query = "SELECT output FROM OutputMetadataEntity output"),
                @NamedQuery(name = "OutputMetadataEntity.getOutputsForInput", query = "SELECT output FROM OutputMetadataEntity output WHERE output.inputID = ?1"),
                @NamedQuery(name = "OutputMetadataEntity.getOutputById", query = "SELECT output FROM OutputMetadataEntity output WHERE output.ID = ?1")
        }
)
public class OutputMetadataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @Column(name = "userOutput")
    private String userOutput;

    @Column(name = "correctOutput")
    private String correctOutput;

    @Column(name = "isHidden")
    private Boolean isHidden;

    @Column(name = "inputID", unique = true)
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

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public Integer getInputID() {
        return inputID;
    }

    public void setInputID(Integer inputID) {
        this.inputID = inputID;
    }
}
