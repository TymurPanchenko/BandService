package com.example.bandservice.repository;

import com.example.bandservice.model.Band;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BandRepository extends CassandraRepository<Band,Integer> {
    @Query("select * from band where name=:name allow filtering")
    Band findByName(@Param("name") String name);
    @Query("select * from band")
    List<Band> findAllBands();
    @Query("select * from band where id=:id allow filtering")
    Band getBandById(@Param("id")int id);
    @Query("update band set name=:name where id=:id")
    Band update(@Param("id")int id, @Param("name")String name);
}
