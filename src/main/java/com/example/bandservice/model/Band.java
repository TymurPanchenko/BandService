package com.example.bandservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Table("band")
public class Band {
    @Id
    @PrimaryKeyColumn(
            name = "id",
            ordinal = 0,
            type = PrimaryKeyType.PARTITIONED
    )
    private Long id;
    @Column(value = "name")
    @NotBlank
    @NotNull
    private String name;
}
