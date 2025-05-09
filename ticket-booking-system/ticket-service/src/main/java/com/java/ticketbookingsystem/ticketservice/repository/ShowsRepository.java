package com.java.ticketbookingsystem.ticketservice.repository;

import com.java.ticketbookingsystem.ticketservice.model.ShowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowsRepository extends JpaRepository<ShowRequest, String> {

}
