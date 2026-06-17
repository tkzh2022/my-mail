package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BizException;
import com.mall.common.util.IdGenerator;
import com.mall.order.entity.Order;
import com.mall.order.entity.Refund;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.mapper.RefundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;

    public Refund createRefund(Long userId, Long orderId, String reason, List<String> evidenceImages) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(40070, "Order not found");
        }
        if (order.getStatus() != 1 && order.getStatus() != 3) {
            throw new BizException(40071, "Order cannot be refunded in current state");
        }

        Refund refund = new Refund();
        refund.setRefundNo(IdGenerator.generateOrderNo());
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setMerchantId(1L);
        refund.setAmount(order.getPayAmount());
        refund.setReason(reason);
        refund.setEvidenceImages(evidenceImages);
        refund.setStatus(0);
        refundMapper.insert(refund);

        order.setStatus(6);
        orderMapper.updateById(order);
        return refund;
    }

    public void merchantApprove(Long merchantId, String refundNo) {
        Refund refund = getByRefundNo(refundNo);
        if (refund.getStatus() != 0) throw new BizException(40072, "Invalid refund state");
        refund.setStatus(1);
        refund.setCompletedAt(LocalDateTime.now());
        refundMapper.updateById(refund);
    }

    public void merchantReject(Long merchantId, String refundNo, String reply) {
        Refund refund = getByRefundNo(refundNo);
        if (refund.getStatus() != 0) throw new BizException(40072, "Invalid refund state");
        refund.setStatus(2);
        refund.setMerchantReply(reply);
        refundMapper.updateById(refund);
    }

    public void adminArbitrate(Long adminId, String refundNo, boolean approve, String decision) {
        Refund refund = getByRefundNo(refundNo);
        refund.setAdminId(adminId);
        refund.setAdminDecision(decision);
        refund.setStatus(approve ? 4 : 5);
        if (approve) refund.setCompletedAt(LocalDateTime.now());
        refundMapper.updateById(refund);
    }

    private Refund getByRefundNo(String refundNo) {
        Refund refund = refundMapper.selectOne(
                new LambdaQueryWrapper<Refund>().eq(Refund::getRefundNo, refundNo));
        if (refund == null) throw new BizException(40073, "Refund not found");
        return refund;
    }
}
