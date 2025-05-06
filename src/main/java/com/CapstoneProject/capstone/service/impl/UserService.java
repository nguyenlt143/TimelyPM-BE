package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.auth.AuthenticateRequest;
import com.CapstoneProject.capstone.dto.request.auth.ChangeForgotPasswordRequest;
import com.CapstoneProject.capstone.dto.request.auth.ChangePasswordRequest;
import com.CapstoneProject.capstone.dto.request.auth.UpdateProfileRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterRequest;
import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterResponse;
import com.CapstoneProject.capstone.enums.GenderEnum;
import com.CapstoneProject.capstone.enums.RoleEnum;
import com.CapstoneProject.capstone.exception.InformationException;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.exception.UserExisted;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.mapper.UserProfileMapper;
import com.CapstoneProject.capstone.model.Role;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.model.UserProfile;
import com.CapstoneProject.capstone.repository.RoleRepository;
import com.CapstoneProject.capstone.repository.UserProfileRepository;
import com.CapstoneProject.capstone.repository.UserRepository;
import com.CapstoneProject.capstone.service.IUserService;
import com.CapstoneProject.capstone.service.impl.otp.OtpService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserProfileMapper userProfileMapper;
    private final AppwriteStorageService appwriteStorageService;
    private final OtpService otpService;
    private final FirebaseAuth firebaseAuth;

    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        String genderStr = request.getUserInfo().getGender();
        Role role = roleRepository.findByName(RoleEnum.USER);
        GenderEnum gender;
        try {
            gender = GenderEnum.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Giới tính không hợp lệ! Chỉ chấp nhận MALE hoặc FEMALE.");
        }

        if (userRepository.findByUsername(request.getUser().getUsername()).isPresent()) {
            throw new UserExisted("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.findByEmail(request.getUser().getEmail()).isPresent()) {
            throw new UserExisted("Email đã tồn tại!");
        }
        User user = userMapper.toModel(request.getUser());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(false);
        user.setRole(role);
        userRepository.save(user);
        otpService.sendOtp(user.getEmail());
        UserProfile userProfile = userMapper.toProfile(request.getUserInfo());
        userProfile.setUser(user);
        userProfile.setGender(request.getUserInfo().getGender());
        userProfileRepository.save(userProfile);
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setUser(userMapper.toResponse(user));
        registerResponse.setUserInfo(userMapper.toResponse(userProfile));

        return registerResponse;
    }

    @Override
    public AuthenticateResponse authenticateUser(AuthenticateRequest request) {
        Authentication authentication;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        var jwtToken = jwtService.generateToken(user);
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("User profile not found"));

        AuthenticateResponse response = new AuthenticateResponse();
        response.setToken(jwtToken);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().getName().name());
        response.setFullName(profile.getFullName());
        return response;
    }

    @Override
    public GetUserResponse getUser() {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found!"));
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Profile not found!"));
        GetUserResponse response = userMapper.getUserResponse(user);
        response.setProfile(userProfileMapper.toProfile(userProfile));
        return response;
    }

    @Override
    public GetUserResponse getUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found!"));
        UserProfile userProfile = userProfileRepository.findByUserId(id).orElseThrow(() -> new NotFoundException("Profile not found!"));
        GetUserResponse response = userMapper.getUserResponse(user);
        response.setProfile(userProfileMapper.toProfile(userProfile));
        return response;
    }

    @Override
    public Boolean deleteUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found!"));
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Override
    public List<GetUserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        List<GetUserResponse> userResponses = users.stream().map(user -> {
            UserProfile userProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("Profile not found!"));
            GetUserResponse response = userMapper.getUserResponse(user);
            response.setProfile(userProfileMapper.toProfile(userProfile));
            return response;
        }).collect(Collectors.toList());
        return userResponses;
    }

    @Override
    public Boolean changePassword(ChangePasswordRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found!"));

        System.out.println(passwordEncoder.encode(request.getOldPassword()));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InformationException("Old password doesn't match!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    @Override
    public Boolean uploadAvatar(MultipartFile file) throws IOException, InterruptedException {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found!"));

        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Profile not found!"));

        userProfile.setAvatarUrl(appwriteStorageService.uploadFileToAppwrite(file));
        userProfile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(userProfile);

        return true;
    }

    @Override
    public GetUserResponse updateProfile(UpdateProfileRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found!"));

        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Profile not found!"));

        GenderEnum gender = null;
        if (request.getGender() != null){
            String genderSt = request.getGender();
            try{
                gender = GenderEnum.valueOf(genderSt.toUpperCase());
            }catch (IllegalArgumentException | NullPointerException e){
                throw new InvalidEnumException("Trạng thái không hợp lệ");
            }
        }

        userProfile.setGender(request.getGender() == null ? userProfile.getGender() : gender.name());
        userProfile.setFullName(request.getFullName() == null ? userProfile.getFullName() : request.getFullName());
        userProfile.setPhone(request.getPhone() == null ? userProfile.getPhone() : request.getPhone());
        userProfile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(userProfile);

        GetUserResponse response = userMapper.getUserResponse(user);
        response.setProfile(userProfileMapper.toProfile(userProfile));
        return response;
    }

    @Transactional
    public AuthenticateResponse handleGoogleLogin(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = registerGoogleUser(email, name, picture);
        }

        return authenticateUser(user);
    }

    @Override
    public AuthenticateResponse loginGoogle(String accessToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(accessToken);
        return processLogin(decodedToken);
    }

    @Override
    public AuthenticateResponse loginFacebook(String accessToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(accessToken);
        return processLogin(decodedToken);
    }

    @Override
    public AuthenticateResponse loginGitHub(String accessToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(accessToken);
        return processLogin(decodedToken);
    }

    private AuthenticateResponse processLogin(FirebaseToken decodedToken) throws FirebaseAuthException {
        Role role = roleRepository.findByName(RoleEnum.USER);
        String email = decodedToken.getEmail();
        String uid = decodedToken.getUid();
        String fallbackEmail = uid + "@github.com";
        Optional<User> existingUser = userRepository.findByEmail(
                email != null ? email : fallbackEmail
        );
        User user;
        UserProfile userProfile = new UserProfile();
        if (existingUser.isPresent()) {
            user = existingUser.orElseThrow(() -> new NotFoundException("Email Not Found"));
        } else {
            String username = email != null ? email.split("@")[0] : "github_user_" + decodedToken.getUid();
            user = new User();
            user.setEmail(email != null ? email : fallbackEmail);
            user.setUsername(username);
            user.setRole(role);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

//            userProfile = new UserProfile();
            userProfile.setUser(user);
            userProfile.setFullName(decodedToken.getName());
            userProfile.setAvatarUrl(decodedToken.getPicture());
            userProfile.setActive(true);
            userProfile.setCreatedAt(LocalDateTime.now());
            userProfile.setUpdatedAt(LocalDateTime.now());
            userProfileRepository.save(userProfile);
        }

        String jwtToken = jwtService.generateToken(user);
        AuthenticateResponse authenticationResponse = new AuthenticateResponse();
        authenticationResponse.setToken(jwtToken);
        authenticationResponse.setRole(user.getRole().getName().name());
        authenticationResponse.setId(user.getId());
        authenticationResponse.setUsername(user.getUsername());
        authenticationResponse.setFullName(userProfile.getFullName());
        return authenticationResponse;
    }

    @Override
    public Boolean verifyAccount(String email, Integer otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email không tồn tại!"));
        boolean isValidOtp = otpService.validateOTP(email, otp);
        if (isValidOtp) {
            user.setActive(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public Boolean resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email không tồn tại!"));
        otpService.resendOtp(user.getEmail());
        return true;
    }

    @Override
    public Boolean forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email không tồn tại!"));
        otpService.sendOtp(user.getEmail());
        return true;
    }

    @Override
    public Boolean changeForgotPassword(ChangeForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Email không tồn tại!"));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    private User registerGoogleUser(String email, String name, String picture) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserExisted("Email đã tồn tại!");
        }

        String username = email.split("@")[0];
        int suffix = 1;
        String baseUsername = username;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix;
            suffix++;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("google-auth-" + email));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        Role role = roleRepository.findByName(RoleEnum.USER);
        user.setRole(role);

        userRepository.save(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setFullName(name);
        userProfile.setAvatarUrl(picture);
        userProfile.setGender(GenderEnum.MALE.name());
        userProfile.setUser(user);

        userProfileRepository.save(userProfile);

        return user;
    }

    private AuthenticateResponse authenticateUser(User user) {
        String jwtToken = jwtService.generateToken(user);

        AuthenticateResponse response = new AuthenticateResponse();
        response.setToken(jwtToken);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().getName().name());
        return response;
    }
}
