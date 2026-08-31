package com.ducknife.project.modules.orderdetail;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.modules.orderdetail.dto.OrderDetailResponse;
import com.ducknife.project.modules.orderdetail.mapper.OrderDetailMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderDetailMapper orderDetailMapper;

    public List<OrderDetailResponse> getOrderDetails() {
        return orderDetailRepository.findAll().stream()
                .map(orderDetailMapper::toResponse)
                .collect(Collectors.toList());
    }
}
