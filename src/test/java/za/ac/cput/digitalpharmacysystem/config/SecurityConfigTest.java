package za.ac.cput.digitalpharmacysystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.controller.UserController;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;
import za.ac.cput.digitalpharmacysystem.service.IUserService;
import za.ac.cput.digitalpharmacysystem.service.JwtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotCreateStaffAccounts() throws Exception {
        mockMvc.perform(post("/digitalpharmacysystem/user/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Courier","email":"courier@example.com","password":"StrongPass123!","role":"COURIER"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administratorCanCreateStaffAccounts() throws Exception {
        when(userService.createStaffUser(any())).thenReturn(new User.Builder()
                .setId(2L)
                .setFullName("Courier")
                .setEmail("courier@example.com")
                .setPasswordHash("encoded")
                .setRole(User.UserRole.COURIER)
                .build());

        mockMvc.perform(post("/digitalpharmacysystem/user/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Courier","email":"courier@example.com","password":"StrongPass123!","role":"COURIER"}
                                """))
                .andExpect(status().isCreated());

        verify(userService).createStaffUser(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotListAllUsers() throws Exception {
        mockMvc.perform(get("/digitalpharmacysystem/user/getAll"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}
