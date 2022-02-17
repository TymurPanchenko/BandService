package com.example.bandservice.repository;

import com.example.bandservice.model.Band;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BandRepository extends CassandraRepository<Band, Long> {
    @Query("select * from band where name=:name allow filtering")
    public Band findByName(@Param("name") String name);
    @Query("select * from band")
    public List<Band> findAllBands();
    @Query("update band set name=:name where id=:id")
    public Band update(@Param("id")Long id, @Param("name")String name);
}
