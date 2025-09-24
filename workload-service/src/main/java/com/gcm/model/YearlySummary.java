package com.gcm.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "year_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlySummary {

    @NotNull
    @Min(2000)
    private Integer yearNumber;

    @Field(name = "monthly_summaries")
    private List<MonthlySummary> months;
}