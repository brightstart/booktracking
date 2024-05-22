package com.tracking.booktracking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("author_by_id")
public class Author {
    @Id @PrimaryKeyColumn(name = "ar_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String id;
    @Column("ar_name")
    @CassandraType(type = Name.TEXT)
    private String name;
    @Column("ar_personal_name")
    @CassandraType(type = Name.TEXT)
    private String personalName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }
}
