package com.ducknife.project.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.OrderRepository;
import com.ducknife.project.modules.order.OrderService;
import com.ducknife.project.modules.order.action.OrderActionFactory;
import com.ducknife.project.modules.order.discount.DiscountCalculator;
import com.ducknife.project.modules.order.dto.OrderRequest;
import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.order.mapper.OrderMapper;
import com.ducknife.project.modules.order.shipping.ShippingFeeCalculator;
import com.ducknife.project.modules.orderdetail.OrderDetail;
import com.ducknife.project.modules.orderdetail.dto.OrderDetailRequest;
import com.ducknife.project.modules.orderdetail.mapper.OrderDetailMapper;
import com.ducknife.project.modules.product.Product;
import com.ducknife.project.modules.product.ProductRepository;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AddOrderTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private DiscountCalculator discountCalculator;
    @Mock
    private ShippingFeeCalculator shippingFeeCalculator;
    @Mock
    private OrderDetailMapper orderDetailMapper;
    @Mock
    private OrderActionFactory orderActionFactory;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private Product product;
    private OrderDetail orderDetail;
    private User user;
    private OrderRequest request;

    @BeforeEach
    public void setUp() {
        order = Order.builder().id(1L).build();
        product = Product.builder().id(1L).name("Spring").build();
        orderDetail = OrderDetail.builder()
                .id(1L)
                .price(new BigDecimal("120000"))
                .quantity(10L)
                .build();
        user = User.builder().username("hung").id(1L).build();
        request = OrderRequest.builder()
                .userId(1L)
                .orderDetails(List.of(
                        OrderDetailRequest.builder()
                                .productId(1L)
                                .quantity(10L)
                                .build()))
                .build();
    }

    @Test
    @DisplayName("Thêm đơn hàng")
    public void themDonHang() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderDetailMapper.toEntity(any(), any(), any())).thenReturn(orderDetail);
        when(shippingFeeCalculator.feeFor(any(), anyInt())).thenReturn(BigDecimal.ZERO);
        when(discountCalculator.rateFor(any(), any())).thenReturn(new BigDecimal("0.15"));

        orderService.add(request);

        // Dùng để bắt đối số truyền vào, ở đây đang bắt Order được truyền vào hàm
        // save();
        // captor là cái để có thể đọc được đối số đó
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(orderRepository, times(1)).save(orderCaptor.capture());
        verify(orderDetailMapper).toEntity(any(), productCaptor.capture(), any());
        verify(discountCalculator).rateFor(userCaptor.capture(), any());

        BigDecimal actual = orderCaptor.getValue().getInvoice().getTotalPrice();
        String productName = productCaptor.getValue().getName();
        String username = userCaptor.getValue().getUsername();

        assertEquals(username, "hung", () -> "Tên khác mong đợi");
        assertEquals(productName, "Spring", () -> "Tên khác mong đợi");
        assertEquals(0, new BigDecimal("1020000").compareTo(actual),
                () -> "Mong đợi 1020000 nhưng nhận được " + actual);
    }
}
