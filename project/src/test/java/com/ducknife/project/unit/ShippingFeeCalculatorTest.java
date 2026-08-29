package com.ducknife.project.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ducknife.project.modules.order.shipping.DefaultOrderFee;
import com.ducknife.project.modules.order.shipping.FreeShippingForLargeOrder;
import com.ducknife.project.modules.order.shipping.ShippingFeeCalculator;
import com.ducknife.project.modules.order.shipping.ShippingStrategy;
import com.ducknife.project.modules.order.shipping.BulkyOrderFee;

/**
 * Test thuan - khong can Spring, khong can DB.
 * Tu dung List<ShippingStrategy> giong het cach Spring inject.
 */
class ShippingFeeCalculatorTest {

    private final List<ShippingStrategy> strategies = List.of(
            new DefaultOrderFee(),
            new FreeShippingForLargeOrder(),
            new BulkyOrderFee());

    private final ShippingFeeCalculator calculator = new ShippingFeeCalculator(strategies);

    private void assertFee(String expected, String subtotal, int qty) {
        BigDecimal actual = calculator.feeFor(new BigDecimal(subtotal), qty);
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "subtotal=" + subtotal + ", qty=" + qty
                        + " -> mong doi " + expected + " nhung nhan duoc " + actual);
    }

    @Test
    @DisplayName("Don nho, it hang -> phi mac dinh 30.000")
    void donNho_phiMacDinh() {
        assertFee("30000", "100000", 2);
    }

    @Test
    @DisplayName("Don tu 500k -> mien phi ship")
    void donTu500k_mienPhi() {
        assertFee("0", "600000", 2);
    }

    @Test
    @DisplayName("Don nho nhung >= 10 san pham -> phi cong kenh 50.000")
    void donNhieuHang_phiCongKenh() {
        assertFee("50000", "100000", 12);
    }

    @Test
    @DisplayName("Vua >= 500k vua >= 10 san pham -> mien phi (uu tien cao nhat thang)")
    void vuaLonVuaNhieu_mienPhi() {
        assertFee("0", "600000", 12);
    }

    @Test
    @DisplayName("Bien: dung 500.000 -> mien phi")
    void bien_dung500k() {
        assertFee("0", "500000", 1);
    }

    @Test
    @DisplayName("Bien: 499.999 -> van tinh phi 30.000")
    void bien_duoi500k() {
        assertFee("30000", "499999", 1);
    }

    @Test
    @DisplayName("Bien: dung 10 san pham -> phi cong kenh")
    void bien_dung10SanPham() {
        assertFee("50000", "100000", 10);
    }

    @Test
    @DisplayName("Bien: 9 san pham -> phi mac dinh")
    void bien_9SanPham() {
        assertFee("30000", "100000", 9);
    }
}
