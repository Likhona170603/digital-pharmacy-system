package za.ac.cput.digitalpharmacysystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.dto.CustomerNameDto;
import za.ac.cput.digitalpharmacysystem.domain.User;

import java.util.List;
import java.util.Optional;

//author: 221066748 (Noxolo Portia Ngubo)

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("select new za.ac.cput.digitalpharmacysystem.dto.CustomerNameDto(u.id, u.fullName) " +
            "from User u where u.role = :role")
    List<CustomerNameDto> findNamesByRole(@Param("role") User.UserRole role);
}