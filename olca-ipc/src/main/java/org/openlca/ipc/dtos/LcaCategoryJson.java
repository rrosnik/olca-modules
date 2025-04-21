package org.openlca.ipc.dtos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LcaCategoryJson {
    public Long id;
    public String refId;
    public String name;
    public String description;
    public Long category;
    public List<Long> categories;
    public ModelType modelType;


//    public Category toCategory() {
//        Category result = new Category();
//        result.id = id;
//        result.refId = refId;
//        result.name = name;
//        result.description = description;
//        result.modelType = modelType;
//        return result;
//    }


    public static LcaCategoryJson from(Category category) {
        LcaCategoryJson dto = new LcaCategoryJson();
        dto.id = category.id;
        dto.refId = category.refId;
        dto.name = category.name;
        dto.description = category.description;
        if (category.category != null) dto.category = category.category.id;
        dto.categories = category.childCategories.stream().map(c -> c.id).toList();
        dto.modelType = category.modelType;
        return dto;
    }
}
