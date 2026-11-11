package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.dto.CustomerNameDto;
import za.ac.cput.digitalpharmacysystem.dto.CreateStaffUserRequest;
import za.ac.cput.digitalpharmacysystem.domain.User;
import java.util.List;

//author: 221066748 (Noxolo Portia Ngubo)

public interface IUserService extends IService<User, Long> {
    List<User> getAll();
    List<CustomerNameDto> getCustomerNames();
    User createStaffUser(CreateStaffUserRequest request);
    User setActive(Long id, boolean active);
}
