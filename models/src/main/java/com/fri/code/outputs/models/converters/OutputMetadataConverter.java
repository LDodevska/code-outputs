package com.fri.code.outputs.models.converters;

import com.fri.code.outputs.lib.OutputMetadata;
import com.fri.code.outputs.models.entities.OutputMetadataEntity;

public class OutputMetadataConverter {

    public static OutputMetadata toDTO(OutputMetadataEntity entity){
        OutputMetadata outputMetadata = new OutputMetadata();
        outputMetadata.setID(entity.getID());
        outputMetadata.setCorrectOutput(entity.getCorrectOutput());
        outputMetadata.setUserOutput(entity.getUserOutput());
        outputMetadata.setHidden(entity.getIsHidden());
        outputMetadata.setInputID(entity.getInputID());

        return outputMetadata;
    }

    public static OutputMetadataEntity toEntity(OutputMetadata outputMetadata){
        OutputMetadataEntity outputMetadataEntity = new OutputMetadataEntity();
        outputMetadataEntity.setID(outputMetadata.getID());
        outputMetadataEntity.setCorrectOutput(outputMetadata.getCorrectOutput());
        outputMetadataEntity.setUserOutput(outputMetadata.getUserOutput());
        outputMetadataEntity.setIsHidden(outputMetadata.getHidden());
        outputMetadataEntity.setInputID(outputMetadata.getInputID());

        return outputMetadataEntity;
    }

}
