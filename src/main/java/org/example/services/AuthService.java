package org.example.services;

import org.example.dto.RegisterRequest;
import org.example.models.Customer;
import org.example.models.User;
import org.example.models.UserRole;
import org.example.repositories.UserRepository;
import org.example.repositories.CustomerRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void register(RegisterRequest request) {

        if(userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("An user with this email already exists");
        }

        User user = new User();

        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(UserRole.CUSTOMER);

        User saved = userRepository.save(user);

        Customer customer = new Customer();

        customer.setUser(saved);
        customer.setCompanyName(request.companyName());
        customer.setPhone(request.phone());

        customerRepository.save(customer);
    }

}