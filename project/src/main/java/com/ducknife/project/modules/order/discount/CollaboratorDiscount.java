package com.ducknife.project.modules.order.discount;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.role.Role;
import com.ducknife.project.modules.user.User;

@Component
public class CollaboratorDiscount implements DiscountStrategy {
    
    @Override
    public boolean supports(User user, BigDecimal subtotal) {
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            if ("ROLE_COLLABORATOR".equals(role.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override 
    public BigDecimal discountRate() {
        return new BigDecimal("0.1");
    }
}
