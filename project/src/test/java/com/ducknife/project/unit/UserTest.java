package com.ducknife.project.unit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.OrderRepository;
import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.order.mapper.OrderMapper;
import com.ducknife.project.modules.role.RoleRepository;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;
import com.ducknife.project.modules.user.UserService;
import com.ducknife.project.modules.user.dto.UserResponse;
import com.ducknife.project.modules.user.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
public class UserTest {
    
    @Mock 
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock 
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private Order order;
    private Pageable pageable;
    private UserResponse userResponse;
    private OrderResponse orderResponse;

    @BeforeEach
    public void setUp() {
        user = User.builder().id(1L).username("Ducknife").build();
        order = Order.builder().id(1L).build();
        userResponse = UserResponse.builder().userId(1L).username("Ducknife").build();
        orderResponse = OrderResponse.builder().id(1L).build();
        pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "fullname");
    }

    @Test
    @DisplayName("Lấy danh sách users")
    public void layDanhSachUsers() {
        List<User> users = List.of(user);
        Page<User> pages = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findByNameLength(pageable)).thenReturn(pages);

        Page<UserResponse> result = userService.getUsers(pageable);
        
        assertTrue(result.getSize() == 10); // kích thước 1 trang có thể có
        assertTrue(result.getContent().size() == 1);

        verify(userRepository, times(1)).findByNameLength(pageable);
    }
}
