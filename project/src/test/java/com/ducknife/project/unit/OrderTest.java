package com.ducknife.project.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.invoice.Invoice;
import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.order.OrderRepository;
import com.ducknife.project.modules.order.OrderService;
import com.ducknife.project.modules.order.action.OrderActionFactory;
import com.ducknife.project.modules.order.discount.DiscountCalculator;
import com.ducknife.project.modules.order.dto.OrderResponse;
import com.ducknife.project.modules.order.mapper.OrderMapper;
import com.ducknife.project.modules.order.shipping.ShippingFeeCalculator;
import com.ducknife.project.modules.orderdetail.OrderDetail;
import com.ducknife.project.modules.orderdetail.mapper.OrderDetailMapper;
import com.ducknife.project.modules.product.ProductRepository;
import com.ducknife.project.modules.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class OrderTest {

    @Mock
    private OrderRepository orderRepository; // tạo repo giả, phải mock hết dependency thì mới chạy đc
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

    // Dữ liệu chung
    private Order order;
    private OrderResponse response;

    @InjectMocks
    private OrderService orderService; // tạo service thật, nhét cái orderRepository giả vào

    // BeforeEach: chuẩn bị dữ liệu chung tránh trùng
    @BeforeEach
    public void setUp() {
        order = Order.builder().id(1L).build();
        response = OrderResponse.builder().id(1L).build();
    }

    @Test
    @DisplayName("Tính tiền đơn hàng")
    public void tinhTienDonHang() {
        long totalPrice = 1000L;
        // assertEquals (Expected, Actual, "Message khi fail")
        assertEquals(1000L, totalPrice, "Total Price không đúng giá");
    }

    @Test
    @DisplayName("Lấy danh sách đơn hàng")
    public void layDanhSachDonHang() {

        // Ra lệnh cho Mock
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        List<OrderResponse> results = orderService.getOrders();
        Invoice invoice = order.getInvoice();

        // kiểm tra kết quả
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());

        assertTrue(results.size() == 1); // Kiểm tra xem có đúng là true hay không
        assertFalse(results.size() > 10); // Kiểm tra xem có đúng là false hay không

        assertNull(invoice); // PASS
        assertNotNull(results); // PASS

        // assertThrows(ResourceNotFoundException.class, () -> {
        // orderService.getOrderById(999L);
        // }); // kiểm tra xem có đúng là ném ra exception này không, nếu ko ném/lỗi
        // khác ->
        // // FAIL
    }

    @Test
    @DisplayName("Đếm số đơn hàng")
    public void demSoDonHang() {

        when(orderRepository.count()).thenReturn(5L);

        Long count = orderService.countOrders();

        assertEquals(5L, count, "Không đúng số đơn hàng");
    }

    @Nested
    @DisplayName("Kiểm tra đơn hàng")
    class kiemTraDonHangTest {
        @Test
        @DisplayName("Kiểm tra đơn hàng tồn tại")
        public void kiemTraDonHangTonTai() {

            when(orderRepository.existsById(1L)).thenReturn(true);

            boolean isExisted = orderService.orderExistedById(1L);

            assertTrue(isExisted);
        }

        @Test
        @DisplayName("Kiểm tra đơn hàng không tồn tại")
        public void kiemTraDonHangKhongTonTai() {

            when(orderRepository.existsById(999L)).thenReturn(false);

            boolean isExisted = orderService.orderExistedById(999L);

            assertFalse(isExisted);
        }
    }

    @Test
    @DisplayName("Tìm đơn hàng theo ID")
    public void timDonHangTheoId() {

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getOrderById(1L);
    
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper, times(1)).toResponse(orderCaptor.capture());

        assertEquals(1L, orderCaptor.getValue().getId());
        assertEquals(1L, result.getId());

        // verify: xác nhận mock được gọi
        verify(orderRepository, times(1)).findById(1L);
        verify(orderMapper, times(1)).toResponse(order);
    }

    @Test
    @DisplayName("Ném lỗi không thấy đơn hàng có ID này")
    public void nemLoiKhongThayDonHang() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });

        verify(orderRepository, times(1)).findById(999L);
        verify(orderMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Ném lỗi không có đơn hàng")
    public void nemLoiKhongCoDonHang() {

        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderResponse> results = orderService.getOrders();

        assertTrue(results.isEmpty());
    }
}
