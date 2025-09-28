package com.outsera.goldenraspberry.repository;

import com.outsera.goldenraspberry.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    @Query("SELECT m FROM MovieEntity m WHERE LOWER(m.winner) = 'yes'")
    List<MovieEntity> findWinners();

    @Query("SELECT m FROM MovieEntity m WHERE LOWER(m.winner) != 'yes' OR m.winner IS NULL")
    List<MovieEntity> findNonWinners();

    List<MovieEntity> findByYear(Integer year);

    @Query("SELECT m FROM MovieEntity m WHERE m.year = :year AND LOWER(m.winner) = LOWER(:winner)")
    List<MovieEntity> findByYearAndWinner(@Param("year") Integer year, @Param("winner") String winner);

    @Query("SELECT DISTINCT m.year FROM MovieEntity m ORDER BY m.year")
    List<Integer> findDistinctYears();

}
