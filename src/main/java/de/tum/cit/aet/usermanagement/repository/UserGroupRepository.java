package de.tum.cit.aet.usermanagement.repository;

import de.tum.cit.aet.usermanagement.domain.UserGroup;
import de.tum.cit.aet.usermanagement.domain.key.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId> {

    @Transactional
    @Modifying
    @Query("DELETE FROM UserGroup ug WHERE ug.id.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
